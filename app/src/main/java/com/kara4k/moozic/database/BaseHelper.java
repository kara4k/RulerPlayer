package com.kara4k.moozic.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.kara4k.moozic.database.DbSchemes.*;

public class BaseHelper extends SQLiteOpenHelper{


    private static final String DATABASE_NAME = "DataBase.db";
    private static final int VERSION = 1;


    public BaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + Playlist.NAME + "(" +
                " _id integer primary key autoincrement, " +
                Playlist.Cols.NAME + ", " +
                Playlist.Cols.TRACK_FILE + ", " +
                Playlist.Cols.FILE_PATH + ", " +
                Playlist.Cols.TRACK_NAME + ", " +
                Playlist.Cols.TRACK_ARTIST + ", " +
                Playlist.Cols.DURATION_MS + ", " +
                Playlist.Cols.DURATION + ", " +
                Playlist.Cols.EXTENSION + ", " +
                Playlist.Cols.DATE + ", " +
                Playlist.Cols.BITRATE + ", " +
                Playlist.Cols.IS_RADIO + " integer, " +
                Playlist.Cols.IS_ONLINE + " integer, " +
                Playlist.Cols.PLAYLIST + " integer, " +
                Playlist.Cols.SOME_STUFF + " integer, " +
                Playlist.Cols.POSITION + " integer)"
        );


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
