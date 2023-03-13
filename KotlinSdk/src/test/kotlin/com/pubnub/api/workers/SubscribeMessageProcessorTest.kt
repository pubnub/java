package com.pubnub.apikt.workers

import com.google.gson.*
import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.UserId
import com.pubnub.apikt.managers.DuplicationManager
import com.pubnub.apikt.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.apikt.models.server.SubscribeMessage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.isA
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.hamcrest.core.Is.`is` as iz

@RunWith(Parameterized::class)
class SubscribeMessageProcessorTest(
    private val messageJson: JsonElement
) {

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Any> {
            return listOf(
                JsonPrimitive("thisIsMessage"),
                JsonObject().apply { add("test", JsonPrimitive("value")) },
                JsonArray().apply {
                    add(JsonPrimitive("array"))
                    add(JsonPrimitive("of"))
                    add(JsonPrimitive("elements"))
                },
                JsonPrimitive(true),
                JsonPrimitive(1337),
                JsonNull.INSTANCE,
                JsonObject().apply {
                    add(
                        "with",
                        JsonObject().apply {
                            add(
                                "array",
                                JsonArray().apply {
                                    add(JsonPrimitive("array"))
                                    add(JsonPrimitive("of"))
                                    add(JsonPrimitive("elements"))
                                }
                            )
                        }
                    )
                },
            )
        }
    }

    @Test
    fun testDifferentJsonMessages() {
        val gson = Gson()
        val configuration = PNConfiguration(userId = UserId("test"))

        val messageProcessor = SubscribeMessageProcessor(
            pubnub = PubNub(configuration),
            duplicationManager = DuplicationManager(configuration)
        )

        val result = messageProcessor.processIncomingPayload(
            gson.fromJson(
                fileMessage(messageJson.toString()),
                SubscribeMessage::class.java
            )
        )

        assertThat(result, isA(PNFileEventResult::class.java))
        assertThat((result as PNFileEventResult).jsonMessage, iz(messageJson))
    }

    private fun fileMessage(messageJson: String) =
        """{"a":"0","f":0,"e":4,"i":"client-52774e6f-2f4e-4915-aefd-e8bb75cd2e7d","p":{"t":"16632349939765880","r":43},"k":"sub-c-4b1dbfef-2fa9-495f-a316-2b634063083d","c":"ch_1663234993171_F4FC4F460F","u":"This is meta","d":{"message":$messageJson,"file":{"id":"30ce0095-3c50-4cdc-a626-bf402d233731","name":"fileNamech_1663234993171_F4FC4F460F.txt"}}}"""
}
