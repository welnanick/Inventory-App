package com.nickwelna.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.nickwelna.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    private static final int INVENTORY = 0;
    private static final int ITEM_ID = 1;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);

    }

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    InventoryDbHelper dbHelper;

    @Override
    public boolean onCreate() {

        dbHelper = new InventoryDbHelper(getContext());
        return true;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {

            case INVENTORY:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {

            case INVENTORY:
                return insertItem(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);

        }

    }

    private Uri insertItem(Uri uri, ContentValues values) {

        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null) {

            throw new IllegalArgumentException("Inventory item requires a name");

        }
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (quantity != null && quantity < 0) {

            throw new IllegalArgumentException("Inventory item requires a positive quantity");

        }
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
        if (price != null && price < 0) {

            throw new IllegalArgumentException("Inventory item requires a positive price");

        }
        String supplierName = values.getAsString(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME);
        if (supplierName == null) {

            throw new IllegalArgumentException("Inventory item requires a supplier name");

        }
        String supplierEmail = values.getAsString(InventoryEntry.COLUMN_ITEM_SUPPLIER_EMAIL);
        if (supplierEmail == null) {

            throw new IllegalArgumentException("Inventory item requires a supplier email");

        }

        SQLiteDatabase inventory = dbHelper.getWritableDatabase();

        long id = inventory.insert(InventoryEntry.TABLE_NAME, null, values);

        if (id == -1) {

            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;

        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {

            case INVENTORY:
                return updateItem(uri, contentValues, selection, selectionArgs);

            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }

    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {

            return 0;

        }

        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (name != null && TextUtils.isEmpty(name)) {

            throw new IllegalArgumentException("Inventory item requires a name");

        }
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (quantity != null && quantity < 0) {

            throw new IllegalArgumentException("Inventory item requires a positive quantity");

        }
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
        if (price != null && price < 0) {

            throw new IllegalArgumentException("Inventory item requires a positive price");

        }
        String supplierName = values.getAsString(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME);
        if (name != null && TextUtils.isEmpty(name)) {

            throw new IllegalArgumentException("Inventory item requires a supplier name");

        }
        String supplierEmail = values.getAsString(InventoryEntry.COLUMN_ITEM_SUPPLIER_EMAIL);
        if (name != null && TextUtils.isEmpty(name)) {

            throw new IllegalArgumentException("Inventory item requires a supplier email");

        }

        SQLiteDatabase inventory = dbHelper.getWritableDatabase();

        int rowsUpdated = inventory.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated > 0) {

            getContext().getContentResolver().notifyChange(uri, null);

        }

        return rowsUpdated;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {

            case INVENTORY:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted > 0) {

                    getContext().getContentResolver().notifyChange(uri, null);

                }
                return rowsDeleted;

            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted > 0) {

                    getContext().getContentResolver().notifyChange(uri, null);

                }
                return rowsDeleted;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        }

    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {

            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;

            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);

        }

    }

}