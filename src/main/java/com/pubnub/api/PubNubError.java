package com.pubnub.api;


import com.fasterxml.jackson.databind.JsonNode;

/**
     * PubNubError object is passed to errorCallback. It contains details of error,
     * like error code, error string, and optional message
     *
     * @author PubNub
     *
     */
    public class PubNubError {

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
        static final int PNERR_ULSSIGN_ERROR = 105;

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
        static final int PNERR_GETINPUTSTREAM = 118;

        /**
         * Malformed URL . Please contact support with error details .
         */
        static final int PNERR_MALFORMED_URL = 119;

        /**
         * Error in opening URL . Please contact support with error details.
         */
        public static final int PNERR_URL_OPEN = 120;

        /**
         * JSON Error while processing API response. Please contact support with
         * error details.
         */
        static final int PNERR_JSON_ERROR = 121;

        /**
         * Protocol Exception . Please contact support with error details.
         */
        static final int PNERR_PROTOCOL_EXCEPTION = 122;

        /**
         * Unable to read input stream . Please contact support with error details.
         */
        static final int PNERR_READINPUT = 123;

        /**
         * Bad gateway . Please contact support with error details.
         */
        static final int PNERR_BAD_GATEWAY = 124;

        /**
         * PubNub server returned HTTP 502 internal server error status code. Please
         * contact support with error details.
         */
        static final int PNERR_INTERNAL_ERROR = 125;

        /**
         * Parsing Error .
         */
        static final int PNERR_PARSING_ERROR = 126;

        /**
         * Bad Request . Please contact support with error details.
         */
        static final int PNERR_BAD_REQUEST = 127;

        public static final int PNERR_HTTP_RC_ERROR = 128;
        /**
         * PubNub server or intermediate server returned HTTP 404 unauthorized
         * status code
         *
         */
        public static final int PNERR_NOT_FOUND = 129;

        /**
         * Subscribe Timeout .
         */
        static final int PNERR_HTTP_SUBSCRIBE_TIMEOUT = 130;

        /**
         * Invalid arguments provided to API
         *
         */
        public static final int PNERR_INVALID_ARGUMENTS = 131;

        /**
         * Channel missing
         *
         */
        public static final int PNERR_CHANNEL_MISSING = 132;

        /**
         * PubNub connection not set on sender
         *
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
        // Error Objects

        static final PubNubError PNERROBJ_TIMEOUT = new PubNubError(PNERR_TIMEOUT, "Timeout Occurred");

        static final PubNubError PNERROBJ_INTERNAL_ERROR = new PubNubError(PNERR_INTERNAL_ERROR, "Internal Error");

        static final PubNubError PNERROBJ_ENCRYPTION_ERROR = new PubNubError(PNERR_ENCRYPTION_ERROR,
                "Error while encrypting message to be published to PubNub Cloud ."
                        + "Please contact support with error details.");

        static final PubNubError PNERROBJ_DECRYPTION_ERROR = new PubNubError(PNERR_DECRYPTION_ERROR, "Decryption Error. "
                + "Please contact support with error details.");

        static final PubNubError PNERROBJ_INVALID_JSON = new PubNubError(PNERR_INVALID_JSON, "Invalid Json. "
                + "Please contact support with error details.");

        static final PubNubError PNERROBJ_JSON_ERROR = new PubNubError(PNERR_JSON_ERROR,
                "JSON Error while processing API response. " + "Please contact support with error details.");

        static final PubNubError PNERROBJ_MALFORMED_URL = new PubNubError(PNERR_MALFORMED_URL, "Malformed URL ."
                + "Please contact support with error details.");

        static final PubNubError PNERROBJ_PUBNUB_ERROR = new PubNubError(PNERR_PUBNUB_ERROR, "PubNub Error");

        static final PubNubError PNERROBJ_URL_OPEN = new PubNubError(PNERR_URL_OPEN, "Error opening url. "
                + "Please contact support with error details.");

        static final PubNubError PNERROBJ_PROTOCOL_EXCEPTION = new PubNubError(PNERR_PROTOCOL_EXCEPTION,
                "Protocol Exception. " + "Please contact support with error details.");

        static final PubNubError PNERROBJ_CONNECT_EXCEPTION = new PubNubError(PNERR_CONNECT_EXCEPTION,
                "Connect Exception. " + "Please verify if network is reachable. ");

        static final PubNubError PNERROBJ_HTTP_RC_ERROR = new PubNubError(PNERR_HTTP_RC_ERROR,
                "Unable to get PnResponse Code. " + "Please contact support with error details.");

        static final PubNubError PNERROBJ_GETINPUTSTREAM = new PubNubError(PNERR_GETINPUTSTREAM,
                "Unable to get Input Stream. " + "Please contact support with error details.");

        static final PubNubError PNERROBJ_READINPUT = new PubNubError(PNERR_READINPUT, "Unable to read Input Stream. "
                + "Please contact support with error details.");

        static final PubNubError PNERROBJ_BAD_REQUEST = new PubNubError(PNERR_BAD_REQUEST, "Bad request. "
                + "Please contact support with error details.");

        public static final PubNubError PNERROBJ_HTTP_ERROR = new PubNubError(PNERR_HTTP_ERROR, "HTTP Error. "
                + "Please check network connectivity. " + "Please contact support with error details if issue persists.");

        static final PubNubError PNERROBJ_BAD_GATEWAY = new PubNubError(PNERR_BAD_GATEWAY, "Bad Gateway. "
                + "Please contact support with error details.");

        static final PubNubError PNERROBJ_CLIENT_TIMEOUT = new PubNubError(PNERR_CLIENT_TIMEOUT, "Client Timeout");

        static final PubNubError PNERROBJ_GATEWAY_TIMEOUT = new PubNubError(PNERR_GATEWAY_TIMEOUT, "Gateway Timeout");

        static final PubNubError PNERROBJ_5023_INTERNAL_ERROR = new PubNubError(PNERR_INTERNAL_ERROR,
                "Internal Server Error. " + "Please contact support with error details.");

        public static final PubNubError PNERROBJ_PARSING_ERROR = new PubNubError(PNERR_PARSING_ERROR, "Parsing Error");

        static final PubNubError PNERROBJ_PUBNUB_EXCEPTION = new PubNubError(PNERR_PUBNUB_EXCEPTION, "PubNub Exception");

        static final PubNubError PNERROBJ_DISCONNECT = new PubNubError(PNERR_DISCONNECT, "Disconnect");

        static final PubNubError PNERROBJ_DISCONN_AND_RESUB = new PubNubError(PNERR_DISCONN_AND_RESUB,
                "Disconnect and Resubscribe");

        static final PubNubError PNERROBJ_FORBIDDEN = new PubNubError(PNERR_FORBIDDEN, "Authentication Failure. "
                + "Incorrect Authentication Key");

        static final PubNubError PNERROBJ_UNAUTHORIZED = new PubNubError(PNERR_UNAUTHORIZED, "Authentication Failure. "
                + "Authentication Key is missing");

        static final PubNubError PNERROBJ_SECRET_KEY_MISSING = new PubNubError(PNERR_SECRET_KEY_MISSING,
                "ULS configuration failed. Secret Key not configured. ");

        static final PubNubError PNERROBJ_ULSSIGN_ERROR = new PubNubError(PNERR_ULSSIGN_ERROR, "Invalid Signature . "
                + "Please contact support with error details.");

        static final PubNubError PNERROBJ_5075_NETWORK_ERROR = new PubNubError(PNERR_NETWORK_ERROR, "Network Error. "
                + "Please verify if network is reachable.");
        static final PubNubError PNERROBJ_NOT_FOUND_ERROR = new PubNubError(PNERR_NOT_FOUND, "Page Not Found"
                + "Please verify if network is reachable." + "Please contact support with error details.");

        public static final PubNubError PNERROBJ_SUBSCRIBE_TIMEOUT = new PubNubError(PNERR_HTTP_SUBSCRIBE_TIMEOUT,
                "Subscribe Timeout.");

        public static final PubNubError PNERROBJ_INVALID_ARGUMENTS = new PubNubError(PNERR_INVALID_ARGUMENTS, "INVALID ARGUMENTS.");

        public static final PubNubError PNERROBJ_CHANNEL_MISSING = new PubNubError(PNERR_CHANNEL_MISSING, "Channel Missing.");

        public static final PubNubError PNERROBJ_CONNECTION_NOT_SET = new PubNubError(PNERR_CONNECTION_NOT_SET,
                "PubNub Connection not set");

        public static final PubNubError PNERROBJ_CHANNEL_GROUP_PARSING_ERROR = new PubNubError(PNERR_CHANNEL_GROUP_PARSING_ERROR,
                "Channel group name is invalid");

        public static final PubNubError PNERROBJ_CRYPTO_ERROR = new PubNubError(PNERR_CRYPTO_ERROR,
                "Error while encrypting/decrypting message." + "Please contact support with error details.");

        public final int errorCode;
        public final int errorCodeExtended;
        public final JsonNode errorObject;
        private final String errorString;
        private String message;

        private PubNubError(int errorCode, int errorCodeExtended, String errorString, JsonNode errorObject, String message) {
            this.errorCodeExtended = errorCodeExtended;
            this.errorCode = errorCode;
            this.errorString = errorString;
            this.errorObject = errorObject;
            this.message = message;
        }

        private PubNubError(int errorCode, int errorCodeExtended, String errorString) {
            this(errorCode, errorCodeExtended, errorString, null, null);
        }

        private PubNubError(int errorCode, int errorCodeExtended, String errorString, JsonNode errorObject) {
            this(errorCode, errorCodeExtended, errorString, errorObject, null);
        }

        private PubNubError(int errorCode, String errorString) {
            this(errorCode, 0, errorString, null, null);
        }

        private PubNubError(int errorCode, int errorCodeExtended, String errorString, String message) {
            this(errorCode, errorCodeExtended, errorString, null, message);
        }

        public PubNubError(PubNubError error, String message) {
            this(error.errorCode, error.errorCodeExtended, error.errorString, null, message);
        }

        public PubNubError(PubNubError error, JsonNode errorObject) {
            this(error.errorCode, error.errorCodeExtended, error.errorString, errorObject, null);
        }

        public String toString() {
            String value = "[Error: " + errorCode + "-" + errorCodeExtended + "] : " + errorString;
            if (errorObject != null) {
                value += " : " + errorObject;
            }
            if (message != null && message.length() > 0) {
                value += " : " + message;
            }

            return value;
        }

        public static PubNubError getErrorObject(PubNubError error, String message) {
            return new PubNubError(error.errorCode, error.errorCodeExtended, error.errorString, message);
        }

        public static PubNubError getErrorObject(PubNubError error, String message, JsonNode errorObject) {
            return new PubNubError(error.errorCode, error.errorCodeExtended, error.errorString, errorObject, message);
        }

        public  static PubNubError getErrorObject(PubNubError error, int errorCodeExtended, JsonNode errorObject) {
            return new PubNubError(error.errorCode, errorCodeExtended, error.errorString, errorObject);
        }

        public static PubNubError getErrorObject(PubNubError error, int errorCodeExtended) {
            return new PubNubError(error.errorCode, errorCodeExtended, error.errorString);
        }

        public static PubNubError getErrorObject(PubNubError error, int errorCodeExtended, String message) {
            return new PubNubError(error.errorCode, errorCodeExtended, error.errorString, message);
        }

        public String getErrorString() {
            return errorString;
        }

        public String getErrorMessage() {
            return message;
        }
    }

