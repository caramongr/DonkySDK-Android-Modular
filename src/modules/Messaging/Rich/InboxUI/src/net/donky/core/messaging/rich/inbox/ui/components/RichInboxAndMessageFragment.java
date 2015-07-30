package net.donky.core.messaging.rich.inbox.ui.components;

import android.content.Intent;
import android.os.Bundle;

import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.ui.components.generic.GenericBuilder;
import net.donky.core.messaging.ui.components.generic.GenericSplitFragment;

/**
 * Implementation of split view fragment for Rich Messages Inbox.
 *
 * Created by Marcin Swierczek
 * 18/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichInboxAndMessageFragment extends GenericSplitFragment<RichInboxFragment, RichMessageFragment, RichMessage> {

    private Class<?> richMessageActivityClass;

    private static final Object sharedLock = new Object();

    public RichInboxAndMessageFragment() {
        super();
        richMessageActivityClass = RichMessageForInboxActivityWithToolbar.class;
        setFragmentBuilders(
                new GenericBuilder<RichInboxFragment>() {
                    @Override
                    public RichInboxFragment build() {
                        return new RichInboxFragment();
                    }
                }, new GenericBuilder<RichMessageFragment>() {
                    @Override
                    public RichMessageFragment build() {
                        return new RichMessageFragment();
                    }
                });
    }

    @Override
    protected Intent getDetailActivityIntent(RichMessage richMessageToDisplay) {

        Intent intent;

        synchronized (sharedLock) {
            intent = new Intent(getActivity().getApplicationContext(), getRichMessageActivityClass());
            sharedLock.notifyAll();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }

        Bundle bundle = new Bundle();

        bundle.putSerializable(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE, richMessageToDisplay);

        intent.putExtras(bundle);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    /**
     * Activity that will display Rich Message.
     *
     * @return Activity child class that will display Rich Message. By default this is RichMessageForInboxActivity which will work with NActionBarThemes.
     */
    private Class<?> getRichMessageActivityClass() {
        return richMessageActivityClass;
    }

    /**
     * Set Activity that will display Rich Message.
     *
     * @return Activity child class that will display Rich Message. By default this is RichMessageForInboxActivity which will work with NActionBarThemes.
     */
    public void setRichMessageActivityClass(Class<?> getRichMessageActivityClass) {
        synchronized (sharedLock) {
            this.richMessageActivityClass = getRichMessageActivityClass;
            sharedLock.notifyAll();
        }
    }
}
