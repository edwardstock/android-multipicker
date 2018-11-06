package com.edwardstock.multipicker.picker.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.ActivityBuilder;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.picker.views.PickerPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_CONFIG;
import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_DIR;
import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_MEDIA_FILE;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class PreviewerActivity extends PickerActivity {

    @BindView(R2.id.mp_preview_pager) ViewPager pager;
    @BindView(R2.id.mp_error_view) TextView errorView;

    private Dir mDir;
    private MediaFile mFile;
    private List<MediaFile> mFiles;
    private FragmentStatePagerAdapter mAdapter;
    private boolean mHiddenControls;
    private List<SystemUiVisibilityListener> mSystemUiVisibilityListeners = new ArrayList<>();

    public Dir getDir() {
        return mDir;
    }

    @Override
    public void onError(Throwable t) {
        super.onError(t);
        errorView.setText(t.getMessage());
    }

    @Override
    public void onError(CharSequence error) {
        super.onError(error);
        errorView.setText(error);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    public void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        decorView.setSystemUiVisibility(flags);

        Stream.of(mSystemUiVisibilityListeners)
                .forEach(item -> item.onChangeVisibleState(false));
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    public void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Stream.of(mSystemUiVisibilityListeners)
                .forEach(item -> item.onChangeVisibleState(true));
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void toggleUiVisibility() {
        if (!mHiddenControls) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
        mHiddenControls = !mHiddenControls;
    }

    public void addVisibilityListener(SystemUiVisibilityListener listener) {
        mSystemUiVisibilityListeners.add(listener);
        listener.onChangeVisibleState(!mHiddenControls);
    }

    public void removeVisibilityListener(SystemUiVisibilityListener listener) {
        mSystemUiVisibilityListeners.remove(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_activity_previewer);
        ButterKnife.bind(this);
        setupToolbar(toolbar);
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.mp_white));

        mDir = getIntent().getParcelableExtra(EXTRA_DIR);
        mFile = getIntent().getParcelableExtra(EXTRA_MEDIA_FILE);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.VISIBLE) {
                toolbar.startAnimation(AnimationUtils.loadAnimation(PreviewerActivity.this, R.anim.slide_down));
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
            } else {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                toolbar.startAnimation(AnimationUtils.loadAnimation(PreviewerActivity.this, R.anim.slide_up));
            }
        });

        showSystemUI();
        final ViewGroup.MarginLayoutParams lp = ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams());
        lp.topMargin = getStatusBarHeight();
        toolbar.setLayoutParams(lp);

        new MediaFileLoader(this).loadDeviceImages(getConfig(), mDir, new MediaFileLoader.OnLoadListener() {
            @Override
            public void onFilesLoadFailed(Throwable t) {
                onError(t);
            }

            @Override
            public void onFilesLoadedSuccess(List<MediaFile> images, List<Dir> dirList) {
                mFiles = images;
                initPager();
            }
        });
    }

    @Override
    protected PickerPresenter getPresenter() {
        return null;
    }

    private void initPager() {
        mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return PreviewPagerItem.newInstance(mFiles.get(i), mDir);
            }

            @Override
            public int getCount() {
                return mFiles.size();
            }
        };

        pager.setAdapter(mAdapter);
        pager.setCurrentItem(mFiles.indexOf(mFile));
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                toolbar.setSubtitle(String.format(Locale.getDefault(), "%d / %d", pager.getCurrentItem() + 1, mFiles.size()));

            }
        });
        toolbar.setTitle(mDir.getName());
        toolbar.setSubtitle(String.format(Locale.getDefault(), "%d / %d", pager.getCurrentItem() + 1, mFiles.size()));

    }

    public interface SystemUiVisibilityListener {
        void onChangeVisibleState(boolean visibleNow);
    }

    static final class Builder extends ActivityBuilder {
        private final Dir mDir;
        private final PickerConfig mConfig;
        private final MediaFile mFile;

        public Builder(@NonNull Activity from, PickerConfig config, Dir dir, MediaFile file) {
            super(from);
            mConfig = config;
            mDir = dir;
            mFile = file;
        }

        @Override
        protected void onBeforeStart(Intent intent) {
            super.onBeforeStart(intent);
            intent.putExtra(EXTRA_CONFIG, mConfig);
            intent.putExtra(EXTRA_MEDIA_FILE, mFile);
            intent.putExtra(EXTRA_DIR, mDir);
        }

        @Override
        protected Class<?> getActivityClass() {
            return PreviewerActivity.class;
        }
    }

}
