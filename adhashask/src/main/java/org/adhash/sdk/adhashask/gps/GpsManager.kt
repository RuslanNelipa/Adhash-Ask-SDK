package org.adhash.sdk.adhashask.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat

class GpsManager(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun tryGetCoordinates(
        onSuccess: (Pair<Double, Double>) -> Unit,
        onError: () -> Unit,
        doFinally: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError()
        } else {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (statusOfGPS) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            location?.let { receivedLoc ->
                                onSuccess(Pair(receivedLoc.latitude, receivedLoc.longitude))
                                locationManager.removeUpdates(this)
                                doFinally()
                            }
                        }

                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {
                        }

                        override fun onProviderEnabled(provider: String?) {}
                        override fun onProviderDisabled(provider: String?) {}
                    })
            } else {
                onError()
            }
        } //todo
    }
}