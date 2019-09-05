package org.adhash.sdk.adhashask.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import org.adhash.sdk.adhashask.constants.LibConstants
import java.io.EOFException
import java.nio.charset.Charset

private val TAG = LibConstants.SDK_TAG + LoggingInterceptor::class.java.simpleName

class LoggingInterceptor : Interceptor {

    private val UTF8 = Charset.forName("UTF-8")

    override fun intercept(chain: Interceptor.Chain): Response {
        return printRequestAndResponse(chain)
    }

    private fun printRequestAndResponse(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val requestBody = request.body()
        val reqUrl = request.url()

        val buffer = Buffer()
        requestBody?.writeTo(buffer)

        var charset: Charset? = UTF8
        val contentType = requestBody?.contentType()
        if (contentType != null) {
            charset = contentType.charset(UTF8)
        }

        val bodySent = if (buffer.isPlaintext()) {
            buffer.readString(charset)
        } else {
            "empty"
        }

        Log.d(TAG, "(SENT -->) url:$reqUrl, body:$bodySent")

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.d(TAG, "<-- HTTP FAILED: $e")
            throw e
        }

        var responseString = "empty"
        val responseBody = response.body()
        val contentLength = responseBody!!.contentLength()
        val url = response.request().url()
        val code = response.code()


        if (response.bodyEncoded()) {
            responseString = "${request.method()}  (encoded body omitted)"
        } else {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val resBuffer = source.buffer()

            val resContentType = responseBody.contentType()
            if (resContentType != null) {
                charset = resContentType.charset(UTF8)
            }

            if (!resBuffer.isPlaintext()) {
                Log.d(TAG, "(GOT <--) url:$url, statusCode:$code, body:$responseString")
                return response
            }

            if (contentLength != 0L) {
                responseString = resBuffer.clone().readString(charset!!)
            }
        }

        Log.d(TAG, "(GOT <--) url:$url, statusCode:$code, body:$responseString")

        return response

    }

    private fun Buffer.isPlaintext(): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (this.size() < 64) this.size() else 64
            this.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    private fun Response.bodyEncoded(): Boolean {
        val contentEncoding = this.headers().get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

}