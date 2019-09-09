package org.adhash.sdk.adhashask.view

import android.util.Log
import org.adhash.sdk.adhashask.constants.Global
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdBidderBody
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.pojo.Navigator
import org.adhash.sdk.adhashask.pojo.ScreenSize
import org.adhash.sdk.adhashask.utils.SystemInfo

private val TAG = Global.SDK_TAG + AdHashVm::class.java.simpleName

class AdHashVm(
    systemInfo: SystemInfo,
    private val gpsManager: GpsManager,
    private val apiClient: ApiClient
) {
    private val adBidderBody = AdBidderBody()

    init {
        buildInitialAdBidder(systemInfo)
    }

    fun setBidderProperty(
        publisherId: String? = null,
        creatives: ArrayList<AdSizes>? = null
    ) {
        publisherId?.let {
            adBidderBody.publisherId = it
            Log.d(TAG, "Publisher ID set")
        }
        creatives?.let {
            adBidderBody.creatives = it
            Log.d(TAG, "Creatives set")
        }
    }

    fun onAttached() {
        Log.d(TAG, "View attached")

        getCoordinates()
    }

    fun onDetached() {
        Log.d(TAG, "View detached")

    }

    private fun buildInitialAdBidder(systemInfo: SystemInfo) {
        with(systemInfo) {
            adBidderBody.apply {
                timezone = getTimeZone()
                location = getPublishedLocation()
                size = ScreenSize(
                    screenWidth = getScreenWidth(),
                    screenHeight = getScreenHeight()
                )
                connection = getConnectionType()
                currentTimestamp = getTimeInUnix()
                orientation = getOrientationScreen()
                navigator = Navigator(
                    platform = getPlatform(),
                    language = getLanguage(),
                    userAgent = getUserAgent(),
                    model = getDeviceName(),
                    type = getPhoneType()
                )
                isp = getCarrierId()
            }
        }
        Log.d(TAG, "Initial bidder creation complete")
    }

    private fun getCoordinates() {
        gpsManager.tryGetCoordinates(
            onSuccess = {
                adBidderBody.gps = "${it.first}, ${it.second}"
                Log.d(TAG, "Coordinates received: ${adBidderBody.gps}")
            },
            doFinally = {
                Log.d(TAG, "Coordinates fetch attempt complete")
                fetchBidder()
            }
        )
    }

    private fun fetchBidder() {
        Log.d(TAG, "Fetching bidder AD")

        apiClient.getAdBidder(adBidderBody,
            onSuccess = {
                Log.d(TAG, "Fetching bidder received")

            },
            onError = { error ->
                Log.e(TAG, "Fetching bidder failed with error: ${error.errorCase}")

            }
        )
    }
}