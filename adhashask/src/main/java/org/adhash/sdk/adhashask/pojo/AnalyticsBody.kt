package org.adhash.sdk.adhashask.pojo

data class AnalyticsBody(
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
)