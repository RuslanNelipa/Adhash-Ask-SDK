package org.adhash.sdk.adhashask.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import org.adhash.sdk.R
import org.adhash.sdk.adhashask.constants.LibConstants
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.utils.SystemInfo

private val TAG = LibConstants.SDK_TAG + AdHashView::class.java.simpleName

class AdHashView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {
    private val vm = AdHashVm(
        systemInfo = SystemInfo(context),
        gpsManager = GpsManager(context),
        apiClient = ApiClient()
    )

    init {
        consumeAttrs(attrs)
    }

    /*START VIEW LIFECYCLE*/
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        vm.onAttached()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        vm.onDetached()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthPixels = MeasureSpec.getSize(width)
        val heightPixels = MeasureSpec.getSize(height)

        vm.setBidderProperty(creatives = arrayListOf(AdSizes(size = "${widthPixels}x${heightPixels}")))
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
    /*END VIEW LIFECYCLE*/

    private fun consumeAttrs(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AdHashView)
        vm.setBidderProperty(publisherId = attributes.getString(R.styleable.AdHashView_publisherId))
        attributes.recycle()
    }
}