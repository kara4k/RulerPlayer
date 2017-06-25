package com.kara4k.moozic;


import android.content.Context;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardTracksHolder {

    private Context mContext;

    public CardTracksHolder(Context context) {
        mContext = context;
    }

    public List<TrackItem> getTracksInDir(File folder) {
        if (folder == null) {
            return null;
        }

        List<TrackItem> list = new ArrayList<>();
        File[] files = folder.listFiles(getFileFilter());

        if (files == null) {
            return list;
        }

        for (int i = 0; i < files.length; i++) {
            TrackItem trackItem = new TrackItem();
            File file = files[i];
            fillTrackData(trackItem, file);
            list.add(trackItem);
        }
        Collections.sort(list);
        return list;
    }

    public static void fillTrackData(TrackItem trackItem, File file) {
        trackItem.setFile(file);
        trackItem.setFilePath(file.getPath());
        trackItem.setName(file.getName());
        trackItem.setTrack(file.isFile());
        trackItem.setDate(file.lastModified());
        String extension = file.getName()
                .substring(file.getName().lastIndexOf(".") + 1).toUpperCase();
        trackItem.setExtension(extension);
        trackItem.setHasInfo(false);
    }

    private FileFilter getFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory() && !file.isHidden()) {
                    return true;
                } else {
                    Pattern pattern = Pattern.compile(mContext.getString(R.string.fileTypesPattern));
                    Matcher matcher = pattern.matcher(file.getName());
                    return matcher.matches();
                }
            }
        };
    }
}
