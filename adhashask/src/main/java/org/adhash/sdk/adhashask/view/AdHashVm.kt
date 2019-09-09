package org.adhash.sdk.adhashask.view

import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdBidderBody
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.pojo.Navigator
import org.adhash.sdk.adhashask.pojo.ScreenSize
import org.adhash.sdk.adhashask.utils.SystemInfo

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
        publisherId?.let { adBidderBody.publisherId = it }
        creatives?.let { adBidderBody.creatives = it }
    }

    fun onAttached(){
        getCoordinates()
    }

    fun onDetached() {

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
    }

    private fun getCoordinates() {
        gpsManager.tryGetCoordinates(
            onSuccess = {
                adBidderBody.gps = "${it.first}, ${it.second}"
            },
            doFinally = {
                fetchBidder()
            }
        )
    }

    private fun fetchBidder() {
        apiClient.getAdBidder(adBidderBody,
            onSuccess = {
                //1st step complete
            },
            onError = {

            }
        )
    }
}