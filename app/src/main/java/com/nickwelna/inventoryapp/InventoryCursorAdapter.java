package com.nickwelna.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nickwelna.inventoryapp.data.InventoryContract.InventoryEntry;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class InventoryCursorAdapter extends CursorAdapter {


    public InventoryCursorAdapter(Context context, Cursor c) {

        super(context, c, 0);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView name = view.findViewById(R.id.name);
        final TextView quantity = view.findViewById(R.id.quantity);
        ImageView itemImage = view.findViewById(R.id.item_image);
        TextView price = view.findViewById(R.id.price);
        Button buy = view.findViewById(R.id.sale);

        String nameValue = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME));
        final int quantityValue = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY));
        String imageUri = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE));
        long priceValue = cursor.getLong(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE));
        final int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));

        name.setText(nameValue);
        quantity.setText(String.format(Locale.US, "%d", quantityValue));
        Uri image = Uri.parse(imageUri);
        itemImage.setImageURI(image);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        BigDecimal priceCorrected = new BigDecimal(priceValue).movePointLeft(2);
        String priceString = format.format(priceCorrected);
        price.setText(priceString);

        buy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                if (quantityValue > 0) {

                    decrementQuantity(context, itemUri, quantityValue, quantity);

                } else {

                    Toast.makeText(context, "No more items left to sell!", Toast.LENGTH_SHORT).show();

                }

            }

        });


    }

    private void decrementQuantity(Context context, Uri itemUri, int quantityValue, TextView quantity) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityValue - 1);
        int numRowsUpdated = context.getContentResolver().update(itemUri, contentValues, null, null);
        if (numRowsUpdated > 0) {

            quantity.setText(String.format(Locale.US, "%d", quantityValue - 1));

        } else {

            Toast.makeText(context, "Error buying item", Toast.LENGTH_SHORT).show();

        }

    }

}