package net.donky.core.messaging.ui.components.generic;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.SearchView;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.messaging.ui.R;
import net.donky.core.messaging.ui.cache.CursorAdapterWithImageCache;
import net.donky.core.messaging.ui.components.DonkyFragment;
import net.donky.core.messaging.ui.helpers.MetricsHelper;
import net.donky.core.network.DonkyNetworkController;

import java.util.ArrayList;
import java.util.Map;

/**
 * Fragment providing setup for list fragments in Donky Messaging.
 *
 * Created by Marcin Swierczek
 * 27/06/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class DonkyMessagingBaseListFragment<T, GFT, CAT extends CursorAdapterWithImageCache> extends DonkyFragment implements MultipleSelectionListener, DualPaneLeftFragment, Selectable<T>, GenericBuilder<GFT>, DetailViewDisplayedListener, DeletionListener {

    // Keys for state bundle
    public static String KEY_SINGLE_POSITION_SELECTED = "KEY_SINGLE_POSITION_SELECTED";
    public static String KEY_SINGLE_ID_SELECTED = "KEY_SINGLE_ID_SELECTED";
    public static String KEY_SINGLE_ID_SELECTED_TO_DELETE = "KEY_SINGLE_ID_SELECTED_TO_DELETE";
    public static String KEY_MULTIPLE_IDS_SELECTED_TO_DELETE = "KEY_MULTIPLE_IDS_SELECTED_TO_DELETE";

    private static final float POPUP_OFFSET_VERTICAL_PRC = 0.75f;

    private ActionMode actionMode;

    private SearchView searchViewAction;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout swipeRefreshLayoutEmptyList;

    private SelectionListener<T> selectionListener;

    private ListView listView;

    private boolean dualPane;

    private ArrayList<String> multipleSelectedToDelete;

    private String singleSelectedIdToDelete;

    private Integer selectedListPosition;

    private String selectedId;

    private Handler handler;

    private Runnable runnable;

    protected Context appContext;

    private OverlayVisibilityController overlayVisibilityController;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(KEY_SINGLE_POSITION_SELECTED)) {
                selectedListPosition = savedInstanceState.getInt(KEY_SINGLE_POSITION_SELECTED);
            }
            if (savedInstanceState.containsKey(KEY_SINGLE_ID_SELECTED)) {
                selectedId = savedInstanceState.getString(KEY_SINGLE_ID_SELECTED);
            }
            if (savedInstanceState.containsKey(KEY_SINGLE_ID_SELECTED_TO_DELETE)) {
                singleSelectedIdToDelete = savedInstanceState.getString(KEY_SINGLE_ID_SELECTED_TO_DELETE);
            }
            if (savedInstanceState.containsKey(KEY_MULTIPLE_IDS_SELECTED_TO_DELETE)) {
                multipleSelectedToDelete = savedInstanceState.getStringArrayList(KEY_MULTIPLE_IDS_SELECTED_TO_DELETE);
            }
        }

        if (appContext == null) {
            appContext = getActivity().getApplicationContext();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        startUpdateTimestampsTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUpdateTimestampsTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getAdapter().closeDiskCache();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (selectedListPosition != null) {
            outState.putInt(KEY_SINGLE_POSITION_SELECTED, selectedListPosition);
        }
        if (selectedId != null) {
            outState.putString(KEY_SINGLE_ID_SELECTED, selectedId);
        }
        if (singleSelectedIdToDelete != null) {
            outState.putString(KEY_SINGLE_ID_SELECTED_TO_DELETE, singleSelectedIdToDelete);
        }
        if (multipleSelectedToDelete != null && !multipleSelectedToDelete.isEmpty()) {
            outState.putStringArrayList(KEY_MULTIPLE_IDS_SELECTED_TO_DELETE, multipleSelectedToDelete);
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Setup search view. Apply listeners and icons.
     *
     * @param searchMenuItem Menu Item with the search widget.
     * @param adapter        Cursor adapter providing data to filtered list view.
     */
    protected void setupSearchMenuItem(final MenuItem searchMenuItem, final CursorAdapter adapter) {

        searchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String filter) {
                adapter.getFilter().filter(filter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String filter) {
                adapter.getFilter().filter(filter);
                return true;
            }
        });

        searchViewAction.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.getFilter().filter(null);
                searchViewActionClosed();
                return false;
            }
        });

        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{R.attr.dk_ic_search_24dp};
        TypedArray array = getActivity().obtainStyledAttributes(typedValue.resourceId, attribute);
        int attributeResourceId = array.getResourceId(0, -1);
        Drawable drawable = getResources().getDrawable(attributeResourceId);
        array.recycle();

        //id from android.support.v7.appcompat.R
        ImageView collapsedSearchIcon = (ImageView) searchViewAction.findViewById(R.id.search_button);
        if (collapsedSearchIcon != null) {
            collapsedSearchIcon.setImageDrawable(drawable);
        }

        typedValue = new TypedValue();
        attribute = new int[]{R.attr.dk_ic_close_24dp};
        array = getActivity().obtainStyledAttributes(typedValue.resourceId, attribute);
        attributeResourceId = array.getResourceId(0, -1);
        drawable = getResources().getDrawable(attributeResourceId);
        array.recycle();

        ImageView closeImage = (ImageView) searchViewAction.findViewById(R.id.search_close_btn);
        if (closeImage != null) {
            closeImage.setImageDrawable(drawable);
        }

    }

    /**
     * Pass swipe to refresh layouts in onCreateView method.
     *
     * @param swipeRefreshLayout          Layout embedding list view with swipe to refresh widget.
     * @param swipeRefreshLayoutEmptyList Layout embedding empty list view TextView with swipe to refresh widget.
     */
    protected void setSwipeToRefreshLayouts(SwipeRefreshLayout swipeRefreshLayout, SwipeRefreshLayout swipeRefreshLayoutEmptyList) {
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.swipeRefreshLayoutEmptyList = swipeRefreshLayoutEmptyList;
    }

    /**
     * Set listeners for swipe to refresh widgets.
     */
    protected void setupSwipeToRefreshLayouts() {

        if (swipeRefreshLayoutEmptyList != null) {

            swipeRefreshLayoutEmptyList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    setupRefreshingCallback(swipeRefreshLayoutEmptyList);
                }
            });
            listView.setEmptyView(swipeRefreshLayoutEmptyList);
        }

        if (swipeRefreshLayout != null) {

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    setupRefreshingCallback(swipeRefreshLayout);
                }
            });
        }

    }

    /**
     * Refresh action callback for swipe to refresh widgets.
     *
     * @param swipeRefreshLayout Layout embedding list view with swipe to refresh widget.
     */
    private void setupRefreshingCallback(final SwipeRefreshLayout swipeRefreshLayout) {

        DonkyNetworkController.getInstance().synchronise(new DonkyListener() {
            @Override
            public void success() {
                refreshListView();
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /**
     * Clears dual pane list row selection.
     */
    protected void clearListViewSingleSelection() {
        listView.clearChoices();
        setSelection(null, null);
    }

    /**
     * Sets selection for dual pane mode.
     *
     * @param selectedInternalDataBaseID The row content id.
     * @param listViewPosition           The position of selected row in list view.
     */
    protected void setSelection(String selectedInternalDataBaseID, Integer listViewPosition) {
        this.selectedId = selectedInternalDataBaseID;
        this.selectedListPosition = listViewPosition;
    }

    /**
     * Gets the dual pane selection.
     *
     * @return Pair of row content id and position in list view.
     */
    protected Pair<String, Integer> getSelection() {
        return new Pair<>(selectedId, selectedListPosition);
    }

    /**
     * Gets the id of item marked as to be deleted.
     *
     * @return Id of item top delete
     */
    public String getSingleSelectedIdToDelete() {
        return singleSelectedIdToDelete;
    }

    /**
     * Sets the id of item marked as to be deleted.
     *
     * @param singleSelectedIdToDelete Id of item top delete
     */
    public void setSingleSelectedIdToDelete(String singleSelectedIdToDelete) {
        this.singleSelectedIdToDelete = singleSelectedIdToDelete;
    }

    /**
     * Mode split view fragment.
     *
     * @return True if there are two fragments visible, list and detail.
     */
    public boolean isDualPane() {
        return dualPane;
    }

    /**
     * Gets the listener for list row selections. Usually detail fragment.
     *
     * @return Listener for list row selections.
     */
    public SelectionListener<T> getSelectionListener() {
        return selectionListener;
    }

    /**
     * Gets the list view.
     *
     * @return List View
     */
    public ListView getListView() {
        return listView;
    }

    /**
     * Stets the list view. Usually the parent class.
     *
     * @param listView ListView to be set.
     */
    public void setListView(ListView listView) {
        this.listView = listView;
    }

    /**
     * Get the ActionMode object. Not null only when action mode is active.
     *
     * @return Toolbar ActionMode object or null if action mode is not active.
     */
    public ActionMode getActionMode() {
        return actionMode;
    }

    /**
     * Sets the Action mode. Usually by parent class.
     *
     * @param actionMode Toolbar ActionMode object or null if action mode is not active.
     */
    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }

    /**
     * Start the timer periodically calling {@link DonkyMessagingBaseListFragment#updateTimestamps()}. Should be called in Fragment onResume.
     */
    protected void startUpdateTimestampsTimer() {

        final int frequency = appContext.getResources().getInteger(R.integer.dk_list_view_timestamps_update_frequency);

        if (handler == null && runnable == null) {

            handler = new Handler();

            runnable = new Runnable() {

                public void run() {
                    updateTimestamps();
                    handler.postDelayed(this, frequency);
                }
            };

            handler.postDelayed(runnable, frequency);
        }
    }

    /**
     * Stops the timer periodically calling {@link DonkyMessagingBaseListFragment#updateTimestamps()}. Should be called in Fragment onPause.
     */
    protected void stopUpdateTimestampsTimer() {

        if (handler != null) {
            handler.removeCallbacks(runnable);
        }

        runnable = null;
        handler = null;
    }

    /**
     * Setup the window list option menu. Offset, position etc.
     *
     * @param popup                        List popup window instance
     * @param richMsgListElementAnchorView The selected list row view
     * @param arrayAdapter                 Adapter with array of options
     * @param onItemClickListener          List item click listener.
     */
    protected void setupOptionsPopup(ListPopupWindow popup, final View richMsgListElementAnchorView, ArrayAdapter arrayAdapter, AdapterView.OnItemClickListener onItemClickListener) {

        popup.setAnchorView(richMsgListElementAnchorView);
        popup.setAdapter(arrayAdapter);
        popup.setVerticalOffset(-(int) (richMsgListElementAnchorView.getHeight() * POPUP_OFFSET_VERTICAL_PRC));
        popup.setHorizontalOffset((int) getResources().getDimension(R.dimen.dk_list_popup_left_offset));
        boolean isOrientationLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        popup.setContentWidth(isOrientationLandscape ? (int) getResources().getDimension(R.dimen.dk_list_popup_width_landscape) : (int) getResources().getDimension(R.dimen.dk_list_popup_width_portrait));
        popup.setOnItemClickListener(onItemClickListener);

    }

    /**
     * Animate transition to list view multi-choice mode.
     *
     * @param checkboxResourceId Resource id of list element checkbox.
     */
    protected void showMultiChoiceModeAnimation(int checkboxResourceId) {

        final int animationDuration = appContext.getResources().getInteger(R.integer.dk_multi_choice_animation_time_millis);

        if (listView.getAdapter().getCount() > 0) {

            for (int i = 0; i < listView.getChildCount(); i++) {

                final int finalI = i;

                final int maxSpace = MetricsHelper.dpToPx(appContext, appContext.getResources().getInteger(R.integer.dk_checkbox_image_dp_width)) + (int) appContext.getResources().getDimension(R.dimen.dk_padding_medium);

                final View view = listView.getChildAt(finalI);

                final CheckBox checkBox;

                if (view != null) {
                    checkBox = (CheckBox) view.findViewById(checkboxResourceId);
                } else {
                    checkBox = null;
                }

                Animation anim = new Animation() {

                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {

                        if (checkBox != null) {
                            if (interpolatedTime == 0) {
                                checkBox.getLayoutParams().height = 1;
                                checkBox.getLayoutParams().width = 1;
                                checkBox.setVisibility(View.VISIBLE);
                            } else {
                                checkBox.getLayoutParams().height = (int) (maxSpace * interpolatedTime);
                                checkBox.getLayoutParams().width = (int) (maxSpace * interpolatedTime);
                                view.requestLayout();
                            }
                        }
                    }

                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }
                };
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.setDuration(animationDuration);
                checkBox.startAnimation(anim);
                listView.invalidate();

            }
        }
    }

    /**
     * Animate transition to list view standard mode.
     *
     * @param checkboxResourceId Resource id of list element checkbox.
     */
    protected void hideMultiChoiceModeAnimation(int checkboxResourceId) {

        final int animationDuration = appContext.getResources().getInteger(R.integer.dk_multi_choice_animation_time_millis);

        if (listView.getAdapter().getCount() > 0) {

            for (int i = 0; i < listView.getChildCount(); i++) {

                final int finalI = i;

                final View view = listView.getChildAt(finalI);

                final CheckBox checkBox;

                if (view != null) {
                    checkBox = (CheckBox) view.findViewById(checkboxResourceId);
                } else {
                    checkBox = null;
                }

                Animation anim = new Animation() {

                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {

                        if (checkBox != null) {

                            int checkBoxInitialSize = checkBox.getLayoutParams().width;

                            if (interpolatedTime == 1) {
                                checkBox.setVisibility(View.GONE);
                            } else {
                                checkBox.getLayoutParams().height = checkBoxInitialSize - (int) (checkBoxInitialSize * interpolatedTime);
                                checkBox.getLayoutParams().width = checkBoxInitialSize - (int) (checkBoxInitialSize * interpolatedTime);
                                view.requestLayout();
                            }
                        }
                    }

                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }
                };
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.setDuration(animationDuration);
                checkBox.startAnimation(anim);
                listView.invalidate();
            }
        }
    }

    /**
     * Clear the state of list view when exiting Action Mode.
     */
    protected void clearListViewMultipleSelectionState(final int checkboxResourceId) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < getListView().getChildCount(); i++) {
                    final CheckBox checkBox = (CheckBox) getListView().getChildAt(i).findViewById(checkboxResourceId);
                    checkBox.setChecked(false);
                }
            }
        });

    }

    /**
     * Change detail view overlay visibility.
     *
     * @param visibility View.VISIBLE, View.GONE or View.INVISIBLE
     */
    protected void setOverlayVisibility(int visibility) {
        if (overlayVisibilityController != null) {
            overlayVisibilityController.setOverlayVisibility(visibility);
        }
    }

    /**
     * refresh list view content.
     */
    protected abstract void refreshListView();

    /**
     * Update visible timestamps in list elements.
     */
    protected abstract void updateTimestamps();

    /**
     * Get {@link CursorAdapterWithImageCache} set for parent class.
     *
     * @return
     */
    protected abstract CAT getAdapter();

    /**
     * Search view has been closed callback.
     */
    protected abstract void searchViewActionClosed();

    /**
     * Search view has been opened callback.
     */
    protected abstract void searchViewActionOpened();

    protected abstract void checkListItem(final String contentId);

    @Override
    public void onMultipleSelectionChanged() {
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    public void setIsInDualPaneDisplayMode(boolean dualPane) {
        this.dualPane = dualPane;
    }

    @Override
    public void setSelectionListener(SelectionListener<T> selectionListener) {
        this.selectionListener = selectionListener;
    }

    @Override
    public void onMessageDisplayed(String contentId) {
        refreshListView();
        checkListItem(contentId);
    }

    @Override
    public void setOverlayVisibilityController(OverlayVisibilityController overlayVisibilityController) {
        this.overlayVisibilityController = overlayVisibilityController;
    }
}
