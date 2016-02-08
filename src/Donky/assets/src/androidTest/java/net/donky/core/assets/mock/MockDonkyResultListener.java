package net.donky.core.assets.mock;

import net.donky.core.DonkyException;
import net.donky.core.DonkyResultListener;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockDonkyResultListener<T> extends DonkyResultListener<T> {


    DonkyException donkyException;

    Map<String, String> validationErrors;

    T result;

    @Override
    public void success(T result) {

        this.result = result;

        synchronized (this) {
            notifyAll(  );
        }

    }

    @Override
    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

        this.donkyException = donkyException;
        this.validationErrors = validationErrors;

        synchronized (this) {
            notifyAll(  );
        }

    }

    public DonkyException getDonkyException() {
        return donkyException;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public T getResult() {
        return result;
    }

}
