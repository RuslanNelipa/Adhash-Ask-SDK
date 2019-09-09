package org.adhash.sdk.adhashask.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import org.adhash.sdk.R
import org.adhash.sdk.adhashask.constants.LibConstants
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdBidderBody
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.pojo.Navigator
import org.adhash.sdk.adhashask.pojo.ScreenSize
import org.adhash.sdk.adhashask.utils.SystemInfo

private val TAG = LibConstants.SDK_TAG + AdHashView::class.java.simpleName

class AdHashView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {

    private val system = SystemInfo(context)
    private val gps = GpsManager(context)
    private val apiClient = ApiClient()

    private lateinit var adBidderBody: AdBidderBody

    init {
        buildInitialAdBidder()
        consumeAttrs(attrs)
    }

    private fun consumeAttrs(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AdHashView)
        adBidderBody = adBidderBody.copy(
            publisherId = attributes.getString(R.styleable.AdHashView_publisherId)
        )
        attributes.recycle()
    }

    /*START VIEW LIFECYCLE*/
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getCoordinates()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        adBidderBody.creatives?.let { getAdSize(widthMeasureSpec, heightMeasureSpec) }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
    /*END VIEW LIFECYCLE*/

    private fun getAdSize(width: Int, height: Int) {
        val widthPixels = MeasureSpec.getSize(width)
        val heightPixels = MeasureSpec.getSize(height)

        adBidderBody = adBidderBody.copy(
            creatives = arrayListOf(
                AdSizes(size = "${widthPixels}x${heightPixels}")
            )
        )
    }

    private fun buildInitialAdBidder() {
        with(system) {
            adBidderBody = AdBidderBody(
                timezone = getTimeZone(),
                location = getPublishedLocation(),
                size = ScreenSize(
                    screenWidth = getScreenWidth(),
                    screenHeight = getScreenHeight()
                ),
                connection = getConnectionType(),
                currentTimestamp = getTimeInUnix(),
                orientation = getOrientationScreen(),
                navigator = Navigator(
                    platform = getPlatform(),
                    language = getLanguage(),
                    userAgent = getUserAgent(),
                    model = getDeviceName(),
                    type = getPhoneType()
                ),
                isp = getCarrierId()
            )
        }
    }

    private fun getCoordinates() {
        gps.tryGetCoordinates(
            onSuccess = {
                adBidderBody = adBidderBody.copy(
                    gps = "${it.first}, ${it.second}"
                )
            },
            onError = {
                fetchBidder()
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