package com.edwardstock.multipicker.picker.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.ActivityBuilder;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.internal.helpers.DisplayHelper;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.views.PickerPresenter;

import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_CONFIG;
import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_DIR;
import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_MEDIA_FILE;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class PreviewerActivity extends PickerActivity {

    private final static String STATE_LAST_PAGE = "STATE_LAST_PAGE";
    private final static String STATE_DIR = "STATE_DIR";
    private final static String STATE_MEDIA_FILE = "STATE_MEDIA_FILE";
    @BindView(R2.id.mp_preview_pager) ViewPager pager;
    @BindView(R2.id.mp_error_view) TextView errorView;
    private Dir mDir;
    private MediaFile mFile;
    private List<MediaFile> mFiles;
    private FragmentStatePagerAdapter mAdapter;
    private boolean mHiddenControls;
    private int mLastPage = -1;
    private WeakHashMap<Integer, PreviewPagerItem> mPages = new WeakHashMap<>();
    private int mPrevPage = 0;

    public Dir getDir() {
        return mDir;
    }

    public final void submitSelection(MediaFile mediaFile) {
        Intent result = new Intent();
        result.putExtra(PickerConst.EXTRA_MEDIA_FILE, mediaFile);
        setResult(RESULT_ADD_FILE_TO_SELECTION, result);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight() {
        if (!hasNavBar()) {
            return 0;
        }

        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void toggleUiVisibility() {
        if (mHiddenControls) {
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

        mHiddenControls = !mHiddenControls;
    }

    public boolean isShowingSystemUi() {
        return !mHiddenControls;
    }

    public boolean hasNavBar() {
        boolean hasPhysicalBackAndHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        hasPhysicalBackAndHomeKey = hasPhysicalBackAndHomeKey && KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        int id = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return !hasPhysicalBackAndHomeKey || (id > 0 && getResources().getBoolean(id)) || ViewConfiguration.get(this).hasPermanentMenuKey();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("Destroy previewer");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLastPage != -1) {
            outState.putInt(STATE_LAST_PAGE, mLastPage);
            outState.putParcelable(STATE_DIR, mDir);
            outState.putParcelable(STATE_MEDIA_FILE, mFile);
        }
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

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_LAST_PAGE)) {
                mLastPage = savedInstanceState.getInt(STATE_LAST_PAGE, -1);
            }

            if (savedInstanceState.containsKey(STATE_DIR)) {
                mDir = savedInstanceState.getParcelable(STATE_DIR);
            }

            if (savedInstanceState.containsKey(STATE_MEDIA_FILE)) {
                mFile = savedInstanceState.getParcelable(STATE_MEDIA_FILE);
            }
        }
        setTitle(mDir.getName());
        toolbar.setTitle(mDir.getName());

        new MediaFileLoader(this).loadDeviceImages(getConfig(), mDir, new MediaFileLoader.OnLoadListener() {
            @Override
            public void onFilesLoadFailed(Throwable t) {
                onError(t);
            }

            @Override
            public void onFilesLoadedSuccess(List<MediaFile> images, List<Dir> dirList) {
                runOnUiThread(() -> {
                    Timber.d("Files loaded: %d (%s)", images.size(), Thread.currentThread().getName());
                    mFiles = images;
                    initPager();
                });
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
                if (!mPages.containsKey(i)) {
                    mPages.put(i, PreviewPagerItem.newInstance(i, mFiles.get(i), mDir));
                }
                return mPages.get(i);
            }

            @Override
            public int getCount() {
                return mFiles.size();
            }
        };

        pager.setOffscreenPageLimit(0);
        pager.setPageMargin(DisplayHelper.dpToPx(this, 8));
        pager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        if (mLastPage == -1) {
            mLastPage = mFiles.indexOf(mFile);
            mPrevPage = mLastPage;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            pager.setCurrentItem(mLastPage, false);
        });
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mPrevPage = mLastPage;
                mLastPage = position;
                doubleCheckPage(mPrevPage, PreviewPagerItem::onPageInactive);
                doubleCheckPage(mLastPage, PreviewPagerItem::onPageActive);
                Timber.d("Set page %d inactive", mPrevPage);
                Timber.d("Set page %d active", mLastPage);
                toolbar.setSubtitle(String.format(Locale.getDefault(), "%d / %d", pager.getCurrentItem() + 1, mFiles.size()));
            }
        });

        toolbar.setSubtitle(String.format(Locale.getDefault(), "%d / %d", pager.getCurrentItem() + 1, mFiles.size()));
    }

    private void doubleCheckPage(int position, Consumer<PreviewPagerItem> pt) {
        if (mPages == null) {
            return;
        }
        if (!mPages.containsKey(position)) {
            return;
        }
        final PreviewPagerItem item = mPages.get(position);
        if (item == null) {
            return;
        }
        pt.onItem(item);
    }

    private interface Consumer<PT> {
        void onItem(PT pt);
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
