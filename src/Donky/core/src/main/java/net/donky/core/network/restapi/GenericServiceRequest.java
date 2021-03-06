package net.donky.core.network.restapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.donky.core.logging.DLog;
import net.donky.core.network.OnConnectionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import retrofit.mime.TypedInput;

/**
 * Base class for all network request objects.
 *
 * Created by Marcin Swierczek
 * 06/05/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class GenericServiceRequest extends OnConnectionListener {

    private FailureDetails[] validationFailureDetails;

    /**
     * Reads input stream from service response and decodes it to string.
     *
     * @param body Typed input stream to decode.
     * @return String decoded from typed input stream.
     */
    protected String readInputStream(TypedInput body) {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }

            return out.toString();

        } catch (IOException e) {

            new DLog("ServiceRequest").error("Client Bad Request and response body processing",e);

            return null;
        }
    }

    protected FailureDetails[] parseFailureDetails(String detailsJson) {

        try {

            Gson gson = new GsonBuilder().create();
            validationFailureDetails = gson.fromJson(detailsJson, FailureDetails[].class);

        } catch (Exception e) {
            new DLog("ServiceRequest").warning("error parsing validation failure data");
        }

        return validationFailureDetails;
    }

    /**
     * Gets network failure errors.
     *
     * @return Network failure errors.
     */
    public FailureDetails[] getValidationFailureDetails() {
        return validationFailureDetails;
    }

    /**
     * Gets map of network failure errors. Key is the property that caused the failure. Value is the failureKey String. Starting from v2.2.0.1 SDK is relaying on this definition.
     *
     * @return Map of network failure errors.
     */
    public Map<String, String> getValidationFailures() {

        Map<String, String> failureMap = new HashMap<>();

        if (validationFailureDetails != null) {

            for (FailureDetails failure : validationFailureDetails) {

                failureMap.put(failure.getProperty(), failure.getFailureKey());

            }
        }

        return failureMap.isEmpty() ? null : failureMap;
    }
}
