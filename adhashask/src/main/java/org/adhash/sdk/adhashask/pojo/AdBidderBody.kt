package org.adhash.sdk.adhashask.pojo

data class AdBidderBody(
    val timezone: Int,
    val location: String,
    val publisherId: String,
    val size: ScreenSize,
    val referrer: String,
    val navigator: Navigator,
    val connection: String,
    val isp: String,
    val orientation: String,
    val gps: String,
    val mobile: Boolean = true,
    val currentTimestamp: Long,
    val creatives: ArrayList<AdSizes>,
    val blockedAdvertisers: ArrayList<String>,
    val recentAdvertisers: ArrayList<String>
)