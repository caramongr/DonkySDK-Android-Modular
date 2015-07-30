package net.donky.core.messaging.rich.ui;

import android.content.Context;
import android.os.Handler;

import net.donky.core.DonkyCore;
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

        if (event != null) {

            if (!DonkyCore.getInstance().isModuleRegistered("DonkyCore","2.0.0.3")) {

                new DLog("RichUIEventHandler").error("Donky Core minimal version 2.0.0.3 required.");

            } else {

                RichUIController.getInstance().checkActivityForegroundAndDisplayRichMessage(event.getRichMessages());

            }

        } else {

            new DLog("handleRichMessageEvent").info("RichMessage received expired.");
        }

    }

}
