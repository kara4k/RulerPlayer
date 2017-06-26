package com.kara4k.moozic;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Preferences {

    public static final String CURRENT_TRACK = "current_track";
    public static final String HAS_NO_CURRENT = "has_no_current";

    public static final String REPEAT_ONE = "repeat_one";

    public static final String SORT_ORDER = "sort_order";


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

//    public static TrackItem getCurrentTrack(Context context) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        String filePath = sp.getString(CURRENT_TRACK, HAS_NO_CURRENT);
//        if (filePath.equals(HAS_NO_CURRENT)) {
//            return null;
//        }
//
//        File file = new File(filePath);
//        if (file.exists()){
//            TrackItem trackItem = new TrackItem();
//            CardTracksHolder.fillTrackData(trackItem,file);
//            Tools.setTrackInfo(trackItem);
//            return trackItem;
//        }
//        return null;
//    }
//
//    public static void setCurrentTrack(Context context, TrackItem trackItem) {
//        if (trackItem.getFile() == null) {
//            return;
//        }
//
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        sp.edit().putString(CURRENT_TRACK, trackItem.getFile().getPath()).apply();
//    }

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


}
