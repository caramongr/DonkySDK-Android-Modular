package net.donky.core;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import net.donky.core.account.DeviceDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.account.UserUpdatedHandler;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.LocalEvent;
import net.donky.core.events.LogMessageEvent;
import net.donky.core.events.NetworkStateChangedEvent;
import net.donky.core.events.RegistrationChangedEvent;
import net.donky.core.gcm.DonkyGcmController;
import net.donky.core.helpers.IdHelper;
import net.donky.core.logging.DLog;
import net.donky.core.logging.DonkyLoggingController;
import net.donky.core.mock.MockDonkyEventListener;
import net.donky.core.mock.MockDonkyListener;
import net.donky.core.mock.MockDonkyResultListener;
import net.donky.core.mock.MockEvent;
import net.donky.core.mock.MockOutboundNotificationListener;
import net.donky.core.mock.MockServerNotificationListener;
import net.donky.core.mock.MockUserUpdated;
import net.donky.core.mock.Service;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.TagDescription;
import net.donky.core.network.content.ContentNotification;
import net.donky.core.network.content.audience.AudienceMember;
import net.donky.core.network.content.audience.SpecifiedUsersAudience;
import net.donky.core.network.content.content.NotificationContent;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * To run this integration test you will need good wifi connection.
 *
 * Created by Marcin Swierczek
 * 27/03/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyKnownUserTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 30000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static String initialUserId = "test_"+Integer.valueOf(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    private static String userIdForTestInitialiseAgainWithDifferentRegistrationDetails = "test_"+Integer.valueOf(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    private static String userIdForTestUpdateRegistration = "test_"+Integer.valueOf(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    private static String userIdForTestUpdateUser = "test_"+Integer.valueOf(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    public DonkyKnownUserTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    @Before
    public void testInit() throws InterruptedException {

        MockDonkyListener listener = new MockDonkyListener();

        TreeMap<String, String> userAdditionalProperties = new TreeMap<>();
        userAdditionalProperties.put("key1", "value1");
        userAdditionalProperties.put("key2", "value2");

        TreeMap<String, String> deviceAdditionalProperties = new TreeMap<>();
        userAdditionalProperties.put("key1", "value1");
        userAdditionalProperties.put("key2", "value2");

        Set<String> tags = new LinkedHashSet<>();
        tags.add("value1");
        tags.add("value2");

        //Subscribe
        List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();
        serverNotificationSubscriptions.add(new Subscription<>("changeColour",
                new NotificationListener<ServerNotification>() {
                    @Override
                    public void onNotification(ServerNotification notification) {

                    }
                }));

        DonkyCore.subscribeToContentNotifications(
                new ModuleDefinition("Color Demo", "1.0.0.0"),
                serverNotificationSubscriptions);

        UserDetails userDetails = new UserDetails();
        userDetails.setUserCountryCode("GBR").
                setUserId(initialUserId).
                setUserFirstName("John2").
                setUserLastName("Smith").
                setUserMobileNumber("07555555555").
                setUserEmailAddress("j.s@me.com").
                setUserDisplayName("John").
                setUserAdditionalProperties(userAdditionalProperties).
                setSelectedTags(tags);

        DeviceDetails deviceDetails = new DeviceDetails("phone2 my favorite", "phone2", deviceAdditionalProperties);

        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<LocalEvent>(LocalEvent.class) {

            @Override
            public void onDonkyEvent(LocalEvent localEvent) {
                Log.i("TEST", "LOCAL EVENT !");
            }

        });

        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, userDetails, deviceDetails, "1.0.0.0", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        DonkyException donkyException = listener.getDonkyException();
        assertNull(donkyException);

        Map<String, String> validationErrors = listener.getValidationErrors();
        assertNull(validationErrors);
    }

    @Test
    public void testInitialiseAgainWithDifferentRegistrationDetails() throws InterruptedException {

        MockDonkyListener listener = new MockDonkyListener();

        Set<String> tags = new LinkedHashSet<>();
        tags.add("value3");

        UserDetails userDetailsOld = DonkyAccountController.getInstance().getCurrentDeviceUser();

        UserDetails userDetails = new UserDetails();
        userDetails.setUserCountryCode("GBR").
                setUserId(userIdForTestInitialiseAgainWithDifferentRegistrationDetails).
                setUserFirstName("John_new_1").
                setUserLastName("Smith_new_1").
                setUserMobileNumber("07555555556").
                setUserEmailAddress("j.s@me.com").
                setUserDisplayName("John_new_1").
                setSelectedTags(tags);

        DeviceDetails deviceDetails = new DeviceDetails("my favorite new", "phone_new", null);

        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, userDetails, deviceDetails, "1.0.0.0", listener);

        synchronized (listener) {
            listener.wait(TIME_OUT*3);
        }

        DonkyException donkyException = listener.getDonkyException();
        assertNotNull(donkyException);

        assertEquals(donkyException.getMessage(), "Cannot initialise more than once.");

    }

    @Test
    public void testRegisterAndUnregisterService() {

        Service input = new Service();
        input.setMessage("HELLO");

        DonkyCore.getInstance().registerService("service", input);

        Service output = (Service) DonkyCore.getInstance().getService("service");

        String message = output.getMessage();

        assertEquals(message, "HELLO");

        DonkyCore.getInstance().unregisterService("service");

        output = (Service) DonkyCore.getInstance().getService("service");

        assertNull(output);
    }

    @Test
    public void testSubscribeToLocalEventAndReceiveThenUnsubscribeAgain() throws InterruptedException {

        MockDonkyEventListener subscriber = new MockDonkyEventListener(MockEvent.class);

        DonkyCore.subscribeToLocalEvent(subscriber);

        DonkyCore.publishLocalEvent(new MockEvent());

        synchronized (subscriber) {
            subscriber.wait(TIME_OUT);
        }

        assertNotNull(subscriber.getEvent());

        subscriber = new MockDonkyEventListener(MockEvent.class);

        DonkyCore.unsubscribeFromLocalEvent(subscriber);

        DonkyCore.publishLocalEvent(new MockEvent());

        synchronized (subscriber) {
            subscriber.wait(TIME_OUT);
        }

        assertNull(subscriber.getEvent());
    }

    @Test
    public void testUnregisterPushAndRegisterAgain() throws InterruptedException {

        MockDonkyListener listener = new MockDonkyListener();

        DonkyGcmController.getInstance().unregisterPush(listener);

        synchronized (listener) {
            listener.wait(TIME_OUT*3);
        }

        DonkyException donkyException = listener.getDonkyException();


        assertNull(donkyException);


        assertNull(listener.getValidationErrors());

        assertEquals(false, DonkyGcmController.getInstance().isRegisteredToGCM());

        listener = new MockDonkyListener();

        DonkyGcmController.getInstance().registerPush(listener);

        synchronized (listener) {
            listener.wait(TIME_OUT*3);
        }

        donkyException = listener.getDonkyException();

        assertNull(donkyException);


        assertNull(listener.getValidationErrors());

        assertEquals(true, DonkyGcmController.getInstance().isRegisteredToGCM());

    }

    @Test
    public void testWriteAndGetLog() {

        DonkyLoggingController.getInstance().writeLog("TEST1", DonkyLoggingController.LogLevel.DEBUG, null);
        DonkyLoggingController.getInstance().writeLog("TEST2", DonkyLoggingController.LogLevel.WARNING, null);
        DonkyLoggingController.getInstance().writeLog("TEST3", DonkyLoggingController.LogLevel.ERROR, null);
        DonkyLoggingController.getInstance().writeLog("TEST4", DonkyLoggingController.LogLevel.SENSITIVE, null);
        DonkyLoggingController.getInstance().writeLog("TEST5", DonkyLoggingController.LogLevel.INFO, null);


        String log = DonkyLoggingController.getInstance().getLog();

        List<String> tokens = new ArrayList<>();

        String patternString = "\\b(" + "TEST1|TEST2|TEST3|TEST4|TEST5" + ")\\b";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(log);

        while (matcher.find()) {
            tokens.add(matcher.group(1));
        }

        assertEquals(true, tokens.size() > 4);
    }

    @Test
    public void testRegisterModuleAndCheckIfExistsWithMinimumVersionSupplied() {

        ModuleDefinition module1 = new ModuleDefinition("module1", "1.1");
        ModuleDefinition module2 = new ModuleDefinition("module2", "1.1.9.9");
        ModuleDefinition module3 = new ModuleDefinition("module3", "3241.13424.93.9222");
        ModuleDefinition module4 = new ModuleDefinition("module4", "3241.13424.93.9222.sdfs");
        ModuleDefinition module5 = new ModuleDefinition("module5", "3241.13424.93.9222.34324");

        DonkyCore.getInstance().registerModule(module1);
        DonkyCore.getInstance().registerModule(module2);
        DonkyCore.getInstance().registerModule(module3);
        DonkyCore.getInstance().registerModule(module4);
        DonkyCore.getInstance().registerModule(module5);

        assertEquals(true, DonkyCore.getInstance().getRegisteredModules().size() >= 5);

        assertEquals(true, DonkyCore.getInstance().isModuleRegistered("module1", "1.0.0.1"));
        assertEquals(true, DonkyCore.getInstance().isModuleRegistered("module2", "1.0.0.1"));
        assertEquals(true, DonkyCore.getInstance().isModuleRegistered("module3", "1.0.0.1"));
        assertEquals(false, DonkyCore.getInstance().isModuleRegistered("module4", "3241.13424.94.1"));
        assertEquals(true, DonkyCore.getInstance().isModuleRegistered("module5", "1.0.0.1"));

        assertEquals(false, DonkyCore.getInstance().isModuleRegistered("module1", "2.2.0.1"));
        assertEquals(false, DonkyCore.getInstance().isModuleRegistered("module2", "2.2.0.1"));
        assertEquals(true, DonkyCore.getInstance().isModuleRegistered("module3", "43.0.0.1"));

    }

    @Test
    public void testUpdateRegistration() throws InterruptedException {

        // Prepare

        DeviceDetails deviceDetails = DonkyAccountController.getInstance().getDeviceDetails();
        UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();

        userDetails.setUserDisplayName("testDisplayName");
        userDetails.setUserId(userIdForTestUpdateRegistration);
        userDetails.setSelectedTags(null);

        TreeMap<String, String> properties = new TreeMap<>();
        properties.put("testKey1", "testValue1");
        properties.put("testKey2", "testValue2");
        properties.put("testKey3", "testValue3");

        deviceDetails.setAdditionalProperties(properties);
        deviceDetails.setDeviceName("testDeviceName");
        deviceDetails.setDeviceType("testDeviceType");

        // Perform change

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().updateRegistrationDetails(userDetails, deviceDetails, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        // Assert

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

        DeviceDetails deviceDetailsNew = DonkyAccountController.getInstance().getDeviceDetails();

        UserDetails userDetailsNew = DonkyAccountController.getInstance().getCurrentDeviceUser();

        assertEquals("testDisplayName", userDetailsNew.getUserDisplayName());
        assertEquals(userIdForTestUpdateRegistration, userDetailsNew.getUserId());

        Set<String> tagsNew = DonkyAccountController.getInstance().getCurrentDeviceUser().getSelectedTags();
        assertNull(tagsNew);

        Map<String, String> propNew = deviceDetailsNew.getAdditionalProperties();

        assertEquals("testValue1", propNew.get("testKey1"));
        assertEquals("testValue2", propNew.get("testKey2"));
        assertEquals("testValue3", propNew.get("testKey3"));

        assertEquals("testDeviceName", deviceDetailsNew.getDeviceName());
        assertEquals("testDeviceType", deviceDetailsNew.getDeviceType());

    }

    @Test
    public void testUpdateUser() throws InterruptedException {

        // Prepare
        UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();

        userDetails.setUserDisplayName("testDisplayName2");
        userDetails.setUserId(userIdForTestUpdateUser);

        Set<String> tags = new LinkedHashSet<>();
        tags.add("tag1");
        tags.add("tag2");
        tags.add("tag3");
        userDetails.setSelectedTags(tags);

        TreeMap<String, String> properties = new TreeMap<>();
        properties.put("testKey1", "testValue1");
        properties.put("testKey2", "testValue2");

        userDetails.setUserAdditionalProperties(properties);

        // Update

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().updateUserDetails(userDetails, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        // Assert

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

        UserDetails userDetailsNew = DonkyAccountController.getInstance().getCurrentDeviceUser();

        assertEquals("testDisplayName2", userDetailsNew.getUserDisplayName());
        assertEquals(userIdForTestUpdateUser, userDetailsNew.getUserId());

        assertEquals("testValue1", userDetailsNew.getUserAdditionalProperties().get("testKey1"));
        assertEquals("testValue2", userDetailsNew.getUserAdditionalProperties().get("testKey2"));

        Set<String> newTags = userDetailsNew.getSelectedTags();

        assertEquals(3, newTags.size());
        assertEquals(true, newTags.contains("tag1"));
        assertEquals(true, newTags.contains("tag2"));
        assertEquals(true, newTags.contains("tag3"));
    }

    @Test
    public void testUpdateDevice() throws InterruptedException {

        // Prepare

        DeviceDetails deviceDetails = DonkyAccountController.getInstance().getDeviceDetails();

        deviceDetails.setDeviceName("testNameUpdateDevice");
        deviceDetails.setDeviceType("testTypeUpdateDevice");

        TreeMap<String, String> properties = new TreeMap<>();
        properties.put("testKey4", "testValue4");

        deviceDetails.setAdditionalProperties(properties);

        // Perform update

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().updateDeviceDetails(deviceDetails, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        // Assert

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

        DeviceDetails deviceDetailsNew = DonkyAccountController.getInstance().getDeviceDetails();

        assertEquals("testNameUpdateDevice", deviceDetailsNew.getDeviceName());

        assertEquals("testTypeUpdateDevice", deviceDetailsNew.getDeviceType());

        Map<String, String> propertiesNew = deviceDetailsNew.getAdditionalProperties();

        assertEquals(1, propertiesNew.size());

        assertEquals("testValue4", propertiesNew.get("testKey4"));

    }

    @Test
    public void testUpdateClient() throws InterruptedException, DonkyException {

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().updateClient(listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

    }

    @Test
    public void testSendContentToSpecificUser() throws JSONException, InterruptedException {

        // Prepare

        MockServerNotificationListener serverNotificationListener = new MockServerNotificationListener();

        List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();
        serverNotificationSubscriptions.add(new Subscription<>("changeColour", serverNotificationListener));


        DonkyCore.subscribeToContentNotifications(
                new ModuleDefinition("Color Demo", "v1"),
                serverNotificationSubscriptions);

        List<ContentNotification> contentNotifications = new LinkedList<>();

        JSONObject jsonObject = new JSONObject("{\"newColour\" : \"#FF0FFF\",\"intervalSeconds\" : 5}");
        contentNotifications.add(new ContentNotification(DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId(), "changeColour", jsonObject));

        // Send

        MockDonkyListener listener = new MockDonkyListener();

        DonkyNetworkController.getInstance().sendContentNotifications(contentNotifications, listener);

        synchronized (serverNotificationListener) {
            serverNotificationListener.wait(TIME_OUT * 3);
        }


        // Assert

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

        assertNotNull(serverNotificationListener.getNotification());

        assertEquals("Custom", serverNotificationListener.getNotification().getType());

        JsonObject data = serverNotificationListener.getNotification().getData();

        Gson gson = new GsonBuilder().create();
        ChangeColor color = gson.fromJson(data, ChangeColor.class);

        assertEquals("changeColour", color.getCustomType());
        assertEquals("#FF0FFF", color.getCustomData().getNewColour());
        assertEquals(5, color.getCustomData().getIntervalSeconds());

    }

    @Test
    public void testIgnoringNotifications() throws JSONException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        int max = 200;

        List<String> ids = new ArrayList<>();

        for (int i = 0; i<max; i++) {
            ids.add(IdHelper.generateId());
        }

        for (int i = 0; i<max; i++) {
            boolean ignore = DonkyNetworkController.getInstance().shouldIgnoreServerNotification(ids.get(i));
            assertEquals(ignore, false);
        }

        boolean ignore = DonkyNetworkController.getInstance().shouldIgnoreServerNotification(ids.get(89));
        assertEquals(ignore, false);

        ignore = DonkyNetworkController.getInstance().shouldIgnoreServerNotification(ids.get(101));
        assertEquals(ignore, true);

        ignore = DonkyNetworkController.getInstance().shouldIgnoreServerNotification(ids.get(120));
        assertEquals(ignore, true);

        ignore = DonkyNetworkController.getInstance().shouldIgnoreServerNotification(ids.get(50));
        assertEquals(ignore, false);

    }

    @Test
    public void testLocalLogEvents() throws InterruptedException {

        MockDonkyEventListener mockDonkyEventListenerLog = new MockDonkyEventListener<>(LogMessageEvent.class);

        DonkyCore.subscribeToLocalEvent(mockDonkyEventListenerLog);

        new DLog("TEST").info("test log");

        synchronized (mockDonkyEventListenerLog) {
            mockDonkyEventListenerLog.wait(TIME_OUT);
        }

        LogMessageEvent logMessageEvent = (LogMessageEvent) mockDonkyEventListenerLog.getEvent();
        assertNotNull(logMessageEvent);
        assertEquals("LogMessageEvent", logMessageEvent.getLocalEventType());

    }

    @Test
    public void testLocalRegistrationEvents() throws InterruptedException {

        MockDonkyEventListener mockDonkyEventListenerRegistrationChange = new MockDonkyEventListener<>(RegistrationChangedEvent.class);

        MockDonkyListener mockDonkyEventListener = new MockDonkyListener();

        DonkyCore.subscribeToLocalEvent(mockDonkyEventListenerRegistrationChange);

        DonkyAccountController.getInstance().updateUserDetails(DonkyAccountController.getInstance().getCurrentDeviceUser().setUserDisplayName("display name 2"), mockDonkyEventListener);

        synchronized (mockDonkyEventListenerRegistrationChange) {
            mockDonkyEventListenerRegistrationChange.wait(TIME_OUT);
        }

        RegistrationChangedEvent registrationChangedEvent = (RegistrationChangedEvent) mockDonkyEventListenerRegistrationChange.getEvent();

        assertNotNull(registrationChangedEvent);
        assertEquals("RegistrationChangedEvent", registrationChangedEvent.getLocalEventType());

    }

    @Test
    public void testRegisterToOutboundContentNotifications() throws InterruptedException, DonkyException, JSONException {

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");

        MockOutboundNotificationListener listener = new MockOutboundNotificationListener();

        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("changeColour", listener);

        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();

        subscriptions.add(outboundNotificationSubscription);

        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);


        // Send content

        ContentNotification contentNotification = new ContentNotification();

        List<AudienceMember> audienceMembers = new LinkedList<>();

        audienceMembers.add(new AudienceMember(DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId(), null));

        contentNotification.putAudience(new SpecifiedUsersAudience(audienceMembers));

        JSONObject jsonObject = new JSONObject("{\"newColour\" : \"#FF0FFF\",\"intervalSeconds\" : 5}");

        contentNotification.putContent(new NotificationContent("changeColour", jsonObject));

        List<ContentNotification> contentNotifications = new LinkedList<>();

        contentNotifications.add(contentNotification);

        // Send

        MockDonkyListener listenerSendContent = new MockDonkyListener();

        DonkyNetworkController.getInstance().sendContentNotifications(contentNotifications, listenerSendContent);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNull(listenerSendContent.getDonkyException());

        assertNull(listenerSendContent.getValidationErrors());

        assertNotNull(listener.getNotification());

        assertEquals(true, listener.getNotification().getBaseNotificationType().equals("changeColour"));

        assertNotNull(listener.getNotification().getJsonData());

    }

    @Test
    public void testRegisterToOutboundClientNotifications() throws InterruptedException, DonkyException, JSONException {

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");

        MockOutboundNotificationListener listener = new MockOutboundNotificationListener();

        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("changeColour", listener);

        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();

        subscriptions.add(outboundNotificationSubscription);

        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);

        // Send content

        ContentNotification contentNotification = new ContentNotification();

        List<AudienceMember> audienceMembers = new LinkedList<>();

        audienceMembers.add(new AudienceMember(DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId(), null));

        contentNotification.putAudience(new SpecifiedUsersAudience(audienceMembers));

        JSONObject jsonObject = new JSONObject("{\"newColour\" : \"#FF0FFF\",\"intervalSeconds\" : 5}");

        contentNotification.putContent(new NotificationContent("changeColour", jsonObject));

        List<ContentNotification> contentNotifications = new LinkedList<>();

        contentNotifications.add(contentNotification);

        // Send

        MockDonkyListener listenerSendContent = new MockDonkyListener();

        DonkyNetworkController.getInstance().sendContentNotifications(contentNotifications, listenerSendContent);

        synchronized (contentNotifications) {
            contentNotifications.wait(TIME_OUT);
        }

        assertNull(listenerSendContent.getDonkyException());

        assertNull(listenerSendContent.getValidationErrors());

        assertNotNull(listener.getNotification());

        assertEquals(true, listener.getNotification().getBaseNotificationType().equals("changeColour"));

        assertNotNull(listener.getNotification().getJsonData());

    }

    @Test
    public void testRegisterWithTheSameData() throws InterruptedException {

        UserDetails oldUserDetails = DonkyDataController.getInstance().getUserDAO().getUserDetails();

        String oldDisplayName = oldUserDetails.getUserDisplayName();
        String oldUserId = oldUserDetails.getUserId();
        String oldCountryCode = oldUserDetails.getCountryCode();
        Set<String> oldSelectedTags = oldUserDetails.getSelectedTags();
        Map<String, String> oldUserAdditionalProperties = oldUserDetails.getUserAdditionalProperties();
        String oldUserEmailAddress = oldUserDetails.getUserEmailAddress();
        String oldUserFirstName = oldUserDetails.getUserFirstName();
        String oldUserMobileNumber = oldUserDetails.getUserMobileNumber();
        String oldUserAvatarId = oldUserDetails.getUserAvatarId();
        String oldUserLastName = oldUserDetails.getUserLastName();

        DeviceDetails deviceDetails = DonkyDataController.getInstance().getDeviceDAO().getDeviceDetails();

        Map<String, String> oldDeviceAdditionalProperties = deviceDetails.getAdditionalProperties();
        String oldDeviceName = deviceDetails.getDeviceName();
        String oldDeviceType = deviceDetails.getDeviceType();

        String oldDeviceSecret = DonkyDataController.getInstance().getDeviceDAO().getDeviceSecret();
        String oldDeviceId = DonkyDataController.getInstance().getDeviceDAO().getDeviceId();

        String oldUserNetworkId = DonkyDataController.getInstance().getUserDAO().getUserNetworkId();

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().reRegisterWithSameUserDetails(listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

        UserDetails newUserDetails = DonkyDataController.getInstance().getUserDAO().getUserDetails();

        assertEquals(oldDisplayName, newUserDetails.getUserDisplayName());
        assertEquals(oldUserId, newUserDetails.getUserId());
        assertEquals(oldCountryCode, newUserDetails.getCountryCode());
        assertEquals(oldUserEmailAddress, newUserDetails.getUserEmailAddress());
        assertEquals(oldUserFirstName, newUserDetails.getUserFirstName());
        assertEquals(oldUserMobileNumber, newUserDetails.getUserMobileNumber());
        assertEquals(oldUserAvatarId, newUserDetails.getUserAvatarId());
        assertEquals(oldUserLastName, newUserDetails.getUserLastName());

        DeviceDetails newDeviceDetails = DonkyDataController.getInstance().getDeviceDAO().getDeviceDetails();

        if (oldDeviceAdditionalProperties != null || newDeviceDetails.getAdditionalProperties() != null) {
            for (String key : oldDeviceAdditionalProperties.keySet()) {
                assertEquals(oldDeviceAdditionalProperties.get(key), newDeviceDetails.getAdditionalProperties().get(key));
            }
        }

        if (oldUserAdditionalProperties != null || newUserDetails.getUserAdditionalProperties() != null) {
            for (String key : oldUserAdditionalProperties.keySet()) {
                assertEquals(oldUserAdditionalProperties.get(key), newUserDetails.getUserAdditionalProperties().get(key));
            }
        }

        if (oldSelectedTags != null || newUserDetails.getSelectedTags() != null) {
            for (String tag : oldSelectedTags) {
                assertEquals(true, newUserDetails.getSelectedTags().contains(tag));
            }
        }

        assertEquals(oldDeviceName, newDeviceDetails.getDeviceName());
        assertEquals(oldDeviceType, newDeviceDetails.getDeviceType());
        assertEquals(oldDeviceSecret, DonkyDataController.getInstance().getDeviceDAO().getDeviceSecret());
        assertEquals(oldDeviceId, DonkyDataController.getInstance().getDeviceDAO().getDeviceId());
        assertEquals(oldUserNetworkId, DonkyDataController.getInstance().getUserDAO().getUserNetworkId());
    }

    @Test
    public void testUserUpdatedHandler() throws InterruptedException {
        UserUpdatedHandler handler = new UserUpdatedHandler();
        MockUserUpdated uu = new MockUserUpdated();
        List<ServerNotification> sn = new LinkedList<>();
        sn.add(uu);
        handler.handleUserUpdatedNotifications(sn);
        String firstName = uu.getData().get("firstName").getAsString();
        String lastName = uu.getData().get("lastName").getAsString();
        String displayName = uu.getData().get("displayName").getAsString();
        String countryIsoCode = uu.getData().get("countryIsoCode").getAsString();
        String emailAddress = uu.getData().get("emailAddress").getAsString();
        String externalUserId = uu.getData().get("externalUserId").getAsString();
        String phoneNumber = uu.getData().get("phoneNumber").getAsString();

        UserDetails user = DonkyAccountController.getInstance().getCurrentDeviceUser();
        assertEquals(firstName, user.getUserFirstName());
        assertEquals(lastName, user.getUserLastName());
        assertEquals(displayName, user.getUserDisplayName());
        assertEquals(countryIsoCode, user.getCountryCode());
        assertEquals(emailAddress, user.getUserEmailAddress());
        assertEquals(externalUserId, user.getUserId());
        assertEquals(phoneNumber, user.getUserMobileNumber());
        assertEquals("a", user.getUserAdditionalProperties().get("1"));
    }

    public void testEventRegistering() throws InterruptedException {

        MockDonkyEventListener subscriber = new MockDonkyEventListener(MockEvent.class);

        DonkyCore.subscribeToLocalEvent(subscriber);
        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<LogMessageEvent>(LogMessageEvent.class) {

            @Override
            public void onDonkyEvent(LogMessageEvent event) {

            }

            @Override
            public String getEventType() {
                return null;
            }
        });
        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<NetworkStateChangedEvent>(NetworkStateChangedEvent.class) {


            @Override
            public void onDonkyEvent(NetworkStateChangedEvent event) {

            }

            @Override
            public String getEventType() {
                return null;
            }
        });
        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<RegistrationChangedEvent>(RegistrationChangedEvent.class) {


            @Override
            public void onDonkyEvent(RegistrationChangedEvent event) {

            }

            @Override
            public String getEventType() {
                return null;
            }
        });

        DonkyCore.subscribeToLocalEvent(subscriber);

        DonkyCore.publishLocalEvent(new MockEvent());

        synchronized (subscriber) {
            subscriber.wait(TIME_OUT);
        }

        assertNotNull(subscriber.getEvent());

        subscriber = new MockDonkyEventListener(MockEvent.class);

        DonkyCore.unsubscribeFromLocalEvent(subscriber);

        DonkyCore.publishLocalEvent(new MockEvent());

        synchronized (subscriber) {
            subscriber.wait(TIME_OUT);
        }

        assertNull(subscriber.getEvent());

    }

    public void testTagsManagement() throws InterruptedException {

        MockDonkyResultListener<List<TagDescription>> mockDonkyResultListener = new MockDonkyResultListener<>();

        DonkyNetworkController.getInstance().getTags(mockDonkyResultListener);

        synchronized (mockDonkyResultListener) {
            mockDonkyResultListener.wait(TIME_OUT);
        }

        assertNull(mockDonkyResultListener.getDonkyException());

        assertNull(mockDonkyResultListener.getValidationErrors());

        List<TagDescription> tags = mockDonkyResultListener.getResult();

        for (TagDescription tagDescription : tags) {

            tagDescription.setSelected(true);

        }

        int numberOfTags = tags.size();

        // Update

        MockDonkyListener listener = new MockDonkyListener();

        DonkyNetworkController.getInstance().updateTags(tags, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

        //Get again

        mockDonkyResultListener = new MockDonkyResultListener<>();

        DonkyNetworkController.getInstance().getTags(mockDonkyResultListener);

        synchronized (mockDonkyResultListener) {
            mockDonkyResultListener.wait(TIME_OUT);
        }

        assertNull(mockDonkyResultListener.getDonkyException());

        assertNull(mockDonkyResultListener.getValidationErrors());

        tags = mockDonkyResultListener.getResult();

        for (TagDescription tagDescription : tags) {

            assertEquals(true, tagDescription.isSelected());

        }

        assertEquals(numberOfTags, tags.size());

    }

    public class ChangeColor {

        @SerializedName("customType")
        private String customType;

        @SerializedName("customData")
        private CustomData customData;

        public class CustomData {

            @SerializedName("newColour")
            private String newColour;

            @SerializedName("intervalSeconds")
            private int intervalSeconds;

            public String getNewColour() {
                return newColour;
            }

            public int getIntervalSeconds() {
                return intervalSeconds;
            }

        }

        public String getCustomType() {
            return customType;
        }

        public CustomData getCustomData() {
            return customData;
        }

    }
}
