package org.adhash.sdk.adhashask.network

import com.google.gson.JsonSyntaxException
import org.adhash.sdk.adhashask.base.BaseResponse
import org.adhash.sdk.adhashask.pojo.ApiError
import org.adhash.sdk.adhashask.pojo.ApiErrorCase
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandler {

    fun consumeThrowable(networkError: Throwable, requestPath: String): ApiError {
        val errorMessage = networkError.message?.let { it } ?: ""

        val case =  when (networkError) {
            is HttpException -> ApiErrorCase.HttpError
            is SocketTimeoutException,
            is UnknownHostException -> ApiErrorCase.NoInternet
            is JsonSyntaxException -> ApiErrorCase.BadRequest
            else -> ApiErrorCase.Unknown
        }

        return ApiError(
            errorCase = case,
            errorMessage = errorMessage,
            requestPath = requestPath
        )
    }

    fun consumeError(response: Response<out BaseResponse>, requestPath: String): ApiError {
        return ApiError(
            errorCase = ApiErrorCase.Unknown,
            errorMessage = response.message(),
            requestPath = requestPath
        )
    }
}