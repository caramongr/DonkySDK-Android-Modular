package net.donky.core.network.restapi;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 03/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class FailureDetails {

    private static final String FAILURE_KEY_USER_ID_ALREADY_TAKEN = "UserIdAlreadyTaken";
    private static final String PROPERTY_USER_ID = "id";

    @SerializedName("property")
    private String property;

    @SerializedName("details")
    private String details;

    @SerializedName("failureKey")
    private String failureKey;

    public String getProperty() {
        return property;
    }

    public String getDetails() {
        return details;
    }

    public String getFailureKey() {
        return failureKey;
    }

    /**
     * Checks if there was a validation error that is indicating that updating user detail failed because the id has already been taken.
     *
     * @param validationErrors Network validation errors map. Property as a key and failureKey as a value.
     * @return
     */
    public static boolean isValidationErrorMapContainingUserIdAlreadyTaken(Map<String, String> validationErrors) {

        if (validationErrors != null) {
            if (validationErrors.containsKey(PROPERTY_USER_ID) && validationErrors.get(PROPERTY_USER_ID).equals(FAILURE_KEY_USER_ID_ALREADY_TAKEN)) {
                return true;
            }
        }

        return false;
    }
}
