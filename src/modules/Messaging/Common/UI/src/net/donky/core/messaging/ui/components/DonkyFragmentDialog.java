package net.donky.core.messaging.ui.components;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;

import net.donky.core.messaging.ui.R;

/**
 * Base Dialog Fragment for Donky Messaging. Sets style from dk_dialog_style attribute.
 *
 * Created by Marcin Swierczek
 * 29/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class DonkyFragmentDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder;

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        if (theme != null) {
            theme.resolveAttribute(R.attr.dk_dialog_style, typedValue, true);
        }
        if (typedValue != null) {
            int style = typedValue.data;
            alertDialogBuilder = new AlertDialog.Builder(getActivity(), style);
        } else {
            alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog);
        }

        prepareDialogBuilder(alertDialogBuilder);

        AlertDialog alertDialog =  alertDialogBuilder.create();

        prepareDialog(alertDialog);

        return alertDialog;
    }

    /**
     * Set dialog fields.
     *
     * @param alertDialog Alert dialog to be shown
     */
    protected abstract void prepareDialog(AlertDialog alertDialog);

    /**
     * Set dialog builder fields.
     *
     * @param alertDialogBuilder Alert dialog builder to create alert dialog
     */
    protected abstract void prepareDialogBuilder(AlertDialog.Builder alertDialogBuilder);
}
