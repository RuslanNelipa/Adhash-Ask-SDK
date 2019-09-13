package org.adhash.sdk.adhashask.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ImageView
import org.adhash.sdk.R
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.storage.AdsStorage
import org.adhash.sdk.adhashask.utils.DataEncryptor
import org.adhash.sdk.adhashask.utils.SystemInfo


class AdHashView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {
    private val vm = AdHashVm(
        systemInfo = SystemInfo(context),
        gpsManager = GpsManager(context),
        adsStorage = AdsStorage(context),
        apiClient = ApiClient(),
        dataEncryptor = DataEncryptor()
    )

    init {
        consumeAttrs(attrs)
    }

    /*START VIEW LIFECYCLE*/
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        vm.setBidderProperty(
            creatives = arrayListOf(
                AdSizes(
                    size = "${MeasureSpec.getSize(widthMeasureSpec)}" +
                            "x" +
                            "${MeasureSpec.getSize(heightMeasureSpec)}"
                ),
                AdSizes(//todo remove. It's for tests
                    size = "728x90"
                ),
                AdSizes(//todo remove. It's for tests
                    size = "350x250"
                )
            )
        )
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        vm.onViewDisplayed()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        vm.onViewDetached()
    }
    /*END VIEW LIFECYCLE*/

    private fun consumeAttrs(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AdHashView)
        vm.setBidderProperty(publisherId = attributes.getString(R.styleable.AdHashView_publisherId))
        attributes.recycle()
    }
}