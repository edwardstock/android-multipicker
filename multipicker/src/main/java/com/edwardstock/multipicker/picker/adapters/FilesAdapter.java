package com.edwardstock.multipicker.picker.adapters;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.PickerConfig;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private LayoutInflater mInflater;

    private List<MediaFile> mItems = new ArrayList<>();
    private OnMediaClickListener mItemClickListener;
    private SelectionTracker<MediaFile> mSelectionTracker;
    private List<MediaFile> mSelectedInit;
    private PickerConfig mConfig;

    public FilesAdapter(PickerConfig config, List<MediaFile> selectedFiles, OnMediaClickListener itemClickListener) {
        mConfig = config;
        mItemClickListener = itemClickListener;
        mSelectedInit = selectedFiles;
    }

    public void setSelectionTracker(SelectionTracker<MediaFile> selectionTracker) {
        mSelectionTracker = selectionTracker;
        if (mSelectedInit != null && !mSelectedInit.isEmpty()) {
            mSelectionTracker.setItemsSelected(mSelectedInit, true);
            mSelectedInit.clear();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }

        return new ViewHolder(
                mInflater.inflate(R.layout.mp_item_image, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final MediaFile file = mItems.get(position);
        final boolean isSelected = mSelectionTracker.isSelected(file);
        boolean isEnableSelection = isSelected || mSelectionTracker.getSelection().size() > 0;

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(.8F);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        viewHolder.imageView.setColorFilter(filter);

        Glide.with(viewHolder.imageView)
                .load(file.getPath())
                .apply(new RequestOptions().error(R.drawable.mp_image_placeholder))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.imageView);

        ConstraintSet target = new ConstraintSet();
        target.clone(viewHolder.root);

        if (isSelected) {
            target.constrainPercentHeight(R.id.mp_image_view_root, .80F);
            target.constrainPercentWidth(R.id.mp_image_view_root, .80F);

        } else {
            target.constrainPercentHeight(R.id.mp_image_view_root, 1.0F);
            target.constrainPercentWidth(R.id.mp_image_view_root, 1.0F);
        }

        if (file.isVideo()) {
            if (file.getVideoInfo() != null) {
                viewHolder.fileTypeDesc.setVisibility(View.VISIBLE);
                viewHolder.fileTypeDesc.setText(file.getVideoInfo().getDuration());
            }
            viewHolder.fileTypeIcon.setImageResource(R.drawable.mp_ic_play_circle_filled);
            viewHolder.fileTypeIcon.setVisibility(View.VISIBLE);
        }

//        target.setAlpha(R.id.mp_file_type_icon, showFileTypeIndicator ? 1.F : 0F);

        if (isEnableSelection) {
            target.setAlpha(R.id.image_selection, 1F);
        } else {
            target.setAlpha(R.id.image_selection, 0F);
        }
        target.setAlpha(R.id.mp_overlay, isSelected ? .5F : 0F);


        viewHolder.selection.setSelected(isSelected);


        viewHolder.itemView.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(file, isSelected, viewHolder.imageView);
            }
        });


        Timber.d("Update item: %d, selection enable: %b", viewHolder.getAdapterPosition(), isEnableSelection);
        new Handler(Looper.getMainLooper()).post(() -> {

            TransitionSet allover = new TransitionSet();
            allover.addTransition(new AutoTransition());
            allover.setDuration(100);

            TransitionSet check = new TransitionSet();
            check.addTarget(R.id.image_selection);
            check.setDuration(150);

            allover.addTransition(check);

            if(mConfig.isEnableSelectionAnimation()) {
                TransitionManager.beginDelayedTransition(viewHolder.root, allover);
            }

            target.applyTo(viewHolder.root);
        });

    }

    public List<MediaFile> getItems() {
        return mItems;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setData(List<MediaFile> files) {
        mItems.clear();
        mItems.addAll(files);
    }

    public MediaFile getItem(int position) {
        return mItems.get(position);
    }

    @Deprecated
    public Selection<MediaFile> getSelectedImages() {
        return mSelectionTracker.getSelection();
    }

    public interface OnMediaClickListener {
        void onClick(MediaFile file, boolean isSelected, View sharedView);
    }

    interface ViewHolderWithDetails<T> {
        ItemDetailsLookup.ItemDetails<T> getItemDetails(FilesAdapter adapter, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ViewHolderWithDetails<MediaFile> {
        public @BindView(R2.id.image_selection) View selection;
        ConstraintLayout root;
        @BindView(R2.id.image_view) ImageView imageView;
        @BindView(R2.id.mp_file_type_icon) ImageView fileTypeIcon;
        @BindView(R2.id.mp_file_type_desc) TextView fileTypeDesc;
        @BindView(R2.id.mp_overlay) View overlay;

        ViewHolder(View itemView) {
            super(itemView);
            root = (ConstraintLayout) itemView;
            ButterKnife.bind(this, itemView);
        }

        @Override
        public ItemDetailsLookup.ItemDetails<MediaFile> getItemDetails(final FilesAdapter adapter, final int position) {
            return new ItemDetailsLookup.ItemDetails<MediaFile>() {
                @Override
                public int getPosition() {
                    return position;
                }

                @Nullable
                @Override
                public MediaFile getSelectionKey() {
                    return adapter.getItem(position);
                }
            };
        }
    }


}
