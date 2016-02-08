package net.donky.core.model;

import android.provider.BaseColumns;

/**
 * Contract for SQL database defining table constants.
 *
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DatabaseSQLContract {

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

}
