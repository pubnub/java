package com.pubnub.api.managers.subscription.utils;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.UserId;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.models.server.SubscribeEnvelope;
import com.pubnub.api.services.SubscribeService;
import lombok.SneakyThrows;
import okhttp3.Request;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubscriptionTestUtils {
    @SneakyThrows
    public static PubNub pubnub(final RetrofitManager retrofitManager) {
        final PNConfiguration pnConfiguration =  new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
        pnConfiguration.setSubscribeKey("fake_sub_key");
        pnConfiguration.setConnectTimeout(1);

        final PubNub pubnub =  new PubNub(pnConfiguration);
        FieldUtils.writeField(pubnub, "retrofitManager", retrofitManager, true);
        return pubnub;
    }

    @NotNull
    @SneakyThrows
    public static TelemetryManager telemetryManager(final PubNub pubnub) {
        return (TelemetryManager) FieldUtils.readField(pubnub, "telemetryManager", true);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static RetrofitManager retrofitManagerMock(final ResponseSupplier<SubscribeEnvelope> responseSupplier) {
        final SubscribeService subscribeServiceMock = mock(SubscribeService.class);

        final ArgumentCaptor<String> subscribeKeyCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> channelCSVCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Map<String, String>> optionsCaptor = ArgumentCaptor.forClass(Map.class);

        when(subscribeServiceMock.subscribe(subscribeKeyCaptor.capture(), channelCSVCaptor.capture(), optionsCaptor.capture()))
                .thenAnswer(invocation -> {
                    final String subscribeKey = subscribeKeyCaptor.getValue();
                    final String channelCSV = channelCSVCaptor.getValue();
                    final Map<String, String> options = optionsCaptor.getValue();

                    final Request.Builder requestBuilder = new Request.Builder();
                    final Request request = requestBuilder.get()
                            .url("Http://example.com/" + channelCSV + "/" + options)
                            .tag(RequestDetails.class, new RequestDetails.SubscribeRequestDetails(subscribeKey, channelCSV, options)).build();

                    return new FakeCall<>(request, responseSupplier);
                });

        final RetrofitManager retrofitManagerMock = mock(RetrofitManager.class);
        when(retrofitManagerMock.getSubscribeService()).thenReturn(subscribeServiceMock);
        return retrofitManagerMock;
    }
}
