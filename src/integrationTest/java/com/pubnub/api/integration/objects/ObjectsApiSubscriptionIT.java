package com.pubnub.api.integration.objects;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNChannelMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ObjectsApiSubscriptionIT extends ObjectsApiBaseIT {
    private final String TEST_CHANNEL = UUID.randomUUID().toString();
    private final String STATUS = "active";
    private final String TYPE = "chat";

    class TestSubscribeCallbackAdapter extends SubscribeCallback {
        @Override
        public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {}

        @Override
        public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {}

        @Override
        public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {}

        @Override
        public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {}

        @Override
        public void uuid(@NotNull PubNub pubnub, @NotNull PNUUIDMetadataResult pnUUIDMetadataResult) {}

        @Override
        public void channel(@NotNull PubNub pubnub, @NotNull PNChannelMetadataResult pnChannelMetadataResult) {}

        @Override
        public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {}

        @Override
        public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {}

        @Override
        public void file(@NotNull final PubNub pubnub, @NotNull final PNFileEventResult pnFileEventResult) {}
    }

    @Test
    public void receivingCallbackObjectsHasBeenSet() throws Exception {
        //given
        final AtomicReference<PNUUIDMetadataResult> uuidMetadataResultHolder = new AtomicReference<>();
        final AtomicReference<PNChannelMetadataResult> channelMetadataResultHolder = new AtomicReference<>();
        final AtomicReference<PNMembershipResult> membershipResultHolder = new AtomicReference<>();

        pubNubUnderTest.addListener(new TestSubscribeCallbackAdapter() {
            @Override
            public void uuid(@NotNull PubNub pubnub, @NotNull PNUUIDMetadataResult pnUUIDMetadataResult) {
                uuidMetadataResultHolder.set(pnUUIDMetadataResult);
            }

            @Override
            public void channel(@NotNull PubNub pubnub, @NotNull PNChannelMetadataResult pnChannelMetadataResult) {
                channelMetadataResultHolder.set(pnChannelMetadataResult);
            }

            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {
                membershipResultHolder.set(pnMembershipResult);
            }
        });
        //when
        pubNubUnderTest.subscribe()
                .channels(Collections.singletonList(TEST_CHANNEL))
                .execute();

        pubNubUnderTest.setChannelMetadata()
                .channel(TEST_CHANNEL)
                .name("The Channel")
                .description("This is test description")
                .status(STATUS)
                .type(TYPE)
                .sync();

        pubNubUnderTest.setMemberships()
                .channelMemberships(Collections.singletonList(PNChannelMembership.channel(TEST_CHANNEL)))
                .sync();

        final String testName = "Test Name";
        pubNubUnderTest.setUUIDMetadata().name(testName)
                .sync();

        //then
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    final PNUUIDMetadataResult receivedUUIDMetadataResult = uuidMetadataResultHolder.get();
                    final PNChannelMetadataResult receivedChannelMetadataResult = channelMetadataResultHolder.get();
                    final PNMembershipResult receivedMembershipResult = membershipResultHolder.get();
                    assertThat(receivedUUIDMetadataResult,
                            allOf(notNullValue(), hasProperty("event", is("set"))));
                    assertThat(receivedChannelMetadataResult,
                            allOf(notNullValue(), hasProperty("event", is("set"))));
                    assertThat(receivedMembershipResult,
                            allOf(notNullValue(), hasProperty("event", is("set"))));
                });

    }

    @Test
    public void receivingCallbackObjectsHasBeenUnset() throws Exception {
        //given
        final AtomicReference<PNUUIDMetadataResult> uuidMetadataResultHolder = new AtomicReference<>();
        final AtomicReference<PNChannelMetadataResult> channelMetadataResultHolder = new AtomicReference<>();
        final AtomicReference<PNMembershipResult> membershipResultHolder = new AtomicReference<>();

        pubNubUnderTest.addListener(new TestSubscribeCallbackAdapter() {
            @Override
            public void uuid(@NotNull PubNub pubnub, @NotNull PNUUIDMetadataResult pnUUIDMetadataResult) {
                uuidMetadataResultHolder.set(pnUUIDMetadataResult);
            }

            @Override
            public void channel(@NotNull PubNub pubnub, @NotNull PNChannelMetadataResult pnChannelMetadataResult) {
                channelMetadataResultHolder.set(pnChannelMetadataResult);
            }

            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {
                membershipResultHolder.set(pnMembershipResult);
            }
        });
        pubNubUnderTest.setChannelMetadata()
                .channel(TEST_CHANNEL)
                .name("The Channel")
                .description("This is test description")
                .sync();

        pubNubUnderTest.setMemberships()
                .channelMemberships(Collections.singletonList(PNChannelMembership.channel(TEST_CHANNEL)))
                .sync();

        final String testName = "Test Name";
        pubNubUnderTest.setUUIDMetadata().name(testName)
                .sync();

        pubNubUnderTest.subscribe()
                .channels(Collections.singletonList(TEST_CHANNEL))
                .execute();
        //when
        pubNubUnderTest.removeUUIDMetadata().sync();

        pubNubUnderTest.removeMemberships()
                .channelMemberships(Collections.singletonList(PNChannelMembership.channel(TEST_CHANNEL)))
                .sync();

        pubNubUnderTest.removeChannelMetadata()
                .channel(TEST_CHANNEL)
                .sync();

        //then
        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    final PNUUIDMetadataResult receivedUUIDMetadataResult = uuidMetadataResultHolder.get();
                    final PNChannelMetadataResult receivedChannelMetadataResult = channelMetadataResultHolder.get();
                    final PNMembershipResult receivedMembershipResult = membershipResultHolder.get();
                    assertThat(receivedUUIDMetadataResult,
                            allOf(notNullValue(), hasProperty("event", is("delete"))));
                    assertThat(receivedChannelMetadataResult,
                            allOf(notNullValue(), hasProperty("event", is("delete"))));
                    assertThat(receivedMembershipResult,
                            allOf(notNullValue(), hasProperty("event", is("delete"))));
                });

    }

}
