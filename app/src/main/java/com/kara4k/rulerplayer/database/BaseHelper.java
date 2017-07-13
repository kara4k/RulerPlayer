package com.kara4k.rulerplayer.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kara4k.rulerplayer.PlaylistHolder;
import com.kara4k.rulerplayer.RadioFragment;
import com.kara4k.rulerplayer.TrackItem;

import static com.kara4k.rulerplayer.database.DbSchemes.Playlist;
import static com.kara4k.rulerplayer.database.DbSchemes.RadioList;

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

        db.execSQL("create table " + RadioList.NAME + "(" +
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

        db.insert(RadioList.NAME, null, getExampleRadioValues());




    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private ContentValues getExampleRadioValues(){
        TrackItem exampleRadio = RadioFragment.createRadioTrack(
                "ZAYCEV.FM", "Example: 256k", "https://zaycevfm.cdnvideo.ru/ZaycevFM_pop_256.mp3");
       return PlaylistHolder.getContentValues(exampleRadio, 0);
    }
}
