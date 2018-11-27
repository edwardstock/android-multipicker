package com.edwardstock.multipicker.picker.adapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.data.Dir;

import java.util.ArrayList;
import java.util.List;


public class DirsAdapter extends RecyclerView.Adapter<DirsAdapter.DirViewHolder> {

    private final OnDirClickListener folderClickListener;
    private LayoutInflater mInflater;
    private List<Dir> mDirs = new ArrayList<>();

    public DirsAdapter(OnDirClickListener folderClickListener) {
        this.folderClickListener = folderClickListener;
    }

    @NonNull
    @Override
    public DirViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        return new DirViewHolder(
                mInflater.inflate(R.layout.mp_item_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final DirViewHolder holder, int position) {
        final Dir dir = mDirs.get(position);

        Glide.with(holder.image)
                .load(dir.getFiles().get(0).getPath())
                .apply(new RequestOptions()
                        .error(R.drawable.mp_folder_placeholder)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.image);

        holder.name.setText(mDirs.get(position).getName());
        holder.number.setText(String.valueOf(mDirs.get(position).getFilesCount()));

        holder.itemView.setOnClickListener(v -> {
            if (folderClickListener != null)
                folderClickListener.onClick(dir);
        });
    }

    public void setData(List<Dir> folders) {
        if (folders != null) {
            mDirs.clear();
            mDirs.addAll(folders);
        }
//        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDirs.size();
    }

    public interface OnDirClickListener {
        void onClick(Dir dir);
    }

    static class DirViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name;
        private TextView number;

        DirViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.tv_name);
            number = itemView.findViewById(R.id.tv_number);
        }
    }
}
