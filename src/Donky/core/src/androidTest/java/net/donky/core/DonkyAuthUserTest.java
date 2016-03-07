package net.donky.core;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.donky.core.account.DeviceDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.DonkyAuthClient;
import net.donky.core.account.UserDetails;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.LocalEvent;
import net.donky.core.mock.MockDonkyListener;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.restapi.RestClient;

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
public class DonkyAuthUserTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 30000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static final String authServiceURL = "URL_POINTING_TO_TEST_AUTH_SERVER";

    private static final Object lock = new Object();

    public DonkyAuthUserTest() {
        super(Application.class);
    }

    class Authenticator implements DonkyAuthenticator {

        @Override
        public void onAuthenticationChallenge(DonkyAuthClient authClient, ChallengeOptions options) {

            signInStevan(authClient, options.getNonce());
        }

    }

    public class AuthResponse {

        @SerializedName("jwt")
        protected String jwt;

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        init();
    }

    private void signInStevan(final DonkyAuthClient authClient, final String nonce) {

        if (DonkyCore.isInitialised()) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    synchronized (lock) {
                        MediaType JSON
                                = MediaType.parse("application/json; charset=utf-8");

                        String id = AuthDetails.getInstance().getId();
                        String password = AuthDetails.getInstance().getPassword();

                        RequestBody body = RequestBody.create(JSON, "{\n" +
                                "    \"username\":\"" + id + "\",\n" +
                                "    \"password\":\"" + password + "\",\n" +
                                "    \"nonce\":\"" + nonce + "\",\n" +
                                "    \"audience\":\"marcin\"\n" +
                                "}");

                        Request request = new Request.Builder()
                                .url(authServiceURL)
                                .post(body)
                                .build();

                        Response response = null;
                        try {
                            response = RestClient.getInstance().getOkHttpClient().newCall(request).execute();
                            String strbody = response.body().string();

                            Gson gson = new Gson();
                            AuthResponse json = gson.fromJson(strbody, AuthResponse.class);
                            authClient.authenticateWithToken(json.jwt);
                        } catch (Exception e) {
                            authClient.authenticateWithToken(null);
                        } finally {
                            lock.notifyAll();
                        }
                    }
                }
            }).start();
        }
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

            DonkyCore.initialiseDonkySDK(getApplication(), apiKey, new Authenticator(), listener);

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

        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, new Authenticator(), listener1);
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, new Authenticator(), listener2);
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, new Authenticator(), listener3);
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, new Authenticator(), listener4);

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

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

        UserDetails userDetailsNew = DonkyAccountController.getInstance().getCurrentDeviceUser();

        assertEquals("testDisplayName2", userDetailsNew.getUserDisplayName());

    }

    @Test
    public void testUpdateUserWrong() throws InterruptedException {

        // Prepare
        UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();

        userDetails.setUserId("new_id");

        // Update

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().updateUserDetails(userDetails, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        // Assert

        assertNotNull(listener.getDonkyException());

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

        assertNull(listener.getDonkyException());

        assertNull(listener.getValidationErrors());

        DeviceDetails deviceDetailsNew = DonkyAccountController.getInstance().getDeviceDetails();

        UserDetails userDetailsNew = DonkyAccountController.getInstance().getCurrentDeviceUser();

        assertEquals("testDisplayName", userDetailsNew.getUserDisplayName());

        assertEquals("testDeviceName", deviceDetailsNew.getDeviceName());

    }

    @Test
    public void testUpdateRegistrationWrong() throws InterruptedException {

        // Prepare

        DeviceDetails deviceDetails = DonkyAccountController.getInstance().getDeviceDetails();
        deviceDetails.setDeviceName("testDeviceName");

        UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();
        userDetails.setUserId("new_id");

        // Perform change

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().updateRegistrationDetails(userDetails, deviceDetails, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        // Assert

        assertNotNull(listener.getDonkyException());

    }

    @Test
    public void testChangeRegistration() throws InterruptedException {

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(AuthDetails.id2);
        userDetails.setUserDisplayName(AuthDetails.id2);

        synchronized (lock) {
            AuthDetails.getInstance().setDetails(AuthDetails.id2, AuthDetails.password2);
            lock.notifyAll();
        }

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().replaceRegistration(userDetails, null, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT*3);
        }

        assertNull(listener.getDonkyException());
        assertNull(listener.getValidationErrors());

        String newUserId = DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId();

        assertEquals(AuthDetails.id2, newUserId);
    }

    @Test
    public void testWrongPasswordRegistration() throws InterruptedException {

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(AuthDetails.id4);
        userDetails.setUserDisplayName(AuthDetails.id4);

        synchronized (lock) {
            AuthDetails.getInstance().setDetails(AuthDetails.id4, AuthDetails.password4);
            lock.notifyAll();
        }

        MockDonkyListener listener = new MockDonkyListener();

        DonkyAccountController.getInstance().replaceRegistration(userDetails, null, listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getDonkyException());

        synchronized (lock) {
            AuthDetails.getInstance().setDetails(AuthDetails.id2, AuthDetails.password2);
            lock.notifyAll();
        }

        MockDonkyListener listener2 = new MockDonkyListener();

        DonkyAccountController.getInstance().replaceRegistration(userDetails, null, listener2);

        synchronized (listener2) {
            listener2.wait(TIME_OUT);
        }

        assertNull(listener2.getDonkyException());
        assertNull(listener2.getValidationErrors());
    }

    @Test
    public void testWrongUserToken() throws InterruptedException {

        synchronized (lock) {
            AuthDetails.getInstance().setDetails(AuthDetails.id3, AuthDetails.password3);
            lock.notifyAll();
        }

        MockDonkyListener listener = new MockDonkyListener();

        DonkyDataController.getInstance().getConfigurationDAO().setAuthorisationToken(null);

        DonkyNetworkController.getInstance().synchronise(listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        assertNotNull(listener.getValidationErrors());
        assertEquals(true, listener.getValidationErrors().size() > 0);
    }

}
