package org.adhash.sdk.adhashask.pojo

data class KeyBody(
    val nonce: Int?,
    val period: Int?,
    var timezone: Int? = null,
    var location: String? = null,
    var publisherId: String? = null,
    var size: ScreenSize? = null,
    var referrer: String? = null,
    var navigator: Navigator? = null,
    var connection: String? = null,
    var isp: String? = null,
    var orientation: String? = null,
    var gps: String? = null,
    var currentTimestamp: Long? = null,
    var creatives: ArrayList<AdSizes>? = null,
    var blockedAdvertisers: ArrayList<String>? = null,
    var recentAdvertisers: List<RecentAd>? = null,
    var mobile: Boolean = true
)