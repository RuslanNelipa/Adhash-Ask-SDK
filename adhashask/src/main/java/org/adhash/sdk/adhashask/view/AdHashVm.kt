package org.adhash.sdk.adhashask.view

import android.util.Log
import org.adhash.sdk.adhashask.constants.Global
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.*
import org.adhash.sdk.adhashask.utils.SystemInfo

private val TAG = Global.SDK_TAG + AdHashVm::class.java.simpleName

class AdHashVm(
    systemInfo: SystemInfo,
    private val gpsManager: GpsManager,
    private var apiClient: ApiClient
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

    fun onViewDisplayed() {
        Log.d(TAG, "View attached")

        getCoordinates()
    }

    fun onViewDetached() {
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
//                recentAdvertisers = adsStorage.getRecentAds() //todo
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

    /*STEP 1*/
    private fun fetchBidder() {
        Log.d(TAG, "Fetching bidder AD")

        apiClient.getAdBidder(adBidderBody,
            onSuccess = { adBidderResponse ->
                Log.d(TAG, "Fetching bidder received: $adBidderBody")
                callAdvertiserUrl(adBidderBody, adBidderResponse)

            },
            onError = { error ->
                Log.e(TAG, "Fetching bidder failed with error: ${error.errorCase}")
            }
        )
    }

    /*STEP 3*/
    private fun callAdvertiserUrl(adBidderBody: AdBidderBody, adBidderResponse: AdBidderResponse) {
        val creatives = adBidderResponse.creatives?.firstOrNull()

        creatives?.run {
            val body = AdvertiserBody(
                expectedHashes = expectedHashes,
                budgetId = budgetId,
                period = adBidderResponse.period,
                nonce = adBidderResponse.nonce,
                timezone = adBidderBody.timezone,
                location = adBidderBody.location,
                publisherId = adBidderBody.publisherId,
                size = adBidderBody.size,
                navigator = adBidderBody.navigator,
                connection = adBidderBody.connection,
                isp = adBidderBody.connection,
                orientation = adBidderBody.orientation,
                gps = adBidderBody.gps,
                creatives = adBidderBody.creatives,
                mobile = adBidderBody.mobile,
                blockedAdvertisers = adBidderBody.blockedAdvertisers,
                currentTimestamp = adBidderBody.currentTimestamp
//            recentAds = adsStorage.getRecentAds() //todo
            )

            apiClient = ApiClient(baseUrl = advertiserURL)
//            apiClient.callAdvertiserUrl(body) //todo

        } ?: run {
            Log.e(TAG, "Creatives are null")
        }
    }

}