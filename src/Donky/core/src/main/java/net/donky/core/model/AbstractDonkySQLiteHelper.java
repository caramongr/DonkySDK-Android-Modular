package net.donky.core.model;

import android.database.sqlite.SQLiteDatabase;

/**
 * Interface for all SQLite helpers that are registered in DonkyCore as Service and need to be included in {@link DatabaseSQLHelper}
 *
 * Created by Marcin Swierczek
 * 05/07/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class AbstractDonkySQLiteHelper {

    public static final String SERVICE_CATEGORY_SQLITE_HELPER = "AbstractDonkySQLiteHelper";

    protected static final String TEXT_TYPE = " TEXT";
    protected static final String INT_TYPE = " INT";
    protected static final String LONG_TYPE = " LONG";
    protected static final String REAL_TYPE = " REAL";
    protected static final String COMMA_SEP = ",";

    public abstract void onCreate(SQLiteDatabase db);

    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public abstract void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
