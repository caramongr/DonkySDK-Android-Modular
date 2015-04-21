package net.donky.core.events;

/**
 * Represent Event raised by Donky Core library or another Donky Module.
 *
 * Created by Marcin Swierczek
 * 17/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LocalEvent {

    private String localEventType;

    /**
     * Base class for all local Donky events.
     *
     */
    public LocalEvent() {

        this.localEventType = this.getClass().getSimpleName();

    }

    /**
     * Type of local event
     *
     * @return Type of local event
     */
    public String getLocalEventType() {
        return localEventType;
    }
}