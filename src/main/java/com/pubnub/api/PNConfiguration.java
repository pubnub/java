package com.pubnub.api;


import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNReconnectionPolicy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.Authenticator;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)

public class PNConfiguration {
    private static final int PRESENCE_TIMEOUT = 300;
    private static final int NON_SUBSCRIBE_REQUEST_TIMEOUT = 10;
    private static final int SUBSCRIBE_TIMEOUT = 310;
    private static final int CONNECT_TIMEOUT = 5;

    @Getter
    private SSLSocketFactory sslSocketFactory;
    @Getter
    private X509ExtendedTrustManager x509ExtendedTrustManager;
    @Getter
    private ConnectionSpec connectionSpec;

    @Getter
    private HostnameVerifier hostnameVerifier;

    /**
     *  Set to true to send a UUID for PubNub instance
     */
    @Getter
    private boolean includeInstanceIdentifier;

    /**
     *  Set to true to send a UUID on each request
     */
    @Getter
    private boolean includeRequestIdentifier;

    /**
     * By default, the origin is pointing directly to PubNub servers. If a proxy origin is needed, set a custom
     * origin using this parameter.
     */
    private String origin;
    private int subscribeTimeout;

    /**
     * In seconds, how long the server will consider this client to be online before issuing a leave event.
     */
    @Setter(AccessLevel.NONE)
    private int presenceTimeout;
    /**
     * In seconds, How often the client should announce it's existence via heartbeating.
     */
    @Setter(AccessLevel.NONE)
    private int heartbeatInterval;

    /**
     * set to true to switch the client to HTTPS:// based communications.
     */
    private boolean secure;
    /**
     * Subscribe Key provided by PubNub
     */
    private String subscribeKey;
    /**
     * Publish Key provided by PubNub.
     */
    private String publishKey;
    private String secretKey;
    private String cipherKey;
    private String authKey;
    private String uuid;
    /**
     * If proxies are forcefully caching requests, set to true to allow the client to randomize the subdomain.
     * This configuration is not supported if custom origin is enabled.
     */
    @Deprecated
    private boolean cacheBusting;

    /**
     * toggle to enable verbose logging.
     */

    private PNLogVerbosity logVerbosity;

    /**
     * Stores the maximum number of seconds which the client should wait for connection before timing out.
     */
    private int connectTimeout;

    /**
     * Reference on number of seconds which is used by client during non-subscription operations to
     * check whether response potentially failed with 'timeout' or not.
     */
    private int nonSubscribeRequestTimeout;

    /**
     * verbosity of heartbeat configuration, by default only alerts on failed heartbeats
     */
    private PNHeartbeatNotificationOptions heartbeatNotificationOptions;

    /**
     * filterExpression used as part of PSV2 specification.
     */
    @Setter
    private String filterExpression;

    /**
     * Reconnection policy which will be used if/when networking goes down
     */
    @Setter
    private PNReconnectionPolicy reconnectionPolicy;

    /**
     * Set how many times the reconneciton manager will try to connect before giving app
     */
    @Setter
    private int maximumReconnectionRetries;

    /**
     * Proxy configuration which will be passed to the networking layer.
     */
    @Setter
    private Proxy proxy;
    @Setter
    private ProxySelector proxySelector;
    @Setter
    private Authenticator proxyAuthenticator;

    @Setter
    private CertificatePinner certificatePinner;

    @Setter
    private HttpLoggingInterceptor httpLoggingInterceptor;

    /**
     * if set, the SDK will alert once the number of messages arrived in one call equal to the threshold
     */
    private Integer requestMessageCountThreshold;

    /**
     * Use Google App Engine based networking configuration
     */
    @Setter
    private boolean googleAppEngineNetworking;
    @Setter
    private boolean startSubscriberThread;

    /**
     * Initialize the PNConfiguration with default values
     */
    public PNConfiguration() {
        setPresenceTimeout(PRESENCE_TIMEOUT);

        uuid = "pn-" + UUID.randomUUID().toString();

        nonSubscribeRequestTimeout = NON_SUBSCRIBE_REQUEST_TIMEOUT;
        subscribeTimeout = SUBSCRIBE_TIMEOUT;
        connectTimeout = CONNECT_TIMEOUT;

        logVerbosity = PNLogVerbosity.NONE;

        heartbeatNotificationOptions = PNHeartbeatNotificationOptions.FAILURES;
        reconnectionPolicy = PNReconnectionPolicy.NONE;

        secure = true;

        includeInstanceIdentifier = false;

        includeRequestIdentifier = true;

        startSubscriberThread = true;

        maximumReconnectionRetries = -1;

    }

    /**
     * set presence configurations for timeout and announce interval.
     *
     * @param timeout  presence timeout; how long before the server considers this client to be gone.
     * @param interval presence announce interval, how often the client should announce itself.
     * @return returns itself.
     */
    public PNConfiguration setPresenceTimeoutWithCustomInterval(int timeout, int interval) {
        this.presenceTimeout = timeout;
        this.heartbeatInterval = interval;

        return this;
    }

    /**
     * set presence configurations for timeout and allow the client to pick the best interval
     *
     * @param timeout presence timeout; how long before the server considers this client to be gone.
     * @return returns itself.
     */
    public PNConfiguration setPresenceTimeout(int timeout) {
        return setPresenceTimeoutWithCustomInterval(timeout, (timeout / 2) - 1);
    }

}