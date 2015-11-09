package net.donky.core.messaging.ui.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import net.donky.core.messaging.logic.model.CommonMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Chat message notification builder.
 *
 * Created by Marcin Swierczek
 * 17/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ChatMessageNotificationBuilder {

    private static final int MAX_MESSAGES_TO_DISPLAY = 3;

    private final Context context;

    private final PendingIntent pendingIntent;

    private ChatMessagePushUIConfiguration chatMessagePushUIConfiguration;

    private List<CommonMessage> chatMessages;

    private Bitmap bitmap;

    /**
     * chat message notification builder.
     *
     * @param context Application Context
     * @param chatMessage chat Message data to include into notification.
     * @param pendingIntent PendingIntent
     * @param chatMessagePushUIConfiguration Description of UI configuration for notification.
     * @param avatar Avatar image.
     */
    public ChatMessageNotificationBuilder(Context context, CommonMessage chatMessage, PendingIntent pendingIntent, ChatMessagePushUIConfiguration chatMessagePushUIConfiguration, Bitmap avatar) {

        this.context = context;


        if (this.chatMessagePushUIConfiguration != null) {
            this.chatMessagePushUIConfiguration = chatMessagePushUIConfiguration;
        } else {
            this.chatMessagePushUIConfiguration = new ChatMessagePushUIConfiguration(context);
        }

        this.pendingIntent = pendingIntent;

        this.chatMessages = new LinkedList<>();
        this.chatMessages.add(chatMessage);

        this.bitmap = avatar;

    }

    /**
     * chat message notification builder.
     *
     * @param context Application Context
     * @param chatMessages chat Messages data to include into notification.
     * @param pendingIntent PendingIntent
     * @param chatMessagePushUIConfiguration Description of UI configuration for notification.
     * @param avatar Avatar image.
     */
    public ChatMessageNotificationBuilder(Context context, List<? extends CommonMessage> chatMessages, PendingIntent pendingIntent, ChatMessagePushUIConfiguration chatMessagePushUIConfiguration, Bitmap avatar) {

        this.context = context;

        if (chatMessagePushUIConfiguration != null) {
            this.chatMessagePushUIConfiguration = chatMessagePushUIConfiguration;
        } else {
            this.chatMessagePushUIConfiguration = new ChatMessagePushUIConfiguration(context);
        }

        this.pendingIntent = pendingIntent;

        this.chatMessages = new LinkedList<>();
        for (CommonMessage chatMessage : chatMessages) {
            this.chatMessages.add(chatMessage);
        }

        this.bitmap = avatar;

    }

    /**
     * Creates Interactive Push system notification.
     *
     * @return Interactive Push system notification.
     */
    public Notification build() {

        if (chatMessages != null) {

            NotificationCompat.Builder builder = null;

            if (chatMessages.size() == 1) {

                CommonMessage chatMessage = chatMessages.get(0);

                builder =

                        new NotificationCompat.Builder(context)
                                .setContentTitle(chatMessage.getSenderDisplayName())
                                .setContentText(chatMessage.getBody())
                                .setVibrate(chatMessagePushUIConfiguration.getVibratePattern())
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(chatMessage.getBody()).setBigContentTitle(chatMessage.getSenderDisplayName()))
                                .setCategory(chatMessagePushUIConfiguration.getCategory())
                                .setPriority(chatMessagePushUIConfiguration.getPriority())
                                .setVisibility(chatMessagePushUIConfiguration.getVisibility())
                                .setSound(chatMessagePushUIConfiguration.getSoundUri())
                                .setColor(chatMessagePushUIConfiguration.getIconBackgroundColor())
                                .setTicker(chatMessage.getSenderDisplayName());

                if (pendingIntent != null) {
                    builder.setContentIntent(pendingIntent);
                } else {
                    chatMessagePushUIConfiguration.getContentIntent();
                }

                builder.setAutoCancel(true);

                if (bitmap != null) {
                    builder.setLargeIcon(bitmap);
                }

                builder.setSmallIcon(chatMessagePushUIConfiguration.getSmallIconId());

            } else if (chatMessages.size() > 1) {

                int messagesToDisplay = Math.min(MAX_MESSAGES_TO_DISPLAY, chatMessages.size());
                int theRest = chatMessages.size() - messagesToDisplay;

                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                inboxStyle.setBigContentTitle(chatMessages.size() + " New Messages");

                String senderDisplayNameFirst = chatMessages.get(0).getSenderDisplayName();
                String descriptionFirst = chatMessages.get(0).getBody();

                if (senderDisplayNameFirst == null) {
                    senderDisplayNameFirst = "";
                }

                for (int i = 0; i < messagesToDisplay; i++) {

                    String senderDisplayName = chatMessages.get(i).getSenderDisplayName();

                    if (senderDisplayName == null) {
                        senderDisplayName = "";
                    }

                    inboxStyle.addLine(senderDisplayName + ": " + chatMessages.get(i).getBody());
                }

                if (theRest > 0) {
                    inboxStyle.setSummaryText("+"+theRest+" more");
                }

                builder =
                        new NotificationCompat.Builder(context)
                                .setContentTitle(chatMessages.size() + " New Messages")
                                .setContentText(senderDisplayNameFirst + ": " + descriptionFirst)
                                .setVibrate(chatMessagePushUIConfiguration.getVibratePattern())
                                .setStyle(inboxStyle)
                                .setCategory(chatMessagePushUIConfiguration.getCategory())
                                .setPriority(chatMessagePushUIConfiguration.getPriority())
                                .setVisibility(chatMessagePushUIConfiguration.getVisibility())
                                .setSound(chatMessagePushUIConfiguration.getSoundUri())
                                .setColor(chatMessagePushUIConfiguration.getIconBackgroundColor())
                                .setTicker(chatMessages.size()+" new messages");

                if (pendingIntent != null) {
                    builder.setContentIntent(pendingIntent);
                } else {
                    chatMessagePushUIConfiguration.getContentIntent();
                }

                builder.setAutoCancel(true);

                if (bitmap != null) {
                    builder.setLargeIcon(bitmap);
                }

                builder.setSmallIcon(chatMessagePushUIConfiguration.getSmallIconId());

            }

            if (builder != null) {
                Notification notification = builder.build();

                notification.ledARGB = context.getResources().getColor(chatMessagePushUIConfiguration.getLedColour());
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                notification.ledOnMS = chatMessagePushUIConfiguration.getLedOnMilliseconds();
                notification.ledOffMS = chatMessagePushUIConfiguration.getLedOffMilliseconds();

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
