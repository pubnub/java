package com.pubnub.api;

import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.builder.SubscribeBuilder;
import com.pubnub.api.builder.UnsubscribeBuilder;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.endpoints.FetchMessages;
import com.pubnub.api.endpoints.History;
import com.pubnub.api.endpoints.Time;
import com.pubnub.api.endpoints.access.Audit;
import com.pubnub.api.endpoints.access.Grant;
import com.pubnub.api.endpoints.channel_groups.AddChannelChannelGroup;
import com.pubnub.api.endpoints.channel_groups.AllChannelsChannelGroup;
import com.pubnub.api.endpoints.channel_groups.DeleteChannelGroup;
import com.pubnub.api.endpoints.channel_groups.ListAllChannelGroup;
import com.pubnub.api.endpoints.channel_groups.RemoveChannelChannelGroup;
import com.pubnub.api.endpoints.presence.GetState;
import com.pubnub.api.endpoints.presence.HereNow;
import com.pubnub.api.endpoints.presence.SetState;
import com.pubnub.api.endpoints.presence.WhereNow;
import com.pubnub.api.endpoints.pubsub.Publish;
import com.pubnub.api.endpoints.push.AddChannelsToPush;
import com.pubnub.api.endpoints.push.ListPushProvisions;
import com.pubnub.api.endpoints.push.RemoveAllPushChannelsForDevice;
import com.pubnub.api.endpoints.push.RemoveChannelsFromPush;
import com.pubnub.api.managers.BasePathManager;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.PublishSequenceManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.SubscriptionManager;
import com.pubnub.api.vendor.Crypto;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.UUID;


public class PubNub {

    @Getter
    private PNConfiguration configuration;

    @Getter
    private MapperManager mapper;

    private String instanceId;

    private SubscriptionManager subscriptionManager;

    private BasePathManager basePathManager;

    private PublishSequenceManager publishSequenceManager;

    private RetrofitManager retrofitManager;

    private static final int TIMESTAMP_DIVIDER = 1000;
    private static final int MAX_SEQUENCE = 65535;

    private static final String SDK_VERSION = "4.6.3";

    public PubNub(PNConfiguration initialConfig) {
        this.configuration = initialConfig;
        this.mapper = new MapperManager();
        this.basePathManager = new BasePathManager(initialConfig);
        this.retrofitManager = new RetrofitManager(this);
        this.subscriptionManager = new SubscriptionManager(this, retrofitManager);
        this.publishSequenceManager = new PublishSequenceManager(MAX_SEQUENCE);
        instanceId = UUID.randomUUID().toString();
    }

    public String getBaseUrl() {
        return this.basePathManager.getBasePath();
    }


    //
    public void addListener(SubscribeCallback listener) {
        subscriptionManager.addListener(listener);
    }

    public void removeListener(SubscribeCallback listener) {
        subscriptionManager.removeListener(listener);
    }

    public SubscribeBuilder subscribe() {
        return new SubscribeBuilder(this.subscriptionManager);
    }

    public UnsubscribeBuilder unsubscribe() {
        return new UnsubscribeBuilder(this.subscriptionManager);
    }

    // start push

    public AddChannelsToPush addPushNotificationsOnChannels() {
        return new AddChannelsToPush(this, this.retrofitManager.getTransactionInstance());
    }

    public RemoveChannelsFromPush removePushNotificationsFromChannels() {
        return new RemoveChannelsFromPush(this, this.retrofitManager.getTransactionInstance());
    }

    public RemoveAllPushChannelsForDevice removeAllPushNotificationsFromDeviceWithPushToken() {
        return new RemoveAllPushChannelsForDevice(this, this.retrofitManager.getTransactionInstance());
    }

    public ListPushProvisions auditPushChannelProvisions() {
        return new ListPushProvisions(this, this.retrofitManager.getTransactionInstance());
    }

    // end push

    public WhereNow whereNow() {
        return new WhereNow(this, this.retrofitManager.getTransactionInstance());
    }

    public HereNow hereNow() {
        return new HereNow(this, this.retrofitManager.getTransactionInstance());
    }

    public Time time() {
        return new Time(this, this.retrofitManager.getTransactionInstance());
    }

    public History history() {
        return new History(this, this.retrofitManager.getTransactionInstance());
    }

    public FetchMessages fetchMessages() {
        return new FetchMessages(this, this.retrofitManager.getTransactionInstance());
    }

    public Audit audit() {
        return new Audit(this, this.retrofitManager.getTransactionInstance());
    }

    public Grant grant() {
        return new Grant(this, this.retrofitManager.getTransactionInstance());
    }

    public GetState getPresenceState() {
        return new GetState(this, this.retrofitManager.getTransactionInstance());
    }

    public SetState setPresenceState() {
        return new SetState(this, subscriptionManager, this.retrofitManager.getTransactionInstance());
    }

    public Publish publish() {
        return new Publish(this, publishSequenceManager, this.retrofitManager.getTransactionInstance());
    }

    public ListAllChannelGroup listAllChannelGroups() {
        return new ListAllChannelGroup(this, this.retrofitManager.getTransactionInstance());
    }

    public AllChannelsChannelGroup listChannelsForChannelGroup() {
        return new AllChannelsChannelGroup(this, this.retrofitManager.getTransactionInstance());
    }

    public AddChannelChannelGroup addChannelsToChannelGroup() {
        return new AddChannelChannelGroup(this, this.retrofitManager.getTransactionInstance());
    }

    public RemoveChannelChannelGroup removeChannelsFromChannelGroup() {
        return new RemoveChannelChannelGroup(this, this.retrofitManager.getTransactionInstance());
    }

    public DeleteChannelGroup deleteChannelGroup() {
        return new DeleteChannelGroup(this, this.retrofitManager.getTransactionInstance());
    }

    // public methods

    /**
     * Perform Cryptographic decryption of an input string using cipher key provided by PNConfiguration
     *
     * @param inputString String to be encrypted
     * @return String containing the encryption of inputString using cipherKey
     */
    public String decrypt(String inputString) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return decrypt(inputString, this.getConfiguration().getCipherKey());
    }

    /**
     * Perform Cryptographic decryption of an input string using the cipher key
     *
     * @param inputString String to be encrypted
     * @param cipherKey   cipher key to be used for encryption
     * @return String containing the encryption of inputString using cipherKey
     * @throws PubNubException throws exception in case of failed encryption
     */
    public String decrypt(String inputString, String cipherKey) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return new Crypto(cipherKey).decrypt(inputString);
    }

    /**
     * Perform Cryptographic encryption of an input string and the cipher key provided by PNConfiguration
     *
     * @param inputString String to be encrypted
     * @return String containing the encryption of inputString using cipherKey
     */
    public  String encrypt(String inputString) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return encrypt(inputString, this.getConfiguration().getCipherKey());
    }

    /**
     * Perform Cryptographic encryption of an input string and the cipher key.
     *
     * @param inputString String to be encrypted
     * @param cipherKey   cipher key to be used for encryption
     * @return String containing the encryption of inputString using cipherKey
     * @throws PubNubException throws exception in case of failed encryption
     */
    public String encrypt(String inputString, String cipherKey) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return new Crypto(cipherKey).encrypt(inputString);
    }

    public int getTimestamp() {
        return (int) ((new Date().getTime()) / TIMESTAMP_DIVIDER);
    }

    /**
     * @return instance uuid.
     */
    public String getInstanceId() {
        return  instanceId;
    }

    /**
     * @return request uuid.
     */
    public String getRequestId() {
        return  UUID.randomUUID().toString();
    }

    /**
     * @return version of the SDK.
     */
    public String getVersion() {
        return SDK_VERSION;
    }

    /**
     * Stop the SDK and terminate all listeners.
     */
    @Deprecated
    public void stop() {
        subscriptionManager.stop();
    }

    /**
     *  Destroy the SDK to evict the connection pools.
     */
    public void destroy() {
        try {
            subscriptionManager.destroy();
            retrofitManager.destroy();
        } catch (Exception error) {
            //
        }
    }

    /**
     * Perform a Reconnect to the network
     */
    public void reconnect() {
        subscriptionManager.reconnect();
    }

    /**
     * Perform a disconnect from the listeners
     */
    public void disconnect() {
        subscriptionManager.disconnect();
    }

    public Publish fire() {
        return publish().shouldStore(false).replicate(false);
    }

    public List<String> getSubscribedChannels() {
        return subscriptionManager.getSubscribedChannels();
    }

    public List<String> getSubscribedChannelGroups() {
        return subscriptionManager.getSubscribedChannelGroups();
    }

    public void unsubscribeAll() {
        subscriptionManager.unsubscribeAll();
    }
}
