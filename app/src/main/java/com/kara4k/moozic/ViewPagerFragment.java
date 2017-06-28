package com.kara4k.moozic;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewPagerFragment<ADAPTER extends FragmentStatePagerAdapter> extends Fragment {

    private LockableViewPager mLockableViewPager;

    abstract ADAPTER getAdapter();

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
        mLockableViewPager.setAdapter(getAdapter());
        return view;
    }

    public void setLocked(boolean lock){
        mLockableViewPager.setSwipeLocked(lock);
    }
}
