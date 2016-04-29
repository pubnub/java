package com.pubnub.api.endpoints.push;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.endpoints.Endpoint;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class AddChannelsToPush extends Endpoint<List<Object>, Boolean> {

    @Setter private PNPushType pushType;
    @Setter private List<String> channels;
    @Setter String deviceId;

    public AddChannelsToPush(PubNub pubnub) {
        super(pubnub);

        channels = new ArrayList<>();
    }

    @Override
    protected boolean validateParams() {
        if (pushType == null) {
            return false;
        }

        if (deviceId == null || deviceId.length() == 0) {
            return false;
        }

        if (channels.size() == 0) {
            return false;
        }

        return true;
    }

    @Override
    protected Call<List<Object>> doWork(Map<String, String> baseParams) throws PubNubException {
        baseParams.put("type", pushType.name().toLowerCase());

        if (channels.size() != 0) {
            baseParams.put("add", PubNubUtil.joinString(channels, ","));
        }

        PushService service = this.createRetrofit().create(PushService.class);
        return service.modifyChannelsForDevice(pubnub.getConfiguration().getSubscribeKey(), deviceId, baseParams);

    }

    @Override
    protected Boolean createResponse(Response<List<Object>> input) throws PubNubException {
        return null;
    }

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNPushNotificationEnabledChannelsOperation;
    }
}
