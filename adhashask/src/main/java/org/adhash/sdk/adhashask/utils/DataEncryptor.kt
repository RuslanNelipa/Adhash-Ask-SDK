package org.adhash.sdk.adhashask.utils

import java.security.MessageDigest


class DataEncryptor  {

    fun checkIfAdExpected(data: String, expectedHashes: ArrayList<String>) : Boolean {
        val hash = sha1(data)
        return expectedHashes.contains(hash)
    }

    private fun sha1(target: String): String {
        val bytes = target.toByteArray()
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}