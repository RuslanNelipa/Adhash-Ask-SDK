package org.adhash.sdk.adhashask.view

import android.graphics.Bitmap
import android.util.Log
import org.adhash.sdk.adhashask.constants.Global
import org.adhash.sdk.adhashask.ext.safeLet
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.*
import org.adhash.sdk.adhashask.storage.AdsStorage
import org.adhash.sdk.adhashask.utils.DataEncryptor
import org.adhash.sdk.adhashask.utils.SystemInfo
import kotlin.collections.ArrayList

private val TAG = Global.SDK_TAG + AdHashVm::class.java.simpleName

class AdHashVm(
    systemInfo: SystemInfo,
    private val gpsManager: GpsManager,
    private val adsStorage: AdsStorage,
    private var apiClient: ApiClient,
    private var dataEncryptor: DataEncryptor
) {
    private val adBidderBody = AdBidderBody()

    private var statesList = mutableListOf<InfoBuildState>()
    private var finalBuilderState = InfoBuildState.values().asList()

    private lateinit var onBitmapReceived: (bmp: Bitmap) -> Unit

    enum class InfoBuildState {
        PublisherId, Gps, Creatives
    }

    init {
        buildInitialAdBidder(systemInfo)
    }

    fun setBidderProperty(
        publisherId: String? = null,
        creatives: ArrayList<AdSizes>? = null
    ) {
        if (adBidderBody.publisherId.isNullOrEmpty()) publisherId?.let {
            adBidderBody.publisherId = it
            addBuilderState(InfoBuildState.PublisherId)
            Log.d(TAG, "Publisher ID set")
        }

        if (adBidderBody.creatives.isNullOrEmpty()) creatives?.let {
            adBidderBody.creatives = creatives
            addBuilderState(InfoBuildState.Creatives)
            Log.d(TAG, "Creatives set")
        }
    }

    fun onAttachedToWindow(onBitmapReceived: (bmp: Bitmap) -> Unit) {
        Log.d(TAG, "View attached")
        this.onBitmapReceived = onBitmapReceived
        getCoordinates()
    }

    fun onDetachedFromWindow() {
        Log.d(TAG, "View detached")

    }

    private fun addBuilderState(state: InfoBuildState) {
        statesList.add(state)
        notifyInfoBuildUpdated()
    }

    private fun notifyInfoBuildUpdated() {
        if (statesList.containsAll(finalBuilderState)) {
            fetchBidder()
        }
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
                recentAdvertisers = listOf(adsStorage.getAllRecentAds())
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
                addBuilderState(InfoBuildState.Gps)
            }
        )
    }

    /*STEP 1*/
    private fun fetchBidder() {
        Log.d(TAG, "Fetching bidder AD")

        apiClient.getAdBidder(adBidderBody,
            onSuccess = { adBidderResponse ->
                callAdvertiserUrl(adBidderBody, adBidderResponse)

            },
            onError = { error ->
                Log.e(TAG, "Fetching bidder failed with error: ${error.errorCase}")
            }
        )
    }

    /*STEP 3*/
    private fun callAdvertiserUrl(adBidderBody: AdBidderBody, adBidderResponse: AdBidderResponse) {
        val creatives = adBidderResponse.creatives
            ?.firstOrNull {
                it.advertiserURL?.isNotEmpty() == true && it.expectedHashes?.isNotEmpty() == true
            }

        safeLet(
            creatives?.advertiserURL,
            creatives?.expectedHashes
        ) { advertiserURL, expectedHashes ->
            val body = AdvertiserBody(
                expectedHashes = expectedHashes,
                budgetId = creatives?.budgetId,
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
                currentTimestamp = adBidderBody.currentTimestamp,
                recentAds = listOf(adsStorage.getAllRecentAds())
            )

            apiClient.callAdvertiserUrl(advertiserURL, body,
                onSuccess = { advertiser ->
                    /*STEP 4*/
                    verifyHashes(advertiser, expectedHashes)

                },
                onError = { error ->
                    Log.e(TAG, "Fetching bidder failed with error: ${error.errorCase}")
                }
            )

        } ?: run {
            Log.e(TAG, "Creatives are null")
        }
    }

    private fun verifyHashes(advertiser: AdvertiserResponse, expectedHashes: ArrayList<String>) {
        /*STEP 5*/
        if (!dataEncryptor.checkIfAdExpected(advertiser.data, expectedHashes)) return

        dataEncryptor.getImageFromData(advertiser.data)
            ?.let(onBitmapReceived) //todo save as displayed ad
            ?: run { Log.e(TAG, "Failed to extract bitmap") }

        //todo do step 6
    }
}