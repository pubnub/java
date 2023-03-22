package com.pubnub.api.endpoints.publishKT

class PNInstanceIdProviderImpl(private val pnInstanceId: String) : PNInstanceIdProvider {
    override fun getPNInstanceId(): String {
        return pnInstanceId
    }
}
