package net.donky.core.automation;

import android.app.Application;
import android.test.ApplicationTestCase;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationListener;
import net.donky.core.OutboundNotification;
import net.donky.core.Subscription;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.automation.mock.MockDonkyListener;
import net.donky.core.automation.mock.MockOutboundNotificationBatchListener;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Marcin Swierczek
 * 15/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AutomationTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 5000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static String initialUserId = "test_"+new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    public AutomationTest() {
        super(Application.class);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

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
                setUserFirstName("John").
                setUserLastName("Smith").
                setUserMobileNumber("07555555555").
                setUserEmailAddress("j.s@me.com").
                setUserDisplayName("John");

        DeviceDetails deviceDetails = new DeviceDetails("phone2 my favorite", "phone2", null);

        // Initialise Donky Rich UI Module
        MockDonkyListener listenerA = new MockDonkyListener();
        DonkyAutomation.initialiseDonkyAutomation(getApplication(), listenerA);

        synchronized (listenerA) {
            listenerA.wait(TIME_OUT);
        }

        DonkyException donkyException = listenerA.getDonkyException();
        assertNull(donkyException);

        Map<String, String> validationErrors = listenerA.getValidationErrors();
        assertNull(validationErrors);

        MockDonkyListener listenerB = new MockDonkyListener();
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, userDetails, deviceDetails, "1.0.0.0", listenerB);

        synchronized (listenerB) {
            listenerB.wait(TIME_OUT);
        }
    }

    @Test
    public void testExecuteThirdPartyTriggers() throws InterruptedException, JSONException {

        final String testTriggerKey = "testTriggerKey";
        final Map<String, String> customData = new HashMap<>();
        customData.put("k1","v1");
        customData.put("k2","v2");

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");
        MockOutboundNotificationBatchListener listener = new MockOutboundNotificationBatchListener();
        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("ExecuteThirdPartyTriggers", listener);
        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();
        subscriptions.add(outboundNotificationSubscription);
        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        MockDonkyListener donkyListener = new MockDonkyListener();

        AutomationController.getInstance().executeThirdPartyTriggerWithKeyImmediately(testTriggerKey, customData, donkyListener);

        synchronized (donkyListener) {
            donkyListener.wait(TIME_OUT);
        }

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        assertNotNull(listener.getNotifications());

        OutboundNotification outboundNotification = listener.getNotifications().get(0);

        assertEquals("ExecuteThirdPartyTriggers", outboundNotification.getJsonData().getString("type"));

    }

}
