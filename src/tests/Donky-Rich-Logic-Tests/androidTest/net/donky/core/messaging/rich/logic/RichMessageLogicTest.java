package net.donky.core.messaging.rich.logic;

import android.app.Application;
import android.database.Cursor;
import android.test.ApplicationTestCase;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.events.CoreInitialisedSuccessfullyEvent;
import net.donky.core.messaging.rich.logic.helpers.RichMessageHelper;
import net.donky.core.messaging.rich.logic.mock.MockDonkyEventListener;
import net.donky.core.messaging.rich.logic.mock.MockDonkyListener;
import net.donky.core.messaging.rich.logic.mock.MockRichMessage;
import net.donky.core.messaging.rich.logic.mock.MockServerNotification;
import net.donky.core.messaging.rich.logic.model.DatabaseSQLContract;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;
import net.donky.core.messaging.rich.logic.model.RichMessagesDAO;
import net.donky.core.network.ServerNotification;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Marcin Swierczek
 * 15/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageLogicTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 5000;

    private static String apiKey = ">>PUT_YOUR_API_KEY_HERE<<";

    private static String initialUserId = "test_"+new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    public RichMessageLogicTest() {
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
        DonkyRichLogic.initialiseDonkyRich(getApplication(), listenerA);

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
    public void testHelperShouldReturnShareRichMessageIntent() {

        MockRichMessage richMessage = new MockRichMessage(false, false, false, false, true, true, true);

        assertNotNull(RichMessageHelper.getShareRichMessageIntent(richMessage, false));

        richMessage = new MockRichMessage(false, false, false, false, false, true, true);

        assertNull(RichMessageHelper.getShareRichMessageIntent(richMessage, false));

    }

    @Test
    public void testHelperShouldReturnIsRichMessageExpired() {

        MockRichMessage richMessage = new MockRichMessage(false, true, false, false, true, true, true);

        assertEquals(true, RichMessageHelper.isRichMessageExpired(richMessage));

        richMessage = new MockRichMessage(false, false, false, false, true, true, true);

        assertEquals(false, RichMessageHelper.isRichMessageExpired(richMessage));

    }

    @Test
    public void testRichMessageSaveAndLoadShouldBeTheSame() {

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();

        assertNotNull(richMessagesDAO);

        MockRichMessage richMessageA = new MockRichMessage(false, false, false, false, false, false, false);
        MockRichMessage richMessageB = new MockRichMessage(false, false, false, false, false, false, false);

        richMessagesDAO.saveRichMessage(richMessageA);
        richMessagesDAO.saveRichMessage(richMessageB);

        RichMessage richMessageLoadedA = richMessagesDAO.getRichMessage(richMessageA.getInternalId());
        RichMessage richMessageLoadedB = richMessagesDAO.getRichMessage(richMessageB.getInternalId());

        assertEquals(true, richMessageA.equals(richMessageLoadedA));

        assertEquals(true, richMessageB.equals(richMessageLoadedB));

        richMessagesDAO.removeAllRichMessages();

        assertEquals(true, richMessagesDAO.getAllRichMessages().isEmpty());

    }

    @Test
    public void testRichMessageSaveLoadRemoveAndLoad() {

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();

        assertNotNull(richMessagesDAO);

        MockRichMessage richMessageA = new MockRichMessage(false, false, false, false, false, false, false);
        MockRichMessage richMessageB = new MockRichMessage(false, false, false, false, false, false, false);

        richMessagesDAO.saveRichMessage(richMessageA);
        richMessagesDAO.saveRichMessage(richMessageB);

        RichMessage richMessageLoadedA = richMessagesDAO.getRichMessage(richMessageA.getInternalId());
        RichMessage richMessageLoadedB = richMessagesDAO.getRichMessage(richMessageB.getInternalId());

        assertEquals(true, richMessageA.equals(richMessageLoadedA));

        assertEquals(true, richMessageB.equals(richMessageLoadedB));

        richMessagesDAO.removeRichMessage(richMessageA.getInternalId());
        richMessagesDAO.removeRichMessage(richMessageB.getInternalId());

        assertNull(richMessagesDAO.getRichMessage(richMessageA.getInternalId()));
        assertNull(richMessagesDAO.getRichMessage(richMessageB.getInternalId()));


    }

    @Test
    public void testMarkAsRead() {

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();

        assertNotNull(richMessagesDAO);

        MockRichMessage richMessageA = new MockRichMessage(false, false, false, false, false, false, false);

        assertEquals(false, richMessageA.isMessageRead());

        richMessagesDAO.saveRichMessage(richMessageA);

        richMessagesDAO.markAsRead(richMessageA.getInternalId());

        RichMessage richMessageLoadedA = richMessagesDAO.getRichMessage(richMessageA.getInternalId());

        assertNotNull(richMessageLoadedA);

        assertEquals(true, richMessageLoadedA.isMessageRead());

    }

    @Test
    public void testGetUnread() {

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();

        assertNotNull(richMessagesDAO);

        richMessagesDAO.removeAllRichMessages();

        assertEquals(true, richMessagesDAO.getAllRichMessages().isEmpty());

        MockRichMessage richMessageA = new MockRichMessage(false, false, false, false, false, false, false);
        richMessageA.setMessageRead(false);
        richMessagesDAO.saveRichMessage(richMessageA);

        MockRichMessage richMessageB = new MockRichMessage(false, false, false, false, false, false, false);
        richMessageB.setMessageRead(false);
        richMessagesDAO.saveRichMessage(richMessageB);

        richMessagesDAO.markAsRead(richMessageA.getInternalId());

        List<RichMessage> richMessagesLoaded = richMessagesDAO.getUnreadRichMessages();

        assertEquals(1, richMessagesLoaded.size());

        RichMessage richMessageBLoaded = richMessagesLoaded.get(0);

        assertEquals(false, richMessageBLoaded.isMessageRead());

    }

    @Test
    public void testGetRichMessageWithExternalId() {

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();

        assertNotNull(richMessagesDAO);

        MockRichMessage richMessageA = new MockRichMessage(false, false, false, false, false, false, false);
        richMessagesDAO.saveRichMessage(richMessageA);

        RichMessage richMessageLoadedA = richMessagesDAO.getRichMessageWithMessageId(richMessageA.getMessageId());

        assertEquals(true, richMessageA.equals(richMessageLoadedA));

    }

    @Test
    public void testRichMessagesFiltering() {

        String textToFilter = "text_to_filter";

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();

        assertNotNull(richMessagesDAO);

        richMessagesDAO.removeAllRichMessages();

        assertEquals(true, richMessagesDAO.getAllRichMessages().isEmpty());

        MockRichMessage richMessageA = new MockRichMessage(false, false, false, false, false, false, false);
        richMessageA.setDescription(textToFilter);
        MockRichMessage richMessageB = new MockRichMessage(false, false, false, false, false, false, false);
        richMessageB.setSenderDisplayName(textToFilter);
        MockRichMessage richMessageC = new MockRichMessage(false, false, false, false, false, false, false);

        List<RichMessage> richMessagesToSave = new LinkedList<>();
        richMessagesToSave.add(richMessageA);
        richMessagesToSave.add(richMessageB);
        richMessagesToSave.add(richMessageC);

        richMessagesDAO.saveRichMessages(richMessagesToSave);

        List<RichMessage> richMessages = richMessagesDAO.getAllRichMessages();
        assertEquals(3, richMessages.size());

        Cursor richMessagesFilteredCursor = richMessagesDAO.getRichMessagesCursorForUI(textToFilter);
        assertEquals(2, richMessagesFilteredCursor.getCount());

        richMessagesFilteredCursor.moveToFirst();
        do {
            String internalId = richMessagesFilteredCursor.getString(richMessagesFilteredCursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId));
            assertEquals(true, richMessageA.getInternalId().equals(internalId) || richMessageB.getInternalId().equals(internalId));
        } while (richMessagesFilteredCursor.moveToNext());

    }

    @Test
    public void testRemovingRichMessages() {

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();
        assertNotNull(richMessagesDAO);
        richMessagesDAO.removeAllRichMessages();

        assertEquals(true, richMessagesDAO.getAllRichMessages().isEmpty());

        MockRichMessage richMessageA = new MockRichMessage(false, false, false, false, false, false, false);
        MockRichMessage richMessageB = new MockRichMessage(false, false, false, false, false, false, false);
        MockRichMessage richMessageC = new MockRichMessage(false, false, false, false, false, false, false);
        MockRichMessage richMessageD = new MockRichMessage(false, false, false, false, false, false, false);
        MockRichMessage richMessageE = new MockRichMessage(false, false, false, false, false, false, false);

        List<RichMessage> richMessagesToSave = new LinkedList<>();
        richMessagesToSave.add(richMessageA);
        richMessagesToSave.add(richMessageB);
        richMessagesToSave.add(richMessageC);
        richMessagesToSave.add(richMessageD);
        richMessagesToSave.add(richMessageE);

        richMessagesDAO.saveRichMessages(richMessagesToSave);

        List<RichMessage> richMessages = richMessagesDAO.getAllRichMessages();
        assertEquals(5, richMessages.size());

        List<RichMessage> richMessagesToRemove1 = new LinkedList<>();
        richMessagesToRemove1.add(richMessageA);
        richMessagesToRemove1.add(richMessageB);

        richMessagesDAO.removeRichMessages(richMessagesToRemove1);

        richMessages = richMessagesDAO.getAllRichMessages();
        assertEquals(3, richMessages.size());

        Set<String> ids = new HashSet<>();
        for (RichMessage richMessage : richMessages) {
            ids.add(richMessage.getInternalId());
        }

        assertEquals(false, ids.contains(richMessageA.getInternalId()));
        assertEquals(false, ids.contains(richMessageB.getInternalId()));

        List<String> idsToRemove = new LinkedList<>();
        idsToRemove.add(richMessageC.getInternalId());
        idsToRemove.add(richMessageD.getInternalId());

        richMessagesDAO.removeRichMessagesWithInternalIds(idsToRemove);

        richMessages = richMessagesDAO.getAllRichMessages();
        assertEquals(1, richMessages.size());

        assertEquals(true, richMessages.get(0).getInternalId().equals(richMessageE.getInternalId()));

    }

    @Test
    public void testRemoveMessagesThatExceededTheAvailabilityPeriod() {

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();
        assertNotNull(richMessagesDAO);
        richMessagesDAO.removeAllRichMessages();
        assertEquals(true, richMessagesDAO.getAllRichMessages().isEmpty());

        MockRichMessage richMessageA = new MockRichMessage(true, true, false, false, false, false, false);
        MockRichMessage richMessageB = new MockRichMessage(false, false, false, false, false, false, false);

        List<RichMessage> richMessagesToSave = new LinkedList<>();
        richMessagesToSave.add(richMessageA);
        richMessagesToSave.add(richMessageB);

        richMessagesDAO.saveRichMessages(richMessagesToSave);

        List<RichMessage> richMessages = richMessagesDAO.getAllRichMessages();
        assertEquals(2, richMessages.size());

        richMessagesDAO.removeMessagesThatExceededTheAvailabilityPeriod();

        richMessages = richMessagesDAO.getAllRichMessages();
        assertEquals(1, richMessages.size());

        assertEquals(true, richMessages.get(0).getInternalId().equals(richMessageB.getInternalId()));
    }

    @Test
    public void testHandleServerNotification() throws InterruptedException {

        String messageId = "51c75d24-1ca3-4971-935d-f85c2bd7034b";

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();
        assertNotNull(richMessagesDAO);
        richMessagesDAO.removeAllRichMessages();
        assertEquals(true, richMessagesDAO.getAllRichMessages().isEmpty());

        List<ServerNotification> serverNotifications = new LinkedList<>();
        MockServerNotification serverNotification = new MockServerNotification();
        serverNotifications.add(serverNotification);

        MockDonkyEventListener mockDonkyEventListener = new MockDonkyEventListener<>(RichMessageEvent.class);
        DonkyCore.subscribeToLocalEvent(mockDonkyEventListener);

        new NotificationHandler().handleRichMessageNotification(serverNotifications);
        synchronized (mockDonkyEventListener) {
            mockDonkyEventListener.wait(TIME_OUT);
        }

        RichMessage richMessageLoaded = richMessagesDAO.getRichMessageWithMessageId(messageId);

        assertNotNull(richMessageLoaded);

        assertNotNull(richMessageLoaded);
        RichMessageEvent richMessageEvent = (RichMessageEvent) mockDonkyEventListener.getEvent();

        assertEquals(messageId, richMessageEvent.getRichMessages().get(0).getMessageId());

    }

    @Test
    public void testHandleServerNotificationInDifferentTimezones() throws InterruptedException, DonkyException {

        //Since below code doesn't change the system setting
//        PackageManager pm = getApplication().getPackageManager();
//        if (pm.checkPermission("android.permission.SET_TIME_ZONE", getApplication().getPackageName()) != PackageManager.PERMISSION_GRANTED) {
//            throw new DonkyException("This test requires SET_TIME_ZONE permission");
//        }
//        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
//        String[] timeZones = TimeZone.getAvailableIDs();
//        am.setTimeZone(timeZones[i]);
        //this test will require manual TimeZone change in Settings/Date&Time

        for (int i=0; i<5; i++) {
            doTheTimeZoneTest();
            //Put a breakpoint above and change TimeZone manually (see comment above) e.g. Pacific/Midway, Europe/Sarajevo, Atlantic/Azores, Australia/Sydney
        }

    }

    private void doTheTimeZoneTest() throws InterruptedException, DonkyException {

        String expiredMessageId = "expiredMessageId";
        String notExpiredMessageId = "notExpiredMessageId";

        RichMessagesDAO richMessagesDAO = RichMessageDataController.getInstance().getRichMessagesDAO();
        assertNotNull(richMessagesDAO);
        richMessagesDAO.removeAllRichMessages();
        assertEquals(true, richMessagesDAO.getAllRichMessages().isEmpty());

        List<ServerNotification> serverNotifications = new LinkedList<>();
        MockServerNotification serverNotificationExpired = new MockServerNotification(expiredMessageId, true);
        MockServerNotification serverNotificationNotExpired = new MockServerNotification(notExpiredMessageId, false);
        serverNotifications.add(serverNotificationExpired);
        serverNotifications.add(serverNotificationNotExpired);

        MockDonkyEventListener mockDonkyEventListener = new MockDonkyEventListener<>(RichMessageEvent.class);
        DonkyCore.subscribeToLocalEvent(mockDonkyEventListener);

        new NotificationHandler().handleRichMessageNotification(serverNotifications);
        synchronized (mockDonkyEventListener) {
            mockDonkyEventListener.wait(TIME_OUT);
        }

        RichMessage expiredMessage = richMessagesDAO.getRichMessageWithMessageId(expiredMessageId);
        assertNull(expiredMessage);

        RichMessage notExpiredMessage = richMessagesDAO.getRichMessageWithMessageId(notExpiredMessageId);
        assertNotNull(notExpiredMessage);
        assertEquals(notExpiredMessage.isReceivedExpired(), false);

        RichMessageEvent richMessageEvent = (RichMessageEvent) mockDonkyEventListener.getEvent();
        List<RichMessage> richMessages = richMessageEvent.getRichMessages();
        assertNotNull(richMessages);
        assertEquals(richMessages.size(), 2);
        for (RichMessage richMessage : richMessages) {
            if (richMessage.getMessageId().equals(expiredMessageId)) {
                assertEquals(richMessage.isReceivedExpired(), true);
            } else if (richMessage.getMessageId().equals(notExpiredMessageId)) {
                assertEquals(richMessage.isReceivedExpired(), false);
            } else {
                throw new DonkyException("Wrong richMessage states, assertion failed");
            }
        }

    }
}
