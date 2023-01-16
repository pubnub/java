package com.pubnub.api.endpoints.files;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.endpoints.BuilderSteps;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNPage;
import com.pubnub.api.models.consumer.files.PNListFilesResult;
import com.pubnub.api.models.server.files.ListFilesResult;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class ListFiles extends Endpoint<ListFilesResult, PNListFilesResult> {

    private static final String LIMIT_QUERY_PARAM = "limit";
    private static final String NEXT_PAGE_QUERY_PARAM = "next";
    private static final String DEFAULT_LIMIT = "100";
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 100;

    private final String channel;
    @Setter
    private Integer limit;
    @Setter
    private PNPage.Next next;

    public ListFiles(String channel,
                     PubNub pubnubInstance,
                     TelemetryManager telemetry,
                     RetrofitManager retrofitInstance, TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, tokenManager);
        this.channel = channel;
    }

    @Override
    protected List<String> getAffectedChannels() {
        return Collections.singletonList(channel);
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null
                || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }

        if (channel == null || channel.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
        }

        if (limit != null && !(MIN_LIMIT <= limit && limit <= MAX_LIMIT)) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS)
                    .errormsg("Limit should be in range from 1 to 100 (both inclusive)")
                    .build();
        }

        if (next != null && (next.getHash() == null || next.getHash().isEmpty())) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS)
                    .errormsg("Next should not be an empty string")
                    .build();
        }
    }

    @Override
    protected Call<ListFilesResult> doWork(Map<String, String> baseParams) throws PubNubException {
        HashMap<String, String> allParams = new HashMap<>(baseParams);

        if (limit != null) {
            allParams.put(LIMIT_QUERY_PARAM, limit.toString());
        } else {
            allParams.put(LIMIT_QUERY_PARAM, DEFAULT_LIMIT);
        }

        if (next != null) {
            allParams.put(NEXT_PAGE_QUERY_PARAM, PubNubUtil.urlEncode(next.getHash()));
        }

        return getRetrofit().getFilesService().listFiles(getPubnub().getConfiguration().getSubscribeKey(),
                channel,
                encodeAuthParamValue(allParams));
    }

    @Override
    protected PNListFilesResult createResponse(Response<ListFilesResult> input) throws PubNubException {
        if (input.body() == null) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_INTERNAL_ERROR)
                    .build();
        }

        return new PNListFilesResult(input.body().getCount(),
                PNPage.next(input.body().getNext()),
                input.body().getStatus(),
                input.body().getData()
        );
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNFileAction;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    public static class Builder implements BuilderSteps.ChannelStep<ListFiles> {

        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final TokenManager tokenManager;

        public Builder(PubNub pubnubInstance,
                       TelemetryManager telemetry,
                       RetrofitManager retrofitInstance,
                       TokenManager tokenManager) {

            this.pubnubInstance = pubnubInstance;
            this.telemetry = telemetry;
            this.retrofitInstance = retrofitInstance;
            this.tokenManager = tokenManager;
        }

        @Override
        public ListFiles channel(String channel) {
            return new ListFiles(channel, pubnubInstance, telemetry, retrofitInstance, tokenManager);
        }
    }

}
