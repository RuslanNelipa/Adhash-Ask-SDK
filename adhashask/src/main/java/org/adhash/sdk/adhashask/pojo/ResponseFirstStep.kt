package org.adhash.sdk.adhashask.pojo

import com.google.gson.annotations.SerializedName

data class ResponseFirstStep(
    @SerializedName("status") var status: String,
    @SerializedName("creatives") var creatives: ArrayList<ResponseAd>,
    @SerializedName("period") var period: Int,
    @SerializedName("nonce") var nonce: Int
)