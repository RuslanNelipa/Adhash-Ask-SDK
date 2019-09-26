package org.adhash.sdk.adhashask.network

import android.util.Log
import com.google.gson.Gson
import org.adhash.sdk.adhashask.base.BaseResponse
import org.adhash.sdk.adhashask.constants.ApiConstants
import org.adhash.sdk.adhashask.constants.Global
import org.adhash.sdk.adhashask.ext.createRetrofit
import org.adhash.sdk.adhashask.ext.extractBaseUrl
import org.adhash.sdk.adhashask.pojo.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val TAG = Global.SDK_TAG + ApiClient::class.java.simpleName

class ApiClient(
    private val gson: Gson
) {

    private val errorHandler = ErrorHandler()

    fun getAdBidder(body: AdBidderBody, onSuccess: (AdBidderResponse) -> Unit, onError: (ApiError) -> Unit) {
        ApiConstants.API_BASE_URL.createRetrofit<AdHashApi>(gson)
            .getAdBidder(bodyAd = body, action = ApiConstants.Query.RTB, version = ApiConstants.Query.VERSION)
            .enqueue(object : Callback<AdBidderResponse> {
                override fun onFailure(call: Call<AdBidderResponse>, error: Throwable) {
                    val apiError = errorHandler.consumeThrowable(error, getRequestPath(call))
                    Log.e(TAG, "API call exception:  $error")
                    onError(apiError)
                }

                override fun onResponse(call: Call<AdBidderResponse>, response: Response<AdBidderResponse>) {
                    response.body()?.let { adBidder ->
                        Log.d(TAG, "Ad Bidder received.")
                        if (adBidder.creatives.isNullOrEmpty()) {
                            onError(ApiError(ApiErrorCase.BadRequest, requestPath = getRequestPath(call)))
                        } else {
                            onSuccess(adBidder)
                        }

                    } ?: run {
                        val httpError = errorHandler.consumeError(response, getRequestPath(call))
                        Log.e(TAG, "Failed to make API call:  $httpError")
                        onError(httpError)
                    }
                }
            })
    }

    fun callAdvertiserUrl(advertiserUrl: String, body: AdvertiserBody, onSuccess: (AdvertiserResponse) -> Unit, onError: (ApiError) -> Unit) {
        advertiserUrl.extractBaseUrl().createRetrofit<AdvertiserApi>(gson)
            .callAdvertiserUrl(advertiserUrl,body).enqueue(object : Callback<AdvertiserResponse> {
                override fun onFailure(call: Call<AdvertiserResponse>, error: Throwable) {
                    val apiError = errorHandler.consumeThrowable(error, getRequestPath(call))
                    Log.e(TAG, "API call exception:  $error")
                    onError(apiError)
                }

                override fun onResponse(call: Call<AdvertiserResponse>, response: Response<AdvertiserResponse>) {
                    response.body()?.let { advertiserResponse ->
                        Log.d(TAG, "Advertiser received.")
                        if (advertiserResponse.status != ApiConstants.STATUS_OK) {
                            onError(ApiError(ApiErrorCase.BadRequest, requestPath = getRequestPath(call)))
                        } else {
                            onSuccess(advertiserResponse)
                        }

                    } ?: run {
                        val httpError = errorHandler.consumeError(response, getRequestPath(call))
                        Log.e(TAG, "Failed to make API call:  $httpError")
                        onError(httpError)
                    }
                }
            })
    }

    fun callAnalyticsModule(url: String, analyticsBody: AnalyticsBody, onSuccess: (BaseResponse) -> Unit, onError: (ApiError) -> Unit){
        url.extractBaseUrl().createRetrofit<AdvertiserApi>(gson)
            .callAnalytics(url).enqueue(
                object : Callback<BaseResponse> {
                    override fun onFailure(call: Call<BaseResponse>, error: Throwable) {
                        val apiError = errorHandler.consumeThrowable(error, getRequestPath(call))
                        Log.e(TAG, "API call exception:  $error")
                        onError(apiError)
                    }

                    override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                        response.body()?.let { advertiserResponse ->
                            Log.d(TAG, "Analytics module succeed.")
                            if (advertiserResponse.status != ApiConstants.STATUS_OK) {
                                onError(ApiError(ApiErrorCase.BadRequest, requestPath = getRequestPath(call)))
                            } else {
                                onSuccess(advertiserResponse)
                            }

                        } ?: run {
                            val httpError = errorHandler.consumeError(response, getRequestPath(call))
                            Log.e(TAG, "Failed to make API call:  $httpError")
                            onError(httpError)
                        }
                    }
                }
            )
    }
    private fun getRequestPath(call: Call<out BaseResponse>) = call.request().url().encodedPath()
}