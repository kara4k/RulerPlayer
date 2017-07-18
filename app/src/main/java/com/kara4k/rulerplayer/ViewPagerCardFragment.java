package com.kara4k.rulerplayer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerCardFragment extends ViewPagerFragment {


    public static ViewPagerCardFragment newInstance() {
        Bundle args = new Bundle();
        ViewPagerCardFragment fragment = new ViewPagerCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    FragmentStatePagerAdapter getAdapter() {
       return new Adapter(getChildFragmentManager()){
           @Override
           protected Fragment getFirstFragment() {
               return CardFragment.newInstance();
           }
       };
    }

}
