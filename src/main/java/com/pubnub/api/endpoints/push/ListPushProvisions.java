package com.pubnub.api.endpoints.push;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.models.consumer.push.PNPushListProvisionsResult;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class ListPushProvisions extends Endpoint<List<String>, PNPushListProvisionsResult> {

    @Setter private PNPushType pushType;
    @Setter private String deviceId;

    public ListPushProvisions(PubNub pubnub) {
        super(pubnub);
    }

    @Override
    protected boolean validateParams() {
        return true;
    }

    @Override
    protected Call<List<String>> doWork(Map<String, String> params) throws PubNubException {
        params.put("type", pushType.name().toLowerCase());
        PushService service = this.createRetrofit().create(PushService.class);
        return service.listChannelsForDevice(pubnub.getConfiguration().getSubscribeKey(), deviceId, params);
    }

    @Override
    protected PNPushListProvisionsResult createResponse(Response<List<String>> input) throws PubNubException {
        if (input.body() == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).build();
        }

        return PNPushListProvisionsResult.builder().channels(input.body()).build();
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
