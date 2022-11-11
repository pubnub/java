package com.pubnub.api.endpoints.objects_vsp.user;

import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.user.User;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.models.server.objects_vsp.user.UpsertUserPayload;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpsertUserTest extends BaseObjectApiTest {
    private UpsertUser objectUnderTest;

    @Mock
    private UserService userServiceMock;
    @Mock
    private Call<EntityEnvelope<User>> call;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = UpsertUser.create(pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());

        when(retrofitManagerMock.getUserService()).thenReturn(userServiceMock);
        when(userServiceMock.upsertUser(eq(testSubscriptionKey), eq(testUserIdValue), any(), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new EntityEnvelope<>()));
    }

    @Test
    public void can_upsertUser() throws PubNubException {
        String updatedName = "updatedName";
        String updatedEmail = "updatedEmail";
        String updatedProfileUrl = "updatedProfileUrl";
        String updatedExternalId = "updatedExternalId";
        Map<String, Object> updatedCustom = new HashMap<>();
        updatedCustom.putIfAbsent("user_param1", "val1");
        updatedCustom.putIfAbsent("user_param2", "val2");
        String updatedStatus = "updatedStatus";
        String updatedType = "updatedType";

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

        ArgumentCaptor<UpsertUserPayload> upsertUserPayloadCaptor = ArgumentCaptor.forClass(UpsertUserPayload.class);
        verify(userServiceMock).upsertUser(eq(testSubscriptionKey), eq(testUserIdValue), upsertUserPayloadCaptor.capture(), any());

        UpsertUserPayload captorValue = upsertUserPayloadCaptor.getValue();
        assertEquals(updatedName, captorValue.getName());
        assertEquals(updatedEmail, captorValue.getEmail());
        assertEquals(updatedProfileUrl, captorValue.getProfileUrl());
        assertEquals(updatedExternalId, captorValue.getExternalId());
        assertEquals(updatedCustom, captorValue.getCustom());
        assertEquals(updatedStatus, captorValue.getStatus());
        assertEquals(updatedType, captorValue.getType());
    }
}