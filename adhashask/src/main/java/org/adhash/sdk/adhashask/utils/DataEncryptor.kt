package org.adhash.sdk.adhashask.utils

import java.security.MessageDigest
import kotlin.experimental.and


class DataEncryptor  {

    fun checkIfExist(data: String, expectedHashes: ArrayList<String>) : Boolean {
        val encodedData = encryptData(data)
        return expectedHashes.contains(encodedData)
    }

    private fun encryptData(data: String) : String {
        val md = MessageDigest.getInstance("SHA-1")
        md.update(data.toByteArray(charset("iso-8859-1")), 0, data.length)
        val sha1hash = md.digest()
        return convertToHex(sha1hash)
    }

    private fun convertToHex(data: ByteArray): String {
        val buf = StringBuilder()
//        for (b in data) {
//            var halfByte = b.ushr(4) and 0x0F //todo add ushr extension
//            var twoHalfs = 0
//            do {
//                buf.append(if (0 <= halfByte && halfByte <= 9) '0' + halfByte else 'a' + (halfByte - 10))
//                halfByte = b and 0x0F
//            } while (twoHalfs++ < 1)
//        }
        return buf.toString()
    }
}