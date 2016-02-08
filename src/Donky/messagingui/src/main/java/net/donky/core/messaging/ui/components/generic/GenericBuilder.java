package net.donky.core.messaging.ui.components.generic;

/**
 * Builder interface for generic templated classes that need to create generic class instance.
 *
 * Created by Marcin Swierczek
 * 18/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface GenericBuilder<T> {

    /**
     * Gets the instance.
     *
     * @return Class instance.
     */
    T build();

}
