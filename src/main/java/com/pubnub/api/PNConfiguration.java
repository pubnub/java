package com.pubnub.api;


import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNLogVerbosity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class PNConfiguration {

    /**
     * By default, the origin is pointing directly to PubNub servers. If a proxy origin is neeeded, set a custom
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
     *  Reference on number of seconds which is used by client during non-subscription operations to
     check whether response potentially failed with 'timeout' or not.
     */
    private int nonSubscribeRequestTimeout;

    /**
     * verbosity of heartbeat configuration, by default only alerts on failed heartbeats
     */
    private PNHeartbeatNotificationOptions heartbeatNotificationOptions;

    /**
     * filterExpression used as part of PSV2 specification.
     */
    @Setter private String filterExpression;

    /**
     * Initialize the PNConfiguration with default values
     */
    public PNConfiguration() {
        setPresenceTimeout(300);

        uuid = UUID.randomUUID().toString();

        nonSubscribeRequestTimeout = 10;
        subscribeTimeout = 310;
        connectTimeout = 5;

        logVerbosity = PNLogVerbosity.NONE;

        heartbeatNotificationOptions = PNHeartbeatNotificationOptions.Failures;
    }

    /**
     *  set presence configurations for timeout and announce interval.
     * @param timeout presence timeout; how long before the server considers this client to be gone.
     * @param interval presence announce interval, how often the client should announce itself.
     * @return returns itself.
     */
    public PNConfiguration setPresenceTimeoutWithCustomInterval(final int timeout, final int interval) {
        this.presenceTimeout = timeout;
        this.heartbeatInterval = interval;

        return this;
    }

    /**
     * set presence configurations for timeout and allow the client to pick the best interval
     * @param timeout presence timeout; how long before the server considers this client to be gone.
     * @return returns itself.
     */
    public PNConfiguration setPresenceTimeout(final int timeout) {
        return setPresenceTimeoutWithCustomInterval(timeout,(timeout / 2) - 1);
    }

}
