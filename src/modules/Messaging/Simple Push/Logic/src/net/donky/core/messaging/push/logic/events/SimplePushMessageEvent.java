package net.donky.core.messaging.push.logic.events;

import net.donky.core.events.LocalEvent;
import net.donky.core.messaging.push.logic.SimplePushData;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marcin Swierczek
 * 10/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SimplePushMessageEvent extends LocalEvent {

    private List<SimplePushData> simplePushDataList;

    @Deprecated
    private boolean receivedExpired;

    public SimplePushMessageEvent(List<SimplePushData> simplePushDataList) {
        super();
        this.simplePushDataList = simplePushDataList;
    }

    public List<SimplePushData> getBatchSimplePushData() {
        return simplePushDataList;
    }

    /**
     * @deprecated please use SimplePushMessageEvent#SimplePushMessageEvent(List)
     * @param simplePushData
     * @param receivedExpired
     * @param receivedExpired True if RichMessage was received expired.
     */
    @Deprecated
    public SimplePushMessageEvent(SimplePushData simplePushData, boolean receivedExpired) {
        super();
        this.receivedExpired = receivedExpired;
        simplePushDataList = new LinkedList<>();
        simplePushDataList.add(simplePushData);
    }

    /**
     * @deprecated please use SimplePushMessageEvent#getBatchSimplePushData
     * @return
     */
    @Deprecated
    public SimplePushData getSimplePushData() {
        if (simplePushDataList != null && !simplePushDataList.isEmpty()) {
            return simplePushDataList.get(0);
        }
        return null;
    }

    /**
     * @deprecated This method should not be used anymore. Push Logic will not set expiry flag at this level anymore!
     * @return
     */
    @Deprecated
    public boolean isReceivedExpired() {
        return receivedExpired;
    }
}
