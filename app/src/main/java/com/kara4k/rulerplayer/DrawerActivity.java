package com.kara4k.rulerplayer;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public abstract class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;

    protected abstract Fragment getFirstFragment();

    abstract void onNavigationItemPressed(MenuItem menuItem);

    abstract void backIsPressed();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setupActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        setFirstFragment();
    }

    private void setupActionBar() {
        try {
            getSupportActionBar().setShowHideAnimationEnabled(true);
            getSupportActionBar().setTitle("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setFirstFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = getFirstFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

    }

    protected Fragment getCurrentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        return fragment;
    }

    void replaceFragment(Fragment newFragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, newFragment).commit();
    }


    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        onNavigationItemPressed(item);
        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            backIsPressed();
        }

    }
}
