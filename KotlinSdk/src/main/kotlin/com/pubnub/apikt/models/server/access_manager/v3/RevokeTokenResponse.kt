package com.pubnub.apikt.models.server.access_manager.v3

data class RevokeTokenResponse(val status: Int, val data: RevokeTokenData, val service: String)
data class RevokeTokenData(val message: String, val token: String)
