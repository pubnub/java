package com.pubnub.api.integration.objects.members;

import com.pubnub.api.PubNubException;
import com.pubnub.api.integration.objects.ObjectsApiBaseIT;
import com.pubnub.api.models.consumer.objects_api.member.PNGetChannelMembersResult;
import com.pubnub.api.models.consumer.objects_api.member.PNSetChannelMembersResult;
import com.pubnub.api.models.consumer.objects_api.member.PNUUID;
import com.pubnub.api.models.consumer.objects_api.uuid.PNSetUUIDMetadataResult;
import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.pubnub.api.endpoints.objects_api.utils.Include.PNUUIDDetailsLevel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class CustomMetadataInMembersPropagationIT extends ObjectsApiBaseIT {
    private final String testUUID = UUID.randomUUID().toString();
    private final String testChannelMetadataId = UUID.randomUUID().toString();
    private final String testExternalId = UUID.randomUUID().toString();

    //Double Brace initialization is done on purpose here to test that GSON can handle that
    private final Map<String, Object> testCustomObjectForUUIDMetadata = new HashMap() {{
        put("key1", "val1");
        put("key2", "val2");
    }};

    private final Map<String, Object> testCustomObjectForMembers = new HashMap() {{
        put("key3", "val3");
        put("key4", "val4");
    }};

    private PNSetUUIDMetadataResult setUUIDMetadataResult;
    private PNSetChannelMembersResult setChannelMembersResult;

    @SuppressWarnings("unchecked")
    @Test
    public void setMembersCustomHappyPath() throws PubNubException {
        final String testProfileUrl = "http://example.com";
        final String testName = "Test Name";
        final String testEmail = "foo@example.com";

        setUUIDMetadataResult = pubNubUnderTest.setUUIDMetadata()
                .uuid(testUUID)
                .name(testName)
                .email(testEmail)
                .externalId(testExternalId)
                .profileUrl(testProfileUrl)
                .custom(testCustomObjectForUUIDMetadata)
                .includeCustom(true)
                .sync();

        setChannelMembersResult = pubNubUnderTest.setChannelMembers()
                .channel(testChannelMetadataId)
                .uuids(Collections.singletonList(
                        PNUUID.uuidWithCustom(setUUIDMetadataResult.getData().getId(),
                                testCustomObjectForMembers)))
                .includeCustom(true)
                .includeUUID(PNUUIDDetailsLevel.UUID_WITH_CUSTOM)
                .sync();

        final PNGetChannelMembersResult getChannelMembersResult = pubNubUnderTest.getChannelMembers()
                .channel(testChannelMetadataId)
                .includeCustom(false)
                .includeUUID(PNUUIDDetailsLevel.UUID)
                .sync();

        assertThat(setUUIDMetadataResult,
                hasProperty("data", hasProperty("custom", notNullValue())));

        assertThat(setChannelMembersResult, hasProperty("data",
                hasItem(
                        allOf(
                                hasProperty("custom", notNullValue()),
                                hasProperty("uuid", allOf(
                                        allOf(
                                                hasProperty("id", is(testUUID)),
                                                hasProperty("name", is(testName)),
                                                hasProperty("email", is(testEmail)),
                                                hasProperty("externalId", is(testExternalId)),
                                                hasProperty("profileUrl", is(testProfileUrl)),
                                                hasProperty("custom", notNullValue()))))))));
        assertThat(getChannelMembersResult, hasProperty("data",
                hasItem(
                        allOf(
                                hasProperty("custom", nullValue()),
                                hasProperty("uuid", allOf(
                                        allOf(
                                                hasProperty("id", is(testUUID)),
                                                hasProperty("name", is(testName)),
                                                hasProperty("email", is(testEmail)),
                                                hasProperty("externalId", is(testExternalId)),
                                                hasProperty("profileUrl", is(testProfileUrl)),
                                                hasProperty("custom", nullValue()))))))));
    }

    @After
    public void cleanUp() {
        try {
            if (setUUIDMetadataResult != null) {
                if (setChannelMembersResult != null) {
                    pubNubUnderTest.removeChannelMembers()
                            .channel(testChannelMetadataId)
                            .uuids(Collections.singleton(PNUUID.uuid(setUUIDMetadataResult.getData().getId())))
                            .sync();
                }

                pubNubUnderTest.removeUUIDMetadata()
                        .uuid(setUUIDMetadataResult.getData().getId())
                        .sync();
            }
        } catch (PubNubException e) {
            e.printStackTrace();
        }
    }
}
