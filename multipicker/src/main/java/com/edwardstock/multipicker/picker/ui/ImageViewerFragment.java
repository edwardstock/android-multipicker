package com.edwardstock.multipicker.picker.ui;

import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.views.BaseFsPresenter;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class ImageViewerFragment extends PickerFileSystemFragment {

    boolean mHiddenControls = false;
    @BindView(R2.id.mp_selection_action_add) ImageView btnAdd;
    @BindView(R2.id.mp_selection_action_send) ImageView btnSend;
    private Dir mDir;
    private MenuItem mSendMenu;
    private MediaFile mFile;
    private boolean mFirstAnim = true;

    public static ImageViewerFragment newInstance(Dir dir, MediaFile file) {
        Bundle args = new Bundle();
        args.putParcelable(PickerConst.EXTRA_MEDIA_FILE, file);
        args.putParcelable(PickerConst.EXTRA_DIR, dir);

        ImageViewerFragment fragment = new ImageViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private MediaFile mSelectedFile;

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();
        safeActivity(act -> {
            Timber.d("Revert activity colors");
            TypedValue typedValue = new TypedValue();
            TypedArray a = act.obtainStyledAttributes(typedValue.data, new int[]{
                    R.attr.colorPrimary,
                    R.attr.colorPrimaryDark,
                    R.attr.titleTextColor
            });
            @ColorInt int primColor = a.getColor(0, getResources().getColor(R.color.mp_colorPrimary));
            @ColorInt int darkColor = a.getColor(1, getResources().getColor(R.color.mp_colorPrimaryDark));
            @ColorInt int textColor = a.getColor(2, getResources().getColor(R.color.mp_textColorSecondary));
            a.recycle();

            act.toolbar.setTitleTextColor(textColor);
            act.toolbar.setSubtitleTextColor(textColor);
            act.toolbar.setBackgroundColor(primColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                act.getWindow().setStatusBarColor(darkColor);
            }

//            act.toolbar.setTranslationY(0);

            Menu menu = act.toolbar.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                item.setVisible(true);
            }


            act.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.mp_colorBackground)));
//            act.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        });
    }

    public Dir getDir() {
        return mDir;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mp_fragment_image_viewer, container, false);
        postponeEnterTransition();

        PhotoView imageView = view.findViewById(R.id.mp_photo_view);
        ButterKnife.bind(this, view);
        mFile = getArguments().getParcelable(PickerConst.EXTRA_MEDIA_FILE);
        mDir = getArguments().getParcelable(PickerConst.EXTRA_DIR);


        safeActivity(act -> {
            Timber.d("Set toolbar color");
            act.toolbar.setTitleTextColor(0x00FFFFFF);
            act.toolbar.setSubtitleTextColor(0x00FFFFFF);
            act.toolbar.setBackgroundColor(0x00FFFFFF);
            act.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF000000));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                act.getWindow().setStatusBarColor(0xFF000000);
            }
            act.getWindow().getDecorView().setBackgroundColor(0xFF000000);
            act.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF000000));

            Menu menu = act.toolbar.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                item.setVisible(false);
            }

            btnSend.setOnClickListener(v -> {
                safeActivity(act1 -> act1.submitResult(Collections.singletonList(mFile)));
            });

//            btnAdd.setOnClickListener(v -> {
//                mSelectedFile = mFile;
//                safeActivity(ac->ac.onBackPressed());
//            });

        });

        imageView.setImageURI(Uri.parse(mFile.getPath()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setTransitionName(getString(R.string.mp_transition_image) + String.valueOf(mFile.getId()));
        }
        startPostponedEnterTransition();

        return view;
    }

    public MediaFile getAddedFile() {
        return mSelectedFile;
    }

    @Override
    protected BaseFsPresenter getPresenter() {
        return null;
    }

}
