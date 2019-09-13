package org.adhash.sdk.adhashask.pojo

data class RecentAd(
    val timestamp: Long,
    val advertiserId: String,
    val campaignId: Int,
    val adId: String
)