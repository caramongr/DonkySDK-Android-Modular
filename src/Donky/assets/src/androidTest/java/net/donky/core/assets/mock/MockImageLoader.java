package net.donky.core.assets.mock;

import android.content.Context;
import android.graphics.Bitmap;

import net.donky.core.assets.NotificationImageLoader;

/**
 * Created by Marcin Swierczek
 * 18/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockImageLoader extends NotificationImageLoader {

    Exception exception;

    Bitmap result;

    @Override
    public void success(Bitmap result) {

        this.result = result;

        synchronized (this) {
            notifyAll(  );
        }

    }

    public MockImageLoader(Context context) {
        super(context);
    }

    public MockImageLoader(Context context, int dps) {
        super(context, dps);
    }

    @Override
    public void failure(Exception e) {

        this.exception = e;

        synchronized (this) {
            notifyAll(  );
        }

    }

    public Exception getException() {
        return exception;
    }

    public Bitmap getResult() {
        return result;
    }
}
