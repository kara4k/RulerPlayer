package com.kara4k.rulerplayer;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public class ZaycevTracksParser extends HandlerThread implements Handler.Callback {

    public static final int FILE_PATH = 1;
    public static final int BITRATE = 2;
    public static final int ZERO_LENGTH = 3;

    private Context mContext;
    private Handler mHandler;
    private int mBitrateCount;
    private int mPathCount;
    private List<TrackItem> mTrackItems;
    private TrackParser mTrackParser;
    private ZaycevFetchr mZaycevFetchr;
    boolean isSizeRequested;
    private Handler mFragmentHandler;

    interface TrackParser {
        void onTracksReceived(List<TrackItem> list);
    }

    public ZaycevTracksParser(Context context, TrackParser trackParser) {
        super(" ");
        mContext = context;
        mTrackItems = new ArrayList<>();
        mTrackParser = trackParser;
    }

    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler(this);
        mZaycevFetchr = new ZaycevFetchr(mContext, mHandler);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == FILE_PATH) {
            mTrackItems.add((TrackItem) msg.obj);
            mPathCount++;
        } else if (msg.what == BITRATE) {
            mBitrateCount++;
        }

        if (isSizeRequested) {
            if (mBitrateCount == msg.arg1 && mPathCount == msg.arg1) {
                notifyInfoLoaded();
            }
        } else {
            if (mPathCount == msg.arg1) {
                notifyInfoLoaded();
            }
        }
        return false;
    }

    private void notifyInfoLoaded() {
        if (mFragmentHandler == null) {
            return;
        }
        mFragmentHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mTrackParser != null) {
                    mTrackParser.onTracksReceived(mTrackItems);
                }
            }
        });

    }

    public void getTracks(final String query, final int page) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resetTracks();
                    isSizeRequested = Preferences.isRequestSize(mContext);
                    mZaycevFetchr.getTracks(query, page);
                } catch (Exception e) {
                    e.printStackTrace();
                    notifyInfoLoaded();
                }
            }
        }).start();
    }

    public void getTracks(final int page) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resetTracks();
                    isSizeRequested = Preferences.isRequestSize(mContext);
                    mZaycevFetchr.getTracks(page);
                } catch (Exception e) {
                    e.printStackTrace();
                    notifyInfoLoaded();
                }
            }
        }).start();
    }

    private void resetTracks() {
        removeMessages();
        mTrackItems = new ArrayList<TrackItem>();
        mBitrateCount = 0;
        mPathCount = 0;
    }

    public void stopMessages() {
        mZaycevFetchr.clearQueue();
        mFragmentHandler = null;
        removeMessages();
    }

    private void removeMessages() {
        if (mHandler != null) {
            mHandler.removeMessages(BITRATE);
            mHandler.removeMessages(FILE_PATH);
            mHandler.removeMessages(ZERO_LENGTH);
        }
    }

    public void setFragmentHandler(Handler fragmentHandler) {
        mFragmentHandler = fragmentHandler;
    }
}


