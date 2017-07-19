package com.kara4k.rulerplayer;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

class NotificationManager {

    public static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_MAIN_ACTIVITY = 5;

    public static final String NOTIFICATION_ACTIONS = "com.kara4k.rulerplayer.notification";
    public static final String ACTION = "action";

    public static final int ACTION_PREV = 0;
    public static final int ACTION_PLAY = 1;
    public static final int ACTION_NEXT = 2;

    private final Context mContext;

    public NotificationManager(Context context) {
        mContext = context;
    }


    public Notification getNotification(MediaPlayer mediaPlayer, TrackItem trackItem) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setCustomContentView(getRemoteView(trackItem));
        builder.setCustomBigContentView(getBigRemoteView(mediaPlayer, trackItem));
        builder.setOngoing(true);

        if (mediaPlayer == null) {
            builder.setSmallIcon(R.drawable.ic_stop_white_24dp);
        } else {
            if (mediaPlayer.isPlaying()) {
                builder.setSmallIcon(R.drawable.ic_play_arrow_white_24dp);
            } else {
                builder.setSmallIcon(R.drawable.ic_pause_white_24dp);
            }
        }
        return builder.build();
    }


    private RemoteViews getRemoteView(TrackItem trackItem) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification);
        fillTrackInfoViews(trackItem, remoteViews);

        remoteViews.setOnClickPendingIntent(R.id.notification, getMainPI());
        return remoteViews;
    }


    private RemoteViews getBigRemoteView(MediaPlayer mediaPlayer, TrackItem trackItem) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_big);
        setPlayPauseIcon(mediaPlayer, remoteViews);
        fillTrackInfoViews(trackItem, remoteViews);

        remoteViews.setOnClickPendingIntent(R.id.notification_big, getMainPI());
        remoteViews.setOnClickPendingIntent(R.id.prev, createActionsPI(ACTION_PREV));
        remoteViews.setOnClickPendingIntent(R.id.play, createActionsPI(ACTION_PLAY));
        remoteViews.setOnClickPendingIntent(R.id.next, createActionsPI(ACTION_NEXT));

        return remoteViews;
    }

    private void setPlayPauseIcon(MediaPlayer mediaPlayer, RemoteViews remoteViews) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            remoteViews.setImageViewResource(R.id.play, R.drawable.ic_pause_white_24dp);
        } else {
            remoteViews.setImageViewResource(R.id.play, R.drawable.ic_play_arrow_white_24dp);
        }
    }

    private void fillTrackInfoViews(TrackItem trackItem, RemoteViews remoteViews) {
        if (trackItem != null) {
            if (trackItem.getTrackName() == null) {
                remoteViews.setInt(R.id.name, "setLines", 2);
                remoteViews.setTextViewText(R.id.name, trackItem.getName());
                remoteViews.setViewVisibility(R.id.artist, View.GONE);
            } else {
                remoteViews.setInt(R.id.name, "setLines", 1);
                remoteViews.setTextViewText(R.id.name, trackItem.getTrackName());
                remoteViews.setTextViewText(R.id.artist, trackItem.getTrackArtist());
                remoteViews.setViewVisibility(R.id.artist, View.VISIBLE);
            }

            remoteViews.setTextViewText(R.id.duration, trackItem.getDuration());
        }
    }

    private PendingIntent createActionsPI(int action) {
        return PendingIntent.getBroadcast(mContext, action, getActionIntent(action), PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private Intent getActionIntent(int action) {
        Intent intent = new Intent(NOTIFICATION_ACTIONS);
        intent.putExtra(ACTION, action);
        return intent;
    }


    private PendingIntent getMainPI() {
        Intent mainIntent = new Intent(RulerPlayerActivity.newIntent(mContext));
        return PendingIntent.getActivity(mContext, REQUEST_MAIN_ACTIVITY
                , mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
