package com.kara4k.rulerplayer;


import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;

public class VersionChecker implements Handler.Callback{

    private static final String VERSION_URL = "https://raw.githubusercontent.com/kara4k/RulerPlayer/master/vc.txt";
    private int MESSAGE_UPDATE = 1;

    private Context mContext;
    private Handler mHandler;

    public VersionChecker(Context context) {
        mContext = context;
        mHandler = new Handler(this);
    }

    public void checkVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String lastVersion = ZaycevFetchr.getUrlString(VERSION_URL);
                    String currentVersion = getCurrentVersion();
                    boolean isLastVersion = isLastVersion(currentVersion, lastVersion.trim());
                    if (!isLastVersion) {
                        mHandler.obtainMessage(MESSAGE_UPDATE).sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    private void showVersionDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.dialog_new_version_title)
                .setMessage(R.string.dialog_new_version_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(
                        R.string.dialog_navigate_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new RateManager(mContext).navigateGitHub();
                    }
                }).create().show();
    }

    private String getCurrentVersion() throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        String versionName = packageInfo.versionName;
        int versionCode = packageInfo.versionCode;
        return String.format("%s;%d", versionName, versionCode);
    }

    private boolean isLastVersion(String current, String last) {
        return current.equals(last);
    }

    @Override
    public boolean handleMessage(Message msg) {
        showVersionDialog();
        return false;
    }
}
