package net.donky.core.sequencing;

import android.app.Application;
import android.test.ApplicationTestCase;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.events.CoreInitialisedSuccessfullyEvent;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.helpers.IdHelper;
import net.donky.core.logging.DLog;
import net.donky.core.network.TagDescription;
import net.donky.core.sequencing.mock.MockDonkyEventListener;
import net.donky.core.sequencing.mock.MockDonkyListener;
import net.donky.core.sequencing.mock.MockDonkySequenceListener;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Marcin Swierczek
 * 15/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SequencingTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 5000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static String initialUserId = "quick_updates_test_1";//"test_"+new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    Random r = new Random();

    DLog log = new DLog("SequencingTest");

    public SequencingTest() {
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
        DonkySequencing.initialiseDonkySequencing(getApplication(), listenerA);

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

        donkyException = listenerB.getDonkyException();
        assertNull(donkyException);

        validationErrors = listenerB.getValidationErrors();
        assertNull(validationErrors);
    }

    @Test
    public void testSequencingUpdates() throws InterruptedException {

        // Create test objects

        List<UserDetails> userDetailsList = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            userDetailsList.add(generateRandomUserDetails());
        }

        List<DeviceDetails> deviceDetailsList = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            deviceDetailsList.add(generateRandomDeviceDetails());
        }

        List<List<TagDescription>> tagsList = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            tagsList.add(generateRandomTags());
        }

        List<TreeMap<String, String>> additionalPropertiesList = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            additionalPropertiesList.add(generateRandomAdditionalProperties(null));
        }

        // Create test list

        final List<TestDataForSequencing> testTimestamps = new ArrayList<>();

        // update using DonkySequenceAccountController

        for (final UserDetails userDetails : userDetailsList) {

            MockDonkySequenceListener mockDonkySequenceListener = new MockDonkySequenceListener() {

                @Override
                public void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    testTimestamps.add(new TestDataForSequencing(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp));
                    assertEquals(true, userDetails.equals(DonkyAccountController.getInstance().getCurrentDeviceUser()));
                    super.success(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    testTimestamps.add(new TestDataForSequencing(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp));
                    super.error(donkyException, validationErrors, taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                    assertEquals(true, false);
                }
            };

            DonkySequenceAccountController.getInstance().updateUserDetails(userDetails, mockDonkySequenceListener);

            synchronized (mockDonkySequenceListener) {
                mockDonkySequenceListener.wait(TIME_OUT);
            }
        }

        for (final DeviceDetails deviceDetails : deviceDetailsList) {

            MockDonkySequenceListener mockDonkySequenceListener = new MockDonkySequenceListener() {

                @Override
                public void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    testTimestamps.add(new TestDataForSequencing(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp));
                    assertEquals(true, deviceDetails.equals(DonkyAccountController.getInstance().getDeviceDetails()));
                    super.success(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    testTimestamps.add(new TestDataForSequencing(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp));
                    super.error(donkyException, validationErrors, taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                    assertEquals(true, false);
                }
            };

            DonkySequenceAccountController.getInstance().updateDeviceDetails(deviceDetails, mockDonkySequenceListener);

            synchronized (mockDonkySequenceListener) {
                mockDonkySequenceListener.wait(TIME_OUT);
            }
        }

        /*
        for (List<TagDescription> tags : tagsList) {

            MockDonkySequenceListener mockDonkySequenceListener = new MockDonkySequenceListener() {

                @Override
                public void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    testTimestamps.add(new TestDataForSequencing(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp));
                    super.success(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    testTimestamps.add(new TestDataForSequencing(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp));
                    super.error(donkyException, validationErrors, taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                    assertEquals(true, false);
                }
            };

            DonkySequenceAccountController.getInstance().updateTags(tags, mockDonkySequenceListener);

            synchronized (mockDonkySequenceListener) {
                mockDonkySequenceListener.wait(TIME_OUT);
            }
        }*/

        for (final TreeMap<String, String> additionalProperties : additionalPropertiesList) {

            MockDonkySequenceListener mockDonkySequenceListener = new MockDonkySequenceListener() {

                @Override
                public void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    testTimestamps.add(new TestDataForSequencing(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp));
                    assertEquals(true, additionalProperties.equals(DonkyAccountController.getInstance().getCurrentDeviceUser().getUserAdditionalProperties()));
                    super.success(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    testTimestamps.add(new TestDataForSequencing(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp));
                    super.error(donkyException, validationErrors, taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                    assertEquals(true, false);
                }
            };

            DonkySequenceAccountController.getInstance().setAdditionalProperties(additionalProperties, mockDonkySequenceListener);

            synchronized (mockDonkySequenceListener) {
                mockDonkySequenceListener.wait(TIME_OUT);
            }

        }

        long previousTaskCreatedTimestamp = 0;
        long previousTaskStartedTimestamp = 0;
        long previousTaskFinishedTimestamp = 0;

        for (TestDataForSequencing timestamp : testTimestamps) {

            log.debug("current created timestamp = " + timestamp.taskStartedTimestamp + "; previous created timestamp =" + previousTaskFinishedTimestamp);
            assertEquals(true, timestamp.taskCreatedTimestamp >= previousTaskCreatedTimestamp);

            log.debug("current start timestamp = " + timestamp.taskStartedTimestamp + "; previous finish timestamp =" + previousTaskFinishedTimestamp);
            assertEquals(true, timestamp.taskStartedTimestamp >= previousTaskFinishedTimestamp);

            previousTaskCreatedTimestamp = timestamp.taskCreatedTimestamp;
            previousTaskStartedTimestamp = timestamp.taskStartedTimestamp;
            previousTaskFinishedTimestamp = timestamp.taskFinishedTimestamp;
        }

    }


    @Test
    public void testUpdatingAdditionalProperties() throws InterruptedException {

        final List<String> values = new ArrayList<>();

        final String key = "testKey";
        final String value1 = DateAndTimeHelper.getUTCTimeFormated(System.currentTimeMillis()) + " FIRST";//"testValue1";


        TreeMap<String, String> startingState = new TreeMap<>();
        startingState.put(key, value1);

        MockDonkySequenceListener mockDonkySequenceListener = new MockDonkySequenceListener() {

            @Override
            public void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();
                String valueA = userDetails.getUserAdditionalProperties().get(key);
                values.add(valueA);
                assertEquals(true, value1.equals(valueA));
                super.success(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                assertEquals(true, false);
                super.error(donkyException, validationErrors, taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
            }

        };

        DonkySequenceAccountController.getInstance().setAdditionalProperties(startingState, mockDonkySequenceListener);

        synchronized (mockDonkySequenceListener) {
            mockDonkySequenceListener.wait(TIME_OUT);
        }

        for (int i = 0; i < 50; i++) {
            MockDonkySequenceListener mockDonkySequenceListenerPrim = new MockDonkySequenceListener() {

                @Override
                public void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();
                    String value = userDetails.getUserAdditionalProperties().get(key);
                    values.add(value);
                    super.success(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                    assertEquals(true, false);
                    super.error(donkyException, validationErrors, taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
                }

            };
            DonkySequenceAccountController.getInstance().setAdditionalProperties(generateRandomAdditionalProperties(key), mockDonkySequenceListenerPrim);
            synchronized (mockDonkySequenceListenerPrim) {
                mockDonkySequenceListenerPrim.wait(TIME_OUT);
            }
        }

        final String value2 = DateAndTimeHelper.getUTCTimeFormated(System.currentTimeMillis()) + " LAST";//"testValue2";

        TreeMap<String, String> endState = new TreeMap<>();
        endState.put(key, value2);

        MockDonkySequenceListener mockDonkySequenceListener2 = new MockDonkySequenceListener() {

            @Override
            public void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();
                String valueB = userDetails.getUserAdditionalProperties().get(key);
                values.add(valueB);
                assertEquals(true, value2.equals(valueB));
                super.success(taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
                assertEquals(true, false);
                super.error(donkyException, validationErrors, taskCreatedTimestamp, taskStartedTimestamp, taskFinishedTimestamp);
            }

        };

        DonkySequenceAccountController.getInstance().setAdditionalProperties(endState, mockDonkySequenceListener2);

        synchronized (mockDonkySequenceListener2) {
            mockDonkySequenceListener2.wait(TIME_OUT);
        }

        for (String str : values) {
            log.debug("Value for key=" + key + " is " + str + "/n");
        }

    }

    private UserDetails generateRandomUserDetails() {

        UserDetails userDetails = new UserDetails();

        userDetails.setUserId(IdHelper.generateId().substring(0, 3)).
                setUserDisplayName(IdHelper.generateId().substring(0, 3)).
                setUserEmailAddress("test@test" + r.nextInt(10) + ".com").
                setCountryCode("GBR").
                setUserFirstName(IdHelper.generateId().substring(0, 3)).
                setUserLastName(IdHelper.generateId().substring(0, 3)).
                setUserMobileNumber("0755555555" + r.nextInt(10)).
                setUserAdditionalProperties(generateRandomAdditionalProperties(null));

        return userDetails;
    }

    private DeviceDetails generateRandomDeviceDetails() {
        return new DeviceDetails(IdHelper.generateId().substring(0, 3), IdHelper.generateId().substring(0, 3), generateRandomAdditionalProperties(null));
    }

    private List<TagDescription> generateRandomTags() {

        List<TagDescription> selectedTags = new LinkedList<>();

        TagDescription tagDescription = new TagDescription();
        tagDescription.setValue(IdHelper.generateId().substring(0,3));
        tagDescription.setSelected(Math.random() < 0.5);
        selectedTags.add(tagDescription);

        return selectedTags;
    }

    private TreeMap<String, String> generateRandomAdditionalProperties(String testKey) {
        TreeMap<String, String> additionalProperties = new TreeMap<>();
        if (testKey == null) {
            additionalProperties.put(IdHelper.generateId().substring(0,3), DateAndTimeHelper.getUTCTimeFormated(System.currentTimeMillis()) + " MIDDLE");
        } else {
            additionalProperties.put(testKey, DateAndTimeHelper.getUTCTimeFormated(System.currentTimeMillis()) + " MIDDLE");
        }
        return additionalProperties;
    }

    private class TestDataForSequencing {

        long taskCreatedTimestamp;

        long taskStartedTimestamp;

        long taskFinishedTimestamp;

        TestDataForSequencing(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
            this.taskCreatedTimestamp = taskCreatedTimestamp;
            this.taskStartedTimestamp = taskStartedTimestamp;
            this.taskFinishedTimestamp = taskFinishedTimestamp;
        }
    }

}
