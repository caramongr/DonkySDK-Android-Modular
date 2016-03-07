package net.donky.core;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import net.donky.core.account.DeviceDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.DonkyAuthClient;
import net.donky.core.account.UserDetails;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.LocalEvent;
import net.donky.core.mock.MockDonkyListener;
import net.donky.core.network.ServerNotification;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * To run this integration test you will need good wifi connection.
 *
 * Created by Marcin Swierczek
 * 27/03/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyNonAuthUserTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 30000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static final Object lock = new Object();

    public DonkyNonAuthUserTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        init();
    }

    public void init() throws InterruptedException {

        synchronized (lock) {
            AuthDetails.getInstance().setDetails(AuthDetails.id1, AuthDetails.password1);
            lock.notifyAll();
        }

        if (!DonkyCore.isInitialised()) {
            final MockDonkyListener listener = new MockDonkyListener();

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

            DonkyCore.subscribeToLocalEvent(new DonkyEventListener<LocalEvent>(LocalEvent.class) {

                @Override
                public void onDonkyEvent(LocalEvent localEvent) {
                    Log.i("TEST", "LOCAL EVENT !");
                }

            });

            DonkyCore.initialiseDonkySDK(getApplication(), apiKey, false, listener);

            synchronized (listener) {
                listener.wait(TIME_OUT);
            }

            DonkyException donkyException = listener.getDonkyException();
            assertNull(donkyException);

            Map<String, String> validationErrors = listener.getValidationErrors();
            assertNull(validationErrors);
        }
    }

    @Test
    public void testInitialiseAgain() throws InterruptedException {

        final MockDonkyListener listener1 = new MockDonkyListener();
        final MockDonkyListener listener2 = new MockDonkyListener();
        final MockDonkyListener listener3 = new MockDonkyListener();
        final MockDonkyListener listener4 = new MockDonkyListener();

        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, false, listener1);
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, true, listener2);
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, false, listener3);
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, new DonkyAuthenticator() {
            @Override
            public void onAuthenticationChallenge(DonkyAuthClient authClient, ChallengeOptions challengeOptions) {
                authClient.authenticateWithToken(null);
            }
        }, listener4);

        synchronized (listener1) {
            listener1.wait(TIME_OUT/10);
        }
        synchronized (listener2) {
            listener2.wait(TIME_OUT/10);
        }
        synchronized (listener3) {
            listener3.wait(TIME_OUT/10);
        }
        synchronized (listener4) {
            listener4.wait(TIME_OUT/10);
        }

        // Assert

        assertNotNull(listener1.getDonkyException());
        assertNotNull(listener2.getDonkyException());
        assertNotNull(listener3.getDonkyException());
        assertNotNull(listener4.getDonkyException());

    }

    @Test
    public void testUpdateUser() throws InterruptedException {

        // Prepare
        UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();

        userDetails.setUserDisplayName("testDisplayName2");

        // Update

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().updateUserDetails(userDetails, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        // Assert
        if (DonkyAccountController.getInstance().isRegistered()) {
            assertNull(listener.getDonkyException());
            assertNull(listener.getValidationErrors());
            UserDetails userDetailsNew = DonkyAccountController.getInstance().getCurrentDeviceUser();
            assertEquals("testDisplayName2", userDetailsNew.getUserDisplayName());
        } else {
            assertNotNull(listener.getDonkyException());
        }
    }

    @Test
    public void testUpdateRegistration() throws InterruptedException {

        // Prepare

        DeviceDetails deviceDetails = DonkyAccountController.getInstance().getDeviceDetails();
        deviceDetails.setDeviceName("testDeviceName");

        UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();
        userDetails.setUserDisplayName("testDisplayName");

        // Perform change

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().updateRegistrationDetails(userDetails, deviceDetails, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        // Assert

        if (DonkyAccountController.getInstance().isRegistered()) {
            assertNull(listener.getDonkyException());
            assertNull(listener.getValidationErrors());
            DeviceDetails deviceDetailsNew = DonkyAccountController.getInstance().getDeviceDetails();
            UserDetails userDetailsNew = DonkyAccountController.getInstance().getCurrentDeviceUser();
            assertEquals("testDisplayName", userDetailsNew.getUserDisplayName());
            assertEquals("testDeviceName", deviceDetailsNew.getDeviceName());
        } else {
            assertNotNull(listener.getDonkyException());
        }
    }

    @Test
     public void testRegistrationAuth() throws InterruptedException {

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(AuthDetails.id2);
        userDetails.setUserDisplayName(AuthDetails.id2);

        synchronized (lock) {
            AuthDetails.getInstance().setDetails(AuthDetails.id2, AuthDetails.password2);
            lock.notifyAll();
        }

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().registerAuthenticated(userDetails, null, null, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT*3);
        }

        assertNotNull(listener.getDonkyException());
    }

    @Test
    public void testRegistration() throws InterruptedException {

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(AuthDetails.id2);
        userDetails.setUserDisplayName(AuthDetails.id2);

        synchronized (lock) {
            AuthDetails.getInstance().setDetails(AuthDetails.id2, AuthDetails.password2);
            lock.notifyAll();
        }

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().register(userDetails, null, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT*3);
        }

        assertNull(listener.getDonkyException());
        assertNull(listener.getValidationErrors());
    }
}
