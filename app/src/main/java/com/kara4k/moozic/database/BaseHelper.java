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

        db.execSQL("create table " + SearchTracks.NAME + "(" +
                " _id integer primary key autoincrement, " +
                SearchTracks.Cols.NAME + ", " +
                SearchTracks.Cols.TRACK_FILE + ", " +
                SearchTracks.Cols.FILE_PATH + ", " +
                SearchTracks.Cols.TRACK_NAME + ", " +
                SearchTracks.Cols.TRACK_ARTIST + ", " +
                SearchTracks.Cols.DURATION_MS + ", " +
                SearchTracks.Cols.DURATION + ", " +
                SearchTracks.Cols.EXTENSION + ", " +
                SearchTracks.Cols.DATE + ", " +
                SearchTracks.Cols.BITRATE + ", " +
                SearchTracks.Cols.IS_RADIO + ", " +
                SearchTracks.Cols.POSITION + " integer)"
        );


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
