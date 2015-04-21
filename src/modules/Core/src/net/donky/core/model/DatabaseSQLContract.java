package net.donky.core.model;

import android.provider.BaseColumns;

/**
 * Contract for SQL database defining table constants.
 *
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class DatabaseSQLContract {

    public DatabaseSQLContract() {
    }

    /* Defines the table contents for Donky Client Notifications */
    public static abstract class ClientNotificationEntry implements BaseColumns {

        public static final String TABLE_NAME = "ClientNotifications";

        public static final String COLUMN_NAME_NOTIFICATION_TYPE = "notificationType";
        public static final String COLUMN_NAME_NOTIFICATION_ID = "notificationId";
        public static final String COLUMN_NAME_NOTIFICATION_JSON_STRING = "json";

        public static final String COLUMN_NAME_ACK_NOTIFICATION_ID = "serverNotificationId";
        public static final String COLUMN_NAME_ACK_NOTIFICATION_TYPE = "originalType";
        public static final String COLUMN_NAME_ACK_NOTIFICATION_RESULT = "result";
        public static final String COLUMN_NAME_ACK_NOTIFICATION_SENT_TIME = "sentTime";
        public static final String COLUMN_NAME_ACK_NOTIFICATION_CUSTOM_NOTIFICATION_TYPE = "customNotificationType";

    }

    /* Defines the table contents for RichMessages */
    public static abstract class RichMessageEntry implements BaseColumns {

        public static final String TABLE_NAME = "RichMessages";

        public static final String COLUMN_NAME_internalId = "internalId";
        public static final String COLUMN_NAME_messageRead = "messageRead";

        public static final String COLUMN_NAME_messageType = "messageType";
        public static final String COLUMN_NAME_senderExternalUserId = "senderExternalUserId";
        public static final String COLUMN_NAME_externalRef = "externalRef";
        public static final String COLUMN_NAME_description = "description";
        public static final String COLUMN_NAME_expiredBody = "expiredBody";
        public static final String COLUMN_NAME_canReply = "canReply";
        public static final String COLUMN_NAME_canForward = "canForward";
        public static final String COLUMN_NAME_canShare = "canShare";
        public static final String COLUMN_NAME_urlToShare = "urlToShare";
        public static final String COLUMN_NAME_silentNotification = "silentNotification";
        public static final String COLUMN_NAME_msgSentTimeStamp = "msgSentTimeStamp";
        public static final String COLUMN_NAME_forwardedBy = "forwardedBy";
        public static final String COLUMN_NAME_forwardingOverlayMessage = "forwardingOverlayMessage";
        public static final String COLUMN_NAME_conversationId = "conversationId";
        public static final String COLUMN_NAME_senderAccountType = "senderAccountType";
        public static final String COLUMN_NAME_senderDisplayName = "senderDisplayName";
        public static final String COLUMN_NAME_body = "body";
        public static final String COLUMN_NAME_messageScope = "messageScope";
        public static final String COLUMN_NAME_senderInternalUserId = "senderInternalUserId";
        public static final String COLUMN_NAME_senderMessageId = "senderMessageId";
        public static final String COLUMN_NAME_messageId = "messageId";
        public static final String COLUMN_NAME_contextItems = "contextItems";
        public static final String COLUMN_NAME_avatarAssetId = "avatarAssetId";
        public static final String COLUMN_NAME_sentTimestamp = "sentTimestamp";
        public static final String COLUMN_NAME_expiryTimeStamp = "expiryTimeStamp";


    }

}
