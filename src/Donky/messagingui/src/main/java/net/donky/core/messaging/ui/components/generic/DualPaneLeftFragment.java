package net.donky.core.messaging.ui.components.generic;

/**
 * Interface to be implemented by left fragment in dual pane fragment. Callback for single/dual mode reconfigurations.
 *
 * Created by Marcin Swierczek
 * 18/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DualPaneLeftFragment extends DualPaneModeListener {

    /**
     * Set the interface to change overlay visibility
     *
     * @param overlayVisibilityController Interface to change overlay visibility
     */
    void setOverlayVisibilityController(OverlayVisibilityController overlayVisibilityController);

}
