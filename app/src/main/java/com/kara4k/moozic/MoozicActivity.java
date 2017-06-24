package com.kara4k.moozic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.MenuItem;

public class MoozicActivity extends DrawerActivity implements CardFragment.CardCallbacks, MusicFragment.ActionModeListener {

    private ActivityCallback mActivityCallback;
    private Player mPlayer;

    interface ActivityCallback {
        void onBackPressed();

        void onMenuPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayer = new Player(this);
    }

    @Override
    protected Fragment getFirstFragment() {
        return CardViewPagerFragment.newInstance();
    }

    @Override
    void onNavigationItemPressed(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_item_card_fragment:
                replaceFragment(CardViewPagerFragment.newInstance());
                break;
            case R.id.test2:
                replaceFragment(CardFragment.newInstance());
                break;
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (mActivityCallback == null) return super.onKeyUp(keyCode, event);
            mActivityCallback.onMenuPressed();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onPlay(TrackItem trackItem) {
        mPlayer.playTrack(trackItem);
    }

    @Override
    public void onPlayPressed(TrackItem trackItem) {
        mPlayer.play(trackItem);
    }

    @Override
    public void onPausePressed() {
        mPlayer.pause();
    }

    @Override
    public void onStopPressed() {
        mPlayer.stop();
    }

    @Override
    void backIsPressed() {
        if (mActivityCallback != null) {
            mActivityCallback.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }

    @Override
    public void onActionModeStart() {

    }

    @Override
    public void onActionModeFinish() {

    }

    public Player getPlayer(){
        return mPlayer;
    }

    public void setActivityCallback(ActivityCallback activityCallback) {
        mActivityCallback = activityCallback;
    }

    public void setPlayerListCallback(Player.PlayerListCallback playerListCallback) {
        mPlayer.setPlayerListCallback(playerListCallback);
    }
}
