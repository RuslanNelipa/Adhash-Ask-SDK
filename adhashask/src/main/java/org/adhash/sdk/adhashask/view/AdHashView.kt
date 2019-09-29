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
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import coil.api.load
import com.google.gson.GsonBuilder
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

private const val SCREENSHOT_HANDLER_DELAY = 2000L

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
    var placeholderDrawable: Drawable? = null
    var errorDrawable: Drawable? = null
    var screenshotUrl: String? = null
    var blockedAdUrl: String? = null

    var version: String? = null
        set(value) = vm.setUserProperties(version = value)
    var adTagId: String? = null
        set(value) = vm.setUserProperties(adTagId = value)
    var adOrder: Int? = null
        set(value) = vm.setUserProperties(adOrder = value)
    var analyticsUrl: String? = null
        set(value) = vm.setUserProperties(analyticsUrl = value)
    var timezone: Int? = null
        set(value) = vm.setUserProperties(timezone = value)
    var location: String? = null
        set(value) = vm.setUserProperties(location = value)
    var screenWidth: Int? = null
        set(value) = vm.setUserProperties(screenWidth = value)
    var screenHeight: Int? = null
        set(value) = vm.setUserProperties(screenHeight = value)
    var platform: String? = null
        set(value) = vm.setUserProperties(platform = value)
    var language: String? = null
        set(value) = vm.setUserProperties(language = value)
    var device: String? = null
        set(value) = vm.setUserProperties(device = value)
    var model: String? = null
        set(value) = vm.setUserProperties(model = value)
    var type: String? = null
        set(value) = vm.setUserProperties(type = value)
    var connection: String? = null
        set(value) = vm.setUserProperties(connection = value)
    var isp: String? = null
        set(value) = vm.setUserProperties(isp = value)
    var orientation: String? = null
        set(value) = vm.setUserProperties(orientation = value)
    var gps: String? = null
        set(value) = vm.setUserProperties(gps = value)
    var creativesSize: String? = null
        set(value) = vm.setUserProperties(creativesSize = value)

    private var onError: ((error: String) -> Unit)? = null

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
        vm.buildBidderProperty(
            creatives = arrayListOf(
                AdSizes(
                    size = "${MeasureSpec.getSize(widthMeasureSpec)}" +
                            "x" +
                            "${MeasureSpec.getSize(heightMeasureSpec)}"
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

    fun setAnalyticsCallbacks(
        onAnalyticsSuccess: (body: String) -> Unit,
        onAnalyticsError: (error: Throwable) -> Unit
    ) {
        vm.setAnalyticsCallbacks(onAnalyticsSuccess, onAnalyticsError)
    }

    fun setErrorCallback(
        onError: (error: String) -> Unit
    ) {
        this.onError = onError
    }

    fun requestNewAd() = vm.fetchBidderAttempt()

    private fun consumeAttrs(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AdHashView)

        try {
            vm.buildBidderProperty(publisherId = attributes.getString(R.styleable.AdHashView_publisherId))
            placeholderDrawable = attributes.getDrawable(R.styleable.AdHashView_placeholderDrawable)
            errorDrawable = attributes.getDrawable(R.styleable.AdHashView_errorDrawable)
            screenshotUrl = attributes.getString(R.styleable.AdHashView_screenshotUrl)
            version = attributes.getString(R.styleable.AdHashView_version)
            adTagId = attributes.getString(R.styleable.AdHashView_adTagId)
            adOrder = attributes.getInteger(R.styleable.AdHashView_adOrder, 0)
            analyticsUrl = attributes.getString(R.styleable.AdHashView_analyticsUrl)
            blockedAdUrl = attributes.getString(R.styleable.AdHashView_blockAdUrl)
            timezone = attributes.getInt(R.styleable.AdHashView_timezone, 0)
            location = attributes.getString(R.styleable.AdHashView_location)
            screenWidth = attributes.getInt(R.styleable.AdHashView_screenWidth, 0)
            screenHeight = attributes.getInt(R.styleable.AdHashView_screenHeight, 0)
            platform = attributes.getString(R.styleable.AdHashView_platform)
            language = attributes.getString(R.styleable.AdHashView_language)
            device = attributes.getString(R.styleable.AdHashView_device)
            model = attributes.getString(R.styleable.AdHashView_model)
            type = attributes.getString(R.styleable.AdHashView_type)
            connection = attributes.getString(R.styleable.AdHashView_connection)
            isp = attributes.getString(R.styleable.AdHashView_isp)
            orientation = attributes.getString(R.styleable.AdHashView_orientation)
            gps = attributes.getString(R.styleable.AdHashView_gps)
            creativesSize = attributes.getString(R.styleable.AdHashView_creativesSize)
            Log.d(TAG, "Attributes extracted")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to read attributes")
        } finally {
            attributes.recycle()

            vm.setUserProperties(
                adTagId = adTagId,
                version = version,
                adOrder = adOrder,
                analyticsUrl = analyticsUrl,
                timezone = timezone,
                location = location,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                platform = platform,
                language = language,
                device = device,
                model = model,
                type = type,
                connection = connection,
                isp = isp,
                orientation = orientation,
                gps = gps,
                creativesSize = creativesSize
            )
        }
    }

    private fun addTouchDetector() {
        setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                vm.attachCoordinatesToUri(motionEvent.x, motionEvent.y)
                openUri()
            }
            true
        }
    }

    private fun handleError(reason: String) {
        Log.e(TAG, "Ad load failed: $reason")
        load(errorDrawable)
        onError?.invoke(reason)
    }

    private fun loadAdBitmap(bitmap: Bitmap, recentAd: RecentAd) {
        load(bitmap) {
            crossfade(true)
            error(errorDrawable)
            placeholder(placeholderDrawable)
            listener(
                onSuccess = { _, _ -> vm.onAdDisplayed(recentAd) }
            )
        }
    }

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