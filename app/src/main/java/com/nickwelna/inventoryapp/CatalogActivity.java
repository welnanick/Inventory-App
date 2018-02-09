package com.nickwelna.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nickwelna.inventoryapp.data.InventoryContract.InventoryEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER_ID = 0;

    InventoryCursorAdapter inventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);

            }

        });

        ListView listView = findViewById(R.id.list_view);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);

        listView.setAdapter(inventoryCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id));
                startActivity(intent);

            }

        });

        getSupportLoaderManager().initLoader(INVENTORY_LOADER_ID, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_insert_dummy_data:
                addItem();
                return true;

            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;

        }
        return super.onOptionsItemSelected(item);

    }

    public void addItem() {

        ContentValues item = new ContentValues();
        item.put(InventoryEntry.COLUMN_ITEM_NAME, "Google Pixel 2 XL");
        item.put(InventoryEntry.COLUMN_ITEM_QUANTITY, 10);
        item.put(InventoryEntry.COLUMN_ITEM_PRICE, 84900);
        String pixelUri = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                getResources().getResourcePackageName(R.drawable.ic_google_pixel_xl) + '/' +
                getResources().getResourceTypeName(R.drawable.ic_google_pixel_xl) + '/' +
                getResources().getResourceEntryName(R.drawable.ic_google_pixel_xl);
        item.put(InventoryEntry.COLUMN_ITEM_IMAGE, pixelUri);
        item.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, "John Doe");
        item.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_EMAIL, "jdoe@fakeemail.com");

        getContentResolver().insert(InventoryEntry.CONTENT_URI, item);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {

                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_IMAGE

        };

        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        inventoryCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        inventoryCursorAdapter.swapCursor(null);

    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                deleteItems();

            }

        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {

                    dialog.dismiss();

                }

            }

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteItems() {

        int numberDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);

        if (numberDeleted == 0) {

            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.category_delete_items_failed),
                    Toast.LENGTH_SHORT).show();

        } else {

            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.category_delete_items_successful),
                    Toast.LENGTH_SHORT).show();

        }

    }

}
