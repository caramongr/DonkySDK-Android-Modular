package net.donky.core.messaging.rich.inbox.ui.components;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.messaging.rich.inbox.ui.R;
import net.donky.core.messaging.rich.inbox.ui.RichInboxUIController;
import net.donky.core.messaging.rich.inbox.ui.RichMessagesListener;
import net.donky.core.messaging.rich.inbox.ui.components.internal.DeleteExpiredDialogFragment;
import net.donky.core.messaging.rich.inbox.ui.components.internal.RichInboxAdapter;
import net.donky.core.messaging.rich.inbox.ui.components.internal.ViewHolder;
import net.donky.core.messaging.rich.logic.helpers.RichMessageHelper;
import net.donky.core.messaging.rich.logic.model.DatabaseSQLContract;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;
import net.donky.core.messaging.ui.components.generic.DialogDismissListener;
import net.donky.core.messaging.ui.components.generic.DonkyMessagingBaseListFragment;
import net.donky.core.messaging.ui.helpers.TimestampsHelper;
import net.donky.core.model.DonkyDataController;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Fragment containing Donky Rich Messaging Inbox.
 * <p/>
 * Created by Marcin Swierczek
 * 04/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichInboxFragment extends DonkyMessagingBaseListFragment<RichMessage, RichInboxFragment, RichInboxAdapter> {


    public static String TAG_DELETE_EXP_MSG_DIALOG = "TAG_DELETE_EXP_MSG_DIALOG";

    private RichInboxAdapter adapter;

    private RichMessagesListener listener;

    DeleteExpiredDialogFragment deleteExpiredDialogFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            deleteExpiredDialogFragment = (DeleteExpiredDialogFragment) getFragmentManager()
                    .findFragmentByTag(TAG_DELETE_EXP_MSG_DIALOG);

            final String singleSelectedToDelete = getSingleSelectedIdToDelete();

            if (singleSelectedToDelete != null) {

                if (deleteExpiredDialogFragment != null) {
                    getFragmentManager().beginTransaction().remove(deleteExpiredDialogFragment).commit();
                }
                deleteExpiredDialogFragment = new DeleteExpiredDialogFragment();

                deleteExpiredDialogFragment.setOnClickListenerAllExpired(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllExpiredMessages();
                    }
                });

                deleteExpiredDialogFragment.setOnClickListenerSingleExpired(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSelectedListRow(singleSelectedToDelete);
                    }
                });

                deleteExpiredDialogFragment.setDismissListener(new DialogDismissListener() {
                    @Override
                    public void onDismiss() {
                        setSingleSelectedIdToDelete(null);
                    }
                });

                deleteExpiredDialogFragment.show(getFragmentManager(), TAG_DELETE_EXP_MSG_DIALOG);
            }

        }

        setupRichMessageListView();

        setupSwipeToRefreshLayouts();

        // Setup rich message received callback
        if (listener == null) {
            listener = new RichMessagesListener() {
                @Override
                public void onUpdate(List<RichMessage> richMessages) {
                    refreshListView();
                }
            };
        }

        RichInboxUIController.getInstance().removeTrackedRemoteNotifications();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View viewFragmentContainer = inflater.inflate(R.layout.dk_rich_msg_inbox_fragment, container, false);

        setSwipeToRefreshLayouts((SwipeRefreshLayout) viewFragmentContainer.findViewById(R.id.swipe_container), (SwipeRefreshLayout) viewFragmentContainer.findViewById(R.id.swipe_container_empty));

        setListView((ListView) viewFragmentContainer.findViewById(R.id.dk_rich_inbox_list_view));

        return viewFragmentContainer;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register for new rich message updates
        RichInboxUIController.getInstance().registerRichMessagesListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister from new rich message updates
        RichInboxUIController.getInstance().unregisterRichMessagesListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (deleteExpiredDialogFragment != null) {
            deleteExpiredDialogFragment.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (menu.findItem(R.id.dk_edit_multiple) == null) {
            inflater.inflate(R.menu.dk_rich_inbox_option_menu, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem searchMenuItem = menu.findItem(R.id.dk_action_search);

        setupSearchMenuItem(searchMenuItem, adapter);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (R.id.dk_edit_multiple == item.getItemId()) {

            getActivity().startActionMode(new ActionMode.Callback() {

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                    mode.setTitle(R.string.dk_inbox_fragment_title);
                    mode.getMenuInflater().inflate(R.menu.dk_rich_inbox_action_menu, menu);
                    setActionMode(mode);
                    updateActionMenu(menu);

                    setOverlayVisibility(View.VISIBLE);

                    getListView().clearChoices();
                    for (int i = 0; i < getListView().getCount(); i++)
                        getListView().setItemChecked(i, false);
                    getListView().post(new Runnable() {
                        @Override
                        public void run() {
                            getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
                        }
                    });

                    final int delay = appContext.getResources().getInteger(R.integer.dk_list_row_selection_delay);

                    // we need some time for clearing any choices in list view to complete
                    getListView().postDelayed(new Runnable() {
                        public void run() {
                            adapter.setIsCheckMultipleActive(true);
                            showMultiChoiceModeAnimation(R.id.dk_rich_message_checkbox);
                        }
                    }, delay);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                    updateActionMenu(menu);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                    if (R.id.dk_delete == item.getItemId()) {

                        deleteSelectedMultipleListRows();
                        mode.finish();

                    } else if (R.id.dk_forward == item.getItemId()) {

                        //TODO
                        mode.finish();

                    } else if (R.id.dk_share == item.getItemId()) {

                        shareSelectedInMultiSelectionModeMessage();
                        mode.finish();

                    }

                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                    adapter.setIsCheckMultipleActive(false);
                    adapter.clearMultiSelectedRichMessageIds();
                    clearListViewMultipleSelectionState(R.id.dk_rich_message_checkbox);

                    setOverlayVisibility(View.GONE);

                    hideMultiChoiceModeAnimation(R.id.dk_rich_message_checkbox);

                    final int delay = appContext.getResources().getInteger(R.integer.dk_list_row_selection_delay);

                    getListView().postDelayed(new Runnable() {
                        public void run() {
                            if (isDualPane()) {
                                getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                                Pair<String, Integer> selection = getSelection();
                                if (selection != null && selection.second != null) {
                                    getListView().clearChoices();
                                    getListView().setItemChecked(selection.second, true);
                                } else {
                                    selectFirstListElement();
                                }
                            }
                        }
                    }, delay);

                    setActionMode(null);
                }


            });
        }

        return false;

    }

    /**
     * Setup Rich Message list view. Create adapter, set click listeners and apply saved selection.
     */
    private void setupRichMessageListView() {

        if (appContext != null) {

            adapter = new RichInboxAdapter(appContext, RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessagesCursorForUI(null), this);

            getListView().setAdapter(adapter);

            getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {
                    if (getActionMode() == null) {
                        getRichMessageOptionsPopup(view).show();
                    }
                    return true;
                }
            });

            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        final int position, long id) {

                    if (getActionMode() == null) {

                        listItemSelectedAction(view, position);

                    } else {

                        ViewHolder viewHolder = (ViewHolder) view.getTag();
                        viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());
                        if (viewHolder.checkBox.isChecked()) {
                            adapter.addMultiSelectedRichMessageId(viewHolder.id);
                        } else {
                            adapter.removeMultiSelectedRichMessageId(viewHolder.id);
                        }

                        onMultipleSelectionChanged();
                        view.invalidate();
                    }
                }
            });

            setSavedSelectionOrFirstListElement();
        }

    }

    /**
     * Apply saved list view selection.
     */
    private void setSavedSelectionOrFirstListElement() {

        if (isDualPane()) {

            getListView().clearChoices();
            getListView().post(new Runnable() {
                @Override
                public void run() {
                    getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                }
            });

            Pair<String, Integer> selection = getSelection();
            final String selectedId = selection.first;
            final Integer selectedListPosition = selection.second;

            getListView().post(new Runnable() {
                public void run() {

                    if (selectedListPosition != null && selectedId != null) {

                        RichMessage richMessage = RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessage(selectedId);

                        if (richMessage != null) {

                            if (RichMessageHelper.isRichMessageExpired(richMessage) && richMessage.getExpiredBody() == null) {
                                getSelectionListener().onSelected(null, isDualPane());
                            } else {
                                getSelectionListener().onSelected(richMessage, isDualPane());
                            }

                            int itemCount = adapter.getCount();
                            if (selectedListPosition < itemCount) {
                                getListView().clearChoices();
                                getListView().setItemChecked(selectedListPosition, true);
                            }

                            return;
                        }
                    }

                    selectFirstListElement();
                }
            });

        } else {

            getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        }

    }

    /**
     * Select latest rich message.
     *
     * @return Selected rich message.
     */
    private void selectFirstListElement() {

        if (isDualPane()) {

            getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

            RichMessage richMessage = adapter.getLatestRichMessage();

            if (richMessage != null) {

                if (RichMessageHelper.isRichMessageExpired(richMessage) && richMessage.getExpiredBody() == null) {
                    getSelectionListener().onSelected(null, isDualPane());
                } else {
                    getSelectionListener().onSelected(richMessage, isDualPane());
                }

                getListView().clearChoices();
                getListView().setItemChecked(0, true);
                setSelection(richMessage.getInternalId(), 0);

            } else {

                getSelectionListener().onSelected(null, isDualPane());
                setSelection(null, null);

            }


        } else {

            getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        }
    }

    /**
     * Perform actions when list row was clicked.
     *
     * @param view     List row view.
     * @param position Position of the view.
     */
    private void listItemSelectedAction(View view, final Integer position) {

        if (view != null) {

            final ViewHolder viewHolder = (ViewHolder) view.getTag();

            if (viewHolder != null) {

                viewHolder.newTag.setVisibility(View.GONE);
                viewHolder.displayName.invalidate();
                viewHolder.isRead = true;
                viewHolder.timestamp.setTextColor(appContext.getResources().getColor(R.color.dk_rich_message_timestamp_read));
                viewHolder.timestamp.setTypeface(Typeface.create(viewHolder.timestamp.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                viewHolder.timestamp.invalidate();

                TextView newFlag = (TextView) view.findViewById(R.id.dk_rich_message_new_flag);
                if (newFlag.getVisibility() == View.VISIBLE) {
                    newFlag.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(viewHolder.avatarAssetId)) {
                    adapter.loadAvatar(viewHolder.avatarAssetId, new WeakReference<>(viewHolder.avatar));
                } else {
                    viewHolder.avatar.setImageDrawable(appContext.getResources().getDrawable(R.drawable.dk_default_avatar_open_192dp));
                }

                if (!viewHolder.isExpired || viewHolder.hasExpiredBody) {

                    getSelectionListener().onSelected(RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessage(viewHolder.id), isDualPane());

                } else {

                    deleteExpiredDialogFragment = new DeleteExpiredDialogFragment();
                    setSingleSelectedIdToDelete(viewHolder.id);

                    deleteExpiredDialogFragment.setOnClickListenerSingleExpired(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteSelectedListRow(viewHolder.id);
                        }
                    });

                    deleteExpiredDialogFragment.setOnClickListenerAllExpired(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteAllExpiredMessages();
                        }
                    });

                    deleteExpiredDialogFragment.setDismissListener(new DialogDismissListener() {
                        @Override
                        public void onDismiss() {
                            setSingleSelectedIdToDelete(null);
                        }
                    });

                    deleteExpiredDialogFragment.show(getFragmentManager(), TAG_DELETE_EXP_MSG_DIALOG);

                    getSelectionListener().onSelected(null, isDualPane());

                }

                if (isDualPane()) {
                    view.setSelected(true);
                }

                setSelection(viewHolder.id, position);

            }
        }

    }

    /**
     * Update visibility of action mode options.
     *
     * @param menu Action mode menu.
     */
    private void updateActionMenu(Menu menu) {

        List<String> selected = adapter.getMultiSelectedRichMessageIdsCopy();

        if (selected.size() == 0) {

            setDeleteMenuItemVisible(menu, false);
            setForwardMenuItemVisible(menu, false);
            setShareMenuItemVisible(menu, false);

        } else if (selected.size() == 1) {

            RichMessage richMessage = RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessage(selected.get(0));

            if (richMessage != null) {

                boolean isMessageExpired = RichMessageHelper.isRichMessageExpired(richMessage);

                // Not Available before Contacts Module release
                if (true || !richMessage.isCanForward() || isMessageExpired) {
                    setForwardMenuItemVisible(menu, false);
                } else {
                    setForwardMenuItemVisible(menu, true);
                }

                if (!richMessage.isCanShare() || isMessageExpired) {
                    setShareMenuItemVisible(menu, false);
                } else {
                    setShareMenuItemVisible(menu, true);
                }

                setDeleteMenuItemVisible(menu, true);
            }

        } else if (selected.size() > 1) {

            setForwardMenuItemVisible(menu, false);
            setShareMenuItemVisible(menu, false);
            setDeleteMenuItemVisible(menu, true);

        }
    }

    /**
     * Set visibility of forward option item.
     *
     * @param menu      Menu containing forward menu item.
     * @param isVisible Visibility to set.
     */
    private void setForwardMenuItemVisible(Menu menu, boolean isVisible) {
        MenuItem forwardMenuItem = menu.findItem(R.id.dk_forward);
        if (forwardMenuItem != null) {
            forwardMenuItem.setVisible(isVisible);
            forwardMenuItem.setEnabled(isVisible);
        }
    }

    /**
     * Set visibility of share option item.
     *
     * @param menu      Menu containing share menu item.
     * @param isVisible Visibility to set.
     */
    private void setShareMenuItemVisible(Menu menu, boolean isVisible) {
        MenuItem shareMenuItem = menu.findItem(R.id.dk_share);
        if (shareMenuItem != null) {
            shareMenuItem.setVisible(isVisible);
            shareMenuItem.setEnabled(isVisible);
        }
    }

    /**
     * Set visibility of delete option item.
     *
     * @param menu      Menu containing delete menu item.
     * @param isVisible Visibility to set.
     */
    private void setDeleteMenuItemVisible(Menu menu, boolean isVisible) {
        MenuItem deleteMenuItem = menu.findItem(R.id.dk_delete);
        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(isVisible);
            deleteMenuItem.setEnabled(isVisible);
        }
    }

    /**
     * Get List Popup Window for given rich message list element view.
     *
     * @param richMsgListElementAnchorView List row view that should have the menu popup.
     * @return List Popup Window for given rich message list element view.
     */
    private ListPopupWindow getRichMessageOptionsPopup(final View richMsgListElementAnchorView) {

        final ListPopupWindow popup = new ListPopupWindow(getActivity());
        final ViewHolder holder = (ViewHolder) richMsgListElementAnchorView.getTag();

        if (holder != null) {

            setupOptionsPopup(popup, richMsgListElementAnchorView,
                    new ArrayAdapter<>(getActivity(), R.layout.dk_rich_msg_popup_list_item, getRichMessageOptionsList(holder.id)),
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                            String item = parent.getItemAtPosition(position).toString();

                            if (item.equals(appContext.getString(R.string.dk_rich_message_option_forward))) {

                                //TODO

                            } else if (item.equals(appContext.getString(R.string.dk_rich_message_option_share))) {

                                shareSelected(holder.id);

                            } else if (item.equals(appContext.getString(R.string.dk_rich_message_option_delete))) {

                                deleteSelectedListRow(holder.id);
                                //deleteSingleRowWithAnimation(holder.id, richMsgListElementAnchorView, null);

                            }

                            popup.dismiss();

                        }
                    });

        }

        return popup;
    }

    /**
     * Gets options available for given rich message. Used in Popup window.
     *
     * @param richMessageInternalId Internal id of rich message.
     * @return List of options
     */
    private List<String> getRichMessageOptionsList(String richMessageInternalId) {

        final ArrayList<String> list = new ArrayList<>();

        RichMessage richMessage = RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessage(richMessageInternalId);

        if (richMessage != null) {

            boolean isMessageExpired = RichMessageHelper.isRichMessageExpired(richMessage);

            if (richMessage.isCanForward() && !isMessageExpired) {
                //TODO list.add(appContext.getString(R.string.dk_rich_message_option_forward));
            }
            if (richMessage.isCanShare() && !isMessageExpired) {
                list.add(appContext.getString(R.string.dk_rich_message_option_share));
            }

            list.add(appContext.getString(R.string.dk_rich_message_option_delete));

        }

        return list;
    }

    @Override
    protected void updateTimestamps() {

        Integer availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();

        final Date currentTime = new Date();

        for (int i = 0; i < getListView().getChildCount(); i++) {

            Cursor cursor = (Cursor) adapter.getItem(i);

            if (cursor != null) {

                final Date dateSent;

                final Date dateExpiry;

                boolean hasEmptyExpiredBody = false;

                try {
                    dateSent = DateAndTimeHelper.parseUtcDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestamp)));
                    dateExpiry = DateAndTimeHelper.parseUtcDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp)));
                    hasEmptyExpiredBody = TextUtils.isEmpty(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiredBody)));
                } catch (CursorIndexOutOfBoundsException exception) {

                    //List View not updated yet
                    return;
                }

                final TextView timeStampView = (TextView) getListView().getChildAt(i).findViewById(R.id.dk_rich_message_timestamp);

                final View view = getListView().getChildAt(i);
                ViewHolder viewHolder = (ViewHolder) view.getTag();

                if (DateAndTimeHelper.isExpired(dateSent, dateExpiry, currentTime, availabilityDays) && !hasEmptyExpiredBody) {
                    viewHolder.timestamp.setText(appContext.getResources().getString(R.string.dk_expired_tag));
                    adapter.setAlphaForListElementContent(viewHolder, 0.5f, 0.75f);
                    viewHolder.isExpired = true;
                } else {
                    if (dateSent != null) {
                        timeStampView.setText(TimestampsHelper.getShortTimestampForRichMessage(appContext, dateSent.getTime(), currentTime.getTime()));
                    }
                }
            }
        }
    }

    /**
     * Delete multiple selected Rich Messages.
     */
    private void deleteSelectedMultipleListRows() {
        adapter.deleteMultiSelectedRichMessageIds();

        final int delay = appContext.getResources().getInteger(R.integer.dk_list_row_selection_delay);

        getListView().postDelayed(new Runnable() {
            public void run() {
                refreshListView();
                selectFirstListElement();
            }
        }, delay);
    }

    /**
     * Delete selected Rich Message immediately without animation.
     *
     * @param selectedIdToDelete List Item selected position.
     */
    private void deleteSelectedListRow(final String selectedIdToDelete) {
        RichMessageDataController.getInstance().getRichMessagesDAO().removeRichMessage(selectedIdToDelete);
        setSingleSelectedIdToDelete(null);
        refreshListView();

        final int delay = appContext.getResources().getInteger(R.integer.dk_list_row_selection_delay);

        getListView().postDelayed(new Runnable() {
            public void run() {
                String savedSelectedId = getSelection().first;
                if (savedSelectedId != null && selectedIdToDelete.equals(savedSelectedId)) {
                    selectFirstListElement();
                }
            }
        }, delay);
    }

    /**
     * Delete expired Rich Messages.
     */
    private void deleteAllExpiredMessages() {

        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... params) {

                Integer availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();

                List<String> richMessageIdsToRemove = new LinkedList<>();

                Cursor cursor = RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessagesCursorForUI(null);

                cursor.moveToPosition(-1);

                while (cursor.moveToNext()) {

                    Date dateSent = DateAndTimeHelper.parseUtcDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestamp)));
                    final Date dateExpiry = DateAndTimeHelper.parseUtcDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp)));

                    if (DateAndTimeHelper.isExpired(dateSent, dateExpiry, new Date(), availabilityDays)) {
                        richMessageIdsToRemove.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId)));
                    }
                }

                cursor.close();

                RichMessageDataController.getInstance().getRichMessagesDAO().removeRichMessagesWithInternalIds(richMessageIdsToRemove);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                refreshListView();
                setSingleSelectedIdToDelete(null);
                clearListViewSingleSelection();
                selectFirstListElement();

            }
        }.execute();

    }

    /**
     * Perform share Rich Message action and clear the states of list view and adapter.
     */
    private void shareSelectedInMultiSelectionModeMessage() {

        List<String> selected = adapter.getMultiSelectedRichMessageIdsCopy();

        if (selected.size() == 1) {
            shareSelected(selected.get(0));
        }

    }

    /**
     * Start share activity for given rich message.
     *
     * @param richMessageInternalId Internal Id of rich message.
     */
    private void shareSelected(String richMessageInternalId) {

        RichMessage richMessage = RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessage(richMessageInternalId);

        if (richMessage != null) {

            Intent shareIntent = RichMessageHelper.getShareRichMessageIntent(richMessage, true);

            // Verify it resolves
            PackageManager packageManager = getActivity().getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(shareIntent, 0);
            if (activities.size() > 0) {
                //sendRichMessageSharedNotification
                startActivity(Intent.createChooser(shareIntent, getString(R.string.dk_rich_message_share_title)));
            }
        }
    }

    @Override
    public void onContentDeleted() {

        if (isDualPane()) {

            Pair<String, Integer> selection = getSelection();

            if (selection != null && selection.first != null && selection.second != null) {

                deleteSelectedListRow(selection.first);
                selectFirstListElement();

            }
        }
    }

    @Override
    public RichInboxFragment build() {
        return new RichInboxFragment();
    }

    @Override
    public RichInboxAdapter getAdapter() {
        return adapter;
    }

    @Override
    protected void refreshListView() {

        getListView().post(new Runnable() {
            public void run() {
                adapter.changeCursor(RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessagesCursorForUI(null));
            }
        });

    }

    @Override
    protected void searchViewActionClosed() {

        getListView().post(new Runnable() {
            public void run() {
                setSavedSelectionOrFirstListElement();
            }
        });

    }

    @Override
    protected void searchViewActionOpened() {

    }

    @Override
    protected void checkListItem(String contentId) {
        // No need for right fragment to check list item in left fragment for rich messages
    }

}
