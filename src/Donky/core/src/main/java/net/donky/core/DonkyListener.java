package net.donky.core;

import java.util.Map;

/**
 * Listener for asynchronous API calls.
 *
 * Created by Marcin Swierczek
 * 05/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DonkyListener {

    /**
     * Operation succeeded.
     */
    public void success();

    /**
     * Operation finished with error.
     * @param donkyException Exception or null if N/A.
     * @param validationErrors Validation errors map or null if N/A.
     */
    public void error(DonkyException donkyException, Map<String, String> validationErrors);

}
