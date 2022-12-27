package com.pubnub.api.integration.objects.memberships;

import com.pubnub.api.PubNubException;
import com.pubnub.api.integration.objects.ObjectsApiBaseIT;
import com.pubnub.api.models.consumer.objects_api.membership.PNChannelMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNGetMembershipsResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNManageMembershipResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNRemoveMembershipResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNSetMembershipResult;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.pubnub.api.endpoints.objects_api.utils.Include.PNChannelDetailsLevel.CHANNEL_WITH_CUSTOM;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class MembershipIT extends ObjectsApiBaseIT {
    private static final Logger LOG = LoggerFactory.getLogger(MembershipIT.class);
    private static final String STATUS = "status";
    private static final String CHANNEL = "channel";

    public final String testChannelId1 = UUID.randomUUID().toString();
    public final String testChannelId2 = UUID.randomUUID().toString();

    private final List<PNSetMembershipResult> createdMembershipsList = new ArrayList<>();

    @Test
    public void setMembershipsHappyPath() throws PubNubException {
        //given
        final List<PNChannelMembership> channelMemberships = Arrays.asList(
                PNChannelMembership.channel(testChannelId1),
                PNChannelMembership.channelWithCustom(testChannelId2, customChannelMembershipObject()));

        //when
        final PNSetMembershipResult setMembershipResult = pubNubUnderTest.setMemberships()
                .channelMemberships(channelMemberships)
                .includeTotalCount(true)
                .includeCustom(true)
                .includeChannel(CHANNEL_WITH_CUSTOM)
                .sync();

        //then
        assertThat(setMembershipResult, allOf(
                notNullValue(),
                hasProperty(STATUS, is(HttpStatus.SC_OK))
        ));
        createdMembershipsList.add(setMembershipResult);

        assertThat(setMembershipResult, allOf(
                hasProperty("data",
                        hasItem(
                                allOf(
                                    hasProperty(CHANNEL,
                                            hasProperty("id", is(testChannelId1))),
                                    hasProperty("custom", nullValue())))),
                hasProperty("data",
                        hasItem(
                                allOf(
                                        hasProperty(CHANNEL,
                                                hasProperty("id", is(testChannelId2))),
                                        hasProperty("custom", notNullValue()))))));
    }

    @Test
    public void getMembershipsHappyPath() throws PubNubException {
        //given
        final List<PNChannelMembership> channelMemberships = Arrays.asList(
                PNChannelMembership.channel(testChannelId1),
                PNChannelMembership.channelWithCustom(testChannelId2, customChannelMembershipObject()));

        final PNSetMembershipResult setMembershipResult = pubNubUnderTest.setMemberships()
                .channelMemberships(channelMemberships)
                .includeTotalCount(true)
                .includeCustom(true)
                .includeChannel(CHANNEL_WITH_CUSTOM)
                .sync();
        createdMembershipsList.add(setMembershipResult);

        //when
        final PNGetMembershipsResult getMembershipsResult = pubNubUnderTest.getMemberships()
                .includeTotalCount(true)
                .includeCustom(true)
                .includeChannel(CHANNEL_WITH_CUSTOM)
                .sync();

        //then
        assertThat(getMembershipsResult, allOf(
                notNullValue(),
                hasProperty(STATUS, is(HttpStatus.SC_OK)),
                hasProperty("data",
                        hasItem(hasProperty(CHANNEL,
                                hasProperty("id", is(testChannelId1))))),
                hasProperty("data",
                        hasItem(hasProperty(CHANNEL,
                                hasProperty("id", is(testChannelId2)))))));
    }

    @Test
    public void removeMembershipsHappyPath() throws PubNubException {
        //given
        final List<PNChannelMembership> channelMemberships = Arrays.asList(PNChannelMembership.channel(testChannelId1),
                PNChannelMembership.channelWithCustom(testChannelId2, customChannelMembershipObject()));

        final PNSetMembershipResult setMembershipResult = pubNubUnderTest.setMemberships()
                .channelMemberships(channelMemberships)
                .includeTotalCount(true)
                .includeCustom(true)
                .includeChannel(CHANNEL_WITH_CUSTOM)
                .sync();
        createdMembershipsList.add(setMembershipResult);

        //when
        final PNRemoveMembershipResult removeMembershipResult = pubNubUnderTest.removeMemberships()
                .channelMemberships(Collections.singletonList(PNChannelMembership.channel(testChannelId2)))
                .includeTotalCount(true)
                .includeCustom(true)
                .includeChannel(CHANNEL_WITH_CUSTOM)
                .sync();

        //then
        assertThat(removeMembershipResult, allOf(
                notNullValue(),
                hasProperty(STATUS, is(HttpStatus.SC_OK)),
                hasProperty("data",
                        hasItem(hasProperty(CHANNEL,
                                hasProperty("id", is(testChannelId1))))),
                hasProperty("data",
                        hasItem(hasProperty(CHANNEL,
                                not(hasProperty("id", is(testChannelId2))))))));

    }

    @Test
    public void manageMembershipsHappyPath() throws PubNubException {
        //given
        final List<PNChannelMembership> channelMembershipsToRemove = Collections.singletonList(
                PNChannelMembership.channelWithCustom(testChannelId1, customChannelMembershipObject()));

        final PNSetMembershipResult setMembershipResult = pubNubUnderTest.setMemberships()
                .channelMemberships(channelMembershipsToRemove)
                .includeTotalCount(true)
                .includeCustom(true)
                .includeChannel(CHANNEL_WITH_CUSTOM)
                .sync();
        createdMembershipsList.add(setMembershipResult);

        final List<PNChannelMembership> channelMembershipsToSet = Collections.singletonList(
                PNChannelMembership.channelWithCustom(testChannelId2, customChannelMembershipObject()));

        //when
        final PNManageMembershipResult manageMembershipResult = pubNubUnderTest.manageMemberships()
                .set(channelMembershipsToSet)
                .remove(channelMembershipsToRemove)
                .includeTotalCount(true)
                .includeCustom(true)
                .includeChannel(CHANNEL_WITH_CUSTOM)
                .sync();

        createdMembershipsList.add(new PNSetMembershipResult(manageMembershipResult));

        //then
        assertThat(manageMembershipResult, allOf(
                notNullValue(),
                hasProperty(STATUS, is(HttpStatus.SC_OK)),
                hasProperty("data", allOf(
                        hasItem(hasProperty(CHANNEL,
                                hasProperty("id", is(channelMembershipsToSet.get(0).getChannel().getId())))),
                        not(hasItem(hasProperty(CHANNEL,
                                hasProperty("id", is(channelMembershipsToRemove.get(0).getChannel().getId())))))))));
    }

    @After
    public void cleanUp() {
        for (final PNSetMembershipResult createdMembership : createdMembershipsList) {
            try {
                final List<PNChannelMembership> channelMemberships = new ArrayList<>();
                for (final PNMembership it : createdMembership.getData()) {
                    final String id = it.getChannel().getId();
                    final PNChannelMembership pnChannelMembership = PNChannelMembership.channel(id);
                    channelMemberships.add(pnChannelMembership);
                }

                pubNubUnderTest.removeMemberships()
                        .channelMemberships(channelMemberships)
                        .sync();

            } catch (Exception e) {
                LOG.warn("Could not cleanup {}", createdMembership, e);
            }
        }
    }

    private static Map<String, Object> customChannelMembershipObject() {
        final Map<String, Object> customMap = new HashMap<>();
        customMap.putIfAbsent("membership_param1", "val1");
        customMap.putIfAbsent("membership_param2", "val2");
        return customMap;
    }
}
