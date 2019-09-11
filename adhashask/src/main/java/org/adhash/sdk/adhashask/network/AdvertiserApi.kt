package org.adhash.sdk.adhashask.network

import org.adhash.sdk.adhashask.pojo.AdvertiserBody
import org.adhash.sdk.adhashask.pojo.AdvertiserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AdvertiserApi {
    @POST
    fun callAdvertiserUrl(@Body advertiserBody: AdvertiserBody): Call<AdvertiserResponse>
}