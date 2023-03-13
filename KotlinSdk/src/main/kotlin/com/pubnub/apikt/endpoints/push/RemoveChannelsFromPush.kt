package com.pubnub.apikt.endpoints.push

import com.pubnub.apikt.*
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushEnvironment
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.models.consumer.push.PNPushRemoveChannelResult
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * @see [PubNub.removePushNotificationsFromChannels]
 */
class RemoveChannelsFromPush internal constructor(
    pubnub: PubNub,
    val pushType: PNPushType,
    val channels: List<String>,
    val deviceId: String,
    val topic: String? = null,
    val environment: PNPushEnvironment = PNPushEnvironment.DEVELOPMENT
) : Endpoint<Void, PNPushRemoveChannelResult>(pubnub) {

    override fun getAffectedChannels() = channels

    override fun validateParams() {
        super.validateParams()
        if (deviceId.isBlank()) throw PubNubException(PubNubError.DEVICE_ID_MISSING)
        if (channels.isEmpty()) throw PubNubException(PubNubError.CHANNEL_MISSING)
        if (pushType == PNPushType.APNS2 && topic.isNullOrBlank()) throw PubNubException(PubNubError.PUSH_TOPIC_MISSING)
    }

    override fun doWork(queryParams: HashMap<String, String>): Call<Void> {

        addQueryParams(queryParams)

        return if (pushType != PNPushType.APNS2)
            pubnub.retrofitManager.pushService
                .modifyChannelsForDevice(
                    subKey = pubnub.configuration.subscribeKey,
                    pushToken = deviceId,
                    options = queryParams
                )
        else
            pubnub.retrofitManager.pushService
                .modifyChannelsForDeviceApns2(
                    subKey = pubnub.configuration.subscribeKey,
                    deviceApns2 = deviceId,
                    options = queryParams
                )
    }

    override fun createResponse(input: Response<Void>): PNPushRemoveChannelResult =
        PNPushRemoveChannelResult()

    override fun operationType() = PNOperationType.PNRemovePushNotificationsFromChannelsOperation

    private fun addQueryParams(queryParams: MutableMap<String, String>) {
        queryParams["remove"] = channels.toCsv()

        if (pushType != PNPushType.APNS2) {
            queryParams["type"] = pushType.toParamString()
            return
        }

        queryParams["environment"] = environment.name.lowercase(Locale.getDefault())
        topic?.run { queryParams["topic"] = this }
    }
}
