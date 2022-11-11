package com.pubnub.api.endpoints.objects_vsp.user;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.user.RemoveUserResult;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.services.vsp.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RemoveUserTest extends BaseObjectApiTest {
    private RemoveUser objectUnderTest;

    @Mock
    private UserService userServiceMock;

    @Mock
    private Call<EntityEnvelope<JsonElement>> call;


    @Before
    public void setUp() throws Exception {
        objectUnderTest = new RemoveUser(pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());
        when(retrofitManagerMock.getUserService()).thenReturn(userServiceMock);
        when(userServiceMock.removeUser(eq(testSubscriptionKey), eq(testUserIdValue), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new RemoveUserResult()));
    }

    @Test
    public void can_remove_user() throws PubNubException, IOException {
        //given

        //when
        objectUnderTest.userId(new UserId(testUserIdValue)).sync();

        //then
        verify(userServiceMock, times(1)).removeUser(eq(testSubscriptionKey), eq(testUserIdValue), any());
    }

    @Test
    public void can_remove_user_taking_userId_from_config() throws PubNubException, IOException {
        //given

        //when
        objectUnderTest.sync();

        //then
        verify(userServiceMock, times(1)).removeUser(eq(testSubscriptionKey), eq(testUserIdValue), any());
    }
}
