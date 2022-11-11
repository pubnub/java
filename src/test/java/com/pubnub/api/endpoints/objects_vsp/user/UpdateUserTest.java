package com.pubnub.api.endpoints.objects_vsp.user;

import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.user.User;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.models.server.objects_vsp.user.UpdateUserPayload;
import com.pubnub.api.services.vsp.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import retrofit2.Call;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UpdateUserTest extends BaseObjectApiTest {
    private UpdateUser objectUnderTest;

    @Mock
    private UserService userServiceMock;

    @Mock
    private Call<EntityEnvelope<User>> call;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = UpdateUser.create(pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());
        when(retrofitManagerMock.getUserService()).thenReturn(userServiceMock);
        when(userServiceMock.updateUser(eq(testSubscriptionKey), eq(testUserIdValue), any(), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new EntityEnvelope<>()));
    }

    @Test
    public void can_update_user_passing_full_object() throws PubNubException {
        //given
        String updatedName = "updatedName";
        String updatedEmail = "updatedEmail";
        String updatedProfileUrl = "updatedProfileUrl";
        String updatedExternalId = "updatedExternalId";
        Map<String, Object> updatedCustom = new HashMap<>();
        updatedCustom.putIfAbsent("user_param1", "val1");
        updatedCustom.putIfAbsent("user_param2", "val2");
        String updatedStatus = "updatedStatus";
        String updatedType = "updatedType";


        //when
        objectUnderTest
                .userId(new UserId(testUserIdValue))
                .name(updatedName)
                .email(updatedEmail)
                .profileUrl(updatedProfileUrl)
                .externalId(updatedExternalId)
                .custom(updatedCustom)
                .includeCustom(false)
                .status(updatedStatus)
                .type(updatedType)
                .sync();

        //then
        ArgumentCaptor<UpdateUserPayload> updateUserPayloadCaptor = ArgumentCaptor.forClass(UpdateUserPayload.class);
        verify(userServiceMock, times(1)).updateUser(eq(testSubscriptionKey), eq(testUserIdValue), updateUserPayloadCaptor.capture(), any());

        UpdateUserPayload captorValue = updateUserPayloadCaptor.getValue();
        assertEquals(updatedName, captorValue.getName());
        assertEquals(updatedEmail, captorValue.getEmail());
        assertEquals(updatedProfileUrl, captorValue.getProfileUrl());
        assertEquals(updatedExternalId, captorValue.getExternalId());
        assertEquals(updatedCustom, captorValue.getCustom());
        assertEquals(updatedStatus, captorValue.getStatus());
        assertEquals(updatedType, captorValue.getType());
    }


    @Test
    public void can_update_user_with_new_name() throws PubNubException {
        //given
        String updatedName = "updatedName";

        //when
        objectUnderTest
                .userId(new UserId(testUserIdValue))
                .name(updatedName)
                .sync();

        //then
        ArgumentCaptor<UpdateUserPayload> updateUserPayloadCaptor = ArgumentCaptor.forClass(UpdateUserPayload.class);
        verify(userServiceMock, times(1)).updateUser(eq(testSubscriptionKey), eq(testUserIdValue), updateUserPayloadCaptor.capture(), any());

        UpdateUserPayload captorValue = updateUserPayloadCaptor.getValue();
        assertEquals(updatedName, captorValue.getName());
    }

    @Test
    public void can_update_user_with_new_email() throws PubNubException {
        //given
        String updatedEmail = "updatedEmail";

        //when
        objectUnderTest
                .userId(new UserId(testUserIdValue))
                .email(updatedEmail)
                .sync();

        //then
        ArgumentCaptor<UpdateUserPayload> updateUserPayloadCaptor = ArgumentCaptor.forClass(UpdateUserPayload.class);
        verify(userServiceMock).updateUser(eq(testSubscriptionKey), eq(testUserIdValue), updateUserPayloadCaptor.capture(), any());

        UpdateUserPayload captorValue = updateUserPayloadCaptor.getValue();
        assertEquals(updatedEmail, captorValue.getEmail());
    }
}