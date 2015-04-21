package net.donky.core.messaging.ui.components.rich;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;

import net.donky.core.messages.RichMessage;

/**
 * Rich message notification builder.
 *
 * Created by Marcin Swierczek
 * 17/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageNotificationBuilder {

    private final Context context;

    private final PendingIntent pendingIntent;

    private RichMessagePushUIConfiguration richMessagePushUIConfiguration;

    private RichMessage richMessage;

    private Bitmap bitmap;

    /**
     * Rich message notification builder.
     *
     * @param context Application Context
     * @param richMessage Rich Message data to include into notification.
     * @param pendingIntent PendingIntent
     * @param richMessagePushUIConfiguration Description of UI configuration for notification.
     * @param avatar Avatar image.
     */
    public RichMessageNotificationBuilder(Context context, RichMessage richMessage, PendingIntent pendingIntent, RichMessagePushUIConfiguration richMessagePushUIConfiguration, Bitmap avatar) {

        this.context = context;


        if (richMessagePushUIConfiguration != null) {
            this.richMessagePushUIConfiguration = richMessagePushUIConfiguration;
        } else {
            this.richMessagePushUIConfiguration = new RichMessagePushUIConfiguration();
        }

        this.pendingIntent = pendingIntent;

        this.richMessage = richMessage;

        this.bitmap = avatar;

    }

    /**
     * Creates Interactive Push system notification.
     *
     * @return Interactive Push system notification.
     */
    public Notification build() {

        NotificationCompat.Builder builder =

                new NotificationCompat.Builder(context)
                        .setContentTitle(richMessage.getSenderDisplayName())
                        .setContentText(Html.fromHtml(handleEmptyMessage(richMessage.getDescription())))
                        .setVibrate(richMessagePushUIConfiguration.getVibratePattern())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(handleEmptyMessage(richMessage.getDescription()))).setBigContentTitle(richMessage.getSenderDisplayName()))
                        .setCategory(richMessagePushUIConfiguration.getCategory())
                        .setPriority(richMessagePushUIConfiguration.getPriority())
                        .setVisibility(richMessagePushUIConfiguration.getVisibility())
                        .setSound(richMessagePushUIConfiguration.getSoundUri())
                        .setColor(richMessagePushUIConfiguration.getIconBackgroundColor())
                        .setTicker(richMessage.getSenderDisplayName());

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        } else {
            richMessagePushUIConfiguration.getContentIntent();
        }

        builder.setAutoCancel(true);

        if (bitmap != null) {
            builder.setLargeIcon(bitmap);
        }

        builder.setSmallIcon(richMessagePushUIConfiguration.getSmallIconId());

        Notification notification = builder.build();

        notification.ledARGB = context.getResources().getColor(richMessagePushUIConfiguration.getLedColour());
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledOnMS = richMessagePushUIConfiguration.getLedOnMilliseconds();
        notification.ledOffMS = richMessagePushUIConfiguration.getLedOffMilliseconds();

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
