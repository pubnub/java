package com.pubnub.apikt.services

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface S3Service {
    @POST
    fun upload(
        @Url url: String,
        @Body form: MultipartBody
    ): Call<Unit>
}
