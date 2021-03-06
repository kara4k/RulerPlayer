package com.kara4k.rulerplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;

public class RulerPlayerActivity extends DrawerActivity implements CardFragment.CardCallbacks, MusicFragment.ActionModeListener {

    private ActivityCallback mActivityCallback;
    private Player mPlayer;
    private ActionsReceiver mActionsReceiver;

    interface ActivityCallback {
        void onBackPressed();

        void onMenuPressed();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayer = Player.getInstance(this);
        startService(DestroyService.newIntent(this));
        mActionsReceiver = new ActionsReceiver();
        registerReceiver(mActionsReceiver,
                new IntentFilter(NotificationManager.NOTIFICATION_ACTIONS));
        mNavigationView.getMenu().getItem(0).setChecked(true);
//        new RateManager(this).checkForRateOffer();
        startFirebase();
        startDozeService();
        new VersionChecker(this).checkVersion();
    }

    private void startDozeService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startService(MusicService.newIntent(this));
        }
    }

    private void startFirebase() {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        if (getIntent() != null) {
            Preferences.setPlaylist(this, false);
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                File file = new File(getIntent().getData().getPath());
                if (file.exists()) {
                    TrackItem trackItem = new TrackItem();
                    CardTracksHolder.fillTrackData(trackItem, file);
                    Tools.setTrackInfo(trackItem);
                    Preferences.setCurrentTrack(this, trackItem);
                    replaceFragment(ViewPagerCardFragment.newInstance());
                    mNavigationView.getMenu().getItem(0).setChecked(true);
                    onPlay(trackItem);
                }
            }
            setIntent(null);
        }
        super.onStart();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    @Override
    protected Fragment getFirstFragment() {
        return ViewPagerCardFragment.newInstance();
    }

    @Override
    void onNavigationItemPressed(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_item_card_fragment:
                replaceFragment(ViewPagerCardFragment.newInstance());
                break;
            case R.id.navigation_item_search_fragment:
                replaceFragment(ViewPagerSearchFragment.newInstance());
                break;
            case R.id.navigation_item_ost:
                replaceFragment(ViewPagerOstFragment.newInstance());
                break;
            case R.id.navigation_item_radio:
                replaceFragment(ViewPagerRadioFragment.newInstance());
                break;
            case R.id.navigation_menu_settings:
                startActivity(SettingsActivity.newIntent(this));
                break;
//            case R.id.navigation_menu_rate_item:
//                new RateManager(this).rateApp();
//                break;
            case R.id.navigation_menu_github_item:
                new RateManager(this).navigateGitHub();
                break;
            case R.id.navigation_menu_share_item:
                new RateManager(this).shareApp();
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
        mPlayer.release();
        stopService(DestroyService.newIntent(this));
        unregisterReceiver(mActionsReceiver);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stopService(MusicService.newIntent(this));
        }
        NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
        nm.cancel(NotificationManager.NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override
    public void onActionModeStart() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        setViewPagerLock(true);
    }

    @Override
    public void onActionModeFinish() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
        setViewPagerLock(false);
    }

    private void setViewPagerLock(boolean lock) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            return;
        }
        if (fragment instanceof ViewPagerFragment) {
            ViewPagerFragment viewPagerFragment = (ViewPagerFragment) fragment;
            viewPagerFragment.setLocked(lock);
        }
    }


    public Player getPlayer() {
        return mPlayer;
    }

    public void setActivityCallback(ActivityCallback activityCallback) {
        mActivityCallback = activityCallback;
    }

    public void setPlayerListCallback(Player.PlayerListCallback playerListCallback) {
        if (mPlayer == null) {
            mPlayer = Player.getInstance(this);
        }
        mPlayer.setPlayerListCallback(playerListCallback);
    }

    public class ActionsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(NotificationManager.ACTION, -1);
            switch (action) {
                case NotificationManager.ACTION_PLAY:
                    mPlayer.togglePlayPause();
                    break;
                case NotificationManager.ACTION_PREV:
                    mPlayer.playPrev();
                    break;
                case NotificationManager.ACTION_NEXT:
                    mPlayer.playNext();
                    break;
            }

        }
    }


    public static Intent newIntent(Context context) {
        return new Intent(context, RulerPlayerActivity.class);
    }
}
