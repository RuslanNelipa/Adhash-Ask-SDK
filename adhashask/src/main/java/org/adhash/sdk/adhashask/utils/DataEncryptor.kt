package org.adhash.sdk.adhashask.utils

import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val algorithm = "AES"
private const val padding = "AES/CTR/PKCS5Padding"
private const val ivSize = 16

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

    fun aes256(target: String, password: String): Any? {
        return try {
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val ivAndCipherText = Base64.decode(target, Base64.NO_WRAP)
            val cipherText = Arrays.copyOfRange(ivAndCipherText, ivSize, ivAndCipherText.size)

            val ivByteArray = Arrays.copyOfRange(ivAndCipherText, 0, ivSize)
            val spec = PBEKeySpec(password.toCharArray(), ivByteArray, 65536, 256)
            val iv = IvParameterSpec(ivByteArray)

            val cipher = Cipher.getInstance(padding)
            cipher.init(Cipher.DECRYPT_MODE, secretKeyFactory.generateSecret(spec), iv)

            val result = cipher.doFinal(cipherText)
            Base64.encodeToString(result, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun base64ToBitmap(base64String: String) = Base64.decode(base64String, Base64.DEFAULT)
        .run { BitmapFactory.decodeByteArray(this, 0, size) }

    fun json(bidder: Any) = gson.toJson(bidder).toString()
}