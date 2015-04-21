package net.donky.core.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.SynchronisationHandler;

/**
 * Intent service processing received GCM messages.
 * <p/>
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyGcmIntentService extends IntentService {

    private static final String KEY_DONKY_TYPE = "type";

    private static final String DONKY_TYPE_NOTIFICATIONS_PENDING = "NOTIFICATIONPENDING";

    private final DLog log;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DonkyGcmIntentService() {
        super("GcmIntentService");
        log = new DLog("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            switch (messageType) {
                case GoogleCloudMessaging.
                        MESSAGE_TYPE_SEND_ERROR:
                    log.error("GCM send error");
                    break;
                case GoogleCloudMessaging.
                        MESSAGE_TYPE_DELETED:
                    log.warning("GCM deleted messages notification received");
                    break;
                case GoogleCloudMessaging.
                        MESSAGE_TYPE_MESSAGE:
                    log.info("GCM message received.");
                    if (extras.containsKey(KEY_DONKY_TYPE)) {
                        String type = extras.getString(KEY_DONKY_TYPE);
                        handleDonkyMessage(intent, type);
                    }
                    break;
            }
        }
    }

    /**
     * Performs appropriate Donky Core Actions according to GCM message type.
     *
     * @param intent GCM Intent delivered by the OS.
     * @param type   Type of GCM message.
     */
    private void handleDonkyMessage(final Intent intent, String type) {

        if (DONKY_TYPE_NOTIFICATIONS_PENDING.equals(type)) {

            try {

                if (DonkyCore.isInitialised() && DonkyAccountController.getInstance().isRegistered() && !DonkyAccountController.getInstance().isUserSuspended()) {

                    if (!DonkyNetworkController.getInstance().isNotificationsSyncInProgress()) {

                        Bundle extras = intent.getExtras();

                        String notificationId = extras.getString("notificationId");

                        if (!TextUtils.isEmpty(notificationId)) {

                            try {

                                log.debug("getServerNotification triggered from GCM id " + notificationId);

                                ServerNotification serverNotification = DonkyNetworkController.getInstance().getServerNotification(notificationId);

                                SynchronisationHandler synchronisationHandler = new SynchronisationHandler(serverNotification);

                                synchronisationHandler.processServerNotifications();

                                log.info("GCM message with single notification processed successfully.");

                            } catch (DonkyException e) {

                                log.error("Error processing single notification in GCM service. Running sync instead.", e);

                                DonkyNetworkController.getInstance().synchroniseSynchronously();

                            }

                        } else {

                            DonkyNetworkController.getInstance().synchroniseSynchronously();

                            log.info("GCM message triggered synchronise successfully.");

                        }

                    } else {

                        DonkyNetworkController.getInstance().setReRunNotificationExchange(true);

                        log.debug("Re run synchronisation scheduled. Notification synchronisation already in progress.");

                    }

                    DonkyGcmBroadcastReceiver.completeWakefulIntent(intent);

                }

            } catch (Exception e) {

                log.error("Error when processing sync from GCM.", e);

            }
        }
    }
}
