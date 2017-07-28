package com.kara4k.rulerplayer;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.Menu;

import java.util.ArrayList;
import java.util.List;

public class OstFragment extends SearchFragment {


    private ArrayList<TrackItem> mMovies;
    private boolean mIsMoviesList;

    public static OstFragment newInstance() {
        Bundle args = new Bundle();
        OstFragment fragment = new OstFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovies = new ArrayList<>();
    }

    @Override
    void onTrackHolderClick(TrackItem trackItem, int newPosition) {
        if (trackItem instanceof MovieItem) {
            new TracksFetchr().execute(trackItem.getFilePath());
            mIsMoviesList = false;
        } else {
            playTrack(trackItem, newPosition);
        }
    }

    @Override
    protected void onRecyclerViewScrolled(int dy) {

    }

    @Override
    void onBackPressed() {
        if (!mIsMoviesList) {
            updateUI(mMovies);
        }
    }

    @Override
    void onOptionsMenuCreate(Menu menu) {
        super.onOptionsMenuCreate(menu);
        mSearchView.setQueryHint(getString(R.string.ost_search_view_hint));
    }

    @Override
    boolean onQuerySearchSubmit(String text) {
        new FilmsFetchr().execute(text);
        mSearchView.clearFocus();
        mIsMoviesList = true;
        return true;
    }


    @Override
    void lastButtonPressed() {
        new FilmsFetchr().execute();
    }

    private void updateUI(List<TrackItem> trackItems) {
        mTracksAdapter.setITEMs(trackItems);
        mTracksAdapter.notifyDataSetChanged();
    }


    class FilmsFetchr extends DialogAsyncTask {

        @Override
        protected List<TrackItem> doInBackground(String... params) {
            try {
                if (params.length == 0) {
                    return KinomaniaFetchr.getPopularMovies();
                } else {
                    return KinomaniaFetchr.getMovies(params[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<TrackItem> trackItems) {
            super.onPostExecute(trackItems);
            mMovies.clear();
            mMovies.addAll(trackItems);
            updateUI(trackItems);
        }
    }

    class TracksFetchr extends DialogAsyncTask {
        @Override
        protected List<TrackItem> doInBackground(String... params) {
            try {
                return KinomaniaFetchr.getTracks(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<TrackItem> trackItems) {
            super.onPostExecute(trackItems);
            updateUI(trackItems);
        }
    }

    abstract class DialogAsyncTask extends AsyncTask<String, Void, List<TrackItem>> {

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
                    mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                            getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    destroyDialog();
                                }
                            });
                    mProgressDialog.show();
                }
            });

        }

        @CallSuper
        @Override
        protected void onPostExecute(List<TrackItem> trackItems) {
            try {
                destroyDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void destroyDialog() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.hide();
                        mProgressDialog = null;
                    }
                }
            });

        }
    }


}
