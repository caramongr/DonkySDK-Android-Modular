package net.donky.core.test;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * Created by Marcin Swierczek
 * 27/03/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class IntegrationAnonymousTest extends ApplicationTestCase<Application> {

    public IntegrationAnonymousTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }


    public void testInitialiseAnonymous() throws InterruptedException {
        /*
        MockDonkyListener listener = new MockDonkyListener();

        DonkyCore.initialiseDonkySDK(getApplication(), ">>ENTER API KEY HERE<<", listener);

        synchronized (listener) {
            listener.wait(2000);
        }

        DonkyException donkyException = listener.getDonkyException();
        assertNull(donkyException);

        Map<String, String> validationErrors = listener.getValidationErrors();
        assertNull(validationErrors);*/
    }
}
