package org.adhash.sdk.adhashask.network

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import kotlin.text.StringBuilder

class AdConnection {

    private val log = AdConnection::class.java.name

    fun getJsonResponse(requestURL: String, method: String, jsonString: String) : String? {
        val url: URL? = createURL(requestURL)
        var jsonResponse: String? = null
        url?.let { site ->
            try {
                jsonResponse = makeHttpRequest(site, method, jsonString)
            } catch (e : IOException){
                Log.e(log, "Error closing input stream", e)
                // todo made error CLOSING INPUT STREAM
            }
        }
        return jsonResponse
    }

    private fun createURL(stringURL: String) : URL? {
        var url: URL? = null
        try {
            url = URL(stringURL)
        } catch (e : MalformedURLException){
            Log.e(log, "Error with creating URL ", e)
            // todo made error CREATE URL
        }
        return url
    }

    private fun makeHttpRequest(url: URL, method: String, jsonString: String) : String {
        var jsonResponse = ""

        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.apply {
                readTimeout = 10000
                connectTimeout = 15000
                requestMethod = method
                // todo add POST Body with information
                setRequestProperty("Content-Type","application/json")
                val outputStream = outputStream
                outputStream.write(jsonString.toByteArray())
                connect()
            }

            if (urlConnection.responseCode == 200) {
                inputStream = urlConnection.inputStream
                jsonResponse = readFromStream(inputStream)
            } else {
                Log.e(log, "Error response code: " + urlConnection.responseCode)
                // todo made error URL CONNECTION + RESPONSE CODE
            }
        } catch (e: IOException){
            Log.e(log, "Problem retrieving the JSON results.", e)
            // todo made error RETRIEVING JSON STRING FROM INPUT
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
        }
        return jsonResponse
    }

    private fun readFromStream(inputStream: InputStream): String {
        val output = StringBuilder()
        val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
        val reader = BufferedReader(inputStreamReader)
        var line = reader.readLine()
        while (line != null){
            output.append(line)
            line = reader.readLine()
        }
        return output.toString()
    }
}