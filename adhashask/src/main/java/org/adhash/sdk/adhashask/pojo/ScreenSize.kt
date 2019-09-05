package org.adhash.sdk.adhashask.pojo

import com.google.gson.annotations.SerializedName

data class ScreenSize(
    @SerializedName("screenWidth") var screenWidthInPx: Int,
    @SerializedName("screenHeight") var screenHeightInPx: Int
)