package com.pubnub.api.endpoints.objects_vsp.space;

import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.space.Space;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.models.server.objects_vsp.space.UpdateSpacePayload;
import com.pubnub.api.services.vsp.SpaceService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateSpaceTest extends BaseObjectApiTest {
    private UpdateSpace objectUnderTest;

    @Mock
    private SpaceService spaceServiceMock;

    @Mock
    private Call<EntityEnvelope<Space>> call;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = UpdateSpace.create(new SpaceId(testSpaceIdValue), pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());


        when(retrofitManagerMock.getSpaceService()).thenReturn(spaceServiceMock);
        when(spaceServiceMock.updateSpace(eq(testSubscriptionKey), eq(testSpaceIdValue), any(), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new EntityEnvelope<>()));
    }

    @Test
    public void can_updateSpace() throws PubNubException {
        //given
        String updatedName = "updatedName";
        String updatedDescription = "updatedDescription";
        Map<String, Object> updatedCustom = new HashMap<>();
        updatedCustom.putIfAbsent("user_param1", "val1");
        updatedCustom.putIfAbsent("user_param2", "val2");
        String updatedStatus = "updatedStatus";
        String updatedType = "updatedType";

        //when
        objectUnderTest
                .name(updatedName)
                .description(updatedDescription)
                .custom(updatedCustom)
                .status(updatedStatus)
                .type(updatedType)
                .sync();

        //then
        ArgumentCaptor<UpdateSpacePayload> updateSpacePayloadCaptor = ArgumentCaptor.forClass(UpdateSpacePayload.class);
        verify(spaceServiceMock, times(1)).updateSpace(eq(testSubscriptionKey), eq(testSpaceIdValue), updateSpacePayloadCaptor.capture(), any());

        UpdateSpacePayload captorValue = updateSpacePayloadCaptor.getValue();
        assertEquals(updatedName, captorValue.getName());
        assertEquals(updatedDescription, captorValue.getDescription());
        assertEquals(updatedCustom, captorValue.getCustom());
        assertEquals(updatedStatus, captorValue.getStatus());
        assertEquals(updatedType, captorValue.getType());

    }
}
