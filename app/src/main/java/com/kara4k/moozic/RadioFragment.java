package com.kara4k.moozic;


import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class RadioFragment extends MusicFragment {


    public static RadioFragment newInstance() {

        Bundle args = new Bundle();

        RadioFragment fragment = new RadioFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void onCreateView() {
        TrackItem trackItem = new TrackItem();
        trackItem.setTrackName("NOVOE RADIO");
        trackItem.setTrackArtist("256k");
        trackItem.setFilePath("http://live.novoeradio.by:8000/novoeradio-256k");
        trackItem.setDurationMs(0);
        trackItem.isTrack();
        trackItem.setDuration("");
        trackItem.setExtension("");
        trackItem.setBitrate("");
        trackItem.setOnline(true);
        trackItem.setHasInfo(true);
        trackItem.setTrack(true);
        trackItem.setRadio(true);
        ArrayList<TrackItem> trackItems = new ArrayList<>();
        trackItems.add(trackItem);
        TrackItem trackItem2 = new TrackItem();
        trackItem2.setTrackName("ZAYCEV_FM");
        trackItem2.setTrackArtist("Example: 256k");
        trackItem2.setFilePath("https://zaycevfm.cdnvideo.ru/ZaycevFM_pop_256.mp3");
        trackItem2.setDurationMs(0);
        trackItem2.setDuration("");
        trackItem2.setExtension("");
        trackItem2.setBitrate("");
        trackItem2.isTrack();
        trackItem2.setOnline(true);
        trackItem2.setHasInfo(true);
        trackItem2.setTrack(true);
        trackItem2.setRadio(true);
        trackItems.add(trackItem2);
//        trackItems.add(trackItem);
        mTracksAdapter.setITEMs(trackItems);
        mTracksAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mTracksAdapter);
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

    }

    @Override
    void onPlayBtnPressed() {

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
        lastBtn.setTitle("Add radio");
        lastBtn.setIcon(R.drawable.ic_add_white_24dp);
    }

    @Override
    void lastButtonPressed() {

    }

    @Override
    void onActionModeCreate(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
    }

    @Override
    void onActionMenuClicked(ActionMode mode, MenuItem item) {

    }
}
