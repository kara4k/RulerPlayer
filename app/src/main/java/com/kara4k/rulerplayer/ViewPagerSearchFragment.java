package com.kara4k.rulerplayer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerSearchFragment extends ViewPagerFragment {


    public static ViewPagerSearchFragment newInstance() {
        Bundle args = new Bundle();
        ViewPagerSearchFragment fragment = new ViewPagerSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    FragmentStatePagerAdapter getAdapter() {
        return new Adapter(getChildFragmentManager()) {
            @Override
            Fragment getFirstFragment() {
                return SearchFragment.newInstance();
            }
        };
    }
}
