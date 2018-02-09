package com.nickwelna.inventoryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nickwelna.inventoryapp.data.InventoryContract.InventoryEntry;
import com.nickwelna.inventoryapp.data.InventoryDbHelper;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER_ID = 0;
    private static final int IMAGE_REQUEST_CODE = 1;

    private Uri itemUri;

    private EditText name;
    private EditText quantity;
    private EditText price;
    private ImageView image;
    private EditText supplierName;
    private EditText supplierEmail;
    private Uri imageUri;

    InventoryDbHelper dbHelper;

    private boolean itemHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            itemHasChanged = true;
            return false;

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        name = findViewById(R.id.edit_item_name);
        quantity = findViewById(R.id.edit_item_quantity);
        price = findViewById(R.id.edit_item_price);
        image = findViewById(R.id.image);
        supplierName = findViewById(R.id.edit_item_supplier_name);
        supplierEmail = findViewById(R.id.edit_item_supplier_email);

        name.setOnTouchListener(touchListener);
        quantity.setOnTouchListener(touchListener);
        price.setOnTouchListener(touchListener);
        supplierEmail.setOnTouchListener(touchListener);
        supplierName.setOnTouchListener(touchListener);
        image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(EditorActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditorActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {

                    selectImage();

                }

            }

        });

        dbHelper = new InventoryDbHelper(this);

        Intent intent = getIntent();
        itemUri = intent.getData();

        if (itemUri == null) {

            setTitle(R.string.editor_activity_title_new_item);

            invalidateOptionsMenu();

        } else {

            setTitle(R.string.editor_activity_title_edit_item);
            getSupportLoaderManager().initLoader(INVENTORY_LOADER_ID, null, this);

        }

        Button increaseQuantity = findViewById(R.id.increase_quantity);
        increaseQuantity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int currentQuantity = Integer.parseInt(quantity.getText().toString());
                quantity.setText(String.format(Locale.US, "%d", currentQuantity + 1));
                itemHasChanged = true;

            }

        });

        Button decreaseQuantity = findViewById(R.id.decrease_quantity);
        decreaseQuantity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int currentQuantity = Integer.parseInt(quantity.getText().toString());
                if (currentQuantity > 0) {

                    quantity.setText(String.format(Locale.US, "%d", currentQuantity - 1));
                    itemHasChanged = true;

                }

            }

        });

        Button orderFromSupplier = findViewById(R.id.order_from_supplier);
        orderFromSupplier.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{supplierEmail.getText().toString()});
                intent.putExtra(Intent.EXTRA_SUBJECT, name.getText().toString() + " Order");
                intent.putExtra(Intent.EXTRA_TEXT, "Hello " +
                        supplierName.getText().toString() +
                        ",\n\nI would like to order more of this product to sell");
                startActivity(intent);

            }

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                selectImage();

            }

        }

    }

    public void selectImage() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {

            if (data != null) {

                imageUri = data.getData();
                image.setImageURI(imageUri);

                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);

            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                String name = this.name.getText().toString();
                String quantity = this.quantity.getText().toString();
                String price = this.price.getText().toString().replace("$.,", "");
                String image = String.valueOf(imageUri);
                String supplierName = this.supplierName.getText().toString();
                String supplierEmail = this.supplierEmail.getText().toString();
                savItem(name, quantity, price, image, supplierName, supplierEmail);
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!itemHasChanged) {

                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;

                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                NavUtils.navigateUpFromSameTask(EditorActivity.this);

                            }

                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);

    }

    public void savItem(String name, String quantity, String price, String image, String supplierName, String supplierEmail) {

        if (itemUri == null && (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantity) ||
                TextUtils.isEmpty(price) || image.equals("null") ||
                TextUtils.isEmpty(supplierName) || TextUtils.isEmpty(supplierName))) {

            Toast.makeText(this, "Invalid Product Information entered", Toast.LENGTH_SHORT).show();

            return;

        }
        if (price.contains(".") && price.indexOf(".") != price.length() - 3) {

            Toast.makeText(this, "Too many or too few decimal places in price", Toast.LENGTH_SHORT).show();
            return;

        }

        if (!price.contains(".")) {

            price = price + "00";

        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, name);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, Integer.parseInt(quantity));
        String priceTrimed = price.replace("$", "");
        priceTrimed = priceTrimed.replace(",", "");
        priceTrimed = priceTrimed.replace(".", "");
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, Long.parseLong(priceTrimed));
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE, image);
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME, supplierName);
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER_EMAIL, supplierEmail);

        if (itemUri == null) {

            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {

                Toast.makeText(this, getString(R.string.editor_save_item_failed),
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, getString(R.string.editor_save_item_successful),
                        Toast.LENGTH_SHORT).show();

            }

        } else {

            int numberUpdated = getContentResolver().update(itemUri, values, null, null);

            if (numberUpdated == 0) {

                Toast.makeText(this, getString(R.string.editor_save_item_failed),
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, getString(R.string.editor_save_item_successful),
                        Toast.LENGTH_SHORT).show();

            }

        }

        finish();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {

                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME,
                InventoryEntry.COLUMN_ITEM_SUPPLIER_EMAIL

        };

        return new CursorLoader(this, itemUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.moveToFirst()) {

            String name = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME));
            int quantity = data.getInt(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY));
            long price = data.getLong(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE));
            String imageUriValue = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE));
            String supplierName = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER_NAME));
            String supplierEmail = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER_EMAIL));

            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
            BigDecimal priceCorrected = new BigDecimal(price).movePointLeft(2);
            String priceString = format.format(priceCorrected);

            this.name.setText(name);
            this.quantity.setText(String.format(Locale.US, "%d", quantity));
            this.price.setText(priceString);
            imageUri = Uri.parse(imageUriValue);
            this.image.setImageURI(imageUri);
            this.supplierName.setText(supplierName);
            this.supplierEmail.setText(supplierEmail);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        name.setText(null);
        quantity.setText(null);
        price.setText(null);

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {

                    dialog.dismiss();

                }

            }

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onBackPressed() {

        if (!itemHasChanged) {

            super.onBackPressed();
            return;

        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finish();

            }

        };

        showUnsavedChangesDialog(discardButtonClickListener);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        if (itemUri == null) {

            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);

        }
        return true;

    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                deletePet();

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

    private void deletePet() {

        if (itemUri != null) {

            int numberDeleted = getContentResolver().delete(itemUri, null, null);

            if (numberDeleted == 0) {

                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();

            }

            finish();

        }

    }

}