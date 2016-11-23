package com.kyvlabs.brrr2.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kyvlabs.brrr2.data.DBBeacon;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gotze on 2016/4/4.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Brrr2.db";
    public static final String FAVORITES_TABLE_NAME = "favorites";
    public static final String FAVORITES_COLUMN_ID = "id";
    public static final String FAVORITES_COLUMN_UUID = "uuid";
    public static final String FAVORITES_COLUMN_MAJOR = "major";
    public static final String FAVORITES_COLUMN_MINOR = "minor";
    public static final String FAVORITES_COLUMN_PICTUREURL = "pictureUrl";
    public static final String FAVORITES_COLUMN_TITLE = "title";
    public static final String FAVORITES_COLUMN_MAINTEXT = "mainText";
    public static final String FAVORITES_COLUMN_TIME = "time";

    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS favorites " +
                        "(id integer primary key, uuid text, major text, minor text, pictureUrl text, title text, mainText text, time text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS favorites");
        onCreate(db);
    }

    public boolean insert(DBBeacon beacon)
    {
        return insert(beacon.getIds().getUuid(), beacon.getIds().getMajor(), beacon.getIds().getMinor());
    }

    public boolean insert (String uuid, String major, String minor)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", uuid);
        contentValues.put("major", major);
        contentValues.put("minor", minor);
        db.insert("favorites", null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from favorites where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FAVORITES_TABLE_NAME);
        return numRows;
    }

    public boolean update (Integer id, String uuid, String major, String minor)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", uuid);
        contentValues.put("major", major);
        contentValues.put("minor", minor);
        db.update("favorites", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer delete (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("favorites",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public Integer delete (DBBeacon beacon)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("favorites",
                "uuid = ?  and major = ? and minor = ?",
                new String[] { beacon.getIds().getUuid(), beacon.getIds().getMajor(), beacon.getIds().getMinor()});
    }

    public ArrayList<String> getAll()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from favorites", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(FAVORITES_COLUMN_UUID)));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean get(DBBeacon beacon)
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from favorites where uuid = ?  and major = ? and minor = ?",  new String[] { beacon.getIds().getUuid(), beacon.getIds().getMajor(), beacon.getIds().getMinor() } );
        return res.getCount() >0 ? true: false;

    }
}
