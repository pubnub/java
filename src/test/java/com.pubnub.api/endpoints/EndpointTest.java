package com.pubnub.api.endpoints;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNOperationType;
import okhttp3.Request;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Map;

public class EndpointTest extends TestHarness {

    PubNub pubnub;


    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
    }

    @Test
    public void testUUID() throws PubNubException {
        Endpoint<Object, Object> endpoint =  new Endpoint<Object, Object>(pubnub) {

            @Override
            protected boolean validateParams() {
                return false;
            }

            @Override
            protected Object createResponse(Response input) throws PubNubException {
                return null;
            }

            @Override
            protected int getConnectTimeout() {
                return 0;
            }

            @Override
            protected int getRequestTimeout() {
                return 0;
            }

            @Override
            protected PNOperationType getOperationType() {
                return null;
            }

            @Override
            protected Call doWork(Map baseParams) throws PubNubException {

                Call<Object> fakeCall = new Call<Object>() {

                    @Override
                    public Response<Object> execute() throws IOException {
                        Response<Object> newResponse = Response.success(null);
                        return newResponse;
                    }

                    @Override
                    public void enqueue(Callback<Object> callback) {

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
                        return null;
                    }

                    @Override
                    public Request request() {
                        return null;
                    }
                };

                Assert.assertEquals("myUUID",baseParams.get("uuid"));
                return fakeCall;
            }
        };

        endpoint.sync();
    }



}
