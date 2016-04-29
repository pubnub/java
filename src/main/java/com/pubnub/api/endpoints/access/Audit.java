package com.pubnub.api.endpoints.access;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerAuditResult;
import com.pubnub.api.models.server.access_manager.AccessManagerAuditPayload;
import com.pubnub.api.endpoints.Endpoint;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class Audit extends Endpoint<Envelope<AccessManagerAuditPayload>, PNAccessManagerAuditResult> {

    @Setter private List<String> authKeys;
    @Setter private String channel;
    @Setter private String channelGroup;

    public Audit(PubNub pubnub) {
        super(pubnub);
    }

    @Override
    protected boolean validateParams() {
        if (pubnub.getConfiguration().getSecretKey() != null && pubnub.getConfiguration().getSecretKey().length() == 0) {
            return false;
        }

        return true;
    }

    @Override
    protected Call<Envelope<AccessManagerAuditPayload>> doWork(Map<String, String> queryParams) throws PubNubException {
        String signature;

        int timestamp = pubnub.getTimestamp();

        String signInput = this.pubnub.getConfiguration().getSubscribeKey() + "\n"
                + this.pubnub.getConfiguration().getPublishKey() + "\n"
                + "audit" + "\n";

        queryParams.put("timestamp", String.valueOf(timestamp));

        if (channel != null) {
            queryParams.put("channel", channel);
        }

        if (channelGroup != null) {
            queryParams.put("channel-group", channelGroup);
        }

        if (authKeys.size() > 0) {
            queryParams.put("auth", PubNubUtil.joinString(authKeys, ","));
        }

        signInput += PubNubUtil.preparePamArguments(queryParams);

        signature = PubNubUtil.signSHA256(this.pubnub.getConfiguration().getSecretKey(), signInput);

        queryParams.put("signature", signature);

        AccessManagerService service = this.createRetrofit().create(AccessManagerService.class);
        return service.audit(pubnub.getConfiguration().getSubscribeKey(), queryParams);
    }

    @Override
    protected PNAccessManagerAuditResult createResponse(final Response<Envelope<AccessManagerAuditPayload>> input) throws PubNubException {
        PNAccessManagerAuditResult.PNAccessManagerAuditResultBuilder pnAccessManagerAuditResult = PNAccessManagerAuditResult.builder();

        if (input.body() == null || input.body().getPayload() == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).build();
        }

        AccessManagerAuditPayload auditPayload = input.body().getPayload();
        pnAccessManagerAuditResult
                .authKeys(auditPayload.getAuthKeys())
                .channel(auditPayload.getChannel())
                .channelGroup(auditPayload.getChannelGroup())
                .level(auditPayload.getLevel())
                .subscribeKey(auditPayload.getSubscribeKey());


        return pnAccessManagerAuditResult.build();
    }

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNAccessManagerAudit;
    }

}
