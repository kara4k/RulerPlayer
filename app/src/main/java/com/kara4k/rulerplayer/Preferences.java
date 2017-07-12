package com.kara4k.rulerplayer;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class Preferences {

    private static final String CURRENT_TRACK = "current_track";
    private static final String REPEAT_ONE = "repeat_one";
    private static final String SORT_ORDER = "sort_order";
    private static final String PLAYLIST = "playlist";
    private static final String SHOW_WARNING_DIALOG = "warning_dialog";




    public static void setCurrentTrack(final Context context, final TrackItem trackItem) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = context.openFileOutput(CURRENT_TRACK, Context.MODE_PRIVATE);
                    ObjectOutputStream os = new ObjectOutputStream(fos);
                    os.writeObject(trackItem);
                    os.close();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static TrackItem getCurrentTrack(Context context) {
        TrackItem trackItem;
        try {
            FileInputStream fis = context.openFileInput(CURRENT_TRACK);
            ObjectInputStream is = new ObjectInputStream(fis);
            trackItem = (TrackItem) is.readObject();
            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            trackItem = null;
        }
        return trackItem;
    }

    public static void setPlaylist(Context context, boolean playlist) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PLAYLIST, playlist).apply();
    }

    public static boolean isPlaylist(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PLAYLIST, false);
    }



    public static void setRepeatOne(Context context, boolean repeatOne) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(REPEAT_ONE, repeatOne).apply();
    }

    public static boolean isRepeatOne(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(REPEAT_ONE, false);
    }

    public static void setSortOrder(Context context, int sortBy) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(SORT_ORDER, sortBy).apply();
    }

    public static int getSortOrder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(SORT_ORDER, CardFragment.SORT_BY_NAME);
    }

    public static String getHomeFolder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(SettingsFragment.FOLDER_HOME, SettingsFragment.UNDEFINED);
    }

    public static void setHomeFolder(Context context, String path) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(SettingsFragment.FOLDER_HOME, path).apply();
    }

    public static String getDownloadFolder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(SettingsFragment.FOLDER_DOWNLOADS, SettingsFragment.UNDEFINED);
    }

    public static void setDownloadFolder(Context context, String path) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(SettingsFragment.FOLDER_DOWNLOADS, path).apply();
    }

    public static boolean isDownloadWifiOnly(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SettingsFragment.WIFI_ONLY, true);
    }

    public static void dontShowDialog(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(SHOW_WARNING_DIALOG, false).apply();
    }

    public static boolean isShowWarningDialog(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SHOW_WARNING_DIALOG, true);
    }


}
