package com.kara4k.moozic;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.Comparator;
import java.util.List;


public class CardFragment extends MusicFragment<CardFragment.CardAdapter, TrackItem>
        implements SearchView.OnQueryTextListener, Player.PlayerListCallback {

    public static final int PERMISSION_STORAGE = 1;

    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_ARTIST = 2;
    public static final int SORT_BY_DATE = 3;
    public static final int SORT_BY_TYPE = 4;

    private File mCurrentDir;
    private CardTracksHolder mCardTracksHolder;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private TrackInfoParser mTrackInfoParser;
    private CardCallbacks mCardCallbacks;
    private CardAdapter mCardAdapter;
    private TrackItem mCurrentTrack;
    private View mView;
    private ActionMenuView mBottomBar;
    private SearchView mSearchView;
    private MenuItem mSearchItem;
    private List<TrackItem> mSearchableItems;
    private boolean mIsViewLoaded = false;
    private boolean mIsSwapMode = false;

    interface CardCallbacks {

        void onPlay(TrackItem trackItem);

        void onPlayPressed(TrackItem trackItem);

        void onPausePressed();

        void onStopPressed();

    }

    public static CardFragment newInstance() {
        return new CardFragment();
    }

    @Override
    CardAdapter getADAPTER() {
        return mCardAdapter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCardCallbacks = (CardCallbacks) getActivity();
        setActivityCallback();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        setHasOptionsMenu(true);
        mSearchableItems = new ArrayList<>();

        mCardTracksHolder = new CardTracksHolder(getContext());

        setupTrackInfoReceiver();
        checkPermissionsAndSetupCurrentFolder();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.card_fragment, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mCardAdapter = new CardAdapter(new ArrayList<TrackItem>());
        mRecyclerView.setAdapter(mCardAdapter);

        ItemTouchHelper.Callback touchHolderCallback = new TouchHolderCallback<CardAdapter>(mCardAdapter) {
            @Override
            public boolean isLongPressDragEnabled() {
                return mIsSwapMode;
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHolderCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        updateUI(mCurrentDir);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIsViewLoaded = true;
    }

    private void updateUI(File dir) {
        mTrackInfoParser.clearQueue();

        if (dir == null) {
            return;
        }
        mCurrentDir = dir;
        List<TrackItem> tracksInDir = mCardTracksHolder.getTracksInDir(dir);
        mCardAdapter.setITEMs(tracksInDir);
        mCardAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mCardAdapter);

        mTrackInfoParser.queueTrackInfo(tracksInDir);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_card_fragment, menu);
        mSearchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);

        mBottomBar = (ActionMenuView) mView.findViewById(R.id.bottom_toolbar);
        Menu bottomMenu = mBottomBar.getMenu();
        inflater.inflate(R.menu.menu_card_controls, bottomMenu);

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
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ArrayList<TrackItem> foundItems = new ArrayList<>();
        for (int i = 0; i < mSearchableItems.size(); i++) {
            String query = newText.toLowerCase();
            String firstField = mSearchableItems.get(i).getFirstField().toLowerCase();
            String secondField = mSearchableItems.get(i).getSecondField().toLowerCase();
            if (firstField.contains(query) || secondField.contains(query)) {
                foundItems.add(mSearchableItems.get(i));
            }
        }
        mCardAdapter.setITEMs(foundItems);
        mCardAdapter.notifyDataSetChanged();
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play_btn:
                if (mCurrentTrack == null) return true;
                mCardCallbacks.onPlayPressed(mCurrentTrack);
                if (mCurrentDir == null) return true;
                File parentFile = mCurrentTrack.getFile().getParentFile();
                if (!mCurrentDir.getPath().equals(parentFile.getPath())) {
                    updateUI(parentFile);
                }
                int currentIndex = mCardAdapter.getCurrentIndex();
                if (currentIndex == -1) return true;
                mLayoutManager.scrollToPosition(currentIndex);
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
            case R.id.folders_btn:
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
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkPermissionsAndSetupCurrentFolder() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        boolean hasSDPermissions = isHasSDPermissions();
        if (hasSDPermissions) {
            setCurrentDir();
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast toast = Toast.makeText(getContext(),
                            R.string.snackbar_sd_card_access, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    setCurrentDir();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void toggleActionBarVisibility() {
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            ActionBar supportActionBar = activity.getSupportActionBar();
            if (supportActionBar != null && supportActionBar.isShowing()) {
                supportActionBar.hide();
            } else if (supportActionBar != null && !supportActionBar.isShowing()) {
                supportActionBar.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void setupTrackInfoReceiver() {
        Handler handler = new Handler();
        mTrackInfoParser = new TrackInfoParser(handler);
        mTrackInfoParser.setInfoParserCallback(new TrackInfoParser.InfoParserCallback() {
            @Override
            public void onComplete(TrackItem filledTrackItem) {
                List<TrackItem> list = mCardAdapter.getAllItems();
                for (int i = 0; i < list.size(); i++) {
                    String trackItemPath = list.get(i).getFile().getPath();
                    if (trackItemPath.equals(filledTrackItem.getFile().getPath())) {
                        list.set(i, filledTrackItem);
                        mCardAdapter.notifyItemChanged(i);

                        if (i == list.size() - 1) {
                            mSearchableItems.clear();
                            mSearchableItems.addAll(mCardAdapter.getAllItems());
                        }
                        break;
                    }
                }
            }
        });
        mTrackInfoParser.start();
        mTrackInfoParser.getLooper();
    }

    private void setActivityCallback() {
        MoozicActivity moozicActivity = (MoozicActivity) getActivity();
        moozicActivity.setPlayerListCallback(this);
        moozicActivity.setActivityCallback(new MoozicActivity.ActivityCallback() {
            @Override
            public void onBackPressed() {
                if (isMenuVisible()) {
                    if (!mSearchView.isIconified()) {
                        mSearchView.setQuery(null, false);
                        mSearchView.clearFocus();
                        mSearchView.setIconified(true);
                    } else {
                        updateUI(mCurrentDir.getParentFile());
                    }
                }
            }

            @Override
            public void onMenuPressed() {
                toggleActionBarVisibility();
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && mIsViewLoaded) {
            setActivityCallback();
            toggleActionBarVisibility();
        }
    }

    private void setCurrentDir() {
        mCurrentTrack = Preferences.getCurrentTrack(getContext());
        if (mCurrentTrack != null) {
            mCurrentDir = mCurrentTrack.getFile().getParentFile();
        } else {
            mCurrentDir = Environment.getExternalStorageDirectory();
        }
    }

    public void setCurrentTrack(TrackItem trackItem) {
        mCurrentTrack = trackItem;
        Preferences.setCurrentTrack(getContext(), trackItem);
    }

    public void playTrack(TrackItem trackItem, int newIndex) {
        if (mCardCallbacks != null) {
            int prevIndex = mCardAdapter.getCurrentIndex();
            setCurrentTrack(trackItem);
            if (prevIndex != -1) {
                mCardAdapter.notifyItemChanged(prevIndex);
            }
            mCardAdapter.notifyItemChanged(newIndex);

            mCardCallbacks.onPlay(trackItem);
        }
    }

    @Override
    public void playNext() {
        int currentIndex = getCurrentIndex();
        if (currentIndex == -1) return;
        if (mCardAdapter.getItemCount() - 1 == currentIndex) {
            searchNext(0);
        } else {
            searchNext(currentIndex + 1);
        }
    }

    private void searchNext(int startIndex) {
        for (int i = startIndex; i < mCardAdapter.getItemCount(); i++) {
            TrackItem trackItem = mCardAdapter.getAllItems().get(i);
            if (trackItem.isTrack()) {
                playTrack(trackItem, i);
                mLayoutManager.scrollToPosition(i);
                break;
            }
        }
    }

    private int getCurrentIndex() {
        if (mCurrentTrack == null || mCurrentDir == null) return -1;
        File parentFile = mCurrentTrack.getFile().getParentFile();
        if (!mCurrentDir.getPath().equals(parentFile.getPath())) {
            updateUI(parentFile);
        }
        return mCardAdapter.getCurrentIndex();

    }

    @Override
    public void playPrev() {
        int currentIndex = getCurrentIndex();
        if (currentIndex == -1) return;

        if (currentIndex == 0) {
            searchPrevFromListEnd();
        } else {
            for (int i = currentIndex - 1; i > -1; i--) {
                TrackItem trackItem = mCardAdapter.getAllItems().get(i);
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
        for (int i = mCardAdapter.getItemCount() - 1; i > -1; i--) {
            TrackItem trackItem = mCardAdapter.getAllItems().get(i);
            if (trackItem.isTrack()) {
                playTrack(trackItem, i);
                mLayoutManager.scrollToPosition(i);
                break;
            }
        }
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
                                mCardAdapter.sortByName();
                                break;
                            case 1:
                                mCardAdapter.sortByArtist();
                                break;
                            case 2:
                                mCardAdapter.sortByDate();
                                break;
                            case 3:
                                mCardAdapter.sortByType();
                                break;
                        }
                        mCardAdapter.notifyDataSetChanged();
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        mTrackInfoParser.quit();
    }

    class CardAdapter extends SelectableAdapter<TrackHolder, TrackItem> {

        public CardAdapter(List<TrackItem> list) {
            super(list);
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
                if (mCurrentTrack.getFile().getPath().equals(mITEMs.get(position).getFile().getPath())) {
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


        int getCurrentIndex() {
            if (mCurrentTrack == null) {
                return -1;
            }

            int index = -1;
            for (int i = 0; i < mITEMs.size(); i++) {
                File file = mITEMs.get(i).getFile();
                if (file.getPath().equals(mCurrentTrack.getFile().getPath())) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        private TrackItem getCurrentTrack() {
            int currentIndex = getCurrentIndex();
            if (currentIndex == -1) return null;
            return mITEMs.get(currentIndex);
        }

        public void sortByName() {
            Collections.sort(mITEMs, new Comparator<TrackItem>() {
                @Override
                public int compare(TrackItem trackItem, TrackItem t1) {
                    if (!trackItem.isTrack() && t1.isTrack()) {
                        return -1;
                    } else if (trackItem.isTrack() && !t1.isTrack()) {
                        return 1;
                    } else if (!trackItem.isTrack() && !t1.isTrack()) {
                        return trackItem.getName().compareToIgnoreCase(t1.getName());
                    } else if (trackItem.getTrackName() != null && t1.getTrackName() != null) {
                        return trackItem.getTrackName().compareToIgnoreCase(t1.getTrackName());
                    } else if (trackItem.getTrackName() == null && t1.getTrackName() != null) {
                        return trackItem.getName().compareToIgnoreCase(t1.getTrackName());
                    } else if (trackItem.getTrackName() != null && t1.getTrackName() == null) {
                        return trackItem.getTrackName().compareToIgnoreCase(t1.getName());
                    } else {
                        return trackItem.getName().compareToIgnoreCase(t1.getName());
                    }

                }
            });

            Preferences.setSortOrder(getContext(), SORT_BY_NAME);
        }

        public void sortByArtist() {
            Collections.sort(mITEMs, new Comparator<TrackItem>() {
                @Override
                public int compare(TrackItem track, TrackItem t1) {
                    if (!track.isTrack() && t1.isTrack()) {
                        return -1;
                    } else if (track.isTrack() && !t1.isTrack()) {
                        return 1;
                    } else if (!track.isTrack() && !t1.isTrack()) {
                        return track.getName().compareToIgnoreCase(t1.getName());
                    } else if (track.getTrackArtist() != null && t1.getTrackArtist() != null) {
                        return track.getTrackArtist().compareToIgnoreCase(t1.getTrackArtist());
                    } else if (track.getTrackArtist() == null && t1.getTrackArtist() != null) {
                        return -1;
                    } else if (track.getTrackArtist() != null && t1.getTrackArtist() == null) {
                        return 1;
                    } else {
                        return track.getName().compareToIgnoreCase(t1.getName());
                    }

                }
            });
            Preferences.setSortOrder(getContext(), SORT_BY_ARTIST);
        }

        public void sortByType() {
            Collections.sort(mITEMs, new Comparator<TrackItem>() {
                @Override
                public int compare(TrackItem track, TrackItem t1) {
                    if (!track.isTrack() && t1.isTrack()) {
                        return -1;
                    } else if (track.isTrack() && !t1.isTrack()) {
                        return 1;
                    } else if (!track.isTrack() && !t1.isTrack()) {
                        return track.getName().compareToIgnoreCase(t1.getName());
                    } else if (!track.getExtension().equals(t1.getExtension())) {
                        return track.getExtension().compareToIgnoreCase(t1.getExtension());
                    } else if (track.getExtension().equals(t1.getExtension())) {
                        return track.getName().compareToIgnoreCase(t1.getName());
                    } else {
                        return 0;
                    }
                }
            });
            Preferences.setSortOrder(getContext(), SORT_BY_TYPE);
        }

        public void sortByDate() {
            Collections.sort(mITEMs, new Comparator<TrackItem>() {
                @Override
                public int compare(TrackItem track, TrackItem t1) {
                    if (!track.isTrack() && t1.isTrack()) {
                        return -1;
                    } else if (track.isTrack() && !t1.isTrack()) {
                        return 1;
                    } else if (!track.isTrack() && !t1.isTrack()) {
                        return track.getName().compareToIgnoreCase(t1.getName());
                    } else if (track.getDate() > t1.getDate()) {
                        return -1;
                    } else if (track.getDate() < t1.getDate()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            Preferences.setSortOrder(getContext(), SORT_BY_DATE);
        }

    }


    class TrackHolder extends SelectableHolder<TrackItem> {

        private TrackItem mTrackItem;
        private TextView mNameTextView;
        private TextView mArtistTextView;
        private TextView mDurationTextView;
        private TextView mExtensionTextView;
        private ImageView mFolderIconImageView;

        @Override
        ActionMode getActionMode() {
            return mActionMode;
        }

        @Override
        void onClick() {
            if (!mTrackItem.isTrack()) {
                updateUI(mTrackItem.getFile());
            } else {
                playTrack(mTrackItem, getAdapterPosition());
            }
        }

        @Override
        void onLongClick() {
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

            setViewsVisibility(trackItem, file);

            if (trackItem.isTrack()) {
                if (trackItem.isHasInfo()) {
                    setTrackName(trackItem);
                    setTrackDuration(trackItem);
                    mExtensionTextView.setText(trackItem.getExtension());
                }
            } else {
                mNameTextView.setText(file.getName());
                mNameTextView.setLines(1);
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

        private void setViewsVisibility(TrackItem trackItem, File file) {
            if (file.isDirectory()) {
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
