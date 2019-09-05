package org.adhash.sdk.adhashask.pojo

import com.google.gson.annotations.SerializedName

data class InfoBody(
    @SerializedName("timezone") var timezone: Int,
    @SerializedName("location") var locationId: String,
    @SerializedName("publisherId") var publisherId: String?,
    @SerializedName("size") var screenSize: ScreenSize,
    @SerializedName("navigator") var navigator: Navigator,
    @SerializedName("connection") var connectionType: String,
    @SerializedName("isp") var idOfInternetProvider: String,
    @SerializedName("orientation") var orientation: String,
    @SerializedName("gps") var gps: String,
    @SerializedName("creatives") var creatives: ArrayList<AdSizes>,
    @SerializedName("mobile") var mobile: Boolean,
    @SerializedName("blockedAdvertisers") var blockedAdvertisers: ArrayList<String>,
    @SerializedName("currentTimestamp") var currentTimestamp: Long,
    @SerializedName("recentAds") var recentAds: ArrayList<RecentAd>
    )