package com.kara4k.moozic;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
                    return SearchFragment.newInstance();
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
