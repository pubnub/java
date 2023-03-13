package com.pubnub.apikt.legacy.endpoints.push

import com.pubnub.apikt.enums.PNPushEnvironment
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.models.consumer.push.payload.PushPayloadHelper
import com.pubnub.apikt.models.consumer.push.payload.PushPayloadHelper.*
import com.pubnub.apikt.models.consumer.push.payload.PushPayloadHelper.APNSPayload.APNS2Configuration
import com.pubnub.apikt.models.consumer.push.payload.PushPayloadHelper.APNSPayload.APS
import org.junit.Assert.*
import org.junit.Test

class PushPayloadHelperHelperTest : BaseTest() {

    @Test
    fun testPayloads_Missing() {
        val pushPayloadHelper = PushPayloadHelper()
        val map = pushPayloadHelper.build()
        assertTrue(map.isEmpty())
    }

    @Test
    fun testPayloads_Null() {
        val pushPayloadHelper = PushPayloadHelper()
        pushPayloadHelper.apnsPayload = null
        pushPayloadHelper.commonPayload = null
        pushPayloadHelper.fcmPayload = null
        pushPayloadHelper.mpnsPayload = null
        val map = pushPayloadHelper.build()
        assertTrue(map.isEmpty())
    }

    @Test
    fun testPayloads_Empty() {
        val pushPayloadHelper = PushPayloadHelper()
        pushPayloadHelper.apnsPayload = APNSPayload()
        pushPayloadHelper.commonPayload = HashMap()
        pushPayloadHelper.fcmPayload = FCMPayload()
        pushPayloadHelper.mpnsPayload = MPNSPayload()
        val map = pushPayloadHelper.build()
        assertTrue(map.isEmpty())
    }

    @Test
    fun testApple_Empty() {
        val pushPayloadHelper = PushPayloadHelper()
        val apnsPayload = APNSPayload()
        apnsPayload.aps = APS()
        apnsPayload.apns2Configurations = ArrayList()
        val map = pushPayloadHelper.build()
        assertTrue(map.isEmpty())
    }

    @Test
    fun testApple_Valid() {
        val pushPayloadHelper = PushPayloadHelper()

        val apnsPayload = APNSPayload()

        val aps = APS()
        aps.alert = "alert"
        aps.badge = 5

        apnsPayload.aps = aps
        apnsPayload.apns2Configurations = emptyList()
        apnsPayload.custom = hashMapOf(
            "key_1" to "1",
            "key_2" to 2
        )
        pushPayloadHelper.apnsPayload = apnsPayload

        val map = pushPayloadHelper.build()

        val pnApnsDataMap = map["pn_apns"] as Map<*, *>
        assertEquals("1", pnApnsDataMap["key_1"])
        assertEquals(2, pnApnsDataMap["key_2"])

        val apsMap = pnApnsDataMap["aps"] as Map<*, *>
        assertEquals("alert", apsMap["alert"])
        assertEquals(5, apsMap["badge"])

        val pushList = pnApnsDataMap["pn_push"] as List<*>
        assertEquals(0, pushList.size)
    }

    @Test
    fun testApple_Aps() {
        val pushPayloadHelper = PushPayloadHelper()
        val apnsPayload = APNSPayload()

        apnsPayload.aps = APS().apply {
            alert = "alert"
            badge = 5
        }
        pushPayloadHelper.apnsPayload = apnsPayload

        apnsPayload.custom = mapOf<String, Any>("key_2" to "2")

        val map = pushPayloadHelper.build()

        val pnApnsDataMap = map["pn_apns"] as Map<*, *>
        val apsMap = pnApnsDataMap["aps"] as Map<*, *>

        assertEquals("alert", apsMap["alert"])
        assertEquals(5, apsMap["badge"])
        assertFalse(apsMap.containsKey("sound"))
        assertFalse(pnApnsDataMap.containsKey("pn_push"))
        assertFalse(pnApnsDataMap.containsKey("key_1"))
        assertEquals("2", pnApnsDataMap["key_2"])
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testApple_PnPushArray() {
        val pushPayloadHelper = PushPayloadHelper()

        val target1 = APNS2Configuration.Target().apply {
            environment = PNPushEnvironment.DEVELOPMENT
            topic = "topic_1"
        }
        val target2 = APNS2Configuration.Target().apply {
            environment = PNPushEnvironment.PRODUCTION
        }
        val target3 = APNS2Configuration.Target().apply {
            environment = PNPushEnvironment.PRODUCTION
            topic = "topic_3"
            excludeDevices = listOf("ex_1", "ex_2")
        }
        val target4 = APNS2Configuration.Target().apply {
            environment = null
            topic = null
            excludeDevices = null
        }
        val target5 = APNS2Configuration.Target().apply {
            environment = PNPushEnvironment.PRODUCTION
            topic = "topic_5"
            excludeDevices = listOf()
        }
        val target6 = APNS2Configuration.Target().apply {
            topic = "topic_6"
            excludeDevices = null
        }
        val target7 = APNS2Configuration.Target()

        val apns2Config1 = APNS2Configuration().apply {
            collapseId = "collapse_1"
            expiration = "exp_1"
            version = "v1"
            targets = null
        }
        val apns2Config2 = APNS2Configuration().apply {
            collapseId = "collapse_2"
            expiration = "exp_2"
            version = "v2"
            targets = emptyList()
        }
        val apns2Config3 = APNS2Configuration().apply {
            collapseId = null
            expiration = ""
            version = "v3"
            targets = listOf(target1, target2, target3, target4, target5, target6, target7)
        }
        val apns2Config4 = APNS2Configuration().apply {
            collapseId = null
            expiration = null
            version = null
        }
        val apns2Config5 = APNS2Configuration()

        pushPayloadHelper.apnsPayload = APNSPayload().apply {
            apns2Configurations = listOf(
                apns2Config1, apns2Config2, apns2Config3, apns2Config4, apns2Config5
            )
        }

        val map = pushPayloadHelper.build()

        val apnsMap = map["pn_apns"] as Map<*, *>
        val pnPushList = apnsMap["pn_push"] as List<Map<*, *>>

        assertEquals(3, pnPushList.size)

        assertEquals("exp_1", pnPushList[0]["expiration"])
        assertEquals("collapse_1", pnPushList[0]["collapse_id"])
        assertEquals("v1", pnPushList[0]["version"])
        assertFalse(pnPushList[0].containsKey("targets"))

        assertEquals("exp_2", pnPushList[1]["expiration"])
        assertEquals("collapse_2", pnPushList[1]["collapse_id"])
        assertEquals("v2", pnPushList[1]["version"])
        assertFalse(pnPushList[1].containsKey("targets"))

        assertEquals("", pnPushList[2]["expiration"])
        assertFalse(pnPushList[2].containsKey("collapse_id"))
        assertEquals("v3", pnPushList[2]["version"])
        assertTrue(pnPushList[2].containsKey("targets"))

        val pnTargetsMap = pnPushList[2]["targets"] as List<Map<*, *>>

        assertEquals("development", pnTargetsMap[0]["environment"])
        assertEquals("topic_1", pnTargetsMap[0]["topic"])
        assertFalse(pnTargetsMap[0].containsKey("excludeDevices"))

        assertEquals("production", pnTargetsMap[1]["environment"])
        assertFalse(pnTargetsMap[1].containsKey("topic_2"))
        assertFalse(pnTargetsMap[1].containsKey("excludeDevices"))

        assertEquals("production", pnTargetsMap[2]["environment"])
        assertEquals("topic_3", pnTargetsMap[2]["topic"])
        assertEquals("ex_1", (pnTargetsMap[2]["excluded_devices"] as List<*>?)!![0])
        assertEquals("ex_2", (pnTargetsMap[2]["excluded_devices"] as List<*>?)!![1])

        assertEquals("production", pnTargetsMap[3]["environment"])
        assertFalse(pnTargetsMap[3].containsKey("topic_5"))
        assertFalse(pnTargetsMap[3].containsKey("excludeDevices"))

        assertFalse(pnTargetsMap[4].containsKey("environment"))
        assertEquals("topic_6", pnTargetsMap[4]["topic"])
        assertFalse(pnTargetsMap[4].containsKey("environment"))
    }

    @Test
    fun testApple_Aps_Empty() {
        val pushPayloadHelper = PushPayloadHelper()

        val apnsPayload = APNSPayload()
        apnsPayload.aps = APS()
        pushPayloadHelper.apnsPayload = apnsPayload

        val map = pushPayloadHelper.build()
        assertTrue(map.isEmpty())
    }

    @Test
    fun testCommonPayload_Valid() {
        val map = PushPayloadHelper().apply {
            commonPayload =
                mapOf(
                    "common_key_1" to 1,
                    "common_key_2" to "2",
                    "common_key_3" to true
                )
        }.build()

        assertEquals(map["common_key_1"], 1)
        assertEquals(map["common_key_2"], "2")
        assertEquals(map["common_key_3"], true)
    }

    @Test
    fun testCommonPayload_Invalid() {
        val pushPayloadHelper = PushPayloadHelper()

        pushPayloadHelper.commonPayload = mapOf()

        val map = pushPayloadHelper.build()
        assertTrue(map.isEmpty())
    }

    @Test
    fun testGoogle_Valid_1() {
        val pushPayloadHelper = PushPayloadHelper()

        pushPayloadHelper.fcmPayload = FCMPayload().apply {
            notification = FCMPayload.Notification().apply {
                body = "Notification body"
                image = null
                title = ""
            }
            custom = mapOf("a" to "a", "b" to 1)
            data = mapOf("data_1" to "a", "data_2" to 1)
        }

        val map = pushPayloadHelper.build()

        val pnFcmMap = map["pn_gcm"] as Map<*, *>
        assertNotNull(pnFcmMap)

        val pnFcmDataMap = pnFcmMap["data"] as Map<*, *>
        val pnFcmNotificationsMap = pnFcmMap["notification"] as Map<*, *>

        assertNotNull(pnFcmDataMap)
        assertNotNull(pnFcmNotificationsMap)

        assertEquals(pnFcmMap["a"], "a")
        assertEquals(pnFcmMap["b"], 1)
        assertEquals(pnFcmDataMap["data_1"], "a")
        assertEquals(pnFcmDataMap["data_2"], 1)
        assertEquals(pnFcmNotificationsMap["body"], "Notification body")
        assertEquals(pnFcmNotificationsMap["image"], null)
        assertEquals(pnFcmNotificationsMap["title"], "")
    }

    @Test
    fun testGoogle_Empty() {
        val pushPayloadHelper = PushPayloadHelper()
        val fcmPayload = FCMPayload()
        pushPayloadHelper.fcmPayload = fcmPayload
        val map = pushPayloadHelper.build()
        val pnFcmMap = map["pn_gcm"]
        assertNull(pnFcmMap)
    }

    @Test
    fun testGoogle_EmptyNotification() {
        val pushPayloadHelper = PushPayloadHelper()
        val notification = FCMPayload.Notification()
        val customMap = HashMap<String, Any>()
        customMap["key_1"] = "1"
        customMap["key_2"] = 2
        val fcmPayload = FCMPayload()
        fcmPayload.notification = notification
        fcmPayload.custom = customMap
        pushPayloadHelper.fcmPayload = fcmPayload
        val map = pushPayloadHelper.build()
        val pnFcmMap = map["pn_gcm"] as Map<*, *>
        val pnFcmNotificationMap = pnFcmMap["notification"]
        assertNull(pnFcmNotificationMap)
    }

    @Test
    fun testGoogle_EmptyData() {
        val pushPayloadHelper = PushPayloadHelper()
        val fcmPayload = FCMPayload()
        val dataMap = HashMap<String, Any>()
        fcmPayload.data = dataMap
        pushPayloadHelper.fcmPayload = fcmPayload
        val map = pushPayloadHelper.build()
        val pnFcmMap = map["pn_gcm"]
        assertNull(pnFcmMap)
    }

    @Test
    fun testGoogle_Valid_2() {
        val pushPayloadHelper = PushPayloadHelper()
        val fcmPayload = FCMPayload().apply {
            data = mapOf(
                "key_1" to "value_1",
                "key_2" to 2,
                "key_3" to true,
                "key_4" to ""
            )
        }
        pushPayloadHelper.fcmPayload = fcmPayload
        val map = pushPayloadHelper.build()

        val pnFcmMap = map["pn_gcm"] as Map<*, *>
        val pnFcmDataMap = pnFcmMap["data"] as Map<*, *>
        assertNotNull(pnFcmDataMap)
        assertFalse(pnFcmDataMap.isEmpty())
        assertTrue(pnFcmDataMap.containsKey("key_1"))
        assertTrue(pnFcmDataMap.containsKey("key_2"))
        assertTrue(pnFcmDataMap.containsKey("key_3"))
        assertTrue(pnFcmDataMap.containsKey("key_4"))
        assertNotNull(pnFcmDataMap["key_1"])
        assertNotNull(pnFcmDataMap["key_2"])
        assertNotNull(pnFcmDataMap["key_3"])
        assertNotNull(pnFcmDataMap["key_4"])
    }

    @Test
    fun testGoogle_Custom() {
        val pushPayloadHelper = PushPayloadHelper()
        pushPayloadHelper.fcmPayload = FCMPayload().apply {
            custom = mapOf(
                "key_1" to "value_1",
                "key_2" to 2,
                "key_3" to true,
                "key_4" to ""
            )
        }
        val map = pushPayloadHelper.build()

        val pnFcmMap = map["pn_gcm"] as Map<*, *>
        assertNotNull(pnFcmMap)
        assertFalse(pnFcmMap.isEmpty())
        assertTrue(pnFcmMap.containsKey("key_1"))
        assertTrue(pnFcmMap.containsKey("key_2"))
        assertTrue(pnFcmMap.containsKey("key_3"))
        assertTrue(pnFcmMap.containsKey("key_4"))
        assertNotNull(pnFcmMap["key_1"])
        assertNotNull(pnFcmMap["key_2"])
        assertNotNull(pnFcmMap["key_3"])
        assertNotNull(pnFcmMap["key_4"])
    }

    @Test
    fun testMicrosoft_Missing() {
        val pushPayloadHelper = PushPayloadHelper()

        val mpnsPayload = MPNSPayload().apply {
            backContent = "Back Content"
            backTitle = "Back Title"
            count = 1
            title = "Title"
            type = "Type"
            custom = mapOf(
                "a" to "a",
                "b" to 1,
                "c" to ""
            )
        }
        pushPayloadHelper.mpnsPayload = mpnsPayload
        val map = pushPayloadHelper.build()

        val pnMpnsMap = map["pn_mpns"] as HashMap<*, *>
        assertNotNull(pnMpnsMap)
        assertEquals(pnMpnsMap["back_content"], "Back Content")
        assertEquals(pnMpnsMap["back_title"], "Back Title")
        assertEquals(pnMpnsMap["count"], 1)
        assertEquals(pnMpnsMap["title"], "Title")
        assertEquals(pnMpnsMap["type"], "Type")
        assertEquals(pnMpnsMap["a"], "a")
        assertEquals(pnMpnsMap["b"], 1)
        assertEquals(pnMpnsMap["c"], "")
    }

    @Test
    fun testMicrosoft_Valid() {
        val pushPayloadHelper = PushPayloadHelper()
        pushPayloadHelper.mpnsPayload =
            MPNSPayload().apply {
                backContent = "Back Content"
                backTitle = "Back Title"
                count = 1
                title = "Title"
                type = "Type"
                custom = mapOf(
                    "a" to "a",
                    "b" to 1,
                    "c" to ""
                )
            }

        val map = pushPayloadHelper.build()
        val pnMpnsMap = map["pn_mpns"] as Map<*, *>

        assertNotNull(pnMpnsMap)
        assertEquals(pnMpnsMap["back_content"], "Back Content")
        assertEquals(pnMpnsMap["back_title"], "Back Title")
        assertEquals(pnMpnsMap["count"], 1)
        assertEquals(pnMpnsMap["title"], "Title")
        assertEquals(pnMpnsMap["type"], "Type")
        assertEquals(pnMpnsMap["a"], "a")
        assertEquals(pnMpnsMap["b"], 1)
        assertEquals(pnMpnsMap["c"], "")
        assertEquals(pnMpnsMap["d"], null)
    }

    @Test
    fun testMicrosoft_Empty() {
        val pushPayloadHelper = PushPayloadHelper()
        val mpnsPayload = MPNSPayload()
        pushPayloadHelper.mpnsPayload = mpnsPayload
        val map = pushPayloadHelper.build()
        val pnMpnsMap = map["pn_mpns"]
        assertNull(pnMpnsMap)
    }

    @Test
    fun testMicrosoft_Custom() {
        val pushPayloadHelper = PushPayloadHelper()
        pushPayloadHelper.mpnsPayload = MPNSPayload().apply {
            backContent = ""
            backTitle = "Back Title"
            count = 1
            title = null
            type = "Type"
            custom = mapOf(
                "a" to "a",
                "b" to 1,
                "c" to ""
            )
        }

        val map = pushPayloadHelper.build()

        val pnMpnsMap = map["pn_mpns"] as Map<*, *>
        assertNotNull(pnMpnsMap)
        assertEquals("", pnMpnsMap["back_content"])
        assertEquals("Back Title", pnMpnsMap["back_title"])
        assertEquals(1, pnMpnsMap["count"])
        assertFalse(pnMpnsMap.containsKey("title"))
        assertEquals("Type", pnMpnsMap["type"])
        assertEquals("a", pnMpnsMap["a"])
        assertEquals(1, pnMpnsMap["b"])
        assertEquals("", pnMpnsMap["c"])
        assertFalse(pnMpnsMap.containsKey("d"))
    }
}
