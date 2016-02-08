package net.donky.core.messaging.ui.intents;

import android.content.Intent;

import net.donky.core.messaging.logic.model.Contact;

import java.util.List;

/**
 * Created by Marcin Swierczek
 * 22/10/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface ChatIntentsProvider {

    Intent getOpenGroupConversationIntent(List<Contact> contacts);

    Intent getOpenSingleConversationIntent(Contact contact);

}
