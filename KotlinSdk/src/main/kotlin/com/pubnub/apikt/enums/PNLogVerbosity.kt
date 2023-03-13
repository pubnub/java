package com.pubnub.apikt.enums

enum class PNLogVerbosity {
    /**
     * No logs.
     *
     * @see [okhttp3.logging.HttpLoggingInterceptor.Level.NONE]
     */
    NONE,

    /**
     * Logs request and response lines and their respective headers and bodies (if present).
     *
     * @see [okhttp3.logging.HttpLoggingInterceptor.Level.BODY]
     */
    BODY,
}
