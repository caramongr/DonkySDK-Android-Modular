package net.donky.core.messaging.rich.inbox.ui;

import net.donky.core.messaging.rich.logic.model.RichMessage;

import java.util.List;

/**
 * UI Listener for incoming Rich Messages.
 *
 * Created by Marcin Swierczek
 * 11/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface RichMessagesListener {

    /**
     * Update UI that displays available rich messages.
     * @param richMessages New Rich Messages, can be null
     */
    void onUpdate(List<RichMessage> richMessages);

}
