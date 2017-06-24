package com.kara4k.moozic;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import static android.content.Context.AUDIO_SERVICE;

public class Player implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {


    public static final int PROGRESS = 1;


    private Context mContext;
    private final AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private boolean playOnInterrupt;
    private Handler mSingleFragHandler;
    private PlayerSingleCallback mPlayerSingleCallback;
    private PlayerListCallback mPlayerListCallback;
    private boolean shouldStop = false;

    interface PlayerSingleCallback {
        void onPlayTrack(TrackItem trackItem);

        void onPlay();

        void onPauseTrack();

        void onStopTrack();

    }

    interface PlayerListCallback {
        void playNext();

        void playPrev();
    }


    public Player(Context context) {
        mContext = context;
        playOnInterrupt = false;
        mAudioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void playToggle() {
        if (mMediaPlayer == null) {
            TrackItem currentTrack = Preferences.getCurrentTrack(mContext);
            playTrack(currentTrack);
        } else {
            togglePlayPause();
        }
    }

    public void playTrack(TrackItem trackItem) {
//        playOnInterrupt = true;
        stop();
        try {
            if (trackItem == null) return;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setDataSource(trackItem.getFile().toString());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            if (mPlayerSingleCallback != null) {
                mPlayerSingleCallback.onPlayTrack(trackItem);
                mPlayerSingleCallback.onPlay();
            }
            startTracking();

        } catch (Exception e) {
            stopTracking();
            e.printStackTrace();
            Toast toast = Toast.makeText(mContext, R.string.source_file_corrupted, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void play() {
        if (mMediaPlayer == null) return;
        mMediaPlayer.start();
        if (mPlayerSingleCallback != null) {
            mPlayerSingleCallback.onPlay();
        }
//        playOnInterrupt = true;
        startTracking();
    }

    public void playNext() {
        if (mPlayerListCallback != null) {
            mPlayerListCallback.playNext();
        }
//        playOnInterrupt = true;
    }

    public void playPrev() {
        if (mPlayerListCallback != null) {
            mPlayerListCallback.playPrev();
        }
//        playOnInterrupt = true;
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
    }

    public void resume() {

        if (mMediaPlayer == null) {
            return;
        }
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            playOnInterrupt = true;
        }
    }

    public void togglePlayPause() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            pause();
//            playOnInterrupt = false;
        } else {
            play();
//            playOnInterrupt = true;
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
//        playOnInterrupt = false;
    }

    @Override
    public void onAudioFocusChange(int i) {
        Log.e("Player", "onAudioFocusChange: " + "here");
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
        if (mMediaPlayer!=null) {
            mMediaPlayer.seekTo(position);
        }
    }

    public void stopTracking(){
        shouldStop = true;
    }

    public void setSingleFragHandler(Handler singleFragHandler) {
        mSingleFragHandler = singleFragHandler;

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        TrackItem currentTrack = Preferences.getCurrentTrack(mContext);
        if (currentTrack == null) {
            return;
        }

        boolean repeatOne = Preferences.isRepeatOne(mContext);
        if (repeatOne) {
            playTrack(currentTrack);
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
}
