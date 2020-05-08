package org.adhash.sdk.adhashask.pojo

data class AnalyticsBody(
    var version: String? = null,
    var adTagId: String? = null,
    var publishedId: String? = null,
    var creativeHash: String? = null,
    var advertiserId: String? = null,
    var pageURL: String? = null,
    var platform: String? = null,
    var connection: String? = null,
    var isp: String? = null,
    var orientation: String? = null,
    var gps: String? = null,
    var language: String? = null,
    var device: String? = null,
    var model: String? = null,
    var type: String? = null,
    var screenWidth: Number? = null,
    var screenHeight: Number? = null,
    var timeZone: Number? = null,
    var width: String? = null,
    var height: String? = null,
    var period: Number? = null,
    var cost: Number? = null,
    var comission: Number? = null,
    var nonce: Number? = null,
    var pageview: Boolean? = null,
    var mobile: Boolean? = null
) {
    fun toQueryMap(): Map<String, String?> {
        return mapOf(
            "version" to "$version",
            "adTagId" to "$adTagId",
            "publisherId" to "$publishedId",
            "creativeHash" to "$creativeHash",
            "advertiserId" to "$advertiserId",
            "pageURL" to "$pageURL",
            "platform" to "$platform",
            "connection" to "$connection",
            "isp" to "$isp",
            "orientation" to "$orientation",
            "gps" to "$gps",
            "language" to "$language",
            "device" to "$device",
            "model" to "$model",
            "type" to "$type",
            "screenWidth" to "$screenWidth",
            "screenHeight" to "$screenHeight",
            "timeZone" to "$timeZone",
            "width" to "$width",
            "height" to "$height",
            "period" to "$period",
            "cost" to "$cost",
            "comission" to "$comission",
            "nonce" to "$nonce",
            "pageview" to "$pageview",
            "mobile" to "$mobile"
        )
    }
}