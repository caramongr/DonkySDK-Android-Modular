package net.donky.core.messaging.rich.inbox.ui;

/**
 * Configuration with Activities classes that should be launched when user taps remote notifications
 *
 * Created by Marcin Swierczek
 * 28/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PushConfiguration {

    Class<?> inboxActivity;

    Class<?> messageActivity;

    /**
     * Recommended constructor.
     *
     * @param inboxActivity Activity class that implements inbox functionality
     * @param messageActivity Activity class that implements message screen functionality
     */
    public PushConfiguration(Class<?> inboxActivity, Class<?> messageActivity) {
        this.inboxActivity = inboxActivity;
        this.messageActivity = messageActivity;
    }

    /**
     * Get Activity that implements inbox functionality
     * @return Activity class that implements inbox functionality
     */
    public Class<?> getInboxActivity() {
        return inboxActivity;
    }

    /**
     * Get Activity that implements message screen functionality
     * @return Activity class that implements message screen functionality
     */
    public Class<?> getMessageActivity() {
        return messageActivity;
    }
}
