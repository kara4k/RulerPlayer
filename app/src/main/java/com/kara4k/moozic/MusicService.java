package com.kara4k.moozic;


import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class MusicService extends Service {

    private PowerManager.WakeLock mWakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MusicService", "onCreate: " + "hh");
        startForeground(1, new Notification()); // TODO: 28.06.2017
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "WakelockTag");
        mWakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MusicService", "onStartCommand: " + "kk");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
        NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
        nm.cancel(1);
        Log.e("MusicService", "onDestroy: " + "finished");
    }
}
