package net.donky.core.events;

/**
 * Event representing new device registration against the same user account.
 *
 * Created by Marcin Swierczek
 * 28/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class NewDeviceEvent extends LocalEvent {

    private String model;

    private String operatingSystem;

    public NewDeviceEvent(String model, String operatingSystem) {
        super();
        this.model = model;
        this.operatingSystem = operatingSystem;
    }

    /**
     * Get new registration device model.
     *
     * @return New registration device model.
     */
    public String getModel() {
        return model;
    }

    /**
     * Get new registration device operating system.
     *
     * @return New registration operating system.
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }
}
