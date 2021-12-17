package com.pubnub.contract

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface MockPubnubService {

    @GET("init")
    fun init(
        @QueryMap options: Map<String?, String?>
    ): Call<Any>

    @GET("expect")
    fun expect(): Call<ExpectResponse>
}

data class ExpectResponse(
    val contract: String,
    val expectations: Expectations
) {
    data class Expectations(
        val pending: List<String>,
        val failed: List<String>
    )
}
