package net.donky.core.messaging.logic.database.listeners;

import android.net.Uri;

/**
 * Created by Marcin Swierczek
 * 09/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DonkyInsertListener {
    void onInsertComplete(int token, Object cookie, Uri uri);
}
