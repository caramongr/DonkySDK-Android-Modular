package net.donky.core.analytics;

import android.app.Application;
import android.content.Intent;
import android.test.ApplicationTestCase;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.ModuleDefinition;
import net.donky.core.OutboundNotification;
import net.donky.core.Subscription;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.analytics.mock.MockDonkyListener;
import net.donky.core.analytics.mock.MockOutboundNotificationBatchListener;
import net.donky.core.events.OnCreateEvent;
import net.donky.core.events.OnPauseEvent;
import net.donky.core.events.OnResumeEvent;
import net.donky.core.network.DonkyNetworkController;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * Created by Marcin Swierczek
 * 15/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AnalyticsTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 5000;

    private static String apiKey = ">>ENTER API KEY HERE<<";

    private static String initialUserId = "test_"+new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    public AnalyticsTest() {
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
        DonkyAnalytics.initialiseAnalytics(getApplication(), listenerA);

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
    public void testNotifyAppStart() throws InterruptedException, JSONException {

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");
        MockOutboundNotificationBatchListener listener = new MockOutboundNotificationBatchListener();
        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("AppLaunch", listener);
        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();
        subscriptions.add(outboundNotificationSubscription);
        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        Intent intent = new Intent();
        DonkyCore.publishLocalEvent(new OnCreateEvent(intent));
        DonkyCore.publishLocalEvent(new OnResumeEvent());

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        assertNotNull(listener.getNotifications());

        OutboundNotification outboundNotification = listener.getNotifications().get(0);

        assertEquals("AppLaunch", outboundNotification.getJsonData().getString("type"));

    }

    @Test
    public void testNotifyAppStop() throws InterruptedException, JSONException {

        ModuleDefinition module = new ModuleDefinition("module outbound", "1");
        MockOutboundNotificationBatchListener listener = new MockOutboundNotificationBatchListener();
        Subscription<OutboundNotification> outboundNotificationSubscription = new Subscription<>("AppSession", listener);
        LinkedList<Subscription<OutboundNotification>> subscriptions = new LinkedList<>();
        subscriptions.add(outboundNotificationSubscription);
        DonkyCore.subscribeToOutboundNotifications(module, subscriptions);

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        Intent intent = new Intent();
        DonkyCore.publishLocalEvent(new OnCreateEvent(intent));
        DonkyCore.publishLocalEvent(new OnResumeEvent());

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        DonkyCore.publishLocalEvent(new OnPauseEvent());

        synchronized (listener) {
            listener.wait(6000);
        }

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getNotifications());

        OutboundNotification outboundNotification = listener.getNotifications().get(0);

        assertEquals("AppSession", outboundNotification.getJsonData().getString("type"));

    }
}