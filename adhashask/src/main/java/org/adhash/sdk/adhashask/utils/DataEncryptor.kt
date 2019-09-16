package org.adhash.sdk.adhashask.utils

import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
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

//    @Throws(Exception::class)
//    fun aes256(target: String, key: String): String? {
//        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") //todo
//        return ""
//    }

    fun aes256(strToDecrypt: String, secret: String): String? {
        return try {
            val secretKey = SecretKeySpec(secret.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            cipher.doFinal(base64ToBytes(strToDecrypt)).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getSecretKey(myKey: String): SecretKeySpec? {
        var sha: MessageDigest? = null
        return try {
            var key = myKey.toByteArray(charset("UTF-8"))
            sha = MessageDigest.getInstance("SHA-1")
            key = sha.digest(key)
            key = key.copyOf(16)
            SecretKeySpec(key, "AES")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

    private fun base64ToBitmap(base64String: String) = Base64.decode(base64String, Base64.DEFAULT)
        .run { BitmapFactory.decodeByteArray(this, 0, size) }

    private fun base64ToBytes(base64String: String) = Base64.decode(base64String, Base64.DEFAULT)

    fun json(bidder: Any) = gson.toJson(bidder).toString()
}