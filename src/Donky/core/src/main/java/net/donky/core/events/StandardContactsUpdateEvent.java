package net.donky.core.events;

import net.donky.core.network.StandardContacts;

/**
 * Event Standard Contacts details update. Standard contacts are defined on the network messaging identities. Details about them is being send to every device registered against App Space.
 *
 * Created by Marcin Swierczek
 * 28/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class StandardContactsUpdateEvent extends LocalEvent {

    private StandardContacts standardContacts;

    public StandardContactsUpdateEvent(StandardContacts standardContacts) {
        super();
        this.standardContacts = standardContacts;
    }

    /**
     * Gets Standard Contacts details. Standard contacts are defined on the network messaging identities. Details about them is being send to every device registered against App Space.
     * @return
     */
    public StandardContacts getStandardContacts() {
        return standardContacts;
    }
}
