package com.kara4k.rulerplayer;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MusicFragment extends Fragment implements
        Player.PlayerListCallback, SearchView.OnQueryTextListener {


    public static final int SORT_BY_NAME = 1;
    private static final int SORT_BY_ARTIST = 2;
    private static final int SORT_BY_DATE = 3;
    private static final int SORT_BY_TYPE = 4;

    ActionMode mActionMode;
    private ActionMode.Callback mModeCallback;
    private ActionModeListener mModeListener;

    CardFragment.CardCallbacks mCardCallbacks;

    private View mView;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    TracksAdapter mTracksAdapter;
    private ActionMenuView mBottomBar;
    SearchView mSearchView;

    TrackItem mCurrentTrack;

    private boolean mIsViewLoaded = false;
    boolean mIsSwapMode = false;

    public interface ActionModeListener {
        void onActionModeStart();

        void onActionModeFinish();
    }

    interface CardCallbacks {

        void onPlay(TrackItem trackItem);

        void onPlayPressed(TrackItem trackItem);

        void onPausePressed();

        void onStopPressed();

    }

    abstract void onCreateView();

    abstract void onTrackHolderClick(TrackItem trackItem, int newPosition);

    abstract int getCurrentTrackIndex();

    abstract void onQuerySearchChanged(String newText);

    abstract void onPlayBtnPressed();

    abstract void onBackPressed();

    abstract boolean onQuerySearchSubmit(String text);

    abstract void onSdCardPermissionGranted(int requestCode);

    abstract void onOptionsMenuCreate(Menu menu);

    abstract void lastButtonPressed();

    abstract void onActionModeCreate(ActionMode mode, Menu menu);

    abstract void onActionMenuClicked(MenuItem item);


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mModeCallback = getActionModeCallback();
        mModeListener = (ActionModeListener) getActivity();
        mCardCallbacks = (CardFragment.CardCallbacks) getActivity();
        setActivityCallback();
        showActionBar();
    }

    private void showActionBar() {
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            ActionBar supportActionBar = activity.getSupportActionBar();
            if (supportActionBar != null && !supportActionBar.isShowing()) {
                supportActionBar.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActivityCallback() {
        RulerPlayerActivity rulerPlayerActivity = (RulerPlayerActivity) getActivity();
        rulerPlayerActivity.setPlayerListCallback(this);
        rulerPlayerActivity.setActivityCallback(new RulerPlayerActivity.ActivityCallback() {
            @Override
            public void onBackPressed() {
                if (isMenuVisible()) {
                    if (!mSearchView.isIconified()) {
                        mSearchView.setQuery(null, false);
                        mSearchView.clearFocus();
                        mSearchView.setIconified(true);
                    } else {
                        MusicFragment.this.onBackPressed();
                    }
                }
            }

            @Override
            public void onMenuPressed() {
            }

        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTracksAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.music_fragment, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mTracksAdapter = new TracksAdapter(new ArrayList<TrackItem>());
        mRecyclerView.setAdapter(mTracksAdapter);
        setupTouchHolderHelper();
        onCreateView();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIsViewLoaded = true;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && mIsViewLoaded) {
            setActivityCallback();
            showActionBar();
        }
    }

    private void setupTouchHolderHelper() {
        ItemTouchHelper.Callback touchHolderCallback = new TouchHolderCallback<TracksAdapter>(mTracksAdapter) {
            @Override
            public boolean isLongPressDragEnabled() {
                return mIsSwapMode;
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHolderCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_music_fragment, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final float screenWidthInDp = displayMetrics.widthPixels / displayMetrics.density;
        if (screenWidthInDp < 360f) {
            menu.getItem(3).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        onOptionsMenuCreate(menu);

        mBottomBar = (ActionMenuView) mView.findViewById(R.id.bottom_toolbar);
        Menu bottomMenu = mBottomBar.getMenu();
        if (bottomMenu.size() != 0) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }

        inflater.inflate(R.menu.menu_music_bottom_controls, bottomMenu);
        boolean isRepeatOne = Preferences.isRepeatOne(getContext());
        setRepeatBtnIcon(isRepeatOne);
        for (int i = 0; i < bottomMenu.size(); i++) {
            bottomMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQuerySearchSubmit(query);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        onQuerySearchChanged(newText);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play_btn:
                onPlayBtnPressed();
                return true;
            case R.id.pause_btn:
                mCardCallbacks.onPausePressed();
                return true;
            case R.id.stop_btn:
                mCardCallbacks.onStopPressed();
                return true;
            case R.id.repeat_mode:
                boolean repeatOne = Preferences.isRepeatOne(getContext());
                repeatOne = !repeatOne;
                Preferences.setRepeatOne(getContext(), repeatOne);
                setRepeatBtnIcon(repeatOne);
                return true;
            case R.id.sort_btn:
                showSortDialog();
                return true;
            case R.id.changeable_btn:
                lastButtonPressed();
                return true;
            case R.id.menu_item_swap_positions:
                mIsSwapMode = !mIsSwapMode;
                if (mIsSwapMode) {
                    item.setIcon(R.drawable.ic_import_export_red_24dp);
                    Toast toast = Toast.makeText(getContext(), R.string.toast_on_swap_mode, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    item.setIcon(R.drawable.ic_import_export_white_24dp);
                    onSwapModeFinished();
                }
                return true;
            case R.id.menu_item_close_app:
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void onSwapModeFinished() {

    }

    void scrollToCurrentTrack() {
        int currentIndex = mTracksAdapter.getCurrentIndex();
        if (currentIndex == -1) return;
        mLayoutManager.scrollToPosition(currentIndex);
    }

    private void showSortDialog() {
        ContextThemeWrapper ctw = new ContextThemeWrapper(getContext(), R.style.AlertDialogStyle);
        String[] dialogItems = getContext().getResources().getStringArray(R.array.dialog_sort_by);
        new AlertDialog.Builder(ctw)
                .setTitle(R.string.dialog_sort_by_title)
                .setItems(dialogItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                mTracksAdapter.sortByName();
                                break;
                            case 1:
                                mTracksAdapter.sortByArtist();
                                break;
                            case 2:
                                mTracksAdapter.sortByDate();
                                break;
                            case 3:
                                mTracksAdapter.sortByType();
                                break;
                        }
                        mTracksAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).create().show();
    }

    private void setRepeatBtnIcon(boolean repeatOne) {
        Menu menu = mBottomBar.getMenu();
        MenuItem item = menu.findItem(R.id.repeat_mode);
        if (repeatOne) {
            item.setIcon(R.drawable.ic_repeat_one_white_24dp);
        } else {
            item.setIcon(R.drawable.ic_repeat_white_24dp);
        }
    }

    void checkSdPermission(int request) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        boolean hasSDPermissions = isHasSDPermissions();
        if (hasSDPermissions) {
            onSdCardPermissionGranted(request);

        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, request);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast toast = Toast.makeText(getContext(),
                    R.string.snackbar_sd_card_access, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            onSdCardPermissionGranted(requestCode);
        }
    }


    private boolean isHasSDPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasReadSDPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasReadSDPermission == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void setCurrentTrack(TrackItem trackItem) {
        mCurrentTrack = trackItem;
        Preferences.setCurrentTrack(getContext(), trackItem);
    }

    void playTrack(TrackItem trackItem, int newIndex) {
        if (mCardCallbacks != null) {
            int prevIndex = mTracksAdapter.getCurrentIndex();
            setCurrentTrack(trackItem);
            if (prevIndex != -1) {
                mTracksAdapter.notifyItemChanged(prevIndex);
            }
            mTracksAdapter.notifyItemChanged(newIndex);

            mCardCallbacks.onPlay(trackItem);
        }
    }


    @Override
    public void repeatCurrent() {
        if (mCurrentTrack == null) return;
        mCardCallbacks.onPlayPressed(mCurrentTrack);
    }

    @Override
    public void playNext() {
        int currentIndex = getCurrentTrackIndex();
        if (currentIndex == -1) return;
        if (mTracksAdapter.getItemCount() - 1 == currentIndex) {
            searchNext(0);
        } else {
            searchNext(currentIndex + 1);
        }
    }

    private void searchNext(int startIndex) {
        for (int i = startIndex; i < mTracksAdapter.getItemCount(); i++) {
            TrackItem trackItem = mTracksAdapter.getAllItems().get(i);
            if (trackItem.isTrack()) {
                playTrack(trackItem, i);
                mLayoutManager.scrollToPosition(i);
                break;
            }
        }
    }

    @Override
    public void playPrev() {
        int currentIndex = getCurrentTrackIndex();
        if (currentIndex == -1) return;

        if (currentIndex == 0) {
            searchPrevFromListEnd();
        } else {
            for (int i = currentIndex - 1; i > -1; i--) {
                TrackItem trackItem = mTracksAdapter.getAllItems().get(i);
                if (trackItem.isTrack()) {
                    playTrack(trackItem, i);
                    mLayoutManager.scrollToPosition(i);
                    break;
                }
                if (i == 0) {
                    searchPrevFromListEnd();
                }
            }
        }
    }

    private void searchPrevFromListEnd() {
        for (int i = mTracksAdapter.getItemCount() - 1; i > -1; i--) {
            TrackItem trackItem = mTracksAdapter.getAllItems().get(i);
            if (trackItem.isTrack()) {
                playTrack(trackItem, i);
                mLayoutManager.scrollToPosition(i);
                break;
            }
        }
    }


    private ActionMode.Callback getActionModeCallback() {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mModeListener.onActionModeStart();
                onActionModeCreate(mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                onActionMenuClicked(item);
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mTracksAdapter.refreshItems();
                mActionMode = null;
                mModeListener.onActionModeFinish();
            }
        };
    }

    void finishActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    void onSelectionChanged() {

    }


    class TracksAdapter extends SelectableAdapter<TrackHolder, TrackItem> {

        public TracksAdapter(List<TrackItem> list) {
            super(list);
            sortTrackList();
        }

        @Override
        protected void onNewItemsSet() {
            sortTrackList();
        }

        public void sortTrackList() {
            int sortOrder = Preferences.getSortOrder(getContext());
            switch (sortOrder) {
                case SORT_BY_NAME:
                    sortByName();
                    break;
                case SORT_BY_ARTIST:
                    sortByArtist();
                    break;
                case SORT_BY_DATE:
                    sortByDate();
                    break;
                case SORT_BY_TYPE:
                    sortByType();
                    break;
            }
        }

        @Override
        public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.track_item, null);
            return new TrackHolder(view);
        }

        @Override
        public void onBindViewHolder(TrackHolder holder, int position) {
            holder.bindItem(mITEMs.get(position));

            if (mCurrentTrack != null) {
                if (mCurrentTrack.getFilePath().equals(mITEMs.get(position).getFilePath())) {
                    holder.itemView.setBackgroundResource(R.drawable.selectable_current_background);
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.selectable_item_background);
                }
            }
        }

        @Override
        ActionMode getActionMode() {
            return mActionMode;
        }

        @Override
        protected void selectionChanged() {
            onSelectionChanged();
        }

        int getCurrentIndex() {
            return getIndex(mCurrentTrack);
        }

        int getIndex(TrackItem trackItem) {
            if (trackItem == null) {
                return -1;
            }

            int index = -1;
            for (int i = 0; i < mITEMs.size(); i++) {
                String filePath = mITEMs.get(i).getFilePath();
                if (filePath.toLowerCase().equals(trackItem.getFilePath().toLowerCase())) {
                    index = i;
                    break;
                }
            }
            return index;
        }


        public void sortByName() {
            Collections.sort(mITEMs, TrackSorts.byName());
            Preferences.setSortOrder(getContext(), SORT_BY_NAME);
        }

        public void sortByArtist() {
            Collections.sort(mITEMs, TrackSorts.byArtist());
            Preferences.setSortOrder(getContext(), SORT_BY_ARTIST);
        }

        public void sortByType() {
            Collections.sort(mITEMs, TrackSorts.byType());
            Preferences.setSortOrder(getContext(), SORT_BY_TYPE);
        }

        public void sortByDate() {
            Collections.sort(mITEMs, TrackSorts.byDate());
            Preferences.setSortOrder(getContext(), SORT_BY_DATE);
        }

    }


    class TrackHolder extends SelectableHolder<TrackItem> {

        private TrackItem mTrackItem;
        private final TextView mNameTextView;
        private final TextView mArtistTextView;
        private final TextView mDurationTextView;
        private final TextView mExtensionTextView;
        private final ImageView mFolderIconImageView;

        @Override
        ActionMode getActionMode() {
            return mActionMode;
        }

        @Override
        void onClick() {
            onTrackHolderClick(mTrackItem, getAdapterPosition());
        }

        @Override
        void onLongClick() {
            if (mTrackItem instanceof MovieItem) {
                return;
            }
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            mActionMode = activity.startSupportActionMode(mModeCallback);

        }

        @Override
        boolean isSwapMode() {
            return mIsSwapMode;
        }

        public TrackHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.track_name_text_view);
            mArtistTextView = (TextView) itemView.findViewById(R.id.artist_text_view);
            mDurationTextView = (TextView) itemView.findViewById(R.id.duration_text_view);
            mExtensionTextView = (TextView) itemView.findViewById(R.id.extension_text_view);
            mFolderIconImageView = (ImageView) itemView.findViewById(R.id.folder_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindItem(TrackItem trackItem) {
            setITEM(trackItem);
            mTrackItem = trackItem;
            File file = trackItem.getFile();

            setViewsVisibility(trackItem);

            if (trackItem.isTrack()) {
                if (trackItem.isHasInfo()) {
                    setTrackName(trackItem);
                    setTrackDuration(trackItem);
                    setBitrateExtension(trackItem);
                }
            } else {
                mNameTextView.setText(file.getName());
                mNameTextView.setLines(1);
            }

            if (trackItem instanceof MovieItem) {
                mFolderIconImageView.setVisibility(View.VISIBLE);
                mExtensionTextView.setVisibility(View.GONE);
            }


        }

        private void setBitrateExtension(TrackItem trackItem) {
            if (!trackItem.isOnline()) {
                boolean showLocalBitrate = Preferences.isShowLocalBitrate(getContext());
                if (showLocalBitrate) {
                    showBitrateExtension(trackItem);
                } else {
                    showExtensionOnly(trackItem);
                }
            } else if (trackItem.isOnline()) {
               showBitrateExtension(trackItem);
            }
        }

        private void showExtensionOnly(TrackItem trackItem) {
            mExtensionTextView.setText(trackItem.getExtension());
        }

        private void showBitrateExtension(TrackItem trackItem) {
            if (trackItem.getBitrate() == null || trackItem.getBitrate().equals("")) {
                showExtensionOnly(trackItem);
            } else {
                String formatted = String.format(
                        "%s | %s", trackItem.getExtension(), trackItem.getBitrate());
                mExtensionTextView.setText(formatted);
            }
        }


        private void setTrackDuration(TrackItem trackItem) {
            mDurationTextView.setText(trackItem.getDuration());
            if (trackItem.getDuration().equals(TrackInfoParser.FILE_CORRUPTED)) {
                mDurationTextView.setTextColor(Color.RED);
            } else {
                mDurationTextView.setTextColor(Color.WHITE);
            }
        }

        private void setTrackName(TrackItem trackItem) {
            if (trackItem.getTrackName() == null) {
                mNameTextView.setLines(2);
                mNameTextView.setText(trackItem.getName());
                mArtistTextView.setVisibility(View.GONE);
            } else {
                mNameTextView.setLines(1);
                mNameTextView.setText(trackItem.getTrackName());
                mArtistTextView.setText(trackItem.getTrackArtist());
                mArtistTextView.setVisibility(View.VISIBLE);
            }
        }

        private void setViewsVisibility(TrackItem trackItem) {
            if (!trackItem.isTrack()) {
                mNameTextView.setText(trackItem.getName());
                mArtistTextView.setVisibility(View.GONE);
                mDurationTextView.setVisibility(View.GONE);
                mExtensionTextView.setVisibility(View.GONE);
                mFolderIconImageView.setVisibility(View.VISIBLE);
            } else {
                mArtistTextView.setVisibility(View.VISIBLE);
                mDurationTextView.setVisibility(View.VISIBLE);
                mExtensionTextView.setVisibility(View.VISIBLE);
                mFolderIconImageView.setVisibility(View.GONE);
            }
        }


    }


}
