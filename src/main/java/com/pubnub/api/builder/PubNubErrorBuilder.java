package com.pubnub.api.builder;

import com.pubnub.api.PubNubError;


public final class PubNubErrorBuilder {

    // Error Codes
    /**
     * Timeout Error .
     */
    public static final int PNERR_TIMEOUT = 100;

    /**
     *
     */
    public static final int PNERR_PUBNUB_ERROR = 101;

    /**
     * Connect Exception . Network Unreachable.
     */
    public static final int PNERR_CONNECT_EXCEPTION = 102;

    /**
     * Please check network connectivity. Please contact support with error
     * details if issue persists.
     */
    public static final int PNERR_HTTP_ERROR = 103;

    /**
     * Client Timeout .
     */
    public static final int PNERR_CLIENT_TIMEOUT = 104;

    /**
     * An ULS singature error occurred . Please contact support with error
     * details.
     */
    public static final int PNERR_ULSSIGN_ERROR = 105;

    /**
     * Please verify if network is reachable
     */
    public static final int PNERR_NETWORK_ERROR = 106;

    /**
     * PubNub Exception .
     */
    public static final int PNERR_PUBNUB_EXCEPTION = 108;

    /**
     * Disconnect .
     */
    public static final int PNERR_DISCONNECT = 109;

    /**
     * Disconnect and Resubscribe Received .
     */
    public static final int PNERR_DISCONN_AND_RESUB = 110;

    /**
     * Gateway Timeout
     */
    public static final int PNERR_GATEWAY_TIMEOUT = 111;

    /**
     * PubNub server returned HTTP 403 forbidden status code. Happens when wrong
     * authentication key is used .
     */
    public static final int PNERR_FORBIDDEN = 112;
    /**
     * PubNub server returned HTTP 401 unauthorized status code Happens when
     * authentication key is missing .
     */
    public static final int PNERR_UNAUTHORIZED = 113;

    /**
     * Secret key not configured
     */
    public static final int PNERR_SECRET_KEY_MISSING = 114;

    // internal error codes

    /**
     * Error while encrypting message to be published to PubNub Cloud . Please
     * contact support with error details.
     */
    public static final int PNERR_ENCRYPTION_ERROR = 115;

    /**
     * Decryption Error . Please contact support with error details.
     */
    public static final int PNERR_DECRYPTION_ERROR = 116;

    /**
     * Invalid Json . Please contact support with error details.
     */
    public static final int PNERR_INVALID_JSON = 117;

    /**
     * Unable to open input stream . Please contact support with error details.
     */
    public static final int PNERR_GETINPUTSTREAM = 118;

    /**
     * Malformed URL . Please contact support with error details .
     */
    public static final int PNERR_MALFORMED_URL = 119;

    /**
     * Error in opening URL . Please contact support with error details.
     */
    public static final int PNERR_URL_OPEN = 120;

    /**
     * JSON Error while processing API response. Please contact support with
     * error details.
     */
    public static final int PNERR_JSON_ERROR = 121;

    /**
     * Protocol Exception . Please contact support with error details.
     */
    public static final int PNERR_PROTOCOL_EXCEPTION = 122;

    /**
     * Unable to read input stream . Please contact support with error details.
     */
    public static final int PNERR_READINPUT = 123;

    /**
     * Bad gateway . Please contact support with error details.
     */
    public static final int PNERR_BAD_GATEWAY = 124;

    /**
     * PubNub server returned HTTP 502 internal server error status code. Please
     * contact support with error details.
     */
    public static final int PNERR_INTERNAL_ERROR = 125;

    /**
     * Parsing Error .
     */
    public static final int PNERR_PARSING_ERROR = 126;

    /**
     * Bad Request . Please contact support with error details.
     */
    public static final int PNERR_BAD_REQUEST = 127;

    public static final int PNERR_HTTP_RC_ERROR = 128;
    /**
     * PubNub server or intermediate server returned HTTP 404 unauthorized
     * status code
     */
    public static final int PNERR_NOT_FOUND = 129;

    /**
     * Subscribe Timeout .
     */
    public static final int PNERR_HTTP_SOCKET_TIMEOUT = 130;

    /**
     * Invalid arguments provided to API
     */
    public static final int PNERR_INVALID_ARGUMENTS = 131;

    /**
     * Channel missing
     */
    public static final int PNERR_CHANNEL_MISSING = 132;

    /**
     * PubNub connection not set on sender
     */
    public static final int PNERR_CONNECTION_NOT_SET = 133;

    /**
     * Error while parsing group name
     */
    public static final int PNERR_CHANNEL_GROUP_PARSING_ERROR = 134;

    /**
     * Crypto Error
     */
    public static final int PNERR_CRYPTO_ERROR = 135;

    /**
     * Group missing
     */
    public static final int PNERR_GROUP_MISSING = 136;

    /**
     * Auth Keys missing
     */
    public static final int PNERR_AUTH_KEYS_MISSING = 137;

    /**
     * Subscribe Key missing
     */
    public static final int PNERR_SUBSCRIBE_KEY_MISSING = 138;

    /**
     * Publish Key missing
     */
    public static final int PNERR_PUBLISH_KEY_MISSING = 139;

    /**
     * State missing
     */
    public static final int PNERR_STATE_MISSING = 140;

    /**
     * Channel and Group missing
     */
    public static final int PNERR_CHANNEL_AND_GROUP_MISSING = 141;

    /**
     * Message missing
     */
    public static final int PNERR_MESSAGE_MISSING = 142;

    /**
     * Push TYpe missing
     */
    public static final int PNERR_PUSH_TYPE_MISSING = 143;

    /**
     * Device ID missing
     */
    public static final int PNERR_DEVICE_ID_MISSING = 144;

    /**
     * Timetoken missing
     */
    public static final int PNERR_TIMETOKEN_MISSING = 145;

    /**
     * Timetoken missing
     */
    public static final int PNERR_CHANNELS_TIMETOKEN_MISMATCH = 146;

    /**
     * UUID missing
     */
    public static final int PNERR_UUID_MISSING = 147;

    /**
     * User ID missing
     */
    public static final int PNERR_USER_ID_MISSING = 148;

    /**
     * User name missing
     */
    public static final int PNERR_USER_NAME_MISSING = 149;

    /**
     * Space missing
     */
    public static final int PNERR_SPACE_MISSING = 150;

    /**
     * Space ID missing
     */
    public static final int PNERR_SPACE_ID_MISSING = 151;

    /**
     * Space name missing
     */
    public static final int PNERR_SPACE_NAME_MISSING = 152;

    /**
     * Resources missing
     */
    public static final int PNERR_RESOURCES_MISSING = 153;

    /**
     * TTL missing
     */
    public static final int PNERR_TTL_MISSING = 154;

    /**
     * Invalid meta parameter
     */
    public static final int PNERR_INVALID_META = 155;

    /**
     * Permission missing
     */
    public static final int PNERR_PERMISSION_MISSING = 156;

    /**
     * Invalid access token
     */
    public static final int PNERR_INVALID_ACCESS_TOKEN = 157;

    /**
     * Message action missing
     */
    public static final int PNERR_MESSAGE_ACTION_MISSING = 158;

    /**
     * Message action type missing
     */
    public static final int PNERR_MESSAGE_ACTION_TYPE_MISSING = 159;

    /**
     * Message action value missing
     */
    public static final int PNERR_MESSAGE_ACTION_VALUE_MISSING = 160;

    /**
     * Message timetoken missing
     */
    public static final int PNERR_MESSAGE_TIMETOKEN_MISSING = 161;

    /**
     * Message action timetoken missing
     */
    public static final int PNERR_MESSAGE_ACTION_TIMETOKEN_MISSING = 162;

    /**
     * Retrieving message actions for multiple channels
     */
    public static final int PNERR_HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS = 163;

    /**
     * Push topic missing
     */
    public static final int PNERR_PUSH_TOPIC_MISSING = 164;

    /**
     * No more pages to load after last one
     */
    public static final int PNERR_PAGINATION_NEXT_OUT_OF_BOUNDS = 165;

    /**
     * No pages to load before first one
     */
    public static final int PNERR_PAGINATION_PREV_OUT_OF_BOUNDS = 166;

    /**
     * Payload too large
     */
    public static final int PNERR_PAYLOAD_TOO_LARGE = 167;

    /**
     * Token missing
     */
    public static final int PNERR_TOKEN_MISSING = 168;

    /**
     * UUID can't be null nor empty
     */
    public static final int PNERR_UUID_NULL_OR_EMPTY = 169;

    /**
     * UserId can't be null nor empty
     */
    public static final int PNERR_USERID_NULL_OR_EMPTY = 170;

    /**
     * SpaceId can't be null nor empty
     */
    public static final int PNERR_SPACEID_NULL_OR_EMPTY = 171;

    /**
     * State must be a JSON object.
     */
    public static final int PNERR_STATE_MUST_BE_JSON_OBJECT = 172;

    // Error Objects
    public static final PubNubError PNERROBJ_TIMEOUT = PubNubError.builder()
            .errorCode(PNERR_TIMEOUT)
            .message("Timeout Occurred")
            .build();

    public static final PubNubError PNERROBJ_INTERNAL_ERROR = PubNubError.builder()
            .errorCode(PNERR_INTERNAL_ERROR)
            .message("Internal Error")
            .build();

    public static final PubNubError PNERROBJ_ENCRYPTION_ERROR = PubNubError.builder()
            .errorCode(PNERR_ENCRYPTION_ERROR)
            .message("Error while encrypting message to be published to PubNub Cloud. Please contact support with "
                    + "error details.")
            .build();

    public static final PubNubError PNERROBJ_DECRYPTION_ERROR = PubNubError.builder()
            .errorCode(PNERR_DECRYPTION_ERROR)
            .message("Decryption Error. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_INVALID_JSON = PubNubError.builder()
            .errorCode(PNERR_INVALID_JSON)
            .message("Invalid Json. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_JSON_ERROR = PubNubError.builder()
            .errorCode(PNERR_JSON_ERROR)
            .message("JSON Error while processing API response. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_MALFORMED_URL = PubNubError.builder()
            .errorCode(PNERR_MALFORMED_URL)
            .message("Malformed URL. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_PUBNUB_ERROR = PubNubError.builder()
            .errorCode(PNERR_PUBNUB_ERROR)
            .message("PubNub Error")
            .build();

    public static final PubNubError PNERROBJ_URL_OPEN = PubNubError.builder()
            .errorCode(PNERR_URL_OPEN)
            .message("Error opening url. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_PROTOCOL_EXCEPTION = PubNubError.builder()
            .errorCode(PNERR_PROTOCOL_EXCEPTION)
            .message("Protocol Exception. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_CONNECT_EXCEPTION = PubNubError.builder()
            .errorCode(PNERR_CONNECT_EXCEPTION)
            .message("Connect Exception. Please verify if network is reachable.")
            .build();

    public static final PubNubError PNERROBJ_HTTP_RC_ERROR = PubNubError.builder()
            .errorCode(PNERR_HTTP_RC_ERROR)
            .message("Unable to get PnResponse Code. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_GETINPUTSTREAM = PubNubError.builder()
            .errorCode(PNERR_GETINPUTSTREAM)
            .message("Unable to get Input Stream Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_READINPUT = PubNubError.builder()
            .errorCode(PNERR_READINPUT)
            .message("Unable to read Input Stream. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_BAD_REQUEST = PubNubError.builder()
            .errorCode(PNERR_BAD_REQUEST)
            .message("Bad request. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_HTTP_ERROR = PubNubError.builder()
            .errorCode(PNERR_HTTP_ERROR)
            .message("HTTP Error. Please check network connectivity. Please contact support with error details if "
                    + "issue persists.")
            .build();

    public static final PubNubError PNERROBJ_BAD_GATEWAY = PubNubError.builder()
            .errorCode(PNERR_BAD_GATEWAY)
            .message("Bad Gateway. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_CLIENT_TIMEOUT = PubNubError.builder()
            .errorCode(PNERR_CLIENT_TIMEOUT)
            .message("Client Timeout")
            .build();

    public static final PubNubError PNERROBJ_GATEWAY_TIMEOUT = PubNubError.builder()
            .errorCode(PNERR_GATEWAY_TIMEOUT)
            .message("Gateway Timeout")
            .build();

    public static final PubNubError PNERROBJ_5023_INTERNAL_ERROR = PubNubError.builder()
            .errorCode(PNERR_INTERNAL_ERROR)
            .message("Internal Server Error. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_PARSING_ERROR = PubNubError.builder()
            .errorCode(PNERR_PARSING_ERROR)
            .message("Parsing Error")
            .build();

    public static final PubNubError PNERROBJ_PUBNUB_EXCEPTION = PubNubError.builder()
            .errorCode(PNERR_PUBNUB_EXCEPTION)
            .message("PubNub Exception")
            .build();

    public static final PubNubError PNERROBJ_DISCONNECT = PubNubError.builder()
            .errorCode(PNERR_DISCONNECT)
            .message("Disconnect")
            .build();

    public static final PubNubError PNERROBJ_DISCONN_AND_RESUB = PubNubError.builder()
            .errorCode(PNERR_DISCONN_AND_RESUB)
            .message("Disconnect and Resubscribe")
            .build();

    public static final PubNubError PNERROBJ_FORBIDDEN = PubNubError.builder()
            .errorCode(PNERR_FORBIDDEN)
            .message("Authentication Failure. Incorrect Authentication Key")
            .build();

    public static final PubNubError PNERROBJ_UNAUTHORIZED = PubNubError.builder()
            .errorCode(PNERR_UNAUTHORIZED)
            .message("Authentication Failure. Authentication Key is missing")
            .build();

    public static final PubNubError PNERROBJ_SECRET_KEY_MISSING = PubNubError.builder()
            .errorCode(PNERR_SECRET_KEY_MISSING)
            .message("ULS configuration failed. Secret Key not configured.")
            .build();

    public static final PubNubError PNERROBJ_SUBSCRIBE_KEY_MISSING = PubNubError.builder()
            .errorCode(PNERR_SUBSCRIBE_KEY_MISSING)
            .message("ULS configuration failed. Subscribe Key not configured.")
            .build();

    public static final PubNubError PNERROBJ_PUBLISH_KEY_MISSING = PubNubError.builder()
            .errorCode(PNERR_PUBLISH_KEY_MISSING)
            .message("ULS configuration failed. Publish Key not configured.")
            .build();

    public static final PubNubError PNERROBJ_ULSSIGN_ERROR = PubNubError.builder()
            .errorCode(PNERR_ULSSIGN_ERROR)
            .message("Invalid Signature. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_5075_NETWORK_ERROR = PubNubError.builder()
            .errorCode(PNERR_NETWORK_ERROR)
            .message("Network Error. Please verify if network is reachable.")
            .build();

    public static final PubNubError PNERROBJ_NOT_FOUND_ERROR = PubNubError.builder()
            .errorCode(PNERR_NOT_FOUND)
            .message("Page Not Found Please verify if network is reachable. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_SOCKET_TIMEOUT = PubNubError.builder()
            .errorCode(PNERR_HTTP_SOCKET_TIMEOUT)
            .message("Socket Timeout.")
            .build();

    public static final PubNubError PNERROBJ_INVALID_ARGUMENTS = PubNubError.builder()
            .errorCode(PNERR_INVALID_ARGUMENTS)
            .message("INVALID ARGUMENTS.")
            .build();

    public static final PubNubError PNERROBJ_CHANNEL_MISSING = PubNubError.builder()
            .errorCode(PNERR_CHANNEL_MISSING)
            .message("Channel Missing.")
            .build();

    public static final PubNubError PNERROBJ_STATE_MISSING = PubNubError.builder()
            .errorCode(PNERR_STATE_MISSING)
            .message("State Missing.")
            .build();

    public static final PubNubError PNERROBJ_STATE_MUST_BE_JSON_OBJECT = PubNubError.builder()
            .errorCode(PNERR_STATE_MUST_BE_JSON_OBJECT)
            .message("State must be a JSON object.")
            .build();

    public static final PubNubError PNERROBJ_MESSAGE_MISSING = PubNubError.builder()
            .errorCode(PNERR_MESSAGE_MISSING)
            .message("Message Missing.")
            .build();

    public static final PubNubError PNERROBJ_PUSH_TYPE_MISSING = PubNubError.builder()
            .errorCode(PNERR_PUSH_TYPE_MISSING)
            .message("Push Type Missing.")
            .build();

    public static final PubNubError PNERROBJ_DEVICE_ID_MISSING = PubNubError.builder()
            .errorCode(PNERR_DEVICE_ID_MISSING)
            .message("Device Id Missing.")
            .build();

    public static final PubNubError PNERROBJ_CONNECTION_NOT_SET = PubNubError.builder()
            .errorCode(PNERR_CONNECTION_NOT_SET)
            .message("PubNub Connection not set")
            .build();

    public static final PubNubError PNERROBJ_GROUP_MISSING = PubNubError.builder()
            .errorCode(PNERR_GROUP_MISSING)
            .message("Group Missing.")
            .build();

    public static final PubNubError PNERROBJ_CHANNEL_AND_GROUP_MISSING = PubNubError.builder()
            .errorCode(PNERR_CHANNEL_AND_GROUP_MISSING)
            .message("Channel and Group Missing.")
            .build();

    public static final PubNubError PNERROBJ_AUTH_KEYS_MISSING = PubNubError.builder()
            .errorCode(PNERR_AUTH_KEYS_MISSING)
            .message("Auth Keys Missing.")
            .build();

    public static final PubNubError PNERROBJ_CHANNEL_GROUP_PARSING_ERROR = PubNubError.builder()
            .errorCode(PNERR_CHANNEL_GROUP_PARSING_ERROR)
            .message("Channel group name is invalid")
            .build();

    public static final PubNubError PNERROBJ_CRYPTO_ERROR = PubNubError.builder()
            .errorCode(PNERR_CRYPTO_ERROR)
            .message("Error while encrypting/decrypting message. Please contact support with error details.")
            .build();

    public static final PubNubError PNERROBJ_TIMETOKEN_MISSING = PubNubError.builder()
            .errorCode(PNERR_TIMETOKEN_MISSING)
            .message("Timetoken Missing.")
            .build();

    public static final PubNubError PNERROBJ_CHANNELS_TIMETOKEN_MISMATCH = PubNubError.builder()
            .errorCode(PNERR_CHANNELS_TIMETOKEN_MISMATCH)
            .message("Channels and timetokens are not equal in size.")
            .build();

    public static final PubNubError PNERROBJ_UUID_MISSING = PubNubError.builder()
            .errorCode(PNERR_UUID_MISSING)
            .message("UUID is missing")
            .build();

    public static final PubNubError PNERROBJ_USER_ID_MISSING = PubNubError.builder()
            .errorCode(PNERR_USER_ID_MISSING)
            .message("User ID is missing")
            .build();

    public static final PubNubError PNERROBJ_USER_NAME_MISSING = PubNubError.builder()
            .errorCode(PNERR_USER_NAME_MISSING)
            .message("User name is missing")
            .build();

    public static final PubNubError PNERROBJ_SPACE_MISSING = PubNubError.builder()
            .errorCode(PNERR_SPACE_MISSING)
            .message("Space is missing")
            .build();

    public static final PubNubError PNERROBJ_SPACE_ID_MISSING = PubNubError.builder()
            .errorCode(PNERR_SPACE_ID_MISSING)
            .message("Space ID is missing")
            .build();

    public static final PubNubError PNERROBJ_SPACE_NAME_MISSING = PubNubError.builder()
            .errorCode(PNERR_SPACE_NAME_MISSING)
            .message("Space name is missing")
            .build();

    public static final PubNubError PNERROBJ_RESOURCES_MISSING = PubNubError.builder()
            .errorCode(PNERR_RESOURCES_MISSING)
            .message("Resources missing")
            .build();

    public static final PubNubError PNERROBJ_TTL_MISSING = PubNubError.builder()
            .errorCode(PNERR_TTL_MISSING)
            .message("TTL missing")
            .build();

    public static final PubNubError PNERROBJ_INVALID_META = PubNubError.builder()
            .errorCode(PNERR_INVALID_META)
            .message("Invalid meta parameter")
            .build();

    public static final PubNubError PNERROBJ_PERMISSION_MISSING = PubNubError.builder()
            .errorCode(PNERR_PERMISSION_MISSING)
            .message("Permission missing")
            .build();

    public static final PubNubError PNERROBJ_INVALID_ACCESS_TOKEN = PubNubError.builder()
            .errorCode(PNERR_INVALID_ACCESS_TOKEN)
            .message("Invalid access token")
            .build();

    public static final PubNubError PNERROBJ_MESSAGE_ACTION_MISSING = PubNubError.builder()
            .errorCode(PNERR_MESSAGE_ACTION_MISSING)
            .message("Message action is missing")
            .build();

    public static final PubNubError PNERROBJ_MESSAGE_ACTION_TYPE_MISSING = PubNubError.builder()
            .errorCode(PNERR_MESSAGE_ACTION_TYPE_MISSING)
            .message("Message action type is missing")
            .build();

    public static final PubNubError PNERROBJ_MESSAGE_ACTION_VALUE_MISSING = PubNubError.builder()
            .errorCode(PNERR_MESSAGE_ACTION_VALUE_MISSING)
            .message("Message action value is missing")
            .build();

    public static final PubNubError PNERROBJ_MESSAGE_TIMETOKEN_MISSING = PubNubError.builder()
            .errorCode(PNERR_MESSAGE_TIMETOKEN_MISSING)
            .message("Message timetoken is missing")
            .build();

    public static final PubNubError PNERROBJ_MESSAGE_ACTION_TIMETOKEN_MISSING = PubNubError.builder()
            .errorCode(PNERR_MESSAGE_ACTION_TIMETOKEN_MISSING)
            .message("Message action timetoken is missing")
            .build();

    public static final PubNubError PNERROBJ_HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS = PubNubError.builder()
            .errorCode(PNERR_HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS)
            .message("History can return message action data for a single channel only. "
                    .concat("Either pass a single channel or disable the includeMessageActions flag."))
            .build();

    public static final PubNubError PNERROBJ_PUSH_TOPIC_MISSING = PubNubError.builder()
            .errorCode(PNERR_PUSH_TOPIC_MISSING)
            .message("Push notification topic is missing. Required only if push type is APNS2.")
            .build();

    public static final PubNubError PNERROBJ_PAGINATION_NEXT_OUT_OF_BOUNDS = PubNubError.builder()
            .errorCode(PNERR_PAGINATION_NEXT_OUT_OF_BOUNDS)
            .message("No more pages to load after last one.")
            .build();

    public static final PubNubError PNERROBJ_PAGINATION_PREV_OUT_OF_BOUNDS = PubNubError.builder()
            .errorCode(PNERR_PAGINATION_PREV_OUT_OF_BOUNDS)
            .message("No pages to load before first one.")
            .build();

    public static final PubNubError PNERROBJ_PAYLOAD_TOO_LARGE = PubNubError.builder()
            .errorCode(PNERR_PAYLOAD_TOO_LARGE)
            .message("Payload too large.")
            .build();

    public static final PubNubError PNERROBJ_TOKEN_MISSING = PubNubError.builder()
            .errorCode(PNERR_TOKEN_MISSING)
            .message("Token missing.")
            .build();

    public static final PubNubError PNERROBJ_UUID_NULL_OR_EMPTY = PubNubError.builder()
            .errorCode(PNERR_UUID_NULL_OR_EMPTY)
            .message("Uuid can't be null nor empty.")
            .build();

    public static final PubNubError PNERROBJ_USERID_NULL_OR_EMPTY = PubNubError.builder()
            .errorCode(PNERR_USERID_NULL_OR_EMPTY)
            .message("UserId can't be null nor empty.")
            .build();

    public static final PubNubError PNERROBJ_SPACEID_NULL_OR_EMPTY = PubNubError.builder()
            .errorCode(PNERR_SPACEID_NULL_OR_EMPTY)
            .message("SpaceId can't be null nor empty.")
            .build();

    private PubNubErrorBuilder() {

    }

    public static PubNubError createCryptoError(int code, String message) {
        return PubNubError.builder()
                .errorCode(PNERR_CRYPTO_ERROR)
                .errorCodeExtended(code)
                .message("Error while encrypting/decrypting message. Please contact support with error details. - ".concat(message))
                .build();
    }

}
