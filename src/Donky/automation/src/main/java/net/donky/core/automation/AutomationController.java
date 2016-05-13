package net.donky.core.automation;

import net.donky.core.DonkyListener;
import net.donky.core.network.DonkyNetworkController;

import java.util.Map;

/**
 * Internal Controller for Automation Module.
 *
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AutomationController {


    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private AutomationController() {

    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final AutomationController INSTANCE = new AutomationController();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static AutomationController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Queue 'Third Party Trigger' Notification.
     *
     * @param triggerKey Custom key for trigger.
     * @param customData Additional data to be send with notification.
     */
    public void executeThirdPartyTrigger(final String triggerKey, final Map<String, String> customData) {

        DonkyNetworkController.getInstance().queueClientNotification(ClientNotification.createExecuteThirdPartyTriggersNotification(triggerKey, customData));

    }

    /**
     * Queue and send 'Third Party Trigger' Notification.
     *
     * @param triggerKey Custom key for trigger.
     * @param customData Additional data to be send with notification.
     * @param listener Callback to be invoked when completed.
     */
    public void executeThirdPartyTriggerWithKeyImmediately(final String triggerKey, final Map<String, String> customData, final DonkyListener listener) {

        DonkyNetworkController.getInstance().queueClientNotification(ClientNotification.createExecuteThirdPartyTriggersNotification(triggerKey, customData));
        DonkyNetworkController.getInstance().synchronise(listener);

    }
}
