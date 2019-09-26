package org.adhash.sdk.adhashask.pojo

data class AnalyticsBody(
    var adTagId: String? = null,
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
    var screenWidth: String? = null,
    var screenHeight: String? = null,
    var timeZone: String? = null,
    var width: String? = null,
    var height: String? = null,
    var period: String? = null,
    var cost: String? = null,
    var comission: String? = null,
    var nonce: String? = null,
    var pageview: Boolean? = null,
    var mobile: Boolean? = null
)