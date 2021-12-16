package com.pubnub.api.endpoints;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.server.FetchMessagesEnvelope;
import com.pubnub.api.services.HistoryService;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HistoryBatchEndpointTest {
    private static final String TEST_CHANNEL_1 = "TEST_CHANNEL_1";
    private static final String TEST_CHANNEL_2 = "TEST_CHANNEL_2";
    private static final String SUBSCRIBE_KEY = "SUB_KEY";
    private static final String TEST_VERSION = "TEST_VERSION";
    private static final int MAX_FOR_FETCH_MESSAGES = 100;
    private static final int MAX_FOR_FETCH_MESSAGES_WITH_ACTIONS = 25;
    private static final int MULTIPLE_CHANNELS_MAX_FOR_FETCH_MESSAGES = 25;
    private static final int EXPECTED_SINGLE_CHANNEL_DEFAULT_MESSAGES = 100;
    private static final int EXPECTED_MULTIPLE_CHANNEL_DEFAULT_MESSAGES = 25;
    private static final int EXPECTED_DEFAULT_MESSAGES_WITH_ACTIONS = 25;
    private static final int EXPECTED_MAX_MESSAGES_WITH_ACTIONS = 25;

    private final PubNub pubnub = pubNubMock();
    private final HistoryService historyService = historyServiceMock();
    private final RetrofitManager retrofitManager = retrofitManagerMock(historyService);
    private final TelemetryManager telemetryManager = mock(TelemetryManager.class);
    private final ArgumentCaptor<Map<String, String>> optionsCaptor = ArgumentCaptor.forClass(Map.class);

    public HistoryBatchEndpointTest() throws PubNubException {
    }

    @Test
    public void forSingleChannelFetchMessagesAlwaysPassMaxWhenItIsInBound() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        final int maximumPerChannel = randomInt(MAX_FOR_FETCH_MESSAGES);
        //when
        fetchMessagesUnderTest
                .channels(Collections.singletonList(TEST_CHANNEL_1))
                .maximumPerChannel(maximumPerChannel)
                .sync();

        //then
        verify(historyService).fetchMessages(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(maximumPerChannel)))));
    }

    @Test
    public void forSingleChannelFetchMessagesAlwaysPassDefaultWhenNonPositiveMaxSpecified() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        final int maximumPerChannel = -(randomInt(100) - 1);
        //when
        fetchMessagesUnderTest
                .channels(Collections.singletonList(TEST_CHANNEL_1))
                .maximumPerChannel(maximumPerChannel)
                .sync();

        //then
        verify(historyService).fetchMessages(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(EXPECTED_SINGLE_CHANNEL_DEFAULT_MESSAGES)))));
    }


    @Test
    public void forSingleChannelFetchMessagesAlwaysPassDefaultWhenMaxNotSpecified() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        //when
        fetchMessagesUnderTest
                .channels(Collections.singletonList(TEST_CHANNEL_1))
                .sync();

        //then
        verify(historyService).fetchMessages(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(EXPECTED_SINGLE_CHANNEL_DEFAULT_MESSAGES)))));
    }

    @Test
    public void forSingleChannelFetchMessagesAlwaysPassDefaultMaxWhenMaxExceeds() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        final int maximumPerChannel = MAX_FOR_FETCH_MESSAGES + randomInt(MAX_FOR_FETCH_MESSAGES);
        //when
        fetchMessagesUnderTest
                .channels(Collections.singletonList(TEST_CHANNEL_1))
                .maximumPerChannel(maximumPerChannel)
                .sync();

        //then
        verify(historyService).fetchMessages(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(EXPECTED_SINGLE_CHANNEL_DEFAULT_MESSAGES)))));
    }

    @Test
    public void forMultipleChannelsFetchMessagesAlwaysPassMaxWhenItIsInBound() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        final int maximumPerChannel = randomInt(MULTIPLE_CHANNELS_MAX_FOR_FETCH_MESSAGES);
        //when
        fetchMessagesUnderTest
                .channels(Arrays.asList(TEST_CHANNEL_1, TEST_CHANNEL_2))
                .maximumPerChannel(maximumPerChannel)
                .sync();

        //then
        verify(historyService).fetchMessages(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(maximumPerChannel)))));
    }

    @Test
    public void forMultipleChannelsFetchMessagesAlwaysPassDefaultWhenNonPositiveMaxSpecified() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        final int maximumPerChannel = -(randomInt(100) - 1);
        //when
        fetchMessagesUnderTest
                .channels(Arrays.asList(TEST_CHANNEL_1, TEST_CHANNEL_2))
                .maximumPerChannel(maximumPerChannel)
                .sync();

        //then
        verify(historyService).fetchMessages(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(EXPECTED_MULTIPLE_CHANNEL_DEFAULT_MESSAGES)))));
    }


    @Test
    public void forMultipleChannelsFetchMessagesAlwaysPassDefaultWhenMaxNotSpecified() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        //when
        fetchMessagesUnderTest
                .channels(Arrays.asList(TEST_CHANNEL_1, TEST_CHANNEL_2))
                .sync();

        //then
        verify(historyService).fetchMessages(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(EXPECTED_MULTIPLE_CHANNEL_DEFAULT_MESSAGES)))));
    }

    @Test
    public void forMultipleChannelsFetchMessagesAlwaysPassDefaultMaxWhenMaxExceeds() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        final int maximumPerChannel = MULTIPLE_CHANNELS_MAX_FOR_FETCH_MESSAGES + randomInt(MULTIPLE_CHANNELS_MAX_FOR_FETCH_MESSAGES);
        //when
        fetchMessagesUnderTest
                .channels(Arrays.asList(TEST_CHANNEL_1, TEST_CHANNEL_2))
                .maximumPerChannel(maximumPerChannel)
                .sync();

        //then
        verify(historyService).fetchMessages(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(EXPECTED_MULTIPLE_CHANNEL_DEFAULT_MESSAGES)))));
    }

    @Test
    public void forSingleChannelFetchMessagesWithActionAlwaysPassMaxWhenItIsInBound() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        final int maximumPerChannel = randomInt(MAX_FOR_FETCH_MESSAGES_WITH_ACTIONS);
        //when
        fetchMessagesUnderTest
                .channels(Collections.singletonList(TEST_CHANNEL_1))
                .maximumPerChannel(maximumPerChannel)
                .includeMessageActions(true)
                .sync();

        //then
        verify(historyService).fetchMessagesWithActions(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(maximumPerChannel)))));
    }

    @Test
    public void forSingleChannelFetchMessagesWithActionAlwaysPassDefaultWhenMaxNotSpecified() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        //when
        fetchMessagesUnderTest
                .channels(Collections.singletonList(TEST_CHANNEL_1))
                .includeMessageActions(true)
                .sync();

        //then
        verify(historyService).fetchMessagesWithActions(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(EXPECTED_DEFAULT_MESSAGES_WITH_ACTIONS)))));
    }

    @Test
    public void forSingleChannelFetchMessagesWithActionAlwaysPassDefaultMaxWhenMaxExceeds() throws PubNubException {
        //given
        final FetchMessages fetchMessagesUnderTest = new FetchMessages(pubnub, telemetryManager, retrofitManager,
                new TokenManager());

        final int maximumPerChannel = MAX_FOR_FETCH_MESSAGES_WITH_ACTIONS +
                randomInt(MAX_FOR_FETCH_MESSAGES_WITH_ACTIONS);
        //when
        fetchMessagesUnderTest
                .channels(Collections.singletonList(TEST_CHANNEL_1))
                .maximumPerChannel(maximumPerChannel)
                .includeMessageActions(true)
                .sync();

        //then
        verify(historyService).fetchMessagesWithActions(eq(SUBSCRIBE_KEY), any(), optionsCaptor.capture());

        final Map<String, String> capturedOptions = optionsCaptor.getValue();
        assertThat(capturedOptions, allOf(
                notNullValue(),
                hasEntry(is("max"), is(Integer.toString(EXPECTED_MAX_MESSAGES_WITH_ACTIONS)))));
    }

    private static int randomInt(int max) {
        final Random random = new Random();
        int randomInt = random.nextInt(max);
        return randomInt + 1;
    }

    @NotNull
    private HistoryService historyServiceMock() {
        final CallAdapter<FetchMessagesEnvelope> fetchMessagesEnvelopeCallAdapter = new CallAdapter<>(new Supplier<Response<FetchMessagesEnvelope>>() {
            @Override
            public Response<FetchMessagesEnvelope> get() {
                final FetchMessagesEnvelope fetchMessagesEnvelope = new FetchMessagesEnvelope();
                fetchMessagesEnvelope.setChannels(Collections.emptyMap());
                return Response.success(fetchMessagesEnvelope);
            }
        });
        final HistoryService historyService = mock(HistoryService.class);
        when(historyService.fetchMessages(eq(SUBSCRIBE_KEY), any(), any())).thenAnswer(new Answer<Call<FetchMessagesEnvelope>>() {
            @Override
            public Call<FetchMessagesEnvelope> answer(final InvocationOnMock invocation) {
                return fetchMessagesEnvelopeCallAdapter;
            }
        });
        when(historyService.fetchMessagesWithActions(eq(SUBSCRIBE_KEY), any(), any())).thenAnswer(new Answer<Call<FetchMessagesEnvelope>>() {
            @Override
            public Call<FetchMessagesEnvelope> answer(final InvocationOnMock invocation) {
                return fetchMessagesEnvelopeCallAdapter;
            }
        });
        return historyService;
    }

    private RetrofitManager retrofitManagerMock(final HistoryService historyService) {
        final RetrofitManager retrofitManager = mock(RetrofitManager.class);
        when(retrofitManager.getHistoryService()).thenReturn(historyService);
        return retrofitManager;
    }

    private PubNub pubNubMock() throws PubNubException {
        final PNConfiguration pnConfiguration = new PNConfiguration(PubNub.generateUUID());
        pnConfiguration.setSubscribeKey(SUBSCRIBE_KEY);

        final PubNub pubnub = mock(PubNub.class);
        when(pubnub.getConfiguration()).thenReturn(pnConfiguration);
        when(pubnub.getVersion()).thenReturn(TEST_VERSION);
        return pubnub;

    }

    private static class CallAdapter<T> implements Call<T> {
        private final Supplier<Response<T>> responseSupplier;

        CallAdapter(final Supplier<Response<T>> responseSupplier) {
            this.responseSupplier = responseSupplier;
        }

        @Override
        public Response<T> execute() throws IOException {
            return responseSupplier.get();
        }

        @Override
        public void enqueue(final Callback<T> callback) {
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
        public Call<T> clone() {
            return null;
        }

        @Override
        public Request request() {
            return null;
        }
    }
}

