package com.nickwelna.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nickwelna.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "store.db";
    public static final int DATABASE_VERSION = 1;

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + InventoryEntry.TABLE_NAME +
            " (" + InventoryEntry._ID + " " + InventoryEntry._ID_TYPE + ", " +
            InventoryEntry.COLUMN_ITEM_NAME + " " + InventoryEntry.COLUMN_ITEM_NAME_TYPE + ", " +
            InventoryEntry.COLUMN_ITEM_QUANTITY + " " + InventoryEntry.COLUMN_ITEM_QUANTITY_TYPE +
            ", " + InventoryEntry.COLUMN_ITEM_PRICE + " " + InventoryEntry.COLUMN_ITEM_PRICE_TYPE +
            ", " + InventoryEntry.COLUMN_ITEM_IMAGE + " " + InventoryEntry.COLUMN_ITEM_IMAGE_TYPE +
            ", " + InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME + " " + InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME_TYPE +
            ", " + InventoryEntry.COLUMN_ITEM_SUPPLIER_EMAIL + " " + InventoryEntry.COLUMN_ITEM_SUPPLIER_EMAIL_TYPE + ");";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS" + InventoryEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);

    }

}
