package org.adhash.sdk.adhashask.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface AnalyticsApi {
    @GET
    fun callAnalytics(@Url url: String, @QueryMap(encoded = true) queryMap: Map<String, String?>): Call<String>
}