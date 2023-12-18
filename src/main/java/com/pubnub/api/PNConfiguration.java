package com.pubnub.api;


import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNReconnectionPolicy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import okhttp3.Authenticator;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Proxy;
import java.net.ProxySelector;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_UUID_NULL_OR_EMPTY;

@Getter
@Setter
@Accessors(chain = true)

@Log
public class PNConfiguration {
    private static final int DEFAULT_DEDUPE_SIZE = 100;
    private static final int PRESENCE_TIMEOUT = 300;
    private static final int MINIMUM_PRESENCE_TIMEOUT = 20;
    private static final int NON_SUBSCRIBE_REQUEST_TIMEOUT = 10;
    private static final int SUBSCRIBE_TIMEOUT = 310;
    private static final int CONNECT_TIMEOUT = 5;
    private static final int FILE_MESSAGE_PUBLISH_RETRY_LIMIT = 5;
    private static final int MAXIMUM_RECONNECTION_RETRIES_DEFAULT = -1; // infinite

    @Getter
    private SSLSocketFactory sslSocketFactory;

    @Getter
    private X509ExtendedTrustManager x509ExtendedTrustManager;

    @Getter
    private ConnectionSpec connectionSpec;

    @Getter
    private HostnameVerifier hostnameVerifier;

    /**
     * Set to true to send a UUID for PubNub instance
     */
    @Getter
    private boolean includeInstanceIdentifier;

    /**
     * Set to true to send a UUID on each request
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
    private String authKey;


    /**
     * @deprecated Use {@link #cryptoModule} instead.
     */
    @Deprecated
    private String cipherKey;

    /**
     * @deprecated Use {@link #cryptoModule} instead.
     */
    @Deprecated
    private boolean useRandomInitializationVector;

    /**
     * CryptoModule is responsible for handling encryption and decryption.
     * If set, all communications to and from PubNub will be encrypted.
     */
    private CryptoModule cryptoModule;

    public CryptoModule getCryptoModule() {
        if (cryptoModule != null) {
            return cryptoModule;
        } else {
            if (cipherKey != null && !cipherKey.isEmpty()) {
                log.warning("cipherKey is deprecated. Use CryptoModule instead");
                return CryptoModule.createLegacyCryptoModule(cipherKey, useRandomInitializationVector);
            } else {
                return null;
            }
        }
    }

    /**
     * @deprecated Use {@link #getUserId()} instead.
     */
    private volatile String uuid;

    /**
     * @deprecated Use {@link #setUserId(UserId)} instead.
     */
    public void setUuid(@NotNull String uuid) {
        PubNubUtil.require(!uuid.trim().isEmpty(), PNERROBJ_UUID_NULL_OR_EMPTY);
        this.uuid = uuid;
    }

    public UserId getUserId() {
        return new UserId(this.uuid);
    }

    public void setUserId(@NotNull UserId userId) {
        this.uuid = userId.getValue();
    }

    /**
     * If proxies are forcefully caching requests, set to true to allow the client to randomize the subdomain.
     * This configuration is not supported if custom origin is enabled.
     */
    @Deprecated
    private boolean cacheBusting;

    /**
     * toggle to enable verbose logging.
     */

    @NotNull
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
     * Suppress leave events when a channel gets disconnected
     */
    private boolean suppressLeaveEvents;

    /**
     * verbosity of heartbeat configuration, by default only alerts on failed heartbeats
     */
    @Nullable
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
    @Nullable
    private PNReconnectionPolicy reconnectionPolicy;

    /**
     * Set how many times the reconnection manager will try to connect before giving up.
     * Max allowed value is 10.
     * Default is 10.
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
    private Integer maximumConnections;

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

    @Setter
    private boolean dedupOnSubscribe;
    @Setter
    private Integer maximumMessagesCacheSize;
    @Setter
    private int fileMessagePublishRetryLimit;

    /**
     * Enables explicit presence control.
     * When set to true heartbeat calls will contain only channels and groups added explicitly
     * using {@link PubNub#presence()}. Should be used only with ACL set on the server side.
     * For more information please contact PubNub support
     *
     * @see PubNub#presence()
     * @see PNConfiguration#heartbeatInterval
     */
    @Deprecated
    @Setter
    private boolean managePresenceListManually;

    /**
     * Initialize the PNConfiguration with default values
     *
     * @param userId
     */
    public PNConfiguration(@NotNull UserId userId) throws PubNubException {
        setPresenceTimeoutWithCustomInterval(PRESENCE_TIMEOUT, 0);

        this.uuid = userId.getValue();
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

        maximumReconnectionRetries = MAXIMUM_RECONNECTION_RETRIES_DEFAULT;

        dedupOnSubscribe = false;
        suppressLeaveEvents = false;
        maximumMessagesCacheSize = DEFAULT_DEDUPE_SIZE;
        useRandomInitializationVector = true;
        fileMessagePublishRetryLimit = FILE_MESSAGE_PUBLISH_RETRY_LIMIT;
        managePresenceListManually = false;
    }

    /**
     * Initialize the PNConfiguration with default values
     *
     * @param uuid
     * @deprecated Use {@link PNConfiguration(UserId)} instead.
     */
    @Deprecated
    public PNConfiguration(@NotNull String uuid) throws PubNubException {
        this(new UserId(uuid));
    }

    /**
     * set presence configurations for timeout and announce interval.
     *
     * @param timeout  presence timeout; how long before the server considers this client to be gone.
     * @param interval presence announce interval, how often the client should announce itself.
     * @return returns itself.
     */
    public PNConfiguration setPresenceTimeoutWithCustomInterval(int timeout, int interval) {
        timeout = validatePresenceTimeout(timeout);
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
        timeout = validatePresenceTimeout(timeout);
        return setPresenceTimeoutWithCustomInterval(timeout, (timeout / 2) - 1);
    }

    private int validatePresenceTimeout(int timeout) {
        int validTimeout = timeout;
        if (timeout < MINIMUM_PRESENCE_TIMEOUT) {
            validTimeout = MINIMUM_PRESENCE_TIMEOUT;
            log.warning("Presence timeout is too low. Defaulting to: " + MINIMUM_PRESENCE_TIMEOUT);
        }
        return validTimeout;
    }

}
