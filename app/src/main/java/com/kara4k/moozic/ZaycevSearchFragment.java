package com.kara4k.moozic;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZaycevSearchFragment extends MusicFragment {


    private String mQuery;
    private int mPage = 1;
    private boolean mHasMore = true;


    public static ZaycevSearchFragment newInstance() {
        Bundle args = new Bundle();
        ZaycevSearchFragment fragment = new ZaycevSearchFragment();
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
                        Log.e("ZaycevSearchFragment", "onScrolled: " + mHasMore);
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
                return ZaycevFetchr.searchTracks(params[0], mPage);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<TrackItem> trackItems) {

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
        }
    }
}
