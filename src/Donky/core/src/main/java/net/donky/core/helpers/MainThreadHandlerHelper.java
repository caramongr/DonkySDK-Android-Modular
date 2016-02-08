package net.donky.core.helpers;

import android.os.Handler;
import android.os.Looper;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 13/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MainThreadHandlerHelper<T> {

    private static Handler handler = new Handler(Looper.getMainLooper());

    public void notifySuccess(final DonkyListener donkyListener) {
        if (donkyListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    donkyListener.success();
                }
            });
        }
    }

    public void notifySuccess(final DonkyResultListener<T> donkyListener, final T result) {
        if (donkyListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    donkyListener.success(result);
                }
            });
        }
    }

    public void notifyError(final DonkyListener donkyListener, final DonkyException donkyException, final Map<String, String> validationErrors) {
        if (donkyListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    donkyListener.error(donkyException, validationErrors);
                }
            });
        }
    }

    public void notifyError(final DonkyResultListener donkyResultListener, final DonkyException donkyException, final Map<String, String> validationErrors) {
        if (donkyResultListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    donkyResultListener.error(donkyException, validationErrors);
                }
            });
        }
    }

}
