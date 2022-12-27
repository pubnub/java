package com.pubnub.api.endpoints;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.token_manager.TokenManager;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class EndpointTest extends TestHarness {

    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        pubnub.getConfiguration().setIncludeInstanceIdentifier(true);
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
    }

    @Test
    public void testBaseParams() throws PubNubException {
        Endpoint<Object, Object> endpoint = new Endpoint<Object, Object>(pubnub, null, null, new TokenManager()) {

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
            }

            @Override
            protected Object createResponse(Response input) throws PubNubException {
                return null;
            }

            @Override
            protected PNOperationType getOperationType() {
                return null;
            }

            @Override
            protected boolean isAuthRequired() {
                return true;
            }

            @Override
            protected Call doWork(Map baseParams) throws PubNubException {

                Call fakeCall = successfulCall();

                Assert.assertEquals("myUUID", baseParams.get("uuid"));
                Assert.assertEquals("PubNubRequestId", baseParams.get("requestid"));
                Assert.assertEquals("PubNubInstanceId", baseParams.get("instanceid"));
                return fakeCall;
            }
        };

        endpoint.sync();
    }

    @Test
    public void payloadTooLargeTest_Sync() {
        Endpoint<Object, Object> endpoint = testEndpoint(call(Response.error(HttpURLConnection.HTTP_ENTITY_TOO_LARGE,
                ResponseBody.create("{}", MediaType.get("application/json")))));

        try {
            endpoint.sync();
            Assert.fail("Exception expected");
        } catch (PubNubException e) {
            Assert.assertEquals(PubNubErrorBuilder.PNERR_PAYLOAD_TOO_LARGE, e.getPubnubError().getErrorCode());
        }
    }

    @Test
    public void payloadTooLargeTest_Async() {
        Endpoint<Object, Object> endpoint = testEndpoint(call(Response.error(HttpURLConnection.HTTP_ENTITY_TOO_LARGE,
                ResponseBody.create("{}", MediaType.get("application/json")))));

        endpoint.async((result, status) -> {
            if (status.isError()) {
                Assert.assertEquals(PubNubErrorBuilder.PNERR_PAYLOAD_TOO_LARGE, status.getStatusCode());
            } else {
                Assert.fail("Error expected");
            }
        });
    }

    private Endpoint<Object, Object> testEndpoint(Call<Object> call) {
        return new Endpoint<Object, Object>(pubnub, null, null, new TokenManager()) {

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
            }

            @Override
            protected Object createResponse(Response<Object> input) throws PubNubException {
                return null;
            }

            @Override
            protected PNOperationType getOperationType() {
                return null;
            }

            @Override
            protected boolean isAuthRequired() {
                return true;
            }

            @Override
            protected Call doWork(Map baseParams) throws PubNubException {

                Call fakeCall = call;
                return fakeCall;
            }
        };
    }

    private Call<Object> call(Response<Object> response) {
        return new Call<Object>() {

            @Override
            public Response<Object> execute() throws IOException {
                return response;
            }

            @Override
            public void enqueue(Callback<Object> callback) {
                Call<Object> that = this;
                Executors.newSingleThreadExecutor().execute(
                        () -> callback.onResponse(that, response)
                );
            }

            @Override
            public boolean isExecuted() {
                return false;
            }

            @Override
            public void cancel() {
            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public Call<Object> clone() {
                return this;
            }

            @Override
            public Request request() {
                return new Request.Builder().build();
            }
        };
    }

    private Call<Object> successfulCall() {
        return call(Response.success(null));
    }
}
