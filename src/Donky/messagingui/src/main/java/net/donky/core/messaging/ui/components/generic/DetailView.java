package net.donky.core.messaging.ui.components.generic;

/**
 * Created by Marcin Swierczek
 * 02/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DetailView {

    void setDeletionListener(DeletionListener deletionListener);

    void setDetailViewPresentedListener(DetailViewPresentedListener detailViewPresentedListener);

}
