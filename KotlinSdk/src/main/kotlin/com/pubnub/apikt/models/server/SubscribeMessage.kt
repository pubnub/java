package com.pubnub.apikt.models.server

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.pubnub.apikt.workers.SubscribeMessageProcessor.Companion.TYPE_FILES
import com.pubnub.apikt.workers.SubscribeMessageProcessor.Companion.TYPE_MESSAGE

class SubscribeMessage(
    @SerializedName("a")
    internal val shard: String?,

    @SerializedName("b")
    internal val subscriptionMatch: String?,

    @SerializedName("c")
    internal val channel: String?,

    @SerializedName("d")
    internal val payload: JsonElement?,

    @SerializedName("f")
    internal val flags: String?,

    @SerializedName("i")
    internal val issuingClientId: String?,

    @SerializedName("k")
    internal val subscribeKey: String?,

    @SerializedName("o")
    internal val originationMetadata: OriginationMetaData?,

    @SerializedName("p")
    internal val publishMetaData: PublishMetaData?,

    @SerializedName("u")
    internal val userMetadata: JsonElement?,

    @SerializedName("e")
    internal val type: Int?

) {

    fun supportsEncryption() = type in arrayOf(null, TYPE_MESSAGE, TYPE_FILES)
}
