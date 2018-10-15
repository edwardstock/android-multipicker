package com.edwardstock.multipicker.picker.adapters;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.MediaFile;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
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
    private MediaFile mWaitintForMeasure = null;
    private OnMeasuredListener mOnMeasuredListener;
    private Map<Integer, Boolean> mItemChanged = new HashMap<>();
    private WeakReference<RecyclerView> mList;
    private boolean mIsScrolling = false;

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
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Context ctx = viewHolder.itemView.getContext();
        final MediaFile file = mItems.get(position);
        final boolean isSelected = mSelectionTracker.isSelected(file);
        boolean isEnableSelection = isSelected || mSelectionTracker.getSelection().size() > 0;
        ViewCompat.setTransitionName(viewHolder.imageView, ctx.getResources().getString(R.string.mp_transition_image) + String.valueOf(file.getId()));

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(.8F);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        viewHolder.imageView.setColorFilter(filter);

        Glide.with(viewHolder.imageView)
                .load(file.getPath())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@android.support.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (mWaitintForMeasure == null || mOnMeasuredListener == null) {
                            return false;
                        }
                        if (file.equals(mWaitintForMeasure)) {
                            mOnMeasuredListener.onMeasured(mWaitintForMeasure);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (mWaitintForMeasure == null || mOnMeasuredListener == null) {
                            return false;
                        }
                        if (file.equals(mWaitintForMeasure)) {
                            mOnMeasuredListener.onMeasured(mWaitintForMeasure);
                        }
                        return false;
                    }
                })
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
        if (mList != null && mList.get() != null) {
            mList.get().post(() -> {
                TransitionSet allover = new TransitionSet();
                allover.addTransition(new AutoTransition());
                allover.setDuration(100);

                TransitionSet check = new TransitionSet();
                check.addTarget(R.id.image_selection);
                check.setDuration(120);

                allover.addTransition(check);
                allover.setOrdering(TransitionSet.ORDERING_TOGETHER);

                if (mConfig.isEnableSelectionAnimation() && !mIsScrolling) {
                    TransitionManager.beginDelayedTransition(viewHolder.root, allover);
                }

                target.applyTo(viewHolder.root);
            });
        }
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

    public void setOnFileMeasuredListener(MediaFile file, OnMeasuredListener listener) {
        mWaitintForMeasure = file;
        mOnMeasuredListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mList = new WeakReference<>(recyclerView);
    }

    public void setIsScrolling(boolean b) {
        mIsScrolling = b;
    }

    public interface OnMeasuredListener {
        void onMeasured(MediaFile file);
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
