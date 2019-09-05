package org.adhash.sdk.adhashask.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import org.adhash.sdk.adhashask.constants.LibConstants
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdBidderBody
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.pojo.Navigator
import org.adhash.sdk.adhashask.pojo.ScreenSize
import org.adhash.sdk.adhashask.utils.SystemInfo

private val TAG = LibConstants.SDK_TAG + AdHashView::class.java.simpleName

class AdHashView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {

    private val system = SystemInfo(context)
    private val apiClient = ApiClient()

    private var adSizeStr: String? = null

    private var adBidderBody: AdBidderBody? = null

//    init {
//        adBidderBody = util.gatherAllInfo(context)
//    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        fetchBidder()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        getViewSize(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getViewSize(width: Int, height: Int) {
        val widthPixels = MeasureSpec.getSize(width)
        val heightPixels = MeasureSpec.getSize(height)
        adSizeStr = "${widthPixels}x$heightPixels"
        Log.d(TAG, "Value of adSizeStr $adSizeStr")
    }

//    fun prepareBanner(publisherId: String) {
//        // todo get internetID, gpsCoordinates, set PublisherId, blocked Ads, recently Ads
////        adSizeStr?.let {size ->
////            AdBidderResponse?.creatives?.add(AdSizes(size))
////        }
//        adBidderBody.creatives.add(AdSizes("300x250"))
//        adBidderBody.publisherId = publisherId
//    }

    private fun fetchBidder(body: AdBidderBody) {
        apiClient.getAdBidder(body,
            onSuccess = {

            },
            onError = {

            }
        )
    }

    private fun buildAdBidder() = AdBidderBody(
        timezone = system.getTimeZone(),
        referrer = "", //?
        location = "http://publisher.whatismycar.com/", //where do we get this?
        publisherId = "0x89c430444df3dc8329aba2c409770fa196b65d3c",
        size = ScreenSize(
            screenWidth = system.getScreenWidth(),
            screenHeight = system.getScreenHeight()
        ),
        navigator = Navigator(
            platform = "Win32",
            language = "en",
            userAgent = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36",
            model = "asd",
            type = "mobile"
        ),
        creatives = arrayListOf(
            AdSizes(size = "300x250")
        ),
        blockedAdvertisers = arrayListOf("0x6a207fd9893bcab1dc9ecb4079c81dc34551ed04"),
        recentAdvertisers = arrayListOf("0x6a207fd9893bcab1dc9ecb4079c81dc34551ed04"),
        connection = system.getConnectionType(),
        currentTimestamp = system.getTimeInUnix(),
        orientation = system.getOrientationScreen(),
        gps = "",
        isp = ""
    )
}