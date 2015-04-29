package net.donky.core.automation;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.automation.events.TriggerExecutedEvent;
import net.donky.core.events.DonkyEventListener;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class of Donky Automation Module.
 *
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyAutomation {

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.0.0.1";

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyAutomation() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyAutomation INSTANCE = new DonkyAutomation();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyAutomation getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Automation Module.
     *
     * @param donkyListener The callback to invoke when the Module is initialised.
     */
    public static void initialiseDonkyAutomation(final Application application, final DonkyListener donkyListener) {

        getInstance().init(donkyListener);

    }

    private void init(final DonkyListener donkyListener) {

        if (!initialised.get()) {

            try {

                DonkyCore.registerModule(new ModuleDefinition(DonkyAutomation.class.getSimpleName(), version));

                DonkyCore.subscribeToLocalEvent(new DonkyEventListener<TriggerExecutedEvent>(TriggerExecutedEvent.class) {

                    @Override
                    public void onDonkyEvent(TriggerExecutedEvent event) {

                        AutomationController.getInstance().executeThirdPartyTriggerWithKeyImmediately(event.getTriggerKey(), event.getCustomData(), new DonkyListener() {
                            @Override
                            public void success() {

                            }

                            @Override
                            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                            }
                        });

                    }

                });

                initialised.set(true);

                if (donkyListener != null) {
                    donkyListener.success();
                }

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising Automation Module");
                donkyException.initCause(e);

                if (donkyListener != null) {
                    donkyListener.error(donkyException, null);
                }

            }

        } else {

            if (donkyListener != null) {
                donkyListener.success();
            }

        }
    }
}
