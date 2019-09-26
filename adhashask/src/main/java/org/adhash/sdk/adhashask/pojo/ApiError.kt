package org.adhash.sdk.adhashask.pojo

data class ApiError(
    val errorCase : ApiErrorCase,
    val errorMessage : String? = "",
    val requestPath : String,
    var shouldRetry : Boolean = false
) : Throwable()

enum class ApiErrorCase {
    NoInternet, BadRequest, HttpError, Unknown
}