package org.adhash.sdk.adhashask.utils

import java.security.MessageDigest


class DataEncryptor {

    fun checkIfAdExpected(data: String, expectedHashes: ArrayList<String>) = sha1(data).let(expectedHashes::contains)

    private fun sha1(target: String) = MessageDigest
        .getInstance("SHA-1")
        .digest(target.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}