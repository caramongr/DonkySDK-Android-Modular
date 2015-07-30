package net.donky.core.messaging.rich.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;

import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;
import net.donky.core.messaging.rich.ui.components.RichMessagePopUpActivity;
import net.donky.core.messaging.ui.notifications.RichMessageNotificationBuilder;
import net.donky.core.messaging.ui.notifications.RichMessagePushUIConfiguration;
import net.donky.core.network.assets.DonkyAssetController;
import net.donky.core.network.assets.NotificationImageLoader;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Internal controller for Rich MEssaging Pop-Up
 *
 * Created by Marcin Swierczek
 * 17/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichUIController {

    Context context;

    /**
     * Stores pending rich messages.
     */
    private final ConcurrentLinkedQueue<RichMessage> pendingRichMessages = new ConcurrentLinkedQueue<>();

    private int requestCode = 3294;

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final RichUIController INSTANCE = new RichUIController();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static RichUIController getInstance() {
        return SingletonHolder.INSTANCE;
    }


    void init(Context context) {

        this.context = context;

    }

    /**
     * Adds {@link RichMessage} to the queue than displays next one if application is in foreground and no other RichMessage is currently displaying. In other case
     * display Notification.
     *
     * @param richMessages RichMessages to display.
     */
    public void checkActivityForegroundAndDisplayRichMessage(List<RichMessage> richMessages) {

        if (richMessages != null) {

            for (RichMessage richMessage : richMessages) {
                if (!richMessage.isReceivedExpired()) {
                    pendingRichMessages.add(richMessage);
                }
            }

            checkActivityForegroundAndDisplayRichMessage();
        }

    }

    /**
     * Displays {@link RichMessage} from the queue if application is in foreground and no other RichMessage is currently displaying. In other case
     * display Notification.
     */
    public void checkActivityForegroundAndDisplayRichMessage() {

        Intent intent = new Intent();

        intent.setAction(RichMessagePopUpActivity.ACTIVITY_IN_FOREGROUND);

        context.sendOrderedBroadcast(intent, null, new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                int result = getResultCode();

                if (result != Activity.RESULT_CANCELED) {
                    // Activity is in foreground
                    return;
                }

                if (LifeCycleObserver.getInstance().isApplicationForegrounded()) {
                    displayPendingRichMessage();

                } else {

                    displayRichMessageNotifications();

                }

            }
        }, null, Activity.RESULT_CANCELED, null, null);
    }

    /**
     * When application is opened display all pending Rich Messages on top.
     */
    public void displayAllRichMessagesOnAppStart() {

        List<RichMessage> unreadMessages = RichMessageDataController.getInstance().getRichMessagesDAO().getUnreadRichMessages();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (RichMessage richMessage : unreadMessages) {

            Integer notificationId = richMessage.getMessageId().hashCode();

            if (manager != null && notificationId != 0) {
                manager.cancel(notificationId);
            }

        }

        List<RichMessage> richMessagesAlreadyAddedToPending = new LinkedList<>();

        for (RichMessage richMessagePending : pendingRichMessages) {

            for (RichMessage richMessageUnread : unreadMessages) {

                if (richMessagePending.getMessageId().equals(richMessageUnread.getMessageId())) {

                    richMessagesAlreadyAddedToPending.add(richMessageUnread);

                }

            }

        }

        unreadMessages.removeAll(richMessagesAlreadyAddedToPending);

        pendingRichMessages.addAll(unreadMessages);

        displayPendingRichMessage();

    }

    /**
     * Start Activity with {@link RichMessage} from the queue.
     */
    private void displayPendingRichMessage() {

        if (!pendingRichMessages.isEmpty()) {

            RichMessage richMessageToDisplay = pendingRichMessages.poll();

            context.startActivity(createRichPopupActivityIntent(richMessageToDisplay, false));

        }

    }

    /**
     * Create Notification for every pending {@link RichMessage}
     */
    private synchronized void displayRichMessageNotifications() {

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        while (!pendingRichMessages.isEmpty()) {

            final RichMessage richMessage = pendingRichMessages.poll();

            if (richMessage != null && richMessage.getMessageId() != null && !richMessage.isSilentNotification()) {

                if (!TextUtils.isEmpty(richMessage.getAvatarAssetId())) {

                    DonkyAssetController.getInstance().downloadAvatar(richMessage.getAvatarAssetId(), new NotificationImageLoader(context) {

                        @Override
                        public void success(Bitmap bitmap) {

                            displayNotification(context, notificationManager,  richMessage, getRichMessagePendingIntent(richMessage, true), null, bitmap);

                        }

                        @Override
                        public void failure(Exception e) {

                            displayNotification(context, notificationManager, richMessage, getRichMessagePendingIntent(richMessage, true), null, null);

                        }

                    });

                } else {

                    displayNotification(context, notificationManager, richMessage, getRichMessagePendingIntent(richMessage, true), null, null);

                }
            }
        }
    }

    /**
     * Create and display RichMessage notification.
     *
     * @param context Application Context.
     * @param notificationManager Notification Manager instance.
     * @param richMessage RichMessage to display when notification will be clicked.
     * @param pendingIntent PendingIntent describing Notification action.
     * @param pushUIConfiguration Configuration for the appearance of the notification.
     * @param avatar Bitmap with the avatar image to display in notification.
     */
    private void displayNotification(Context context, NotificationManager notificationManager, RichMessage richMessage, PendingIntent pendingIntent, RichMessagePushUIConfiguration pushUIConfiguration, Bitmap avatar) {

        RichMessageNotificationBuilder builder = new RichMessageNotificationBuilder(context, richMessage, pendingIntent, pushUIConfiguration, avatar);

        notificationManager.notify(richMessage.getMessageId().hashCode(), builder.build());

    }

    /**
     * Gets PendingIntent to open application main Activity.
     *
     * @return PendingIntent to open application main Activity.
     */
    private PendingIntent getApplicationPendingIntent(boolean openedFromNotification) {

        return PendingIntent.getActivity(context, requestCode, createApplicationIntent(openedFromNotification), PendingIntent.FLAG_UPDATE_CURRENT);

    }

    /**
     * Gets PendingIntent to open application RichMessage Activity.
     *
     * @param richMessageToDisplay RichMessage to display in an Activity.
     * @return
     */
    private PendingIntent getRichMessagePendingIntent(RichMessage richMessageToDisplay, boolean openedFromNotification) {

        return PendingIntent.getActivity(context, requestCode, createRichPopupActivityIntent(richMessageToDisplay, openedFromNotification), PendingIntent.FLAG_UPDATE_CURRENT);

    }

    /**
     * Gets Intent to open application main Activity.
     *
     * @return Intent to open application main Activity.
     */
    private Intent createApplicationIntent(boolean openedFromNotification) {

        String packageName = context.getApplicationContext().getPackageName();

        PackageManager pm = context.getPackageManager();

        Intent intent = pm.getLaunchIntentForPackage(packageName);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }

        intent.putExtra(LifeCycleObserver.EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION, openedFromNotification);

        return intent;
    }

    /**
     * Gets Intent to open RichMessage Activity.
     *
     * @return Intent to open RichMessage Activity.
     */
    private Intent createRichPopupActivityIntent(RichMessage richMessageToDisplay, boolean openedFromNotification) {

        Intent intent = new Intent(context, RichMessagePopUpActivity.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }

        intent.putExtra(LifeCycleObserver.EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION, openedFromNotification);

        Bundle bundle = new Bundle();

        bundle.putSerializable(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE, richMessageToDisplay);

        intent.putExtras(bundle);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;

    }

}
