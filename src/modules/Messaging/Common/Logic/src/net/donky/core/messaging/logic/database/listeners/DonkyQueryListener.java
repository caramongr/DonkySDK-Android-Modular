package net.donky.core.messaging.logic.database.listeners;

import android.database.Cursor;

/**
 * Created by Marcin Swierczek
 * 09/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DonkyQueryListener {

    void onQueryComplete(int token, Object cookie, Cursor cursor);

}
