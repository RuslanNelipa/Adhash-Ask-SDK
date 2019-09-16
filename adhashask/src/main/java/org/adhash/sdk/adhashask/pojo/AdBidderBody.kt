package org.adhash.sdk.adhashask.pojo

data class AdBidderBody(
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
    var mobile: Boolean = true,
    var currentTimestamp: Long? = null,
    var creatives: ArrayList<AdSizes>? = null,
    var blockedAdvertisers: ArrayList<String>? = null,
    var recentAdvertisers: List<List<Any?>?>? = null
) {
    override fun toString(): String {
        return "AdBidderBody(timezone=$timezone, " +
                "location=$location, " +
                "publisherId=$publisherId, " +
                "size=$size," +
                " referrer=$referrer," +
                " navigator=$navigator," +
                " connection=$connection," +
                " isp=$isp," +
                " orientation=$orientation," +
                " gps=$gps," +
                " mobile=$mobile," +
                " currentTimestamp=$currentTimestamp," +
                " creatives=$creatives," +
                " blockedAdvertisers=$blockedAdvertisers," +
                " recentAdvertisers=$recentAdvertisers)"
    }
}