package com.kara4k.moozic;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public abstract class MusicFragment<ADAPTER extends SelectableAdapter, ITEM extends SearchableItem>
        extends Fragment {

    protected ActionMode mActionMode;
    protected ActionMode.Callback mModeCallback;
    private ActionModeListener mModeListener;

    public interface ActionModeListener {
        void onActionModeStart();

        void onActionModeFinish();
    }

    abstract ADAPTER getADAPTER();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mModeCallback = getActionModeCallback();
        mModeListener = (ActionModeListener) getActivity();
    }

    private ActionMode.Callback getActionModeCallback() {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mModeListener.onActionModeStart();
                mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                getADAPTER().refreshItems();
                mActionMode = null;
                mModeListener.onActionModeFinish();
            }
        };
    }


}
