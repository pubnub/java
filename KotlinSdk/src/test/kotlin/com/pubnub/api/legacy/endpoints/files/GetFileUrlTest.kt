package com.pubnub.apikt.legacy.endpoints.files

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.UserId
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import java.util.*

class GetFileUrlTest {
    private val channel = "channel"
    private val fileName = "fileName"
    private val fileId = "fileId"
    private val defaultQueryParams: Set<String> = HashSet(Arrays.asList("pnsdk", "requestid", "uuid"))

    @Test
    @Throws(PubNubException::class)
    fun noAdditionalQueryParamsWhenNotSecretNorAuth() {
        // given
        val pubnub = PubNub(config())

        // when
        val url = pubnub.getFileUrl(
            channel = channel,
            fileName = fileName,
            fileId = fileId
        ).sync()!!.url

        // then
        val queryParamNames = queryParameterNames(url)
        queryParamNames.removeAll(defaultQueryParams)
        Assert.assertEquals(emptySet<Any>(), queryParamNames)
    }

    @Test
    @Throws(PubNubException::class)
    fun signatureAndTimestampQueryParamsAreSetWhenSecret() {
        // given
        val pubnub = PubNub(withSecret(config()))

        // when
        val url = pubnub.getFileUrl(
            channel = channel,
            fileName = fileName,
            fileId = fileId
        ).sync()!!.url

        // then
        val queryParamNames = queryParameterNames(url)
        queryParamNames.removeAll(defaultQueryParams)
        assertThat<Collection<String>>(queryParamNames, Matchers.containsInAnyOrder("signature", "timestamp"))
    }

    @Test
    @Throws(PubNubException::class)
    fun authQueryParamIsSetWhenAuth() {
        // given
        val pubnub = PubNub(withAuth(config()))

        // when
        val url = pubnub.getFileUrl(
            channel = channel,
            fileName = fileName,
            fileId = fileId
        ).sync()!!.url

        // then
        val queryParamNames = queryParameterNames(url)
        queryParamNames.removeAll(defaultQueryParams)
        assertThat<Collection<String>>(queryParamNames, Matchers.containsInAnyOrder("auth"))
    }

    @Test
    @Throws(PubNubException::class)
    fun signatureAndTimestampAndAuthQueryParamsAreSetWhenSecretAndAuth() {
        // given
        val pubnub = PubNub(withSecret(withAuth(config())))

        // when
        val url = pubnub.getFileUrl(
            channel = channel,
            fileName = fileName,
            fileId = fileId
        ).sync()!!.url

        // then
        println(url)
        val queryParamNames = queryParameterNames(url)
        queryParamNames.removeAll(defaultQueryParams)
        assertThat<Collection<String>>(
            queryParamNames,
            Matchers.containsInAnyOrder("auth", "signature", "timestamp")
        )
    }

    private fun config(): PNConfiguration {
        val config = PNConfiguration(userId = UserId(PubNub.generateUUID()))
        config.publishKey = "pk"
        config.subscribeKey = "sk"
        return config
    }

    private fun withSecret(config: PNConfiguration): PNConfiguration {
        config.secretKey = "secK"
        return config
    }

    private fun withAuth(config: PNConfiguration): PNConfiguration {
        config.authKey = "ak"
        return config
    }

    private fun queryParameterNames(url: String): MutableCollection<String> {
        return HashSet(url.toHttpUrl().queryParameterNames)
    }
}
