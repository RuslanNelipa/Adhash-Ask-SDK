package org.adhash.sdk.adhashask.utils

import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import org.adhash.sdk.adhashask.pojo.AdBidderBody
import java.security.MessageDigest
import javax.crypto.Cipher


class DataEncryptor {
    fun getImageFromData(data: String?) = data
        .takeIf { it?.startsWith("data:image") == true }
        ?.substringAfter("base64")
        ?.drop(1)
        ?.let(::base64ToBitmap)

    fun sha1(target: String) = MessageDigest
        .getInstance("SHA-1")
        .digest(target.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })

    @Throws(Exception::class)
    fun aes256(target: String, key: String): String? {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") //todo
        return ""
    }


    private fun base64ToBitmap(base64String: String) = Base64.decode(base64String, Base64.DEFAULT)
        .run { BitmapFactory.decodeByteArray(this, 0, size) }

    fun json(bidder: Any) = Gson().toJson(bidder).toString()
}