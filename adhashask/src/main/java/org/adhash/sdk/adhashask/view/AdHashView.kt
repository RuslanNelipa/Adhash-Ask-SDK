package org.adhash.sdk.adhashask.view

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import coil.api.load
import com.google.gson.GsonBuilder
import org.adhash.sdk.R
import org.adhash.sdk.adhashask.constants.Global
import org.adhash.sdk.adhashask.gps.GpsManager
import org.adhash.sdk.adhashask.network.ApiClient
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.pojo.RecentAd
import org.adhash.sdk.adhashask.storage.AdsStorage
import org.adhash.sdk.adhashask.utils.Aes
import org.adhash.sdk.adhashask.utils.DataEncryptor
import org.adhash.sdk.adhashask.utils.SystemInfo

private val TAG = Global.SDK_TAG + AdHashView::class.java.simpleName

private const val SCREENSHOT_HANDLER_DELAY = 3000L

class AdHashView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {

    private val gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    private val vm = AdHashVm(
        context = context,
        systemInfo = SystemInfo(context),
        gpsManager = GpsManager(context),
        adsStorage = AdsStorage(context, gson),
        apiClient = ApiClient(gson),
        dataEncryptor = DataEncryptor(gson)
    )

    /*Attributes*/
    private var placeholderDrawable: Drawable? = null
    private var errorDrawable: Drawable? = null
    private var screenshotUrl: String? = null
    private var version: String? = null
    private var adTagId: String? = null
    private var adOrder: Int? = null

    private var screenshotUrlOpened = false
    private val screenshotHandler = Handler()
    private val screenshotRunnable by lazy {
        val delay = SCREENSHOT_HANDLER_DELAY
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        ScreenshotRunnable(delay, am)
    }

    init {
        consumeAttrs(attrs)
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        vm.setBidderProperty(
            creatives = arrayListOf(
//                AdSizes(
//                    size = "${MeasureSpec.getSize(widthMeasureSpec)}" +
//                            "x" +
//                            "${MeasureSpec.getSize(heightMeasureSpec)}"
//                ),
//                AdSizes(//todo remove. It's for tests
//                    size = "728x90"
//                )
//            ,
                AdSizes(//todo remove. It's for tests
                    size = "300x250"
                )
            )
        )
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addTouchDetector()
        vm.onAttachedToWindow(
            onBitmapReceived = ::loadAdBitmap,
            onError = ::handleError
        )
        disableAdForVisionImpaired()
        detectScreenShotService()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        vm.onDetachedFromWindow()
        stopDetectingScreenshots()
    }
    /*END VIEW LIFECYCLE*/

    private fun consumeAttrs(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AdHashView)

        try {
            vm.setBidderProperty(publisherId = attributes.getString(R.styleable.AdHashView_publisherId))
            placeholderDrawable = attributes.getDrawable(R.styleable.AdHashView_placeholderDrawable)
            errorDrawable = attributes.getDrawable(R.styleable.AdHashView_errorDrawable)
            screenshotUrl = attributes.getString(R.styleable.AdHashView_screenshotUrl)
            version = attributes.getString(R.styleable.AdHashView_version)
            adTagId = attributes.getString(R.styleable.AdHashView_adTagId)
            adOrder = attributes.getInteger(R.styleable.AdHashView_adOrder, 0)
            Log.d(TAG, "Attributes extracted")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to read attributes")
        } finally {
            attributes.recycle()

            vm.setUserProperties(
                adTagId = adTagId,
                version = version,
                adOrder = adOrder
            )
        }
    }

    private fun addTouchDetector() {
        setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN)
                openUri()
            true
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

    private fun disableAdForVisionImpaired() {
        if (vm.isTalkbackEnabled()) visibility = View.GONE
    }

    private fun openUri() = vm.getUri()?.let { uri ->
        Intent(Intent.ACTION_VIEW)
            .apply { data = uri }
            .also { intent -> context.startActivity(intent) }
    } ?: handleError("URL not found")

    private fun openScreenshotUrl() = screenshotUrl?.let {
        Intent(Intent.ACTION_VIEW)
            .apply { data = Uri.parse(it) }
            .also { intent -> context.startActivity(intent) }
    }

    private fun detectScreenShotService() {
        screenshotUrl?.let {
            screenshotHandler.postDelayed(screenshotRunnable, SCREENSHOT_HANDLER_DELAY)
        }
    }

    private fun stopDetectingScreenshots() {
        screenshotHandler.removeCallbacks(screenshotRunnable)
    }

    private inner class ScreenshotRunnable(
        private val delay: Long,
        private val activityManager: ActivityManager
    ) : Runnable {
        override fun run() {
            activityManager.getRunningServices(200)
                .filter { it.process == "com.android.systemui:screenshot" }
                .takeIf { !screenshotUrlOpened }
                ?.forEach { _ ->
                    openScreenshotUrl()
                    screenshotUrlOpened = true
                }
                ?.also { screenshotHandler.postDelayed(this, delay) }

        }
    }
}