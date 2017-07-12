package com.kara4k.rulerplayer;


import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.View;

abstract class SelectableHolder<ITEM extends SearchableItem> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {


    private HolderCallbacks mCallbacks;
    private ITEM mITEM;

    abstract ActionMode getActionMode();
    abstract void onClick();
    abstract void onLongClick();
    abstract boolean isSwapMode();


    public interface HolderCallbacks {
        void toggleSelection(int position, boolean isSelected);
    }

    public SelectableHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void setCallbacks(HolderCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public void onClick(View view) {
        if (getActionMode() == null) {
            onClick();
        } else {
            if (mCallbacks == null) {
                return;
            }
            if(mITEM.isTrack())
            toggleSelection(view);
        }

    }

    @Override
    public boolean onLongClick(View view) {
        if(isSwapMode()) return false;
        if (getActionMode() == null) {
            if (!mITEM.isTrack()) return true;
            onLongClick();
            getActionMode().setTitle("1");
            toggleSelection(view);
        } else {
            getActionMode().finish();
        }
        return true;
    }

    private void toggleSelection(View view) {
        view.setSelected(!view.isSelected());
        mCallbacks.toggleSelection(getAdapterPosition(), view.isSelected());
    }

    public ITEM getITEM() {
        return mITEM;
    }

    protected void setITEM(ITEM ITEM) {
        mITEM = ITEM;
    }
}
