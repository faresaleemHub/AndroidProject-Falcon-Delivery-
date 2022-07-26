package com.aqsa.graduation_project_app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    private static final String DB_Name = "ReceiptPurchases";
    SQLiteDatabase db;
    Context c;

    public MyHelper(@Nullable Context context) {
        super(context, DB_Name, null, DB_VERSION);
        c = context;
        if(getWritableDatabase()!=null)
            db=getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(PurchaseItem.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*
    //insert methods
    //1-Notes
    public boolean insertPurchaseItem(PurchaseItem purchaseItem) {
        ContentValues cv1 = new ContentValues();
        cv1.put(PurchaseItem.COL_PRODUCT_ID, purchaseItem.getId());
        cv1.put(PurchaseItem.COL_BuyPrice, purchaseItem.getBuyPrice());
        cv1.put(PurchaseItem.COL_QUANTITY, purchaseItem.getQuantity());
        cv1.put(PurchaseItem.COL_START_DATE_VALIDATION, purchaseItem.getStartDateValidation());
        cv1.put(PurchaseItem.COL_END_DATE_VALIDATION, purchaseItem.getEndDateValidation());
        long d = db.insert(PurchaseItem.Tabel_Name, null, cv1);
        return d != -1;
    }
    //update
    public boolean updatetPurchaseItem(PurchaseItem purchaseItem,String id) {
        ContentValues cv1 = new ContentValues();
        cv1.put(PurchaseItem.COL_PRODUCT_ID, purchaseItem.getId());
        cv1.put(PurchaseItem.COL_BuyPrice, purchaseItem.getBuyPrice());
        cv1.put(PurchaseItem.COL_QUANTITY, purchaseItem.getQuantity());
        cv1.put(PurchaseItem.COL_START_DATE_VALIDATION, purchaseItem.getStartDateValidation());
        cv1.put(PurchaseItem.COL_END_DATE_VALIDATION, purchaseItem.getEndDateValidation());

        long d = db.update(PurchaseItem.Tabel_Name, cv1, PurchaseItem.COL_ID+"="+id,null);
        return d != -1;
    }

    //1- delete note
    public boolean deletePurchaseItem(String id){
        db=getWritableDatabase();
        db.execSQL("delete from "+PurchaseItem.Tabel_Name+" where id = "+id);
        return true;
    }

    //select all notes
    public ArrayList<PurchaseItem> getAllPurchaseItemsForA_Receipt() {//correct
        ArrayList<PurchaseItem> data = new ArrayList<>();

        String sql = "select * from " + PurchaseItem.Tabel_Name;

        db = getReadableDatabase();

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(PurchaseItem.COL_ID));
                String ProductID = cursor.getString(cursor.getColumnIndex(PurchaseItem.COL_PRODUCT_ID));
                String BuyPrice = cursor.getString(cursor.getColumnIndex(PurchaseItem.COL_BuyPrice));
                String Quantity = cursor.getString(cursor.getColumnIndex(PurchaseItem.COL_QUANTITY));
                String StartDateValidation = cursor.getString(cursor.getColumnIndex(PurchaseItem.COL_START_DATE_VALIDATION));
                String EndDateValidation = cursor.getString(cursor.getColumnIndex(PurchaseItem.COL_END_DATE_VALIDATION));

                data.add(new PurchaseItem(id,ProductID,BuyPrice,Quantity,StartDateValidation,EndDateValidation));
            }
            while (cursor.moveToNext());
            cursor.close();
            return data;
        }
        return null;
    }

     */
}
