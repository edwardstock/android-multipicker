package com.edwardstock.multipicker.picker.ui;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.views.BaseFsPresenter;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Collections;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class ImageViewerFragment extends PickerFileSystemFragment {

    boolean mHiddenControls = false;
    private Dir mDir;

    public static ImageViewerFragment newInstance(Dir dir, MediaFile file) {
        Bundle args = new Bundle();
        args.putParcelable(PickerConst.EXTRA_MEDIA_FILE, file);
        args.putParcelable(PickerConst.EXTRA_DIR, dir);

        ImageViewerFragment fragment = new ImageViewerFragment();
        fragment.setArguments(args);
        return fragment;
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
            act.toolbar.setBackgroundColor(primColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                act.getWindow().setStatusBarColor(darkColor);
            }
            act.toolbar.setTranslationY(0);

            act.toolbar.getMenu().removeItem(mSendMenu.getItemId());

            act.getWindow().getDecorView().setBackgroundColor(0xFFFFFFFF);
            act.getWindow().setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
            act.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private MenuItem mSendMenu;
    private MediaFile mFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mp_fragment_image_viewer, container, false);
        postponeEnterTransition();

        PhotoView imageView = view.findViewById(R.id.mp_photo_view);
        mFile = getArguments().getParcelable(PickerConst.EXTRA_MEDIA_FILE);

        safeActivity(act -> {
            Timber.d("Set toolbar color");
            act.toolbar.setTitleTextColor(0x00FFFFFF);
            act.toolbar.setBackgroundColor(0x00FFFFFF);
            act.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF000000));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                act.getWindow().setStatusBarColor(0xFF000000);
            }
            act.getWindow().getDecorView().setBackgroundColor(0xFF000000);
            act.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF000000));

            Menu menu = act.toolbar.getMenu();
            mSendMenu = menu.add(R.string.mp_done);
            mSendMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            mSendMenu.setIcon(R.drawable.mp_ic_send_white);
            mSendMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    safeActivity(act-> act.submitResult(Collections.singletonList(mFile)));
                    return true;
                }
            });

        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setTransitionName(getString(R.string.mp_transition_image)+String.valueOf(mFile.getId()));
        }
        mDir = getArguments().getParcelable(PickerConst.EXTRA_DIR);

        imageView.setImageURI(Uri.parse(mFile.getPath()));

        imageView.setOnClickListener(v -> {
            if (!mHiddenControls) {
                hideSystemUI();
            } else {
                showSystemUI();
            }
            mHiddenControls = !mHiddenControls;
        });

        showSystemUI();
        startPostponedEnterTransition();

        return view;
    }

    public Dir getDir() {
        return mDir;
    }

    @Override
    protected BaseFsPresenter getPresenter() {
        return null;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void showSystemUI() {
        safeActivity(act -> {
            act.toolbar.animate()
                    .translationY(getStatusBarHeight())
                    .setDuration(150)
                    .start();
            act.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        });

    }

    private void hideSystemUI() {
        safeActivity(act -> {
            act.toolbar.animate()
                    .translationY(0)
                    .setDuration(150)
                    .start();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            }

            act.getWindow().getDecorView().setSystemUiVisibility(flags);
        });
    }
}
