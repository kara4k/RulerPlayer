package com.kara4k.rulerplayer;


import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;

public class SearchFragment extends MusicFragment {

    private static final int DOWNLOAD_TRACK = 1;


    private String mQuery;
    private int mPage = 1;
    private boolean mIsSearch;
    private boolean mHasMore = true;
    private boolean mIsIconify = false;

    private ProgressDialog mProgressDialog;
    private ZaycevTracksParser mZaycevTracksParser;

    public static SearchFragment newInstance() {
        Bundle args = new Bundle();
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Handler handler = new Handler();
        mZaycevTracksParser = new ZaycevTracksParser(getContext(), new ZaycevTracksParser.TrackParser() {
            @Override
            public void onTracksReceived(List<TrackItem> list) {
                if (list.size() < 20) {
                    mHasMore = false;
                }
                destroyDialog();
                if (mPage == 1) {
                    mTracksAdapter.setITEMs(list);
                } else {
                    mTracksAdapter.appendItems(list);
                }
                mTracksAdapter.notifyDataSetChanged();
            }
        });
        mZaycevTracksParser.setFragmentHandler(handler);
        mZaycevTracksParser.start();
        mZaycevTracksParser.getLooper();
    }

    @Override
    void onCreateView() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                onRecyclerViewScrolled(dy);
            }
        });

    }

    protected void onRecyclerViewScrolled(int dy) {
        if (dy > 0) {
            int itemCount = mLayoutManager.getItemCount();
            int lastCompletelyVisible = mLayoutManager.findLastCompletelyVisibleItemPosition();
            if (lastCompletelyVisible == itemCount - 1) {
                mPage++;
                if (mIsSearch) {
                    if (mHasMore) {
                        showDialog();
                        mZaycevTracksParser.getTracks(mQuery, mPage);
                    }
                } else {
                    showDialog();
                    mZaycevTracksParser.getTracks(mPage);
                }
            }
        }
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
        if (mCurrentTrack == null) return;
        mCardCallbacks.onPlayPressed(mCurrentTrack);
        int currentIndex = mTracksAdapter.getCurrentIndex();
        if (currentIndex == -1) return;
        mLayoutManager.scrollToPosition(currentIndex);
    }

    @Override
    void onBackPressed() {

    }

    @Override
    boolean onQuerySearchSubmit(String text) {
        mIsSearch = true;
        mQuery = text;
        mPage = 1;
        mHasMore = true;
        mSearchView.clearFocus();
        showDialog();
        mZaycevTracksParser.getTracks(text, mPage);
        return true;
    }

    @Override
    void onOptionsMenuCreate(Menu menu) {
        MenuItem lastBtn = menu.findItem(R.id.changeable_btn);
        lastBtn.setTitle(R.string.last_btn_title_search_fragment);
        lastBtn.setIcon(R.drawable.ic_format_list_numbered_white_24dp);

        mSearchView.setIconified(mIsIconify);
        mIsIconify = true;
    }

    @Override
    void onSdCardPermissionGranted(int requestCode) {
        switch (requestCode) {
            case DOWNLOAD_TRACK:
                downloadTracks();
                finishActionMode();
                break;
        }

    }

    private void downloadTracks() {
        DownloadManager dm = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);
        List<TrackItem> selectedItems = mTracksAdapter.getSelectedItems();
        for (int i = 0; i < selectedItems.size(); i++) {
            TrackItem trackItem = selectedItems.get(i);
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(trackItem.getFilePath()))
                    .setMimeType("audio/mp3")
                    .setAllowedOverRoaming(false)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setTitle(String.format("%s - %s.mp3", trackItem.getTrackArtist(), trackItem.getTrackName()));
            setNetworks(request);
            setDownloadPath(trackItem, request);
            try {
                dm.enqueue(request);
            } catch (SecurityException e) {
                e.printStackTrace();
                showSingleToast(i);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        String.format("%s - %s.mp3", trackItem.getTrackArtist(), trackItem.getTrackName()));
                dm.enqueue(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showSingleToast(int i) {
        if (i == 0) {
            Toast toast = Toast.makeText(getActivity(),
                    R.string.toast_save_to_default_downloads, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void setDownloadPath(TrackItem trackItem, DownloadManager.Request request) {
        String downloadDirPath = Preferences.getDownloadFolder(getContext());
        File downloadDir = new File(downloadDirPath);
        if (downloadDir.exists()) {
            File file = new File(String.format("%s/%s - %s.mp3", downloadDirPath,
                    trackItem.getTrackArtist(), trackItem.getTrackName()));
            request.setDestinationUri(Uri.fromFile(file));
        } else {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    String.format("%s - %s.mp3", trackItem.getTrackArtist(), trackItem.getTrackName()));
        }
    }

    private void setNetworks(DownloadManager.Request request) {
        boolean isWifiOnly = Preferences.isDownloadWifiOnly(getContext());
        if (isWifiOnly) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        } else {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        }
    }


    @Override
    void lastButtonPressed() {
        mHasMore = true;
        mIsSearch = false;
        mPage = 1;
        showDialog();
        mZaycevTracksParser.getTracks(mPage);
    }

    @Override
    void onActionModeCreate(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_actions_search_fragment, menu);
    }

    @Override
    void onActionMenuClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_select_all:
                mTracksAdapter.selectAll();
                break;
            case R.id.menu_item_download_tracks:
                checkSdPermission(DOWNLOAD_TRACK);
                break;
        }
    }

    private void showDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mProgressDialog = new ProgressDialog(getContext());
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setMessage("Loading");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                            getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    destroyDialog();
                                }
                            });
                    mProgressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void destroyDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.hide();
                        mProgressDialog = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mZaycevTracksParser.stopMessages();
    }

}
