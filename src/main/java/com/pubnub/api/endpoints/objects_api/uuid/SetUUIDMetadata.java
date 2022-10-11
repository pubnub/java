package com.pubnub.api.endpoints.objects_api.uuid;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.UUIDEndpoint;
import com.pubnub.api.endpoints.objects_api.utils.Include.CustomIncludeAware;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingCustomInclude;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.uuid.PNSetUUIDMetadataResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata;
import com.pubnub.api.models.server.objects_api.SetUUIDMetadataPayload;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import retrofit2.Call;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public abstract class SetUUIDMetadata extends UUIDEndpoint<SetUUIDMetadata, EntityEnvelope<PNUUIDMetadata>, PNSetUUIDMetadataResult>
        implements CustomIncludeAware<SetUUIDMetadata> {
    SetUUIDMetadata(
            final PubNub pubnubInstance,
            final TelemetryManager telemetry,
            final RetrofitManager retrofitInstance,
            final CompositeParameterEnricher compositeParameterEnricher,
            final TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
    }

    public static SetUUIDMetadata create(final PubNub pubNub,
                                         final TelemetryManager telemetryManager,
                                         final RetrofitManager retrofitManager,
                                         final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault();
        return new SetUUIDMetadataCommand(pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
    }

    public abstract SetUUIDMetadata name(String name);
    public abstract SetUUIDMetadata email(String email);
    public abstract SetUUIDMetadata profileUrl(String profileUrl);
    public abstract SetUUIDMetadata externalId(String externalId);
    public abstract SetUUIDMetadata custom(Map<String, Object> custom);
    public abstract SetUUIDMetadata status(String name);
    public abstract SetUUIDMetadata type(String name);
}

final class SetUUIDMetadataCommand extends SetUUIDMetadata implements HavingCustomInclude<SetUUIDMetadata> {
    private String name;
    private String email;
    private String profileUrl;
    private String externalId;
    private Map<String, Object> custom;
    private String status;
    private String type;

    SetUUIDMetadataCommand(final PubNub pubNub,
                           final TelemetryManager telemetryManager,
                           final RetrofitManager retrofitManager,
                           final CompositeParameterEnricher compositeParameterEnricher,
                           final TokenManager tokenManager) {
        super(pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
    }



    @Override
    protected Call<EntityEnvelope<PNUUIDMetadata>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        //This is workaround to accept custom maps that are instances of anonymous classes not handled by gson
        final HashMap<String, Object> customHashMap = new HashMap<String, Object>();
        if (custom != null) {
            customHashMap.putAll(custom);
        }

        final SetUUIDMetadataPayload setUUIDMetadataPayload = new SetUUIDMetadataPayload(name, email, externalId,
                profileUrl, customHashMap, status, type);

        return getRetrofit()
                .getUuidMetadataService()
                .setUUIDsMetadata(getPubnub().getConfiguration().getSubscribeKey(),
                        effectiveUuid(), setUUIDMetadataPayload, effectiveParams);
    }

    @Override
    protected PNSetUUIDMetadataResult createResponse(Response<EntityEnvelope<PNUUIDMetadata>> input)
            throws PubNubException {
        if (input.body() != null) {
            return new PNSetUUIDMetadataResult(input.body());
        } else {
            return new PNSetUUIDMetadataResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSetUuidMetadataOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }

    @Override
    public SetUUIDMetadata name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SetUUIDMetadata email(String email) {
        this.email = email;
        return this;
    }

    @Override
    public SetUUIDMetadata profileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
        return this;
    }

    @Override
    public SetUUIDMetadata externalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    @Override
    public SetUUIDMetadata custom(Map<String, Object> custom) {
        this.custom = custom;
        return this;
    }

    @Override
    public SetUUIDMetadata status(String status) {
       this.status = status;
       return this;
    }

    @Override
    public SetUUIDMetadata type(String type) {
        this.type = type;
        return this;
    }
}
