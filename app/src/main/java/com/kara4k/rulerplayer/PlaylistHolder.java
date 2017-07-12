package com.kara4k.rulerplayer;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kara4k.rulerplayer.database.BaseHelper;
import com.kara4k.rulerplayer.database.TrackItemCursorWrapper;

import java.util.ArrayList;
import java.util.List;

import static com.kara4k.rulerplayer.database.DbSchemes.Playlist;

public class PlaylistHolder {

    private static PlaylistHolder sPlaylistHolder;

    private final SQLiteDatabase mDatabase;

    public static PlaylistHolder getInstance(Context context) {
        if (sPlaylistHolder == null) {
            sPlaylistHolder = new PlaylistHolder(context);
        }
        return sPlaylistHolder;
    }

    private PlaylistHolder(Context context) {
        mDatabase = new BaseHelper(context).getWritableDatabase();
    }

    public void addTracks(List<TrackItem> trackItems) {
        for (int i = 0; i < trackItems.size(); i++) {
            TrackItem trackItem = trackItems.get(i);
            boolean isExist = isExist(trackItem);
            if (!isExist) {
                ContentValues values = getContentValues(trackItem, i);
                mDatabase.insert(Playlist.NAME, null, values);
            }
        }

    }



    public void getRadioItems() {

    }

    public void updateItemsPositions(List<TrackItem> items){
        for (int i = 0; i < items.size(); i++) {
            updateItemPosition(items.get(i), i);
        }
    }


    public void updateItemPosition(TrackItem trackItem, int position) {
        String filePath = trackItem.getFilePath();
        ContentValues values = new ContentValues();
        values.put(Playlist.Cols.POSITION, position);
        mDatabase.update(Playlist.NAME, values, Playlist.Cols.FILE_PATH + " = ?", new String[]{filePath});
    }



    public boolean isExist(TrackItem trackItem) {
        String clause = Playlist.Cols.FILE_PATH + " = ?";
        String[] args = new String[]{trackItem.getFilePath()};
        Cursor cursor = mDatabase.query(Playlist.NAME, null, clause, args, null, null, null);
        return cursor.moveToFirst();
    }

    public void deleteItems(List<TrackItem> trackItems) {
        for (int i = 0; i < trackItems.size(); i++) {
            deleteItem(trackItems.get(i));
        }
    }


    public void deleteItem(TrackItem trackItem) {
        String filePath = String.valueOf(trackItem.getFilePath());
        mDatabase.delete(Playlist.NAME, Playlist.Cols.FILE_PATH + " = ?",
                new String[]{filePath});
    }

    public void deleteAllItems() {
        mDatabase.delete(Playlist.NAME, null, null);
    }

    public List<TrackItem> getItems() {
        TrackItemCursorWrapper cursorWrapper = queryItems(null, null);
        List<TrackItem> tracks = new ArrayList<>();
        if (cursorWrapper.moveToFirst()) {
            while (!cursorWrapper.isAfterLast()) {
                tracks.add(cursorWrapper.getTrackItem());
                cursorWrapper.moveToNext();
            }
        }
        return tracks;
    }

    private TrackItemCursorWrapper queryItems(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(Playlist.NAME, null, whereClause, whereArgs,
                null, null, Playlist.Cols.POSITION + " ASC");
        return new TrackItemCursorWrapper(cursor);
    }




    static ContentValues getContentValues(TrackItem trackItem, int position) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlist.Cols.NAME, trackItem.getName());
        if (trackItem.getFile() != null) {
            contentValues.put(Playlist.Cols.TRACK_FILE, trackItem.getFile().getPath());
        }
        contentValues.put(Playlist.Cols.FILE_PATH, trackItem.getFilePath());
        contentValues.put(Playlist.Cols.TRACK_NAME, trackItem.getTrackName());
        contentValues.put(Playlist.Cols.TRACK_ARTIST, trackItem.getTrackArtist());
        contentValues.put(Playlist.Cols.DURATION_MS, trackItem.getDurationMs());
        contentValues.put(Playlist.Cols.DURATION, trackItem.getDuration());
        contentValues.put(Playlist.Cols.EXTENSION, trackItem.getExtension());
        contentValues.put(Playlist.Cols.DATE, trackItem.getDate());
        contentValues.put(Playlist.Cols.BITRATE, trackItem.getBitrate());
        int radio = trackItem.isRadio() ? 1 : 0;
        contentValues.put(Playlist.Cols.IS_RADIO, radio);
        int online = trackItem.isOnline() ? 1 : 0;
        contentValues.put(Playlist.Cols.IS_ONLINE, online);
        contentValues.put(Playlist.Cols.POSITION, position);

        return contentValues;
    }

}
