package com.kara4k.moozic.database;


import android.database.Cursor;
import android.database.CursorWrapper;

import com.kara4k.moozic.TrackItem;

import java.io.File;

import static com.kara4k.moozic.database.DbSchemes.SearchTracks;

public class TrackItemCursorWrapper extends CursorWrapper {

    public TrackItemCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public TrackItem getTrackItem() {
        String name = getString(getColumnIndex(SearchTracks.Cols.NAME));
        String trackFile = getString(getColumnIndex(SearchTracks.Cols.TRACK_FILE));
        String filePath = getString(getColumnIndex(SearchTracks.Cols.FILE_PATH));
        String trackName = getString(getColumnIndex(SearchTracks.Cols.TRACK_NAME));
        String trackArtist = getString(getColumnIndex(SearchTracks.Cols.TRACK_ARTIST));
        int durationMs = getInt(getColumnIndex(SearchTracks.Cols.DURATION_MS));
        String duration = getString(getColumnIndex(SearchTracks.Cols.DURATION));
        String extension = getString(getColumnIndex(SearchTracks.Cols.EXTENSION));
        long date = getLong(getColumnIndex(SearchTracks.Cols.DATE));
        String bitrate = getString(getColumnIndex(SearchTracks.Cols.BITRATE));
        int isRadio = (getColumnIndex(SearchTracks.Cols.IS_RADIO));

        int position = getInt(getColumnIndex(SearchTracks.Cols.POSITION));

        File file = new File(trackFile);
        if (!file.exists()) return null;

        TrackItem item = new TrackItem();
        item.setTrack(true);
        item.setOnline(false);
        item.setRadio(isRadio == 1);
        item.setName(name);
        item.setFile(file);
        item.setFilePath(filePath);
        item.setTrackName(trackName);
        item.setTrackArtist(trackArtist);
        item.setDurationMs(durationMs);
        item.setDuration(duration);
        item.setExtension(extension);
        item.setDate(date);
        item.setBitrate(bitrate);
        return item;
    }


}


