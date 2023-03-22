package com.pubnub.api.endpoints.publishKT

interface TokenRetriever {
    fun getToken(): String?
}
