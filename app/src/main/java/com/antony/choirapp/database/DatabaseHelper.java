package com.antony.choirapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.antony.choirapp.models.Mp3Item;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "Choir.db";
    private static final String TABLE_NAME = "mp3_table";
    private static final String COL_2 = "SONG_NAME";
    private static final String COL_3 = "ADDED_USER";
    private static final String COL_4 = "IS_DOWNLOADED";
    private static final String COL_5 = "PATH";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,SONG_NAME TEXT,ADDED_USER TEXT,IS_DOWNLOADED INTEGER,PATH TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertData(String name, String addeduser, int is_downloaded, String path) {

        List<Mp3Item> mp3Items;
        mp3Items = getAllData();
        boolean isAvailable = false;
        for (Mp3Item mp3Item : mp3Items) {
            if (name.equals(mp3Item.getmSongName())) {
                isAvailable = true;
            }
        }
        if (!isAvailable) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_2, name);
            contentValues.put(COL_3, addeduser);
            contentValues.put(COL_4, is_downloaded);
            contentValues.put(COL_5, path);
            db.insert(TABLE_NAME, null, contentValues);
            db.close();
        }
    }

    public List<Mp3Item> getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        List<Mp3Item> mp3Items = new ArrayList<>();
        mp3Items.clear();
        res.moveToFirst();
        for (int i = 0; i < res.getCount(); i++) {
            Mp3Item mp3Item = new Mp3Item();
            mp3Item.setmSongName(res.getString(1));
            mp3Item.setmAddedUser(res.getString(2));
            mp3Item.setPath("");
            mp3Items.add(mp3Item);
            res.moveToNext();
        }
        res.close();
        db.close();
        return mp3Items;
    }

    public String getPath(String songName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select PATH from " + TABLE_NAME + " where SONG_NAME = ?", new String[]{songName});
        res.moveToFirst();
        String path = res.getString(0);
        res.close();
        db.close();
        return path;
    }

    public int isDownloaded(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select IS_DOWNLOADED from " + TABLE_NAME + " where SONG_NAME = ?", new String[]{name});
        res.moveToFirst();
        int is_downloaded = res.getInt(0);
        res.close();
        db.close();
        return is_downloaded;
    }


    public void updateData(String name, int is_downloaded) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_4, is_downloaded);
        db.update(TABLE_NAME, contentValues, "SONG_NAME = ?", new String[]{name});
        db.close();
    }

    public void deleteData(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "SONG_NAME = ?", new String[]{name});
        db.close();
    }
}
