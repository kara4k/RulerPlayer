package com.kara4k.moozic;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class MusicService extends Service {

    public static Intent newIntent(Context context){
        return new Intent(context, MusicService.class);
    }

    private PowerManager.WakeLock mWakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TrackItem currentTrack = Preferences.getCurrentTrack(getApplicationContext());
        startForeground(NotificationManager.NOTIFICATION_ID
                , new NotificationManager(getApplicationContext())
                        .getNotification(null, currentTrack));
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "WakelockTag");
        mWakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
        NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
        nm.cancel(NotificationManager.NOTIFICATION_ID);
        Log.e("MusicService", "onDestroy: " + "finished");
    }
}
