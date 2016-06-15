package net.donky.core.messaging.rich.inbox.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import net.donky.core.assets.DonkyAssetController;
import net.donky.core.assets.NotificationImageLoader;
import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.inbox.ui.components.RichInboxAndMessageActivityWithToolbar;
import net.donky.core.messaging.rich.inbox.ui.components.RichMessageActivityWithToolbar;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.ui.notifications.RichMessageNotificationBuilder;
import net.donky.core.messaging.ui.notifications.RichMessagePushUIConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Main class to control Rich Inbox Module. Holds UI listeners and creates remote notifications if no UI listener has been registered.
 *
 * Created by Marcin Swierczek
 * 17/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichInboxUIController {

    Context context;

    CopyOnWriteArraySet<RichMessagesListener> richMessagesListeners;

    CopyOnWriteArraySet<Integer> remoteNotificationsIds;

    PushConfiguration pushConfiguration;

    private RichInboxUIController() {
        richMessagesListeners = new CopyOnWriteArraySet<>();
        remoteNotificationsIds = new CopyOnWriteArraySet<>();
    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final RichInboxUIController INSTANCE = new RichInboxUIController();
    }

    /**
     * Get instance of Donky Rich Inbox Controller singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static RichInboxUIController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    void init(Context context, PushConfiguration pushConfiguration) {
        this.context = context;
        this.pushConfiguration = pushConfiguration;
    }

    /**
     * Add UI component listener for incoming rich messages. If there is no listener registered SDk will display remote notification.
     *
     * @param richMessagesListener Incoming rich messages.
     */
    public void registerRichMessagesListener(RichMessagesListener richMessagesListener) {
        richMessagesListeners.add(richMessagesListener);
    }

    /**
     * Remove UI component listener for incoming rich messages. If there is no listener registered SDk will display remote notification.
     *
     * @param richMessagesListener Incoming rich messages.
     */
    public void unregisterRichMessagesListener(RichMessagesListener richMessagesListener) {
        richMessagesListeners.remove(richMessagesListener);
    }

    /**
     * Notify UI components about incoming {@link RichMessage}s
     * @param richMessages Incoming rich mesages.
     */
    public void notifyListeners(List<RichMessage> richMessages) {

        if (richMessages != null) {

            for (RichMessagesListener listener : richMessagesListeners) {
                listener.onUpdate(richMessages);
            }

            if (richMessagesListeners.isEmpty()) {
                displayRichMessageNotification(richMessages);
            }

        } else {

            for (RichMessagesListener listener : richMessagesListeners) {
                listener.onUpdate(null);
            }

        }
    }

    /**
     * Create Notification for {@link RichMessage}
     */
    private synchronized void displayRichMessageNotification(final List<RichMessage> richMessages) {

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (richMessages != null && richMessages.size() == 1) {

            for (final RichMessage richMessage : richMessages) {

                if (richMessage != null && richMessage.getMessageId() != null && !richMessage.isSilentNotification() && !richMessage.isReceivedExpired()) {

                    if (!TextUtils.isEmpty(richMessage.getAvatarAssetId())) {

                            DonkyAssetController.getInstance().downloadImageAsset(richMessage.getAvatarAssetId(), new NotificationImageLoader(context) {

                            @Override
                            public void success(Bitmap bitmap) {

                                displayNotification(context, notificationManager, richMessage, createRichActivityPendingIntent(richMessage), null, bitmap);

                            }

                            @Override
                            public void failure(Exception e) {

                                displayNotification(context, notificationManager, richMessage, createRichActivityPendingIntent(richMessage), null, null);

                            }

                        });

                    } else {

                        displayNotification(context, notificationManager, richMessage, createRichActivityPendingIntent(richMessage), null, null);

                    }
                }
            }

        } else if (richMessages != null && richMessages.size() > 1){

            List<RichMessage> nonExpiredRichMessages = new ArrayList<>();

            boolean isSilent = true;

            for (RichMessage richMessage : richMessages) {
                if (!richMessage.isSilentNotification()) {
                    isSilent = false;
                    break;
                }
                if (!richMessage.isReceivedExpired()) {
                    nonExpiredRichMessages.add(richMessage);
                }
            }

            if (!isSilent && !nonExpiredRichMessages.isEmpty()) {
                displayNotification(context, notificationManager, nonExpiredRichMessages, createRichInboxPendingIntent(), null, null);
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

        trackRemoteNotificationWithId(richMessage.getMessageId().hashCode());

    }

    /**
     * Create and display RichMessage notification.
     *
     * @param context Application Context.
     * @param notificationManager Notification Manager instance.
     * @param richMessages RichMessages to display when notification will be clicked.
     * @param pendingIntent PendingIntent describing Notification action.
     * @param pushUIConfiguration Configuration for the appearance of the notification.
     * @param avatar Bitmap with the avatar image to display in notification.
     */
    private void displayNotification(Context context, NotificationManager notificationManager, List<RichMessage> richMessages, PendingIntent pendingIntent, RichMessagePushUIConfiguration pushUIConfiguration, Bitmap avatar) {

        RichMessageNotificationBuilder builder = new RichMessageNotificationBuilder(context, richMessages, pendingIntent, pushUIConfiguration, avatar);

        Integer randomId = new Random().nextInt(Integer.MAX_VALUE);

        notificationManager.notify(randomId, builder.build());

        trackRemoteNotificationWithId(randomId);

    }

    /**
     * Gets Intent to open application main Activity.
     *
     * @return Intent to open application main Activity.
     */
    private Intent createApplicationIntent() {

        String packageName = context.getApplicationContext().getPackageName();

        PackageManager pm = context.getPackageManager();

        Intent intent = pm.getLaunchIntentForPackage(packageName);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }

        intent.putExtra(LifeCycleObserver.EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION, true);

        return intent;
    }

    /**
     * Gets Intent to open RichMessage Activity.
     *
     * @return Intent to open RichMessage Activity.
     */
    private PendingIntent createRichActivityPendingIntent(RichMessage richMessageToDisplay) {

        Class<?> messageActivityClass;

        if (pushConfiguration != null && pushConfiguration.getMessageActivity() != null) {
            messageActivityClass = pushConfiguration.getMessageActivity();
        } else {
            messageActivityClass = RichMessageActivityWithToolbar.class;
        }

        Intent intent = new Intent(context, messageActivityClass);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE, richMessageToDisplay);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(LifeCycleObserver.EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION, true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addNextIntent(createApplicationIntent());
        //stackBuilder.addNextIntent(new Intent(context,RichInboxAndMessageActivity.class));
        stackBuilder.addNextIntent(intent);

        int requestCode;

        if (richMessageToDisplay != null && !TextUtils.isEmpty(richMessageToDisplay.getMessageId())) {
            requestCode = richMessageToDisplay.getMessageId().hashCode();
        } else {
            requestCode = 8712;
        }

        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_ONE_SHOT);

    }

    /**
     * Gets Intent to open Rich Message Inbox Activity.
     *
     * @return Intent to open Rich Message Inbox Activity.
     */
    private PendingIntent createRichInboxPendingIntent() {

        Class<?> messageInboxClass;

        if (pushConfiguration != null && pushConfiguration.getInboxActivity() != null) {
            messageInboxClass = pushConfiguration.getInboxActivity();
        } else {
            messageInboxClass = RichInboxAndMessageActivityWithToolbar.class;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addNextIntent(createApplicationIntent());
        stackBuilder.addNextIntent(new Intent(context, messageInboxClass));

        int requestCode = 8711;
        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_ONE_SHOT );

    }

    public void trackRemoteNotificationWithId(Integer notificationId) {
        remoteNotificationsIds.add(notificationId);
    }

    public void removeTrackedRemoteNotifications() {

        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for (Integer id : remoteNotificationsIds) {
            notificationManager.cancel(id);
        }

        remoteNotificationsIds.clear();

    }
}
