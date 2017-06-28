package com.kara4k.moozic;


import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;

public class SearchFragment extends MusicFragment {


    private String mQuery;
    private int mPage = 1;
    private boolean mHasMore = true;


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
                if (dy > 0) {
                    int itemCount = mLayoutManager.getItemCount();
                    int lastCompletelyVisible = mLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastCompletelyVisible == itemCount - 1) {
                        Log.e("SearchFragment", "onScrolled: " + mHasMore);
                        mPage++;
                        if (mHasMore) {
                            new TracksFetchr().execute(mQuery);
                        }
                    }
                }
            }
        });

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
        mQuery = text;
        mPage = 1;
        mHasMore = true;
        new TracksFetchr().execute(text);
        mSearchView.clearFocus();
        return true;
    }

    @Override
    protected void onBottomBarCreated(Menu menu) { // TODO: 28.06.2017
        MenuItem lastBtn = menu.findItem(R.id.last_btn);
        lastBtn.setTitle("Categories");
        lastBtn.setIcon(R.drawable.ic_list_white_24dp);
    }

    @Override
    void onSdCardPermissionGranted(int requestCode) {

    }


    @Override
    void lastButtonPressed() {

    }

    @Override
    void onActionModeCreate(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
    }

    @Override
    void onActionMenuClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play_btn:
                DownloadManager dm = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);
                List<TrackItem> selectedItems = mTracksAdapter.getSelectedItems(); // TODO: 25.06.2017 db + permiss + settings
                for (int i = 0; i < selectedItems.size(); i++) {
                    TrackItem trackItem = selectedItems.get(i);
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(trackItem.getFilePath()))
                            .setMimeType("audio/MP3")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setTitle(String.format("%s - %s.mp3", trackItem.getTrackArtist(), trackItem.getTrackName()))
                            .setDescription("descript")
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                    String.format("%s - %s.mp3", trackItem.getTrackArtist(), trackItem.getTrackName()));
                    ;
                    long enqueue = dm.enqueue(request);
                }
                break;

            case R.id.pause_btn:
                PlaylistHolder.getInstance(getContext()).addTracks(mTracksAdapter.getSelectedItems());
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
                    mProgressDialog.show();
                }
            });

        }

        @Override
        protected List<TrackItem> doInBackground(String... params) {
            try {
                return ZaycevFetchr.searchTracks(params[0], mPage);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<TrackItem> trackItems) {

            try {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.hide();
                    mProgressDialog = null;
                }

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
    }
}
