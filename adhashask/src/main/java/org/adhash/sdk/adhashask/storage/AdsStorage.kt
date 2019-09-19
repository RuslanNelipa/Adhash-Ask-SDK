package org.adhash.sdk.adhashask.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.adhash.sdk.adhashask.ext.genericType
import org.adhash.sdk.adhashask.pojo.RecentAd

class AdsStorage(context: Context, private val gson: Gson) {
    private val preferenceName = context.packageName + ".storage.ads"
    private var prefs = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun saveRecentAd(recentAd: RecentAd) {
        prefs.edit().putString(recentAd.timestamp.toString(), gson.toJson(recentAd)).apply()
    }

    fun getAllRecentAds() = prefs.all.values
        .filterIsInstance<String>()
        .map { gson.fromJson<RecentAd>(it, RecentAd::class.java) }
        .toList()
}