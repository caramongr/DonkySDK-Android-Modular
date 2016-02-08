package net.donky.core;

import java.util.Map;

/**
 * General Exception thrown by Donky Core SDK.
 *
 * Created by Marcin Swierczek
 * 13/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyException extends Exception {

    /**
     * Map of validation errors.
     */
    private Map<String, String> validationErrors;

    /**
     * @param description Exception description.
     */
    public DonkyException(String description) {
        super(description);
    }

    /**
     * @param exception Exception that is a cause.
     */
    public DonkyException(Exception exception) {
        super(exception.getLocalizedMessage());
        initCause(exception);
    }

    /**
     * @param description Exception description.
     * @param
     */
    public DonkyException(String description, Map<String, String> validationErrors) {
        super(description);
        this.validationErrors = validationErrors;
    }

    /**
     * Get map of validation errors. Null if N/A
     * @return Map of validation errors. Null if N/A
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
