package org.adhash.sdk.adhashask.utils;

import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Aes  {
//    public void aes(android.content.Context ctx, String url, String key) {
//        String function = "" +
//                "function sum(textBox1, textBox2){" +
//                "var x=textBox1;" +
//                "var y=textBox2;" +
//                "var sum=0;" +
//                "sum=Number(x)+Number(y);" +
//                "return sum;" +
//                "}";
//        WebView vw = new WebView(ctx);
//        vw.getSettings().setJavaScriptEnabled(true);
////        JSInterface jsiInterface = new JSInterface(vw);
////        vw.addJavascriptInterface(jsiInterface, "JSInterface");
////        vw.loadUrl("javascript:testEcho('Hello World!')");
//
//        decrypt(function, "", "");
//
//        vw.evaluateJavascript( "function sum(textBox1, textBox2){" +
//                "var x=textBox1;" +
//                "var y=textBox2;" +
//                "var sum=0;" +
//                "sum=Number(x)+Number(y);" +
//                "return sum;" +
//                "}", this);
//    }
//
//    @Override
//    public void onReceiveValue(String s) {
//        Log.d("test", "");
//
//    }

//
//    public class JSInterface{
//
//        private WebView mAppView;
//        public JSInterface  (WebView appView) {
//            this.mAppView = appView;
//        }
//
//        public void doEchoTest(String echo){
//            Toast toast = Toast.makeText(mAppView.getContext(), echo, Toast.LENGTH_SHORT);
//            toast.show();
//        }
//    }

    public static class CipherObj{
        String ciphertext;
        String password;
        CipherObj(String ciphertext, String password){
            this.ciphertext = ciphertext;
            this.password = password;
        }

    }
    public static CipherObj decrypt2(String url, String key) {
        CipherObj cipher = new CipherObj(url, key);
        Context cx = Context.enter();
        try {
            // Initialize the standard objects (Object, Function, etc.). This must be done before scripts can be
            // executed. The null parameter tells initStandardObjects
            // to create and return a scope object that we use
            // in later calls.
            Scriptable scope = cx.initStandardObjects();

            // Pass the Stock Java object to the JavaScript context
            Object wrappedStock = Context.javaToJS(cipher, scope);
            ScriptableObject.putProperty(scope, "cipher", wrappedStock);

            // Execute the script
            cx.evaluateString(scope, cipherw, "EvaluationScript", 1, null);
            return cipher;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Exit the Context. This removes the association between the Context and the current thread and is an
            // essential cleanup action. There should be a call to exit for every call to enter.
            Context.exit();
        }
        return null;
    }

    public static String decrypt(String url, String key) {
        CipherObj test = decrypt2(url, key);
        Object[] params = new Object[]{url, key, "256"};
        Context rhino = Context.enter();

        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();
            rhino.evaluateString(scope, cipher, "JavaScript", 1, null);
            Object obj = scope.get("decrypt", scope);

            if (obj instanceof Function) {
                Function jsFunction = (Function) obj;

                Object jsResult = jsFunction.call(rhino, scope, scope, params);
                String result = Context.toString(jsResult);
                return result;
            }
        } catch (Exception e) {
            Log.d("test", "");
        } finally {
            Context.exit();
        }

        return null;
    }

    private static final String cipherw =
                    "        let ciphertext = cipher.ciphertext" +
                    "        let password = cipher.password" +
                            "const nBits = 256;\n" +
                    "        if (![ 128, 192, 256 ].includes(nBits)) throw new Error('Key size is not 128 / 192 / 256');\n" +
                    "        ciphertext = base64Decode(String(ciphertext));\n" +
                    "        password = utf8Encode(String(password));\n" +
                    "\n" +
                    "        const nBytes = nBits/8; // no bytes in key\n" +
                    "        const pwBytes = new Array(nBytes);\n" +
                    "        for (let i=0; i<nBytes; i++) { \n" +
                    "            pwBytes[i] = i<password.length ?  password.charCodeAt(i) : 0;\n" +
                    "        }\n" +
                    "        let key = cipher(pwBytes, keyExpansion(pwBytes));\n" +
                    "        key = key.concat(key.slice(0, nBytes-16)); \n" +
                    "\n" +
                    "        const counterBlock = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ];\n" +
                    "        for (let i=0; i<8; i++) counterBlock[i] = ciphertext.charCodeAt(i);\n" +
                    "\n" +
                    "        const ciphertextBytes = new Array(ciphertext.length-8);\n" +
                    "        for (let i=8; i<ciphertext.length; i++) ciphertextBytes[i-8] = ciphertext.charCodeAt(i);\n" +
                    "\n" +
                    "        const plaintextBytes = nistDecryption(ciphertextBytes, key, counterBlock);\n" +
                    "        const plaintextUtf8 = plaintextBytes.map(i => String.fromCharCode(i)).join('');\n" +
                    "        const plaintext = utf8Decode(plaintextUtf8);\n" +
                    "\n" +
                    "        function base64Decode(str) {\n" +
                    "            if (typeof atob != 'undefined') return atob(str); \n" +
                    "            if (typeof Buffer != 'undefined') return new Buffer(str, 'base64').toString('binary'); \n" +
                    "            throw new Error('No Base64 Decode');\n" +
                    "        }\n" +
                    "\n" +
                    "        function utf8Encode(str) {\n" +
                    "            try {\n" +
                    "              return new TextEncoder().encode(str, 'utf-8').reduce((prev, curr) => prev + String.fromCharCode(curr), '');\n" +
                    "            } catch (e) { \n" +
                    "              return unescape(encodeURIComponent(str)); \n" +
                    "            } \n" +
                    "        }\n" +
                    "        \n" +
                    "        function keyExpansion(key) {\n" +
                    "          const Nb = 4;            \n" +
                    "          const Nk = key.length/4; \n" +
                    "          const Nr = Nk + 6;      \n" +
                    "\n" +
                    "          const w = new Array(Nb*(Nr+1));\n" +
                    "          let temp = new Array(4);\n" +
                    "\n" +
                    "       \n" +
                    "          for (let i=0; i<Nk; i++) {\n" +
                    "              const r = [ key[4*i], key[4*i+1], key[4*i+2], key[4*i+3] ];\n" +
                    "              w[i] = r;\n" +
                    "          }\n" +
                    "\n" +
                    "       \n" +
                    "          for (let i=Nk; i<(Nb*(Nr+1)); i++) {\n" +
                    "              w[i] = new Array(4);\n" +
                    "              for (let t=0; t<4; t++) temp[t] = w[i-1][t];\n" +
                    "             \n" +
                    "              if (i % Nk == 0) {\n" +
                    "                  temp = Aes.subWord(Aes.rotWord(temp));\n" +
                    "                  for (let t=0; t<4; t++) temp[t] ^= Aes.rCon[i/Nk][t];\n" +
                    "              }\n" +
                    "            \n" +
                    "              else if (Nk > 6 && i%Nk == 4) {\n" +
                    "                  temp = Aes.subWord(temp);\n" +
                    "              }\n" +
                    "             \n" +
                    "              for (let t=0; t<4; t++) w[i][t] = w[i-Nk][t] ^ temp[t];\n" +
                    "          }\n" +
                    "\n" +
                    "          return w;\n" +
                    "        }\n" +
                    "\n" +
                    "        function cipher(input, w) {\n" +
                    "            const Nb = 4;\n" +
                    "            const Nr = w.length/Nb - 1;\n" +
                    "\n" +
                    "            let state = [ [], [], [], [] ];\n" +
                    "            for (let i=0; i<4*Nb; i++) state[i%4][Math.floor(i/4)] = input[i];\n" +
                    "\n" +
                    "            state = Aes.addRoundKey(state, w, 0, Nb);\n" +
                    "\n" +
                    "            for (let round=1; round<Nr; round++) {\n" +
                    "                state = Aes.subBytes(state, Nb);\n" +
                    "                state = Aes.shiftRows(state, Nb);\n" +
                    "                state = Aes.mixColumns(state, Nb);\n" +
                    "                state = Aes.addRoundKey(state, w, round, Nb);\n" +
                    "            }\n" +
                    "\n" +
                    "            state = Aes.subBytes(state, Nb);\n" +
                    "            state = Aes.shiftRows(state, Nb);\n" +
                    "            state = Aes.addRoundKey(state, w, Nr, Nb);\n" +
                    "\n" +
                    "            const output = new Array(4*Nb);\n" +
                    "            for (let i=0; i<4*Nb; i++) output[i] = state[i%4][Math.floor(i/4)];\n" +
                    "\n" +
                    "            return output;\n" +
                    "        }\n" +
                    "\n" +
                    "        function nistDecryption(ciphertext, key, counterBlock) {\n" +
                    "            const blockSize = 16;\n" +
                    "            const keySchedule = Aes.keyExpansion(key);\n" +
                    "            const blockCount = Math.ceil(ciphertext.length/blockSize);\n" +
                    "            const plaintext = new Array(ciphertext.length);\n" +
                    "\n" +
                    "            for (let b=0; b<blockCount; b++) {\n" +
                    "                const cipherCntr = Aes.cipher(counterBlock, keySchedule);\n" +
                    "                const blockLength = b<blockCount-1 ? blockSize : (ciphertext.length-1)%blockSize + 1;\n" +
                    "                for (let i=0; i<blockLength; i++) {\n" +
                    "                    plaintext[b*blockSize + i] = cipherCntr[i] ^ ciphertext[b*blockSize + i];\n" +
                    "                }\n" +
                    "                counterBlock[blockSize-1]++;\n" +
                    "                for (let i=blockSize-1; i>=8; i--) {\n" +
                    "                    counterBlock[i-1] += counterBlock[i] >> 8;\n" +
                    "                    counterBlock[i] &= 0xff;\n" +
                    "                }\n" +
                    "                if (typeof WorkerGlobalScope != 'undefined' && self instanceof WorkerGlobalScope) {\n" +
                    "                    if (b%1000 == 0) self.postMessage({ progress: b/blockCount });\n" +
                    "                }\n" +
                    "            }\n" +
                    "\n" +
                    "            return plaintext;\n" +
                    "        }\n" +
                    "\n" +
                    "        function utf8Decode(str) {\n" +
                    "            try {\n" +
                    "                return new TextEncoder().decode(str, 'utf-8').reduce((prev, curr) => prev + String.fromCharCode(curr), '');\n" +
                    "            } catch (e) { \n" +
                    "                return decodeURIComponent(escape(str)); \n" +
                    "            }\n" +
                    "        }\n" +
                    "        return plaintext;\n";

    private static final String cipher =
            "function decrypt(ciphertext, password) {\n" +
                    "        const nBits = 256;\n" +
                    "        if (![ 128, 192, 256 ].includes(nBits)) throw new Error('Key size is not 128 / 192 / 256');\n" +
                    "        ciphertext = base64Decode(String(ciphertext));\n" +
                    "        password = utf8Encode(String(password));\n" +
                    "\n" +
                    "        const nBytes = nBits/8; // no bytes in key\n" +
                    "        const pwBytes = new Array(nBytes);\n" +
                    "        for (let i=0; i<nBytes; i++) { \n" +
                    "            pwBytes[i] = i<password.length ?  password.charCodeAt(i) : 0;\n" +
                    "        }\n" +
                    "        let key = cipher(pwBytes, keyExpansion(pwBytes));\n" +
                    "        key = key.concat(key.slice(0, nBytes-16)); \n" +
                    "\n" +
                    "        const counterBlock = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ];\n" +
                    "        for (let i=0; i<8; i++) counterBlock[i] = ciphertext.charCodeAt(i);\n" +
                    "\n" +
                    "        const ciphertextBytes = new Array(ciphertext.length-8);\n" +
                    "        for (let i=8; i<ciphertext.length; i++) ciphertextBytes[i-8] = ciphertext.charCodeAt(i);\n" +
                    "\n" +
                    "        const plaintextBytes = nistDecryption(ciphertextBytes, key, counterBlock);\n" +
                    "        const plaintextUtf8 = plaintextBytes.map(i => String.fromCharCode(i)).join('');\n" +
                    "        const plaintext = utf8Decode(plaintextUtf8);\n" +
                    "\n" +
                    "        function base64Decode(str) {\n" +
                    "            if (typeof atob != 'undefined') return atob(str); \n" +
                    "            if (typeof Buffer != 'undefined') return new Buffer(str, 'base64').toString('binary'); \n" +
                    "            throw new Error('No Base64 Decode');\n" +
                    "        }\n" +
                    "\n" +
                    "        function utf8Encode(str) {\n" +
                    "            try {\n" +
                    "              return new TextEncoder().encode(str, 'utf-8').reduce((prev, curr) => prev + String.fromCharCode(curr), '');\n" +
                    "            } catch (e) { \n" +
                    "              return unescape(encodeURIComponent(str)); \n" +
                    "            } \n" +
                    "        }\n" +
                    "        \n" +
                    "        function keyExpansion(key) {\n" +
                    "          const Nb = 4;            \n" +
                    "          const Nk = key.length/4; \n" +
                    "          const Nr = Nk + 6;      \n" +
                    "\n" +
                    "          const w = new Array(Nb*(Nr+1));\n" +
                    "          let temp = new Array(4);\n" +
                    "\n" +
                    "       \n" +
                    "          for (let i=0; i<Nk; i++) {\n" +
                    "              const r = [ key[4*i], key[4*i+1], key[4*i+2], key[4*i+3] ];\n" +
                    "              w[i] = r;\n" +
                    "          }\n" +
                    "\n" +
                    "       \n" +
                    "          for (let i=Nk; i<(Nb*(Nr+1)); i++) {\n" +
                    "              w[i] = new Array(4);\n" +
                    "              for (let t=0; t<4; t++) temp[t] = w[i-1][t];\n" +
                    "             \n" +
                    "              if (i % Nk == 0) {\n" +
                    "                  temp = Aes.subWord(Aes.rotWord(temp));\n" +
                    "                  for (let t=0; t<4; t++) temp[t] ^= Aes.rCon[i/Nk][t];\n" +
                    "              }\n" +
                    "            \n" +
                    "              else if (Nk > 6 && i%Nk == 4) {\n" +
                    "                  temp = Aes.subWord(temp);\n" +
                    "              }\n" +
                    "           \n" +
                    "              for (let t=0; t<4; t++) w[i][t] = w[i-Nk][t] ^ temp[t];\n" +
                    "          }\n" +
                    "\n" +
                    "          return w;\n" +
                    "        }\n" +
                    "\n" +
                    "        function cipher(input, w) {\n" +
                    "            const Nb = 4;\n" +
                    "            const Nr = w.length/Nb - 1;\n" +
                    "\n" +
                    "            let state = [ [], [], [], [] ];\n" +
                    "            for (let i=0; i<4*Nb; i++) state[i%4][Math.floor(i/4)] = input[i];\n" +
                    "\n" +
                    "            state = Aes.addRoundKey(state, w, 0, Nb);\n" +
                    "\n" +
                    "            for (let round=1; round<Nr; round++) {\n" +
                    "                state = Aes.subBytes(state, Nb);\n" +
                    "                state = Aes.shiftRows(state, Nb);\n" +
                    "                state = Aes.mixColumns(state, Nb);\n" +
                    "                state = Aes.addRoundKey(state, w, round, Nb);\n" +
                    "            }\n" +
                    "\n" +
                    "            state = Aes.subBytes(state, Nb);\n" +
                    "            state = Aes.shiftRows(state, Nb);\n" +
                    "            state = Aes.addRoundKey(state, w, Nr, Nb);\n" +
                    "\n" +
                    "            const output = new Array(4*Nb);\n" +
                    "            for (let i=0; i<4*Nb; i++) output[i] = state[i%4][Math.floor(i/4)];\n" +
                    "\n" +
                    "            return output;\n" +
                    "        }\n" +
                    "\n" +
                    "        function nistDecryption(ciphertext, key, counterBlock) {\n" +
                    "            const blockSize = 16;\n" +
                    "            const keySchedule = Aes.keyExpansion(key);\n" +
                    "            const blockCount = Math.ceil(ciphertext.length/blockSize);\n" +
                    "            const plaintext = new Array(ciphertext.length);\n" +
                    "\n" +
                    "            for (let b=0; b<blockCount; b++) {\n" +
                    "                const cipherCntr = Aes.cipher(counterBlock, keySchedule);\n" +
                    "                const blockLength = b<blockCount-1 ? blockSize : (ciphertext.length-1)%blockSize + 1;\n" +
                    "                for (let i=0; i<blockLength; i++) {\n" +
                    "                    plaintext[b*blockSize + i] = cipherCntr[i] ^ ciphertext[b*blockSize + i];\n" +
                    "                }\n" +
                    "                counterBlock[blockSize-1]++;\n" +
                    "                for (let i=blockSize-1; i>=8; i--) {\n" +
                    "                    counterBlock[i-1] += counterBlock[i] >> 8;\n" +
                    "                    counterBlock[i] &= 0xff;\n" +
                    "                }\n" +
                    "                if (typeof WorkerGlobalScope != 'undefined' && self instanceof WorkerGlobalScope) {\n" +
                    "                    if (b%1000 == 0) self.postMessage({ progress: b/blockCount });\n" +
                    "                }\n" +
                    "            }\n" +
                    "\n" +
                    "            return plaintext;\n" +
                    "        }\n" +
                    "\n" +
                    "        function utf8Decode(str) {\n" +
                    "            try {\n" +
                    "                return new TextEncoder().decode(str, 'utf-8').reduce((prev, curr) => prev + String.fromCharCode(curr), '');\n" +
                    "            } catch (e) { \n" +
                    "                return decodeURIComponent(escape(str)); \n" +
                    "            }\n" +
                    "        }\n" +
                    "        return plaintext;\n" +
                    "    }";
}
