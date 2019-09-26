package org.adhash.sdk.adhashask.network

import org.adhash.sdk.adhashask.base.BaseResponse
import org.adhash.sdk.adhashask.pojo.AdvertiserBody
import org.adhash.sdk.adhashask.pojo.AdvertiserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface AdvertiserApi {
    @POST
    fun callAdvertiserUrl(
        @Url url: String,
        @Body advertiserBody: AdvertiserBody
    ): Call<AdvertiserResponse>

    @GET
    fun callAnalytics(
        @Url url: String
    ): Call<BaseResponse>
}