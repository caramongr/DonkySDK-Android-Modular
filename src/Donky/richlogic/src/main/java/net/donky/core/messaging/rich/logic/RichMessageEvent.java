package net.donky.core.messaging.rich.logic;

import net.donky.core.events.LocalEvent;
import net.donky.core.messaging.rich.logic.model.RichMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Local Event representing RichMessage
 *
 * Created by Marcin Swierczek
 * 10/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageEvent extends LocalEvent {

    private List<RichMessage> richMessages;

    @Deprecated
    private boolean receivedExpired;

    /**
     * Local Event representing RichMessage
     *
     * @param richMessages Received RichMessages
     */
    public RichMessageEvent(List<RichMessage> richMessages) {
        super();
        this.richMessages = richMessages;
    }

    /**
     * Gets received RichMessage
     *
     * @return Received RichMessage
     */
    public List<RichMessage> getRichMessages() {
        return richMessages;
    }

    /**
     * Local Event representing RichMessage
     * @deprecated please use RichMessageEvent#RichMessageEvent(List)
     * @param richMessage Received RichMessages
     * @param receivedExpired True if SimplePush was received expired.
     */
    @Deprecated
    public RichMessageEvent(RichMessage richMessage, boolean receivedExpired) {
        super();
        this.receivedExpired = receivedExpired;
        richMessages = new LinkedList<>();
        richMessages.add(richMessage);
    }

    /**
     * @deprecated please use SimplePushMessageEvent#getBatchSimplePushData
     * @return
     */
    @Deprecated
    public RichMessage getRichMessage() {
        if (richMessages != null && !richMessages.isEmpty()) {
            return richMessages.get(0);
        }
        return null;
    }

    /**
     * @deprecated This method should not be used anymore. Rich Logic will not set expiry flag at this level anymore!
     * @return
     */
    @Deprecated
    public boolean isReceivedExpired() {
        return receivedExpired;
    }
}