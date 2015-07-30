package net.donky.core.messaging.ui.components.generic;

/**
 * Created by Marcin Swierczek
 * 14/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface OverlayVisibilityController {

    /**
     * Set the right panel overlay visibility.
     *
     * @param visibility View.VISIBLE, View.GONE or View.INVISIBLE
     */
    void setOverlayVisibility(int visibility);
}
