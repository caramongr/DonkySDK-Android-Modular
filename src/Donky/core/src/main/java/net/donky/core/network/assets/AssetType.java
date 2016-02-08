package net.donky.core.network.assets;

/**
 * Created by Marcin Swierczek
 * 18/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public enum AssetType {
    MessageAsset(1),
    AccountAvatar(2);

    private int value;

    private AssetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
