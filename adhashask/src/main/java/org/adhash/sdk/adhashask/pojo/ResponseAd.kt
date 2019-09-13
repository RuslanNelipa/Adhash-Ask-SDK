package org.adhash.sdk.adhashask.pojo

data class ResponseAd(
    val advertiserId: String?,
    val advertiserURL: String?,
    val expectedHashes: ArrayList<String>?,
    val maxPrice: Double?,
    val commission: Double?,
    val budgetId: Int?,
    val bidId: Int?
)