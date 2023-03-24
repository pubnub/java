package com.pubnub.api.endpoints.files;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PublishFileMessageTest {
    private PublishFileMessage objectUnderTest;
    private PubNub pubNub;

    private static final String SPACE_ID_QUERY_PARAM = "space-id";
    private static final String MESSAGE_TYPE_QUERY_PARAM = "type";

    @BeforeEach
    void setUp() throws PubNubException {
        PNConfiguration pnConfiguration = new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
        pnConfiguration.setSubscribeKey("demo");
        pnConfiguration.setPublishKey("demo");

        pubNub = new PubNub(pnConfiguration);

    }

    @Test
    void when_spaceId_and_message_type_is_provide_then_they_should_be_added_to_url_query_param() throws PubNubException {
        Map<String, String> baseParams = getBaseParams();
        String expectedSpaceIdValue = "mySpace";
        String expectedType = "messageType";
        objectUnderTest = pubNub.publishFileMessage()
                .channel("channel")
                .fileName("myFileName.png")
                .fileId("someFileId")
                .spaceId(new SpaceId(expectedSpaceIdValue))
                .type(expectedType);

        Call<List<Object>> publishFileMessageCall = objectUnderTest.doWork(baseParams);

        assertEquals(expectedSpaceIdValue, publishFileMessageCall.request().url().queryParameter(SPACE_ID_QUERY_PARAM));
        assertEquals(expectedType, publishFileMessageCall.request().url().queryParameter(MESSAGE_TYPE_QUERY_PARAM));
    }

    @Test
    void when_spaceId_and_messageType_are_not_provided_then_they_should_not_be_added_to_url_query_param() throws PubNubException {
        Map<String, String> baseParams = getBaseParams();
        objectUnderTest = pubNub.publishFileMessage()
                .channel("channel")
                .fileName("myFileName.png")
                .fileId("someFileId");

        Call<List<Object>> publishFileMessageCall = objectUnderTest.doWork(baseParams);

        assertNull(publishFileMessageCall.request().url().queryParameter(SPACE_ID_QUERY_PARAM));
        assertNull(publishFileMessageCall.request().url().queryParameter(MESSAGE_TYPE_QUERY_PARAM));
    }

    private Map<String, String> getBaseParams() {
        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("pnsdk", "PubNub-Java-Unified/6.3.0");
        baseParams.put("requestid", "c644219c-894d-462e-b0b8-0bed96651d9b");
        baseParams.put("uuid", "client-7ad6d0c0-568c-4f7d-a159-da4fbf0ab3e9");
        return baseParams;
    }
}
