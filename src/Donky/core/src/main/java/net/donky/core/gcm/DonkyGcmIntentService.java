package net.donky.core.gcm;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.SynchronisationHandler;

import org.json.JSONException;

/**
 * Intent service processing received GCM messages.
 *
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyGcmIntentService extends GcmListenerService {

    private static final String KEY_DONKY_TYPE = "type";

    private static final String DONKY_TYPE_NOTIFICATIONS_PENDING = "NOTIFICATIONPENDING";

    private static final String DONKY_TYPE_MSG_REJECTED = "MSGREJECTED";

    private static final String DONKY_TYPE_RICH_MSG = "RICHMSG";

    private static final String DIRECT_MESSAGE_TYPE = "DonkyMessage";

    private static DLog log;

    /**
     * Inform DonkyGcmIntentService about incoming GCM message. Call this method in {@link GcmListenerService#onMessageReceived} method if you implemented {@link GcmListenerService} in your app.
     * If you didn't implement such class DonkyGcmIntentService will receive GCM message automatically.
     *
     * @param from describes message sender.
     * @param data message data as String key/value pairs.
     */
    public static void onDonkyMessageReceived(String from, Bundle data) {

        if (data != null) {

            String type = data.getString(KEY_DONKY_TYPE);

            if (!TextUtils.isEmpty(type)) {
                handleDonkyMessage(data, type);
            }
        }
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String type = data.getString(KEY_DONKY_TYPE);

        if (log == null) {
            log = new DLog("DonkyGcmIntentService");
        }
        log.debug("GCM received " + type);

        try {
            if (!TextUtils.isEmpty(type)) {
                if (!handleDonkyDirectMessage(data, type)) {
                    handleDonkyMessage(data, type);
                }
            }
        } catch (JSONException e) {
            log.warning("Direct GCM message processed with error.");
            DonkyNetworkController.getInstance().setReRunNotificationExchange(true);
        }
    }

    /**
     * Performs appropriate Donky Core Actions according to GCM message type.
     *
     * @param data GCM bundle delivered by the OS.
     * @param type Type of GCM message.
     */
    private static void handleDonkyMessage(final Bundle data, String type) {

        if (DONKY_TYPE_NOTIFICATIONS_PENDING.equals(type) || DONKY_TYPE_MSG_REJECTED.contentEquals(type)) {

            try {

                if (DonkyCore.isInitialised() && DonkyAccountController.getInstance().isRegistered() && !DonkyAccountController.getInstance().isUserSuspended()) {

                    if (!DonkyNetworkController.getInstance().isNotificationsSyncInProgress()) {

                        String notificationId = data.getString("notificationId");

                        if (!TextUtils.isEmpty(notificationId) && !isRichMessageType(data)) {

                            try {

                                ServerNotification serverNotification = DonkyNetworkController.getInstance().getServerNotification(notificationId);

                                SynchronisationHandler synchronisationHandler = new SynchronisationHandler(serverNotification);

                                synchronisationHandler.processServerNotifications();

                                log.info("GCM message with single notification processed successfully.");

                            } catch (DonkyException e) {

                                log.warning("Error processing single notification in GCM service. Running sync instead.");

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

                }

            } catch (Exception e) {

                Log.e("DonkyGcmIntentService", "Error processing NOTIFICATIONS_PENDING message.");

            }
        }
    }

    /**
     * Extracts the Donky message content from GCM message and process.
     *
     * @param data GCM bundle delivered by the OS.
     * @param type Type of GCM message.
     * @return True if Direct Message has been found and processed.
     */
    private static boolean handleDonkyDirectMessage(final Bundle data, String type) throws JSONException {

        boolean isPart = AssemblingManager.DIRECT_MESSAGE_PART_TYPE.equals(type);

        if (isPart || DIRECT_MESSAGE_TYPE.equals(type)) {

            ServerNotification sn;

            if (isPart || AssemblingManager.DIRECT_MESSAGE_NOTIFICATION_TYPE_RM.equals(data.getString(AssemblingManager.DIRECT_MESSAGE_NOTIFICATION_TYPE_KEY))) {

                Bundle assembledBundle = AssemblingManager.getInstance().assembleMessage(data, isPart);

                if (assembledBundle == null) {
                    return true;
                } else {
                    sn = new ServerNotification(assembledBundle);
                }
            } else {
                sn = new ServerNotification(data);
            }

            SynchronisationHandler synchronisationHandler = new SynchronisationHandler(sn);
            synchronisationHandler.processServerNotifications();
            log.info("Direct GCM message processed successfully.");

            return true;
        }

        return false;
    }

    /**
     * Check if GCM message is notifying about pending rich message.
     *
     * @param extras GCM intent extras Bundle.
     * @return
     */
    private static boolean isRichMessageType(Bundle extras) {
        String notificationType = extras.getString("notificationType");
        return !TextUtils.isEmpty(notificationType) && notificationType.equals(DONKY_TYPE_RICH_MSG);
    }
}
