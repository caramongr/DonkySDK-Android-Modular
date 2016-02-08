package net.donky.core.messaging.rich.logic.model;

import android.provider.BaseColumns;

/**
 * Created by Marcin Swierczek
 * 05/07/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DatabaseSQLContract {

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
        public static final String COLUMN_NAME_sentTimestampLong = "sentTimestampL";
    }
}
