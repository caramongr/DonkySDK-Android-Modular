package net.donky.core.sequencing;

import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.network.TagDescription;
import net.donky.core.sequencing.internal.DonkySequenceController;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The replacement for DonkyAccountController class to make account updates on the network that are executed synchronously.
 * If you use only this class to make updates of account they will be executed in the same order you made the calls.
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkySequenceAccountController {

    DonkySequenceController donkySequenceController;

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkySequenceAccountController() {}

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkySequenceAccountController INSTANCE = new DonkySequenceAccountController();
    }

    /**
     * Get instance of DonkySequencing singleton.
     *
     * @return Static instance of DonkySequencing singleton.
     */
    public static DonkySequenceAccountController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    void init() {
        donkySequenceController = new DonkySequenceController();
    }

    /**
     * Update any custom data related to the registration.
     *
     * @param userDetails   User details to be updated.
     * @param deviceDetails Device details to be updated.
     * @param listener      The callback to invoke when the command has executed. Registration errors will be fed back through this.
     */
    public void updateRegistrationDetails(final UserDetails userDetails, final DeviceDetails deviceDetails, final DonkySequenceListener listener) {
        donkySequenceController.addRegistrationUpdateTask(userDetails, deviceDetails, listener);
    }

    /**
     * Update user registration details.
     *
     * @param user     New user details.
     * @param listener Callback to invoke when task is completed.
     */
    public void updateUserDetails(final UserDetails user, final DonkySequenceListener listener) {
        donkySequenceController.addUserUpdateTask(user, listener);
    }

    /**
     * Update device registration details.
     *
     * @param deviceDetails New device details.
     * @param listener      Callback to invoke when task is completed.
     */
    public void updateDeviceDetails(final DeviceDetails deviceDetails, final DonkySequenceListener listener) {
        donkySequenceController.addDeviceUpdateTask(deviceDetails, listener);
    }

    /**
     * Updates the list of tags selected by the user on Donky Network.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void updateTags(List<TagDescription> tags, final DonkySequenceListener listener) {
        donkySequenceController.addUpdateTagsTask(tags, listener);
    }

    /**
     * Set additional properties for device registration.
     *
     * @param additionalProperties Additional properties for device registration.
     */
    public void setAdditionalProperties(TreeMap<String, String> additionalProperties, final DonkySequenceListener listener) {
        donkySequenceController.addUpdateAdditionalPropertiesTask(additionalProperties, listener);
    }
}
