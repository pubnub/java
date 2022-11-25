package com.pubnub.api.integration.objects.memberships;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.integration.managers.subscription.SubscribeCallbackAdapter;
import com.pubnub.api.integration.objects.ObjectsApiBaseIT;
import com.pubnub.api.models.consumer.objects_api.channel.PNSetChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNChannelMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNGetMembershipsResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNSetMembershipResult;
import org.awaitility.core.ThrowingRunnable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pubnub.api.endpoints.objects_api.utils.Include.PNChannelDetailsLevel.CHANNEL;
import static com.pubnub.api.endpoints.objects_api.utils.Include.PNChannelDetailsLevel.CHANNEL_WITH_CUSTOM;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CustomMetadataInMembershipPropagationIT extends ObjectsApiBaseIT {
    private final String testChannelMetadataId = UUID.randomUUID().toString();
    private final Map<String, Object> testCustomObjectForChannelMetadata = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>("key1", "val1"),
                    new AbstractMap.SimpleImmutableEntry<>("key2", "val2"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    private final Map<String, Object> testCustomObjectForMembership = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>("key3", "val3"),
                    new AbstractMap.SimpleImmutableEntry<>("key4", "val4"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    private PNSetChannelMetadataResult setChannelMetadataResult;
    private PNSetMembershipResult setMembershipResult;


    private CopyOnWriteArrayList<PNMembershipResult> pnMembershipResults = new CopyOnWriteArrayList<>();

    @Before
    public void setCallbackListener() {
        pubNubUnderTest.addListener(new SubscribeCallbackAdapter() {
            @Override
            public void membership(final PubNub pubnub, final PNMembershipResult pnMembershipResult) {
                pnMembershipResults.add(pnMembershipResult);
            }
        });

        pubNubUnderTest.subscribe()
                .channels(Collections.singletonList(testChannelMetadataId))
                .execute();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setMembershipCustomHappyPath() throws PubNubException {
        final String testChannelName = "The Name of the Channel";
        final String testDescription = "Some interesting channel description";
        setChannelMetadataResult = pubNubUnderTest.setChannelMetadata()
                .channel(testChannelMetadataId)
                .name(testChannelName)
                .description(testDescription)
                .custom(testCustomObjectForChannelMetadata)
                .includeCustom(true)
                .sync();

        setMembershipResult = pubNubUnderTest.setMemberships()
                .channelMemberships(Collections.singletonList(
                        PNChannelMembership.channelWithCustom(setChannelMetadataResult.getData().getId(),
                                testCustomObjectForMembership)))
                .includeCustom(true)
                .includeChannel(CHANNEL_WITH_CUSTOM)
                .sync();

        final PNGetMembershipsResult getMembershipsResult = pubNubUnderTest.getMemberships()
                .includeCustom(false)
                .includeChannel(CHANNEL)
                .sync();

        assertThat(setChannelMetadataResult,
                hasProperty("data", hasProperty("custom", notNullValue())));
        assertThat(setMembershipResult, hasProperty("data",
                hasItem(
                        allOf(
                                hasProperty("custom", notNullValue()),
                                hasProperty("channel", allOf(
                                        allOf(
                                                hasProperty("id", is(testChannelMetadataId)),
                                                hasProperty("name", is(testChannelName)),
                                                hasProperty("description", is(testDescription)),
                                                hasProperty("custom", notNullValue()))))))));
        assertThat(getMembershipsResult, hasProperty("data",
                hasItem(
                        allOf(
                                hasProperty("custom", nullValue()),
                                hasProperty("channel", allOf(
                                        allOf(
                                                hasProperty("id", is(testChannelMetadataId)),
                                                hasProperty("name", is(testChannelName)),
                                                hasProperty("description", is(testDescription)),
                                                hasProperty("custom", nullValue()))))))));

        String userIdValue = pubNubUnderTest.getConfiguration().getUserId().getValue();
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                assertThat(pnMembershipResults, hasItem(
                        hasProperty("data", hasProperty("uuid", is(userIdValue)))));
            }
        });
    }

    @After
    public void cleanUp() {
        try {
            if (setChannelMetadataResult != null) {
                if (setMembershipResult != null) {
                    pubNubUnderTest.removeMemberships()
                            .channelMemberships(Collections.singletonList(PNChannelMembership.channel(setChannelMetadataResult.getData().getId())))
                            .sync();
                }

                pubNubUnderTest.removeChannelMetadata()
                        .channel(setChannelMetadataResult.getData().getId())
                        .sync();
            }
        } catch (PubNubException e) {
            e.printStackTrace();
        }
    }
}
