package com.pubnub.apikt.models.consumer.push.payload

import com.pubnub.apikt.enums.PNPushEnvironment
import java.util.*

class PushPayloadHelper {

    var commonPayload: Map<String, Any>? = null
    var fcmPayload: FCMPayload? = null
    var mpnsPayload: MPNSPayload? = null
    var apnsPayload: APNSPayload? = null

    fun build(): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            apnsPayload?.let {
                it.toMap().run {
                    if (isNotEmpty()) {
                        put("pn_apns", this)
                    }
                }
            }
            fcmPayload?.let {
                it.toMap().run {
                    if (isNotEmpty()) {
                        put("pn_gcm", this)
                    }
                }
            }
            mpnsPayload?.let {
                it.toMap().run {
                    if (isNotEmpty()) {
                        put("pn_mpns", this)
                    }
                }
            }
            commonPayload?.let { putAll(it) }
        }
    }

    class APNSPayload : PushPayloadSerializer {
        var aps: APS? = null
        var apns2Configurations: List<APNS2Configuration>? = null
        var custom: Map<String, Any>? = null

        override fun toMap(): Map<String, Any> {
            return mutableMapOf<String, Any>().apply {
                aps?.let {
                    it.toMap().run {
                        if (this.isNotEmpty()) {
                            put("aps", this)
                        }
                    }
                }
                apns2Configurations?.let { put("pn_push", it.map { it.toMap() }.filter { it.isNotEmpty() }.toList()) }
                custom?.let { putAll(it) }
            }
        }

        class APS : PushPayloadSerializer {
            var alert: Any? = null
            var badge: Int? = null
            var sound: String? = null

            override fun toMap(): Map<String, Any> {
                return mutableMapOf<String, Any>().apply {
                    alert?.let { put("alert", it) }
                    badge?.let { put("badge", it) }
                    sound?.let { put("sound", it) }
                }
            }
        }

        class APNS2Configuration : PushPayloadSerializer {
            var collapseId: String? = null
            var expiration: String? = null
            var targets: List<Target>? = null
            var version: String? = null

            override fun toMap(): Map<String, Any> {
                return mutableMapOf<String, Any>().apply {
                    collapseId?.let { put("collapse_id", it) }
                    expiration?.let { put("expiration", it) }
                    targets?.let {
                        it.map { it.toMap() }.filter { it.isNotEmpty() }.toList().run {
                            if (this.isNotEmpty()) {
                                put("targets", this)
                            }
                        }
                    }
                    version?.let { put("version", it) }
                }
            }

            class Target : PushPayloadSerializer {
                var topic: String? = null
                var excludeDevices: List<String>? = null
                var environment: PNPushEnvironment? = null

                override fun toMap(): Map<String, Any> {
                    return mutableMapOf<String, Any>().apply {
                        topic?.let { put("topic", it) }
                        excludeDevices?.let { put("excluded_devices", it) }
                        environment?.let { put("environment", it.name.lowercase(Locale.getDefault())) }
                    }
                }
            }
        }
    }

    class MPNSPayload : PushPayloadSerializer {
        var count: Int? = null
        var backTitle: String? = null
        var title: String? = null
        var backContent: String? = null
        var type: String? = null
        var custom: Map<String, Any>? = null

        override fun toMap(): Map<String, Any> {
            return mutableMapOf<String, Any>().apply {
                count?.let { put("count", it) }
                backTitle?.let { put("back_title", it) }
                title?.let { put("title", it) }
                backContent?.let { put("back_content", it) }
                type?.let { put("type", it) }
                custom?.let { putAll(it) }
            }
        }
    }

    class FCMPayload : PushPayloadSerializer {
        var custom: Map<String, Any>? = null
        var data: Map<String, Any>? = null
        var notification: Notification? = null

        override fun toMap(): Map<String, Any> {
            return mutableMapOf<String, Any>().apply {
                custom?.let { putAll(it) }
                data?.let {
                    if (it.isNotEmpty()) {
                        put("data", it)
                    }
                }
                notification?.let {
                    it.toMap().run {
                        if (this.isNotEmpty()) {
                            put("notification", this)
                        }
                    }
                }
            }
        }

        class Notification : PushPayloadSerializer {
            var title: String? = null
            var body: String? = null
            var image: String? = null

            override fun toMap(): Map<String, Any> {
                return mutableMapOf<String, Any>().apply {
                    title?.let { put("title", it) }
                    body?.let { put("body", it) }
                    image?.let { put("image", it) }
                }
            }
        }
    }
}
