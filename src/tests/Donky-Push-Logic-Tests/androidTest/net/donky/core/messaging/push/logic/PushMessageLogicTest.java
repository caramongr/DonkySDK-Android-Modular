package net.donky.core.messaging.push.logic;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.test.ApplicationTestCase;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.ModuleDefinition;
import net.donky.core.OutboundNotification;
import net.donky.core.Subscription;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.events.CoreInitialisedSuccessfullyEvent;
import net.donky.core.messaging.push.logic.events.SimplePushMessageEvent;
import net.donky.core.messaging.push.logic.mock.MockDonkyEventListener;
import net.donky.core.messaging.push.logic.mock.MockDonkyListener;
import net.donky.core.messaging.push.logic.mock.MockOutboundNotificationBatchListener;
import net.donky.core.messaging.push.logic.mock.MockServerNotification;
import net.donky.core.messaging.push.logic.mock.MockSimplePushData;
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
public class PushMessageLogicTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 5000;

    private static String apiKey = ">>ENTER API KEY HERE<<";

    private static String initialUserId = "test_"+new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    public PushMessageLogicTest() {
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

        MockDonkyEventListener mockDonkyEventListenerInitialised = new MockDonkyEventListener<>(CoreInitialisedSuccessfullyEvent.class);
        DonkyCore.subscribeToLocalEvent(mockDonkyEventListenerInitialised);

        MockDonkyListener listenerA = new MockDonkyListener();
        DonkyPushLogic.initialiseDonkyPush(getApplication(), listenerA);

        synchronized (listenerA) {
            listenerA.wait(TIME_OUT);
        }

        DonkyException donkyException = listenerA.getDonkyException();
        assertNull(donkyException);

        Map<String, String> validationErrors = listenerA.getValidationErrors();
        assertNull(validationErrors);

        MockDonkyListener listenerB = new MockDonkyListener();
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, userDetails, deviceDetails, "1.0.0.0", listenerB);

        synchronized (mockDonkyEventListenerInitialised) {
            mockDonkyEventListenerInitialised.wait(TIME_OUT);
        }
    }

    @Test
    public void testHandleServerNotification() throws InterruptedException {

        String messageId = "ca0280bd-3ce9-4203-bf82-4bda003bd13d";

        List<ServerNotification> serverNotifications = new LinkedList<>();
        MockServerNotification serverNotification = new MockServerNotification();
        serverNotifications.add(serverNotification);

        MockDonkyEventListener mockDonkyEventListener = new MockDonkyEventListener<>(SimplePushMessageEvent.class);
        DonkyCore.subscribeToLocalEvent(mockDonkyEventListener);

        new SimplePushHandler().handleSimplePushMessage(serverNotifications);
        synchronized (mockDonkyEventListener) {
            mockDonkyEventListener.wait(TIME_OUT);
        }

        SimplePushMessageEvent richMessageEvent = (SimplePushMessageEvent) mockDonkyEventListener.getEvent();

        assertEquals(messageId, richMessageEvent.getBatchSimplePushData().get(0).getMessageId());

    }

    @Test
    public void testCreatePendingIntent() throws InterruptedException, JSONException, PendingIntent.CanceledException {

        MockSimplePushData mockSimplePushData = new MockSimplePushData(2);
        PendingIntent pendingIntent = PushLogicController.getInstance().createPendingIntent(mockSimplePushData.getButtonSets().get(0).getButtonSetActions()[0], mockSimplePushData, 999);
        assertNotNull(pendingIntent);
    }

    @Test
    public void testPushReporting() throws InterruptedException, JSONException {

        MockSimplePushData mockSimplePushData = new MockSimplePushData(2);
        Bundle bundle = PushLogicController.getInstance().getReportingDataBundle(mockSimplePushData.getButtonSets().get(0).getButtonSetActions()[0], mockSimplePushData);
        Intent intent = new Intent();
        intent.putExtras(bundle);

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");
        MockOutboundNotificationBatchListener mockOutboundNotificationBatchListener = new MockOutboundNotificationBatchListener();
        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("InteractionResult", mockOutboundNotificationBatchListener);
        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();
        subscriptions.add(outboundNotificationSubscription);
        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        PushLogicController.getInstance().reportPushNotificationClicked(intent);

        List<net.donky.core.network.ClientNotification> notifications = DonkyDataController.getInstance().getNotificationDAO().getNotifications();
        assertEquals(1, notifications.size());

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        assertNotNull(mockOutboundNotificationBatchListener.getNotifications());
        OutboundNotification outboundNotification = mockOutboundNotificationBatchListener.getNotifications().get(0);

        assertEquals("InteractionResult", outboundNotification.getJsonData().getString("type"));
        assertEquals(mockSimplePushData.getMessageId(), outboundNotification.getJsonData().getString("messageId"));
    }

}
