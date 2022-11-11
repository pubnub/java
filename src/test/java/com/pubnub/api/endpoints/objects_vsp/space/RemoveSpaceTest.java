package com.pubnub.api.endpoints.objects_vsp.space;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.space.RemoveSpaceResult;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.services.vsp.SpaceService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import retrofit2.Call;
import retrofit2.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RemoveSpaceTest extends BaseObjectApiTest {
    private RemoveSpace objectUnderTest;

    protected final String testSpaceIdValue = RandomStringUtils.randomAlphabetic(20);

    @Mock
    private SpaceService spaceServiceMock;

    @Mock
    private Call<EntityEnvelope<JsonElement>> call;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = new RemoveSpace(new SpaceId(testSpaceIdValue), pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());
        when(retrofitManagerMock.getSpaceService()).thenReturn(spaceServiceMock);
        when(spaceServiceMock.removeSpace(eq(testSubscriptionKey), eq(testSpaceIdValue), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new EntityEnvelope<>()));
    }

    @Test
    public void can_remove_space() throws PubNubException {
        //given

        //when
        objectUnderTest.sync();

        //then
        verify(spaceServiceMock).removeSpace(eq(testSubscriptionKey), eq(testSpaceIdValue), any());
    }
}