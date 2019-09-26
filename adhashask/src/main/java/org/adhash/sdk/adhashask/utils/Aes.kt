package org.adhash.sdk.adhashask.utils

import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebView

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable

object Aes {
    private fun cipher(input: String, w: String) = "function cipher($input, $w) {\n" +
            "        const Nb = 4;               // block size (in words): no of columns in state (fixed at 4 for AES)\n" +
            "        const Nr = w.length/Nb - 1; // no of rounds: 10/12/14 for 128/192/256-bit keys\n" +
            "\n" +
            "        let state = [ [], [], [], [] ];  // initialise 4×Nb byte-array 'state' with input [§3.4]\n" +
            "        for (let i=0; i<4*Nb; i++) state[i%4][Math.floor(i/4)] = input[i];\n" +
            "\n" +
            "        state = addRoundKey(state, w, 0, Nb);\n" +
            "\n" +
            "        for (let round=1; round<Nr; round++) {\n" +
            "            state = subBytes(state, Nb);\n" +
            "            state = shiftRows(state, Nb);\n" +
            "            state = mixColumns(state, Nb);\n" +
            "            state = addRoundKey(state, w, round, Nb);\n" +
            "        }\n" +
            "\n" +
            "        state = subBytes(state, Nb);\n" +
            "        state = shiftRows(state, Nb);\n" +
            "        state = addRoundKey(state, w, Nr, Nb);\n" +
            "\n" +
            "        const output = new Array(4*Nb);  // convert state to 1-d array before returning [§3.4]\n" +
            "        for (let i=0; i<4*Nb; i++) output[i] = state[i%4][Math.floor(i/4)];\n" +
            "\n" +
            "        return output;\n" +
            "    }"

    private fun addRoundKey(state: String, w: String, rnd: String, Nb: String)= "function addRoundKey($state, $w, $rnd, $Nb) {\n" +
            "        for (let r=0; r<4; r++) {\n" +
            "            for (let c=0; c<Nb; c++) state[r][c] ^= w[rnd*4+c][r];\n" +
            "        }\n" +
            "        return state;\n" +
            "    }"

    private fun subBytes(s: String, Nb: String)= "function subBytes($s, $Nb) {\n" +
            "        for (let r=0; r<4; r++) {\n" +
            "            for (let c=0; c<Nb; c++) s[r][c] = Aes.sBox[s[r][c]];\n" +
            "        }\n" +
            "        return s;\n" +
            "    }"

    private fun shiftRows(s: String, Nb: String) = "function  shiftRows($s, $Nb) {\n" +
            "        const t = new Array(4);\n" +
            "        for (let r=1; r<4; r++) {\n" +
            "            for (let c=0; c<4; c++) t[c] = s[r][(c+r)%Nb];  \n" +
            "            for (let c=0; c<4; c++) s[r][c] = t[c];       \n" +
            "        }          \n" +
            "        return s; \n" +
            "    }"

    private fun mixColumns(s: String, Nb: String) ="function mixColumns($s, $Nb) {\n" +
            "        for (let c=0; c<Nb; c++) {\n" +
            "            const a = new Array(Nb);  // 'a' is a copy of the current column from 's'\n" +
            "            const b = new Array(Nb);  // 'b' is a•{02} in GF(2^8)\n" +
            "            for (let r=0; r<4; r++) {\n" +
            "                a[r] = s[r][c];\n" +
            "                b[r] = s[r][c]&0x80 ? s[r][c]<<1 ^ 0x011b : s[r][c]<<1;\n" +
            "            }\n" +
            "            // a[n] ^ b[n] is a•{03} in GF(2^8)\n" +
            "            s[0][c] = b[0] ^ a[1] ^ b[1] ^ a[2] ^ a[3]; // {02}•a0 + {03}•a1 + a2 + a3\n" +
            "            s[1][c] = a[0] ^ b[1] ^ a[2] ^ b[2] ^ a[3]; // a0 • {02}•a1 + {03}•a2 + a3\n" +
            "            s[2][c] = a[0] ^ a[1] ^ b[2] ^ a[3] ^ b[3]; // a0 + a1 + {02}•a2 + {03}•a3\n" +
            "            s[3][c] = a[0] ^ b[0] ^ a[1] ^ a[2] ^ b[3]; // {03}•a0 + a1 + a2 + {02}•a3\n" +
            "        }\n" +
            "        return s;\n" +
            "    }"

    private fun base64Decode(str: String) = "function base64Decode($str) {\n" +
            "        if (typeof atob != 'undefined') return atob(str); // browser\n" +
            "        if (typeof Buffer != 'undefined') return new Buffer(str, 'base64').toString('binary'); // Node.js\n" +
            "        throw new Error('No Base64 Decode');\n" +
            "    }"


    private fun utf8Encode(str: String) = "function utf8Encode($str) {\n" +
            "        try {\n" +
            "            return new TextEncoder().encode(str, 'utf-8').reduce((prev, curr) => prev + String.fromCharCode(curr), '');\n" +
            "        } catch (e) { // no TextEncoder available?\n" +
            "            return unescape(encodeURIComponent(str)); // monsur.hossa.in/2012/07/20/utf-8-in-javascript.html\n" +
            "        }\n" +
            "    }"

    private fun utf8Decode(str: String) = "function utf8Decode($str) {\n" +
            "        try {\n" +
            "            return new TextEncoder().decode(str, 'utf-8').reduce((prev, curr) => prev + String.fromCharCode(curr), '');\n" +
            "        } catch (e) { // no TextEncoder available?\n" +
            "            return decodeURIComponent(escape(str)); // monsur.hossa.in/2012/07/20/utf-8-in-javascript.html\n" +
            "        }\n" +
            "    }"

    private fun subWord(w: String) = "static subWord($w) {\n" +
            "        for (let i=0; i<4; i++) w[i] = Aes.sBox[w[i]];\n" +
            "        return w;\n" +
            "    }"

    private fun rotWord(w: String) = "static rotWord($w) {\n" +
            "        const tmp = w[0];\n" +
            "        for (let i=0; i<3; i++) w[i] = w[i+1];\n" +
            "        w[3] = tmp;\n" +
            "        return w;\n" +
            "    }"

    private fun decrypt(a:String,b:String,c:String) = "function decrypt($a,$b,$c){function d(a,b){const c=4,d=b.length/4-1;let e=[[],[],[],[]];for(let c=0;c<16;c++)e[c%4][Math.floor(c/4)]=a[c];e=i(e,b,0,4);for(let j=1;j<d;j++)e=f(e,c),e=g(e,c),e=h(e,c),e=i(e,b,j,c);e=f(e,4),e=g(e,4),e=i(e,b,d,4);const j=Array(16);for(let c=0;c<16;c++)j[c]=e[c%4][Math.floor(c/4)];return j}function e(a){const b=a.length/4,c=b+6,d=Array(4*(c+1));let e=[,,,,];for(let c=0;c<b;c++){const b=[a[4*c],a[4*c+1],a[4*c+2],a[4*c+3]];d[c]=b}for(let f=b;f<4*(c+1);f++){d[f]=[,,,,];for(let a=0;4>a;a++)e[a]=d[f-1][a];if(0==f%b){e=j(k(e));for(let a=0;4>a;a++)e[a]^=n[f/b][a]}else 6<b&&4==f%b&&(e=j(e));for(let a=0;4>a;a++)d[f][a]=d[f-b][a]^e[a]}return d}function f(a,b){for(let d=0;4>d;d++)for(let e=0;e<b;e++)a[d][e]=m[a[d][e]];return a}function g(a,b){const d=[,,,,];for(let e=1;4>e;e++){for(let f=0;4>f;f++)d[f]=a[e][(f+e)%b];for(let b=0;4>b;b++)a[e][b]=d[b]}return a}function h(d,e){for(let f=0;f<e;f++){const c=Array(e),a=Array(e);for(let b=0;4>b;b++)c[b]=d[b][f],a[b]=128&d[b][f]?283^d[b][f]<<1:d[b][f]<<1;d[0][f]=a[0]^c[1]^a[1]^c[2]^c[3],d[1][f]=c[0]^a[1]^c[2]^a[2]^c[3],d[2][f]=c[0]^c[1]^a[2]^c[3]^a[3],d[3][f]=c[0]^a[0]^c[1]^c[2]^a[3]}return d}function i(a,b,d,e){for(let f=0;4>f;f++)for(let g=0;g<e;g++)a[f][g]^=b[4*d+g][f];return a}function j(a){for(let b=0;4>b;b++)a[b]=m[a[b]];return a}function k(a){const b=a[0];for(let b=0;3>b;b++)a[b]=a[b+1];return a[3]=b,a}function l(a,b,c){const f=16,g=e(b),h=Math.ceil(a.length/16),j=Array(a.length);for(let e=0;e<h;e++){const b=d(c,g),k=e<h-1?f:(a.length-1)%f+1;for(let c=0;c<k;c++)j[e*f+c]=b[c]^a[e*f+c];c[15]++;for(let a=15;8<=a;a--)c[a-1]+=c[a]>>8,c[a]&=255;\"undefined\"!=typeof WorkerGlobalScope&&self instanceof WorkerGlobalScope&&0==e%1e3&&self.postMessage({progress:e/h})}return j}const m=[99,124,119,123,242,107,111,197,48,1,103,43,254,215,171,118,202,130,201,125,250,89,71,240,173,212,162,175,156,164,114,192,183,253,147,38,54,63,247,204,52,165,229,241,113,216,49,21,4,199,35,195,24,150,5,154,7,18,128,226,235,39,178,117,9,131,44,26,27,110,90,160,82,59,214,179,41,227,47,132,83,209,0,237,32,252,177,91,106,203,190,57,74,76,88,207,208,239,170,251,67,77,51,133,69,249,2,127,80,60,159,168,81,163,64,143,146,157,56,245,188,182,218,33,16,255,243,210,205,12,19,236,95,151,68,23,196,167,126,61,100,93,25,115,96,129,79,220,34,42,144,136,70,238,184,20,222,94,11,219,224,50,58,10,73,6,36,92,194,211,172,98,145,149,228,121,231,200,55,109,141,213,78,169,108,86,244,234,101,122,174,8,186,120,37,46,28,166,180,198,232,221,116,31,75,189,139,138,112,62,181,102,72,3,246,14,97,53,87,185,134,193,29,158,225,248,152,17,105,217,142,148,155,30,135,233,206,85,40,223,140,161,137,13,191,230,66,104,65,153,45,15,176,84,187,22],n=[[0,0,0,0],[1,0,0,0],[2,0,0,0],[4,0,0,0],[8,0,0,0],[16,0,0,0],[32,0,0,0],[64,0,0,0],[128,0,0,0],[27,0,0,0],[54,0,0,0]];if(![128,192,256].includes(c))throw new Error(\"Key size is not 128 / 192 / 256\");a=function(a){if(\"undefined\"!=typeof atob)return atob(a);if(\"undefined\"!=typeof Buffer)return new Buffer(a,\"base64\").toString(\"binary\");throw new Error(\"No Base64 Decode\")}(a+\"\"),b=function(a){try{return new TextEncoder().encode(a,\"utf-8\").reduce((a,b)=>a+String.fromCharCode(b),\"\")}catch(b){return unescape(encodeURIComponent(a))}}(b+\"\");const o=c/8,p=Array(o);for(let d=0;d<o;d++)p[d]=d<b.length?b.charCodeAt(d):0;let q=d(p,e(p));q=q.concat(q.slice(0,o-16));const r=[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];for(let d=0;8>d;d++)r[d]=a.charCodeAt(d);const s=Array(a.length-8);for(let d=8;d<a.length;d++)s[d-8]=a.charCodeAt(d);const t=l(s,q,r),u=t.map(a=>String.fromCharCode(a)).join(\"\"),v=function(a){try{return new TextEncoder().decode(a,\"utf-8\").reduce((a,b)=>a+String.fromCharCode(b),\"\")}catch(b){return decodeURIComponent(escape(a))}}(u);return v}"

    private fun decryptFull(ciphertext: String, password: String, nBits: String) = "function decrypt(ciphertext, password, nBits) {\n" +
            "\n" +
            "  function cipher(input, w) {\n" +
            "    const Nb = 4; // block size (in words): no of columns in state (fixed at 4 for AES)\n" +
            "    const Nr = w.length / Nb - 1; // no of rounds: 10/12/14 for 128/192/256-bit keys\n" +
            "\n" +
            "    let state = [[], [], [], []]; // initialise 4×Nb byte-array 'state' with input [§3.4]\n" +
            "    for (let i = 0; i < 4 * Nb; i++) state[i % 4][Math.floor(i / 4)] = input[i];\n" +
            "\n" +
            "    state = addRoundKey(state, w, 0, Nb);\n" +
            "\n" +
            "    for (let round = 1; round < Nr; round++) {\n" +
            "      state = subBytes(state, Nb);\n" +
            "      state = shiftRows(state, Nb);\n" +
            "      state = mixColumns(state, Nb);\n" +
            "      state = addRoundKey(state, w, round, Nb);\n" +
            "    }\n" +
            "\n" +
            "    state = subBytes(state, Nb);\n" +
            "    state = shiftRows(state, Nb);\n" +
            "    state = addRoundKey(state, w, Nr, Nb);\n" +
            "\n" +
            "    const output = new Array(4 * Nb);\n" +
            "    for (let i = 0; i < 4 * Nb; i++) output[i] = state[i % 4][Math.floor(i / 4)];\n" +
            "\n" +
            "    return output;\n" +
            "  }\n" +
            "\n" +
            "  function keyExpansion(key) {\n" +
            "    const Nb = 4;\n" +
            "    const Nk = key.length / 4;\n" +
            "    const Nr = Nk + 6;\n" +
            "\n" +
            "    const w = new Array(Nb * (Nr + 1));\n" +
            "    let temp = new Array(4);\n" +
            "\n" +
            "\n" +
            "    for (let i = 0; i < Nk; i++) {\n" +
            "      const r = [key[4 * i], key[4 * i + 1], key[4 * i + 2], key[4 * i + 3]];\n" +
            "      w[i] = r;\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    for (let i = Nk; i < (Nb * (Nr + 1)); i++) {\n" +
            "      w[i] = new Array(4);\n" +
            "      for (let t = 0; t < 4; t++) temp[t] = w[i - 1][t];\n" +
            "\n" +
            "      if (i % Nk == 0) {\n" +
            "        temp = subWord(rotWord(temp));\n" +
            "        for (let t = 0; t < 4; t++) temp[t] ^= rCon[i / Nk][t];\n" +
            "      } else if (Nk > 6 && i % Nk == 4) {\n" +
            "        temp = subWord(temp);\n" +
            "      }\n" +
            "\n" +
            "      for (let t = 0; t < 4; t++) w[i][t] = w[i - Nk][t] ^ temp[t];\n" +
            "    }\n" +
            "\n" +
            "    return w;\n" +
            "  }\n" +
            "\n" +
            "  function subBytes(s, Nb) {\n" +
            "    for (let r = 0; r < 4; r++) {\n" +
            "      for (let c = 0; c < Nb; c++) s[r][c] = sBox[s[r][c]];\n" +
            "    }\n" +
            "    return s;\n" +
            "  }\n" +
            "\n" +
            "  function shiftRows(s, Nb) {\n" +
            "    const t = new Array(4);\n" +
            "    for (let r = 1; r < 4; r++) {\n" +
            "      for (let c = 0; c < 4; c++) t[c] = s[r][(c + r) % Nb];\n" +
            "      for (let c = 0; c < 4; c++) s[r][c] = t[c];\n" +
            "    }\n" +
            "    return s;\n" +
            "  }\n" +
            "\n" +
            "  function mixColumns(s, Nb) {\n" +
            "    for (let c = 0; c < Nb; c++) {\n" +
            "      const a = new Array(Nb);\n" +
            "      const b = new Array(Nb);\n" +
            "      for (let r = 0; r < 4; r++) {\n" +
            "        a[r] = s[r][c];\n" +
            "        b[r] = s[r][c] & 0x80 ? s[r][c] << 1 ^ 0x011b : s[r][c] << 1;\n" +
            "      }\n" +
            "\n" +
            "      s[0][c] = b[0] ^ a[1] ^ b[1] ^ a[2] ^ a[3];\n" +
            "      s[1][c] = a[0] ^ b[1] ^ a[2] ^ b[2] ^ a[3];\n" +
            "      s[2][c] = a[0] ^ a[1] ^ b[2] ^ a[3] ^ b[3];\n" +
            "      s[3][c] = a[0] ^ b[0] ^ a[1] ^ a[2] ^ b[3];\n" +
            "    }\n" +
            "    return s;\n" +
            "  }\n" +
            "\n" +
            "  function addRoundKey(state, w, rnd, Nb) {\n" +
            "    for (let r = 0; r < 4; r++) {\n" +
            "      for (let c = 0; c < Nb; c++) state[r][c] ^= w[rnd * 4 + c][r];\n" +
            "    }\n" +
            "    return state;\n" +
            "  }\n" +
            "\n" +
            "  function subWord(w) {\n" +
            "    for (let i = 0; i < 4; i++) w[i] = sBox[w[i]];\n" +
            "    return w;\n" +
            "  }\n" +
            "\n" +
            "  function rotWord(w) {\n" +
            "    const tmp = w[0];\n" +
            "    for (let i = 0; i < 3; i++) w[i] = w[i + 1];\n" +
            "    w[3] = tmp;\n" +
            "    return w;\n" +
            "  }\n" +
            "\n" +
            "  function base64Decode(str) {\n" +
            "    if (typeof atob != 'undefined') return atob(str);\n" +
            "    if (typeof Buffer != 'undefined') return new Buffer(str, 'base64').toString('binary');\n" +
            "    throw new Error('No Base64 Decode');\n" +
            "  }\n" +
            "\n" +
            "  function utf8Encode(str) {\n" +
            "    try {\n" +
            "      return new TextEncoder().encode(str, 'utf-8').reduce((prev, curr) => prev + String.fromCharCode(curr), '');\n" +
            "    } catch (e) {\n" +
            "      return unescape(encodeURIComponent(str));\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  function utf8Decode(str) {\n" +
            "    try {\n" +
            "      return new TextEncoder().decode(str, 'utf-8').reduce((prev, curr) => prev + String.fromCharCode(curr), '');\n" +
            "    } catch (e) {\n" +
            "      return decodeURIComponent(escape(str));\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  function nistDecryption(ciphertext, key, counterBlock) {\n" +
            "    const blockSize = 16;\n" +
            "\n" +
            "    // generate key schedule - an expansion of the key into distinct Key Rounds for each round\n" +
            "    const keySchedule = keyExpansion(key);\n" +
            "\n" +
            "    const blockCount = Math.ceil(ciphertext.length / blockSize);\n" +
            "    const plaintext = new Array(ciphertext.length);\n" +
            "\n" +
            "    for (let b = 0; b < blockCount; b++) {\n" +
            "      // ---- decrypt counter block; Oⱼ = CIPHₖ(Tⱼ) ----\n" +
            "      const cipherCntr = cipher(counterBlock, keySchedule);\n" +
            "\n" +
            "      // block size is reduced on final block\n" +
            "      const blockLength = b < blockCount - 1 ? blockSize : (ciphertext.length - 1) % blockSize + 1;\n" +
            "\n" +
            "      // ---- xor ciphertext with ciphered counter byte-by-byte; Pⱼ = Cⱼ ⊕ Oⱼ ----\n" +
            "      for (let i = 0; i < blockLength; i++) {\n" +
            "        plaintext[b * blockSize + i] = cipherCntr[i] ^ ciphertext[b * blockSize + i];\n" +
            "      }\n" +
            "\n" +
            "      // increment counter block (counter in 2nd 8 bytes of counter block, big-endian)\n" +
            "      counterBlock[blockSize - 1]++;\n" +
            "      // and propagate carry digits\n" +
            "      for (let i = blockSize - 1; i >= 8; i--) {\n" +
            "        counterBlock[i - 1] += counterBlock[i] >> 8;\n" +
            "        counterBlock[i] &= 0xff;\n" +
            "      }\n" +
            "\n" +
            "      // if within web worker, announce progress every 1000 blocks (roughly every 50ms)\n" +
            "      if (typeof WorkerGlobalScope != 'undefined' && self instanceof WorkerGlobalScope) {\n" +
            "        if (b % 1000 == 0) self.postMessage({ progress: b / blockCount });\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    return plaintext;\n" +
            "  }\n" +
            "\n" +
            "  const sBox = [\n" +
            "          0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,\n" +
            "          0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,\n" +
            "          0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,\n" +
            "          0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,\n" +
            "          0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,\n" +
            "          0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,\n" +
            "          0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,\n" +
            "          0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,\n" +
            "          0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,\n" +
            "          0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,\n" +
            "          0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,\n" +
            "          0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,\n" +
            "          0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,\n" +
            "          0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,\n" +
            "          0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,\n" +
            "          0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16,\n" +
            "      ];\n" +
            "\n" +
            "  const rCon = [\n" +
            "        [0x00, 0x00, 0x00, 0x00],\n" +
            "        [0x01, 0x00, 0x00, 0x00],\n" +
            "        [0x02, 0x00, 0x00, 0x00],\n" +
            "        [0x04, 0x00, 0x00, 0x00],\n" +
            "        [0x08, 0x00, 0x00, 0x00],\n" +
            "        [0x10, 0x00, 0x00, 0x00],\n" +
            "        [0x20, 0x00, 0x00, 0x00],\n" +
            "        [0x40, 0x00, 0x00, 0x00],\n" +
            "        [0x80, 0x00, 0x00, 0x00],\n" +
            "        [0x1b, 0x00, 0x00, 0x00],\n" +
            "        [0x36, 0x00, 0x00, 0x00],\n" +
            "      ];\n" +
            "\n" +
            "  if (![128, 192, 256].includes(nBits)) throw new Error('Key size is not 128 / 192 / 256');\n" +
            "  ciphertext = base64Decode(String(ciphertext));\n" +
            "  password = utf8Encode(String(password));\n" +
            "\n" +
            "  // use AES to encrypt password (mirroring encrypt routine)\n" +
            "  const nBytes = nBits / 8; // no bytes in key\n" +
            "  const pwBytes = new Array(nBytes);\n" +
            "  for (let i = 0; i < nBytes; i++) { // use 1st nBytes chars of password for key\n" +
            "    pwBytes[i] = i < password.length ? password.charCodeAt(i) : 0;\n" +
            "  }\n" +
            "  let key = cipher(pwBytes, keyExpansion(pwBytes));\n" +
            "  key = key.concat(key.slice(0, nBytes - 16)); // expand key to 16/24/32 bytes long\n" +
            "\n" +
            "  // recover nonce from 1st 8 bytes of ciphertext into 1st 8 bytes of counter block\n" +
            "  const counterBlock = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];\n" +
            "  for (let i = 0; i < 8; i++) counterBlock[i] = ciphertext.charCodeAt(i);\n" +
            "\n" +
            "  // convert ciphertext to byte array (skipping past initial 8 bytes)\n" +
            "  const ciphertextBytes = new Array(ciphertext.length - 8);\n" +
            "  for (let i = 8; i < ciphertext.length; i++) ciphertextBytes[i - 8] = ciphertext.charCodeAt(i);\n" +
            "\n" +
            "  // ------------ perform decryption ------------\n" +
            "  const plaintextBytes = nistDecryption(ciphertextBytes, key, counterBlock);\n" +
            "\n" +
            "  // convert byte array to (utf-8) plaintext string\n" +
            "  const plaintextUtf8 = plaintextBytes.map(i => String.fromCharCode(i)).join('');\n" +
            "\n" +
            "  // decode from UTF8 back to Unicode multi-byte chars\n" +
            "  const plaintext = utf8Decode(plaintextUtf8);\n" +
            "\n" +
            "  return plaintext;\n" +
            "}"

    fun decryptWv(context: android.content.Context, url: String, key: String){
        val wv = WebView(context)
        wv.settings.javaScriptEnabled = true
        wv.evaluateJavascript(decryptFull(url, key, "256")) {
            Log.d("T", "")
        }
    }

    fun decrypt(url: String, key: String): String? {
        val params = arrayOf(url, key, 256)
        val rhino = Context.enter()

        rhino.optimizationLevel = -1
        try {
            val scope = rhino.initStandardObjects()
            rhino.evaluateString(scope, "", "JavaScript", 1, null)
            val obj = scope.get("decrypt", scope)

            if (obj is Function) {

                val jsResult = obj.call(rhino, scope, scope, params)
                return Context.toString(jsResult)
            }
        } catch (e: Exception) {
            Log.d("test", "")
        } finally {
            Context.exit()
        }

        return null
    }
}
