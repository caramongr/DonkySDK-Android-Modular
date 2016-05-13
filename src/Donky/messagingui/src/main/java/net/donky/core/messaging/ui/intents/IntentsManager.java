package net.donky.core.messaging.ui.intents;

/**
 * Created by Marcin Swierczek
 * 23/10/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class IntentsManager {

    ChatIntentsProvider chatIntentsProvider;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private IntentsManager() {

    }

    public boolean isChatAvailable() {
        return chatIntentsProvider != null;
    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final IntentsManager INSTANCE = new IntentsManager();
    }

    /**
     * Get instance of Donky Messaging UI singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static IntentsManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public ChatIntentsProvider getChatIntentsProvider() {
        return chatIntentsProvider;
    }

    public void setChatIntentsProvider(ChatIntentsProvider chatIntentsProvider) {
        this.chatIntentsProvider = chatIntentsProvider;
    }
}
