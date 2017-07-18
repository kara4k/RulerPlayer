package com.kara4k.rulerplayer;


import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;

public class SearchFragment extends MusicFragment {

    private static final int DOWNLOAD_TRACK = 1;


    private String mQuery;
    private int mPage = 1;
    private boolean mIsSearch;
    private boolean mHasMore = true;
    private boolean mIsIconify = false;


    public static SearchFragment newInstance() {
        Bundle args = new Bundle();
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
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
                        new TracksFetchr().execute(mQuery);
                    }
                } else {
                    new TracksFetchr().execute();
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
        new TracksFetchr().execute(text);
        mSearchView.clearFocus();
        return true;
    }

    @Override
    protected void onCreateOptionsMenu() {
        mSearchView.setIconified(mIsIconify);
        mIsIconify = true;
    }


    @Override
    void onBottomBarCreated(Menu menu) {
        MenuItem lastBtn = menu.findItem(R.id.last_btn);
        lastBtn.setTitle(R.string.last_btn_title_search_fragment);
        lastBtn.setIcon(R.drawable.ic_format_list_numbered_white_24dp);
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
            dm.enqueue(request);
        }
    }

    private void setDownloadPath(TrackItem trackItem, DownloadManager.Request request) {
        String downloadDirPath = Preferences.getDownloadFolder(getContext());
        File downloadDir = new File(downloadDirPath);
        if (downloadDir.exists()) {
            File file = new File(String.format("%s/%s - %s.mp3", downloadDirPath, trackItem.getTrackArtist(), trackItem.getTrackName()));
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
        new TracksFetchr().execute();
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


    class TracksFetchr extends AsyncTask<String, Void, List<TrackItem>> {

        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = new ProgressDialog(getContext());
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setMessage("Loading");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                }
            });

        }

        @Override
        protected List<TrackItem> doInBackground(String... params) {
            try {
                if (params.length == 0) {
                    return ZaycevFetchr.getTracks(mPage);
                }
                return ZaycevFetchr.getTracks(params[0], mPage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<TrackItem> trackItems) {

            try {
                destroyDialog();

                if (trackItems == null) {
                    trackItems = new ArrayList<>();
                }

                if (trackItems.size() < 20) mHasMore = false;

                if (mPage == 1) {
                    mTracksAdapter.setITEMs(trackItems);
                } else {
                    mTracksAdapter.appendItems(trackItems);
                }
                mTracksAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void destroyDialog() {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.hide();
                mProgressDialog = null;
            }
        }

    }
}
