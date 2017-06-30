package com.kara4k.moozic;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kara4k.moozic.database.BaseHelper;
import com.kara4k.moozic.database.DbSchemes;
import com.kara4k.moozic.database.TrackItemCursorWrapper;

import java.util.ArrayList;
import java.util.List;

import static com.kara4k.moozic.database.DbSchemes.RadioList;

public class RadioListHolder {

    private static RadioListHolder sRadioListHolder;

    private final SQLiteDatabase mDatabase;

    public static RadioListHolder getInstance(Context context) {
        if (sRadioListHolder == null) {
            sRadioListHolder = new RadioListHolder(context);
        }
        return sRadioListHolder;
    }

    private RadioListHolder(Context context) {
        mDatabase = new BaseHelper(context).getWritableDatabase();
    }

    public void addRadio(TrackItem radioTrack) {
        int position = getLastPosition();
        ContentValues values = PlaylistHolder.getContentValues(radioTrack, ++position);
        mDatabase.insert(RadioList.NAME, null, values);
    }

    public void deleteItems(List<TrackItem> trackItems) {
        for (int i = 0; i < trackItems.size(); i++) {
            deleteItem(trackItems.get(i));
        }
    }

    public void deleteItem(TrackItem trackItem) {
        String filePath = String.valueOf(trackItem.getFilePath());
        mDatabase.delete(RadioList.NAME, DbSchemes.Playlist.Cols.FILE_PATH + " = ?",
                new String[]{filePath});
    }

    public boolean isExist(TrackItem trackItem) {
        String clause = DbSchemes.Playlist.Cols.FILE_PATH + " = ?";
        String[] args = new String[]{trackItem.getFilePath()};
        Cursor cursor = mDatabase.query(RadioList.NAME, null, clause, args, null, null, null);
        return cursor.moveToFirst();
    }

    public void updateRadio(TrackItem trackItem, int position) {
        ContentValues values = PlaylistHolder.getContentValues(trackItem, position);
        mDatabase.update(RadioList.NAME, values, DbSchemes.Playlist.Cols.POSITION + " = ?"
                , new String[]{String.valueOf(position)});
    }

    public void updateItemsPositions(List<TrackItem> items){
        for (int i = 0; i < items.size(); i++) {
            updateItemPosition(items.get(i), i);
        }
    }


    public void updateItemPosition(TrackItem trackItem, int position) {
        String filePath = trackItem.getFilePath();
        ContentValues values = new ContentValues();
        values.put(DbSchemes.Playlist.Cols.POSITION, position);
        mDatabase.update(RadioList.NAME, values, DbSchemes.Playlist.Cols.FILE_PATH + " = ?", new String[]{filePath});
    }

    public List<TrackItem> getItems() {
        TrackItemCursorWrapper cursorWrapper = queryRadioItems(null, null);
        List<TrackItem> tracks = new ArrayList<>();
        if (cursorWrapper.moveToFirst()) {
            while (!cursorWrapper.isAfterLast()) {
                tracks.add(cursorWrapper.getTrackItem());
                cursorWrapper.moveToNext();
            }
        }
        return tracks;
    }

    public int getLastPosition() {
        Cursor cursor = mDatabase.query(RadioList.NAME
                , new String[]{DbSchemes.Playlist.Cols.POSITION}
                , null, null, null, null, DbSchemes.Playlist.Cols.POSITION + " DESC");
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex(DbSchemes.Playlist.Cols.POSITION));
        } else {
            return -1;
        }
    }

    private TrackItemCursorWrapper queryRadioItems(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(RadioList.NAME, null, whereClause, whereArgs,
                null, null, DbSchemes.Playlist.Cols.POSITION + " ASC");
        return new TrackItemCursorWrapper(cursor);
    }

}
