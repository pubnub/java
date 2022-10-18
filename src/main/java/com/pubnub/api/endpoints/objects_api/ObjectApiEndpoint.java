package com.pubnub.api.endpoints.objects_api;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import lombok.Getter;
import retrofit2.Call;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pubnub.api.endpoints.objects_api.utils.Include.INCLUDE_PARAM_NAME;
import static lombok.AccessLevel.PROTECTED;

public abstract class ObjectApiEndpoint<INPUT, OUTPUT> extends Endpoint<INPUT, OUTPUT> {

    static final String TYPE_AND_STATUS_INCLUDE_PARAMS = "type,status";
    @Getter(PROTECTED)
    private final CompositeParameterEnricher compositeParameterEnricher;

    protected ObjectApiEndpoint(final PubNub pubnubInstance,
                                final TelemetryManager telemetry,
                                final RetrofitManager retrofitInstance,
                                final CompositeParameterEnricher compositeParameterEnricher,
                                final TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, tokenManager);
        this.compositeParameterEnricher = compositeParameterEnricher;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null
                || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        compositeParameterEnricher.validateParameters();
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
    protected boolean isAuthRequired() {
        return true;
    }

    @Override
    protected Call<INPUT> doWork(Map<String, String> baseParams) throws PubNubException {
        return executeCommand(encodeParams(compositeParameterEnricher.enrichParameters(new HashMap<>(baseParams))));
    }

    protected abstract Call<INPUT> executeCommand(Map<String, String> effectiveParams) throws PubNubException;

    @Override
    protected Map<String, String> createBaseParams() {
        Map<String, String> baseParams = super.createBaseParams();
        baseParams.put(INCLUDE_PARAM_NAME, TYPE_AND_STATUS_INCLUDE_PARAMS);
        return baseParams;
    }
}
