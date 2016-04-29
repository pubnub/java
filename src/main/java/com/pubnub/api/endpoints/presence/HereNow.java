package com.pubnub.api.endpoints.presence;


import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.endpoints.Endpoint;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class HereNow extends Endpoint<Envelope<Object>, PNHereNowResult> {
    @Setter private List<String> channels;
    @Setter private List<String> channelGroups;
    @Setter private Boolean includeState;
    @Setter private Boolean includeUUIDs;

    public HereNow(PubNub pubnub) {
        super(pubnub);
        channels = new ArrayList<>();
        channelGroups = new ArrayList<>();
    }


    @Override
    protected boolean validateParams() {
        return true;
    }

    @Override
    protected Call<Envelope<Object>> doWork(Map<String, String> params) {

        if (includeState == null) {
            includeState = false;
        }

        if (includeUUIDs == null) {
            includeUUIDs = true;
        }

        String channelCSV;

        PresenceService service = this.createRetrofit().create(PresenceService.class);

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
            return service.hereNow(pubnub.getConfiguration().getSubscribeKey(), channelCSV, params);
        } else {
            return service.globalHereNow(pubnub.getConfiguration().getSubscribeKey(), params);
        }
    }

    @Override
    protected PNHereNowResult createResponse(Response<Envelope<Object>> input) {
        PNHereNowResult herenowData;

        if (channels.size() > 1 || channelGroups.size() > 0) {
            herenowData = parseMultipleChannelResponse(input.body().getPayload());
        } else {
            herenowData = parseSingleChannelResponse(input.body());
        }

        return herenowData;
    }

    private PNHereNowResult parseSingleChannelResponse(Envelope<Object> input) {
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

    private PNHereNowResult parseMultipleChannelResponse(Object input) {
        Map<String, Object> parsedInput = (Map<String, Object>) input;

        PNHereNowResult hereNowData =  PNHereNowResult.builder()
            .channels(new HashMap<String, PNHereNowChannelData>())
            .totalChannels((Integer) parsedInput.get("total_channels"))
            .totalOccupancy((Integer) parsedInput.get("total_occupancy"))
            .build();

        Map<String, Object> channels = (HashMap<String, Object>) parsedInput.get("channels");

        for (String channelName: channels.keySet()) {
            Map<String, Object> channel = (Map<String, Object>) channels.get(channelName);

            PNHereNowChannelData.PNHereNowChannelDataBuilder hereNowChannelData = PNHereNowChannelData.builder()
                .channelName(channelName)
                .occupancy((Integer) channel.get("occupancy"));

            if (includeUUIDs) {
                hereNowChannelData.occupants(prepareOccupantData(channel.get("uuids")));
            } else {
                hereNowChannelData.occupants(null);
            }

            hereNowData.getChannels().put(channelName, hereNowChannelData.build());
        }

        return hereNowData;
    }

    private List<PNHereNowOccupantData> prepareOccupantData(Object input) {
        List<PNHereNowOccupantData> occupantsResults = new ArrayList<PNHereNowOccupantData>();

        if (includeState != null && includeState) {
            for (Map<String, Object> occupant : (List<Map<String, Object>>) input) {
                PNHereNowOccupantData.PNHereNowOccupantDataBuilder hereNowOccupantData = PNHereNowOccupantData.builder();
                hereNowOccupantData.uuid((String) occupant.get("uuid"));
                hereNowOccupantData.state(occupant.get("state"));

                occupantsResults.add(hereNowOccupantData.build());
            }
        } else {
            for (String occupant : (List<String>) input) {
                PNHereNowOccupantData.PNHereNowOccupantDataBuilder hereNowOccupantData = PNHereNowOccupantData.builder();
                hereNowOccupantData.uuid(occupant);
                hereNowOccupantData.state(null);

                occupantsResults.add(hereNowOccupantData.build());
            }
        }
        return occupantsResults;
    }

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNHereNowOperation;
    }

}
