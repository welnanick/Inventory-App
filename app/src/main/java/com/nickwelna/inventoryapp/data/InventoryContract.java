package com.nickwelna.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.nickwelna.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";


    private InventoryContract() {

    }

    public static abstract class InventoryEntry implements BaseColumns {

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_NAME_TYPE = "TEXT";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_QUANTITY_TYPE = "INTEGER";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_PRICE_TYPE = "INTEGER";
        public static final String COLUMN_ITEM_IMAGE = "image";
        public static final String COLUMN_ITEM_IMAGE_TYPE = "TEXT";
        public static final String COLUMN_ITEM_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_ITEM_SUPPLIER_NAME_TYPE = "TEXT";
        public static final String COLUMN_ITEM_SUPPLIER_EMAIL = "supplier_email";
        public static final String COLUMN_ITEM_SUPPLIER_EMAIL_TYPE = "TEXT";

    }

}
