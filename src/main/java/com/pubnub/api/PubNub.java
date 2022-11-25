package com.pubnub.api;

import com.pubnub.api.builder.PresenceBuilder;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.builder.SubscribeBuilder;
import com.pubnub.api.builder.UnsubscribeBuilder;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.endpoints.DeleteMessages;
import com.pubnub.api.endpoints.FetchMessages;
import com.pubnub.api.endpoints.History;
import com.pubnub.api.endpoints.MessageCounts;
import com.pubnub.api.endpoints.Time;
import com.pubnub.api.endpoints.access.Grant;
import com.pubnub.api.endpoints.access.GrantToken;
import com.pubnub.api.endpoints.access.RevokeToken;
import com.pubnub.api.endpoints.access.builder.GrantTokenBuilder;
import com.pubnub.api.endpoints.channel_groups.AddChannelChannelGroup;
import com.pubnub.api.endpoints.channel_groups.AllChannelsChannelGroup;
import com.pubnub.api.endpoints.channel_groups.DeleteChannelGroup;
import com.pubnub.api.endpoints.channel_groups.ListAllChannelGroup;
import com.pubnub.api.endpoints.channel_groups.RemoveChannelChannelGroup;
import com.pubnub.api.endpoints.files.DeleteFile;
import com.pubnub.api.endpoints.files.DownloadFile;
import com.pubnub.api.endpoints.files.GetFileUrl;
import com.pubnub.api.endpoints.files.ListFiles;
import com.pubnub.api.endpoints.files.PublishFileMessage;
import com.pubnub.api.endpoints.files.SendFile;
import com.pubnub.api.endpoints.message_actions.AddMessageAction;
import com.pubnub.api.endpoints.message_actions.GetMessageActions;
import com.pubnub.api.endpoints.message_actions.RemoveMessageAction;
import com.pubnub.api.endpoints.objects_api.channel.GetAllChannelsMetadata;
import com.pubnub.api.endpoints.objects_api.channel.GetChannelMetadata;
import com.pubnub.api.endpoints.objects_api.channel.RemoveChannelMetadata;
import com.pubnub.api.endpoints.objects_api.channel.SetChannelMetadata;
import com.pubnub.api.endpoints.objects_api.members.GetChannelMembers;
import com.pubnub.api.endpoints.objects_api.members.ManageChannelMembers;
import com.pubnub.api.endpoints.objects_api.members.RemoveChannelMembers;
import com.pubnub.api.endpoints.objects_api.members.SetChannelMembers;
import com.pubnub.api.endpoints.objects_api.memberships.GetMemberships;
import com.pubnub.api.endpoints.objects_api.memberships.ManageMemberships;
import com.pubnub.api.endpoints.objects_api.memberships.RemoveMemberships;
import com.pubnub.api.endpoints.objects_api.memberships.SetMemberships;
import com.pubnub.api.endpoints.objects_api.uuid.GetAllUUIDMetadata;
import com.pubnub.api.endpoints.objects_api.uuid.GetUUIDMetadata;
import com.pubnub.api.endpoints.objects_api.uuid.RemoveUUIDMetadata;
import com.pubnub.api.endpoints.objects_api.uuid.SetUUIDMetadata;
import com.pubnub.api.endpoints.presence.GetState;
import com.pubnub.api.endpoints.presence.HereNow;
import com.pubnub.api.endpoints.presence.SetState;
import com.pubnub.api.endpoints.presence.WhereNow;
import com.pubnub.api.endpoints.pubsub.Publish;
import com.pubnub.api.endpoints.pubsub.Signal;
import com.pubnub.api.endpoints.push.AddChannelsToPush;
import com.pubnub.api.endpoints.push.ListPushProvisions;
import com.pubnub.api.endpoints.push.RemoveAllPushChannelsForDevice;
import com.pubnub.api.endpoints.push.RemoveChannelsFromPush;
import com.pubnub.api.managers.BasePathManager;
import com.pubnub.api.managers.DelayedReconnectionManager;
import com.pubnub.api.managers.DuplicationManager;
import com.pubnub.api.managers.ListenerManager;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.PublishSequenceManager;
import com.pubnub.api.managers.ReconnectionManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.StateManager;
import com.pubnub.api.managers.SubscriptionManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.managers.token_manager.TokenParser;
import com.pubnub.api.models.consumer.access_manager.v3.PNToken;
import com.pubnub.api.vendor.Crypto;
import com.pubnub.api.vendor.FileEncryptionUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class PubNub {

    @Getter
    private @NotNull PNConfiguration configuration;

    @Getter
    private @NotNull MapperManager mapper;

    private String instanceId;

    private SubscriptionManager subscriptionManager;

    private BasePathManager basePathManager;

    private PublishSequenceManager publishSequenceManager;

    private TelemetryManager telemetryManager;

    private RetrofitManager retrofitManager;

    private final TokenParser tokenParser;

    private static final int TIMESTAMP_DIVIDER = 1000;
    private static final int MAX_SEQUENCE = 65535;

    private static final String SDK_VERSION = "6.3.0";
    private final ListenerManager listenerManager;
    private final StateManager stateManager;

    private final TokenManager tokenManager;

    public PubNub(@NotNull PNConfiguration initialConfig) {
        this.configuration = initialConfig;
        this.mapper = new MapperManager();
        this.telemetryManager = new TelemetryManager();
        this.basePathManager = new BasePathManager(initialConfig);
        this.listenerManager = new ListenerManager(this);
        this.retrofitManager = new RetrofitManager(this);
        this.stateManager = new StateManager(this.configuration);
        this.tokenManager = new TokenManager();
        final ReconnectionManager reconnectionManager = new ReconnectionManager(this);
        final DelayedReconnectionManager delayedReconnectionManager = new DelayedReconnectionManager(this);
        final DuplicationManager duplicationManager = new DuplicationManager(this.configuration);
        this.subscriptionManager = new SubscriptionManager(this,
                retrofitManager,
                this.telemetryManager,
                stateManager,
                listenerManager,
                reconnectionManager,
                delayedReconnectionManager,
                duplicationManager,
                tokenManager);
        this.publishSequenceManager = new PublishSequenceManager(MAX_SEQUENCE);
        this.tokenParser = new TokenParser();
        instanceId = UUID.randomUUID().toString();
    }

    /**
     * @deprecated
     */
    @NotNull
    public static String generateUUID() {
        return "pn-" + UUID.randomUUID();
    }

    @NotNull
    public String getBaseUrl() {
        return this.basePathManager.getBasePath();
    }


    public void addListener(@NotNull SubscribeCallback listener) {
        listenerManager.addListener(listener);
    }

    public void removeListener(@NotNull SubscribeCallback listener) {
        listenerManager.removeListener(listener);
    }

    @NotNull
    public SubscribeBuilder subscribe() {
        return new SubscribeBuilder(this.subscriptionManager);
    }

    @NotNull
    public UnsubscribeBuilder unsubscribe() {
        return new UnsubscribeBuilder(this.subscriptionManager);
    }

    @NotNull
    public PresenceBuilder presence() {
        return new PresenceBuilder(this.subscriptionManager);
    }

    // start push

    @NotNull
    public AddChannelsToPush addPushNotificationsOnChannels() {
        return new AddChannelsToPush(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public RemoveChannelsFromPush removePushNotificationsFromChannels() {
        return new RemoveChannelsFromPush(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public RemoveAllPushChannelsForDevice removeAllPushNotificationsFromDeviceWithPushToken() {
        return new RemoveAllPushChannelsForDevice(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public ListPushProvisions auditPushChannelProvisions() {
        return new ListPushProvisions(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    // end push

    @NotNull
    public WhereNow whereNow() {
        return new WhereNow(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public HereNow hereNow() {
        return new HereNow(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public Time time() {
        return new Time(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public History history() {
        return new History(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public FetchMessages fetchMessages() {
        return new FetchMessages(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public DeleteMessages deleteMessages() {
        return new DeleteMessages(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public MessageCounts messageCounts() {
        return new MessageCounts(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public Grant grant() {
        return new Grant(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    /**
     * @deprecated Use {@link #grantToken(Integer)} instead.
     */
    @NotNull
    public GrantTokenBuilder grantToken() {
        return new GrantTokenBuilder(new GrantToken(this, this.telemetryManager, this.retrofitManager, this.tokenManager));
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public GrantTokenBuilder grantToken(Integer ttl) {
        return new GrantTokenBuilder(new GrantToken(this, this.telemetryManager, this.retrofitManager, this.tokenManager)).ttl(ttl);
    }

    @NotNull
    public RevokeToken revokeToken() {
        return new RevokeToken(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public GetState getPresenceState() {
        return new GetState(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public SetState setPresenceState() {
        return new SetState(this, subscriptionManager, this.telemetryManager, this.retrofitManager, tokenManager);
    }

    @NotNull
    public Publish publish() {
        return new Publish(this, publishSequenceManager, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public Signal signal() {
        return new Signal(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public ListAllChannelGroup listAllChannelGroups() {
        return new ListAllChannelGroup(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public AllChannelsChannelGroup listChannelsForChannelGroup() {
        return new AllChannelsChannelGroup(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public AddChannelChannelGroup addChannelsToChannelGroup() {
        return new AddChannelChannelGroup(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public RemoveChannelChannelGroup removeChannelsFromChannelGroup() {
        return new RemoveChannelChannelGroup(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public DeleteChannelGroup deleteChannelGroup() {
        return new DeleteChannelGroup(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    // Start Objects API

    public SetUUIDMetadata setUUIDMetadata() {
        return SetUUIDMetadata.create(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public GetAllUUIDMetadata getAllUUIDMetadata() {
        return GetAllUUIDMetadata.create(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public GetUUIDMetadata getUUIDMetadata() {
        return GetUUIDMetadata.create(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public RemoveUUIDMetadata removeUUIDMetadata() {
        return new RemoveUUIDMetadata(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    public SetChannelMetadata.Builder setChannelMetadata() {
        return SetChannelMetadata.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public GetAllChannelsMetadata getAllChannelsMetadata() {
        return GetAllChannelsMetadata.create(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public GetChannelMetadata.Builder getChannelMetadata() {
        return GetChannelMetadata.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    public RemoveChannelMetadata.Builder removeChannelMetadata() {
        return RemoveChannelMetadata.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public GetMemberships getMemberships() {
        return GetMemberships.create(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public SetMemberships.Builder setMemberships() {
        return SetMemberships.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public RemoveMemberships.Builder removeMemberships() {
        return RemoveMemberships.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public ManageMemberships.Builder manageMemberships() {
        return ManageMemberships.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public GetChannelMembers.Builder getChannelMembers() {
        return GetChannelMembers.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public SetChannelMembers.Builder setChannelMembers() {
        return SetChannelMembers.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public RemoveChannelMembers.Builder removeChannelMembers() {
        return RemoveChannelMembers.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public ManageChannelMembers.Builder manageChannelMembers() {
        return ManageChannelMembers.builder(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    // End Objects API

    // Start Message Actions API

    @NotNull
    public AddMessageAction addMessageAction() {
        return new AddMessageAction(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public GetMessageActions getMessageActions() {
        return new GetMessageActions(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    @NotNull
    public RemoveMessageAction removeMessageAction() {
        return new RemoveMessageAction(this, this.telemetryManager, this.retrofitManager, this.tokenManager);
    }

    // End Message Actions API

    @NotNull
    public SendFile.Builder sendFile() {
        return SendFile.builder(this, telemetryManager, retrofitManager, tokenManager);
    }

    public ListFiles.Builder listFiles() {
        return new ListFiles.Builder(this, telemetryManager, retrofitManager, tokenManager);
    }

    public GetFileUrl.Builder getFileUrl() {
        return GetFileUrl.builder(
                this,
                telemetryManager,
                retrofitManager,
                tokenManager);
    }

    public DownloadFile.Builder downloadFile() {
        return DownloadFile.builder(
                this,
                telemetryManager,
                retrofitManager,
                tokenManager);
    }

    public DeleteFile.Builder deleteFile() {
        return DeleteFile.builder(
                this,
                telemetryManager,
                retrofitManager,
                tokenManager);
    }

    public PublishFileMessage.Builder publishFileMessage() {
        return PublishFileMessage.builder(
                this,
                telemetryManager,
                retrofitManager,
                tokenManager);
    }

    // public methods

    /**
     * Perform Cryptographic decryption of an input string using cipher key provided by PNConfiguration
     *
     * @param inputString String to be encrypted
     * @return String containing the encryption of inputString using cipherKey
     */
    @Nullable
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
    @Nullable
    public String decrypt(String inputString, String cipherKey) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }
        boolean dynamicIV = this.getConfiguration().isUseRandomInitializationVector();
        return new Crypto(cipherKey, dynamicIV).decrypt(inputString);
    }

    public InputStream decryptInputStream(InputStream inputStream) throws PubNubException {
        return decryptInputStream(inputStream, this.getConfiguration().getCipherKey());
    }

    public InputStream decryptInputStream(InputStream inputStream, String cipherKey) throws PubNubException {
        return FileEncryptionUtil.decrypt(cipherKey, inputStream);
    }

    /**
     * Perform Cryptographic encryption of an input string and the cipher key provided by PNConfiguration
     *
     * @param inputString String to be encrypted
     * @return String containing the encryption of inputString using cipherKey
     */
    @Nullable
    public String encrypt(String inputString) throws PubNubException {
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
    @Nullable
    public String encrypt(String inputString, String cipherKey) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        boolean dynamicIV = this.getConfiguration().isUseRandomInitializationVector();
        return new Crypto(cipherKey, dynamicIV).encrypt(inputString);
    }

    public InputStream encryptInputStream(InputStream inputStream) throws PubNubException {
        return encryptInputStream(inputStream, this.getConfiguration().getCipherKey());
    }

    public InputStream encryptInputStream(InputStream inputStream, String cipherKey) throws PubNubException {
        return FileEncryptionUtil.encrypt(cipherKey, inputStream);
    }

    public int getTimestamp() {
        return (int) ((new Date().getTime()) / TIMESTAMP_DIVIDER);
    }

    /**
     * @return instance uuid.
     */
    @NotNull
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * @return request uuid.
     */
    @NotNull
    public String getRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * @return version of the SDK.
     */
    @NotNull
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
     * Destroy the SDK to cancel all ongoing requests and stop heartbeat timer.
     */
    public void destroy() {
        try {
            subscriptionManager.destroy(false);
            retrofitManager.destroy(false);
        } catch (Exception error) {
            //
        }
    }

    /**
     * Force destroy the SDK to evict the connection pools and close executors.
     */
    public void forceDestroy() {
        try {
            subscriptionManager.destroy(true);
            retrofitManager.destroy(true);
            telemetryManager.stopCleanUpTimer();
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

    @NotNull
    public Publish fire() {
        return publish().shouldStore(false).replicate(false);
    }

    @NotNull
    public List<String> getSubscribedChannels() {
        return stateManager.subscriptionStateData(false).getChannels();
    }

    @NotNull
    public List<String> getSubscribedChannelGroups() {
        return this.stateManager.subscriptionStateData(false).getChannelGroups();
    }

    public void unsubscribeAll() {
        subscriptionManager.unsubscribeAll();
    }

    public PNToken parseToken(String token) throws PubNubException {
        return tokenParser.unwrapToken(token);
    }

    public void setToken(String token) {
        tokenManager.setToken(token);
    }
}
