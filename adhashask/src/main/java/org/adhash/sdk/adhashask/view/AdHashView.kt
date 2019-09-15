package org.adhash.sdk.adhashask.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import coil.api.load
import org.adhash.sdk.R
import org.adhash.sdk.adhashask.constants.Global
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.pojo.RecentAd
import org.adhash.sdk.adhashask.storage.AdsStorage
import org.adhash.sdk.adhashask.utils.DataEncryptor
import org.adhash.sdk.adhashask.utils.SystemInfo


private val TAG = Global.SDK_TAG + AdHashView::class.java.simpleName

class AdHashView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {
    private val vm = AdHashVm(
        systemInfo = SystemInfo(context),
        gpsManager = GpsManager(context),
        adsStorage = AdsStorage(context),
        apiClient = ApiClient(),
        dataEncryptor = DataEncryptor()
    )

    /*Attributes*/
    private var placeholderDrawable: Drawable? = null
    private var errorDrawable: Drawable? = null

    init {
        consumeAttrs(attrs)
    }

    private fun openUrl() {
        vm.getUri()?.let { uri ->
            Intent(Intent.ACTION_VIEW)
                .apply { data = uri }
                .also { intent -> context.startActivity(intent) }
        } ?: handleError("URL not found")
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
        setOnClickListener { openUrl() }
        vm.onAttachedToWindow(
            onBitmapReceived = ::loadAdBitmap,
            onError = ::handleError
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        vm.onDetachedFromWindow()
    }
    /*END VIEW LIFECYCLE*/

    private fun consumeAttrs(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AdHashView)

        try {
            vm.setBidderProperty(publisherId = attributes.getString(R.styleable.AdHashView_publisherId))
            placeholderDrawable = attributes.getDrawable(R.styleable.AdHashView_placeholderDrawable)
            errorDrawable = attributes.getDrawable(R.styleable.AdHashView_errorDrawable)
            Log.d(TAG, "Attributes extracted")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to read attributes")
        } finally {
            attributes.recycle()
        }
    }

    private fun handleError(reason: String) {
        Log.e(TAG, "Ad load failed: $reason")
        load(getErrorDrawable())
    }

    private fun loadAdBitmap(bitmap: Bitmap, recentAd: RecentAd) {
        load(bitmap) {
            crossfade(true)
            error(getErrorDrawable())
            placeholder(placeholderDrawable)
            listener(
                onSuccess = { _, _ -> vm.onAdDisplayed(recentAd) }
            )
        }
    }

    private fun getErrorDrawable() = errorDrawable ?: ContextCompat.getDrawable(context, R.drawable.ic_cross_24)
}