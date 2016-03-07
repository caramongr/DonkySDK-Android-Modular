package net.donky.core.signalr;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationListener;
import net.donky.core.Subscription;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.LocalEvent;
import net.donky.core.logging.DLog;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.restapi.secured.SynchroniseResponse;
import net.donky.core.signalr.internal.helpers.JsonParsingHelper;
import net.donky.core.signalr.mock.MockDonkyListener;
import net.donky.core.signalr.mock.MockDonkyResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Marcin Swierczek
 * 15/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SignalRTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 5000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static String initialUserId = "test_1";//"test_"+new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    Random r = new Random();

    DLog log = new DLog("SequencingTest");

    public SignalRTest() {
        super(Application.class);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        init();
    }

    public void init() throws InterruptedException {

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

            DonkyCore.initialiseDonkySDK(getApplication(), apiKey, listener);

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
    @Deprecated
    public void testConnecting() throws InterruptedException {

        DonkySignalRController.getInstance().startSignalR();

        Object object = new Object();

        synchronized (object) {
            object.wait(TIME_OUT);
        }

        MockDonkyResultListener<SynchroniseResponse> listener = new MockDonkyResultListener<>();

        DonkySignalRController.getInstance().synchronise(new ArrayList<ClientNotification>(), listener);

        synchronized (listener) {
            listener.wait(TIME_OUT);
        }

        /*
            @Deprecated
            Will fail in current implementation, signalR won't connect unless the activity is foregrounded
         */
        assertNull(listener.getDonkyException());
        assertNotNull(listener.getResult());

    }


    @Test
    public void testParseJSON() throws InterruptedException, JSONException {

        int valueIntOne = 1;
        int valueIntTwo = 145634342;
        String keyInt = "string";

        boolean valueBool = true;
        String keyBool = "boolean";

        String valueString = "hello";
        String keyString = "hello";

        String keyArray = "array";

        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put(keyBool, valueBool);
        jsonObject1.put(keyInt,valueIntOne);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put(keyString, valueString);
        jsonObject2.put(keyInt, valueIntTwo);

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject1);
        jsonArray.put(jsonObject2);

        JSONObject jsonObject3 = new JSONObject();
        jsonObject3.put(keyArray, jsonArray);

        Map<String, Object> map = JsonParsingHelper.convertJSONObjectToMap(jsonObject3);

        List<?> list = (List<?>) map.get(keyArray);

        assertNotNull(list);

        Object obj1 = list.get(0);

        assertNotNull(obj1);

        Map<String,Object> map1 = (Map<String,Object>) obj1;

        assertEquals(true, map1.get(keyBool));
        assertEquals(true, valueIntOne == (int) map1.get(keyInt));

        Object obj2 = list.get(1);

        assertNotNull(obj2);

        Map<String,Object> map2 = (Map<String,Object>) obj2;

        assertEquals(true, valueString.equals(map2.get(keyString)));
        assertEquals(true, valueIntTwo == (int) map2.get(keyInt));
    }
}
