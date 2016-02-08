package net.donky.core.messaging.ui.components.generic;

/**
 * List selection listener.
 *
 * Created by Marcin Swierczek
 * 18/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface SelectionListener<T> {

    /**
     * List element representing given object has been selected.
     *
     * @param selected Details for selected list row.
     * @param isSplitViewMode True if the screen is spilt between list and detail fragments
     */
    void onSelected(T selected, boolean isSplitViewMode);

    void onSelectedNew(T selected);
}