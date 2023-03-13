package com.pubnub.apikt.models.server

import com.google.gson.annotations.SerializedName

class OriginationMetaData(
    @SerializedName("t")
    internal val timetoken: Long?,

    @SerializedName("r")
    internal val region: Int?
)
