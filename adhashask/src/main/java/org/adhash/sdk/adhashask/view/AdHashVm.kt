package org.adhash.sdk.adhashask.view

import android.content.Context
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
import java.lang.Exception
import kotlin.collections.ArrayList


private val TAG = Global.SDK_TAG + AdHashVm::class.java.simpleName

class AdHashVm(
    private val context: Context,
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
    private var onAnalyticsSuccess: ((body: String) -> Unit)? = null
    private var onAnalyticsError: ((body: Throwable) -> Unit)? = null

    private var uri: Uri? = null
    private var adTagId: String? = null
    private var version: String? = null
    private var adOrder: Int? = null
    private var analyticsUrl: String? = null

    enum class InfoBuildState {
        PublisherId, Gps, Creatives
    }

    init {
        buildInitialAdBidder(systemInfo)
    }

    fun setUserProperties(
        adTagId: String? = null,
        version: String? = null,
        adOrder: Int? = null,
        analyticsUrl: String? = null
    ) {
        adTagId?.let { this.adTagId = adTagId }
        version?.let { this.version = version }
        adOrder?.let { this.adOrder = adOrder }
        analyticsUrl?.let { this.analyticsUrl = analyticsUrl }
    }

    fun setAnalyticsCallbacks(
        onAnalyticsSuccess: (body: String) -> Unit,
        onAnalyticsError: (error: Throwable) -> Unit
    ) {
        this.onAnalyticsSuccess = onAnalyticsSuccess
        this.onAnalyticsError = onAnalyticsError
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
        adsStorage.clear()
    }

    fun onAdDisplayed(recentAd: RecentAd) {
        adsStorage.saveRecentAd(recentAd)
    }

    fun getUri() = uri

    fun isTalkbackEnabled() = systemInfo.isTalkBackEnabled()

    fun attachCoordinatesToUri(x: Number, y: Number) {
        uri = uri?.buildUpon()
            ?.appendQueryParameter("offsetX", x.toString())
            ?.appendQueryParameter("offsetY", y.toString())
            ?.build()
    }

    fun fetchBidderAttempt() {
        if (builderStatesList.containsAll(completeBuilderState)) {
            fetchBidder()
        }
    }

    private fun addBuilderState(state: InfoBuildState) {
        builderStatesList.add(state)
        fetchBidderAttempt()
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
                url = dataEncryptor.encryptUtf8(uri)
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
                Log.d(TAG, "AdBidder response: ${adBidderResponse.let(dataEncryptor::json)}")

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
                budgetId = budgetId,
                timezone = adBidderBody.timezone,
                location = adBidderBody.location,
                publisherId = adBidderBody.publisherId,
                size = adBidderBody.size,
                navigator = adBidderBody.navigator,
                connection = adBidderBody.connection,
                isp = adBidderBody.isp,
                orientation = adBidderBody.orientation,
                gps = adBidderBody.gps,
                creatives = adBidderBody.creatives,
                mobile = adBidderBody.mobile,
                blockedAdvertisers = adBidderBody.blockedAdvertisers,
                currentTimestamp = adBidderBody.currentTimestamp,
                recentAdvertisers = adBidderBody.recentAdvertisers,
                period = adBidderResponse.period,
                nonce = adBidderResponse.nonce,
                url = dataEncryptor.encryptUtf8(uri)
            )
            Log.d(TAG, "Advertiser Body: ${body.let(dataEncryptor::json)}")

            apiClient.callAdvertiserUrl(advertiserURL, body,
                onSuccess = { advertiser ->
                    Log.d(TAG, "Advertiser response: ${advertiser.let(dataEncryptor::json)}")

                    /*STEP 4*/
                    verifyHashes(
                        adBidderResponse,
                        advertiserId,
                        budgetId,
                        advertiser,
                        expectedHashes,
                        body.nonce,
                        body.period
                    )
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
        adBidderResponse: AdBidderResponse,
        advertiserId: String,
        campaignId: Int,
        advertiser: AdvertiserResponse,
        expectedHashes: ArrayList<String>,
        nonce: Int?,
        period: Int?
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

                    decryptUrl(adBidderResponse, advertiser.url, adId, nonce, period)
                }
                ?: run { Log.e(TAG, "Failed to extract bitmap".also(onError)) }

        } else {
            Log.e(TAG, "Advertiser not expected".also(onError))
        }
    }

    private fun decryptUrl(adBidderResponse: AdBidderResponse, url: String, adId: String, nonce: Int?, period: Int?) {
        val keyBody = KeyBody(
            nonce = nonce,
            period = period,
            timezone = adBidderBody.timezone,
            location = adBidderBody.location,
            publisherId = adBidderBody.publisherId,
            size = adBidderBody.size,
            referrer = adBidderBody.referrer,
            navigator = adBidderBody.navigator,
            connection = adBidderBody.connection,
            isp = adBidderBody.isp,
            orientation = adBidderBody.orientation,
            gps = adBidderBody.gps,
            mobile = adBidderBody.mobile,
            currentTimestamp = adBidderBody.currentTimestamp,
            creatives = adBidderBody.creatives,
            blockedAdvertisers = adBidderBody.blockedAdvertisers,
            recentAdvertisers = adBidderBody.recentAdvertisers
        )

        Log.d(TAG, "Key body : ${keyBody.let(dataEncryptor::json)}")

        val key = keyBody
            .let(dataEncryptor::json)
            .let(dataEncryptor::sha1)

        dataEncryptor.aes256(context, url, key,
            onReceived = {
                parseUrl(it, adId, adBidderResponse)
                callAnalyticsModule(adId, adBidderResponse)
            }
        )

        Log.d(TAG, "Encrypted URL: $url")
        Log.d(TAG, "Encrypted KEY: $key")
    }

    /*STEP 8*/
    private fun parseUrl(url: String, adId: String, adBidderResponse: AdBidderResponse) {
        Log.d(TAG, "Decrypted URL: $url")

        try {
            uri = Uri.parse(url)
                .buildUpon()
                .appendQueryParameter("adTagId ", adTagId)
                .appendQueryParameter("publishedId ", adBidderBody.publisherId)
                .appendQueryParameter("creativeHash ", adId)
                .appendQueryParameter("advertiserId ", adBidderResponse.creatives?.firstOrNull()?.advertiserId)
                .appendQueryParameter("location ", adBidderBody.location)
                .appendQueryParameter("platform ", adBidderBody.navigator?.platform)
                .appendQueryParameter("connection ", adBidderBody.connection)
                .appendQueryParameter("isp ", adBidderBody.isp)
                .appendQueryParameter("orientation ", adBidderBody.orientation)
                .appendQueryParameter("gps ", adBidderBody.gps)
                .appendQueryParameter("language ", adBidderBody.navigator?.language)
                .appendQueryParameter("device ", adBidderBody.navigator?.model?.substringBefore(" "))
                .appendQueryParameter("model ", adBidderBody.navigator?.model?.substringAfter(" "))
                .appendQueryParameter("type ", adBidderBody.navigator?.type)
                .appendQueryParameter("screenWidth ", adBidderBody.size?.screenWidth.toString())
                .appendQueryParameter("screenHeight ", adBidderBody.size?.screenHeight.toString())
                .appendQueryParameter("timeZone ", adBidderBody.timezone.toString())
                .appendQueryParameter("width ", adBidderBody.creatives?.firstOrNull()?.size?.substringBefore("x"))
                .appendQueryParameter("height ", adBidderBody.creatives?.firstOrNull()?.size?.substringAfter("x"))
                .appendQueryParameter("period ", adBidderResponse.period.toString())
                .appendQueryParameter("cost ", adBidderResponse.creatives?.firstOrNull()?.maxPrice.toString())
                .appendQueryParameter("commission ", adBidderResponse.creatives?.firstOrNull()?.commission.toString())
                .appendQueryParameter("nonce ", adBidderResponse.nonce.toString())
                .appendQueryParameter("mobile ", true.toString())
                .appendQueryParameter("budgetId ", adBidderResponse.creatives?.firstOrNull()?.budgetId.toString())
                .appendQueryParameter("timestamp ", systemInfo.getTimeInUnix().toString())
                .appendQueryParameter("version ", version.toString())
                .appendQueryParameter("url ", version.toString())
                .build()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to build click URL")
        }
    }

    /*STEP 7*/
    private fun callAnalyticsModule(adId: String, adBidderResponse: AdBidderResponse) {
        analyticsUrl?.let { url ->
            val analyticsQuery = AnalyticsBody(
                adTagId = adTagId,
                publishedId = adBidderBody.publisherId,
                creativeHash = adId,
                advertiserId = adBidderResponse.creatives?.firstOrNull()?.advertiserId,
                pageURL = adBidderBody.location,
                platform = adBidderBody.navigator?.platform,
                connection = adBidderBody.connection,
                isp = adBidderBody.isp,
                orientation = adBidderBody.orientation,
                gps = adBidderBody.gps,
                language = adBidderBody.navigator?.language,
                device = adBidderBody.navigator?.model?.substringBefore(" "),
                model = adBidderBody.navigator?.model?.substringAfter(" "),
                type = adBidderBody.navigator?.type,
                screenWidth = adBidderBody.size?.screenWidth,
                screenHeight = adBidderBody.size?.screenHeight,
                timeZone = adBidderBody.timezone,
                width = adBidderBody.creatives?.firstOrNull()?.size?.substringBefore("x"),
                height = adBidderBody.creatives?.firstOrNull()?.size?.substringAfter("x"),
                period = adBidderResponse.period,
                cost = adBidderResponse.creatives?.firstOrNull()?.maxPrice,
                comission = adBidderResponse.creatives?.firstOrNull()?.commission,
                nonce = adBidderResponse.nonce,
                pageview = (adOrder == 1 || adOrder == 0),
                mobile = true
            )

            try {
                apiClient.callAnalyticsModule(url, analyticsQuery, onAnalyticsSuccess, onAnalyticsError)
            } catch (e: Exception) {
                onAnalyticsError?.invoke(e)
            }

        } ?: onAnalyticsError?.invoke(Throwable("Analytics URL is empty"))
    }

    private fun String.isAdExpected(expectedHashes: ArrayList<String>): Boolean = this.let(expectedHashes::contains)
}