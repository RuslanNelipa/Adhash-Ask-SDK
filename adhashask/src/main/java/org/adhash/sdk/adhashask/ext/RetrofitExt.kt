package org.adhash.sdk.adhashask.ext

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.adhash.sdk.adhashask.network.LoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


inline fun <reified T> String.createRetrofit(): T = Retrofit.Builder()
    .baseUrl(this)
    .client(
        OkHttpClient.Builder()
            .addInterceptor(LoggingInterceptor())
            .build()
    )
    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
    .build()
    .create(T::class.java)