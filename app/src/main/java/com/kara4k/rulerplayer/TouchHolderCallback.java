package com.kara4k.rulerplayer;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class TouchHolderCallback<ADAPTER extends SelectableAdapter> extends ItemTouchHelper.Callback {

    private final ADAPTER mAdapter;

    private int mFromPos = -1;
    private int mToPos = -1;

    public TouchHolderCallback(ADAPTER adapter) {
        this.mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder holderFrom,
                          RecyclerView.ViewHolder holderTo) {

        SelectableHolder fromHolder = (SelectableHolder) holderFrom;
        SelectableHolder toHolder = (SelectableHolder) holderTo;
        int fromPos = fromHolder.getAdapterPosition();
        int toPos = toHolder.getAdapterPosition();

        if (mFromPos != fromPos || mToPos != toPos) {
            mFromPos = fromPos;
            mToPos = toPos;
            if (fromHolder.getITEM().isTrack() && toHolder.getITEM().isTrack()) {
                mAdapter.onItemMove(holderFrom.getAdapterPosition(), holderTo.getAdapterPosition());
            }
            return true;
        }
        return false;

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

}
