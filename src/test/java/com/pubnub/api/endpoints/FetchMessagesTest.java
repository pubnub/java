package com.pubnub.api.endpoints;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.services.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FetchMessagesTest {
    private FetchMessages objectUnderTest;

    @Mock
    private PubNub pubnub;
    @Mock
    private TelemetryManager telemetryManage;
    @Mock
    private RetrofitManager retrofit;
    @Mock
    private TokenManager tokenManager;
    @Mock
    private HistoryService historyService;
    @Mock
    private PNConfiguration pnConfiguration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectUnderTest = new FetchMessages(pubnub, telemetryManage, retrofit, tokenManager);

        when(retrofit.getHistoryService()).thenReturn(historyService);
        when(pubnub.getConfiguration()).thenReturn(pnConfiguration);
        when(pnConfiguration.getSubscribeKey()).thenReturn("mySybKey");
    }

    @Test
    void when_includeMessageType_equal_true_is_specify_explicitly_in_api_call_then_request_should_contain_proper_request_param() throws PubNubException {
        Map<String, String> baseParams = getBaseParams();
        ArgumentCaptor<Map<String, String>> extendedParamsCaptor = ArgumentCaptor.forClass(Map.class);
        objectUnderTest.channels(Arrays.asList("channel"));
        objectUnderTest.includeMessageActions(false);
        objectUnderTest.includeMeta(true);
        objectUnderTest.includeMessageType(true);
        objectUnderTest.includeSpaceId(false);

        objectUnderTest.doWork(baseParams);

        verify(historyService, times(1)).fetchMessages(anyString(), anyString(), extendedParamsCaptor.capture());

        Map<String, String> extendedRequestParams = extendedParamsCaptor.getValue();
        assertTrue(Boolean.parseBoolean(extendedRequestParams.get("include_message_type")));
        assertTrue(Boolean.parseBoolean(extendedRequestParams.get("include_type")));
        assertFalse(Boolean.parseBoolean(extendedRequestParams.get("include_space_id")));
    }

    @Test
    void when_includeMessageType_equal_false_is_specify_explicitly_in_api_call_then_request_should_contain_proper_request_param() throws PubNubException {
        Map<String, String> baseParams = getBaseParams();
        ArgumentCaptor<Map<String, String>> extendedParamsCaptor = ArgumentCaptor.forClass(Map.class);
        objectUnderTest.channels(Arrays.asList("channel"));
        objectUnderTest.includeMessageActions(false);
        objectUnderTest.includeMeta(true);
        objectUnderTest.includeMessageType(false);
        objectUnderTest.includeSpaceId(true);

        objectUnderTest.doWork(baseParams);

        verify(historyService, times(1)).fetchMessages(anyString(), anyString(), extendedParamsCaptor.capture());

        Map<String, String> extendedRequestParams = extendedParamsCaptor.getValue();
        assertFalse(Boolean.parseBoolean(extendedRequestParams.get("include_message_type")));
        assertFalse(Boolean.parseBoolean(extendedRequestParams.get("include_type")));
        assertTrue(Boolean.parseBoolean(extendedRequestParams.get("include_space_id")));
    }

    @Test
    void when_includeSpaceId_is_not_provided_then_messages_request_should_not_contain_includeSpaceId_request_param() throws PubNubException {
        Map<String, String> baseParams = getBaseParams();
        ArgumentCaptor<Map<String, String>> extendedParamsCaptor = ArgumentCaptor.forClass(Map.class);
        objectUnderTest.channels(Arrays.asList("channel"));
        objectUnderTest.includeMessageActions(false);
        objectUnderTest.includeMeta(true);
        objectUnderTest.includeMessageType(false);

        objectUnderTest.doWork(baseParams);

        verify(historyService, times(1)).fetchMessages(anyString(), anyString(), extendedParamsCaptor.capture());

        Map<String, String> extendedRequestParams = extendedParamsCaptor.getValue();
        assertFalse(Boolean.parseBoolean(extendedRequestParams.get("include_space_id")));
    }

    private Map<String, String> getBaseParams() {
        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("pnsdk", "PubNub-Java-Unified/6.3.0");
        baseParams.put("requestid", "c644219c-894d-462e-b0b8-0bed96651d9b");
        baseParams.put("uuid", "client-7ad6d0c0-568c-4f7d-a159-da4fbf0ab3e9");
        return baseParams;
    }
}
