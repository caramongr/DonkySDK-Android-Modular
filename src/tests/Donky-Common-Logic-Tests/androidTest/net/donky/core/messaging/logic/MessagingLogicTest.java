package net.donky.core.messaging.logic;

import android.app.Application;
import android.test.ApplicationTestCase;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.ModuleDefinition;
import net.donky.core.OutboundNotification;
import net.donky.core.Subscription;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.messaging.logic.mock.MockDonkyListener;
import net.donky.core.messaging.logic.mock.MockMessageReceivedDetails;
import net.donky.core.messaging.logic.mock.MockOutboundNotificationBatchListener;
import net.donky.core.messaging.logic.mock.MockServerNotification;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Marcin Swierczek
 * 15/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessagingLogicTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 5000;

    private static String apiKey = ">>ENTER API KEY HERE<<";

    private static String initialUserId = "test_"+new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    public MessagingLogicTest() {
        super(Application.class);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        UserDetails userDetails = new UserDetails();
        userDetails.setUserCountryCode("GBR").
                setUserId(initialUserId).
                setUserFirstName("John").
                setUserLastName("Smith").
                setUserMobileNumber("07555555555").
                setUserEmailAddress("j.s@me.com").
                setUserDisplayName("John");

        DeviceDetails deviceDetails = new DeviceDetails("phone2 my favorite", "phone2", null);

        MockDonkyListener listenerA = new MockDonkyListener();
        DonkyMessaging.initialiseDonkyMessaging(getApplication(), listenerA);

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
    public void testMessageReceivedNotification() throws InterruptedException, JSONException {

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");

        MockOutboundNotificationBatchListener listener = new MockOutboundNotificationBatchListener();

        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("MessageReceived", listener);

        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();

        subscriptions.add(outboundNotificationSubscription);

        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        ServerNotification serverNotification = new MockServerNotification();

        MessageReceivedDetails messageReceivedDetails = new MockMessageReceivedDetails(false, serverNotification);

        MessagingInternalController.getInstance().queueMessageReceivedNotification(messageReceivedDetails);

        List<net.donky.core.network.ClientNotification> notifications = DonkyDataController.getInstance().getNotificationDAO().getNotifications();

        assertEquals(1, notifications.size());

        net.donky.core.network.ClientNotification clientNotificationQueued = notifications.get(0);

        assertEquals("MessageReceived", clientNotificationQueued.getJson().getString("type"));

        assertEquals(messageReceivedDetails.getMessageId(), clientNotificationQueued.getJson().getString("messageId"));

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getNotifications());

        OutboundNotification outboundNotification = listener.getNotifications().get(0);

        assertEquals("MessageReceived", outboundNotification.getJsonData().getString("type"));

        assertEquals(messageReceivedDetails.getMessageId(), outboundNotification.getJsonData().getString("messageId"));

    }

    @Test
    public void testMessageReadNotification() throws InterruptedException, JSONException {

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");

        MockDonkyListener listener = new MockDonkyListener();

        MockOutboundNotificationBatchListener mockOutboundNotificationBatchListener = new MockOutboundNotificationBatchListener();

        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("MessageRead", mockOutboundNotificationBatchListener);

        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();

        subscriptions.add(outboundNotificationSubscription);

        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);

        MockCommonMessage mockCommonMessage = new MockCommonMessage(false, false);

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        MessagingInternalController.getInstance().sendMessageReadNotification(mockCommonMessage, listener);
        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(mockOutboundNotificationBatchListener.getNotifications());

        OutboundNotification outboundNotification = mockOutboundNotificationBatchListener.getNotifications().get(0);

        assertEquals("MessageRead", outboundNotification.getJsonData().getString("type"));

        assertEquals(mockCommonMessage.getMessageId(), outboundNotification.getJsonData().getString("messageId"));

    }

    @Test
    public void testMessageSharedNotification() throws InterruptedException, JSONException {

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");

        MockOutboundNotificationBatchListener listener = new MockOutboundNotificationBatchListener();

        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("MessageShared", listener);

        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();

        subscriptions.add(outboundNotificationSubscription);

        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        MockCommonMessage mockCommonMessage = new MockCommonMessage(false, false);

        MessagingInternalController.getInstance().queueMessageSharedNotification(mockCommonMessage, null);

        List<net.donky.core.network.ClientNotification> notifications = DonkyDataController.getInstance().getNotificationDAO().getNotifications();

        assertEquals(1, notifications.size());

        net.donky.core.network.ClientNotification clientNotificationQueued = notifications.get(0);

        assertEquals("MessageShared", clientNotificationQueued.getJson().getString("type"));

        assertEquals(mockCommonMessage.getMessageId(), clientNotificationQueued.getJson().getString("messageId"));

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getNotifications());

        OutboundNotification outboundNotification = listener.getNotifications().get(0);

        assertEquals("MessageShared", outboundNotification.getJsonData().getString("type"));

        assertEquals(mockCommonMessage.getMessageId(), outboundNotification.getJsonData().getString("messageId"));

    }
}
