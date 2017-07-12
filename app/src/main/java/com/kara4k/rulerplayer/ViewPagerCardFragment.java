package com.kara4k.rulerplayer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
        return new Adapter(getChildFragmentManager());
    }


    class Adapter extends FragmentStatePagerAdapter {


        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return CardFragment.newInstance();
                case 1:
                    return SinglePlayerFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

}
