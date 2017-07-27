package com.kara4k.rulerplayer;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class OnlineTracksParser implements Handler.Callback {

    public static final int BITRATE = 1;
    public static final int FILE_PATH = 2;
    public static final int ZERO_LENGTH = 3;


    private Handler mHandler;
    private int mBitrateCount;
    private int mPathCount;
    private List<TrackItem> mTrackItems;
    private TrackParser mTrackParser;
    private Fetchr mFetchr;

    interface TrackParser {
        void onTracksReceived(List<TrackItem> list);
    }

    public OnlineTracksParser(TrackParser trackParser) {
        mTrackItems = new ArrayList<>();
        mHandler = new Handler(this);
        mTrackParser = trackParser;
        mFetchr = new Fetchr(mHandler);
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.e("OnlineTracksParser", "handleMessage: " + msg.what);
        Fetchr.SendObj sendObj = (Fetchr.SendObj) msg.obj;
        if (msg.what == BITRATE) {
            mBitrateCount++;
            Log.e("bitrate", String.valueOf(mBitrateCount) + " " + ((Fetchr.SendObj) msg.obj).getTotalCount());
            sendObj.getTrackItem().setBitrate(sendObj.getBitrate());
            sendObj.getTrackItem().setExtension(sendObj.getFileSize());
            mTrackItems.add(sendObj.getTrackItem());

        } else if (msg.what == FILE_PATH) {
            mPathCount++;
            Log.e("path", String.valueOf(mPathCount) + " " + ((Fetchr.SendObj) msg.obj).getTotalCount());
            sendObj.getTrackItem().setFilePath(sendObj.getFilePath());
        }

        int totalCount = sendObj.getTotalCount();
        if (mBitrateCount == totalCount && mPathCount == totalCount) {
            Log.e("OnlineTracksParser", "handleMessage: " + "last");
            mTrackParser.onTracksReceived(mTrackItems);
        }
        return false;
    }

    public void getTracks(final String query, final int page) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resetTracks(page);
                    mFetchr.getTracks(query, page);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getTracks(final int page) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resetTracks(page);
                    mFetchr.getTracks(page);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void resetTracks(int page) {
//        if (page == 1) {
            mTrackItems = new ArrayList<TrackItem>();
//        }
        mBitrateCount = 0;
        mPathCount = 0;
    }

    public void setTrackParser(TrackParser trackParser) {
        mTrackParser = trackParser;
    }

    public void clearQueue() {
        mFetchr.clearQueue();
        if (mHandler != null) {
            mHandler.removeMessages(BITRATE);
            mHandler.removeMessages(FILE_PATH);
        }
    }
}


