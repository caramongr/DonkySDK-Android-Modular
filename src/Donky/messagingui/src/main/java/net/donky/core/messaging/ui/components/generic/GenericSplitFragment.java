package net.donky.core.messaging.ui.components.generic;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.donky.core.messaging.ui.R;
import net.donky.core.messaging.ui.components.DonkyFragment;

/**
 * Fragment that containing two fragments (list and detail) and display one or two depending on display size and orientation.
 *
 * Created by Marcin Swierczek
 * 21/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class GenericSplitFragment<FRAGMENT_LEFT extends Fragment & GenericBuilder<FRAGMENT_LEFT> & Selectable<T> & DualPaneLeftFragment & DeletionListener & DetailViewDisplayedListener, FRAGMENT_RIGHT extends Fragment & GenericBuilder<FRAGMENT_RIGHT> & SelectionListener<T> & DetailView & DualPaneModeListener, T> extends DonkyFragment implements SelectionListener<T>, OverlayVisibilityController {

    public enum DISPLAY_MODE {
        ALWAYS_SINGLE,
        MIXED,
        ALWAYS_SPLIT
    }

    // Fragment tags
    private static String TAG_FRAGMENT_LEFT = "TAG_FRAGMENT_LEFT";
    private static String TAG_FRAGMENT_RIGHT = "TAG_FRAGMENT_RIGHT";

    public static String KEY_DISPLAY_MODE_MIXED = "KEY_DISPLAY_MODE_MIXED";

    // fragment builders
    private GenericBuilder<FRAGMENT_LEFT> fragmentBuilderLeft;
    private GenericBuilder<FRAGMENT_RIGHT> fragmentBuilderRight;

    private SelectionListener<T> selectionListener;

    private boolean dualPane;

    DISPLAY_MODE displayMode;

    private DeletionListener deletionListener;

    private DetailViewDisplayedListener detailViewDisplayedListener;

    private View overlay;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View detailsFrame = getActivity().findViewById(R.id.dk_right_container);
        dualPane = detailsFrame != null
                && detailsFrame.getVisibility() == View.VISIBLE;

        setLeftFragment();
        if (dualPane) {
            setRightFragment();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View viewFragmentContainer;

        int smallestScreenWidthDp = getResources().getConfiguration().smallestScreenWidthDp;

        boolean isOrientationLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (smallestScreenWidthDp >= 600 && smallestScreenWidthDp < 720) {

            displayMode = DISPLAY_MODE.MIXED;

            if (isOrientationLandscape) {
                viewFragmentContainer = inflater.inflate(R.layout.dk_layout_split_fragment, container, false);
                overlay = viewFragmentContainer.findViewById(R.id.dk_overlay);
            } else {
                viewFragmentContainer = inflater.inflate(R.layout.dk_layout_no_split_fragment, container, false);
            }

        } else if (smallestScreenWidthDp >= 720) {

            //displayMode = DISPLAY_MODE.ALWAYS_SPLIT;
            //viewFragmentContainer = inflater.inflate(R.layout.dk_layout_split_fragment, container, false);

            displayMode = DISPLAY_MODE.MIXED;

            if (isOrientationLandscape) {
                viewFragmentContainer = inflater.inflate(R.layout.dk_layout_split_fragment, container, false);
                overlay = viewFragmentContainer.findViewById(R.id.dk_overlay);
            } else {
                viewFragmentContainer = inflater.inflate(R.layout.dk_layout_no_split_fragment, container, false);
            }

        } else {

            displayMode = DISPLAY_MODE.ALWAYS_SINGLE;
            viewFragmentContainer = inflater.inflate(R.layout.dk_layout_no_split_fragment, container, false);
        }

        return viewFragmentContainer;
    }

    @Override
    public void onSelected(T item, boolean isSplitViewMode) {
        if (dualPane) {
            this.selectionListener.onSelected(item, dualPane);
        } else {
            Intent intent = getDetailActivityIntent(item);
            intent.putExtra(KEY_DISPLAY_MODE_MIXED, displayMode == DISPLAY_MODE.MIXED);
            startActivity(intent);
        }
    }

    @Override
    public void onSelectedNew(T item) {
        if (dualPane) {
            this.selectionListener.onSelectedNew(item);
        } else {
            Intent intent = getDetailActivityIntent(item);
            intent.putExtra(KEY_DISPLAY_MODE_MIXED, displayMode == DISPLAY_MODE.MIXED);
            startActivity(intent);
        }
    }

    /**
     * Sets the fragment builders for left(list) and right(detail) fragments. Parent class responsibility.
     *
     * @param fragmentBuilderList Left fragment {@link GenericBuilder}.
     * @param fragmentBuilderDetail Right fragment {@link GenericBuilder}.
     */
    public void setFragmentBuilders(GenericBuilder<FRAGMENT_LEFT> fragmentBuilderList, GenericBuilder<FRAGMENT_RIGHT> fragmentBuilderDetail) {
        this.fragmentBuilderRight = fragmentBuilderDetail;
        this.fragmentBuilderLeft = fragmentBuilderList;
    }

    /**
     * Commits the right fragment transaction.
     */
    private void setRightFragment() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.dk_right_container, prepareRightFragment(), TAG_FRAGMENT_RIGHT).
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                    commit();
    }

    /**
     * Commits the left fragment transaction.
     */
    private void setLeftFragment() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.dk_left_container, prepareLeftFragment(), TAG_FRAGMENT_LEFT).
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                    commit();
    }

    /**
     * Gets the right fragment.
     *
     * @return Right(detail) fragment.
     */
    private FRAGMENT_RIGHT prepareRightFragment() {

        FRAGMENT_RIGHT fragmentRight = (FRAGMENT_RIGHT) getChildFragmentManager().findFragmentByTag(TAG_FRAGMENT_RIGHT);

        if (fragmentRight == null) {

            fragmentRight = fragmentBuilderRight.build();

        }

        this.selectionListener = fragmentRight;
        fragmentRight.setIsInDualPaneDisplayMode(dualPane);
        fragmentRight.setDeletionListener(new DeletionListener() {
            @Override
            public void onContentDeleted() {
                if (deletionListener != null) {
                    deletionListener.onContentDeleted();
                }
            }
        });
        fragmentRight.setDetailViewPresentedListener(new DetailViewPresentedListener() {
            @Override
            public void onDetailViewPresented(String contentId) {
                if (detailViewDisplayedListener != null) {
                    detailViewDisplayedListener.onMessageDisplayed(contentId);
                }
            }
        });

        return fragmentRight;
    }

    /**
     * Gets the left fragment.
     *
     * @return Left(list) fragment.
     */
    private FRAGMENT_LEFT prepareLeftFragment() {

        FRAGMENT_LEFT fragmentLeft = (FRAGMENT_LEFT) getChildFragmentManager().findFragmentByTag(TAG_FRAGMENT_LEFT);

        if (fragmentLeft == null) {

            fragmentLeft = fragmentBuilderLeft.build();

        }

        this.deletionListener = fragmentLeft;
        this.detailViewDisplayedListener = fragmentLeft;
        fragmentLeft.setIsInDualPaneDisplayMode(dualPane);
        fragmentLeft.setSelectionListener(this);
        fragmentLeft.setOverlayVisibilityController(this);
        return fragmentLeft;
    }

    /**
     * Gets the intent used when left(list) list view row is clicked. Should open the detail view activity.
     *
     * @param selected Selected object.
     * @return Intent to open detail view for selected object.
     */
    protected abstract Intent getDetailActivityIntent(T selected);

    @Override
    public void setOverlayVisibility(int visibility) {
        if (overlay != null) {
            overlay.setVisibility(visibility);
        }
    }
}
