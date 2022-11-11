package com.pubnub.api.integration.objects_vsp.user;

import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.objects_vsp.user.FetchUser;
import com.pubnub.api.integration.objects.ObjectsApiBaseIT;
import com.pubnub.api.models.consumer.objects_vsp.user.RemoveUserResult;
import com.pubnub.api.models.consumer.objects_vsp.user.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class UserIT extends ObjectsApiBaseIT {
    private final String randomUserIdValue = getRandomUserIdValue();
    private UserId randomUserId;
    private final String randomName = randomName();
    private final String randomEmail = randomEmail();
    private final String randomProfileUrl = randomProfileUrl();
    private final String randomExternalId = randomExternalId();

    @Before
    public void setUp() throws Exception {
        randomUserId = new UserId(randomUserIdValue);
    }

    @Test
    public void createUserHappyPath() throws PubNubException {
        //given

        //when
        final User user = pubNubUnderTest.createUser()
                .userId(randomUserId)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUserObject())
                .includeCustom(true)
                .status(STATUS_ACTIVE)
                .type(TYPE_HUMAN)
                .sync();

        //then
        assertNotNull(user);
        assertEquals(randomUserIdValue, user.getId().getValue());
        assertEquals(randomName, user.getName());
        assertEquals(randomEmail, user.getEmail());
        assertEquals(randomProfileUrl, user.getProfileUrl());
        assertEquals(randomExternalId, user.getExternalId());
        assertNotNull(user.getCustom());
        assertEquals(STATUS_ACTIVE, user.getStatus());
        assertEquals(TYPE_HUMAN, user.getType());
//        assertNotNull(user.getUpdated());
//        assertNotNull(user.getETag());
    }

    @Test
    public void fetchUserHappyPath() throws PubNubException {
        //given
        pubNubUnderTest.createUser()
                .userId(randomUserId)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUserObject())
                .includeCustom(true)
                .status(STATUS_ACTIVE)
                .type(TYPE_HUMAN)
                .sync();

        //when
        User user = pubNubUnderTest.fetchUser()
                .userId(randomUserId)
                .includeCustom(true)
                .sync();

        assertNotNull(user);
        assertEquals(randomUserIdValue, user.getId().getValue());
        assertEquals(randomName, user.getName());
        assertEquals(randomEmail, user.getEmail());
        assertEquals(randomProfileUrl, user.getProfileUrl());
        assertEquals(randomExternalId, user.getExternalId());
        assertNotNull(user.getCustom());
        assertEquals(STATUS_ACTIVE, user.getStatus());
        assertEquals(TYPE_HUMAN, user.getType());
//        assertNotNull(user.getUpdated());
//        assertNotNull(user.getETag());
    }


    @Test
    public void removeUserHappyPath() throws PubNubException {
        // given
        pubNubUnderTest.createUser()
                .userId(randomUserId)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUserObject())
                .includeCustom(true)
                .status(STATUS_ACTIVE)
                .type(TYPE_HUMAN)
                .sync();

        //when
        RemoveUserResult removeUserResult = pubNubUnderTest.removeUser()
                .userId(randomUserId)
                .sync();

        //then
        assertNotNull(removeUserResult);
        assertEquals(HttpStatus.SC_OK, removeUserResult.getStatus());

        //verify if user not exist
        FetchUser fetchUser = pubNubUnderTest.fetchUser()
                .userId(randomUserId)
                .includeCustom(true);

        PubNubException exception = assertThrows(PubNubException.class, () -> fetchUser.sync());

        assertTrue(exception.getMessage().contains("Requested resource not found"));
    }

    @Test
    public void updateUser_passing_full_object_happyPath() throws PubNubException {
        // given
        String updatedName = "updatedName" + randomName();
        String updatedEmail = "updatedEmail" + randomEmail();
        String updatedProfileUrl = "updatedProfileUrl" + randomProfileUrl();
        String updatedExternalId = "updatedExternalId" + randomExternalId();
        Map<String, Object> updatedCustom = updatedCustomUserObject();
        String updatedStatus = "updatedStatus" + STATUS_ACTIVE;
        String updatedType = "updatedType" + TYPE_HUMAN;

        pubNubUnderTest.createUser()
                .userId(randomUserId)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUserObject())
                .includeCustom(true)
                .status(STATUS_ACTIVE)
                .type(TYPE_HUMAN)
                .sync();

        // when
        User userAfterUpdate = pubNubUnderTest.updateUser()
                .userId(randomUserId)
                .name(updatedName)
                .email(updatedEmail)
                .profileUrl(updatedProfileUrl)
                .externalId(updatedExternalId)
                .custom(updatedCustom)
                .includeCustom(true)
                .status(updatedStatus)
                .type(updatedType)
                .sync();

        // then
        assertNotNull(userAfterUpdate);
        assertEquals(randomUserIdValue, userAfterUpdate.getId().getValue());
        assertEquals(updatedName, userAfterUpdate.getName());
        assertEquals(updatedEmail, userAfterUpdate.getEmail());
        assertEquals(updatedProfileUrl, userAfterUpdate.getProfileUrl());
        assertEquals(updatedExternalId, userAfterUpdate.getExternalId());
        assertEquals("val1_updated", userAfterUpdate.getCustom().get("param1"));
        assertEquals("val2_updated", userAfterUpdate.getCustom().get("param2").toString());
        assertEquals("added", userAfterUpdate.getCustom().get("param3").toString());
        assertEquals(updatedStatus, userAfterUpdate.getStatus());
        assertEquals(updatedType, userAfterUpdate.getType());
//        assertNotNull(userAfterUpdate.getUpdated()); //waiting for https://pubnub.atlassian.net/browse/ENG-4203
//        assertNotNull(userAfterUpdate.getETag());

        User user = pubNubUnderTest.fetchUser()
                .userId(randomUserId)
                .includeCustom(true)
                .sync();

        assertNotNull(user);
        assertEquals(randomUserIdValue, user.getId().getValue());
        assertEquals(updatedName, user.getName());
        assertEquals(updatedEmail, user.getEmail());
        assertEquals(updatedProfileUrl, user.getProfileUrl());
        assertEquals(updatedExternalId, user.getExternalId());
        assertNotNull(user.getCustom());
        assertEquals(updatedStatus, user.getStatus());
        assertEquals(updatedType, user.getType());
//        assertNotNull(user.getUpdated()); waiting on https://pubnub.atlassian.net/browse/ENG-4203
//        assertNotNull(user.getETag());
    }

    @Test(expected = PubNubException.class)
    public void updateUser_should_throw_exception_when_updating_user_that_does_not_exist() throws PubNubException {
        // given
        String updatedName = "updatedName" + randomName();

        // when
        User user = pubNubUnderTest.updateUser()
                .userId(randomUserId)
                .name(updatedName)
                .sync();

        //then
        assertEquals(randomUserIdValue, user.getId());
        assertEquals(updatedName, user.getName());
    }

    @Test
    public void upsertUserHappyPath_newUserCreated() throws PubNubException {
        //given

        //when
        User user = pubNubUnderTest.upsertUser()
                .userId(randomUserId)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUserObject())
                .includeCustom(true)
                .status(STATUS_ACTIVE)
                .type(TYPE_HUMAN)
                .sync();

        //then
        assertNotNull(user);
        assertEquals(randomUserIdValue, user.getId().getValue());
        assertEquals(randomName, user.getName());
        assertEquals(randomEmail, user.getEmail());
        assertEquals(randomProfileUrl, user.getProfileUrl());
        assertEquals(randomExternalId, user.getExternalId());
        assertNotNull(user.getCustom());
        assertEquals(STATUS_ACTIVE, user.getStatus());
        assertEquals(TYPE_HUMAN, user.getType());
//        assertNotNull(user.getUpdated());
//        assertNotNull(user.getETag());
    }

    @Test
    public void upsertUserHappyPath_existingUserUpdated() throws PubNubException {
        //given
        String updatedName = "updatedName" + randomName();
        String updatedEmail = "updatedEmail" + randomEmail();
        String updatedProfileUrl = "updatedProfileUrl" + randomProfileUrl();
        String updatedExternalId = "updatedExternalId" + randomExternalId();
        Map<String, Object> updateCustom = updatedCustomUserObject();
        String updatedStatus = "updatedStatus" + STATUS_ACTIVE;
        String updatedType = "updatedType" + TYPE_HUMAN;

        pubNubUnderTest.createUser()
                .userId(randomUserId)
                .name(randomName)
                .email(randomEmail)
                .profileUrl(randomProfileUrl)
                .externalId(randomExternalId)
                .custom(customUserObject())
                .includeCustom(true)
                .status(STATUS_ACTIVE)
                .type(TYPE_HUMAN)
                .sync();

        //when
        User user = pubNubUnderTest.upsertUser()
                .userId(randomUserId)
                .name(updatedName)
                .email(updatedEmail)
                .profileUrl(updatedProfileUrl)
                .externalId(updatedExternalId)
                .custom(updateCustom)
                .includeCustom(true)
                .status(updatedStatus)
                .type(updatedType)
                .sync();

        //then
        assertNotNull(user);
        assertEquals(randomUserIdValue, user.getId().getValue());
        assertEquals(updatedName, user.getName());
        assertEquals(updatedEmail, user.getEmail());
        assertEquals(updatedProfileUrl, user.getProfileUrl());
        assertEquals(updatedExternalId, user.getExternalId());
        assertEquals("val1_updated", user.getCustom().get("param1").toString());
        assertEquals("val2_updated", user.getCustom().get("param2").toString());
        assertEquals("added", user.getCustom().get("param3").toString());
        assertEquals(updatedStatus, user.getStatus());
        assertEquals(updatedType, user.getType());
//        assertNotNull(user.getUpdated());
//        assertNotNull(user.getETag());
    }

    @After
    public void tearDown() throws Exception {
        pubNubUnderTest.removeUser()
                .userId(randomUserId)
                .sync();
    }

    private String getRandomUserIdValue() {
        return "userId" + new Random().nextInt(100000);
    }

    private String randomExternalId() {
        return UUID.randomUUID().toString();
    }

    private String randomEmail() {
        return RandomStringUtils.randomAlphabetic(6) + "@example.com";
    }

    private String randomProfileUrl() {
        return "http://" + RandomStringUtils.randomAlphabetic(5, 15) + ".com";
    }
}
