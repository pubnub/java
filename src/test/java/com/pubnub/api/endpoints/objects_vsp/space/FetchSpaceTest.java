package com.pubnub.api.endpoints.objects_vsp.space;

import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.space.Space;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.services.vsp.SpaceService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import retrofit2.Call;
import retrofit2.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FetchSpaceTest extends BaseObjectApiTest {
    private FetchSpace objectUnderTest;

    @Mock
    private SpaceService spaceServiceMock;

    @Mock
    private Call<EntityEnvelope<Space>> call;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = FetchSpace.create(new SpaceId(testSpaceIdValue), pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());
        when(retrofitManagerMock.getSpaceService()).thenReturn(spaceServiceMock);
        when(spaceServiceMock.fetchSpace(eq(testSubscriptionKey), eq(testSpaceIdValue), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new EntityEnvelope<>()));
    }

    @Test
    public void can_fetch_space() throws PubNubException {
        //given

        //when
        objectUnderTest.sync();

        //then
        verify(spaceServiceMock, times(1)).fetchSpace(eq(testSubscriptionKey), eq(testSpaceIdValue), any());
    }
}