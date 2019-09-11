package org.adhash.sdk.adhashask.storage

import android.content.Context
import com.google.gson.Gson
import org.adhash.sdk.adhashask.pojo.RecentAd

class AdsStorage(context: Context) {

    private val preferenceName = context.packageName + ".storage.ads"
    private var prefs = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveRecentAd(recentAd: RecentAd) {
        prefs.edit().putString(recentAd.adId, gson.toJson(recentAd)).apply()
    }

    fun getAllRecentAds(advertiserId: String) = prefs.all.values
        .filterIsInstance<String>()
        .map { gson.fromJson(it, RecentAd::class.java) }
        .filter { it.advertiserId == advertiserId }
        .toList()

    fun getAllRecentAds() = prefs.all.values
        .filterIsInstance<String>()
        .map { gson.fromJson(it, RecentAd::class.java) }
        .toList()
}