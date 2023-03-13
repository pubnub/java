package com.pubnub.apikt.services

import com.pubnub.apikt.models.server.Envelope
import com.pubnub.apikt.models.server.access_manager.AccessManagerGrantPayload
import com.pubnub.apikt.models.server.access_manager.v3.GrantTokenResponse
import com.pubnub.apikt.models.server.access_manager.v3.RevokeTokenResponse
import retrofit2.Call
import retrofit2.http.*

internal interface AccessManagerService {

    @GET("/v2/auth/grant/sub-key/{subKey}")
    fun grant(
        @Path("subKey") subKey: String,
        @QueryMap options: Map<String, String>
    ): Call<Envelope<AccessManagerGrantPayload>>

    @POST("/v3/pam/{subKey}/grant")
    fun grantToken(
        @Path("subKey") subKey: String?,
        @Body body: Any?,
        @QueryMap options: Map<String, String>
    ): Call<GrantTokenResponse>

    @DELETE("/v3/pam/{subKey}/grant/{token}")
    fun revokeToken(
        @Path("subKey") subKey: String,
        @Path("token", encoded = true) token: String,
        @QueryMap queryParams: Map<String, String>
    ): Call<RevokeTokenResponse>
}
