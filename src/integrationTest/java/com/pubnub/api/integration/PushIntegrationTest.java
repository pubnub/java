package com.pubnub.api.integration;

import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNPushEnvironment;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import com.pubnub.api.models.consumer.push.PNPushListProvisionsResult;
import org.junit.Test;

import java.util.*;

import static com.pubnub.api.integration.util.RandomGenerator.randomNumber;
import static org.junit.Assert.*;

public class PushIntegrationTest extends BaseIntegrationTest {

    private final List<String> expectedChannels = new ArrayList<>();
    private String expectedDeviceId;
    private String expectedTopic;

    @Override
    protected void onBefore() {
        for (int i = 0; i < 3; i++) {
            expectedChannels.add(UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.US));
        }

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 70; i++) {
            builder.append(randomNumber(0, 9));
        }
        expectedDeviceId = builder.toString();

        expectedTopic = UUID.randomUUID().toString();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEnumNames() {
        assertEquals("apns", PNPushType.APNS.toString());
        assertEquals("gcm", PNPushType.GCM.toString());
        assertEquals("gcm", PNPushType.FCM.toString());
        assertEquals("mpns", PNPushType.MPNS.toString());
        assertEquals("apns2", PNPushType.APNS2.toString());
    }

    @Test
    public void testCycle() throws PubNubException, InterruptedException {
        for (PNPushType value : PNPushType.values()) {
            performCompleteCycle(value);
        }
    }

    private void performCompleteCycle(PNPushType pushType) throws InterruptedException, PubNubException {
        pubNub.addPushNotificationsOnChannels()
                .channels(expectedChannels)
                .pushType(pushType)
                .topic(expectedTopic)
                .deviceId(expectedDeviceId)
                .sync();

        Thread.sleep(1000);

        final PNPushListProvisionsResult result = pubNub.auditPushChannelProvisions()
                .deviceId(expectedDeviceId)
                .pushType(pushType)
                .topic(expectedTopic)
                .environment(PNPushEnvironment.DEVELOPMENT)
                .sync();
        assert result != null;
        assertTrue(result.getChannels().containsAll(expectedChannels));

        Thread.sleep(1000);

        pubNub.removePushNotificationsFromChannels()
                .pushType(pushType)
                .environment(PNPushEnvironment.DEVELOPMENT)
                .deviceId(expectedDeviceId)
                .topic(expectedTopic)
                .channels(Collections.singletonList(expectedChannels.get(0)))
                .sync();

        Thread.sleep(1000);

        final PNPushListProvisionsResult oneRemoved = pubNub.auditPushChannelProvisions()
                .deviceId(expectedDeviceId)
                .pushType(pushType)
                .topic(expectedTopic)
                .environment(PNPushEnvironment.DEVELOPMENT)
                .sync();
        assert oneRemoved != null;
        assertFalse(oneRemoved.getChannels().isEmpty());
        assertFalse(oneRemoved.getChannels().contains(expectedChannels.get(0)));

        Thread.sleep(1000);

        pubNub.removeAllPushNotificationsFromDeviceWithPushToken()
                .pushType(pushType)
                .environment(PNPushEnvironment.DEVELOPMENT)
                .deviceId(expectedDeviceId)
                .topic(expectedTopic)
                .sync();

        Thread.sleep(1000);

        final PNPushListProvisionsResult listResult = pubNub.auditPushChannelProvisions()
                .deviceId(expectedDeviceId)
                .pushType(pushType)
                .topic(expectedTopic)
                .environment(PNPushEnvironment.DEVELOPMENT)
                .sync();

        assert listResult != null;
        assertTrue(listResult.getChannels().isEmpty());
        assertFalse(listResult.getChannels().containsAll(expectedChannels));
    }
}
