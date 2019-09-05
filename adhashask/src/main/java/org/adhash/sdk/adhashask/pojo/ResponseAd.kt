package org.adhash.sdk.adhashask.pojo

import com.google.gson.annotations.SerializedName

data class ResponseAd(
    @SerializedName("advertiserId") var advertiserId: String,
    @SerializedName("advertiserURL") var advertiserURL: String,
    @SerializedName("expectedHashes") var expectedHashes: ArrayList<String>,
    @SerializedName("maxPrice") var maxPrice: Double,
    @SerializedName("commission") var commission: Double,
    @SerializedName("budgetId") var budgetId: Int,
    @SerializedName("bidId") var bidId: Int
)