package com.pubnub.api.endpoints.push;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNPushEnvironment;
import com.pubnub.api.models.consumer.push.payload.PushPayloadHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushPayloadHelperHelperTest extends TestHarness {

    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
    }

    @Test
    public void testPayloads_Missing() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();
        Map<String, Object> map = pushPayloadHelper.build();
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testPayloads_Null() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        pushPayloadHelper.setApnsPayload(null);
        pushPayloadHelper.setCommonPayload(null);
        pushPayloadHelper.setFcmPayload(null);
        pushPayloadHelper.setMpnsPayload(null);

        Map<String, Object> map = pushPayloadHelper.build();
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testPayloads_Empty() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        pushPayloadHelper.setApnsPayload(new PushPayloadHelper.APNSPayload());
        pushPayloadHelper.setCommonPayload(new HashMap<>());
        pushPayloadHelper.setFcmPayload(new PushPayloadHelper.FCMPayload());
        pushPayloadHelper.setMpnsPayload(new PushPayloadHelper.MPNSPayload());

        Map<String, Object> map = pushPayloadHelper.build();

        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testApple_Empty() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.APNSPayload apnsPayload = new PushPayloadHelper.APNSPayload();

        apnsPayload.setAps(new PushPayloadHelper.APNSPayload.APS());
        apnsPayload.setApns2Configurations(new ArrayList<>());

        Map<String, Object> map = pushPayloadHelper.build();

        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testApple_Valid() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.APNSPayload apnsPayload = new PushPayloadHelper.APNSPayload();

        PushPayloadHelper.APNSPayload.APS aps = new PushPayloadHelper.APNSPayload.APS();
        aps.setAlert("alert");
        aps.setBadge(5);

        HashMap<String, Object> customMap = new HashMap<>();
        customMap.put("key_1", "1");
        customMap.put("key_2", 2);
        customMap.put("key_3", null);

        apnsPayload.setAps(aps);
        apnsPayload.setApns2Configurations(new ArrayList<>());
        apnsPayload.setCustom(customMap);

        pushPayloadHelper.setApnsPayload(apnsPayload);

        Map<String, Object> map = pushPayloadHelper.build();

        HashMap<String, Object> pnApnsDataMap = (HashMap<String, Object>) map.get("pn_apns");

        Assert.assertEquals("1", pnApnsDataMap.get("key_1"));
        Assert.assertEquals(2, pnApnsDataMap.get("key_2"));
        Assert.assertFalse(pnApnsDataMap.containsKey("key_3"));

        HashMap<String, Object> apsMap = (HashMap<String, Object>) pnApnsDataMap.get("aps");
        Assert.assertEquals("alert", apsMap.get("alert"));
        Assert.assertEquals(5, apsMap.get("badge"));

        List<Object> pushList = (List<Object>) pnApnsDataMap.get("pn_push");
        Assert.assertEquals(0, pushList.size());
    }

    @Test
    public void testApple_Aps() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.APNSPayload apnsPayload = new PushPayloadHelper.APNSPayload();
        apnsPayload.setAps(new PushPayloadHelper.APNSPayload.APS()
                .setAlert("alert")
                .setBadge(5)
        );
        pushPayloadHelper.setApnsPayload(apnsPayload);

        HashMap<String, Object> customMap = new HashMap<>();
        customMap.put("key_1", null);
        customMap.put("key_2", "2");
        apnsPayload.setCustom(customMap);

        Map<String, Object> map = pushPayloadHelper.build();

        HashMap<String, Object> pnApnsDataMap = (HashMap<String, Object>) map.get("pn_apns");
        HashMap<String, Object> apsMap = (HashMap<String, Object>) pnApnsDataMap.get("aps");

        Assert.assertEquals("alert", apsMap.get("alert"));
        Assert.assertEquals(5, apsMap.get("badge"));
        Assert.assertFalse(apsMap.containsKey("sound"));

        Assert.assertFalse(pnApnsDataMap.containsKey("pn_push"));

        Assert.assertFalse(pnApnsDataMap.containsKey("key_1"));
        Assert.assertEquals("2", pnApnsDataMap.get("key_2"));

    }

    @Test
    public void testApple_PnPushArray() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.APNSPayload.APNS2Configuration.Target target1 =
                new PushPayloadHelper.APNSPayload.APNS2Configuration.Target()
                        .setEnvironment(PNPushEnvironment.DEVELOPMENT)
                        .setTopic("topic_1");

        PushPayloadHelper.APNSPayload.APNS2Configuration.Target target2 =
                new PushPayloadHelper.APNSPayload.APNS2Configuration.Target()
                        .setEnvironment(PNPushEnvironment.PRODUCTION);

        PushPayloadHelper.APNSPayload.APNS2Configuration.Target target3 =
                new PushPayloadHelper.APNSPayload.APNS2Configuration.Target()
                        .setEnvironment(PNPushEnvironment.PRODUCTION)
                        .setTopic("topic_3")
                        .setExcludeDevices(Arrays.asList("ex_1", "ex_2"));

        PushPayloadHelper.APNSPayload.APNS2Configuration.Target target4 =
                new PushPayloadHelper.APNSPayload.APNS2Configuration.Target()
                        .setEnvironment(null)
                        .setTopic(null)
                        .setExcludeDevices(null);

        PushPayloadHelper.APNSPayload.APNS2Configuration.Target target5 =
                new PushPayloadHelper.APNSPayload.APNS2Configuration.Target()
                        .setEnvironment(PNPushEnvironment.PRODUCTION)
                        .setTopic("topic_5")
                        .setExcludeDevices(Arrays.asList());

        PushPayloadHelper.APNSPayload.APNS2Configuration.Target target6 =
                new PushPayloadHelper.APNSPayload.APNS2Configuration.Target()
                        .setTopic("topic_6")
                        .setExcludeDevices(null);

        PushPayloadHelper.APNSPayload.APNS2Configuration.Target target7 =
                new PushPayloadHelper.APNSPayload.APNS2Configuration.Target();

        PushPayloadHelper.APNSPayload.APNS2Configuration apns2Config1 = new PushPayloadHelper.APNSPayload.APNS2Configuration()
                .setCollapseId("collapse_1")
                .setExpiration("exp_1")
                .setVersion("v1")
                .setTargets(null);

        PushPayloadHelper.APNSPayload.APNS2Configuration apns2Config2 = new PushPayloadHelper.APNSPayload.APNS2Configuration()
                .setCollapseId("collapse_2")
                .setExpiration("exp_2")
                .setVersion("v2")
                .setTargets(new ArrayList<>());

        PushPayloadHelper.APNSPayload.APNS2Configuration apns2Config3 = new PushPayloadHelper.APNSPayload.APNS2Configuration()
                .setCollapseId(null)
                .setExpiration("")
                .setVersion("v3")
                .setTargets(Arrays.asList(
                        target1,
                        target2,
                        target3,
                        target4,
                        target5,
                        target6,
                        target7
                ));

        PushPayloadHelper.APNSPayload.APNS2Configuration apns2Config4 = new PushPayloadHelper.APNSPayload.APNS2Configuration()
                .setCollapseId(null)
                .setExpiration(null)
                .setVersion(null);

        PushPayloadHelper.APNSPayload.APNS2Configuration apns2Config5 = new PushPayloadHelper.APNSPayload.APNS2Configuration();

        PushPayloadHelper.APNSPayload apnsPayload = new PushPayloadHelper.APNSPayload();

        List<PushPayloadHelper.APNSPayload.APNS2Configuration> apns2Configurations = new ArrayList<>();
        apns2Configurations.add(apns2Config1);
        apns2Configurations.add(apns2Config2);
        apns2Configurations.add(apns2Config3);
        apns2Configurations.add(apns2Config4);
        apns2Configurations.add(apns2Config5);
        apnsPayload.setApns2Configurations(apns2Configurations);

        pushPayloadHelper.setApnsPayload(apnsPayload);

        Map<String, Object> map = pushPayloadHelper.build();


        HashMap<String, Object> apnsMap = (HashMap<String, Object>) map.get("pn_apns");
        List<HashMap<String, Object>> pnPushList = (List<HashMap<String, Object>>) apnsMap.get("pn_push");

        Assert.assertEquals(3, pnPushList.size());

        HashMap<String, Object> pushItemMap1 = pnPushList.get(0);

        Assert.assertEquals("exp_1", pnPushList.get(0).get("expiration"));
        Assert.assertEquals("collapse_1", pnPushList.get(0).get("collapse_id"));
        Assert.assertEquals("v1", pnPushList.get(0).get("version"));
        Assert.assertFalse(pnPushList.get(0).containsKey("targets"));

        Assert.assertEquals("exp_2", pnPushList.get(1).get("expiration"));
        Assert.assertEquals("collapse_2", pnPushList.get(1).get("collapse_id"));
        Assert.assertEquals("v2", pnPushList.get(1).get("version"));
        Assert.assertFalse(pnPushList.get(1).containsKey("targets"));

        Assert.assertEquals("", pnPushList.get(2).get("expiration"));
        Assert.assertFalse(pnPushList.get(2).containsKey("collapse_id"));
        Assert.assertEquals("v3", pnPushList.get(2).get("version"));
        Assert.assertTrue(pnPushList.get(2).containsKey("targets"));


        List<HashMap<String, Object>> pnTargetsMap = (List<HashMap<String, Object>>) pnPushList.get(2).get("targets");

        Assert.assertEquals("development", pnTargetsMap.get(0).get("environment"));
        Assert.assertEquals("topic_1", pnTargetsMap.get(0).get("topic"));
        Assert.assertFalse(pnTargetsMap.get(0).containsKey("excludeDevices"));

        Assert.assertEquals("production", pnTargetsMap.get(1).get("environment"));
        Assert.assertFalse(pnTargetsMap.get(1).containsKey("topic_2"));
        Assert.assertFalse(pnTargetsMap.get(1).containsKey("excludeDevices"));

        Assert.assertEquals("production", pnTargetsMap.get(2).get("environment"));
        Assert.assertEquals("topic_3", pnTargetsMap.get(2).get("topic"));
        Assert.assertEquals("ex_1", ((List) pnTargetsMap.get(2).get("excluded_devices")).get(0));
        Assert.assertEquals("ex_2", ((List) pnTargetsMap.get(2).get("excluded_devices")).get(1));

        Assert.assertEquals("production", pnTargetsMap.get(3).get("environment"));
        Assert.assertFalse(pnTargetsMap.get(3).containsKey("topic_5"));
        Assert.assertFalse(pnTargetsMap.get(3).containsKey("excludeDevices"));

        Assert.assertFalse(pnTargetsMap.get(4).containsKey("environment"));
        Assert.assertEquals("topic_6", pnTargetsMap.get(4).get("topic"));
        Assert.assertFalse(pnTargetsMap.get(4).containsKey("environment"));
    }

    @Test
    public void testApple_Aps_Empty() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.APNSPayload apnsPayload = new PushPayloadHelper.APNSPayload();
        apnsPayload.setAps(new PushPayloadHelper.APNSPayload.APS());
        pushPayloadHelper.setApnsPayload(apnsPayload);

        Map<String, Object> map = pushPayloadHelper.build();
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testCommonPayload_Valid() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        HashMap<String, Object> commonPayload = new HashMap<>();
        commonPayload.put("common_key_1", 1);
        commonPayload.put("common_key_2", "2");
        commonPayload.put("common_key_3", true);
        pushPayloadHelper.setCommonPayload(commonPayload);

        Map<String, Object> map = pushPayloadHelper.build();

        Assert.assertEquals(map.get("common_key_1"), 1);
        Assert.assertEquals(map.get("common_key_2"), "2");
        Assert.assertEquals(map.get("common_key_3"), true);
    }

    @Test
    public void testCommonPayload_Invalid() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        HashMap<String, Object> commonPayload = new HashMap<>();
        commonPayload.put("common_key_1", null);
        commonPayload.put("common_key_2", null);
        commonPayload.put("common_key_3", null);
        pushPayloadHelper.setCommonPayload(commonPayload);

        Map<String, Object> map = pushPayloadHelper.build();

        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testGoogle_Valid_1() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.FCMPayload fcmPayload = new PushPayloadHelper.FCMPayload();

        fcmPayload.setNotification(
                new PushPayloadHelper.FCMPayload.Notification()
                        .setBody("Notification body")
                        .setImage(null)
                        .setTitle("")
                        .setClickAction("FOO_ACTION")
        );

        HashMap<String, Object> customFcmPayload = new HashMap<>();
        customFcmPayload.put("a", "a");
        customFcmPayload.put("b", 1);
        customFcmPayload.put("c", null);
        fcmPayload.setCustom(customFcmPayload);

        HashMap<String, Object> dataFcmPayload = new HashMap<>();
        dataFcmPayload.put("data_1", "a");
        dataFcmPayload.put("data_2", 1);
        dataFcmPayload.put("data_3", null);
        fcmPayload.setData(dataFcmPayload);
        pushPayloadHelper.setFcmPayload(fcmPayload);

        Map<String, Object> map = pushPayloadHelper.build();
        HashMap<String, Object> pnFcmMap = (HashMap<String, Object>) map.get("pn_gcm");

        Assert.assertNotNull(pnFcmMap);

        HashMap<String, Object> pnFcmDataMap = (HashMap<String, Object>) pnFcmMap.get("data");
        HashMap<String, Object> pnFcmNotificationsMap = (HashMap<String, Object>) pnFcmMap.get("notification");

        Assert.assertNotNull(pnFcmDataMap);
        Assert.assertNotNull(pnFcmNotificationsMap);

        Assert.assertEquals(pnFcmMap.get("a"), "a");
        Assert.assertEquals(pnFcmMap.get("b"), 1);
        Assert.assertEquals(pnFcmMap.get("c"), null);

        Assert.assertEquals(pnFcmDataMap.get("data_1"), "a");
        Assert.assertEquals(pnFcmDataMap.get("data_2"), 1);
        Assert.assertEquals(pnFcmDataMap.get("data_3"), null);

        Assert.assertEquals(pnFcmNotificationsMap.get("body"), "Notification body");
        Assert.assertEquals(pnFcmNotificationsMap.get("image"), null);
        Assert.assertEquals(pnFcmNotificationsMap.get("title"), "");
        Assert.assertEquals(pnFcmNotificationsMap.get("click_action"), "FOO_ACTION");
    }

    @Test
    public void testGoogle_Empty() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.FCMPayload fcmPayload = new PushPayloadHelper.FCMPayload();
        pushPayloadHelper.setFcmPayload(fcmPayload);

        Map<String, Object> map = pushPayloadHelper.build();


        HashMap<String, Object> pnFcmMap = (HashMap<String, Object>) map.get("pn_gcm");

        Assert.assertNull(pnFcmMap);
    }

    @Test
    public void testGoogle_EmptyNotification() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.FCMPayload.Notification notification = new PushPayloadHelper.FCMPayload.Notification();
        HashMap<String, Object> customMap = new HashMap<>();
        customMap.put("key_1", "1");
        customMap.put("key_2", 2);

        PushPayloadHelper.FCMPayload fcmPayload = new PushPayloadHelper.FCMPayload();
        fcmPayload.setNotification(notification);
        fcmPayload.setCustom(customMap);

        pushPayloadHelper.setFcmPayload(fcmPayload);

        Map<String, Object> map = pushPayloadHelper.build();


        HashMap<String, Object> pnFcmMap = (HashMap<String, Object>) map.get("pn_gcm");
        HashMap<String, Object> pnFcmNotificationMap = (HashMap<String, Object>) pnFcmMap.get("notification");

        Assert.assertNull(pnFcmNotificationMap);
    }

    @Test
    public void testGoogle_EmptyData() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.FCMPayload fcmPayload = new PushPayloadHelper.FCMPayload();
        HashMap<String, Object> dataMap = new HashMap<>();
        fcmPayload.setData(dataMap);

        pushPayloadHelper.setFcmPayload(fcmPayload);

        Map<String, Object> map = pushPayloadHelper.build();

        HashMap<String, Object> pnFcmMap = (HashMap<String, Object>) map.get("pn_gcm");


        Assert.assertNull(pnFcmMap);
    }

    @Test
    public void testGoogle_Valid_2() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.FCMPayload fcmPayload = new PushPayloadHelper.FCMPayload();

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("key_1", "value_1");
        dataMap.put("key_2", 2);
        dataMap.put("key_3", true);
        dataMap.put("key_4", "");
        dataMap.put("key_5", null);
        fcmPayload.setData(dataMap);

        pushPayloadHelper.setFcmPayload(fcmPayload);

        Map<String, Object> map = pushPayloadHelper.build();

        HashMap<String, Object> pnFcmMap = (HashMap<String, Object>) map.get("pn_gcm");
        HashMap<String, Object> pnFcmDataMap = (HashMap<String, Object>) pnFcmMap.get("data");

        Assert.assertNotNull(pnFcmDataMap);
        Assert.assertFalse(pnFcmDataMap.isEmpty());
        Assert.assertTrue(pnFcmDataMap.containsKey("key_1"));
        Assert.assertTrue(pnFcmDataMap.containsKey("key_2"));
        Assert.assertTrue(pnFcmDataMap.containsKey("key_3"));
        Assert.assertTrue(pnFcmDataMap.containsKey("key_4"));
        Assert.assertTrue(pnFcmDataMap.containsKey("key_5"));
        Assert.assertNotNull(pnFcmDataMap.get("key_1"));
        Assert.assertNotNull(pnFcmDataMap.get("key_2"));
        Assert.assertNotNull(pnFcmDataMap.get("key_3"));
        Assert.assertNotNull(pnFcmDataMap.get("key_4"));
        Assert.assertNull(pnFcmDataMap.get("key_5"));
    }

    @Test
    public void testGoogle_Custom() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.FCMPayload fcmPayload = new PushPayloadHelper.FCMPayload();

        HashMap<String, Object> customMap = new HashMap<>();
        customMap.put("key_1", "value_1");
        customMap.put("key_2", 2);
        customMap.put("key_3", true);
        customMap.put("key_4", "");
        customMap.put("key_5", null);
        fcmPayload.setCustom(customMap);

        pushPayloadHelper.setFcmPayload(fcmPayload);

        Map<String, Object> map = pushPayloadHelper.build();

        HashMap<String, Object> pnFcmMap = (HashMap<String, Object>) map.get("pn_gcm");

        Assert.assertNotNull(pnFcmMap);
        Assert.assertFalse(pnFcmMap.isEmpty());
        Assert.assertTrue(pnFcmMap.containsKey("key_1"));
        Assert.assertTrue(pnFcmMap.containsKey("key_2"));
        Assert.assertTrue(pnFcmMap.containsKey("key_3"));
        Assert.assertTrue(pnFcmMap.containsKey("key_4"));
        Assert.assertFalse(pnFcmMap.containsKey("key_5"));
        Assert.assertNotNull(pnFcmMap.get("key_1"));
        Assert.assertNotNull(pnFcmMap.get("key_2"));
        Assert.assertNotNull(pnFcmMap.get("key_3"));
        Assert.assertNotNull(pnFcmMap.get("key_4"));
        Assert.assertNull(pnFcmMap.get("key_5"));
    }

    @Test
    public void testMicrosoft_Missing() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.MPNSPayload mpnsPayload = new PushPayloadHelper.MPNSPayload();

        mpnsPayload.setBackContent("Back Content");
        mpnsPayload.setBackTitle("Back Title");
        mpnsPayload.setCount(1);
        mpnsPayload.setTitle("Title");
        mpnsPayload.setType("Type");

        HashMap<String, Object> customMpnsPayload = new HashMap<>();
        customMpnsPayload.put("a", "a");
        customMpnsPayload.put("b", 1);
        customMpnsPayload.put("c", "");
        customMpnsPayload.put("d", null);
        mpnsPayload.setCustom(customMpnsPayload);

        pushPayloadHelper.setMpnsPayload(mpnsPayload);

        Map<String, Object> map = pushPayloadHelper.build();
        HashMap<String, Object> pnMpnsMap = (HashMap<String, Object>) map.get("pn_mpns");

        Assert.assertNotNull(pnMpnsMap);

        Assert.assertEquals(pnMpnsMap.get("back_content"), "Back Content");
        Assert.assertEquals(pnMpnsMap.get("back_title"), "Back Title");
        Assert.assertEquals(pnMpnsMap.get("count"), 1);
        Assert.assertEquals(pnMpnsMap.get("title"), "Title");
        Assert.assertEquals(pnMpnsMap.get("type"), "Type");
        Assert.assertEquals(pnMpnsMap.get("a"), "a");
        Assert.assertEquals(pnMpnsMap.get("b"), 1);
        Assert.assertEquals(pnMpnsMap.get("c"), "");
        Assert.assertEquals(pnMpnsMap.get("d"), null);
    }

    @Test
    public void testMicrosoft_Valid() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.MPNSPayload mpnsPayload = new PushPayloadHelper.MPNSPayload();

        mpnsPayload.setBackContent("Back Content");
        mpnsPayload.setBackTitle("Back Title");
        mpnsPayload.setCount(1);
        mpnsPayload.setTitle("Title");
        mpnsPayload.setType("Type");

        HashMap<String, Object> customMpnsPayload = new HashMap<>();
        customMpnsPayload.put("a", "a");
        customMpnsPayload.put("b", 1);
        customMpnsPayload.put("c", "");
        customMpnsPayload.put("d", null);
        mpnsPayload.setCustom(customMpnsPayload);

        pushPayloadHelper.setMpnsPayload(mpnsPayload);

        Map<String, Object> map = pushPayloadHelper.build();
        HashMap<String, Object> pnMpnsMap = (HashMap<String, Object>) map.get("pn_mpns");

        Assert.assertNotNull(pnMpnsMap);

        Assert.assertEquals(pnMpnsMap.get("back_content"), "Back Content");
        Assert.assertEquals(pnMpnsMap.get("back_title"), "Back Title");
        Assert.assertEquals(pnMpnsMap.get("count"), 1);
        Assert.assertEquals(pnMpnsMap.get("title"), "Title");
        Assert.assertEquals(pnMpnsMap.get("type"), "Type");
        Assert.assertEquals(pnMpnsMap.get("a"), "a");
        Assert.assertEquals(pnMpnsMap.get("b"), 1);
        Assert.assertEquals(pnMpnsMap.get("c"), "");
        Assert.assertEquals(pnMpnsMap.get("d"), null);
    }

    @Test
    public void testMicrosoft_Empty() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.MPNSPayload mpnsPayload = new PushPayloadHelper.MPNSPayload();

        pushPayloadHelper.setMpnsPayload(mpnsPayload);

        Map<String, Object> map = pushPayloadHelper.build();
        HashMap<String, Object> pnMpnsMap = (HashMap<String, Object>) map.get("pn_mpns");

        Assert.assertNull(pnMpnsMap);
    }

    @Test
    public void testMicrosoft_Custom() {
        PushPayloadHelper pushPayloadHelper = new PushPayloadHelper();

        PushPayloadHelper.MPNSPayload mpnsPayload = new PushPayloadHelper.MPNSPayload();

        mpnsPayload.setBackContent("");
        mpnsPayload.setBackTitle("Back Title");
        mpnsPayload.setCount(1);
        mpnsPayload.setTitle(null);
        mpnsPayload.setType("Type");

        HashMap<String, Object> customMpnsPayload = new HashMap<>();
        customMpnsPayload.put("a", "a");
        customMpnsPayload.put("b", 1);
        customMpnsPayload.put("c", "");
        customMpnsPayload.put("d", null);
        mpnsPayload.setCustom(customMpnsPayload);

        pushPayloadHelper.setMpnsPayload(mpnsPayload);

        Map<String, Object> map = pushPayloadHelper.build();
        HashMap<String, Object> pnMpnsMap = (HashMap<String, Object>) map.get("pn_mpns");

        Assert.assertNotNull(pnMpnsMap);

        Assert.assertEquals("", pnMpnsMap.get("back_content"));
        Assert.assertEquals("Back Title", pnMpnsMap.get("back_title"));
        Assert.assertEquals(1, pnMpnsMap.get("count"));
        Assert.assertFalse(pnMpnsMap.containsKey("title"));
        Assert.assertEquals("Type", pnMpnsMap.get("type"));
        Assert.assertEquals("a", pnMpnsMap.get("a"));
        Assert.assertEquals(1, pnMpnsMap.get("b"));
        Assert.assertEquals("", pnMpnsMap.get("c"));
        Assert.assertFalse(pnMpnsMap.containsKey("d"));
    }

}
