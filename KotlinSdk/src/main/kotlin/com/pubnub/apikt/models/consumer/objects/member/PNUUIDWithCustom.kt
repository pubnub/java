package com.pubnub.apikt.models.consumer.objects.member

@Deprecated(
    message = "Use PNMember.Partial",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith(
        "PNMember.Partial(uuidId = uuid, custom = custom)",
        "com.pubnub.apikt.models.consumer.objects.member.PNMember"
    )
)
data class PNUUIDWithCustom(
    override val uuid: String,
    override val custom: Any? = null
) : MemberInput {
    companion object {
        @Deprecated(
            message = "Use PNMember.Partial",
            level = DeprecationLevel.ERROR,
            replaceWith = ReplaceWith(
                "PNMember.Partial(uuidId = uuid, custom = custom)",
                "com.pubnub.apikt.models.consumer.objects.member.PNMember"
            )
        )
        fun of(
            uuid: String,
            custom: Any? = null
        ) = PNMember.Partial(uuidId = uuid, custom = custom)
    }

    override val status: String? = null
}
