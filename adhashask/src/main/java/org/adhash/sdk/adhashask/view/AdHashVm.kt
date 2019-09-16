package org.adhash.sdk.adhashask.view

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import org.adhash.sdk.adhashask.constants.Global
import org.adhash.sdk.adhashask.ext.safeLet
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.*
import org.adhash.sdk.adhashask.storage.AdsStorage
import org.adhash.sdk.adhashask.utils.DataEncryptor
import org.adhash.sdk.adhashask.utils.SystemInfo

private val TAG = Global.SDK_TAG + AdHashVm::class.java.simpleName

class AdHashVm(
    private val systemInfo: SystemInfo,
    private val gpsManager: GpsManager,
    private val adsStorage: AdsStorage,
    private var apiClient: ApiClient,
    private val dataEncryptor: DataEncryptor
) {
    private val adBidderBody = AdBidderBody()
    private var builderStatesList = mutableListOf<InfoBuildState>()
    private var completeBuilderState = InfoBuildState.values().asList()

    private lateinit var onBitmapReceived: (bmp: Bitmap, recentAd: RecentAd) -> Unit
    private lateinit var onError: (reason: String) -> Unit

    private var uri: Uri? = null

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

    fun onAttachedToWindow(
        onBitmapReceived: (bmp: Bitmap, recentAd: RecentAd) -> Unit,
        onError: (reason: String) -> Unit
    ) {
        Log.d(TAG, "View attached")
        this.onBitmapReceived = onBitmapReceived
        this.onError = onError
        getCoordinates()
    }

    fun onDetachedFromWindow() {
        Log.d(TAG, "View detached")
    }

    fun onAdDisplayed(recentAd: RecentAd) {
        adsStorage.saveRecentAd(
            listOf(
                recentAd.timestamp,
                recentAd.advertiserId,
                recentAd.campaignId,
                recentAd.adId
            )
        )
    }

    fun getUri() = uri

    fun isTalkbackEnabled() = systemInfo.isTalkBackEnabled()

    private fun addBuilderState(state: InfoBuildState) {
        builderStatesList.add(state)
        notifyInfoBuildUpdated()
    }

    private fun notifyInfoBuildUpdated() {
        if (builderStatesList.containsAll(completeBuilderState)) {
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
                recentAdvertisers = adsStorage.getAllRecentAds()
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
        Log.d(TAG, "Fetching bidder AD: ${adBidderBody.let(dataEncryptor::json)}")

        apiClient.getAdBidder(adBidderBody,
            onSuccess = { adBidderResponse ->
                callAdvertiserUrl(adBidderBody, adBidderResponse)

            },
            onError = { error ->
                Log.e(TAG, "Fetching bidder failed with error: ${error.errorCase}".also(onError))
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
            creatives?.advertiserId,
            creatives?.budgetId,
            creatives?.advertiserURL,
            creatives?.expectedHashes
        ) { advertiserId, budgetId, advertiserURL, expectedHashes ->
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
                recentAds = adBidderBody.recentAdvertisers
            )

            apiClient.callAdvertiserUrl(advertiserURL, body,
                onSuccess = { advertiser ->
                    /*STEP 4*/
                    verifyHashes(advertiserId, budgetId, advertiser, expectedHashes)
                },
                onError = { error ->
                    Log.e(TAG, "Fetching bidder failed with error: ${error.errorCase}".also(onError))
                }
            )

        } ?: run {
            Log.e(TAG, "Creatives are null".also(onError))
        }
    }

    private fun verifyHashes(
        advertiserId: String,
        campaignId: Int,
        advertiser: AdvertiserResponse,
        expectedHashes: ArrayList<String>
    ) {
        /*STEP 5*/
        val adId = dataEncryptor.sha1(advertiser.data)

        if (adId.isAdExpected(expectedHashes)) {
            dataEncryptor.getImageFromData(advertiser.data)
                ?.let { bmp ->
                    onBitmapReceived(
                        bmp, RecentAd(
                            timestamp = systemInfo.getTimeInUnix(),
                            advertiserId = advertiserId,
                            campaignId = campaignId,
                            adId = adId
                        )
                    )

                    decryptUrl(advertiser.url)
                }
                ?: run { Log.e(TAG, "Failed to extract bitmap".also(onError)) }

        } else {
            Log.e(TAG, "Advertiser not expected".also(onError))
        }
    }

    private fun decryptUrl(url: String) {
        Log.d(TAG, "JSON for URL: ${adBidderBody.let(dataEncryptor::json)}") //todo temp logs
        Log.d(TAG, "KEY for URL after SHA-1: ${adBidderBody.let(dataEncryptor::json).let(dataEncryptor::sha1)}") //todo temp logs

        val key = adBidderBody
            .let(dataEncryptor::json)
            .let(dataEncryptor::sha1)

//        val url = dataEncryptor.aes256(url, key)

        Log.d(TAG, "Encrypted URL: $url")
        Log.d(TAG, "Encrypted KEY: $key")

    }

    private fun String.isAdExpected(expectedHashes: ArrayList<String>): Boolean = this.let(expectedHashes::contains)
}