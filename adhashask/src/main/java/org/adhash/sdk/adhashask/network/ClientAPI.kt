package org.adhash.sdk.adhashask.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import retrofit2.converter.scalars.ScalarsConverterFactory


class ClientAPI {

    var gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    fun create(url: String): AdHashApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(AdHashApi::class.java)
    }
}