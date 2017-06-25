package com.kara4k.moozic;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CardFragment extends MusicFragment
        implements SearchView.OnQueryTextListener, Player.PlayerListCallback {

    public static final int PERMISSION_STORAGE = 1;


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
        checkPermissionsAndSetupCurrentFolder();
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
        if (mCurrentTrack == null) return;
        mCardCallbacks.onPlayPressed(mCurrentTrack);
        if (mCurrentDir == null) return;
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

    private void checkPermissionsAndSetupCurrentFolder() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        boolean hasSDPermissions = isHasSDPermissions();
        if (hasSDPermissions) {
            setCurrentDir();
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast toast = Toast.makeText(getContext(),
                            R.string.snackbar_sd_card_access, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    setCurrentDir();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean isHasSDPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadSDPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasReadSDPermission == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
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
        if (mCurrentTrack != null) {
            mCurrentDir = mCurrentTrack.getFile().getParentFile();
        } else {
            mCurrentDir = Environment.getExternalStorageDirectory();
        }
    }

    @Override
    protected void setCurrentTrack(TrackItem trackItem) {
        super.setCurrentTrack(trackItem);
        Preferences.setCurrentTrack(getContext(), trackItem);
    }

    @Override
    int getCurrentTrackIndex() {
        if (mCurrentTrack == null || mCurrentDir == null) return -1;
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
