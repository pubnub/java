package com.pubnub.api.crypto.exception

import com.pubnub.api.models.consumer.PNStatus

/**
 * List of known PubNub errors. Observe them in [PubNubException.pubnubError] in [PNStatus.exception].
 *
 * @property code The error code.
 * @property message The error message.
 */
enum class PubNubError(private val code: Int, val message: String) {

    TIMEOUT(
        100,
        "Timeout Occurred"
    ),

    CONNECT_EXCEPTION(
        102,
        "Connect Exception. Please verify if network is reachable"
    ),

    SECRET_KEY_MISSING(
        114,
        "ULS configuration failed. Secret Key not configured"
    ),

    JSON_ERROR(
        121,
        "JSON Error while processing API response"
    ),
    INTERNAL_ERROR(
        125,
        "Internal Error"
    ),
    PARSING_ERROR(
        126,
        "Parsing Error"
    ),
    INVALID_ARGUMENTS(
        131,
        "Invalid arguments"
    ),
    CONNECTION_NOT_SET(
        133,
        "PubNub Connection not set"
    ),

    GROUP_MISSING(
        136,
        "Group Missing"
    ),

    SUBSCRIBE_KEY_MISSING(
        138,
        "ULS configuration failed. Subscribe Key not configured."
    ),

    PUBLISH_KEY_MISSING(
        139,
        "ULS configuration failed. Publish Key not configured."
    ),

    SUBSCRIBE_TIMEOUT(
        130,
        "Subscribe Timeout"
    ),

    HTTP_ERROR(
        103,
        "HTTP Error. Please check network connectivity. Please contact support with error details if the issue persists."
    ),

    MESSAGE_MISSING(
        142,
        "Message Missing"
    ),

    CHANNEL_MISSING(
        132,
        "Channel Missing"
    ),

    CRYPTO_ERROR(
        135,
        "Error while encrypting/decrypting message. Please contact support with error details."
    ),

    STATE_MISSING(
        140,
        "State Missing."
    ),

    CHANNEL_AND_GROUP_MISSING(
        141,
        "Channel and Group Missing."
    ),

    PUSH_TYPE_MISSING(
        143,
        "Push Type Missing."
    ),

    DEVICE_ID_MISSING(
        144,
        "Device ID Missing"
    ),

    TIMETOKEN_MISSING(
        145,
        "Timetoken Missing."
    ),

    CHANNELS_TIMETOKEN_MISMATCH(
        146,
        "Channels and timetokens are not equal in size."
    ),

    USER_MISSING(
        147,
        "User is missing"
    ),

    USER_ID_MISSING(
        148,
        "User ID is missing"
    ),

    USER_NAME_MISSING(
        149,
        "User name is missing"
    ),

    RESOURCES_MISSING(
        153,
        "Resources missing"
    ),

    PERMISSION_MISSING(
        156,
        "Permission missing"
    ),

    INVALID_ACCESS_TOKEN(
        157,
        "Invalid access token"
    ),

    MESSAGE_ACTION_MISSING(
        158,
        "Message action is missing."
    ),

    MESSAGE_ACTION_TYPE_MISSING(
        159,
        "Message action type is missing."
    ),

    MESSAGE_ACTION_VALUE_MISSING(
        160,
        "Message action value is missing."
    ),

    MESSAGE_TIMETOKEN_MISSING(
        161,
        "Message timetoken is missing."
    ),

    MESSAGE_ACTION_TIMETOKEN_MISSING(
        162,
        "Message action timetoken is missing."
    ),

    HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS(
        163,
        "History can return message action data for a single channel only. Either pass a single channel or disable the includeMessageActions flag."
    ),

    PUSH_TOPIC_MISSING(
        164,
        "Push notification topic is missing. Required only if push type is APNS2."
    ),

    TOKEN_MISSING(
        168,
        "Token missing"
    ),

    UUID_NULL_OR_EMPTY(
        169,
        "Uuid can't be null nor empty"
    ),

    USERID_NULL_OR_EMPTY(
        170,
        "UserId can't have empty value"
    ),

    CHANNEL_OR_CHANNEL_GROUP_MISSING(
        171,
        "Please, provide channel or channelGroup"
    ),

    UNKNOWN_CRYPTOR(
        172,
        "Cryptor not found."
    ),

    CRYPTOR_DATA_HEADER_SIZE_TO_SMALL(
        173,
        "Cryptor data size is to small."
    ),

    CRYPTOR_HEADER_VERSION_UNKNOWN(
        174,
        "Cryptor header version unknown. Please, update SDK."
    ),

    CRYPTOR_HEADER_PARSE_ERROR(
        175,
        "Cryptor header parse error."
    ),

    ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED(
        176,
        "Encryption of empty data not allowed."
    ),

    ;

    override fun toString(): String {
        return "PubNubError(name=$name, code=$code, message='$message')"
    }
}
