package com.kara4k.rulerplayer;


import android.support.annotation.NonNull;

import java.io.File;
import java.io.Serializable;

public class TrackItem extends SearchableItem implements Comparable<TrackItem>, Serializable {

    String mName;
    File mFile;
    String mFilePath;
    boolean mIsTrack;
    String mTrackName;
    String mTrackArtist;
    int mDurationMs;
    String mDuration;
    String mExtension;
    long mDate;
    String mBitrate;
    boolean mHasInfo;
    boolean mIsOnline;
    boolean mIsRadio;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public File getFile() {
        return mFile;
    }

    public void setFile(File filePath) {
        this.mFile = filePath;
    }

    public boolean isTrack() {
        return mIsTrack;
    }


    public void setTrack(boolean track) {
        mIsTrack = track;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String trackName) {
        this.mTrackName = trackName;
    }

    public String getTrackArtist() {
        return mTrackArtist;
    }

    public void setTrackArtist(String trackArtist) {
        this.mTrackArtist = trackArtist;
    }

    public boolean isRadio() {
        return mIsRadio;
    }

    public void setRadio(boolean radio) {
        mIsRadio = radio;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        this.mDuration = duration;
    }

    public String getExtension() {
        return mExtension;
    }

    public void setExtension(String extension) {
        this.mExtension = extension;
    }

    public long getDate() {
        return mDate;
    }

    public boolean isOnline() {
        return mIsOnline;
    }

    public void setOnline(boolean online) {
        mIsOnline = online;
    }

    public void setDate(long date) {
        this.mDate = date;
    }

    public boolean isHasInfo() {
        return mHasInfo;
    }

    public int getDurationMs() {
        return mDurationMs;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public void setDurationMs(int durationMs) {
        mDurationMs = durationMs;
    }

    public String getBitrate() {
        return mBitrate;
    }

    public void setBitrate(String bitrate) {
        mBitrate = bitrate;
    }

    public void setHasInfo(boolean hasInfo) {
        mHasInfo = hasInfo;
    }

    @Override
    public int compareTo(@NonNull TrackItem trackItem) {
        if (this.mFile.isDirectory() && !trackItem.mFile.isDirectory()) {
            return -1;
        } else if (!this.mFile.isDirectory() && trackItem.mFile.isDirectory()) {
            return 1;
        } else
            return this.mName.compareToIgnoreCase(trackItem.mName);

    }

    @Override
    protected String getFirstField() {
        if(mTrackName == null) return mName;
        if (mTrackName.equals("")) return mName;
        return mTrackName;
    }

    @Override
    protected String getSecondField() {
        if (mTrackArtist == null) return mName;
        if (mTrackArtist.equals("")) return mName;
        return mTrackArtist;
    }
}
