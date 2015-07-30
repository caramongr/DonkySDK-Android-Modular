package net.donky.core.messaging.rich.inbox.ui.components.internal;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.messaging.rich.inbox.ui.R;
import net.donky.core.messaging.rich.logic.model.DatabaseSQLContract;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;
import net.donky.core.messaging.ui.cache.CursorAdapterWithImageCache;
import net.donky.core.messaging.ui.components.generic.MultipleSelectionListener;
import net.donky.core.messaging.ui.helpers.MetricsHelper;
import net.donky.core.messaging.ui.helpers.TimestampsHelper;
import net.donky.core.model.DonkyDataController;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Cursor Adapter for Rich Messages Inbox.
 *
 * Created by Marcin Swierczek
 * 07/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichInboxAdapter extends CursorAdapterWithImageCache {

    private static final String DISK_CACHE_UNIQUE_NAME = "RichInboxThumbnails";

    private boolean isCheckMultipleActive;

    private CopyOnWriteArraySet<String> selectedRichMessageIds;

    private MultipleSelectionListener listener;

    private Integer availabilityDays;

    private final int maxSpace;

    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param cursor The cursor from which to get the data.
     * @param listener Callback to be invoked when multiple list view elements selection changed. Used e.g. to update action mode in fragment.
     */
    public RichInboxAdapter(Context context, Cursor cursor, MultipleSelectionListener listener) {
        super(context, cursor, 0, DISK_CACHE_UNIQUE_NAME, (int) context.getResources().getDimension(R.dimen.dk_download_avatar_rescale_to_width));
        this.isCheckMultipleActive = false;
        this.selectedRichMessageIds = new CopyOnWriteArraySet<>();
        this.listener = listener;
        this.availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();
        this.maxSpace = MetricsHelper.dpToPx(context, context.getResources().getInteger(R.integer.dk_checkbox_image_dp_width)) + (int) context.getResources().getDimension(R.dimen.dk_padding_medium);

        setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {

                if (constraint != null) {
                    return RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessagesCursorForUI(constraint.toString());
                }

                return RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessagesCursorForUI(null);
            }
        });

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.dk_listview_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.displayName = (TextView) view.findViewById(R.id.dk_rich_message_display_name);
        viewHolder.timestamp = (TextView) view.findViewById(R.id.dk_rich_message_timestamp);
        viewHolder.description = (TextView) view.findViewById(R.id.dk_rich_message_description);
        viewHolder.avatar = (ImageView) view.findViewById(R.id.dk_rich_message_image);
        viewHolder.checkBox = (CheckBox) view.findViewById(R.id.dk_rich_message_checkbox);
        viewHolder.newTag = (TextView) view.findViewById(R.id.dk_rich_message_new_flag);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.displayName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName)));
        Date dateSent = DateAndTimeHelper.parseUtcDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestamp)));

        holder.description.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description)));
        holder.id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId));
        holder.isRead = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead)) != 0;
        holder.avatarAssetId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_avatarAssetId));
        holder.hasExpiredBody = !TextUtils.isEmpty(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiredBody)));

        if (!holder.isRead) {
            holder.timestamp.setTypeface(Typeface.create(holder.timestamp.getTypeface(), Typeface.BOLD), Typeface.BOLD);
            holder.timestamp.setTextColor(context.getResources().getColor(R.color.dk_rich_message_timestamp));
            holder.timestamp.invalidate();
            holder.newTag.setVisibility(View.VISIBLE);
            holder.avatar.setImageDrawable(context.getResources().getDrawable(R.drawable.dk_default_avatar_closed_192dp));
        } else {
            holder.displayName.invalidate();
            holder.timestamp.setTypeface(Typeface.create(holder.timestamp.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
            holder.timestamp.setTextColor(context.getResources().getColor(R.color.dk_rich_message_timestamp_read));
            holder.timestamp.invalidate();
            holder.newTag.setVisibility(View.GONE);
            holder.avatar.setImageDrawable(context.getResources().getDrawable(R.drawable.dk_default_avatar_open_192dp));
        }

        if (!TextUtils.isEmpty(holder.avatarAssetId)) {
            loadAvatar(holder.avatarAssetId, new WeakReference<>(holder.avatar));
        }

        if (isCheckMultipleActive) {
            holder.checkBox.setChecked(selectedRichMessageIds.contains(holder.id));

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (holder.checkBox.isChecked()) {
                        selectedRichMessageIds.add(holder.id);
                    } else {
                        selectedRichMessageIds.remove(holder.id);
                    }

                    listener.onMultipleSelectionChanged();
                }
            });
            holder.checkBox.getLayoutParams().height = maxSpace;
            holder.checkBox.getLayoutParams().width = maxSpace;
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        final Date dateExpiry = DateAndTimeHelper.parseUtcDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp)));

        if (DateAndTimeHelper.isExpired(dateSent, dateExpiry, System.currentTimeMillis(), availabilityDays) && !holder.hasExpiredBody) {
            holder.timestamp.setText(context.getResources().getString(R.string.dk_expired_tag));
            setAlphaForListElementContent(holder, 0.5f, 0.75f);
            holder.isExpired = true;
        } else {
            if (dateSent != null) {
                holder.timestamp.setText(TimestampsHelper.getShortTimestamp(context, dateSent.getTime(), System.currentTimeMillis()));
            }
            setAlphaForListElementContent(holder, 1.0f, 1.0f);
            holder.isExpired = false;
        }

    }

    /**
     * Adjust alpha of row view holder components.
     * @param holder View Holder for rich message list view row.
     * @param alpha Alpha chanel to set for the views.
     */
    public void setAlphaForListElementContent(ViewHolder holder, float alpha, float timestampAlfpha) {
        holder.avatar.setAlpha(alpha);
        holder.newTag.setAlpha(alpha);
        holder.displayName.setAlpha(alpha);
        holder.timestamp.setAlpha(timestampAlfpha);
        holder.description.setAlpha(alpha);
    }

    /**
     * Set true if List View is in multiple selection mode.
     * @param isCheckMultipleActive True if List View is in multiple selection mode.
     */
    public void setIsCheckMultipleActive(boolean isCheckMultipleActive) {
        this.isCheckMultipleActive = isCheckMultipleActive;
    }

    /**
     * Returns the latest rich message.
     * @return Latest rich message.
     */
    public RichMessage getLatestRichMessage() {
        Cursor cursor = getCursor();
        if (cursor != null && getCount() > 0) {
            cursor.moveToPosition(0);
            return RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId)));
        }
        return null;
    }

    /**
     * Get Internal ids of selected Rich Messages in multi-select mode.
     * @return
     */
    public List<String> getMultiSelectedRichMessageIdsCopy() {
        return new ArrayList<>(selectedRichMessageIds);
    }

    /**
     * Clear selection for multi-selection mode.
     */
    public void clearMultiSelectedRichMessageIds() {
        selectedRichMessageIds.clear();
    }

    /**
     * Mark Rich Message as selected in multi-selection mode.
     * @param id Internal id of selected rich message.
     */
    public void addMultiSelectedRichMessageId(String id) {
        selectedRichMessageIds.add(id);
    }

    /**
     * Mark Rich Message as not selected in multi-selection mode.
     * @param id Internal id of unselected rich message.
     */
    public void removeMultiSelectedRichMessageId(String id) {
        selectedRichMessageIds.remove(id);
    }

    /**
     * Delete all selected Rich Messages in multi-selection mode.
     */
    public void deleteMultiSelectedRichMessageIds(){

        for (String id : selectedRichMessageIds) {
            RichMessageDataController.getInstance().getRichMessagesDAO().removeRichMessage(id);
            removeMultiSelectedRichMessageId(id);
        }

    }

}
