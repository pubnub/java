package com.pubnub.api.endpoints.presence;


import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.server.Envelope;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;

@Accessors(chain = true, fluent = true)
public class HereNow extends Endpoint<Envelope<JsonElement>, PNHereNowResult> {
    @Setter
    private List<String> channels;
    @Setter
    private List<String> channelGroups;
    @Setter
    private Boolean includeState;
    @Setter
    private Boolean includeUUIDs;

    public HereNow(PubNub pubnubInstance,
                   TelemetryManager telemetryManager,
                   RetrofitManager retrofit,
                   TokenManager tokenManager) {
        super(pubnubInstance, telemetryManager, retrofit, tokenManager);
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
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
    }

    @Override
    protected Call<Envelope<JsonElement>> doWork(Map<String, String> params) {

        if (includeState == null) {
            includeState = false;
        }

        if (includeUUIDs == null) {
            includeUUIDs = true;
        }

        String channelCSV;

        if (includeState) {
            params.put("state", "1");
        }
        if (!includeUUIDs) {
            params.put("disable_uuids", "1");
        }
        if (channelGroups.size() > 0) {
            params.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        if (channels.size() > 0) {
            channelCSV = PubNubUtil.joinString(channels, ",");
        } else {
            channelCSV = ",";
        }

        if (channels.size() > 0 || channelGroups.size() > 0) {
            return this.getRetrofit().getExtendedPresenceService().hereNow(this.getPubnub().getConfiguration().getSubscribeKey(), channelCSV, params);
        } else {
            return this.getRetrofit().getExtendedPresenceService().globalHereNow(this.getPubnub().getConfiguration().getSubscribeKey(), params);
        }
    }

    @Override
    protected PNHereNowResult createResponse(Response<Envelope<JsonElement>> input) {
        PNHereNowResult herenowData;

        if (channels.isEmpty() && channelGroups.isEmpty()) {
            herenowData = parseMultipleChannelResponse(input.body().getPayload());
        } else {
            if (channels.size() > 1 || channelGroups.size() > 0) {
                herenowData = parseMultipleChannelResponse(input.body().getPayload());
            } else {
                herenowData = parseSingleChannelResponse(input.body());
            }
        }

        return herenowData;
    }

    private PNHereNowResult parseSingleChannelResponse(Envelope<JsonElement> input) {
        PNHereNowResult hereNowData = PNHereNowResult.builder()
                .totalChannels(1)
                .channels(new HashMap<String, PNHereNowChannelData>())
                .totalOccupancy(input.getOccupancy())
                .build();

        PNHereNowChannelData.PNHereNowChannelDataBuilder hereNowChannelData = PNHereNowChannelData.builder()
                .channelName(channels.get(0))
                .occupancy(input.getOccupancy());

        if (includeUUIDs) {
            hereNowChannelData.occupants(prepareOccupantData(input.getUuids()));
            hereNowData.getChannels().put(channels.get(0), hereNowChannelData.build());
        }

        return hereNowData;
    }

    private PNHereNowResult parseMultipleChannelResponse(JsonElement input) {
        MapperManager mapper = getPubnub().getMapper();
        PNHereNowResult hereNowData = PNHereNowResult.builder()
                .channels(new HashMap<String, PNHereNowChannelData>())
                .totalChannels(mapper.elementToInt(input, "total_channels"))
                .totalOccupancy(mapper.elementToInt(input, "total_occupancy"))
                .build();

        Iterator<Map.Entry<String, JsonElement>> it = mapper.getObjectIterator(input, "channels");
        while (it.hasNext()) {
            Map.Entry<String, JsonElement> entry = it.next();

            PNHereNowChannelData.PNHereNowChannelDataBuilder hereNowChannelData = PNHereNowChannelData.builder()
                    .channelName(entry.getKey())
                    .occupancy(mapper.elementToInt(entry.getValue(), "occupancy"));

            if (includeUUIDs) {
                hereNowChannelData.occupants(prepareOccupantData(mapper.getField(entry.getValue(), "uuids")));
            } else {
                hereNowChannelData.occupants(null);
            }

            hereNowData.getChannels().put(entry.getKey(), hereNowChannelData.build());
        }

        return hereNowData;
    }

    private List<PNHereNowOccupantData> prepareOccupantData(JsonElement input) {
        List<PNHereNowOccupantData> occupantsResults = new ArrayList<>();
        MapperManager mapper = getPubnub().getMapper();

        if (includeState != null && includeState) {
            Iterator<JsonElement> it = mapper.getArrayIterator(input);
            while (it.hasNext()) {
                JsonElement occupant = it.next();
                PNHereNowOccupantData.PNHereNowOccupantDataBuilder hereNowOccupantData =
                        PNHereNowOccupantData.builder();
                hereNowOccupantData.uuid(mapper.elementToString(occupant, "uuid"));
                hereNowOccupantData.state(mapper.getField(occupant, "state"));

                occupantsResults.add(hereNowOccupantData.build());
            }
        } else {
            Iterator<JsonElement> it = mapper.getArrayIterator(input);
            while (it.hasNext()) {
                JsonElement occupant = it.next();
                PNHereNowOccupantData.PNHereNowOccupantDataBuilder hereNowOccupantData =
                        PNHereNowOccupantData.builder();
                hereNowOccupantData.uuid(mapper.elementToString(occupant));
                hereNowOccupantData.state(null);

                occupantsResults.add(hereNowOccupantData.build());
            }
        }

        return occupantsResults;
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNHereNowOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

}
