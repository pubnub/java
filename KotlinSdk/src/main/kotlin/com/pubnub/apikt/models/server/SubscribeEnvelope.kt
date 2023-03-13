package com.pubnub.apikt.models.server

import com.google.gson.annotations.SerializedName

class SubscribeEnvelope(

    @SerializedName("m")
    internal val messages: List<SubscribeMessage>,

    @SerializedName("t")
    internal val metadata: SubscribeMetaData
)
