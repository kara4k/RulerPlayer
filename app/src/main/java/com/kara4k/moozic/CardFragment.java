package com.kara4k.moozic;


import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CardFragment extends MusicFragment
        implements SearchView.OnQueryTextListener, Player.PlayerListCallback {

    public static final int SHOW_TRACKS = 1;
    public static final int DELETE_FILES = 2;

    private File mCurrentDir;
    private CardTracksHolder mCardTracksHolder;
    private TrackInfoParser mTrackInfoParser;
    private List<TrackItem> mSearchableItems;
    private boolean mIsPlaylist;
    private MenuItem mPlaylistBtn;


    public static CardFragment newInstance() {
        return new CardFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchableItems = new ArrayList<>();
        mCardTracksHolder = new CardTracksHolder(getContext());
        setupTrackInfoReceiver();
        mIsPlaylist = Preferences.isPlaylist(getContext());
        Log.e("CardFragment", "onCreate: " + mIsPlaylist);
        checkSdPermission(SHOW_TRACKS);
    }

    @Override
    void onCreateView() {
        if (mIsPlaylist) {
            showPlaylist();
        } else {
            updateUI(mCurrentDir);
            scrollToCurrentTrack();
        }
    }

    @Override
    void onBackPressed() {
        if (!mIsPlaylist) {
            updateUI(mCurrentDir.getParentFile());
        }
    }


    @Override
    void onPlayBtnPressed() {
        if (mCurrentTrack == null) return;
        mCardCallbacks.onPlayPressed(mCurrentTrack);
        if (mCurrentDir == null || mCurrentTrack.isOnline()) return;
        File parentFile = mCurrentTrack.getFile().getParentFile();
        if (!mCurrentDir.getPath().equals(parentFile.getPath())) {
            updateUI(parentFile);
        }
        scrollToCurrentTrack();
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
    void onSdCardPermissionGranted(int requestCode) {
        switch (requestCode) {
            case SHOW_TRACKS:
                setCurrentDir();
                break;
            case DELETE_FILES:
                deleteSelectedFiles();
                break;
        }
    }

    private void deleteSelectedFiles() {
        List<TrackItem> selectedItems = mTracksAdapter.getSelectedItems();
        for (int i = 0; i < selectedItems.size(); i++) {
            TrackItem trackItem = selectedItems.get(i);
            boolean deleted = trackItem.getFile().delete();
            if (!deleted) {
                Toast toast = Toast.makeText(getContext()
                        , getString(R.string.toast_cant_delete_file) + trackItem.getName()
                        , Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
        finishActionMode();
        updateUI(mCurrentDir);
    }

    @Override
    protected void onBottomBarCreated(Menu menu) {
        mPlaylistBtn = menu.findItem(R.id.last_btn);
        setPlaylistBtnIcon();
    }

    private void setPlaylistBtnIcon() {
        if (mIsPlaylist) {
            mPlaylistBtn.setIcon(R.drawable.ic_folder_special_white_24dp);
        } else {
            mPlaylistBtn.setIcon(R.drawable.ic_playlist_play_white_24dp);
        }
    }

    @Override
    void lastButtonPressed() {
        if (mActionMode != null || mIsSwapMode) return;
        if (!mIsPlaylist) {
            showPlaylist();
        } else {
            if (mCurrentTrack != null && mCurrentTrack.getFile() != null) {
                updateUI(mCurrentTrack.getFile().getParentFile());
                scrollToCurrentTrack();
            } else { // TODO: 30.06.2017 default dir
                updateUI(Environment.getExternalStorageDirectory());
            }
        }
        mIsPlaylist = !mIsPlaylist;
        Preferences.setPlaylist(getContext(), mIsPlaylist);
        setPlaylistBtnIcon();
    }

    @Override
    protected void onSwapModeFinished() {
        if (mIsPlaylist) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PlaylistHolder.getInstance(getContext()).updateItemsPositions(mTracksAdapter.getAllItems());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void showPlaylist() {
        mTracksAdapter.setITEMs(PlaylistHolder.getInstance(getContext()).getItems());
        mTracksAdapter.notifyDataSetChanged();
    }


    @Override
    void onActionModeCreate(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_actions_card_fragment, menu);
        if (mIsPlaylist) {
            menu.findItem(R.id.menu_item_delete_file).setVisible(false);
            menu.findItem(R.id.menu_item_playlist_add).setVisible(false);
        } else {
            menu.findItem(R.id.menu_item_playlist_remove).setVisible(false);
        }
    }

    @Override
    void onActionMenuClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_select_all:
                mTracksAdapter.selectAll();
                break;
            case R.id.menu_item_playlist_add:
                PlaylistHolder.getInstance(getContext())
                        .addTracks(mTracksAdapter.getSelectedItems());
                finishActionMode();
                break;
            case R.id.menu_item_delete_file:
                showDeleteFilesDialog();
                break;
            case R.id.menu_item_playlist_remove:
                List<TrackItem> selectedItems = mTracksAdapter.getSelectedItems();
                PlaylistHolder.getInstance(getContext()).deleteItems(selectedItems);
                finishActionMode();
                showPlaylist();
                break;
        }
    }


    private void showDeleteFilesDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_delete_files_title)
                .setMessage(R.string.dialog_delete_files_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkSdPermission(DELETE_FILES);
                    }
                })
                .create().show();
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
