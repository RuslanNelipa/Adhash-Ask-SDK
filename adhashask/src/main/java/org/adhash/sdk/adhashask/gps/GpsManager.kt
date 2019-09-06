package org.adhash.sdk.adhashask.gps

import android.content.Context

class GpsManager(private val context: Context){
    fun tryGetCoordinates(onSuccess: (Pair<Double, Double>) -> Unit,
                          doFinally: () -> Unit): Pair<Double, Double>{
        return Pair(10.10, 20.20) //todo
    }
}