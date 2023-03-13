package com.pubnub.apikt.integration

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.models.consumer.PNStatus
import org.junit.Test
import java.util.*

class PushPayloadHelperIntegrationTest : BaseIntegrationTest() {

    @Test
    fun testIntercept() {
        val expectedChannel = UUID.randomUUID().toString()

        val payload = Gson().fromJson(json, JsonObject::class.java)

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                pubnub.publish(
                    channel = expectedChannel,
                    message = payload
                ).sync()!!
            }
        })

        pubnub.subscribe(
            channels = listOf(expectedChannel),
            withPresence = true
        )

        wait()
    }

    private val json = """
            {
              "match": {
                "tournament": "Barclay's Premier League",
                "date": "2018-04-05 13:30:45",
                "venue": "Anfield Road",
                "title": "Goal! 90 min",
                "summary": "Liverpool - Chelsea 2:1"
              },
              "pn_gcm": {
                "data": {
                  "title": "Goal! 90 min",
                  "summary": "Liverpool - Chelsea 2:1"
                }
              },
              "pn_apns": {
                "aps": {
                  "title": "Goal! 90 min",
                  "summary": "Liverpool - Chelsea 2:1"
                }
              },
              "pn_mpns": {
                "title": "Goal! 90 min",
                "summary": "Liverpool - Chelsea 2:1"
              }
            }
    """.trimIndent()
}
