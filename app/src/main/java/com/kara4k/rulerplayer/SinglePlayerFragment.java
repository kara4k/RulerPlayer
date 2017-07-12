package com.kara4k.rulerplayer;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import static com.kara4k.rulerplayer.TrackInfoParser.getDuration;

public class SinglePlayerFragment extends Fragment implements Handler.Callback,
        Player.PlayerSingleCallback, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private SeekBar mSeekBar;
    private Player mPlayer;
    private RulerView mRulerView;
    private ImageButton mPlayImgBtn;
    private TextView mDurationTextView;
    private TextView mProgressTextView;
    private TextView mTrackNameTextView;
    private TextView mTrackArtistTextView;
    private RulerCycleView mRulerCycleView;
    private LinearLayout mCycleLayout;
    private LinearLayout mDurationLayout;
    private boolean mIsRadio;


    public static SinglePlayerFragment newInstance() {
        Bundle args = new Bundle();
        SinglePlayerFragment fragment = new SinglePlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Handler handler = new Handler(this);
        MoozicActivity activity = (MoozicActivity) getActivity();
        mPlayer = activity.getPlayer();
        mPlayer.setSingleFragHandler(handler);
        mPlayer.setPlayerSingleCallback(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MoozicActivity moozicActivity = (MoozicActivity) getActivity();
        moozicActivity.setActivityCallback(null);
        mPlayer.setSingleFragHandler(null);
        mPlayer.setPlayerSingleCallback(null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_player_fragment, container, false);
        mPlayImgBtn = (ImageButton) view.findViewById(R.id.play_image_button);
        mPlayImgBtn.setOnClickListener(this);
        ImageButton prevImgBtn = (ImageButton) view.findViewById(R.id.prev_image_button);
        prevImgBtn.setOnClickListener(this);
        ImageButton nextImgBtn = (ImageButton) view.findViewById(R.id.next_image_button);
        nextImgBtn.setOnClickListener(this);
        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mRulerView = (RulerView) view.findViewById(R.id.ruler_view);
        mRulerCycleView = (RulerCycleView) view.findViewById(R.id.ruler_cycle_view);
        mDurationTextView = (TextView) view.findViewById(R.id.duration_text_view);
        mProgressTextView = (TextView) view.findViewById(R.id.progress_text_view);
        mTrackNameTextView = (TextView) view.findViewById(R.id.track_name_text_view);
        mTrackArtistTextView = (TextView) view.findViewById(R.id.artist_text_view);
        Button startCycleBtn = (Button) view.findViewById(R.id.start_cycle_button);
        startCycleBtn.setOnClickListener(this);
        Button finishCycleBtn = (Button) view.findViewById(R.id.finish_cycle_button);
        finishCycleBtn.setOnClickListener(this);
        mCycleLayout = (LinearLayout) view.findViewById(R.id.cycle_layout);
        mDurationLayout = (LinearLayout) view.findViewById(R.id.duration_layout);
        initLastTrack();
        return view;
    }

    private void initLastTrack() {
        TrackItem lastTrack = Preferences.getCurrentTrack(getContext());
        if (lastTrack == null) {
            return;
        }

        setTrackName(lastTrack);
        setTrackDuration(lastTrack);

        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayImgBtn.setImageResource(R.drawable.ic_pause_white_48dp);
        }

        setupRadioViewsVisibility(lastTrack.isRadio());

        if (lastTrack.isRadio()) {
            return;
        }

        mDurationTextView.setText(lastTrack.getDuration());
        if (lastTrack.getDurationMs() == -1) {
            return;
        }
        mSeekBar.setMax(lastTrack.getDurationMs());

    }

    private void setupRadioViewsVisibility(boolean isRadio) {
        mIsRadio = isRadio;
        if (isRadio) {
            mDurationLayout.setVisibility(View.GONE);
            mSeekBar.setVisibility(View.GONE);
            mRulerView.setVisibility(View.GONE);
        } else {
            mDurationLayout.setVisibility(View.VISIBLE);
            mSeekBar.setVisibility(View.VISIBLE);
            mRulerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            hideActionBar();
            if (mPlayer != null) {
                mPlayer.startTracking();
            }
            setActivityCallback();
        } else {
            if (mPlayer != null) {
                mPlayer.stopTracking();
            }

        }
    }

    private void setActivityCallback() {
        MoozicActivity moozicActivity = (MoozicActivity) getActivity();
        moozicActivity.setActivityCallback(new MoozicActivity.ActivityCallback() {
            @Override
            public void onBackPressed() {
            }

            @Override
            public void onMenuPressed() {
                if (mIsRadio) {
                    return;
                }
                int visibility = mRulerCycleView.getVisibility();
                if (visibility == View.VISIBLE) {
                    mRulerCycleView.setVisibility(View.GONE);
                    mCycleLayout.setVisibility(View.GONE);
                    mRulerCycleView.stopCycle();
                } else {
                    mRulerCycleView.setVisibility(View.VISIBLE);
                    mCycleLayout.setVisibility(View.VISIBLE);
                }
            }

        });
    }


    private void hideActionBar() {
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar().isShowing()) {
                activity.getSupportActionBar().hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message.what == Player.PROGRESS) {
            int progress = message.arg1;
            String progressString = getDuration(String.valueOf(progress));
            mSeekBar.setProgress(progress);
            mProgressTextView.setText(progressString);
        }
        if (message.what == Player.BUFFERING) {
            int percentBuffered = message.arg1;
            mRulerView.setBuffering(percentBuffered);
            if (percentBuffered == 100) {
                mRulerView.stopBuffering();
            }

        }
        return true;
    }

    @Override
    public void onPlayTrack(final TrackItem trackItem) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRulerView.stopBuffering();
                setTrackName(trackItem);
                setTrackDuration(trackItem);

                setupRadioViewsVisibility(trackItem.isRadio());

                if (mIsRadio) {
                    return;
                }

                mSeekBar.setMax(mPlayer.getDuration());
                mDurationTextView.setText(trackItem.getDuration());
                mRulerView.invalidate();
            }
        });


    }

    private void setTrackDuration(TrackItem trackItem) {
        mDurationTextView.setText(trackItem.getDuration());
        if (trackItem.getDuration().equals(TrackInfoParser.FILE_CORRUPTED)) {
            mDurationTextView.setTextColor(Color.RED);
        } else {
            mDurationTextView.setTextColor(Color.WHITE);
        }
    }

    private void setTrackName(TrackItem trackItem) {
        if (trackItem.getTrackName() == null) {
            mTrackNameTextView.setText(trackItem.getName());
            mTrackArtistTextView.setVisibility(View.GONE);
        } else {
            mTrackNameTextView.setText(trackItem.getTrackName());
            mTrackArtistTextView.setText(trackItem.getTrackArtist());
            mTrackArtistTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPlay() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlayImgBtn.setImageResource(R.drawable.ic_pause_white_48dp);
            }
        });

    }

    @Override
    public void onPauseTrack() {
        mPlayImgBtn.setImageResource(R.drawable.ic_play_arrow_white_48dp);
    }

    @Override
    public void onStopTrack() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRulerCycleView.stopCycle();
                mRulerView.stopBuffering();
                mPlayImgBtn.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                mSeekBar.setProgress(0);
                mProgressTextView.setText("0:00");
                mRulerCycleView.setVisibility(View.GONE);
                mCycleLayout.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_image_button:
                if (mPlayer == null) return;
                mPlayer.playToggle();
                break;
            case R.id.next_image_button:
                if (mPlayer == null) return;
                mPlayer.stopTracking();
                mRulerCycleView.stopCycle();
                mPlayer.playNext();
                break;
            case R.id.prev_image_button:
                if (mPlayer == null) return;
                mPlayer.stopTracking();
                mRulerCycleView.stopCycle();
                mPlayer.playPrev();
                break;
            case R.id.start_cycle_button:
                mRulerCycleView.setCycleStart();
                break;
            case R.id.finish_cycle_button:
                mRulerCycleView.setCycleEnd();
                checkPositionInNewThread();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRulerCycleView.stopCycle();
    }

    private void checkPositionInNewThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRulerCycleView.isCycleMode()) {
                    if (mPlayer.getPosition() >= mRulerCycleView.getEndValue()) {
                        mPlayer.seekTo(mRulerCycleView.getStartValue());
                    } else if (mPlayer.getPosition() < mRulerCycleView.getStartValue()) {
                        mPlayer.seekTo(mRulerCycleView.getStartValue());
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int position, boolean isUser) {
        if (isUser) {
            mPlayer.seekTo(position);
            String positionString = getDuration(String.valueOf(position));
            mProgressTextView.setText(positionString);
            mRulerView.setBufferingStart();
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
