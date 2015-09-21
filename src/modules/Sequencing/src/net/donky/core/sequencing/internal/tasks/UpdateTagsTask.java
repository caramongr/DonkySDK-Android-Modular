package net.donky.core.sequencing.internal.tasks;

import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.TagDescription;
import net.donky.core.sequencing.DonkySequenceListener;
import net.donky.core.sequencing.internal.DonkySequenceController;

import java.util.List;

/**
 * Synchronised task to update tags on the network.
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateTagsTask extends UpdateTask {

    final List<TagDescription> tags;

    public UpdateTagsTask(final DonkySequenceController donkySequenceController, List<TagDescription> tags, DonkySequenceListener listener) {
        super(donkySequenceController, listener);
        this.tags = tags;
    }

    @Override
    public void performTask() {
        taskStartedTimestamp = System.currentTimeMillis();
        DonkyNetworkController.getInstance().updateTags(tags, listener);
    }
}
