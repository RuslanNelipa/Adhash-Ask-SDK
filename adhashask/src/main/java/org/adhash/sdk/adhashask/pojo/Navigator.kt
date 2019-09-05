package org.adhash.sdk.adhashask.pojo

import com.google.gson.annotations.SerializedName

data class Navigator(
    @SerializedName("platform") var platformOfSystem: String,
    @SerializedName("language") var languageOfSystem: String,
    @SerializedName("device") var deviceBrand: String,
    @SerializedName("model") var modelOfDevice: String,
    @SerializedName("type") var type: String
)