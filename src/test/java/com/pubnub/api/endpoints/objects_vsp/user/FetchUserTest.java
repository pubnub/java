package com.pubnub.api.endpoints.objects_vsp.user;

import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.user.User;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.services.vsp.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import retrofit2.Call;
import retrofit2.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class FetchUserTest extends BaseObjectApiTest {
    private FetchUser objectUnderTest;

    @Mock
    private UserService userServiceMock;

    @Mock
    private Call<EntityEnvelope<User>> call;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = FetchUser.create(pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());
        when(retrofitManagerMock.getUserService()).thenReturn(userServiceMock);
        when(userServiceMock.fetchUser(eq(testSubscriptionKey), eq(testUserIdValue), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new EntityEnvelope<>()));
    }

    @Test
    public void can_fetch_user() throws PubNubException {
        //given

        //when
        objectUnderTest.userId(new UserId(testUserIdValue)).sync();

        //then
        verify(userServiceMock, times(1)).fetchUser(eq(testSubscriptionKey), eq(testUserIdValue), any());
    }
}