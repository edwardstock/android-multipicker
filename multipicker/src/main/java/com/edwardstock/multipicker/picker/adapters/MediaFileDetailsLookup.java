package com.edwardstock.multipicker.picker.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.edwardstock.multipicker.data.MediaFile;

import java.lang.ref.WeakReference;
import androidx.recyclerview.selection.ItemDetailsLookup;

/**
 * android-image-picker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class MediaFileDetailsLookup extends ItemDetailsLookup<MediaFile> {
    private WeakReference<RecyclerView> mRecyclerView;

    public MediaFileDetailsLookup(RecyclerView recyclerView) {
        mRecyclerView = new WeakReference<>(recyclerView);
    }

    @Nullable
    @Override
    public ItemDetails<MediaFile> getItemDetails(@NonNull MotionEvent e) {
        View view = mRecyclerView.get().findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = mRecyclerView.get().getChildViewHolder(view);
            if (viewHolder instanceof FilesAdapter.ViewHolderWithDetails) {
                int position = viewHolder.getAdapterPosition();
                return ((FilesAdapter.ViewHolderWithDetails<MediaFile>) viewHolder).getItemDetails(((FilesAdapter) mRecyclerView.get().getAdapter()), position);
            }
        }

        return null;

    }
}
