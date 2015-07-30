package net.donky.core.messaging.ui.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;

import net.donky.core.messaging.logic.CommonMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Rich message notification builder.
 *
 * Created by Marcin Swierczek
 * 17/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageNotificationBuilder {

    private static final int MAX_MESSAGES_TO_DISPLAY = 3;

    private final Context context;

    private final PendingIntent pendingIntent;

    private RichMessagePushUIConfiguration richMessagePushUIConfiguration;

    private List<CommonMessage> richMessages;

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
    public RichMessageNotificationBuilder(Context context, CommonMessage richMessage, PendingIntent pendingIntent, RichMessagePushUIConfiguration richMessagePushUIConfiguration, Bitmap avatar) {

        this.context = context;


        if (richMessagePushUIConfiguration != null) {
            this.richMessagePushUIConfiguration = richMessagePushUIConfiguration;
        } else {
            this.richMessagePushUIConfiguration = new RichMessagePushUIConfiguration(context);
        }

        this.pendingIntent = pendingIntent;

        this.richMessages = new LinkedList<>();
        this.richMessages.add(richMessage);

        this.bitmap = avatar;

    }

    /**
     * Rich message notification builder.
     *
     * @param context Application Context
     * @param richMessages Rich Messages data to include into notification.
     * @param pendingIntent PendingIntent
     * @param richMessagePushUIConfiguration Description of UI configuration for notification.
     * @param avatar Avatar image.
     */
    public RichMessageNotificationBuilder(Context context, List<? extends CommonMessage> richMessages, PendingIntent pendingIntent, RichMessagePushUIConfiguration richMessagePushUIConfiguration, Bitmap avatar) {

        this.context = context;

        if (richMessagePushUIConfiguration != null) {
            this.richMessagePushUIConfiguration = richMessagePushUIConfiguration;
        } else {
            this.richMessagePushUIConfiguration = new RichMessagePushUIConfiguration(context);
        }

        this.pendingIntent = pendingIntent;

        this.richMessages = new LinkedList<>();
        for (CommonMessage richMessage : richMessages) {
            this.richMessages.add(richMessage);
        }

        this.bitmap = avatar;

    }

    /**
     * Creates Interactive Push system notification.
     *
     * @return Interactive Push system notification.
     */
    public Notification build() {

        if (richMessages != null) {

            NotificationCompat.Builder builder = null;

            if (richMessages.size() == 1) {

                CommonMessage richMessage = richMessages.get(0);

                builder =

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

            } else if (richMessages.size() > 1) {

                int messagesToDisplay = Math.min(MAX_MESSAGES_TO_DISPLAY, richMessages.size());
                int theRest = richMessages.size() - messagesToDisplay;

                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                inboxStyle.setBigContentTitle(richMessages.size() + " New Messages");

                String senderDisplayNameFirst = richMessages.get(0).getSenderDisplayName();
                String descriptionFirst = richMessages.get(0).getDescription();

                if (senderDisplayNameFirst == null) {
                    senderDisplayNameFirst = "";
                }

                for (int i = 0; i < messagesToDisplay; i++) {

                    String senderDisplayName = richMessages.get(i).getSenderDisplayName();

                    if (senderDisplayName == null) {
                        senderDisplayName = "";
                    }

                    inboxStyle.addLine(senderDisplayName + ": " + richMessages.get(i).getDescription());
                }

                if (theRest > 0) {
                    inboxStyle.setSummaryText("+"+theRest+" more");
                }

                builder =
                        new NotificationCompat.Builder(context)
                                .setContentTitle(richMessages.size() + " New Messages")
                                .setContentText(senderDisplayNameFirst + ": " + descriptionFirst)
                                .setVibrate(richMessagePushUIConfiguration.getVibratePattern())
                                .setStyle(inboxStyle)
                                .setCategory(richMessagePushUIConfiguration.getCategory())
                                .setPriority(richMessagePushUIConfiguration.getPriority())
                                .setVisibility(richMessagePushUIConfiguration.getVisibility())
                                .setSound(richMessagePushUIConfiguration.getSoundUri())
                                .setColor(richMessagePushUIConfiguration.getIconBackgroundColor())
                                .setTicker(richMessages.size()+" new messages");

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

            }

            if (builder != null) {
                Notification notification = builder.build();

                notification.ledARGB = context.getResources().getColor(richMessagePushUIConfiguration.getLedColour());
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                notification.ledOnMS = richMessagePushUIConfiguration.getLedOnMilliseconds();
                notification.ledOffMS = richMessagePushUIConfiguration.getLedOffMilliseconds();

                return notification;
            }
        }

        return null;
    }

    /**
     * Returns an empty value of message
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
