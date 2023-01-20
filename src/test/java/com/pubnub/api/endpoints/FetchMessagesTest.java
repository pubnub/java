package com.pubnub.api.endpoints;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.models.server.FetchMessagesEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FetchMessagesTest {
    private FetchMessages objectUnderTest;
    private PubNub pubnub;

    private static final String INCLUDE_USER_MESSAGE_TYPE_QUERY_PARAM = "include_type";
    private static final String INCLUDE_PN_MESSAGE_TYPE_QUERY_PARAM = "include_message_type";
    private static final String INCLUDE_SPACE_ID_QUERY_PARAM = "include_space_id";

    @BeforeEach
    void setUp() throws PubNubException {
        PNConfiguration pnConfiguration = new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
        pnConfiguration.setSubscribeKey("demo");
        pnConfiguration.setPublishKey("demo");

        pubnub = new PubNub(pnConfiguration);
    }

    @Test
    void when_includeMessageType_equal_true_is_specified_explicitly_in_api_call_then_request_should_contain_includeMessageType_request_param_set_to_true() throws PubNubException {
        //Given
        Map<String, String> baseParams = getBaseParams();
        objectUnderTest = pubnub.fetchMessages()
                .channels(Arrays.asList("channel"))
                .includeMessageActions(false)
                .includeMeta(true)
                .includeMessageType(true)
                .includeSpaceId(false);

        //When
        Call<FetchMessagesEnvelope> call = objectUnderTest.doWork(baseParams);

        //Then
        String include_message_type = call.request().url().queryParameter(INCLUDE_PN_MESSAGE_TYPE_QUERY_PARAM);
        String include_type = call.request().url().queryParameter(INCLUDE_USER_MESSAGE_TYPE_QUERY_PARAM);
        String include_space_id = call.request().url().queryParameter(INCLUDE_SPACE_ID_QUERY_PARAM);
        assertTrue(Boolean.parseBoolean(include_message_type));
        assertTrue(Boolean.parseBoolean(include_type));
        assertFalse(Boolean.parseBoolean(include_space_id));
    }

    @Test
    void when_includeMessageType_equal_false_is_specified_explicitly_in_api_call_then_request_should_contain_includeMessageType_request_param_set_to_false() throws PubNubException {
        //Given
        Map<String, String> baseParams = getBaseParams();
        FetchMessages objectUnderTest = pubnub.fetchMessages()
                .channels(Arrays.asList("channel"))
                .includeMessageActions(false)
                .includeMeta(true)
                .includeMessageType(false)
                .includeSpaceId(true);

        //When
        Call<FetchMessagesEnvelope> call = objectUnderTest.doWork(baseParams);

        //Then
        String include_message_type = call.request().url().queryParameter(INCLUDE_PN_MESSAGE_TYPE_QUERY_PARAM);
        String include_type = call.request().url().queryParameter(INCLUDE_USER_MESSAGE_TYPE_QUERY_PARAM);
        String include_space_id = call.request().url().queryParameter(INCLUDE_SPACE_ID_QUERY_PARAM);
        assertFalse(Boolean.parseBoolean(include_message_type));
        assertFalse(Boolean.parseBoolean(include_type));
        assertTrue(Boolean.parseBoolean(include_space_id));
    }

    @Test
    void when_includeMessageType_is_not_provided_then_messages_request_should_contain_includeMessageType_request_param_set_to_true() throws PubNubException {
        //Given
        Map<String, String> baseParams = getBaseParams();
        FetchMessages objectUnderTest = pubnub.fetchMessages()
                .channels(Arrays.asList("channel"))
                .includeMessageActions(false)
                .includeMeta(true)
                .includeSpaceId(true);

        //When
        Call<FetchMessagesEnvelope> call = objectUnderTest.doWork(baseParams);

        //Then
        String include_message_type = call.request().url().queryParameter(INCLUDE_PN_MESSAGE_TYPE_QUERY_PARAM);
        String include_type = call.request().url().queryParameter(INCLUDE_USER_MESSAGE_TYPE_QUERY_PARAM);
        String include_space_id = call.request().url().queryParameter(INCLUDE_SPACE_ID_QUERY_PARAM);
        assertTrue(Boolean.parseBoolean(include_message_type));
        assertTrue(Boolean.parseBoolean(include_type));
        assertTrue(Boolean.parseBoolean(include_space_id));
    }

    @Test
    void when_includeSpaceId_is_not_provided_then_messages_request_should_contain_includeSpaceId_request_param_set_to_false() throws PubNubException {
        //Given
        Map<String, String> baseParams = getBaseParams();
        FetchMessages objectUnderTest = pubnub.fetchMessages()
                .channels(Arrays.asList("channel"))
                .includeMessageActions(false)
                .includeMeta(true);

        //When
        Call<FetchMessagesEnvelope> call = objectUnderTest.doWork(baseParams);

        //Then
        String include_space_id = call.request().url().queryParameter(INCLUDE_SPACE_ID_QUERY_PARAM);
        assertFalse(Boolean.parseBoolean(include_space_id));
    }

    private Map<String, String> getBaseParams() {
        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("pnsdk", "PubNub-Java-Unified/6.3.0");
        baseParams.put("requestid", "c644219c-894d-462e-b0b8-0bed96651d9b");
        baseParams.put("uuid", "client-7ad6d0c0-568c-4f7d-a159-da4fbf0ab3e9");
        return baseParams;
    }
}
