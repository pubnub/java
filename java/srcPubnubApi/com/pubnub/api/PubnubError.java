package com.pubnub.api;

import org.json.JSONObject;

/**
 * PubnubError object is passed to errorCallback. It contains details of error, like
 * error code, error string, and optional message
 *
 * @author Pubnub
 *
 */
public class PubnubError {


    // Error Codes

    /**
     * Timeout Error .
     */
    public static final int         PNERR_TIMEOUT                              =       100;


    /**
     *
     */
    public static final int         PNERR_PUBNUB_ERROR                         =       101;

    /**
     * Connect Exception .
     * Network Unreachable.
     */
    public static final int         PNERR_CONNECT_EXCEPTION                    =       102;


    /**
     * Please check network connectivity.
     * Please contact support with error details if issue persists.
     */
    public static final int         PNERR_HTTP_ERROR                           =       103;



    /**
     * Client Timeout .
     */
    public static final int         PNERR_CLIENT_TIMEOUT                       =       104;


    /**
     * An ULS singature error occurred .
     * Please contact support with error details.
     */
    static final int                PNERR_ULSSIGN_ERROR                        =       105;

    /**
     * Please verify if network is reachable
     */
    public static final int         PNERR_NETWORK_ERROR                        =       106;

    /**
     * Pubnub Exception .
     */
    public static final int         PNERR_PUBNUB_EXCEPTION                     =       108;


    /**
     * Disconnect .
     */
    public static final int         PNERR_DISCONNECT                           =       109;


    /**
     * Disconnect and Resubscribe Received .
     */
    public static final int         PNERR_DISCONN_AND_RESUB                    =       110;


    /**
     * Gateway Timeout
     */
    public static final int         PNERR_GATEWAY_TIMEOUT                      =       111;

    /**
     * Pubnub server returned HTTP 403 forbidden status code.
     * Happens when wrong authentication key is used .
     */
    public static final int         PNERR_FORBIDDEN                            =       112;
    /**
     * Pubnub server returned HTTP 401 unauthorized status code
     * Happens when authentication key is missing .
     */
    public static final int         PNERR_UNAUTHORIZED                         =       113;

    /**
     * Secret key not configured
     */
    public static final int         PNERR_SECRET_KEY_MISSING                   =       114;


    // internal error codes

    /**
     * Error while encrypting message to be published to Pubnub Cloud .
     * Please contact support with error details.
     */
    public static final int         PNERR_ENCRYPTION_ERROR                     =       115;

    /**
     * Decryption Error .
     * Please contact support with error details.
     */
    public static final int         PNERR_DECRYPTION_ERROR                     =       116;

    /**
     * Invalid Json .
     * Please contact support with error details.
     */
    public static final int         PNERR_INVALID_JSON                         =       117;

    /**
     * Unable to open input stream .
     * Please contact support with error details.
     */
    static final int                PNERR_GETINPUTSTREAM                       =       118;

    /**
     * Malformed URL .
     * Please contact support with error details .
     */
    static final int                PNERR_MALFORMED_URL                        =       119;

    /**
     * Error in opening URL .
     * Please contact support with error details.
     */
    public static final int         PNERR_URL_OPEN                             =       120;

    /**
     * JSON Error while processing API response.
     * Please contact support with error details.
     */
    static final int                PNERR_JSON_ERROR                           =       121;

    /**
     * Protocol Exception .
     * Please contact support with error details.
     */
    static final int                PNERR_PROTOCOL_EXCEPTION                   =       122;

    /**
     * Unable to read input stream .
     * Please contact support with error details.
     */
    static final int                PNERR_READINPUT                            =       123;

    /**
     * Bad gateway .
     * Please contact support with error details.
     */
    static final int                PNERR_BAD_GATEWAY                          =       124;

    /**
     * Pubnub server returned HTTP 502 internal server error status code.
     * Please contact support with error details.
     */
    static final int                PNERR_INTERNAL_ERROR                       =       125;

    /**
     * Parsing Error .
     */
    static final int                PNERR_PARSING_ERROR                        =       126;

    /**
     * Bad Request .
     * Please contact support with error details.
     */
    static final int                PNERR_BAD_REQUEST                          =       127;


    public static final int                PNERR_HTTP_RC_ERROR                        =       128;
    /**
     * Pubnub server or intermediate server returned HTTP 404 unauthorized status code
     *
     */
    public static final int         PNERR_NOT_FOUND                         =       129;


    /**
     * Subscribe Timeout .
     */
    static final int                PNERR_HTTP_SUBSCRIBE_TIMEOUT               =       130;

    /**
     * Invalid arguments provided to API
     *
     */
    public static final int         PNERR_INVALID_ARGUMENTS                     =       131;

    /**
     * Channel missing
     *
     */
    public static final int         PNERR_CHANNEL_MISSING                     =       132;

    /**
     * Pubnub connection not set on sender
     *
     */
    public static final int         PNERR_CONNECTION_NOT_SET                     =       133;

    // Error Objects

    static final PubnubError        PNERROBJ_TIMEOUT                           =
        new PubnubError     (
        PNERR_TIMEOUT,  "Timeout Occurred"
    );

    static final PubnubError        PNERROBJ_INTERNAL_ERROR                    =
        new PubnubError     (
        PNERR_INTERNAL_ERROR ,  "Internal Error"
    );

    static final PubnubError        PNERROBJ_ENCRYPTION_ERROR                  =
        new PubnubError     (
        PNERR_ENCRYPTION_ERROR ,
        "Error while encrypting message to be published to Pubnub Cloud ." +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_DECRYPTION_ERROR                  =
        new PubnubError     (
        PNERR_DECRYPTION_ERROR ,
        "Decryption Error. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_INVALID_JSON                      =
        new PubnubError     (
        PNERR_INVALID_JSON ,
        "Invalid Json. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_JSON_ERROR                        =
        new PubnubError     (
        PNERR_JSON_ERROR ,
        "JSON Error while processing API response. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_MALFORMED_URL                     =
        new PubnubError     (
        PNERR_MALFORMED_URL ,
        "Malformed URL ." +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_PUBNUB_ERROR                      =
        new PubnubError     (
        PNERR_PUBNUB_ERROR ,
        "Pubnub Error"
    );

    static final PubnubError        PNERROBJ_URL_OPEN                          =
        new PubnubError     (
        PNERR_URL_OPEN ,
        "Error opening url. "   +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_PROTOCOL_EXCEPTION                =
        new PubnubError     (
        PNERR_PROTOCOL_EXCEPTION ,
        "Protocol Exception. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_CONNECT_EXCEPTION                 =
        new PubnubError     (
        PNERR_CONNECT_EXCEPTION ,
        "Connect Exception. " +
        "Please verify if network is reachable. "
    );

    static final PubnubError        PNERROBJ_HTTP_RC_ERROR                     =
        new PubnubError     (
        PNERR_HTTP_RC_ERROR ,
        "Unable to get Response Code. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_GETINPUTSTREAM                    =
        new PubnubError     (
        PNERR_GETINPUTSTREAM ,
        "Unable to get Input Stream. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_READINPUT                         =
        new PubnubError     (
        PNERR_READINPUT ,
        "Unable to read Input Stream. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_BAD_REQUEST                       =
        new PubnubError     (
        PNERR_BAD_REQUEST ,
        "Bad request. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_HTTP_ERROR                        =
        new PubnubError     (
        PNERR_HTTP_ERROR ,
        "HTTP Error. " +
        "Please check network connectivity. " +
        "Please contact support with error details if issue persists."
    );

    static final PubnubError        PNERROBJ_BAD_GATEWAY                       =
        new PubnubError     (
        PNERR_BAD_GATEWAY ,
        "Bad Gateway. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_CLIENT_TIMEOUT                    =
        new PubnubError     (
        PNERR_CLIENT_TIMEOUT ,
        "Client Timeout"
    );

    static final PubnubError        PNERROBJ_GATEWAY_TIMEOUT                   =
        new PubnubError     (
        PNERR_GATEWAY_TIMEOUT ,
        "Gateway Timeout"
    );

    static final PubnubError        PNERROBJ_5023_INTERNAL_ERROR               =
        new PubnubError     (
        PNERR_INTERNAL_ERROR ,
        "Internal Server Error. " +
        "Please contact support with error details."
    );

    static final PubnubError        PNERROBJ_PARSING_ERROR                     =
        new PubnubError     (
        PNERR_PARSING_ERROR ,
        "Parsing Error"
    );

    static final PubnubError        PNERROBJ_PUBNUB_EXCEPTION                  =
        new PubnubError     (
        PNERR_PUBNUB_EXCEPTION ,
        "Pubnub Exception"
    );

    static final PubnubError        PNERROBJ_DISCONNECT                        =
        new PubnubError     (
        PNERR_DISCONNECT ,
        "Disconnect"
    );

    static final PubnubError        PNERROBJ_DISCONN_AND_RESUB                 =
        new PubnubError     (
        PNERR_DISCONN_AND_RESUB ,
        "Disconnect and Resubscribe"
    );

    static final PubnubError        PNERROBJ_FORBIDDEN                         =
        new PubnubError     (
        PNERR_FORBIDDEN ,
        "Authentication Failure. " +
        "Incorrect Authentication Key"
    );

    static final PubnubError PNERROBJ_UNAUTHORIZED                             =
        new PubnubError     (
        PNERR_UNAUTHORIZED ,
        "Authentication Failure. " +
        "Authentication Key is missing"
    );

    static final PubnubError PNERROBJ_SECRET_KEY_MISSING                       =
        new PubnubError     (
        PNERR_SECRET_KEY_MISSING ,
        "ULS configuration failed. Secret Key not configured. "
    );

    static final PubnubError PNERROBJ_ULSSIGN_ERROR                            =
        new PubnubError     (
        PNERR_ULSSIGN_ERROR ,
        "Invalid Signature . " +
        "Please contact support with error details."
    );

    static final PubnubError PNERROBJ_5075_NETWORK_ERROR                       =
        new PubnubError     (
        PNERR_NETWORK_ERROR ,
        "Network Error. " +
        "Please verify if network is reachable."
    );
    static final PubnubError PNERROBJ_NOT_FOUND_ERROR                       =
            new PubnubError     (
            PNERR_NOT_FOUND ,
            "Page Not Found" +
            "Please verify if network is reachable." +
            "Please contact support with error details."
        );

    static final PubnubError PNERROBJ_SUBSCRIBE_TIMEOUT                       =
            new PubnubError     (
            PNERR_HTTP_SUBSCRIBE_TIMEOUT ,
            "Subscribe Timeout."
    );

    static final PubnubError PNERROBJ_INVALID_ARGUMENTS                       =
            new PubnubError     (
            PNERR_INVALID_ARGUMENTS ,
            "INVALID ARGUMENTS."
    );

    static final PubnubError PNERROBJ_CHANNEL_MISSING                      =
            new PubnubError     (
            PNERR_CHANNEL_MISSING ,
            "Channel Missing."
    );

    static final PubnubError PNERROBJ_CONNECTION_NOT_SET                       =
            new PubnubError     (
            PNERR_CONNECTION_NOT_SET ,
            "Pubnub Connection not set"
    );


    public  final int errorCode;
    public  final int errorCodeExtended;
    public  final JSONObject errorObject;
    private final String errorString;
    private String message;

    private PubnubError(int errorCode, int errorCodeExtended, String errorString, JSONObject errorObject, String message) {
        this.errorCodeExtended = errorCodeExtended;
        this.errorCode = errorCode;
        this.errorString = errorString;
        this.errorObject = errorObject;
        this.message = message;
    }

    private PubnubError(int errorCode, int errorCodeExtended, String errorString) {
        this(errorCode, errorCodeExtended, errorString, null, null);
    }
    private PubnubError(int errorCode, int errorCodeExtended, String errorString, JSONObject errorObject) {
        this(errorCode, errorCodeExtended, errorString, errorObject, null);
    }

    private PubnubError(int errorCode, String errorString) {
        this(errorCode, 0, errorString, null, null);
    }
    private PubnubError(int errorCode, int errorCodeExtended, String errorString, String message) {
        this(errorCode, errorCodeExtended, errorString, null, message);
    }
    public PubnubError(PubnubError error, String message) {
        this(error.errorCode, error.errorCodeExtended, error.errorString, null, message);
    }
    public PubnubError(PubnubError error, JSONObject errorObject) {
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

    static PubnubError getErrorObject(PubnubError error, String message) {
        return new PubnubError(error.errorCode, error.errorCodeExtended, error.errorString, message);
    }
    static PubnubError getErrorObject(PubnubError error, String message, JSONObject errorObject) {
        return new PubnubError(error.errorCode, error.errorCodeExtended, error.errorString, errorObject, message);
    }
    static PubnubError getErrorObject(PubnubError error, int errorCodeExtended, JSONObject errorObject) {
        return new PubnubError(error.errorCode, errorCodeExtended, error.errorString, errorObject);
    }
    static PubnubError getErrorObject(PubnubError error, int errorCodeExtended) {
        return new PubnubError(error.errorCode, errorCodeExtended, error.errorString);
    }
    static PubnubError getErrorObject(PubnubError error, int errorCodeExtended, String message) {
        return new PubnubError(error.errorCode, errorCodeExtended, error.errorString, message);
    }
    public String getErrorString() {
        return errorString;
    }
}
