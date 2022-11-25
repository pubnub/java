package com.pubnub.api.endpoints.presence;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.presence.PNWhereNowResult;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.server.presence.WhereNowPayload;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class WhereNow extends Endpoint<Envelope<WhereNowPayload>, PNWhereNowResult> {

    @Setter
    private String uuid;

    public WhereNow(PubNub pubnub,
                    TelemetryManager telemetryManager,
                    RetrofitManager retrofit,
                    TokenManager tokenManager) {
        super(pubnub, telemetryManager, retrofit, tokenManager);
    }

    @Override
    protected List<String> getAffectedChannels() {
        return null;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
    }

    @Override
    protected Call<Envelope<WhereNowPayload>> doWork(Map<String, String> params) throws PubNubException {
        return this.getRetrofit().getExtendedPresenceService().whereNow(this.getPubnub().getConfiguration().getSubscribeKey(),
                this.uuid != null ? this.uuid : this.getPubnub().getConfiguration().getUserId().getValue(), params);
    }

    @Override
    protected PNWhereNowResult createResponse(Response<Envelope<WhereNowPayload>> input) throws PubNubException {
        if (input.body() == null || input.body().getPayload() == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR).build();
        }

        PNWhereNowResult pnPresenceWhereNowResult = PNWhereNowResult.builder()
                .channels(input.body().getPayload().getChannels())
                .build();

        return pnPresenceWhereNowResult;
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNWhereNowOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }
}
