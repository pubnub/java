package com.pubnub.api.endpoints;


import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNErrorData;
import com.pubnub.api.models.consumer.PNStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit2.Call;
import retrofit2.Response;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Log
public abstract class Endpoint<Input, Output> implements RemoteAction<Output> {

    @Getter(AccessLevel.PROTECTED)
    private PubNub pubnub;
    @Getter(AccessLevel.PROTECTED)
    private RetrofitManager retrofit;

    @Getter(AccessLevel.NONE)
    private TelemetryManager telemetryManager;

    @Getter(AccessLevel.NONE)
    private PNCallback<Output> cachedCallback;

    @Getter(AccessLevel.NONE)
    private Call<Input> call;

    @Setter(AccessLevel.PUBLIC)
    @Accessors(chain = true, fluent = true)
    private Map<String, String> queryParam;

    /**
     * If the endpoint failed to execute and we do not want to alert the user, flip this to true
     * This operation is handy if we internally cancelled the endpoint.
     */
    @Getter(AccessLevel.NONE)
    private boolean silenceFailures;

    private MapperManager mapper;

    private final TokenManager tokenManager;

    public Endpoint(PubNub pubnubInstance,
                    TelemetryManager telemetry,
                    RetrofitManager retrofitInstance,
                    TokenManager tokenManager) {
        this.pubnub = pubnubInstance;
        this.retrofit = retrofitInstance;
        this.tokenManager = tokenManager;

        this.mapper = this.pubnub.getMapper();
        this.telemetryManager = telemetry;
    }

    @Override
    @Nullable
    public Output sync() throws PubNubException {
        this.validateParams();

        call = doWork(createBaseParams());
        Response<Input> serverResponse;
        Output response;

        try {
            serverResponse = call.execute();
        } catch (IOException e) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR)
                    .errormsg(e.toString())
                    .affectedCall(call)
                    .cause(e)
                    .build();
        }

        if (isError(serverResponse)) {
            String responseBodyText;
            JsonElement responseBody;

            try {
                responseBodyText = serverResponse.errorBody().string();
            } catch (IOException e) {
                responseBodyText = "N/A";
            }

            try {
                responseBody = mapper.fromJson(responseBodyText, JsonElement.class);
            } catch (PubNubException e) {
                responseBody = null;
            }

            throw createPubNubException(serverResponse, responseBodyText, responseBody);
        }

        storeRequestLatency(serverResponse, getOperationType());
        response = createResponse(serverResponse);

        return response;
    }

    @Override
    public void async(@NotNull final PNCallback<Output> callback) {
        cachedCallback = callback;

        try {
            this.validateParams();
            call = doWork(createBaseParams());
        } catch (PubNubException pubnubException) {
            callback.onResponse(null,
                    createStatusResponse(PNStatusCategory.PNBadRequestCategory, null, pubnubException,
                            null, null));
            return;
        }

        call.enqueue(new retrofit2.Callback<Input>() {

            @Override
            public void onResponse(Call<Input> performedCall, Response<Input> response) {
                Output callbackResponse;

                if (isError(response)) {

                    String responseBodyText;
                    JsonElement responseBody;
                    JsonElement responseBodyPayload = null;
                    ArrayList<String> affectedChannels = new ArrayList<>();
                    ArrayList<String> affectedChannelGroups = new ArrayList<>();

                    try (ResponseBody errorBody = response.errorBody()) {
                        if (errorBody != null) {
                            responseBodyText = errorBody.string();
                        } else {
                            responseBodyText = "N/A";
                        }
                    } catch (IOException e) {
                        responseBodyText = "N/A";
                    }

                    try {
                        responseBody = mapper.fromJson(responseBodyText, JsonElement.class);
                    } catch (PubNubException e) {
                        responseBody = null;
                    }

                    if (responseBody != null && mapper.isJsonObject(responseBody) && mapper.hasField(responseBody,
                            "payload")) {
                        responseBodyPayload = mapper.getField(responseBody, "payload");
                    }

                    PNStatusCategory pnStatusCategory = PNStatusCategory.PNUnknownCategory;
                    final PubNubException ex = createPubNubException(response, responseBodyText, responseBody);

                    if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                        pnStatusCategory = PNStatusCategory.PNAccessDeniedCategory;

                        if (responseBodyPayload != null && mapper.hasField(responseBodyPayload, "channels")) {
                            Iterator<JsonElement> it = mapper.getArrayIterator(responseBodyPayload, "channels");
                            while (it.hasNext()) {
                                JsonElement objNode = it.next();
                                affectedChannels.add(mapper.elementToString(objNode));
                            }
                        }

                        if (responseBodyPayload != null && mapper.hasField(responseBodyPayload, "channel-groups")) {
                            Iterator<JsonElement> it = mapper.getArrayIterator(responseBodyPayload, "channel-groups");
                            while (it.hasNext()) {
                                JsonElement objNode = it.next();
                                String channelGroupName =
                                        mapper.elementToString(objNode).substring(0, 1).equals(":")
                                                ? mapper.elementToString(objNode).substring(1)
                                                : mapper.elementToString(objNode);
                                affectedChannelGroups.add(channelGroupName);
                            }
                        }

                    }

                    if (response.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        pnStatusCategory = PNStatusCategory.PNBadRequestCategory;
                    }

                    callback.onResponse(null,
                            createStatusResponse(pnStatusCategory, response, ex, affectedChannels,
                                    affectedChannelGroups));
                    return;
                }
                storeRequestLatency(response, getOperationType());

                try {
                    callbackResponse = createResponse(response);
                } catch (PubNubException pubnubException) {
                    callback.onResponse(null,
                            createStatusResponse(PNStatusCategory.PNMalformedResponseCategory, response,
                                    pubnubException, null, null));
                    return;
                }

                callback.onResponse(callbackResponse,
                        createStatusResponse(PNStatusCategory.PNAcknowledgmentCategory, response,
                                null, null, null));
            }

            @Override
            public void onFailure(Call<Input> performedCall, Throwable throwable) {
                if (silenceFailures) {
                    return;
                }

                PNStatusCategory pnStatusCategory;

                PubNubException.PubNubExceptionBuilder pubnubException = PubNubException.builder()
                        .errormsg(throwable.getMessage())
                        .cause(throwable);

                try {
                    throw throwable;
                } catch (UnknownHostException networkException) {
                    pubnubException.pubnubError(PubNubErrorBuilder.PNERROBJ_CONNECTION_NOT_SET);
                    pnStatusCategory = PNStatusCategory.PNUnexpectedDisconnectCategory;
                } catch (SocketException | SSLException exception) {
                    pubnubException.pubnubError(PubNubErrorBuilder.PNERROBJ_CONNECT_EXCEPTION);
                    pnStatusCategory = PNStatusCategory.PNUnexpectedDisconnectCategory;
                } catch (SocketTimeoutException socketTimeoutException) {
                    pubnubException.pubnubError(PubNubErrorBuilder.PNERROBJ_SOCKET_TIMEOUT);
                    pnStatusCategory = PNStatusCategory.PNTimeoutCategory;
                } catch (Throwable throwable1) {
                    pubnubException.pubnubError(PubNubErrorBuilder.PNERROBJ_HTTP_ERROR);
                    if (performedCall.isCanceled()) {
                        pnStatusCategory = PNStatusCategory.PNCancelledCategory;
                    } else {
                        pnStatusCategory = PNStatusCategory.PNBadRequestCategory;
                    }
                }

                callback.onResponse(null,
                        createStatusResponse(pnStatusCategory, null, pubnubException.build(),
                                null, null));

            }
        });
    }

    private PubNubException createPubNubException(Response<Input> response,
                                                  String responseBodyText,
                                                  JsonElement responseBody) {
        if (response.code() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE
                || response.code() == HttpURLConnection.HTTP_REQ_TOO_LONG) {
            return PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_PAYLOAD_TOO_LARGE)
                    .affectedCall(call)
                    .statusCode(response.code())
                    .jso(responseBody)
                    .errormsg(PubNubErrorBuilder.PNERROBJ_PAYLOAD_TOO_LARGE.getMessage())
                    .build();
        }

        return PubNubException.builder()
                .pubnubError(PubNubErrorBuilder.PNERROBJ_HTTP_ERROR)
                .errormsg(responseBodyText)
                .jso(responseBody)
                .statusCode(response.code())
                .affectedCall(call)
                .build();
    }

    @Override
    public void retry() {
        silenceFailures = false;
        async(cachedCallback);
    }

    /**
     * cancel the operation but do not alert anybody, useful for restarting the heartbeats and subscribe loops.
     */
    @Override
    public void silentCancel() {
        if (call != null && !call.isCanceled()) {
            this.silenceFailures = true;
            call.cancel();
        }
    }

    protected boolean isError(Response<Input> response) {
        return response.code() != HttpURLConnection.HTTP_OK;
    }

    private PNStatus createStatusResponse(PNStatusCategory category, Response<Input> response, Exception throwable,
                                          ArrayList<String> errorChannels, ArrayList<String> errorChannelGroups) {
        PNStatus.PNStatusBuilder pnStatus = PNStatus.builder();

        pnStatus.executedEndpoint(this);

        if (response == null || throwable != null) {
            pnStatus.error(true);
        }
        if (throwable != null) {
            PNErrorData pnErrorData = new PNErrorData(throwable.getMessage(), throwable);
            pnStatus.errorData(pnErrorData);
        }

        if (response != null) {
            pnStatus.statusCode(response.code());
            pnStatus.tlsEnabled(response.raw().request().url().isHttps());
            pnStatus.origin(response.raw().request().url().host());
            pnStatus.uuid(response.raw().request().url().queryParameter("uuid"));
            pnStatus.authKey(response.raw().request().url().queryParameter(PubNubUtil.AUTH_QUERY_PARAM_NAME));
            pnStatus.clientRequest(response.raw().request());
        }

        pnStatus.operation(getOperationType());
        pnStatus.category(category);

        if (errorChannels != null && !errorChannels.isEmpty()) {
            pnStatus.affectedChannels(errorChannels);
        } else {
            pnStatus.affectedChannels(getAffectedChannels());
        }

        if (errorChannelGroups != null && !errorChannelGroups.isEmpty()) {
            pnStatus.affectedChannelGroups(errorChannelGroups);
        } else {
            pnStatus.affectedChannelGroups(getAffectedChannelGroups());
        }

        return pnStatus.build();
    }

    private void storeRequestLatency(Response response, PNOperationType type) {
        if (this.telemetryManager != null) {
            long latency = response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis();
            this.telemetryManager.storeLatency(latency, type.toString());
        }
    }

    protected Map<String, String> createBaseParams() {
        Map<String, String> params = new HashMap<>();

        if (queryParam != null) {
            params.putAll(queryParam);
        }

        params.put("pnsdk", "PubNub-Java-Unified/".concat(this.pubnub.getVersion()));
        params.put("uuid", this.pubnub.getConfiguration().getUserId().getValue());

        if (this.pubnub.getConfiguration().isIncludeInstanceIdentifier()) {
            params.put("instanceid", pubnub.getInstanceId());
        }

        if (this.pubnub.getConfiguration().isIncludeRequestIdentifier()) {
            params.put("requestid", pubnub.getRequestId());
        }

        if (isAuthRequired()) {
            final String token = tokenManager.getToken();
            if (token != null) {
                params.put(PubNubUtil.AUTH_QUERY_PARAM_NAME, token);
            } else if (this.pubnub.getConfiguration().getAuthKey() != null) {
                params.put(PubNubUtil.AUTH_QUERY_PARAM_NAME, pubnub.getConfiguration().getAuthKey());
            }
        }

        if (this.telemetryManager != null) {
            params.putAll(this.telemetryManager.operationsLatency());
        }

        return params;
    }

    protected Map<String, String> encodeParams(Map<String, String> params) {
        Map<String, String> encodedParams = new HashMap<>(params);
        if (encodedParams.containsKey(PubNubUtil.AUTH_QUERY_PARAM_NAME)) {
            encodedParams.put(PubNubUtil.AUTH_QUERY_PARAM_NAME, PubNubUtil.urlEncode(encodedParams.get(PubNubUtil.AUTH_QUERY_PARAM_NAME)));
        }
        return encodedParams;
    }

    protected abstract List<String> getAffectedChannels();

    protected abstract List<String> getAffectedChannelGroups();

    protected abstract void validateParams() throws PubNubException;

    protected abstract Call<Input> doWork(Map<String, String> baseParams) throws PubNubException;

    protected abstract Output createResponse(Response<Input> input) throws PubNubException;

    protected abstract PNOperationType getOperationType();

    protected abstract boolean isAuthRequired();

}
