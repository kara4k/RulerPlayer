package com.kara4k.moozic;


import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.List;

public class TrackInfoParser extends HandlerThread {

    public static final int GET_TRACK_INFO = 1;

    public static final String FILE_CORRUPTED = "-----";

    private Handler mHandler;
    private final MediaMetadataRetriever mDataRetriever;

    private Handler mFragmentHandler;
    private InfoParserCallback mInfoParserCallback;


    interface InfoParserCallback {
        void onComplete(TrackItem trackItem);
    }

    public TrackInfoParser(Handler fragmentHandler) {
        super("parser");
        mFragmentHandler = fragmentHandler;
        mDataRetriever = new MediaMetadataRetriever();
    }


    public void queueTrackInfo(List<TrackItem> trackItems) {
        for (int i = 0; i < trackItems.size(); i++) {
            if (trackItems.get(i).isTrack()) {
                mHandler.obtainMessage(GET_TRACK_INFO, trackItems.get(i)).sendToTarget();
            }
        }
    }


    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == GET_TRACK_INFO) {
                    TrackItem trackItem = (TrackItem) msg.obj;
                    handle(trackItem);
                }
            }
        };

    }

    private void handle(final TrackItem trackItem) {

        if (trackItem == null) {
            Log.e("TrackInfoParser", "handle: " + "here");
            return;
        }

        setTrackInfo(trackItem);

        mFragmentHandler.post(new Runnable() {
            @Override
            public void run() {
                mInfoParserCallback.onComplete(trackItem);
            }
        });


    }

    private void setTrackInfo(final TrackItem trackItem) {
        File file = trackItem.getFile();
        mDataRetriever.setDataSource(file.getPath());

        String trackName = mDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String trackArtist = mDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String trackDuration = mDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String bitrate = mDataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        String duration = getDuration(trackDuration);
        int durationMs = getDurationMs(trackDuration);

        trackItem.setTrackName(trackName);
        trackItem.setTrackArtist(trackArtist);
        trackItem.setDuration(duration);
        trackItem.setDurationMs(durationMs);
        trackItem.setBitrate(getBitrate(bitrate));
        trackItem.setHasInfo(true);


    }

    static int getDurationMs(String trackDuration) {
        int durationMs;
        if (trackDuration != null) {
            try {
                durationMs = Integer.parseInt(trackDuration);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        } else {
            durationMs = -1;
        }
        return durationMs;
    }

    public static String getBitrate(String bitrate) {
        if (bitrate == null) {
            return FILE_CORRUPTED;
        }
        return String.format("%s kbps", bitrate.replace("000", ""));
    }

    @NonNull
    public static String getDuration(String trackDuration) {
        if (trackDuration != null) {
            try {
                long time = Long.parseLong(trackDuration) / 1000;
                long h = time / 3600;
                long m = (time - h * 3600) / 60;
                long s = time - (h * 3600 + m * 60);
                String sec = String.valueOf(s);
                if (s < 10) {
                    sec = "0" + sec;
                }
                String dur;
                if (h == 0) {
                    dur = m + ":" + sec;
                } else {
                    dur = h + ":" + m + ":" + sec;
                }
                return dur;
            } catch (NumberFormatException e) {
                return FILE_CORRUPTED;
            }
        } else {
            return FILE_CORRUPTED;
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(GET_TRACK_INFO);
        mFragmentHandler.removeMessages(GET_TRACK_INFO);
    }

    public void setInfoParserCallback(InfoParserCallback infoParserCallback) {
        mInfoParserCallback = infoParserCallback;
    }
}
