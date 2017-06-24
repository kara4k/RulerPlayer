package com.kara4k.moozic;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.io.IOException;

public class ZaycevFragment extends Fragment {

    public static ZaycevFragment newInstance() {
        Bundle args = new Bundle();
        ZaycevFragment fragment = new ZaycevFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ZaycevFetchr.dodo("30 seconds to mars");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
