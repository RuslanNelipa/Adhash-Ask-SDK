package org.adhash.sdk.adhashask.pojo

import org.adhash.sdk.adhashask.base.BaseResponse

data class AdBidderResponse(
    val creatives: ArrayList<ResponseAd>?,
    val period: Int?,
    val nonce: Int?

) : BaseResponse()