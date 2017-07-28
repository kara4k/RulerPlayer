package com.kara4k.rulerplayer;


import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class SelectableAdapter<SVH extends SelectableHolder, ITEM extends SearchableItem>
        extends RecyclerView.Adapter<SVH> implements SelectableHolder.HolderCallbacks {

    List<ITEM> mITEMs;
    private SparseBooleanArray mSelectedItems;
    private int mSelectedItemsCount;

    abstract ActionMode getActionMode();

    @Override
    public void onBindViewHolder(SVH holder, int position, List<Object> payloads) {
        holder.setCallbacks(this);
        boolean isSelected = mSelectedItems.get(position, false);
        holder.itemView.setSelected(isSelected);
        super.onBindViewHolder(holder, position, payloads);
    }

    public SelectableAdapter(List<ITEM> list) {
        if (list == null) {
            throw new IllegalArgumentException("list may not be null");
        }
        mITEMs = list;
        mSelectedItems = new SparseBooleanArray();
        mSelectedItemsCount = 0;
    }

    @Override
    public int getItemCount() {
        return mITEMs.size();
    }

    public void refreshItems() {
        mSelectedItems = new SparseBooleanArray();
        mSelectedItemsCount = 0;
        notifyDataSetChanged();
    }

    public void selectAll() {
        if (getActionMode() == null) {
            return;
        }

        for (int i = 0; i < mITEMs.size(); i++) {
            mSelectedItems.put(i, true);
        }
        mSelectedItemsCount = mITEMs.size();
        getActionMode().setTitle(String.valueOf(mSelectedItemsCount));
        notifyDataSetChanged();
    }

    public List<ITEM> getAllItems() {
        return mITEMs;
    }

    public List<ITEM> getSelectedItems() {
        List<ITEM> items = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); i++) {
            boolean isSelected = mSelectedItems.valueAt(i);
            int key = mSelectedItems.keyAt(i);
            if (isSelected) {
                items.add(mITEMs.get(key));
            }
        }
        return items;
    }

    public List<Integer> getSelectedIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); i++) {
            boolean isSelected = mSelectedItems.valueAt(i);
            int key = mSelectedItems.keyAt(i);
            if (isSelected) {
                indexes.add(key);
            }
        }
        return indexes;
    }

    void selectionChanged() {

    }

    @Override
    public void toggleSelection(int position, boolean isSelected) {
        if (getActionMode() == null) {
            return;
        }

        mSelectedItems.put(position, isSelected);
        mSelectedItemsCount = isSelected ? ++mSelectedItemsCount : --mSelectedItemsCount;
        getActionMode().setTitle(String.valueOf(mSelectedItemsCount));
        selectionChanged();
        if (mSelectedItemsCount == 0) {
            getActionMode().finish();
        }
    }

    public void appendItems(List<ITEM> items) {
        if (mITEMs == null) {
            mITEMs = new ArrayList<>();
        }
        mITEMs.addAll(items);
        notifyDataSetChanged();
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mITEMs, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }


    public void setITEMs(List<ITEM> ITEMs) {
        mITEMs = ITEMs;
        onNewItemsSet();
    }

    public void setITEMs(List<ITEM> ITEMs, boolean isCallOnItemsSet) {
        mITEMs = ITEMs;
        if (isCallOnItemsSet) {
            onNewItemsSet();
        }
    }

    protected void onNewItemsSet() {

    }
}
