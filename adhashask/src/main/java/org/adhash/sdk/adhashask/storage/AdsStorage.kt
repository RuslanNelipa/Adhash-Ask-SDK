package org.adhash.sdk.adhashask.storage

import android.content.Context
import com.google.gson.Gson
import org.adhash.sdk.adhashask.pojo.RecentAd

class AdsStorage(context: Context, private val gson: Gson) {
    private var recentAdsPrefs = context.getSharedPreferences(context.packageName + ".recent.ads", Context.MODE_PRIVATE)

    fun saveRecentAd(recentAd: RecentAd) {
        recentAdsPrefs.edit().putString(recentAd.timestamp.toString(), gson.toJson(recentAd)).apply()
    }

    fun getAllRecentAds() = recentAdsPrefs.all.values
        .filterIsInstance<String>()
        .map { gson.fromJson<RecentAd>(it, RecentAd::class.java) }
        .toList()

    fun clear() {
        recentAdsPrefs.edit().clear().apply()
    }
}