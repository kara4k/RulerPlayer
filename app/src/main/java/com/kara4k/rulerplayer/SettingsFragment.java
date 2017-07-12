package com.kara4k.rulerplayer;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final String VERSION_KEY = "version";
    public static final String FOLDER_HOME = "home_folder";
    public static final String FOLDER_DOWNLOADS = "download_folder";
    public static final String WIFI_ONLY = "wifi_only";

    public static final String UNDEFINED = "-1";
    private static final int REQUEST_HOME_DIR = 1;
    private static final int REQUEST_DOWNLOAD_DIR = 2;
    private Preference mHomeFolderPref;
    private Preference mDownloadFolderPref;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setVersionInfo();

        mHomeFolderPref = findPreference(FOLDER_HOME);
        mHomeFolderPref.setOnPreferenceClickListener(this);
        setHomeFolderPrefSummary();

        mDownloadFolderPref = findPreference(FOLDER_DOWNLOADS);
        mDownloadFolderPref.setOnPreferenceClickListener(this);
        setDownloadFolderPrefSummary();
    }

    private void setHomeFolderPrefSummary() {
        String homeFolder = Preferences.getHomeFolder(getActivity());
        if (homeFolder.equals(UNDEFINED)) {
            mHomeFolderPref.setSummary(R.string.pref_folder_undefined);
        } else {
            mHomeFolderPref.setSummary(homeFolder);
        }
    }

    private void setDownloadFolderPrefSummary() {
        String downloadFolder = Preferences.getDownloadFolder(getActivity());
        if (downloadFolder.equals(UNDEFINED)) {
            mDownloadFolderPref.setSummary(R.string.pref_folder_undefined);
        } else {
            mDownloadFolderPref.setSummary(downloadFolder);
        }
    }



    private void setVersionInfo() {
        Preference versionPref = findPreference(VERSION_KEY);
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            versionPref.setSummary(String.format("%s (%d)", versionName, versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(FOLDER_HOME)) {
            checkSdPermission(REQUEST_HOME_DIR);
        } else if (preference.getKey().equals(FOLDER_DOWNLOADS)) {
            checkSdPermission(REQUEST_DOWNLOAD_DIR);
        }
        return false;
    }

    private void checkSdPermission(int request) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast toast = Toast.makeText(getActivity(),
                    R.string.toast_sd_card_not_found, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        boolean hasSDPermissions = isHasSDPermissions();
        if (hasSDPermissions) {
            onSdCardPermissionGranted(request);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, request);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast toast = Toast.makeText(getActivity(),
                    R.string.snackbar_sd_card_access, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            onSdCardPermissionGranted(requestCode);
        }
    }


    private boolean isHasSDPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadSDPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasReadSDPermission == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void onSdCardPermissionGranted(int requestCode) {
        showFoldersDialog(requestCode);
    }

    private void showFoldersDialog(int requestCode) {
        String folder = getCurrentFolder(requestCode);
        FolderSelectorDialogFragment dialog
                = FolderSelectorDialogFragment.newInstance(folder);
        dialog.setTargetFragment(this, requestCode);
        dialog.show(getFragmentManager(), "dialog");

    }

    private String getCurrentFolder(int requestCode) {
        String currentFolder = UNDEFINED;
        switch (requestCode) {
            case REQUEST_HOME_DIR:
                currentFolder = Preferences.getHomeFolder(getActivity());
                break;
            case REQUEST_DOWNLOAD_DIR:
                currentFolder = Preferences.getDownloadFolder(getActivity());
                break;
        }
        return currentFolder;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra(FolderSelectorDialogFragment.PATH);
            switch (requestCode) {
                case REQUEST_HOME_DIR:
                    Preferences.setHomeFolder(getActivity(), path);
                    mHomeFolderPref.setSummary(path);
                    break;
                case REQUEST_DOWNLOAD_DIR:
                    Preferences.setDownloadFolder(getActivity(), path);
                    mDownloadFolderPref.setSummary(path);
                    break;
            }
        }
    }
}
