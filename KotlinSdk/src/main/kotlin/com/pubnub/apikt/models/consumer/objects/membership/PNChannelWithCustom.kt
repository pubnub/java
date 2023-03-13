package com.pubnub.apikt.models.consumer.objects.membership

@Deprecated(
    message = "Use PNChannelMembership.Partial",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith(
        "PNChannelMembership.Partial(channelId = channel, custom = custom)",
        "com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembership"
    )
)
data class PNChannelWithCustom(
    override val channel: String,
    override val custom: Any? = null
) : ChannelMembershipInput {
    override val status: String? = null

    companion object {
        @Deprecated(
            message = "Use PNChannelMembership.Partial",
            level = DeprecationLevel.ERROR,
            replaceWith = ReplaceWith(
                "PNChannelMembership.Partial(channelId = channel, custom = custom)",
                "com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembership"
            )
        )
        fun of(
            channel: String,
            custom: Any? = null
        ) = PNChannelMembership.Partial(channelId = channel, custom = custom)
    }
}
