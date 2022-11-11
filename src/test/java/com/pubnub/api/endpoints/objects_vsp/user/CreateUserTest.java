package com.pubnub.api.endpoints.objects_vsp.user;

import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.user.User;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.models.server.objects_vsp.user.CreateUserPayload;
import com.pubnub.api.services.vsp.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CreateUserTest extends BaseObjectApiTest {
    private CreateUser objectUnderTest;
    @Mock
    private UserService userServiceMock;
    @Mock
    private Call<EntityEnvelope<User>> call;


    @Before
    public void setUp() throws IOException, PubNubException {
        objectUnderTest = CreateUser.create(pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());

        when(retrofitManagerMock.getUserService()).thenReturn(userServiceMock);
        when(userServiceMock.createUser(eq(testSubscriptionKey), eq(testUserIdValue), any(), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new EntityEnvelope<>()));
    }

    @Test
    public void can_createUser_with_userId() throws PubNubException {
        //given

        //when
        objectUnderTest.userId(new UserId(testUserIdValue)).sync();

        //then
        verify(userServiceMock, times(1)).createUser(eq(testSubscriptionKey), eq(testUserIdValue), any(), any());
    }

    @Test
    public void can_createUser_with_name() throws PubNubException {
        //given
        final String userName = RandomStringUtils.randomAlphabetic(20);

        //when
        objectUnderTest.name(userName).sync();

        //then
        ArgumentCaptor<CreateUserPayload> createUserPayloadCaptor = ArgumentCaptor.forClass(CreateUserPayload.class);
        verify(userServiceMock).createUser(eq(testSubscriptionKey), eq(testUserIdValue), createUserPayloadCaptor.capture(), any());

        final CreateUserPayload capturedCreateUserPayload = createUserPayloadCaptor.getValue();
        assertEquals(userName, capturedCreateUserPayload.getName());
    }

    @Test
    public void can_createUser_with_email() throws PubNubException {
        //given
        final String testEmail = RandomStringUtils.randomAlphabetic(10) + "@example.com";

        //when
        objectUnderTest.email(testEmail).sync();

        //then
        ArgumentCaptor<CreateUserPayload> createUserPayloadArgumentCaptor = ArgumentCaptor.forClass(CreateUserPayload.class);
        verify(userServiceMock, times(1)).createUser(eq(testSubscriptionKey), eq(testUserIdValue), createUserPayloadArgumentCaptor.capture(), any());

        final CreateUserPayload capturedCreateUserPayload = createUserPayloadArgumentCaptor.getValue();
        assertEquals(testEmail, capturedCreateUserPayload.getEmail());
    }

    @Test
    public void can_createUser_with_externalId() throws PubNubException {
        //given
        String testExternalId = RandomStringUtils.randomAlphabetic(20);

        //when
        objectUnderTest.externalId(testExternalId).sync();

        //then
        ArgumentCaptor<CreateUserPayload> createUserPayloadArgumentCaptor = ArgumentCaptor.forClass(CreateUserPayload.class);
        verify(userServiceMock, times(1)).createUser(eq(testSubscriptionKey), eq(testUserIdValue), createUserPayloadArgumentCaptor.capture(), any());

        final CreateUserPayload capturedCreateUserPayload = createUserPayloadArgumentCaptor.getValue();
        assertEquals(testExternalId, capturedCreateUserPayload.getExternalId());
    }

    @Test
    public void can_createUser_with_profileUrl() throws PubNubException {
        //given
        final String profileUrl = "http://" + RandomStringUtils.randomAlphabetic(5) + ".example.com";

        //when
        objectUnderTest.profileUrl(profileUrl).sync();

        //then
        ArgumentCaptor<CreateUserPayload> createUserPayloadArgumentCaptor = ArgumentCaptor.forClass(CreateUserPayload.class);
        verify(userServiceMock, times(1)).createUser(eq(testSubscriptionKey), eq(testUserIdValue), createUserPayloadArgumentCaptor.capture(), any());

        final CreateUserPayload capturedCreateUserPayload = createUserPayloadArgumentCaptor.getValue();
        assertEquals(profileUrl, capturedCreateUserPayload.getProfileUrl());
    }

    @Test
    public void can_createUser_with_custom() throws PubNubException {
        //given
        final Map<String, Object> testCustom = new HashMap<>();
        testCustom.put("key1", RandomStringUtils.random(10));
        testCustom.put("key2", RandomStringUtils.random(10));

        //when
        objectUnderTest.custom(testCustom).sync();

        //then
        ArgumentCaptor<CreateUserPayload> createUserPayloadArgumentCaptor = ArgumentCaptor.forClass(CreateUserPayload.class);
        verify(userServiceMock, times(1)).createUser(eq(testSubscriptionKey), eq(testUserIdValue), createUserPayloadArgumentCaptor.capture(), any());

        final CreateUserPayload capturedCreateUserPayload = createUserPayloadArgumentCaptor.getValue();
        assertEquals(testCustom, capturedCreateUserPayload.getCustom());
    }

    @Test
    public void can_createUser_with_status() throws PubNubException {
        //given
        final String testStatus = "active_" + RandomStringUtils.randomAlphabetic(5);

        //when
        objectUnderTest.status(testStatus).sync();

        //then
        ArgumentCaptor<CreateUserPayload> createUserPayloadArgumentCaptor = ArgumentCaptor.forClass(CreateUserPayload.class);
        verify(userServiceMock, times(1)).createUser(eq(testSubscriptionKey), eq(testUserIdValue), createUserPayloadArgumentCaptor.capture(), any());

        final CreateUserPayload capturedCreateUserPayload = createUserPayloadArgumentCaptor.getValue();
        assertEquals(testStatus, capturedCreateUserPayload.getStatus());
    }

    @Test
    public void can_createUser_with_type() throws PubNubException {
        //given
        final String testType = "type" + RandomStringUtils.randomAlphabetic(5);

        //when
        objectUnderTest.type(testType).sync();

        //then
        ArgumentCaptor<CreateUserPayload> createUserPayloadArgumentCaptor = ArgumentCaptor.forClass(CreateUserPayload.class);
        verify(userServiceMock, times(1)).createUser(eq(testSubscriptionKey), eq(testUserIdValue), createUserPayloadArgumentCaptor.capture(), any());

        final CreateUserPayload capturedCreateUserPayload = createUserPayloadArgumentCaptor.getValue();
        assertEquals(testType, capturedCreateUserPayload.getType());
    }
}
