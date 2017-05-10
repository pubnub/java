package com.pubnub.api.endpoints.access;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerKeyData;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.server.access_manager.AccessManagerGrantPayload;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@Accessors(chain = true, fluent = true)
public class Grant extends Endpoint<Envelope<AccessManagerGrantPayload>, PNAccessManagerGrantResult> {

    @Setter
    private boolean read;
    @Setter
    private boolean write;
    @Setter
    private boolean manage;
    @Setter
    private Integer ttl;


    @Setter
    private List<String> authKeys;
    @Setter
    private List<String> channels;
    @Setter
    private List<String> channelGroups;

    public Grant(PubNub pubnub, Retrofit retrofit) {
        super(pubnub, retrofit);
        authKeys = new ArrayList<>();
        channels = new ArrayList<>();
        channelGroups = new ArrayList<>();
    }

    @Override
    protected List<String> getAffectedChannels() {
        return channels;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return channelGroups;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSecretKey() == null || this.getPubnub().getConfiguration().getSecretKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SECRET_KEY_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getPublishKey() == null || this.getPubnub().getConfiguration().getPublishKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PUBLISH_KEY_MISSING).build();
        }
        if (authKeys.size() > 0 && channels.size() == 0 && channelGroups.size() == 0) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_AND_GROUP_MISSING).build();
        }
    }

    @Override
    protected Call<Envelope<AccessManagerGrantPayload>> doWork(Map<String, String> queryParams) throws PubNubException {

        if (channels.size() > 0) {
            queryParams.put("channel", PubNubUtil.joinString(channels, ","));
        }

        if (channelGroups.size() > 0) {
            queryParams.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        if (authKeys.size() > 0) {
            queryParams.put("auth", PubNubUtil.joinString(authKeys, ","));
        }

        if (ttl != null && ttl >= -1) {
            queryParams.put("ttl", String.valueOf(ttl));
        }

        queryParams.put("r", (read) ? "1" : "0");
        queryParams.put("w", (write) ? "1" : "0");
        queryParams.put("m", (manage) ? "1" : "0");

        AccessManagerService service = this.getRetrofit().create(AccessManagerService.class);
        return service.grant(this.getPubnub().getConfiguration().getSubscribeKey(), queryParams);
    }

    @Override
    protected PNAccessManagerGrantResult createResponse(Response<Envelope<AccessManagerGrantPayload>> input) throws PubNubException {
        MapperManager mapperManager = getPubnub().getMapper();
        PNAccessManagerGrantResult.PNAccessManagerGrantResultBuilder pnAccessManagerGrantResult = PNAccessManagerGrantResult.builder();

        if (input.body() == null || input.body().getPayload() == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR).build();
        }

        AccessManagerGrantPayload data = input.body().getPayload();
        Map<String, Map<String, PNAccessManagerKeyData>> constructedChannels = new HashMap<>();
        Map<String, Map<String, PNAccessManagerKeyData>> constructedGroups = new HashMap<>();

        // we have a case of a singular channel.
        if (data.getChannel() != null) {
            constructedChannels.put(data.getChannel(), data.getAuthKeys());
        }

        if (channelGroups != null) {
            if (channelGroups.size() == 1) {
                constructedGroups.put(mapperManager.elementToString(data.getChannelGroups()), data.getAuthKeys());
            } else if (channelGroups.size() > 1) {
                for (Iterator<Map.Entry<String, JsonElement>> it = mapperManager.getObjectIterator(data.getChannelGroups()); it.hasNext();) {
                    Map.Entry<String, JsonElement> channelGroup = it.next();
                    constructedGroups.put(channelGroup.getKey(), createKeyMap(channelGroup.getValue()));
                }
            }
        }

        if (data.getChannels() != null) {
            for (String fetchedChannel : data.getChannels().keySet()) {
                constructedChannels.put(fetchedChannel, data.getChannels().get(fetchedChannel).getAuthKeys());
            }
        }

        return pnAccessManagerGrantResult
                .subscribeKey(data.getSubscribeKey())
                .level(data.getLevel())
                .ttl(data.getTtl())
                .channels(constructedChannels)
                .channelGroups(constructedGroups)
                .build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNAccessManagerGrant;
    }

    @Override
    protected boolean isAuthRequired() {
        return false;
    }

    private Map<String, PNAccessManagerKeyData> createKeyMap(JsonElement input) {
        Map<String, PNAccessManagerKeyData> result = new HashMap<>();
        MapperManager mapper = getPubnub().getMapper();

        for (Iterator<Map.Entry<String, JsonElement>> it = mapper.getObjectIterator(input, "auths"); it.hasNext();) {
            Map.Entry<String, JsonElement> keyMap = it.next();
            PNAccessManagerKeyData pnAccessManagerKeyData = new PNAccessManagerKeyData()
                    .setManageEnabled(mapper.getAsBoolean(keyMap.getValue(), "m"))
                    .setWriteEnabled(mapper.getAsBoolean(keyMap.getValue(), "w"))
                    .setReadEnabled(mapper.getAsBoolean(keyMap.getValue(), "r"));

            result.put(keyMap.getKey(), pnAccessManagerKeyData);
        }

        return result;
    }

}
