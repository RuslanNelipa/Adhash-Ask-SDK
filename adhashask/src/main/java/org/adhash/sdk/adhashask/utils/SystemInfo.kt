package org.adhash.sdk.adhashask.utils

import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.res.Configuration
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import org.adhash.sdk.BuildConfig
import org.adhash.sdk.adhashask.constants.ConnectionType
import org.adhash.sdk.adhashask.constants.DeviceType
import org.adhash.sdk.adhashask.constants.Orientation
import java.util.*
import kotlin.math.sqrt


class SystemInfo(private val context: Context) {

    fun getTimeZone(): Int {
        val timeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
        return timeZone.takeIf { it.length > 6 }
            ?.let { timeZone.substring(3, 6) }
            ?.toInt()
            ?: 0
    }

    fun getScreenHeight(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display?.getSize(size)

        return size.y
    }

    fun getScreenWidth(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display?.getSize(size)

        return size.x
    }

    fun getPhoneType(): String {
        val metrics = context.resources.displayMetrics
        val yInches = metrics.heightPixels / metrics.ydpi
        val xInches = metrics.widthPixels / metrics.xdpi
        val diagonalInches = sqrt((xInches * xInches + yInches * yInches).toDouble())
        return if (diagonalInches >= 6.5) {
            DeviceType.TABLET
        } else {
            DeviceType.MOBILE
        }
    }

    @Suppress("DEPRECATION")
    fun getConnectionType(): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            capabilities?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.TRANSPORT_WIFI
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.TRANSPORT_CELULLAR
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.TRANSPORT_ETHERNET
                    hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> ConnectionType.TRANSPORT_BLUETOOTH
                    else -> ConnectionType.CONNECTION_UNKNOWN
                }
            } ?: ConnectionType.CONNECTION_UNKNOWN
        } else {
            connectivityManager.activeNetworkInfo?.let { networkInfo ->
                when {
                    networkInfo.type == ConnectivityManager.TYPE_WIFI -> ConnectionType.TRANSPORT_WIFI
                    networkInfo.type == ConnectivityManager.TYPE_MOBILE -> when (networkInfo.subtype) {
                        TelephonyManager.NETWORK_TYPE_1xRTT -> ConnectionType.CONNECTION_1xRTT
                        TelephonyManager.NETWORK_TYPE_CDMA -> ConnectionType.CONNECTION_CDMA
                        TelephonyManager.NETWORK_TYPE_EDGE -> ConnectionType.CONNECTION_EDGE
                        TelephonyManager.NETWORK_TYPE_GPRS -> ConnectionType.CONNECTION_GPRS
                        TelephonyManager.NETWORK_TYPE_IDEN -> ConnectionType.CONNECTION_IDEN
                        TelephonyManager.NETWORK_TYPE_GSM -> ConnectionType.CONNECTION_GSM
                        TelephonyManager.NETWORK_TYPE_EVDO_0 -> ConnectionType.CONNECTION_EVDO_0
                        TelephonyManager.NETWORK_TYPE_EVDO_A -> ConnectionType.CONNECTION_EVDO_A
                        TelephonyManager.NETWORK_TYPE_EVDO_B -> ConnectionType.CONNECTION_EVDO_B
                        TelephonyManager.NETWORK_TYPE_HSDPA -> ConnectionType.CONNECTION_HSDPA
                        TelephonyManager.NETWORK_TYPE_HSPA -> ConnectionType.CONNECTION_HSPA
                        TelephonyManager.NETWORK_TYPE_HSPAP -> ConnectionType.CONNECTION_HSPAP
                        TelephonyManager.NETWORK_TYPE_HSUPA -> ConnectionType.CONNECTION_HSUPA
                        TelephonyManager.NETWORK_TYPE_UMTS -> ConnectionType.CONNECTION_UMTS
                        TelephonyManager.NETWORK_TYPE_EHRPD -> ConnectionType.CONNECTION_EHRPD
                        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> ConnectionType.CONNECTION_TD_SCDMA
                        TelephonyManager.NETWORK_TYPE_LTE -> ConnectionType.CONNECTION_LTE
                        TelephonyManager.NETWORK_TYPE_IWLAN -> ConnectionType.CONNECTION_IWLAN
                        else -> ConnectionType.CONNECTION_UNKNOWN
                    }
                    else -> ConnectionType.CONNECTION_UNKNOWN
                }
            } ?: ConnectionType.CONNECTION_UNKNOWN
        }
    }

    fun getOrientationScreen(): String {
        return when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
            Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
            else -> ConnectionType.CONNECTION_UNKNOWN
        }
    }

    fun getTimeInUnix(): Long {
        val cal = Calendar.getInstance()
        val timeZone = cal.timeZone
        val calDate = Calendar.getInstance(TimeZone.getDefault()).time
        var milliseconds = calDate.time
        milliseconds += timeZone.getOffset(milliseconds)
        return milliseconds / 1000
    }

    fun getPublishedLocation():String = context.packageName

    fun getPlatform() = "Android API ${getVersionCode()}"

    fun getUserAgent() = System.getProperty("http.agent") ?: ""

    fun getLanguage(): String = Locale.getDefault().displayLanguage

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        return if (model.startsWith(manufacturer))
            capitalizeDeviceModel(model)
        else
            capitalizeDeviceModel(manufacturer) + " " + model
    }

    fun getCarrierId() = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
        .run { networkOperatorName } ?: ""

    fun isTalkBackEnabled() =
        (context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager?)
            ?.run { isEnabled || isTouchExplorationEnabled } ?: false

    private fun capitalizeDeviceModel(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c))
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }

        return phrase.toString()
    }


    private fun getVersionCode() = Build.VERSION.SDK_INT

    fun getLibraryVersion() = BuildConfig.VERSION_NAME
}