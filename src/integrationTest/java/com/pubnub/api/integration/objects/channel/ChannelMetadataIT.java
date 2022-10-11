package com.pubnub.api.integration.objects.channel;

import com.pubnub.api.PubNubException;
import com.pubnub.api.integration.objects.ObjectsApiBaseIT;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadata;
import com.pubnub.api.models.consumer.objects_api.channel.PNGetAllChannelsMetadataResult;
import com.pubnub.api.models.consumer.objects_api.channel.PNGetChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.channel.PNRemoveChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.channel.PNSetChannelMetadataResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ChannelMetadataIT extends ObjectsApiBaseIT {
    private final static Logger LOG = LoggerFactory.getLogger(ChannelMetadataIT.class);
    private static final int NUMBER_OF_RANDOM_TEST_NAMES = 10;
    private static final int FETCH_LIMIT = 3;

    private final List<String> randomChannelMetadataIds = randomChannelMetadataIds();
    private final String randomChannelMetadataId = randomChannelMetadataIds.get(0);

    private final String randomDescription = randomDescription();
    private final String STATUS = "active";
    private final String TYPE = "chat";

    private final List<PNSetChannelMetadataResult> createdChannelMetadataList = new ArrayList<>();

    @Test
    public void setChannelHappyPath() throws PubNubException {
        //given

        //when
        final PNSetChannelMetadataResult setChannelMetadataResult = pubNubUnderTest.setChannelMetadata()
                .channel(randomChannelMetadataId)
                .description(randomDescription)
                .custom(customChannelObject())
                .includeCustom(true)
                .status(STATUS)
                .type(TYPE)
                .sync();

        //then
        assertNotNull(setChannelMetadataResult);
        assertEquals(HttpStatus.SC_OK, setChannelMetadataResult.getStatus());
        createdChannelMetadataList.add(setChannelMetadataResult);
        assertEquals(randomChannelMetadataId, setChannelMetadataResult.getData().getId());
        assertEquals(setChannelMetadataResult.getData().getDescription(),
                setChannelMetadataResult.getData().getDescription());
        assertNotNull(setChannelMetadataResult.getData().getCustom());
        assertEquals(STATUS, setChannelMetadataResult.getData().getStatus());
        assertEquals(TYPE, setChannelMetadataResult.getData().getType());
    }

    @Test
    public void getChannelHappyPath() throws PubNubException {
        //given
        final PNSetChannelMetadataResult setChannelMetadataResult = pubNubUnderTest.setChannelMetadata()
                .channel(randomChannelMetadataId)
                .description(randomDescription)
                .custom(customChannelObject())
                .includeCustom(true)
                .status(STATUS)
                .type(TYPE)
                .sync();
        createdChannelMetadataList.add(setChannelMetadataResult);

        //when
        final PNGetChannelMetadataResult getChannelMetadataResult = pubNubUnderTest.getChannelMetadata()
                .channel(randomChannelMetadataId)
                .includeCustom(true)
                .sync();

        //then
        assertNotNull(getChannelMetadataResult);
        assertEquals(HttpStatus.SC_OK, getChannelMetadataResult.getStatus());

        assertEquals(randomChannelMetadataId, getChannelMetadataResult.getData().getId());
        assertEquals(setChannelMetadataResult.getData().getName(), getChannelMetadataResult.getData().getName());
        assertEquals(setChannelMetadataResult.getData().getDescription(),
                getChannelMetadataResult.getData().getDescription());
        assertNotNull(setChannelMetadataResult.getData().getCustom());
        assertEquals(STATUS, setChannelMetadataResult.getData().getStatus());
        assertEquals(TYPE, setChannelMetadataResult.getData().getType());
    }

    @Test
    public void getAllChannelHappyPath() throws PubNubException {
        //given

        for (String testChannelName: randomChannelMetadataIds) {
            final PNSetChannelMetadataResult setChannelMetadataResult = pubNubUnderTest.setChannelMetadata()
                    .channel(testChannelName)
                    .description(randomDescription)
                    .custom(customChannelObject())
                    .includeCustom(true)
                    .sync();
            createdChannelMetadataList.add(setChannelMetadataResult);
        }

        //when
        final PNGetAllChannelsMetadataResult getAllChannelsMetadataResult = pubNubUnderTest
                .getAllChannelsMetadata()
                .includeCustom(true)
                .includeTotalCount(true)
                .limit(FETCH_LIMIT)
                .sync();

        //then
        assertThat(getAllChannelsMetadataResult, allOf(
                notNullValue(),
                hasProperty("status", equalTo(HttpStatus.SC_OK)),
                hasProperty("data", allOf(
                        not(empty()),
                        hasSize(FETCH_LIMIT))),
                hasProperty("totalCount", greaterThanOrEqualTo(NUMBER_OF_RANDOM_TEST_NAMES)),
                hasProperty("next", not(isEmptyOrNullString())),
                hasProperty("prev", isEmptyOrNullString())));
    }

    @Test
    public void getAllChannelsTraversingPagesHappyPath() throws PubNubException {
        //given

        for (String testChannelMetadataId: randomChannelMetadataIds) {
            final PNSetChannelMetadataResult setChannelMetadataResult = pubNubUnderTest.setChannelMetadata()
                    .channel(testChannelMetadataId)
                    .description(randomDescription)
                    .custom(customChannelObject())
                    .includeCustom(true)
                    .sync();
            createdChannelMetadataList.add(setChannelMetadataResult);
        }

        //when
        final PNGetAllChannelsMetadataResult firstGetAllChannelsMetadataResult = pubNubUnderTest
                .getAllChannelsMetadata()
                .includeCustom(true)
                .includeTotalCount(true)
                .limit(FETCH_LIMIT)
                .sync();


        final PNGetAllChannelsMetadataResult secondGetAllChannelsMetadataResult = pubNubUnderTest
                .getAllChannelsMetadata()
                .includeCustom(true)
                .includeTotalCount(true)
                .limit(FETCH_LIMIT)
                .page(firstGetAllChannelsMetadataResult.nextPage())
                .sync();

        final PNGetAllChannelsMetadataResult firstAgainGetAllChannelsMetadataResult = pubNubUnderTest
                .getAllChannelsMetadata()
                .includeCustom(true)
                .includeTotalCount(true)
                .limit(FETCH_LIMIT)
                .page(secondGetAllChannelsMetadataResult.nextPage()) //to illustrate that last overrides
                .page(secondGetAllChannelsMetadataResult.previousPage())
                .sync();

        //then
        final List<String> firstResultIds = extractChannelIds(firstGetAllChannelsMetadataResult);
        final List<String> secondResultIds = extractChannelIds(secondGetAllChannelsMetadataResult);
        final List<String> thirdResultIds = extractChannelIds(firstAgainGetAllChannelsMetadataResult);

        assertThat(firstResultIds, allOf(
                containsInAnyOrder(thirdResultIds.toArray()),
                not(containsInAnyOrder(secondResultIds.toArray()))));
    }

    @Test
    public void removeChannelHappyPath() throws PubNubException {
        //given
        final PNSetChannelMetadataResult setChannelMetadataResult = pubNubUnderTest.setChannelMetadata()
                .channel(randomChannelMetadataId)
                .description(randomDescription)
                .custom(customChannelObject())
                .includeCustom(true)
                .status(STATUS)
                .type(TYPE)
                .sync();
        createdChannelMetadataList.add(setChannelMetadataResult);

        //when
        final PNRemoveChannelMetadataResult removeChannelMetadataResult = pubNubUnderTest
                .removeChannelMetadata()
                .channel(randomChannelMetadataId)
                .sync();

        //then
        assertNotNull(removeChannelMetadataResult);
        assertEquals(HttpStatus.SC_OK, removeChannelMetadataResult.getStatus());
    }

    @After
    public void cleanUp() {
        createdChannelMetadataList.forEach(setChannelMetadataResult -> {
            try {
                pubNubUnderTest.removeChannelMetadata()
                        .channel(setChannelMetadataResult.getData().getId())
                        .sync();
            }
            catch (Exception e) {
                LOG.warn("Could not cleanup {}", setChannelMetadataResult, e);
            }
        });
    }

    private Map<String, Object> customChannelObject() {
        final Map<String, Object> customMap = new HashMap<>();
        customMap.putIfAbsent("channel_param1", "val1");
        customMap.putIfAbsent("channel_param2", "val2");
        return customMap;
    }

    private static String randomDescription() {
        return RandomStringUtils.randomAlphabetic(50, 160);
    }

    private static String randomChannelName() {
        return RandomStringUtils.randomAlphabetic(5, 10);
    }

    private static List<String> randomChannelMetadataIds() {
        final List<String> uuids = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_RANDOM_TEST_NAMES; i++) {
            uuids.add(randomChannelName());
        }
        return uuids;
    }

    @NotNull
    private static List<String> extractChannelIds(final PNGetAllChannelsMetadataResult pnGetAllChannelsMetadataResult) {
        final List<String> ids = new ArrayList<>();
        for (PNChannelMetadata pnChannelMetadata: pnGetAllChannelsMetadataResult.getData()) {
            ids.add(pnChannelMetadata.getId());
        }
        return ids;
    }

}
