package com.kara4k.moozic;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerRadioFragment extends ViewPagerFragment {

    public static ViewPagerRadioFragment newInstance() {
        Bundle args = new Bundle();
        ViewPagerRadioFragment fragment = new ViewPagerRadioFragment();
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
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RadioFragment.newInstance();
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
