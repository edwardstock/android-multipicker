package com.edwardstock.multipicker.picker.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.helpers.DisplayHelper;
import com.edwardstock.multipicker.internal.helpers.ExceptionHelper;
import com.jsibbold.zoomage.ZoomageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class PreviewImagePagerItem extends PreviewPagerItem {

    @BindView(R2.id.mp_photo_view) ZoomageView imageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);

        progress.setVisibility(View.VISIBLE);
        btnSend.setOnClickListener(v -> {
//            submitResult(Collections.singletonList(mFile));
        });

        imageView.setOnClickListener(v -> {
            safeActivity(PreviewerActivity::toggleUiVisibility);
        });


        return view;
    }

    @Override
    protected void initMedia(MediaFile mediaFile) {
        final int width = DisplayHelper.getWidth(getContext());
        final int height = DisplayHelper.getHeight(getContext());

        ExceptionHelper.doubleTryOOM(() -> {
            Glide.with(this)
                    .load(getMediaFile().getPath())
                    .apply(new RequestOptions()
                            .override(width, height)
                            .centerInside()
                            .encodeQuality(80)
                    )
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Timber.e(e);
                            progress.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
        }, t -> {
            imageView.setImageResource(R.drawable.mp_bg_black_error);
            progress.setVisibility(View.GONE);
        }, "mp_preview_load");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mp_page_preview_image_item;
    }
}
