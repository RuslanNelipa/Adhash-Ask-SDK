package org.adhash.sdk.adhashask.ext

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.adhash.sdk.adhashask.network.LoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.MalformedURLException
import java.net.URL


inline fun <reified T> String.createRetrofit(vararg interceptors: Interceptor): T =
    Retrofit.Builder()
        .baseUrl(this)
        .client(
            OkHttpClient.Builder()
                .apply {
                    interceptors.forEach { addInterceptor(it) }
                }
                .addInterceptor(LoggingInterceptor())
                .build()
        )
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .disableHtmlEscaping()
                    .create()
            )
        )
        .build()
        .create(T::class.java)

fun String.extractBaseUrl() = try {
    URL(this).run { "$protocol://$host/" }
} catch (e: MalformedURLException) {
    this
}
