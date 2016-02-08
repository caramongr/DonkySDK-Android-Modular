package net.donky.sample.rich.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;

import net.donky.core.assets.DonkyAssetController;
import net.donky.core.assets.NotificationImageLoader;
import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.logic.RichMessageEvent;
import net.donky.core.messaging.rich.logic.model.RichMessage;

import java.util.List;

/**
 * Class to process incoming Rich Messages.
 *
 * Created by Marcin Swierczek
 * 29/01/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PushHandler {

    /**
     * For every Rich Message received by Donky Core display a Remote Notification. Clicked Notification will open associated Rich Message in WebVeiw
     * @param context Application context
     * @param event Local Event with received new Rich Messages.
     */
    void handleRichMessageEvent(Context context, RichMessageEvent event) {

        if (event != null) {

            final NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            List<RichMessage> richMessages = event.getRichMessages();
            for (RichMessage richMessage : richMessages) {
                displayNotification(context, notificationManager, richMessage);
            }
        }
    }

    void displayNotification(final Context context, final NotificationManager notificationManager, final RichMessage richMessage) {

        if (!TextUtils.isEmpty(richMessage.getAvatarAssetId())) {

            DonkyAssetController.getInstance().downloadImageAsset(
                    richMessage.getAvatarAssetId(),
                    new NotificationImageLoader(context) {

                        @Override
                        public void success(Bitmap bitmap) {
                            notificationManager.notify(richMessage.getMessageId().hashCode(),   buildRemoteNotification(context, notificationManager, richMessage, bitmap));
                        }

                        @Override
                        public void failure(Exception e) {
                            notificationManager.notify(richMessage.getMessageId().hashCode(),   buildRemoteNotification(context, notificationManager, richMessage, null));
                        }

                    });

        } else {
            notificationManager.notify(richMessage.getMessageId().hashCode(),   buildRemoteNotification(context, notificationManager, richMessage, null));
        }
    }

    /**
     * Build remote notification object to be passed to Android Notification Centre.
     * @param context Application context
     * @param notificationManager Android Notification Manager
     * @param richMessage Rich message for which the remote notification should be created.
     * @param bitmap that will be used as Avatr image in Remote Notification
     * @return Remote notification object to be passed to Android Notification Centre.
     */
    private Notification buildRemoteNotification(Context context, NotificationManager notificationManager, RichMessage richMessage, Bitmap bitmap) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(richMessage.getSenderDisplayName())
                .setContentText(
                        Html.fromHtml(handleEmptyMessage(richMessage.getDescription())))
                .setStyle(
                        new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(handleEmptyMessage(richMessage.getDescription())))
                                .setBigContentTitle(richMessage.getSenderDisplayName()))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setTicker(richMessage.getSenderDisplayName())
                .setContentIntent(createRichActivityPendingIntent(context, richMessage))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.donky_notification_small_icon_rich);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        // Sets the Avatar image
        if (bitmap != null) {
            builder.setLargeIcon(bitmap);
        }

        return builder.build();
    }

    /**
     * Returns an empty value of HTML body if message is an empty String
     *
     * @param message The HTML message body.
     * @return The new message body
     */
    private String handleEmptyMessage(String message) {

        if (TextUtils.isEmpty(message)) {
            return "<i></i>";
        } else {
            return message;
        }

    }

    /**
     * Gets Intent to open RichMessage Activity.
     *
     * @return Intent to open RichMessage Activity.
     */
    private PendingIntent createRichActivityPendingIntent(Context context, RichMessage richMessage) {

        Intent intent = new Intent(context, MainActivity.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }

        Bundle bundle = new Bundle();
        // Serialize the Rich Message Object to be received by Main Activity
        bundle.putSerializable(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE, richMessage);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Inform the Donky Core Module that the Application has been open by clicking the remote notification. Important for stats.
        intent.putExtra(LifeCycleObserver.EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION, true);

        return PendingIntent.getActivity(
                context,
                8712,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );
    }
}