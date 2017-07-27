package com.kara4k.rulerplayer;


import java.util.Comparator;

public class TrackSorts {

    public static Comparator<TrackItem> byName() {
        return new Comparator<TrackItem>() {
            @Override
            public int compare(TrackItem trackItem, TrackItem t1) {
                if (!trackItem.isTrack() && t1.isTrack()) {
                    return -1;
                } else if (trackItem.isTrack() && !t1.isTrack()) {
                    return 1;
                } else if (!trackItem.isTrack() && !t1.isTrack()) {
                    return trackItem.getName().compareToIgnoreCase(t1.getName());
                } else if (trackItem.getTrackName() != null && t1.getTrackName() != null) {
                    return trackItem.getTrackName().compareToIgnoreCase(t1.getTrackName());
                } else if (trackItem.getTrackName() == null && t1.getTrackName() != null) {
                    return trackItem.getName().compareToIgnoreCase(t1.getTrackName());
                } else if (trackItem.getTrackName() != null && t1.getTrackName() == null) {
                    return trackItem.getTrackName().compareToIgnoreCase(t1.getName());
                } else {
                    return trackItem.getName().compareToIgnoreCase(t1.getName());
                }

            }
        };
    }

    public static Comparator<TrackItem> byArtist() {
        return new Comparator<TrackItem>() {
            @Override
            public int compare(TrackItem track, TrackItem t1) {
                if (!track.isTrack() && t1.isTrack()) {
                    return -1;
                } else if (track.isTrack() && !t1.isTrack()) {
                    return 1;
                } else if (!track.isTrack() && !t1.isTrack()) {
                    return track.getName().compareToIgnoreCase(t1.getName());
                } else if (track.getTrackArtist() != null && t1.getTrackArtist() != null) {
                    return track.getTrackArtist().compareToIgnoreCase(t1.getTrackArtist());
                } else if (track.getTrackArtist() == null && t1.getTrackArtist() != null) {
                    return -1;
                } else if (track.getTrackArtist() != null && t1.getTrackArtist() == null) {
                    return 1;
                } else {
                    return track.getName().compareToIgnoreCase(t1.getName());
                }
            }
        };
    }

    public static Comparator<TrackItem> byType() {
        return new Comparator<TrackItem>() {
            @Override
            public int compare(TrackItem track, TrackItem t1) {
                if (!track.isTrack() && t1.isTrack()) {
                    return -1;
                } else if (track.isTrack() && !t1.isTrack()) {
                    return 1;
                } else if (!track.isTrack() && !t1.isTrack()) {
                    return track.getName().compareToIgnoreCase(t1.getName());
                } else if (!track.getExtension().equals(t1.getExtension())) {
                    return track.getExtension().compareToIgnoreCase(t1.getExtension());
                } else {
                    return 0;
                }
            }
        };
    }

    public static Comparator<TrackItem> byDate() {
        return new Comparator<TrackItem>() {
            @Override
            public int compare(TrackItem track, TrackItem t1) {
                if (!track.isTrack() && t1.isTrack()) {
                    return -1;
                } else if (track.isTrack() && !t1.isTrack()) {
                    return 1;
                } else if (!track.isTrack() && !t1.isTrack()) {
                    return track.getName().compareToIgnoreCase(t1.getName());
                } else if (track.getDate() > t1.getDate()) {
                    return -1;
                } else if (track.getDate() < t1.getDate()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }
}
