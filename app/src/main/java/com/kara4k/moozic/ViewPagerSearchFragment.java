package com.kara4k.moozic;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewPagerSearchFragment extends Fragment {

    private LockableViewPager mLockableViewPager;

    public static ViewPagerSearchFragment newInstance() {
        Bundle args = new Bundle();
        ViewPagerSearchFragment fragment = new ViewPagerSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        mLockableViewPager = (LockableViewPager) view.findViewById(R.id.view_pager);
        mLockableViewPager.setAdapter(new Adapter(getChildFragmentManager()));
        return view;
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
