package com.kara4k.moozic;


import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CardFragment extends MusicFragment
        implements SearchView.OnQueryTextListener, Player.PlayerListCallback {


    private File mCurrentDir;
    private CardTracksHolder mCardTracksHolder;
    private TrackInfoParser mTrackInfoParser;
    private List<TrackItem> mSearchableItems;


    public static CardFragment newInstance() {
        return new CardFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchableItems = new ArrayList<>();
        mCardTracksHolder = new CardTracksHolder(getContext());
        setupTrackInfoReceiver();
        checkSdPermission();
    }

    @Override
    void onCreateView() {
        updateUI(mCurrentDir);
    }

    @Override
    void onBackPressed() {
        updateUI(mCurrentDir.getParentFile());
    }

    @Override
    void onPlayBtnPressed() {
        if (mCurrentTrack == null ) return;
        mCardCallbacks.onPlayPressed(mCurrentTrack);
        if (mCurrentDir == null || mCurrentTrack.isOnline()) return;
        File parentFile = mCurrentTrack.getFile().getParentFile();
        if (!mCurrentDir.getPath().equals(parentFile.getPath())) {
            updateUI(parentFile);
        }
        int currentIndex = mTracksAdapter.getCurrentIndex();
        if (currentIndex == -1) return;
        mLayoutManager.scrollToPosition(currentIndex);
    }

    @Override
    void onTrackHolderClick(TrackItem trackItem, int newPosition) {
        if (!trackItem.isTrack()) {
            updateUI(trackItem.getFile());
        } else {
            playTrack(trackItem, newPosition);
        }
    }

    @Override
    void onQuerySearchChanged(String newText) {
        ArrayList<TrackItem> foundItems = new ArrayList<>();
        for (int i = 0; i < mSearchableItems.size(); i++) {
            String query = newText.toLowerCase();
            String firstField = mSearchableItems.get(i).getFirstField().toLowerCase();
            String secondField = mSearchableItems.get(i).getSecondField().toLowerCase();
            if (firstField.contains(query) || secondField.contains(query)) {
                foundItems.add(mSearchableItems.get(i));
            }
        }
        mTracksAdapter.setITEMs(foundItems);
        mTracksAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onQuerySearchSubmit(String query) {
        return false;
    }

    @Override
    void onSdCardPermissionGranted() {
        setCurrentDir();
    }

    private void updateUI(File dir) {
        mTrackInfoParser.clearQueue();
        if (dir == null) {
            return;
        }
        mCurrentDir = dir;
        List<TrackItem> tracksInDir = mCardTracksHolder.getTracksInDir(dir);
        mTracksAdapter.setITEMs(tracksInDir);
        mTracksAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mTracksAdapter);

        mTrackInfoParser.queueTrackInfo(tracksInDir);
    }

    private void setupTrackInfoReceiver() {
        Handler handler = new Handler();
        mTrackInfoParser = new TrackInfoParser(handler);
        mTrackInfoParser.setInfoParserCallback(new TrackInfoParser.InfoParserCallback() {
            @Override
            public void onComplete(TrackItem filledTrackItem) {
                List<TrackItem> list = mTracksAdapter.getAllItems();
                for (int i = 0; i < list.size(); i++) {
                    String trackItemPath = list.get(i).getFile().getPath();
                    if (trackItemPath.equals(filledTrackItem.getFile().getPath())) {
                        list.set(i, filledTrackItem);
                        mTracksAdapter.notifyItemChanged(i);

                        if (i == list.size() - 1) {
                            mSearchableItems.clear();
                            mSearchableItems.addAll(mTracksAdapter.getAllItems());
                        }
                        break;
                    }
                }
            }
        });
        mTrackInfoParser.start();
        mTrackInfoParser.getLooper();
    }

    private void setCurrentDir() {
        mCurrentTrack = Preferences.getCurrentTrack(getContext());
        if (mCurrentTrack != null && !mCurrentTrack.isOnline()) {
            mCurrentDir = mCurrentTrack.getFile().getParentFile();
        } else {
            mCurrentDir = Environment.getExternalStorageDirectory();
        }
    }


    @Override
    int getCurrentTrackIndex() {
        if (mCurrentTrack == null || mCurrentTrack.isOnline() || mCurrentDir == null) return -1;
        File parentFile = mCurrentTrack.getFile().getParentFile();
        if (!mCurrentDir.getPath().equals(parentFile.getPath())) {
            updateUI(parentFile);
        }
        return mTracksAdapter.getCurrentIndex();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTrackInfoParser.quit();
    }

}
