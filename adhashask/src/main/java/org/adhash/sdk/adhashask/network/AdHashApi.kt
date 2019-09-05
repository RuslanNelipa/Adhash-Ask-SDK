package org.adhash.sdk.adhashask.network

import org.adhash.sdk.adhashask.constants.ApiConstants
import org.adhash.sdk.adhashask.pojo.AdBidderBody
import org.adhash.sdk.adhashask.pojo.AdBidderResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AdHashApi {
    @POST(ApiConstants.Endpoint.GET_AD_BIDDER)
    fun getAdBidder(
        @Query(ApiConstants.Param.ACTION) action: String,
        @Query(ApiConstants.Param.VERSION) version: Double,
        @Body bodyAd: AdBidderBody

    ): Call<AdBidderResponse>
}