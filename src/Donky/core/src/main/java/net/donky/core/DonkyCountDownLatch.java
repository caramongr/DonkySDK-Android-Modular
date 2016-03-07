package net.donky.core;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Simple DonkyCountDownLatch extention to remember errors.
 *
 * Created by Marcin Swierczek
 * 11/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyCountDownLatch<T> extends CountDownLatch {

    DonkyException donkyException;

    Map<String, String> validationErrors;

    T result;

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked
     *              before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public DonkyCountDownLatch(int count) {
        super(count);
    }

    public DonkyException getDonkyException() {
        return donkyException;
    }

    public void setError(DonkyException donkyException, Map<String, String> validationErrors) {
        this.donkyException = donkyException;
        this.validationErrors = validationErrors;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
};
