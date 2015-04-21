package net.donky.core.automation.events;

import net.donky.core.events.LocalEvent;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class TriggerExecutedEvent extends LocalEvent {

    String triggerKey;

    Map<String, String> customData;

    public TriggerExecutedEvent(String triggerKey, Map<String, String> customData) {
        super();

        this.triggerKey = triggerKey;
        this.customData = customData;
    }

    public String getTriggerKey() {
        return triggerKey;
    }

    public Map<String, String> getCustomData() {
        return customData;
    }
}
