package com.pubnub.api.endpoints.publishKT

interface ConfigurationNeededForPublish {
    fun useEncryption(): Boolean
    fun getPublishKey(): String
    fun getSubscribeKey(): String
    fun getCipherKey(): String
    fun useRandomInitializationVector(): Boolean
}
