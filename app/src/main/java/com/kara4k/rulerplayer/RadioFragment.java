package com.kara4k.rulerplayer;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class RadioFragment extends MusicFragment {

    public static final int ADD_RADIO = 1;
    public static final int UPDATE_RADIO = 2;

    private List<TrackItem> mSearchableItems;

    public static RadioFragment newInstance() {
        Bundle args = new Bundle();
        RadioFragment fragment = new RadioFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchableItems = new ArrayList<>();
    }

    @Override
    void onCreateView() {
        mCurrentTrack = Preferences.getCurrentTrack(getContext());
        updateUI();
    }

    private void updateUI() {
        List<TrackItem> radioList = RadioListHolder.getInstance(getContext()).getItems();
        mTracksAdapter.setITEMs(radioList);
        mTracksAdapter.notifyDataSetChanged();
        mSearchableItems.clear();
        mSearchableItems.addAll(radioList);
    }

    @Override
    void onTrackHolderClick(TrackItem trackItem, int newPosition) {
        playTrack(trackItem, newPosition);
    }

    @Override
    int getCurrentTrackIndex() {
        return mTracksAdapter.getCurrentIndex();
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
    void onPlayBtnPressed() {
        if (mCurrentTrack == null) return;
        mCardCallbacks.onPlayPressed(mCurrentTrack);
        scrollToCurrentTrack();
    }

    @Override
    void onBackPressed() {

    }

    @Override
    boolean onQuerySearchSubmit(String text) {
        return false;
    }

    @Override
    void onSdCardPermissionGranted(int requestCode) {

    }

    @Override
    protected void onBottomBarCreated(Menu menu) {
        MenuItem lastBtn = menu.findItem(R.id.last_btn);
        lastBtn.setTitle(R.string.last_btn_add_radio_title);
        lastBtn.setIcon(R.drawable.ic_add_white_24dp);
    }

    @Override
    void lastButtonPressed() {
        RadioDialogFragment radioDialog = RadioDialogFragment.newInstance();
        radioDialog.setTargetFragment(this, ADD_RADIO);
        radioDialog.show(getFragmentManager(), "radio");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String radioName = data.getStringExtra(RadioDialogFragment.NAME);
            String radioDesc = data.getStringExtra(RadioDialogFragment.DESC);
            String radioPath = data.getStringExtra(RadioDialogFragment.PATH);
            TrackItem radioTrack = createRadioTrack(radioName, radioDesc, radioPath);
            switch (requestCode) {
                case ADD_RADIO:
                    RadioListHolder.getInstance(getContext()).addRadio(radioTrack);
                    updateUI();
                    break;
                case UPDATE_RADIO:
                    int position = data.getIntExtra(RadioDialogFragment.POSITION, -1);
                    if (position == -1) {
                        return;
                    }
                    RadioListHolder.getInstance(getContext()).updateRadio(radioTrack, position);
                    finishActionMode();
                    updateUI();
                    break;
            }
        }

    }

    private TrackItem createRadioTrack(String radioName, String radioDesc, String radioPath) {
        TrackItem trackItem = new TrackItem();
        trackItem.setTrackName(radioName);
        trackItem.setTrackArtist(radioDesc);
        trackItem.setFilePath(radioPath);
        trackItem.setDurationMs(0);
        trackItem.setDuration(" ");
        trackItem.setExtension(" ");
        trackItem.setBitrate(" ");
        trackItem.setOnline(true);
        trackItem.setHasInfo(true);
        trackItem.setTrack(true);
        trackItem.setRadio(true);
        return trackItem;
    }

    @Override
    void onActionModeCreate(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_actions_radio_fragment, menu);
    }

    @Override
    void onActionMenuClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_select_all:
                mTracksAdapter.selectAll();
                break;
            case R.id.menu_item_edit_radio:
                TrackItem trackItem = mTracksAdapter.getSelectedItems().get(0);
                RadioDialogFragment dialogFragment = RadioDialogFragment.newInstance(
                        trackItem.mTrackName,
                        trackItem.getTrackArtist(),
                        trackItem.getFilePath(),
                        mTracksAdapter.getSelectedIndexes().get(0)
                );
                dialogFragment.setTargetFragment(this, UPDATE_RADIO);
                dialogFragment.show(getFragmentManager(), "radio");
                break;
            case R.id.menu_item_delete_radio:
                List<TrackItem> selectedItems = mTracksAdapter.getSelectedItems();
                RadioListHolder.getInstance(getContext()).deleteItems(selectedItems);
                finishActionMode();
                updateUI();
                updatePositions();
                break;
        }
    }

    @Override
    protected void onSwapModeFinished() {
        updatePositions();
    }

    private void updatePositions() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RadioListHolder.getInstance(getContext()).updateItemsPositions(mTracksAdapter.getAllItems());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onSelectionChanged() {
        if (mActionMode != null) {
            if (mActionMode.getTitle().equals("1")) {
                mActionMode.getMenu().findItem(R.id.menu_item_edit_radio).setVisible(true);
            } else {
                mActionMode.getMenu().findItem(R.id.menu_item_edit_radio).setVisible(false);
            }
        }
    }
}
