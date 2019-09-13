package org.adhash.sdk.adhashask.utils

import android.graphics.BitmapFactory
import android.util.Base64
import java.security.MessageDigest


class DataEncryptor {

    fun checkIfAdExpected(data: String, expectedHashes: ArrayList<String>) = sha1(data).let(expectedHashes::contains)

    fun getImageFromData(data: String) = data
        .takeIf { it.startsWith("data:image") }
        ?.substringAfter("base64")
        ?.drop(1)
        ?.let(::base64ToBitmap)

    private fun sha1(target: String) = MessageDigest
        .getInstance("SHA-1")
        .digest(target.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })


    private fun base64ToBitmap(base64String: String) = Base64.decode(base64String, Base64.DEFAULT)
        .run { BitmapFactory.decodeByteArray(this, 0, size) }
}