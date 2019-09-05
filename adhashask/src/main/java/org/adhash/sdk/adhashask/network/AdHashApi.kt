package org.adhash.sdk.adhashask.network

import org.adhash.sdk.adhashask.pojo.InfoBody
import org.adhash.sdk.adhashask.pojo.ResponseFirstStep
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AdHashApi {

    @Headers("Content-Type: application/json")
    @POST("{endurl}")
    fun sendInfoBody(
        @Path("endurl") endUrl: String,
        @Body body: InfoBody
    ): Call<ResponseFirstStep>
}