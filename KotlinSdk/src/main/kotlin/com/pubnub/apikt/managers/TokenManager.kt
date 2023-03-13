package com.pubnub.apikt.managers

class TokenManager {
    @Volatile
    private var token: String? = null

    fun setToken(token: String?) {
        this.token = token
    }

    fun getToken(): String? {
        return token
    }
}
