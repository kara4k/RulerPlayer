package com.kara4k.rulerplayer;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

class RateManager implements DialogInterface.OnClickListener {


    private static final String MARKET_URL = "http://play.google.com/store/apps/details?id=";
    private static final String RATE_PREFS = "rate_preferences";
    private static final String LAUNCH_COUNT_KEY = "launch_count";
    private static final String SHOW_DIALOG_KEY = "is_show_dialog";
    private static final int LAUNCHES_TO_SHOW = 7;


    private final Context mContext;
    private final SharedPreferences mSharedPreferences;
    private boolean mIsShowDialog;
    private int mLaunchCount;


    public RateManager(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(RATE_PREFS, Context.MODE_PRIVATE);
        setupValues();
    }

    private void setupValues() {
        mLaunchCount = getLaunchCount();
        mIsShowDialog = isShowDialog();
    }

    private int getLaunchCount() {
        return mSharedPreferences.getInt(LAUNCH_COUNT_KEY, 1);
    }

    private void setLaunchCount(int count) {
        mSharedPreferences.edit().putInt(LAUNCH_COUNT_KEY, count).apply();
    }

    private void setIsShowDialog(boolean isShow) {
        mSharedPreferences.edit().putBoolean(SHOW_DIALOG_KEY, isShow).apply();
    }

    private boolean isShowDialog() {
        return mSharedPreferences.getBoolean(SHOW_DIALOG_KEY, true);
    }

    public void checkForRateOffer() {
        if (!mIsShowDialog) {
            return;
        }

        if (mLaunchCount >= LAUNCHES_TO_SHOW) {
            showRateOfferDialog();
        } else {
            setLaunchCount(++mLaunchCount);
        }

    }

    private void showRateOfferDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.rate_dialog_title)
                .setMessage(R.string.rate_dialog_message)
                .setPositiveButton(R.string.rate_dialog_positive, this)
                .setNegativeButton(R.string.rate_dialog_negative, this)
                .setNeutralButton(R.string.rate_dialog_neutral, this)
                .create().show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                rateApp();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                setIsShowDialog(false);
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                setLaunchCount(0);
                setIsShowDialog(true);
                break;
        }
    }

    public void rateApp() {
        setIsShowDialog(false);

        Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            mContext.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(MARKET_URL.concat(mContext.getPackageName()))));
        }
    }

        public void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        String extraText = MARKET_URL.concat(mContext.getPackageName());
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.app_name));
        sendIntent.putExtra(Intent.EXTRA_TEXT, extraText);
        mContext.startActivity(sendIntent);
    }
}
