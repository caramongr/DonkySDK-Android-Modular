package net.donky.core.messaging.ui.components;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

/**
 * http://stackoverflow.com/a/18875394 fix for nested fragments back stack
 *
 * Created by Marcin Swierczek
 * 19/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyFragment extends Fragment {

    private static final Field childFragmentManagerField;

    static {
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mChildFragmentManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        childFragmentManagerField = f;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (childFragmentManagerField != null) {
            try {
                childFragmentManagerField.set(this, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void hideSoftKeyboard(Context appContext) {

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }
}
