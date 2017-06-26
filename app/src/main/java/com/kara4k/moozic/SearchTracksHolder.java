package com.kara4k.moozic;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.kara4k.moozic.database.BaseHelper;

import static com.kara4k.moozic.database.DbSchemes.SearchTracks;

public class SearchTracksHolder {

    private static SearchTracksHolder sSearchTracksHolder;

    private final SQLiteDatabase mDatabase;

    public static SearchTracksHolder getInstance(Context context) {
        if (sSearchTracksHolder == null) {
            sSearchTracksHolder = new SearchTracksHolder(context);
        }
        return sSearchTracksHolder;
    }

    private SearchTracksHolder(Context context) {
        mDatabase = new BaseHelper(context).getWritableDatabase();
    }

    public void addTrack(TrackItem trackItem) {

    }
//    String name = getString(getColumnIndex(SearchTracks.Cols.NAME));
//    String trackFile = getString(getColumnIndex(SearchTracks.Cols.TRACK_FILE));
//    String filePath = getString(getColumnIndex(SearchTracks.Cols.FILE_PATH));
//    String trackName = getString(getColumnIndex(SearchTracks.Cols.TRACK_NAME));
//    String trackArtist = getString(getColumnIndex(SearchTracks.Cols.TRACK_ARTIST));
//    int durationMs = getInt(getColumnIndex(SearchTracks.Cols.DURATION_MS));
//    String duration = getString(getColumnIndex(SearchTracks.Cols.DURATION));
//    String extension = getString(getColumnIndex(SearchTracks.Cols.EXTENSION));
//    long date = getLong(getColumnIndex(SearchTracks.Cols.DATE));
//    String bitrate = getString(getColumnIndex(SearchTracks.Cols.BITRATE));
//    int isRadio = (getColumnIndex(SearchTracks.Cols.IS_RADIO));
    private static ContentValues getContentValues(TrackItem trackItem){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SearchTracks.Cols.NAME, trackItem.getName()); // TODO: 26.06.2017
        contentValues.put(SearchTracks.Cols.TRACK_FILE, trackItem.getFile().getPath());
        contentValues.put(SearchTracks.Cols.NAME, trackItem.getName());
        contentValues.put(SearchTracks.Cols.NAME, trackItem.getName());
        contentValues.put(SearchTracks.Cols.NAME, trackItem.getName());
        contentValues.put(SearchTracks.Cols.NAME, trackItem.getName());

        return contentValues;
    }

}
