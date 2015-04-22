package net.donky.core.messaging.push.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;

import net.donky.core.messaging.push.logic.DonkyPushLogic;
import net.donky.core.messaging.push.logic.PushLogicController;
import net.donky.core.messaging.push.logic.SimplePushData;

/**
 * Builds System Notification for simple and interactive notifications.
 *
 * Created by Marcin Swierczek
 * 10/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SystemNotificationBuilder {

    private final int notificationId;

    /**
     * Builds System Notification for simple and interactive notifications.
     *
     * @param notificationId Notification id that should be also stored in Intent bundle to cancel notification.
     */
    SystemNotificationBuilder(int notificationId) {

        this.notificationId = notificationId;

    }

    /**
     * Creates Interactive Push system notification.
     *
     * @param context Application Context.
     * @param simplePushUIConfiguration UI configuration for system notification.
     * @param simplePushData Description of the message received from Donky Network.
     * @param avatar
     * @return Interactive Push system notification.
     */
    public Notification buildSimplePushUINotification(Context context, SimplePushUIConfiguration simplePushUIConfiguration, SimplePushData simplePushData, Bitmap avatar) {

        NotificationCompat.Builder builder =

                new NotificationCompat.Builder(context)
                        .setSmallIcon(simplePushUIConfiguration.getSmallIconId())
                        .setContentTitle(simplePushData.getSenderDisplayName())
                        .setContentText(Html.fromHtml(handleEmptyMessage(simplePushData.getBody())))
                        .setVibrate(simplePushUIConfiguration.getVibratePattern())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(handleEmptyMessage(simplePushData.getBody()))).setBigContentTitle(simplePushData.getSenderDisplayName()))
                        .setCategory(simplePushUIConfiguration.getCategory())
                        .setPriority(simplePushUIConfiguration.getPriority())
                        .setVisibility(simplePushUIConfiguration.getVisibility())
                        .setSound(simplePushUIConfiguration.getSoundUri())
                        .setColor(simplePushUIConfiguration.getIconBackgroundColor())
                        .setTicker(simplePushData.getSenderDisplayName());

        if (avatar != null) {
            builder.setLargeIcon(avatar);
        }

        if (simplePushData.getButtonSets() != null) {

            SimplePushData.ButtonSet buttonSetToUse = null;

            for (SimplePushData.ButtonSet buttonSet : simplePushData.getButtonSets()) {

                if (buttonSet != null && DonkyPushLogic.PLATFORM.equals(buttonSet.getPlatform())) {

                    buttonSetToUse = buttonSet;

                }

            }

            if (buttonSetToUse != null) {



                    if (buttonSetToUse.getButtonSetActions() != null) {

                        if (buttonSetToUse.getButtonSetActions().length == 2) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                                for (int i = 0; i < buttonSetToUse.getButtonSetActions().length; i++) {

                                    PendingIntent pendingIntent = PushLogicController.getInstance().createPendingIntent(buttonSetToUse.getButtonSetActions()[i], simplePushData, notificationId);

                                    builder.addAction(
                                            simplePushUIConfiguration.getActionButtonIconIds()[i],
                                            buttonSetToUse.getButtonSetActions()[i].getLabel(),
                                            pendingIntent
                                    );

                                }

                                builder.setAutoCancel(false);

                            } else {

                                PendingIntent pendingIntent = PushLogicController.getInstance().createPendingIntent(buttonSetToUse.getButtonSetActions()[1], simplePushData, notificationId);

                                builder.setContentIntent(pendingIntent);

                                builder.setAutoCancel(true);

                            }
                        } else if (buttonSetToUse.getButtonSetActions().length == 1) {

                            PendingIntent pendingIntent = PushLogicController.getInstance().createPendingIntent(buttonSetToUse.getButtonSetActions()[0], simplePushData, notificationId);

                            builder.setContentIntent(pendingIntent);

                            builder.setAutoCancel(true);

                        }
                    }

            } else {

                builder.setContentIntent(PushLogicController.getInstance().createPendingIntent(null, null, notificationId));
                builder.setAutoCancel(true);

            }

        } else {
            builder.setContentIntent(PushLogicController.getInstance().createPendingIntent(null, null, notificationId));
            builder.setAutoCancel(true);
        }

        Notification notification = builder.build();

        notification.ledARGB = context.getResources().getColor(simplePushUIConfiguration.getLedColour());
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledOnMS = simplePushUIConfiguration.getLedOnMilliseconds();
        notification.ledOffMS = simplePushUIConfiguration.getLedOffMilliseconds();

        return notification;
    }

    /**
     * Returns a non-empty value of message
     *
     * @param message The message
     * @return The new message
     */
    private String handleEmptyMessage(String message) {

        if (TextUtils.isEmpty(message)) {
            return "<i></i>";
        } else {
            return message;
        }

    }
}
