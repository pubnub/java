package com.pubnub.apikt.endpoints.push

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushEnvironment
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.models.consumer.push.PNPushRemoveAllChannelsResult
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * @see [PubNub.removeAllPushNotificationsFromDeviceWithPushToken]
 */
class RemoveAllPushChannelsForDevice internal constructor(
    pubnub: PubNub,
    val pushType: PNPushType,
    val deviceId: String,
    val environment: PNPushEnvironment = PNPushEnvironment.DEVELOPMENT,
    val topic: String? = null
) : Endpoint<Void, PNPushRemoveAllChannelsResult>(pubnub) {

    override fun validateParams() {
        super.validateParams()
        if (deviceId.isBlank()) throw PubNubException(PubNubError.DEVICE_ID_MISSING)
        if (pushType == PNPushType.APNS2 && topic.isNullOrBlank()) throw PubNubException(PubNubError.PUSH_TOPIC_MISSING)
    }

    override fun doWork(queryParams: HashMap<String, String>): Call<Void> {

        addQueryParams(queryParams)

        return if (pushType != PNPushType.APNS2)
            pubnub.retrofitManager.pushService
                .removeAllChannelsForDevice(
                    subKey = pubnub.configuration.subscribeKey,
                    pushToken = deviceId,
                    options = queryParams
                )
        else
            pubnub.retrofitManager.pushService
                .removeAllChannelsForDeviceApns2(
                    subKey = pubnub.configuration.subscribeKey,
                    deviceApns2 = deviceId,
                    options = queryParams
                )
    }

    override fun createResponse(input: Response<Void>): PNPushRemoveAllChannelsResult =
        PNPushRemoveAllChannelsResult()

    override fun operationType() = PNOperationType.PNRemoveAllPushNotificationsOperation

    private fun addQueryParams(queryParams: MutableMap<String, String>) {
        if (pushType != PNPushType.APNS2) {
            queryParams["type"] = pushType.toParamString()
            return
        }

        queryParams["environment"] = environment.name.lowercase(Locale.getDefault())
        topic?.run { queryParams["topic"] = this }
    }
}
