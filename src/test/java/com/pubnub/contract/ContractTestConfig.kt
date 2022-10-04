package com.pubnub.contract

import org.aeonbits.owner.Config
import org.aeonbits.owner.Config.Sources
import org.aeonbits.owner.ConfigFactory

@Sources("file:test.properties")
interface ContractTestConfig : Config {
    @Config.Key("pamSubKey")
    fun pamSubKey(): String?

    @Config.Key("pamPubKey")
    fun pamPubKey(): String?

    @Config.Key("pamSecKey")
    fun pamSecKey(): String?

    @Config.Key("subKey")
    fun subKey(): String?

    @Config.Key("pubKey")
    fun pubKey(): String?

    @Config.Key("serverHostPort")
    fun serverHostPort(): String

    @Config.Key("serverMock")
    fun serverMock(): Boolean

    @Config.Key("personasLocation")
    fun personasLocation(): String
}

val CONTRACT_TEST_CONFIG: ContractTestConfig = ConfigFactory.create(ContractTestConfig::class.java, System.getenv())
