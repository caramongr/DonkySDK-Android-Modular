package net.donky.core.messaging.rich.inbox.ui.components.internal;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * View holder storing views for Rich Message Inbox list row.
 *
 * Created by Marcin Swierczek
 * 10/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ViewHolder {

    public String id;
    public TextView displayName;
    public TextView timestamp;
    public TextView description;
    public ImageView avatar;
    public CheckBox checkBox;
    public TextView newTag;
    public boolean isRead;
    public boolean isExpired;
    public boolean hasExpiredBody;
    public String avatarAssetId;

}
