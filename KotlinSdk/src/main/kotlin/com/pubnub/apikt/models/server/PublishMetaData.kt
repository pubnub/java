package com.pubnub.apikt.models.server

import com.google.gson.annotations.SerializedName

class PublishMetaData(
    @SerializedName("t")
    internal val publishTimetoken: Long?,

    @SerializedName("r")
    internal val region: Int?
)
