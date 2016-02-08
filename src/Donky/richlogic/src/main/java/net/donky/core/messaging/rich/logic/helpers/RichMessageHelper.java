package net.donky.core.messaging.rich.logic.helpers;

import android.content.Intent;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.messaging.logic.MessagingInternalController;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.model.DonkyDataController;
import net.donky.core.settings.AppSettings;

import java.util.Date;

/**
 * Helper for rich messaging modules.
 *
 * Created by Marcin Swierczek
 * 21/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageHelper {

    private static final String shareTag = "[{]UrlToShare[}]";

    /**
     * Returns Intent for sharing rich message.
     *
     * @param message Message text to send.
     */
    public static Intent getShareRichMessageIntent(RichMessage message, boolean sendMessageSharedNotification) {

        if (message != null && message.isCanShare()) {

            String shareMessage = AppSettings.getInstance().getShareMessage();
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage.replaceAll(shareTag, message.getUrlToShare()));
            shareIntent.setType("text/plain");

            // Temporary solution
            if (sendMessageSharedNotification) {
                MessagingInternalController.getInstance().queueMessageSharedNotification(message, "unknown");
            }

            return shareIntent;

        } else {
            return null;
        }

    }

    /**
     * Check if rich message has expired message.
     *
     * @param richMessage Rich message to examine.
     * @return True if rich message has expired.
     */
    public static boolean isRichMessageExpired(RichMessage richMessage) {

        if (richMessage != null) {

            Date dateSent = DateAndTimeHelper.parseUtcDate(richMessage.getSentTimestamp());
            Date dateExpiry = DateAndTimeHelper.parseUtcDate(richMessage.getExpiryTimeStamp());
            Integer availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();

            return DateAndTimeHelper.isExpired(dateSent, dateExpiry, new Date(), availabilityDays);

        } else {

            return false;
        }
    }

}
