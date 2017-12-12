package com.example.averygrimes.phone_wallet_keys;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;

import java.util.Calendar;

public class Database extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Bluetooth.db";
    public static final String TABLE_NAME = "device_table";
    public static final String COL_1 = "DEVICENAME";
    public static final String COL_2 = "STATUS";
    public static final String COL_3 = "TIME";
    public static final String COL_4 = "DATE";

    public Database(Context context){

        super(context,DATABASE_NAME,null,2);
    }

    //create table
    @Override
    public void onCreate (SQLiteDatabase db){
        String createTable = "CREATE TABLE " + TABLE_NAME + "(DEVICENAME TEXT, TIME TEXT, DATE TEXT, STATUS TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String Dname, String Dtime, String Ddate, String Dstatus)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, Dname);
        contentValues.put(COL_3, Dtime);
        contentValues.put(COL_4, Ddate);
        contentValues.put(COL_2, Dstatus);
        long result = db.insert(TABLE_NAME,null ,contentValues);

        //if data as inserted incorrectly it will return -1
        if (result == -1){
            return false;
        } else{
            return true;
        }
    }

    //contents of the querry
    public Cursor getListContents(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }
}
