package net.donky.core.messaging.logic.database.listeners;

/**
 * Created by Marcin Swierczek
 * 09/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DonkyDeleteListener {
    void onDeleteComplete(int token, Object cookie, int result);
}
