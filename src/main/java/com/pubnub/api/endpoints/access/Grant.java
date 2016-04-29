package com.pubnub.api.endpoints.access;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.server.access_manager.AccessManagerGrantPayload;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerKeyData;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerKeysData;
import com.pubnub.api.endpoints.Endpoint;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Accessors(chain = true, fluent = true)
public class Grant extends Endpoint<Envelope<AccessManagerGrantPayload>, PNAccessManagerGrantResult> {

    @Setter private boolean read;
    @Setter private boolean write;
    @Setter private boolean manage;
    @Setter private Integer ttl;


    @Setter private List<String> authKeys;
    @Setter private List<String> channels;
    @Setter private List<String> channelGroups;

    public Grant(PubNub pubnub) {
        super(pubnub);
    }

    @Override
    protected boolean validateParams() {
        return true;
    }

    @Override
    protected Call<Envelope<AccessManagerGrantPayload>> doWork(Map<String, String> queryParams) throws PubNubException {
        String signature;

        String signInput = this.pubnub.getConfiguration().getSubscribeKey() + "\n"
                + this.pubnub.getConfiguration().getPublishKey() + "\n"
                + "grant" + "\n";

        queryParams.put("timestamp", String.valueOf(pubnub.getTimestamp()));

        if (channels != null && channels.size() > 0) {
            queryParams.put("channel", PubNubUtil.joinString(channels, ","));
        }

        if (channelGroups != null && channelGroups.size() > 0) {
            queryParams.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        if (authKeys != null & authKeys.size() > 0) {
            queryParams.put("auth", PubNubUtil.joinString(authKeys, ","));
        }

        if (ttl != null && ttl >= -1) {
            queryParams.put("ttl", String.valueOf(ttl));
        }

        queryParams.put("r", (read) ? "1" : "0");
        queryParams.put("w", (write) ? "1" : "0");
        queryParams.put("m", (manage) ? "1" : "0");

        signInput += PubNubUtil.preparePamArguments(queryParams);

        signature = PubNubUtil.signSHA256(this.pubnub.getConfiguration().getSecretKey(), signInput);

        queryParams.put("signature", signature);

        AccessManagerService service = this.createRetrofit().create(AccessManagerService.class);
        return service.grant(pubnub.getConfiguration().getSubscribeKey(), queryParams);
    }

    @Override
    protected PNAccessManagerGrantResult createResponse(Response<Envelope<AccessManagerGrantPayload>> input) throws PubNubException {
        ObjectMapper mapper = new ObjectMapper();
        PNAccessManagerGrantResult.PNAccessManagerGrantResultBuilder pnAccessManagerGrantResult = PNAccessManagerGrantResult.builder();

        if (input.body() == null || input.body().getPayload() == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).build();
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
                constructedGroups.put(data.getChannelGroups().asText(), data.getAuthKeys());
            } else if (channelGroups.size() > 1) {
                try {
                    HashMap<String, PNAccessManagerKeysData> channelGroupKeySet = mapper.readValue(data.getChannelGroups().toString(),
                            new TypeReference<HashMap<String, PNAccessManagerKeysData>>() {});

                    for (String fetchedChannelGroup : channelGroupKeySet.keySet()) {
                        constructedGroups.put(fetchedChannelGroup, channelGroupKeySet.get(fetchedChannelGroup).getAuthKeys());
                    }

                } catch (IOException e) {
                    throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).errormsg(e.getMessage()).build();
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

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNAccessManagerGrant;
    }

}
