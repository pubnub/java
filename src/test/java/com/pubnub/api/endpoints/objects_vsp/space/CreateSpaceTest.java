package com.pubnub.api.endpoints.objects_vsp.space;

import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.endpoints.objects_api.BaseObjectApiTest;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.space.Space;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.models.server.objects_vsp.space.CreateSpacePayload;
import com.pubnub.api.services.vsp.SpaceService;
import org.apache.commons.lang3.RandomStringUtils;
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

public class CreateSpaceTest extends BaseObjectApiTest {
    private CreateSpace objectUnderTest;

    @Mock
    private SpaceService spaceServiceMock;

    @Mock
    private Call<EntityEnvelope<Space>> call;

    @Before
    public void setUp() throws Exception {
        objectUnderTest = CreateSpace.create(new SpaceId(testSpaceIdValue), pubNubMock, telemetryManagerMock, retrofitManagerMock, new TokenManager());

        when(retrofitManagerMock.getSpaceService()).thenReturn(spaceServiceMock);
        when(spaceServiceMock.createSpace(eq(testSubscriptionKey), eq(testSpaceIdValue), any(), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new EntityEnvelope<>()));
    }

    @Test
    public void can_createSpace() throws PubNubException {
        //given
        final String spaceName = RandomStringUtils.randomAlphabetic(20);
        final String spaceDescription = "Space description" + RandomStringUtils.randomAlphabetic(5);
        final Map<String, Object> testCustom = new HashMap<>();
        testCustom.put("key1", RandomStringUtils.random(10));
        testCustom.put("key2", RandomStringUtils.random(10));
        final String testStatus = "active_" + RandomStringUtils.randomAlphabetic(5);
        final String testType = "type" + RandomStringUtils.randomAlphabetic(5);


        //when
        objectUnderTest
                .name(spaceName)
                .description(spaceDescription)
                .custom(testCustom)
                .status(testStatus)
                .type(testType)
                .sync();

        //then
        ArgumentCaptor<CreateSpacePayload> createSpacePayloadCaptor = ArgumentCaptor.forClass(CreateSpacePayload.class);
        verify(spaceServiceMock).createSpace(eq(testSubscriptionKey), eq(testSpaceIdValue), createSpacePayloadCaptor.capture(), any());

        CreateSpacePayload captorValue = createSpacePayloadCaptor.getValue();
        assertEquals(spaceName, captorValue.getName());
        assertEquals(spaceDescription, captorValue.getDescription());
        assertEquals(testCustom, captorValue.getCustom());
        assertEquals(testStatus, captorValue.getStatus());
        assertEquals(testType, captorValue.getType());
    }
}
