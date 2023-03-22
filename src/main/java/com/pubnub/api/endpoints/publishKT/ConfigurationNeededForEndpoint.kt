package com.pubnub.api.endpoints.publishKT


interface ConfigurationNeededForEndpoint {
    fun getSubscribeKey(): String
    fun getPublishKey(): String
    fun generatePnsdk(): String
    fun getUserIdValue(): String //ToDO zamienić na String
    fun includeInstanceIdentifier(): Boolean
    fun includeRequestIdentifier(): Boolean
    fun isAuthKeyValid(): Boolean
    fun getAuthKey(): String
}
