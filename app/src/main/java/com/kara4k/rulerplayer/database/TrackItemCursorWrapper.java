package com.kara4k.rulerplayer.database;


import android.database.Cursor;
import android.database.CursorWrapper;

import com.kara4k.rulerplayer.TrackItem;

import java.io.File;

import static com.kara4k.rulerplayer.database.DbSchemes.Playlist;

public class TrackItemCursorWrapper extends CursorWrapper {

    public TrackItemCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public TrackItem getTrackItem() {
        String name = getString(getColumnIndex(Playlist.Cols.NAME));
        String trackFile = getString(getColumnIndex(Playlist.Cols.TRACK_FILE));
        String filePath = getString(getColumnIndex(Playlist.Cols.FILE_PATH));
        String trackName = getString(getColumnIndex(Playlist.Cols.TRACK_NAME));
        String trackArtist = getString(getColumnIndex(Playlist.Cols.TRACK_ARTIST));
        int durationMs = getInt(getColumnIndex(Playlist.Cols.DURATION_MS));
        String duration = getString(getColumnIndex(Playlist.Cols.DURATION));
        String extension = getString(getColumnIndex(Playlist.Cols.EXTENSION));
        long date = getLong(getColumnIndex(Playlist.Cols.DATE));
        String bitrate = getString(getColumnIndex(Playlist.Cols.BITRATE));
        int isRadio = getInt(getColumnIndex(Playlist.Cols.IS_RADIO));
        int isOnline = getInt(getColumnIndex(Playlist.Cols.IS_ONLINE));

        int position = getInt(getColumnIndex(Playlist.Cols.POSITION));

        TrackItem item = new TrackItem();
        item.setTrack(true);
        item.setHasInfo(true);
        item.setOnline(isOnline == 1);
        item.setRadio(isRadio == 1);
        item.setName(name);
        if (trackFile!=null) {
            File file = new File(trackFile);
            if (file.exists()) {
                item.setFile(file);
            }
        }
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


