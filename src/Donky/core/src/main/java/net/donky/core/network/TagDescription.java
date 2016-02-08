package net.donky.core.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class TagDescription {

    @SerializedName("value")
    private String value;

    @SerializedName("isSelected")
    private boolean isSelected;

    public String getValue() {
        return value;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
