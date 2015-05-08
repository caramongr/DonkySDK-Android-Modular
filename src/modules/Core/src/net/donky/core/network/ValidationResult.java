package net.donky.core.network;

import android.util.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * Class representing the  validation result of generic objects.
 *
 * Created by Marcin Swierczek
 * 07/05/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ValidationResult<T> {

    /**
     * Content Notification should conform to size limit configured on the network [256K by default].
     */
    public static final String REASON_SIZE_LIMIT_EXCEEDED = "REASON_SIZE_LIMIT_EXCEEDED";

    private List<Pair<T, String>> failures;

    ValidationResult(Pair<T, String> failures) {
        this.failures = new LinkedList<>();
        this.failures.add(failures);
    }

    public ValidationResult() {
        this.failures = new LinkedList<>();
    }

    /**
     * Adds object that failed the validation procedure.
     * @param notification
     * @param reason
     */
    void addFailure(T notification, String reason) {
        failures.add(new Pair<>(notification, reason));
    }

    /**
     * Gets objects that failed the validation procedure.
     * @return
     */
    public List<Pair<T, String>> getFailures() {
        return failures;
    }
}
