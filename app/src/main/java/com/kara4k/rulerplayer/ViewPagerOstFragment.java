package com.kara4k.rulerplayer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerOstFragment extends ViewPagerFragment {

    public static ViewPagerOstFragment newInstance() {
        Bundle args = new Bundle();
        ViewPagerOstFragment fragment = new ViewPagerOstFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    FragmentStatePagerAdapter getAdapter() {
        return new Adapter(getChildFragmentManager()) {
            @Override
            Fragment getFirstFragment() {
                return OstFragment.newInstance();
            }
        };
    }


}
