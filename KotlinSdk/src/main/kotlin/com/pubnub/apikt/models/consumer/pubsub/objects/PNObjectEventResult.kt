package com.pubnub.apikt.models.consumer.pubsub.objects

import com.google.gson.JsonDeserializer
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadata
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata
import com.pubnub.apikt.models.consumer.pubsub.BasePubSubResult
import com.pubnub.apikt.models.consumer.pubsub.PubSubResult
import com.pubnub.apikt.utils.PolymorphicDeserializer
import com.pubnub.apikt.utils.UnwrapSingleField

data class PNObjectEventResult(
    private val result: BasePubSubResult,
    val extractedMessage: PNObjectEventMessage
) : PubSubResult by result

internal object ObjectExtractedMessageDeserializer :
    JsonDeserializer<PNObjectEventMessage> by PolymorphicDeserializer.dispatchByFieldsValues(
        fields = listOf("event", "type"),
        mappingFieldValuesToClass = mapOf(
            listOf("set", "channel") to PNSetChannelMetadataEventMessage::class.java,
            listOf("set", "uuid") to PNSetUUIDMetadataEventMessage::class.java,
            listOf("set", "membership") to PNSetMembershipEventMessage::class.java,
            listOf("delete", "channel") to PNDeleteChannelMetadataEventMessage::class.java,
            listOf("delete", "uuid") to PNDeleteUUIDMetadataEventMessage::class.java,
            listOf("delete", "membership") to PNDeleteMembershipEventMessage::class.java
        )
    )

@JsonAdapter(ObjectExtractedMessageDeserializer::class)
sealed class PNObjectEventMessage {
    abstract val source: String
    abstract val version: String
    abstract val event: String
    abstract val type: String
}

data class PNSetChannelMetadataEventMessage(
    override val source: String,
    override val version: String,
    override val event: String,
    override val type: String,
    val data: PNChannelMetadata
) : PNObjectEventMessage()

data class PNSetUUIDMetadataEventMessage(
    override val source: String,
    override val version: String,
    override val event: String,
    override val type: String,
    val data: PNUUIDMetadata
) : PNObjectEventMessage()

data class PNSetMembershipEventMessage(
    override val source: String,
    override val version: String,
    override val event: String,
    override val type: String,
    val data: PNSetMembershipEvent
) : PNObjectEventMessage()

data class PNDeleteMembershipEventMessage(
    override val source: String,
    override val version: String,
    override val event: String,
    override val type: String,
    val data: PNDeleteMembershipEvent
) : PNObjectEventMessage()

data class PNDeleteChannelMetadataEventMessage(
    override val source: String,
    override val version: String,
    override val event: String,
    override val type: String,
    @JsonAdapter(UnwrapSingleField::class)
    @SerializedName("data")
    val channel: String
) : PNObjectEventMessage()

data class PNDeleteUUIDMetadataEventMessage(
    override val source: String,
    override val version: String,
    override val event: String,
    override val type: String,
    @JsonAdapter(UnwrapSingleField::class)
    @SerializedName("data")
    val uuid: String
) : PNObjectEventMessage()

data class PNSetMembershipEvent(
    @JsonAdapter(UnwrapSingleField::class)
    @SerializedName("channel")
    val channel: String,
    @JsonAdapter(UnwrapSingleField::class)
    val uuid: String,
    val custom: Any?,
    val eTag: String,
    val updated: String,
    val status: String?
)

data class PNDeleteMembershipEvent(
    @JsonAdapter(UnwrapSingleField::class)
    @SerializedName("channel")
    val channelId: String,
    @JsonAdapter(UnwrapSingleField::class)
    val uuid: String
)
