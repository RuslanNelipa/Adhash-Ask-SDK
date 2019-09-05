package org.adhash.sdk.adhashask.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import org.adhash.sdk.adhashask.network.AdConnection
import org.adhash.sdk.adhashask.pojo.InfoBody
import org.adhash.sdk.adhashask.pojo.ScreenSize
import java.util.*
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.view.WindowManager
import org.adhash.sdk.adhashask.constants.LibConstants
import org.adhash.sdk.adhashask.pojo.Navigator
import org.adhash.sdk.BuildConfig
import kotlin.collections.ArrayList
import kotlin.math.sqrt


class MakeInfoBodyUtil {

    private val log = AdConnection::class.java.name

    fun gatherAllInfo(mCtx: Context): InfoBody {

        val timeZone = getTimeZone()
        Log.e(log, "Value of TimeZone $timeZone")

        val locationId = "http://${BuildConfig.APPLICATION_ID}"

        Log.e(log, "Value of locationId $locationId")

        val screenSize = getScreenSizes(mCtx)
        Log.e(log, "Value of screenSize $screenSize")

        val platform = "Android API" + Build.VERSION.SDK_INT
        Log.e(log, "Value of platform $platform")
        val language = Locale.getDefault().language
        Log.e(log, "Value of language $language")
        val device = Build.BRAND
        Log.e(log, "Value of BRAND $device")
        val model = Build.MODEL
        Log.e(log, "Value of model $model")
        val type = getPhoneType(mCtx)
        Log.e(log, "Value of type $type")
        val navigator = Navigator(platform, language, device, model, type)

        val connection = getConnectionType(mCtx)
        Log.e(log, "Value of connection $connection")

        val orientation = getOrientationScreen(mCtx)
        Log.e(log, "Value of orientation $orientation")

        val unixTime = getTimeInUnix()
        Log.e(log, "Value of unixTime $unixTime")
        return InfoBody(timeZone, locationId, null, screenSize, navigator, connection, "...", orientation, "...", ArrayList(), true, ArrayList(), unixTime, ArrayList())

        // todo get internetID, gpsCoordinates, set PublisherId, blocked Ads, recently Ads
    }

    private fun getTimeZone(): Int {
        var timeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
        timeZone = timeZone.substring(3,6)
        return timeZone.toInt()
    }

    private fun getScreenSizes(mCtx: Context) : ScreenSize {
        val windowManager = mCtx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        return ScreenSize(width, height)
    }

    private fun getPhoneType(mCtx: Context): String {
        val metrics = mCtx.resources.displayMetrics
        val yInches = metrics.heightPixels / metrics.ydpi
        val xInches = metrics.widthPixels / metrics.xdpi
        val diagonalInches = sqrt((xInches * xInches + yInches * yInches).toDouble())
        return if (diagonalInches >= 6.5) {
            LibConstants.tablet
        } else {
            LibConstants.mobile
        }
    }

    private fun getConnectionType(mCtx: Context) : String {
        val connMgr = mCtx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val network = connMgr.activeNetwork
            val capabilities = connMgr.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                return LibConstants.CONNECTION_WIFI
            } else if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                val tm = mCtx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                when(tm.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).dataNetworkType){
                    TelephonyManager.NETWORK_TYPE_1xRTT -> return LibConstants.CONNECTION_1xRTT
                    TelephonyManager.NETWORK_TYPE_CDMA -> return LibConstants.CONNECTION_CDMA
                    TelephonyManager.NETWORK_TYPE_EDGE -> return LibConstants.CONNECTION_EDGE
                    TelephonyManager.NETWORK_TYPE_GPRS -> return LibConstants.CONNECTION_GPRS
                    TelephonyManager.NETWORK_TYPE_IDEN -> return LibConstants.CONNECTION_IDEN
                    TelephonyManager.NETWORK_TYPE_GSM -> return LibConstants.CONNECTION_GSM
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> return LibConstants.CONNECTION_EVDO_0
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> return LibConstants.CONNECTION_EVDO_A
                    TelephonyManager.NETWORK_TYPE_EVDO_B -> return LibConstants.CONNECTION_EVDO_B
                    TelephonyManager.NETWORK_TYPE_HSDPA -> return LibConstants.CONNECTION_HSDPA
                    TelephonyManager.NETWORK_TYPE_HSPA -> return LibConstants.CONNECTION_HSPA
                    TelephonyManager.NETWORK_TYPE_HSPAP -> return LibConstants.CONNECTION_HSPAP
                    TelephonyManager.NETWORK_TYPE_HSUPA -> return LibConstants.CONNECTION_HSUPA
                    TelephonyManager.NETWORK_TYPE_UMTS -> return LibConstants.CONNECTION_UMTS
                    TelephonyManager.NETWORK_TYPE_EHRPD -> return LibConstants.CONNECTION_EHRPD
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return LibConstants.CONNECTION_TD_SCDMA
                    TelephonyManager.NETWORK_TYPE_LTE -> return LibConstants.CONNECTION_LTE
                    TelephonyManager.NETWORK_TYPE_IWLAN -> return LibConstants.CONNECTION_IWLAN
                    TelephonyManager.NETWORK_TYPE_NR -> return LibConstants.CONNECTION_NR
                    TelephonyManager.NETWORK_TYPE_UNKNOWN -> return LibConstants.CONNECTION_UNKNOWN
                }
            }
        } else { todo new method need addition permission*/
            // delete when minSdkVersion update to 24
            connMgr.activeNetworkInfo?.let { networkInfo ->
                if (networkInfo.type == ConnectivityManager.TYPE_WIFI){
                    return LibConstants.CONNECTION_WIFI
                } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE){
                    when (networkInfo.subtype) {
                        TelephonyManager.NETWORK_TYPE_1xRTT -> return LibConstants.CONNECTION_1xRTT
                        TelephonyManager.NETWORK_TYPE_CDMA -> return LibConstants.CONNECTION_CDMA
                        TelephonyManager.NETWORK_TYPE_EDGE -> return LibConstants.CONNECTION_EDGE
                        TelephonyManager.NETWORK_TYPE_GPRS -> return LibConstants.CONNECTION_GPRS
                        TelephonyManager.NETWORK_TYPE_IDEN -> return LibConstants.CONNECTION_IDEN
                        TelephonyManager.NETWORK_TYPE_GSM -> return LibConstants.CONNECTION_GSM
                        TelephonyManager.NETWORK_TYPE_EVDO_0 -> return LibConstants.CONNECTION_EVDO_0
                        TelephonyManager.NETWORK_TYPE_EVDO_A -> return LibConstants.CONNECTION_EVDO_A
                        TelephonyManager.NETWORK_TYPE_EVDO_B -> return LibConstants.CONNECTION_EVDO_B
                        TelephonyManager.NETWORK_TYPE_HSDPA -> return LibConstants.CONNECTION_HSDPA
                        TelephonyManager.NETWORK_TYPE_HSPA -> return LibConstants.CONNECTION_HSPA
                        TelephonyManager.NETWORK_TYPE_HSPAP -> return LibConstants.CONNECTION_HSPAP
                        TelephonyManager.NETWORK_TYPE_HSUPA -> return LibConstants.CONNECTION_HSUPA
                        TelephonyManager.NETWORK_TYPE_UMTS -> return LibConstants.CONNECTION_UMTS
                        TelephonyManager.NETWORK_TYPE_EHRPD -> return LibConstants.CONNECTION_EHRPD
                        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return LibConstants.CONNECTION_TD_SCDMA
                        TelephonyManager.NETWORK_TYPE_LTE -> return LibConstants.CONNECTION_LTE
                        TelephonyManager.NETWORK_TYPE_IWLAN -> return LibConstants.CONNECTION_IWLAN
                        TelephonyManager.NETWORK_TYPE_NR -> return LibConstants.CONNECTION_NR
                        TelephonyManager.NETWORK_TYPE_UNKNOWN -> return LibConstants.CONNECTION_UNKNOWN
                    }
                }
            }
//        }
        return LibConstants.CONNECTION_UNKNOWN
    }

    private fun getOrientationScreen(mCtx: Context): String{
        when (mCtx.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> return LibConstants.orientation_landscape
            Configuration.ORIENTATION_PORTRAIT -> return LibConstants.orientation_portrait
        }
        return LibConstants.CONNECTION_UNKNOWN
    }

    private fun getTimeInUnix(): Long {
        val cal = Calendar.getInstance()
        val timeZone = cal.timeZone
        val calDate = Calendar.getInstance(TimeZone.getDefault()).time
        var milliseconds = calDate.time
        milliseconds += timeZone.getOffset(milliseconds)
        return milliseconds / 1000L
    }
}