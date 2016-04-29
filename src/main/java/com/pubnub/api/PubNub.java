package com.pubnub.api;

import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.builder.SubscribeBuilder;
import com.pubnub.api.builder.UnsubscribeBuilder;
import com.pubnub.api.endpoints.History;
import com.pubnub.api.endpoints.Time;
import com.pubnub.api.endpoints.access.Audit;
import com.pubnub.api.endpoints.access.Grant;
import com.pubnub.api.endpoints.channel_groups.*;
import com.pubnub.api.endpoints.presence.GetState;
import com.pubnub.api.endpoints.presence.HereNow;
import com.pubnub.api.endpoints.presence.SetState;
import com.pubnub.api.endpoints.presence.WhereNow;
import com.pubnub.api.endpoints.pubsub.Publish;
import com.pubnub.api.endpoints.push.*;
import com.pubnub.api.managers.BasePathManager;
import com.pubnub.api.managers.PublishSequenceManager;
import com.pubnub.api.managers.SubscriptionManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;


@Getter
@Slf4j
public class PubNub {

    private PNConfiguration configuration;

    @Getter(AccessLevel.NONE)
    private SubscriptionManager subscriptionManager;
    @Getter(AccessLevel.NONE)
    private BasePathManager basePathManager;
    @Getter(AccessLevel.NONE)
    private PublishSequenceManager publishSequenceManager;

    private String sdkVersion;

    public PubNub(final PNConfiguration initialConfig) {
        this.configuration = initialConfig;
        this.subscriptionManager = new SubscriptionManager(this);
        this.basePathManager = new BasePathManager(initialConfig);
        this.publishSequenceManager = new PublishSequenceManager(65535);

        sdkVersion = fetchSDKVersion();
    }

    public String getBaseUrl() {
        return this.basePathManager.getBasePath();
    }


    //
    public final void addListener(SubscribeCallback listener) {
        subscriptionManager.addListener(listener);
    }

    public final void removeListener(SubscribeCallback listener) {
        subscriptionManager.removeListener(listener);
    }

    public final SubscribeBuilder subscribe() {
        return new SubscribeBuilder(this.subscriptionManager);
    }

    public final UnsubscribeBuilder unsubscribe() { return new UnsubscribeBuilder(this.subscriptionManager); }

    // start push

    public final AddChannelsToPush addPushNotificationsOnChannels() {
        return new AddChannelsToPush(this);
    }

    public final RemoveChannelsFromPush removePushNotificationsFromChannels() {
        return new RemoveChannelsFromPush(this);
    }

    public final RemoveAllPushChannelsForDevice removeAllPushNotificationsFromDeviceWithPushToken() {
        return new RemoveAllPushChannelsForDevice(this);
    }

    public final ListPushProvisions auditPushChannelProvisions() {
        return new ListPushProvisions(this);
    }

    // end push

    public final WhereNow whereNow() {
        return new WhereNow(this);
    }
    public final HereNow hereNow() {
        return new HereNow(this);
    }

    public final Time time() {
        return new Time(this);
    }

    public final History history() { return new History(this); }


    public final Audit audit() {
        return new Audit(this);
    }
    public final Grant grant() {
        return new Grant(this);
    }

    public final GetState getPresenceState() {
        return new GetState(this);
    }
    public final SetState setPresenceState() {
        return new SetState(this, subscriptionManager);
    }

    public final Publish publish() {
        return new Publish(this, publishSequenceManager);
    }

    public final ListAllChannelGroup listAllChannelGroups() {
        return new ListAllChannelGroup(this);
    }

    public final AllChannelsChannelGroup listChannelsForChannelGroup() {
        return new AllChannelsChannelGroup(this);
    }

    public final AddChannelChannelGroup addChannelsToChannelGroup() {
        return new AddChannelChannelGroup(this);
    }

    public final RemoveChannelChannelGroup removeChannelsFromChannelGroup() {
        return new RemoveChannelChannelGroup(this);
    }

    public final DeleteChannelGroup deleteChannelGroup() {
        return new DeleteChannelGroup(this);
    }

    // public methods

    /**
     * Perform Cryptographic decryption of an input string using cipher key provided by PNConfiguration
     * @param inputString String to be encrypted
     * @return String containing the encryption of inputString using cipherKey
     */
    public final String decrypt(String inputString) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return decrypt(inputString, this.getConfiguration().getCipherKey());
    }

    /**
     * Perform Cryptographic decryption of an input string using the cipher key
     * @param inputString String to be encrypted
     * @param cipherKey cipher key to be used for encryption
     * @throws PubNubException throws exception in case of failed encryption
     * @return String containing the encryption of inputString using cipherKey
     */
    public final String decrypt(final String inputString, final String cipherKey) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return new Crypto(cipherKey).decrypt(inputString);
    }

    /**
     * Perform Cryptographic encryption of an input string and the cipher key provided by PNConfiguration
     * @param inputString String to be encrypted
     * @return String containing the encryption of inputString using cipherKey
     */
    public final String encrypt(final String inputString) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return encrypt(inputString, this.getConfiguration().getCipherKey());
    }

    /**
     * Perform Cryptographic encryption of an input string and the cipher key.
     * @param inputString String to be encrypted
     * @param cipherKey cipher key to be used for encryption
     * @throws PubNubException throws exception in case of failed encryption
     * @return String containing the encryption of inputString using cipherKey
     */
    public final String encrypt(final String inputString, final String cipherKey) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return new Crypto(cipherKey).encrypt(inputString);
    }

    public int getTimestamp() {
        return (int) ((new Date().getTime()) / 1000);
    }

    /**
     * @return version of the SDK.
     */
    public String getVersion() {
        return sdkVersion;
    }

    /**
     * Stop the SDK and terminate all listeners.
     */
    public final void stop() {
        subscriptionManager.stop();
    }

    /**
     * Perform a Reconnect to the network
     */
    public final void reconnect() {
        subscriptionManager.reconnect();
    }

    /**
     * fetch the SDK version from the resource files.
     * @return Stringified representation of the SDK version.
     */
    private String fetchSDKVersion() {
        Properties prop = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.properties");
        try {
            prop.load(in);
        } catch (IOException e) {
            return "N/A";
        }

        return prop.getProperty("version");

    }
}
