package org.adhash.sdk.adhashask.network

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.adhash.sdk.adhashask.base.BaseResponse
import org.adhash.sdk.adhashask.constants.ApiConstants
import org.adhash.sdk.adhashask.pojo.ApiError
import org.adhash.sdk.adhashask.constants.Global
import org.adhash.sdk.adhashask.pojo.AdBidderBody
import org.adhash.sdk.adhashask.pojo.AdBidderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val TAG = Global.SDK_TAG + ApiClient::class.java.simpleName

class ApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(LoggingInterceptor())
        .build()

    private val apiEndpoint = Retrofit.Builder()
        .baseUrl(ApiConstants.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()
        .create(AdHashApi::class.java)

    private val errorHandler = ErrorHandler()

    fun getAdBidder(
        body: AdBidderBody,
        onSuccess: (AdBidderResponse) -> Unit,
        onError: (ApiError) -> Unit
    ) {
        apiEndpoint.getAdBidder(
            bodyAd = body,
            action = ApiConstants.Query.RTB,
            version = ApiConstants.Query.VERSION
        ).enqueue(object : Callback<AdBidderResponse> {
            override fun onFailure(call: Call<AdBidderResponse>, error: Throwable) {
                val apiError = errorHandler.consumeThrowable(error, getRequestPath(call))
                Log.e(TAG, "API call exception:  $error")
                onError(apiError)
            }

            override fun onResponse(
                call: Call<AdBidderResponse>,
                response: Response<AdBidderResponse>
            ) {
                response.body()?.let { adBidder ->
                    Log.d(TAG, "Ad Bidder received: $adBidder")
                    onSuccess(adBidder)

                } ?: run {
                    val httpError = errorHandler.consumeError(response, getRequestPath(call))
                    Log.e(TAG, "Failed to make API call:  $httpError")
                    onError(httpError)
                }
            }
        })
    }

    private fun getRequestPath(call: Call<out BaseResponse>) = call.request().url().encodedPath()
}