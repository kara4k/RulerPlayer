package com.kara4k.rulerplayer;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class FolderSelectorDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String PARENT = "parent";
    public static final String PATH = "path";

    private RecyclerView mRecyclerView;
    private File mSelectedDir;
    private FolderSelectorDialogFragment mDialog;

    public static FolderSelectorDialogFragment newInstance(String parent) {
        Bundle args = new Bundle();
        args.putString(PARENT, parent);
        FolderSelectorDialogFragment fragment = new FolderSelectorDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = this;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, null);

        String parent = SettingsFragment.UNDEFINED;
        if (getArguments() != null) {
            parent = getArguments().getString(PARENT, SettingsFragment.UNDEFINED);
        }

        File file = new File(parent);

        if (parent.equals(SettingsFragment.UNDEFINED) || !file.exists()) {
            file = Environment.getExternalStorageDirectory();
        }

        Adapter adapter = new Adapter(CardTracksHolder.getFolders(file));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setMinimumHeight(2000);

        mSelectedDir = file;

        return new AlertDialog.Builder(getActivity())
                .setView(mRecyclerView)
                .setTitle(file.getPath())
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent();
        intent.putExtra(PATH, mSelectedDir.getPath());
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }

    class Adapter extends RecyclerView.Adapter<FolderHolder> {

        final ArrayList<File> mFiles;

        public Adapter(ArrayList<File> files) {
            mFiles = files;
        }

        @Override
        public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.folder_item, null);
            return new FolderHolder(view);
        }

        @Override
        public void onBindViewHolder(FolderHolder holder, int position) {
            holder.bindHolder(mFiles.get(position));
        }

        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }

    class FolderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private File mFile;
        private final TextView mNameTextView;
        private final ImageView mFolderIconImageView;

        public FolderHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.folder_name_text_view);
            mFolderIconImageView = (ImageView) itemView.findViewById(R.id.folder_icon_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindHolder(File file) {
            mFile = file;
            mNameTextView.setText(file.getName());
            if (file.getName().equals("...")) {
                mFolderIconImageView.setVisibility(View.GONE);
            } else {
                mFolderIconImageView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (mFile.getName().equals("...")) {
                if (isDownloadInternalParent()) return;
                if (hasNoParent()) return;
                mSelectedDir = mSelectedDir.getParentFile();
                mRecyclerView.setAdapter(new Adapter(CardTracksHolder.getFolders(mSelectedDir)));
            } else {
                mSelectedDir = mFile;
                mRecyclerView.setAdapter(new Adapter(CardTracksHolder.getFolders(mFile)));
            }
            mDialog.getDialog().setTitle(mSelectedDir.getPath());
        }

        private boolean hasNoParent() {
            return mSelectedDir.getParentFile() == null;
        }

        private boolean isDownloadInternalParent() {
            if (getTargetRequestCode() == SettingsFragment.REQUEST_DOWNLOAD_DIR) {
                if (mSelectedDir.equals(Environment.getExternalStorageDirectory())) {
                    return true;
                }
            }
            return false;
        }
    }
}
