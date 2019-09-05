package org.adhash.sdk.adhashask.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdBidderBody
import org.adhash.sdk.adhashask.utils.MakeInfoBodyUtil

class AdHashView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {

    private val util = MakeInfoBodyUtil()
    private val apiClient = ApiClient()

    private val adBidderBody: AdBidderBody

    private var adSizeStr: String? = null
    private val log = AdHashView::class.java.name

    init {
        adBidderBody = util.gatherAllInfo(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        fetchBidder()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        getViewSize(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getViewSize(width: Int, height: Int) {
        val widthPixels = MeasureSpec.getSize(width)
        val heightPixels = MeasureSpec.getSize(height)
        adSizeStr = "${widthPixels}x$heightPixels"
        Log.e(log, "Value of adSizeStr $adSizeStr")
    }

//    fun prepareBanner(publisherId: String) {
//        // todo get internetID, gpsCoordinates, set PublisherId, blocked Ads, recently Ads
////        adSizeStr?.let {size ->
////            AdBidderResponse?.creatives?.add(AdSizes(size))
////        }
//        adBidderBody.creatives.add(AdSizes("300x250"))
//        adBidderBody.publisherId = publisherId
//    }

    private fun fetchBidder() {
        apiClient.getAdBidder(adBidderBody,
            onSuccess = {

            },
            onError = {

            }
        )
    }
}