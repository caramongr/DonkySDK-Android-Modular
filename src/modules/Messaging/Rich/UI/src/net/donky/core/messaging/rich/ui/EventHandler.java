package net.donky.core.messaging.rich.ui;

import android.content.Context;
import android.os.Handler;

import net.donky.core.events.ApplicationStartEvent;
import net.donky.core.logging.DLog;
import net.donky.core.messaging.rich.logic.RichMessageEvent;

/**
 * Class to handle Local Events.
 *
 * Created by Marcin Swierczek
 * 14/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class EventHandler {

    Context context;

    EventHandler(Context context) {

        this.context = context;

    }

    /**
     * Handle Application start event.
     *
     * @param event Application start event.
     */
    public void handleApplicationStartEvent(ApplicationStartEvent event) {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RichUIController.getInstance().displayAllRichMessagesOnAppStart();

            }
        }, 500);

    }

    /**
     * Handle RichMessage received event.
     *
     * @param event RichMessage received event.
     */
    public void handleRichMessageEvent(RichMessageEvent event) {

        if (event != null && !event.isReceivedExpired()) {

            RichUIController.getInstance().checkActivityForegroundAndDisplayRichMessage(event.getRichMessage());

        } else {

            new DLog("handleRichMessageEvent").info("RichMessage received expired.");
        }

    }

}
