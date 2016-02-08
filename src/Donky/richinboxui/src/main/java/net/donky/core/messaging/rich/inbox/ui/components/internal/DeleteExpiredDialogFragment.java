package net.donky.core.messaging.rich.inbox.ui.components.internal;


import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import net.donky.core.messaging.rich.inbox.ui.R;
import net.donky.core.messaging.ui.components.DonkyFragmentDialog;
import net.donky.core.messaging.ui.components.generic.DialogDismissListener;

/**
 * Dialog fragment shown when user click the expired Rich Message.
 *
 * Created by Marcin Swierczek
 * 15/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DeleteExpiredDialogFragment extends DonkyFragmentDialog {

    private DialogInterface.OnClickListener onClickListenerSingleExpired;

    private DialogInterface.OnClickListener onClickListenerAllExpiredButton;

    private DialogDismissListener dismissListener;

    /**
     * Set callback to be invoked when delete all expired button has been clicked.
     * @param onClickListenerAllExpiredButton
     */
    public void setOnClickListenerAllExpired(DialogInterface.OnClickListener onClickListenerAllExpiredButton) {
        this.onClickListenerAllExpiredButton = onClickListenerAllExpiredButton;
    }

    /**
     * Set callback to be invoked when delete button has been clicked.
     * @param onClickListenerSingleExpired
     */
    public void setOnClickListenerSingleExpired(DialogInterface.OnClickListener onClickListenerSingleExpired) {
        this.onClickListenerSingleExpired = onClickListenerSingleExpired;
    }

    /**
     * Callback to be invoked when onBackPressed is pressed.
     * @param dismissListener Listener
     */
    public void setDismissListener(DialogDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    @Override
    protected void prepareDialogBuilder(AlertDialog.Builder alertDialogBuilder) {
        alertDialogBuilder.setTitle(getResources().getString(R.string.dk_inbox_expired_dialog_title));
        alertDialogBuilder.setMessage(getResources().getString(R.string.dk_inbox_expired_dialog_message));
        alertDialogBuilder.setNeutralButton(getResources().getString(R.string.dk_inbox_expired_dialog_delete_all_expired_button), onClickListenerAllExpiredButton);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.dk_inbox_expired_dialog_delete_button), onClickListenerSingleExpired);
    }

    @Override
    protected void prepareDialog(AlertDialog alertDialog) {
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog,
                                 int keyCode, android.view.KeyEvent event) {
                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    dismissListener.onDismiss();
                    dismiss();
                    return true;
                } else return false;
            }
        });
    }
}
