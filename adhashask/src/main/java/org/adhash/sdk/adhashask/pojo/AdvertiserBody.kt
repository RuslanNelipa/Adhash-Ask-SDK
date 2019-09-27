package org.adhash.sdk.adhashask.pojo


data class AdvertiserBody(
    var orientation: String? = null,
    var blockedAdvertisers: List<Any?>? = null,
    var period: Int? = null,
    var expectedHashes: List<String?>? = null,
    var recentAdvertisers: List<RecentAd>? = null,
    var timezone: Int? = null,
    var isp: String? = null,
    var navigator: Navigator? = null,
    var creatives: List<AdSizes?>? = null,
    var mobile: Boolean? = null,
    var budgetId: Int? = null,
    var gps: String? = null,
    var nonce: Int? = null,
    var publisherId: String? = null,
    var size: ScreenSize? = null,
    var currentTimestamp: Long? = null,
    var location: String? = null,
    var connection: String? = null,
    var url: String? = null
)