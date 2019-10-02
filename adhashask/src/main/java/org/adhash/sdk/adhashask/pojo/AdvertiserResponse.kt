package org.adhash.sdk.adhashask.pojo

import org.adhash.sdk.adhashask.base.BaseResponse

data class AdvertiserResponse(
    val url: String,
    val data: String,
    val width: Int,
    val height: Int

) : BaseResponse()