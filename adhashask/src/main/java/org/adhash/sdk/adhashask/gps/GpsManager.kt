package org.adhash.sdk.adhashask.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat

private const val GPS_GRACE_ACCURACY_LIMIT = 200
private const val GPS_FETCH_TIMEOUT = 2000L

class GpsManager(private val context: Context) {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    @SuppressLint("MissingPermission")
    fun tryGetCoordinates(
        onSuccess: (Pair<Double, Double>) -> Unit,
        doFinally: () -> Unit
    ) {

        when {
            !isPermissionGranted() || !isGpsEnabled() -> doFinally()
            else -> fetchLocation(onSuccess, doFinally)
        }
    }

    private fun isPermissionGranted() = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun isGpsEnabled() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun getLastKnowLocation() = try {
        locationManager
            .getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?.takeIf { it.accuracy < GPS_GRACE_ACCURACY_LIMIT }
    } catch (e: SecurityException) {
        null
    }

    private fun fetchLocation(
        onSuccess: (Pair<Double, Double>) -> Unit,
        doFinally: () -> Unit
    ) {
        getLastKnowLocation()?.let { location ->
            onSuccess(Pair(location.latitude, location.longitude))
            doFinally()

        } ?: run {
            requestLocationUpdates(onSuccess, doFinally)
        }
    }

    private fun requestLocationUpdates(
        onSuccess: (Pair<Double, Double>) -> Unit,
        doFinally: () -> Unit
    ) {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0f, TimeoutLocationListener(onSuccess, doFinally)
            )
        } catch (e: SecurityException) {
            doFinally()
        }
    }

    inner class TimeoutLocationListener(
        private val onSuccess: (Pair<Double, Double>) -> Unit,
        private val doFinally: () -> Unit
    ) : LocationListener {

        private val timeoutHandler = Handler()
        private val timeoutRunnable = Runnable {
            locationManager.removeUpdates(this)
            doFinally()
        }

        init {
            timeoutHandler.postDelayed(timeoutRunnable, GPS_FETCH_TIMEOUT)
        }

        override fun onLocationChanged(location: Location?) {
            location?.let {
                onSuccess(Pair(location.latitude, location.longitude))
                doFinally()
                locationManager.removeUpdates(this)
                timeoutHandler.removeCallbacks(timeoutRunnable)

            } ?: doFinally()
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

        override fun onProviderEnabled(p0: String?) {}

        override fun onProviderDisabled(p0: String?) {}

    }
}