package org.adhash.sdk.adhashask.storage

import android.content.Context
import com.google.gson.Gson
import org.adhash.sdk.adhashask.pojo.RecentAd

class AdsStorage(context: Context, private val gson: Gson) {
    private var recentAdsPrefs = context.getSharedPreferences(context.packageName + ".recent.ads", Context.MODE_PRIVATE)
    private var blockedAdsPrefs = context.getSharedPreferences(context.packageName + ".blocked.ads", Context.MODE_PRIVATE)

    fun saveRecentAd(recentAd: RecentAd) {
        recentAdsPrefs.edit().putString(recentAd.timestamp.toString(), gson.toJson(recentAd)).apply()
    }

    fun saveBlockedAd(recentAd: RecentAd) {
        blockedAdsPrefs.edit().putString(recentAd.timestamp.toString(), recentAd.advertiserId).apply()
    }

    fun getAllRecentAds() = recentAdsPrefs.all.values
        .filterIsInstance<String>()
        .map { gson.fromJson<RecentAd>(it, RecentAd::class.java) }
        .toList()

    fun getAllBlockedAds() = blockedAdsPrefs.all.values
        .filterIsInstance<String>()
        .toMutableList()

    fun clear() {
        blockedAdsPrefs.edit().clear().apply()
        recentAdsPrefs.edit().clear().apply()
    }
}