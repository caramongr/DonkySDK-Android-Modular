package net.donky.core.messaging.logic.events;

import net.donky.core.events.LocalEvent;

import java.util.List;

/**
 * Local event for synchronisation of read messages across devices.
 *
 * Created by Marcin Swierczek
 * 21/08/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SyncMessageReadEvent extends LocalEvent {

    private List<String> ids;

    /**
     * Local event for synchronisation of read messages across devices.
     * @param ids Message ids of messages deleted on another device.
     */
    public SyncMessageReadEvent(List<String> ids) {
        super();
        this.ids = ids;
    }

    /**
     * Gets ids of meaages read on another devices devices.
     */
    public List<String> getIds() {
        return ids;
    }
}
