package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.models.consumer.history.PNMessageCountResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class MessageCountTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testSyncDisabled() {

        String payload = "[\"Use of the history API requires the Storage & Playback which is not enabled for this " +
                "subscribe key.Login to your PubNub Dashboard Account and enable Storage & Playback.Contact support " +
                "@pubnub.com if you require further assistance.\",0,0]";

        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(aResponse().withBody(payload)));

        try {
            pubnub.messageCounts()
                    .channels(Collections.singletonList("my_channel"))
                    .channelsTimetoken(Collections.singletonList(10000L))
                    .sync();
        } catch (PubNubException ex) {
            assertEquals("History is disabled", ex.getErrormsg());
        }
    }

    @Test
    public void testSingleChannelWithSingleToken() throws PubNubException {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19}}")));

        PNMessageCountResult response = pubnub.messageCounts()
                .channels(Collections.singletonList("my_channel"))
                .channelsTimetoken(Collections.singletonList(10000L))
                .sync();

        assert response != null;

        assertEquals(response.getChannels().size(), 1);
        assertFalse(response.getChannels().containsKey("channel_does_not_exist"));
        assertTrue(response.getChannels().containsKey("my_channel"));
        for (Map.Entry<String, Long> stringLongEntry : response.getChannels().entrySet()) {
            assertEquals("my_channel", stringLongEntry.getKey());
            assertEquals(Long.valueOf("19"), stringLongEntry.getValue());
        }
    }

    @Test
    public void testSingleChannelWithMultipleTokens() {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19}}")));

        PubNubException exception = null;

        try {
            pubnub.messageCounts()
                    .channels(Collections.singletonList("my_channel"))
                    .channelsTimetoken(Arrays.asList(10000L, 20000L))
                    .sync();
        } catch (PubNubException e) {
            exception = e;
        } finally {
            assertNotNull(exception);
            assertEquals(PubNubErrorBuilder.PNERROBJ_CHANNELS_TIMETOKEN_MISMATCH.getMessage(),
                    exception.getPubnubError().getMessage());
        }
    }

    @Test
    public void testMultipleChannelsWithSingleToken() throws PubNubException {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel,new_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19, \"new_channel\":5}}")));

        PNMessageCountResult response = pubnub.messageCounts()
                .channels(Arrays.asList("my_channel", "new_channel"))
                .channelsTimetoken(Collections.singletonList(10000L))
                .sync();

        assert response != null;

        assertEquals(response.getChannels().size(), 2);
        assertFalse(response.getChannels().containsKey("channel_does_not_exist"));
        assertTrue(response.getChannels().containsKey("my_channel"));
        assertTrue(response.getChannels().containsKey("new_channel"));

        for (Map.Entry<String, Long> stringLongEntry : response.getChannels().entrySet()) {
            if (stringLongEntry.getKey().equals("my_channel")) {
                assertEquals(Long.valueOf("19"), stringLongEntry.getValue());
            } else if (stringLongEntry.getKey().equals("new_channel")) {
                assertEquals(Long.valueOf("5"), stringLongEntry.getValue());
            }
        }
    }

    @Test
    public void testMultipleChannelsWithMultipleTokens() throws PubNubException {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel,new_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19, \"new_channel\":5}}")));

        PNMessageCountResult response = pubnub.messageCounts()
                .channels(Arrays.asList("my_channel", "new_channel"))
                .channelsTimetoken(Arrays.asList(10000L, 20000L))
                .sync();

        assert response != null;

        assertEquals(response.getChannels().size(), 2);
        assertFalse(response.getChannels().containsKey("channel_does_not_exist"));
        assertTrue(response.getChannels().containsKey("my_channel"));
        assertTrue(response.getChannels().containsKey("new_channel"));

        for (Map.Entry<String, Long> stringLongEntry : response.getChannels().entrySet()) {
            if (stringLongEntry.getKey().equals("my_channel")) {
                assertEquals(Long.valueOf("19"), stringLongEntry.getValue());
            } else if (stringLongEntry.getKey().equals("new_channel")) {
                assertEquals(Long.valueOf("5"), stringLongEntry.getValue());
            }
        }
    }

    @Test
    public void testWithoutTimeToken() {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19}}")));

        PubNubException exception = null;
        try {
            pubnub.messageCounts()
                    .channels(Collections.singletonList("my_channel"))
                    .sync();
        } catch (PubNubException ex) {
            exception = ex;
        } finally {
            assertNotNull(exception);
            assertEquals(PubNubErrorBuilder.PNERROBJ_TIMETOKEN_MISSING.getMessage(),
                    exception.getPubnubError().getMessage());
        }
    }

    @Test
    public void testWithoutChannelsSingleToken() {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19, \"new_channel\":5}}")));

        PubNubException exception = null;
        try {
            pubnub.messageCounts()
                    .channelsTimetoken(Collections.singletonList(10000L))
                    .sync();
        } catch (PubNubException ex) {
            exception = ex;
        } finally {
            assertNotNull(exception);
            assertEquals(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING.getMessage(),
                    exception.getPubnubError().getMessage());
        }
    }

    @Test
    public void testWithoutChannelsMultipleTokens() {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19, \"new_channel\":5}}")));

        PubNubException exception = null;
        try {
            pubnub.messageCounts()
                    .channelsTimetoken(Arrays.asList(10000L, 20000L))
                    .sync();
        } catch (PubNubException ex) {
            exception = ex;
        } finally {
            assertNotNull(exception);
            assertEquals(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING.getMessage(),
                    exception.getPubnubError().getMessage());
        }
    }

    @Test
    public void testChannelWithSingleEmptyToken() {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19}}")));

        PubNubException exception = null;
        try {
            pubnub.messageCounts()
                    .channels(Collections.singletonList("my_channel"))
                    .channelsTimetoken(Collections.singletonList(null))
                    .sync();
        } catch (PubNubException ex) {
            exception = ex;
        } finally {
            assertNotNull(exception);
            assertEquals(PubNubErrorBuilder.PNERROBJ_TIMETOKEN_MISSING.getMessage(),
                    exception.getPubnubError().getMessage());
        }
    }

    @Test
    public void testChannelWithMultipleNullTokens() {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":19}}")));

        PubNubException exception = null;
        try {
            pubnub.messageCounts()
                    .channels(Arrays.asList("my_channel", "my_channel_1", "my_channel_2"))
                    .channelsTimetoken(Arrays.asList(10000L, null, 20000L))
                    .sync();
        } catch (PubNubException ex) {
            exception = ex;
        } finally {
            assertNotNull(exception);
            assertEquals(PubNubErrorBuilder.PNERROBJ_TIMETOKEN_MISSING.getMessage(),
                    exception.getPubnubError().getMessage());
        }
    }


}
