package com.pubnub.api.integration.objects.uuid;

import com.pubnub.api.PubNubException;
import com.pubnub.api.integration.objects.ObjectsApiBaseIT;
import com.pubnub.api.models.consumer.objects_api.uuid.PNGetAllUUIDMetadataResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNGetUUIDMetadataResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNRemoveUUIDMetadataResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNSetUUIDMetadataResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
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

public class UUIDMetadataIT extends ObjectsApiBaseIT {
    private static final Logger LOG = LoggerFactory.getLogger(UUIDMetadataIT.class);
    private static final int NUMBER_OF_RANDOM_TEST_UUIDS = 10;
    private static final int FETCH_LIMIT = 3;

    private final List<String> randomTestUUIDs = randomTestUUIDs();
    private final String randomTestUUID = randomTestUUIDs.get(0);

    private final List<PNSetUUIDMetadataResult> createdUUIDMetadataList = new ArrayList<>();
    private final String randomName = randomName();
    private final String randomEmail = randomEmail();
    private final String randomProfileUrl = randomProfileUrl();
    private final String randomExternalId = randomExternalId();
    private final String statusValue = "active";
    private final String typeValue = "gold";

    @Test
    public void setUUIDHappyPath() throws PubNubException {
        //given

        //when
        final PNSetUUIDMetadataResult setUUIDMetadataResult = pubNubUnderTest.setUUIDMetadata()
                .uuid(randomTestUUID)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUUIDObject())
                .includeCustom(true)
                .status(statusValue)
                .type(typeValue)
                .sync();

        //then
        assertNotNull(setUUIDMetadataResult);
        assertEquals(HttpStatus.SC_OK, setUUIDMetadataResult.getStatus());
        createdUUIDMetadataList.add(setUUIDMetadataResult);
        assertEquals(randomTestUUID, setUUIDMetadataResult.getData().getId());
        assertEquals(randomName, setUUIDMetadataResult.getData().getName());
        assertEquals(randomEmail, setUUIDMetadataResult.getData().getEmail());
        assertEquals(randomProfileUrl, setUUIDMetadataResult.getData().getProfileUrl());
        assertEquals(randomExternalId, setUUIDMetadataResult.getData().getExternalId());
        assertNotNull(setUUIDMetadataResult.getData().getCustom());
        assertEquals(statusValue, setUUIDMetadataResult.getData().getStatus());
        assertEquals(typeValue, setUUIDMetadataResult.getData().getType());
    }

    @Test
    public void getUUIDHappyPath() throws PubNubException {
        //given
        final PNSetUUIDMetadataResult setUUIDMetadataResult = pubNubUnderTest.setUUIDMetadata()
                .uuid(randomTestUUID)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUUIDObject())
                .includeCustom(true)
                .status(statusValue)
                .type(typeValue)
                .sync();
        createdUUIDMetadataList.add(setUUIDMetadataResult);

        //when
        final PNGetUUIDMetadataResult getUUIDMetadataResult = pubNubUnderTest.getUUIDMetadata()
                .uuid(randomTestUUID)
                .includeCustom(true)
                .sync();

        //then
        assertNotNull(getUUIDMetadataResult);
        assertEquals(HttpStatus.SC_OK, getUUIDMetadataResult.getStatus());

        assertEquals(randomTestUUID, getUUIDMetadataResult.getData().getId());
        assertEquals(setUUIDMetadataResult.getData().getName(), getUUIDMetadataResult.getData().getName());
        assertEquals(setUUIDMetadataResult.getData().getEmail(), getUUIDMetadataResult.getData().getEmail());
        assertEquals(setUUIDMetadataResult.getData().getProfileUrl(), getUUIDMetadataResult.getData().getProfileUrl());
        assertEquals(setUUIDMetadataResult.getData().getExternalId(), getUUIDMetadataResult.getData().getExternalId());
        assertNotNull(getUUIDMetadataResult.getData().getCustom());
        assertEquals(statusValue, getUUIDMetadataResult.getData().getStatus());
        assertEquals(typeValue, getUUIDMetadataResult.getData().getType());
    }

    @Test
    public void getAllUUIDHappyPath() throws PubNubException {
        //given
        for (String testUUID : randomTestUUIDs) {
            final PNSetUUIDMetadataResult setUUIDMetadataResult = pubNubUnderTest.setUUIDMetadata()
                    .uuid(testUUID)
                    .name(randomName)
                    .email(randomEmail)
                    .profileUrl(randomProfileUrl)
                    .externalId(randomExternalId)
                    .custom(customUUIDObject())
                    .includeCustom(true)
                    .sync();
            createdUUIDMetadataList.add(setUUIDMetadataResult);
        }

        //when
        final PNGetAllUUIDMetadataResult getAllUUIDMetadataResult = pubNubUnderTest.getAllUUIDMetadata()
                .includeCustom(true)
                .includeTotalCount(true)
                .limit(FETCH_LIMIT)
                .sync();

        //then
        assertThat(getAllUUIDMetadataResult, allOf(
                notNullValue(),
                hasProperty("status", equalTo(HttpStatus.SC_OK)),
                hasProperty("data", allOf(
                        not(empty()),
                        hasSize(FETCH_LIMIT))),
                hasProperty("totalCount", greaterThanOrEqualTo(NUMBER_OF_RANDOM_TEST_UUIDS)),
                hasProperty("next", not(isEmptyOrNullString())),
                hasProperty("prev", isEmptyOrNullString())));
    }

    @Test
    public void removeUUIDHappyPath() throws PubNubException {
        //given
        final PNSetUUIDMetadataResult setUUIDMetadataResult = pubNubUnderTest.setUUIDMetadata()
                .uuid(randomTestUUID)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUUIDObject())
                .includeCustom(true)
                .status(statusValue)
                .type(typeValue)
                .sync();
        createdUUIDMetadataList.add(setUUIDMetadataResult);

        //when
        final PNRemoveUUIDMetadataResult removeUUIDMetadataResult = pubNubUnderTest.removeUUIDMetadata()
                .uuid(randomTestUUID)
                .sync();

        //then
        assertNotNull(removeUUIDMetadataResult);
        assertEquals(HttpStatus.SC_OK, removeUUIDMetadataResult.getStatus());
    }

    @After
    public void cleanUp() {
        createdUUIDMetadataList.forEach(pnSetUUIDMetadataResult -> {
            try {
                pubNubUnderTest.removeUUIDMetadata()
                        .uuid(pnSetUUIDMetadataResult.getData().getId())
                        .sync();
            } catch (Exception e) {
                LOG.warn("Could not cleanup {}", pnSetUUIDMetadataResult, e);
            }
        });
    }

    private Map<String, Object> customUUIDObject() {
        return new HashMap<String, Object>() {
            {
                putIfAbsent("uuid_param1", "val1");
                putIfAbsent("uuid_param2", "val2");
            }
        };
    }

    private String randomExternalId() {
        return UUID.randomUUID().toString();
    }

    private String randomEmail() {
        return RandomStringUtils.randomAlphabetic(6) + "@example.com";
    }

    private String randomName() {
        return RandomStringUtils.randomAlphabetic(5, 10) + " " + RandomStringUtils.randomAlphabetic(5, 10);
    }

    private String randomProfileUrl() {
        return "http://" + RandomStringUtils.randomAlphabetic(5, 15) + ".com";
    }

    private static List<String> randomTestUUIDs() {
        final List<String> uuids = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_RANDOM_TEST_UUIDS; i++) {
            uuids.add(UUID.randomUUID().toString());
        }
        return uuids;
    }
}
