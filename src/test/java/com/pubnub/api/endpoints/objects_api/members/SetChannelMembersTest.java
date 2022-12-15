package com.pubnub.api.endpoints.objects_api.members;

import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.member.PNMembers;
import com.pubnub.api.models.consumer.objects_api.member.PNUUID;
import com.pubnub.api.models.consumer.objects_api.member.PNUUID.UUIDWithCustom;
import com.pubnub.api.models.server.objects_api.PatchMemberPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import com.pubnub.api.services.ChannelMetadataService;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
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
import java.util.UUID;

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

public class SetChannelMembersTest extends BaseObjectApiTest {
    @Mock protected ChannelMetadataService channelMetadataServiceMock;
    @Captor private ArgumentCaptor<PatchMemberPayload> patchMembersPayloadArgumentCaptor;

    private final String testChannel = UUID.randomUUID().toString();

    @Before
    public void retrofitMocks() {
        when(retrofitManagerMock.getChannelMetadataService()).thenReturn(channelMetadataServiceMock);
        when(channelMetadataServiceMock.patchMembers(eq(testSubscriptionKey), eq(testChannel), any(), any()))
                .thenAnswer(mockRetrofitSuccessfulCall(() -> {
                    final EntityArrayEnvelope<PNMembers> envelope = new EntityArrayEnvelope<>();
                    return envelope;
                }));
        when(channelMetadataServiceMock.getMembers(eq(testSubscriptionKey), eq(testChannel), any()))
                .thenAnswer(mockRetrofitSuccessfulCall(() -> {
                    final EntityArrayEnvelope<PNMembers> envelope = new EntityArrayEnvelope<>();
                    return envelope;
                }));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setChanelMembersTest() throws PubNubException {
        //given
        final Map<String, Object> customObject = customObject();
        final Collection<PNUUID> uuids = Arrays.asList(
                PNUUID.uuid(randomUUIDId()),
                PNUUID.uuidWithCustom(randomUUIDId(), customObject));
        final SetChannelMembers.Builder setChannelMembersUnderTest = SetChannelMembers.builder(pubNubMock, telemetryManagerMock,
                retrofitManagerMock,
                new TokenManager());

        //when
        setChannelMembersUnderTest
                .channel(testChannel)
                .uuids(uuids)
                .sync();

        //then
        verify(channelMetadataServiceMock, times(1)).patchMembers(eq(testSubscriptionKey),
                eq(testChannel), patchMembersPayloadArgumentCaptor.capture(), any());
        final PatchMemberPayload capturedPatchMemberPayload = patchMembersPayloadArgumentCaptor.getValue();
        final List<String> uuidsPassedToService = extractUUIDs(capturedPatchMemberPayload.getSet());

        final List<String> expectedUUIDs = extractUUIDs(uuids);

        final List<Map<String, Object>> passedCustomObjects = new ArrayList<>();
        for (final PNUUID it : capturedPatchMemberPayload.getSet()) {
            if (it instanceof UUIDWithCustom) {
                final UUIDWithCustom uuidWithCustom = (UUIDWithCustom) it;
                final Object custom = uuidWithCustom.getCustom();
                if (custom instanceof Map) {
                    final Map<String, Object> stringStringMap = (Map<String, Object>) custom;
                    passedCustomObjects.add(stringStringMap);
                }
            }
        }

        assertThat(passedCustomObjects, hasItems(customObject));
        assertThat(capturedPatchMemberPayload.getDelete(), is(empty()));
        assertThat(uuidsPassedToService, containsInAnyOrder(expectedUUIDs.toArray()));
    }

    @NotNull
    private List<String> extractUUIDs(final Collection<PNUUID> set) {
        final List<String> uuidsPassedToService = new ArrayList<>();
        for (final PNUUID uuids : set) {
            final String id = uuids.getUuid().getId();
            uuidsPassedToService.add(id);
        }
        return uuidsPassedToService;
    }

    @Test
    public void getChannelMembersTest() throws PubNubException {
        //given
        final GetChannelMembers.Builder getChannelMembersUnderTest = GetChannelMembers
                .builder(pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());

        //when
        getChannelMembersUnderTest
                .channel(testChannel)
                .sync();

        //then
        verify(channelMetadataServiceMock, times(1))
                .getMembers(eq(testSubscriptionKey), eq(testChannel), any());
    }

    @Test
    public void manageChannelMembersTest() throws PubNubException {
        //given
        final Map<String, Object> customObject = customObject();
        final Collection<PNUUID> uuidsToSet = Arrays.asList(
                PNUUID.uuid(randomUUIDId()),
                PNUUID.uuidWithCustom(randomUUIDId(), customObject));
        final Collection<PNUUID> uuidsToRemove = Arrays.asList(
                PNUUID.uuid(randomUUIDId()),
                PNUUID.uuidWithCustom(randomUUIDId(), customObject));

        final ManageChannelMembers.Builder manageChannelMembersUnderTest = ManageChannelMembers.builder(pubNubMock,
                telemetryManagerMock,
                retrofitManagerMock, new TokenManager());

        //when
        manageChannelMembersUnderTest
                .channel(testChannel)
                .set(uuidsToSet)
                .remove(uuidsToRemove)
                .sync();

        //then
        verify(channelMetadataServiceMock, times(1)).patchMembers(eq(testSubscriptionKey),
                eq(testChannel), patchMembersPayloadArgumentCaptor.capture(), any());
        final PatchMemberPayload capturedPatchMemberPayload = patchMembersPayloadArgumentCaptor.getValue();
        final List<String> uuidsToSetPassedToService = extractUUIDs(capturedPatchMemberPayload.getSet());
        final List<String> uuidsToDeletePassedToService = extractUUIDs(capturedPatchMemberPayload.getDelete());

        assertThat(uuidsToSetPassedToService, containsInAnyOrder(extractUUIDs(uuidsToSet).toArray()));
        assertThat(uuidsToDeletePassedToService, containsInAnyOrder(extractUUIDs(uuidsToRemove).toArray()));
    }

    @Test
    public void removeChannelMembersTest() throws PubNubException {
        //given
        final Collection<PNUUID> uuids = Arrays.asList(
                PNUUID.uuid(randomUUIDId()),
                PNUUID.uuid(randomUUIDId()));

        final RemoveChannelMembers.Builder removeChannelMembersUnderTest = RemoveChannelMembers.builder(pubNubMock,
                telemetryManagerMock, retrofitManagerMock, new TokenManager());

        //when
        removeChannelMembersUnderTest
                .channel(testChannel)
                .uuids(uuids)
                .sync();

        //then
        verify(channelMetadataServiceMock, times(1)).patchMembers(eq(testSubscriptionKey),
                eq(testChannel), patchMembersPayloadArgumentCaptor.capture(), any());
        final PatchMemberPayload capturedPatchMemberPayload = patchMembersPayloadArgumentCaptor.getValue();
        final List<String> uuidsPassedToService = extractUUIDs(capturedPatchMemberPayload.getDelete());

        final List<String> expectedUUIDs = extractUUIDs(uuids);

        assertThat(capturedPatchMemberPayload.getSet(), is(empty()));
        assertThat(uuidsPassedToService, containsInAnyOrder(expectedUUIDs.toArray()));
    }


    private static Map<String, Object> customObject() {
        final Map<String, Object> custom = new HashMap<>();
        custom.put("key1", RandomStringUtils.random(10));
        custom.put("key2", RandomStringUtils.random(10));
        return custom;
    }

    private static String randomUUIDId() {
        return RandomStringUtils.randomAlphabetic(10);
    }
}