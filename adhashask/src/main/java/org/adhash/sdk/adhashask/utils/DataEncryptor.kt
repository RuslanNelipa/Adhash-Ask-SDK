package org.adhash.sdk.adhashask.utils

import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DataEncryptor(private val gson: Gson) {

    fun getImageFromData(data: String?) = data
        .takeIf { it?.startsWith("data:image") == true }
        ?.substringAfter("base64")
        ?.drop(1)
        ?.let(::base64ToBitmap)

    fun sha1(target: String) = MessageDigest
        .getInstance("SHA-1")
        .digest(target.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })

    fun aes256(strToDecrypt: String, secret: String): String? {
        return try {
            val secretBytes = secret.toByteArray(charset("UTF-8"))
            val secretKey = SecretKeySpec(secretBytes, "AES")
            val cipher = Cipher.getInstance("AES/CTR/NoPadding")
            val iv = strToDecrypt.toByteArray(charset("UTF-8")).copyOfRange(0, 16)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            cipher.doFinal(base64ToBytes(strToDecrypt)).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun base64ToBitmap(base64String: String) = Base64.decode(base64String, Base64.DEFAULT)
        .run { BitmapFactory.decodeByteArray(this, 0, size) }

    private fun base64ToBytes(base64String: String) = Base64.decode(base64String, Base64.DEFAULT)

    fun json(bidder: Any) = gson.toJson(bidder).toString()
}