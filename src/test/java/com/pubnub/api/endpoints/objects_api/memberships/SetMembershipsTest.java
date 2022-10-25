package com.pubnub.api.endpoints.objects_api.memberships;

import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.membership.PNChannelMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembership;
import com.pubnub.api.models.server.objects_api.PatchMembershipPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import com.pubnub.api.services.UUIDMetadataService;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pubnub.api.models.consumer.objects_api.membership.PNChannelMembership.ChannelWithCustom;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SetMembershipsTest extends BaseObjectApiTest {
    @Mock protected UUIDMetadataService uuidMetadataServiceMock;
    @Captor private ArgumentCaptor<PatchMembershipPayload> patchMembershipPayloadArgumentCaptor;

    @Before
    public void retrofitMocks() {
        when(retrofitManagerMock.getUuidMetadataService()).thenReturn(uuidMetadataServiceMock);
        when(uuidMetadataServiceMock.patchMembership(eq(testSubscriptionKey), eq(testUUID), any(), any()))
                .thenAnswer(mockRetrofitSuccessfulCall(() -> {
                    final EntityArrayEnvelope<PNMembership> envelope = new EntityArrayEnvelope<>();
                    return envelope;
                }));
        when(uuidMetadataServiceMock.getMemberships(eq(testSubscriptionKey), eq(testUUID), any()))
                .thenAnswer(mockRetrofitSuccessfulCall(() -> {
                    final EntityArrayEnvelope<PNMembership> envelope = new EntityArrayEnvelope<>();
                    return envelope;
                }));
    }

    @Test
    public void setMembershipTest() throws PubNubException {
        //given
        final Map<String, Object> customObject = customObject();
        final Collection<PNChannelMembership> channelMemberships = Arrays.asList(
                PNChannelMembership.channel(randomChannelId()),
                PNChannelMembership.channelWithCustom(randomChannelId(), customObject));
        final SetMemberships.Builder setMembershipsUnderTest = SetMemberships.builder(pubNubMock,
                telemetryManagerMock, retrofitManagerMock, new TokenManager());

        //when
        setMembershipsUnderTest
                .channelMemberships(channelMemberships)
                .sync();

        //then
        verify(uuidMetadataServiceMock, times(1))
                .patchMembership(eq(testSubscriptionKey), eq(testUUID),
                        patchMembershipPayloadArgumentCaptor.capture(), any());
        final PatchMembershipPayload capturedPatchMembershipPayload = patchMembershipPayloadArgumentCaptor.getValue();
        final List<String> channelIdsToSetPassedToService = extractChannelIds(capturedPatchMembershipPayload.getSet());

        final List<String> expectedChannelIds = extractChannelIds(channelMemberships);

        final List<Map<String, Object>> passedCustomObjects = new ArrayList<>();
        for (final PNChannelMembership it : capturedPatchMembershipPayload.getSet()) {
            if (it instanceof ChannelWithCustom) {
                final ChannelWithCustom channelWithCustom = (ChannelWithCustom) it;
                final Object custom = channelWithCustom.getCustom();
                if (custom instanceof Map) {
                    final Map<String, Object> stringStringMap = (Map<String, Object>) custom;
                    passedCustomObjects.add(stringStringMap);
                }
            }
        }

        assertThat(passedCustomObjects, hasItems(customObject));
        assertThat(capturedPatchMembershipPayload.getDelete(), is(empty()));
        assertThat(channelIdsToSetPassedToService, containsInAnyOrder(expectedChannelIds.toArray()));
    }

    @Test
    public void getMembershipTest() throws PubNubException {
        //given
        final GetMemberships getMembershipsUnderTest = GetMemberships.create(pubNubMock,
                telemetryManagerMock, retrofitManagerMock, new TokenManager());

        //when
        getMembershipsUnderTest
                .sync();

        //then
        verify(uuidMetadataServiceMock, times(1))
                .getMemberships(eq(testSubscriptionKey), eq(testUUID), any());
    }

    @Test
    public void manageMembershipTest() throws PubNubException {
        //given
        final Map<String, Object> customObject = customObject();
        final Collection<PNChannelMembership> channelMembershipsToSet = Arrays.asList(
                PNChannelMembership.channel(randomChannelId()),
                PNChannelMembership.channelWithCustom(randomChannelId(), customObject));
        final Collection<PNChannelMembership> channelMembershipsToRemove = Arrays.asList(
                PNChannelMembership.channel(randomChannelId()),
                PNChannelMembership.channelWithCustom(randomChannelId(), customObject));

        final ManageMemberships.Builder manageMembershipsUnderTest = ManageMemberships.builder(pubNubMock,
                telemetryManagerMock,
                retrofitManagerMock, new TokenManager());

        //when
        manageMembershipsUnderTest
                .set(channelMembershipsToSet)
                .remove(channelMembershipsToRemove)
                .sync();

        //then
        verify(uuidMetadataServiceMock, times(1))
                .patchMembership(eq(testSubscriptionKey), eq(testUUID),
                        patchMembershipPayloadArgumentCaptor.capture(), any());
        final PatchMembershipPayload capturedPatchMembershipPayload = patchMembershipPayloadArgumentCaptor.getValue();
        final List<String> channelIdsToSetPassedToService = extractChannelIds(capturedPatchMembershipPayload.getSet());
        final List<String> channelIdsToDeletePassedToService = extractChannelIds(capturedPatchMembershipPayload.getDelete());

        assertThat(channelIdsToSetPassedToService, containsInAnyOrder(extractChannelIds(channelMembershipsToSet).toArray()));
        assertThat(channelIdsToDeletePassedToService, containsInAnyOrder(extractChannelIds(channelMembershipsToRemove).toArray()));
    }

    @Test
    public void removeMembershipTest() throws PubNubException {
        //given
        final Collection<PNChannelMembership> channelMemberships = Arrays.asList(
                PNChannelMembership.channel(randomChannelId()),
                PNChannelMembership.channel(randomChannelId()));

        final RemoveMemberships.Builder removeMembershipsUnderTest = RemoveMemberships
                .builder(pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());

        //when
        removeMembershipsUnderTest
                .channelMemberships(channelMemberships)
                .sync();

        //then
        verify(uuidMetadataServiceMock, times(1))
                .patchMembership(eq(testSubscriptionKey), eq(testUUID),
                        patchMembershipPayloadArgumentCaptor.capture(), any());
        final PatchMembershipPayload capturedPatchMembershipPayload = patchMembershipPayloadArgumentCaptor.getValue();
        final List<String> channelIdsPassedToService = extractChannelIds(capturedPatchMembershipPayload.getDelete());

        final List<String> expectedChannelIds = extractChannelIds(channelMemberships);

        assertThat(capturedPatchMembershipPayload.getSet(), is(empty()));
        assertThat(channelIdsPassedToService, containsInAnyOrder(expectedChannelIds.toArray()));
    }

    @NotNull
    private static List<String> extractChannelIds(final Collection<PNChannelMembership> channelMemberships) {
        final List<String> channelIdsPassedToService = new ArrayList<>();
        for (final PNChannelMembership channelMembership : channelMemberships) {
            final String id = channelMembership.getChannel().getId();
            channelIdsPassedToService.add(id);
        }
        return channelIdsPassedToService;
    }

    private static Map<String, Object> customObject() {
        final Map<String, Object> custom = new HashMap<>();
        custom.put("key1", RandomStringUtils.random(10));
        custom.put("key2", RandomStringUtils.random(10));
        return custom;
    }

    private static String randomChannelId() {
        return RandomStringUtils.randomAlphabetic(10);
    }


}