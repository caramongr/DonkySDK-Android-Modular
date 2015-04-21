package net.donky.core.messaging.rich.logic;

import net.donky.core.events.LocalEvent;
import net.donky.core.messages.RichMessage;

/**
 * Local Event representing RichMessage
 *
 * Created by Marcin Swierczek
 * 10/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageEvent extends LocalEvent {

    private RichMessage richMessage;

    private boolean receivedExpired;

    /**
     * Local Event representing RichMessage
     *
     * @param richMessage Received RichMessage
     * @param receivedExpired True if RichMessage was received expired.
     */
    public RichMessageEvent(RichMessage richMessage, boolean receivedExpired) {
        super();
        this.richMessage = richMessage;
        this.receivedExpired = receivedExpired;
    }

    /**
     * Was the RichMessage received expired.
     *
     * @return True if RichMessage was received expired.
     */
    public boolean isReceivedExpired() {
        return receivedExpired;
    }

    /**
     * Gets received RichMessage
     *
     * @return Received RichMessage
     */
    public RichMessage getRichMessage() {
        return richMessage;
    }
}