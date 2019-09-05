package org.adhash.sdk.adhashask.pojo

data class AdBidderBody(
    val timezone: Int,
    val referrer: String,
    val location: String,
    val publisherId: String,
    val size: ScreenSize,
    val navigator: Navigator,
    val creatives: ArrayList<AdSizes>,
    val blockedAdvertisers: ArrayList<String>,
    val recentAdvertisers: ArrayList<String>
)