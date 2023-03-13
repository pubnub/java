package com.pubnub.apikt.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

internal interface TimeService {

    @GET("/time/0")
    fun fetchTime(@QueryMap options: Map<String, String>): Call<List<Long>>
}
