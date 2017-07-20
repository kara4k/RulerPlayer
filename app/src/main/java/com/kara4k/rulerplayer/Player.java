package com.kara4k.rulerplayer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;

import static android.content.Context.AUDIO_SERVICE;

public class Player implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {


    public static final int PROGRESS = 1;
    public static final int BUFFERING = 2;

    private static Player sPlayer;

    private final Context mContext;
    private final AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private boolean playOnInterrupt;
    private Handler mSingleFragHandler;
    private PlayerSingleCallback mPlayerSingleCallback;
    private PlayerListCallback mPlayerListCallback;
    private boolean shouldStop = false;
    private final Player mPlayer;
    private final NotificationManager mNotificationManager;


    interface PlayerSingleCallback {
        void onPlayTrack(TrackItem trackItem);

        void onPlay();

        void onPauseTrack();

        void onStopTrack();

    }

    interface PlayerListCallback {

        void playNext();

        void playPrev();

        void repeatCurrent();
    }

    public static Player getInstance(Context context) {
        if (sPlayer == null) {
            sPlayer = new Player(context);
        }
        return sPlayer;
    }

    private Player(Context context) {
        mPlayer = this;
        mContext = context;
        playOnInterrupt = false;
        mAudioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mNotificationManager = new NotificationManager(mContext);
        mContext.registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }


    public void playToggle() {
        if (mMediaPlayer == null) {
            repeatCurrent();
        } else {
            togglePlayPause();
        }
    }

    public void playTrack(final TrackItem trackItem) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                stop();
                try {
                    if (trackItem == null) return;
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setOnCompletionListener(mPlayer);
                    mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mp, int percent) {
                            if (mSingleFragHandler != null) {
                                mSingleFragHandler.obtainMessage(BUFFERING, percent, 0).sendToTarget();
                            }
                        }
                    });
                    mMediaPlayer.setDataSource(trackItem.getFilePath());
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();

                    if (mPlayerSingleCallback != null) {
                        mPlayerSingleCallback.onPlayTrack(trackItem);
                        mPlayerSingleCallback.onPlay();
                    }

                    startTracking();
                    updateNotification(trackItem);
                } catch (Exception e) {
                    stopTracking();
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void play() {
        if (mMediaPlayer == null) return;
        mMediaPlayer.start();
        if (mPlayerSingleCallback != null) {
            mPlayerSingleCallback.onPlay();
        }
        startTracking();
        updateNotification(null);
    }

    public void playNext() {
        if (mPlayerListCallback != null) {
            mPlayerListCallback.playNext();
        }
    }

    private void repeatCurrent() {
        if (mPlayerListCallback != null) {
            mPlayerListCallback.repeatCurrent();
        }
    }

    public void playPrev() {
        if (mPlayerListCallback != null) {
            mPlayerListCallback.playPrev();
        }
    }

    public void play(TrackItem trackItem) {
        if (mMediaPlayer == null) {
            playTrack(trackItem);

        } else {
            play();
        }
    }

    public void pause() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        if (mPlayerSingleCallback != null) {
            mPlayerSingleCallback.onPauseTrack();
        }
        stopTracking();
        updateNotification(null);
    }

    public void togglePlayPause() {
        if (mMediaPlayer == null) {
            mPlayerListCallback.repeatCurrent();
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void stop() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        if (mPlayerSingleCallback != null) {
            mPlayerSingleCallback.onStopTrack();
        }
        updateNotification(null);
    }

    public boolean isPlaying() {
        try {
            if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onAudioFocusChange(int i) {
        if (mMediaPlayer != null) {
            if (i <= 0 && i != -3) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    playOnInterrupt = true;
                }
            } else if (i > 0 && playOnInterrupt) {
                play();
                playOnInterrupt = false;
            }
        }
    }

    public void release() {
        releaseMediaPlayer();
        mAudioManager.abandonAudioFocus(this);
        mContext.unregisterReceiver(headsetReceiver);
        sPlayer = null;
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public int getDuration() {
        if (mMediaPlayer == null) return -1;
        return mMediaPlayer.getDuration();
    }

    public int getPosition() {
        if (mMediaPlayer == null) return -1;
        return mMediaPlayer.getCurrentPosition();
    }

    public void startTracking() {
        shouldStop = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!shouldStop) {
                    try {
                        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                            if (mSingleFragHandler == null) {
                                shouldStop = true;
                                continue;
                            }
                            mSingleFragHandler.obtainMessage(PROGRESS,
                                    mMediaPlayer.getCurrentPosition(), 0).sendToTarget();

                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    public void stopTracking() {
        shouldStop = true;
    }

    public void setSingleFragHandler(Handler singleFragHandler) {
        mSingleFragHandler = singleFragHandler;

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        boolean repeatOne = Preferences.isRepeatOne(mContext);
        if (repeatOne) {
            repeatCurrent();
        } else {
            playNext();
        }
    }

    public void setPlayerSingleCallback(PlayerSingleCallback playerSingleCallback) {
        mPlayerSingleCallback = playerSingleCallback;
    }

    public void setPlayerListCallback(PlayerListCallback playerListCallback) {
        mPlayerListCallback = playerListCallback;
    }

    private void updateNotification(TrackItem trackItem) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(mContext);
        nm.notify(NotificationManager.NOTIFICATION_ID
                , mNotificationManager.getNotification(mMediaPlayer, trackItem));
    }

    private final BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        pause();
                        break;
                    case 1:
                        play();
                        break;
                    default:
                        break;
                }
            }
        }
    };
}
