package org.adhash.sdk.adhashask.network

import com.google.gson.JsonSyntaxException
import org.adhash.sdk.adhashask.base.BaseResponse
import org.adhash.sdk.adhashask.constants.ApiError
import org.adhash.sdk.adhashask.constants.ApiErrorCase
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandler {

    fun consumeThrowable(networkError: Throwable, requestPath: String): ApiError {
        val errorMessage = networkError.message?.let { it } ?: ""

        val case =  when (networkError) {
            is HttpException -> extractErrorCase(networkError)
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

    fun consumeError(response: Response<out BaseResponse>, requestPath: String): ApiError{
        //todo parse HttpErrors
        return ApiError(
            errorCase = ApiErrorCase.Unknown,
            errorMessage = response.message(),
            requestPath = requestPath
        )
//        when(response.errorBody()){}

    }

    private fun extractErrorCase(error: HttpException): ApiErrorCase {
        return ApiErrorCase.Unknown
        //todo read error body
//        error.response().errorBody()?.let { errorBody ->
//            return try {
//                val errorJson = JSONObject(errorBody.string())
//                val errorActionName = errorJson.optString("error")
//                ErrorActions.getEnumValueByKey(errorActionName)
//            } catch (exception : Exception) { null }
//        } ?: run { return null }
    }
}