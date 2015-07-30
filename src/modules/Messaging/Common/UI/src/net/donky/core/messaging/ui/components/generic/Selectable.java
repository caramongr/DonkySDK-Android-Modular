package net.donky.core.messaging.ui.components.generic;

/**
 * Interface for fragments that want to register listeners e.g. for list item selection.
 *
 * Created by Marcin Swierczek
 * 19/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface Selectable<T> {

    /**
     * Sets the listener for generic class object selection.
     *
     * @param selectionListener Listener to set.
     */
    void setSelectionListener(SelectionListener<T> selectionListener);

}
