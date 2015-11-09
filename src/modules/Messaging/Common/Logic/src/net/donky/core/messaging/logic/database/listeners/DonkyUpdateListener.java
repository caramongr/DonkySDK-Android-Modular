package net.donky.core.messaging.logic.database.listeners;

/**
 * Created by Marcin Swierczek
 * 09/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DonkyUpdateListener {
    void onUpdateComplete(int token, Object cookie, int result);
}
