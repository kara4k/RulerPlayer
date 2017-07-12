package com.kara4k.rulerplayer;


import android.media.MediaMetadataRetriever;

import java.io.File;


class Tools {

    public static void setTrackInfo(final TrackItem trackItem) {
        MediaMetadataRetriever dataRetriever = new MediaMetadataRetriever();
        File file = trackItem.getFile();
        dataRetriever.setDataSource(file.getPath());

        String trackName = dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String trackArtist = dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String trackDuration = dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String bitrate = dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        String duration = TrackInfoParser.getDuration(trackDuration);
        int durationMs = TrackInfoParser.getDurationMs(trackDuration);

        trackItem.setTrackName(trackName);
        trackItem.setTrackArtist(trackArtist);
        trackItem.setDuration(duration);
        trackItem.setDurationMs(durationMs);
        trackItem.setBitrate(TrackInfoParser.getBitrate(bitrate));
        trackItem.setHasInfo(true);

    }

}
