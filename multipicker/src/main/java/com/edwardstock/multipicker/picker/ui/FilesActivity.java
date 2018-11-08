package com.edwardstock.multipicker.picker.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Surface;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.ActivityBuilder;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.internal.PickerSavePath;
import com.edwardstock.multipicker.internal.helpers.DisplayHelper;
import com.edwardstock.multipicker.picker.adapters.FilesAdapter;
import com.edwardstock.multipicker.picker.adapters.MediaFileDetailsLookup;
import com.edwardstock.multipicker.picker.adapters.MediaFileKeyProvider;
import com.edwardstock.multipicker.picker.views.FilesPresenter;
import com.edwardstock.multipicker.picker.views.FilesView;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_CONFIG;
import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_DIR;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class FilesActivity extends PickerActivity implements FilesView {

    private final static String LIST_STATE = "LIST_STATE";
    private final static String LAST_PREVIEW_STATE = "LAST_PREVIEW";
    FilesPresenter presenter;
    @BindView(R2.id.list) RecyclerView list;
    @BindView(R2.id.mp_selection_title) TextView selectionTitle;
    @BindView(R2.id.mp_selection_action_clear) ImageView selectionClearAction;
    @BindView(R2.id.mp_selection_action_done) ImageView selectionDoneAction;
    @BindView(R2.id.progress) ProgressBar progress;
    @BindView(R2.id.selection_root_sub) ConstraintLayout rootSub;
    private SelectionTracker<MediaFile> mSelectionTracker;
    private boolean mLastSelectionState = false;
    private Dir mDir;
    private PickerConfig mConfig;
    private List<MediaFile> mSelected = new ArrayList<>(10);
    private MediaFile mLastPreview = null;
    private Parcelable mListState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        presenter = new FilesPresenter();
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(LAST_PREVIEW_STATE)) {
            mLastPreview = savedInstanceState.getParcelable(LAST_PREVIEW_STATE);
        }

        setContentView(R.layout.mp_activity_filesystem);
        supportPostponeEnterTransition();
        ButterKnife.bind(this);
        setupToolbar(toolbar);
        if (getConfig().getTitle() != null) {
            toolbar.setTitle(getConfig().getTitle());
        }
        presenter.attachView(this);
        presenter.onRestoreSavedState(savedInstanceState);

        mDir = getIntent().getParcelableExtra(EXTRA_DIR);
        mConfig = getIntent().getParcelableExtra(EXTRA_CONFIG);
        presenter.handleExtras(getIntent().getExtras());
        presenter.updateFiles(new MediaFileLoader(this));

        if (mLastPreview != null) {
            presenter.setOnFileMeasuredListener(mLastPreview, mf -> {
                supportStartPostponedEnterTransition();
                Timber.d("Start transition after load preview");
            });
        } else {
            Timber.w("Last preview is null");
            supportStartPostponedEnterTransition();
        }

        if (mSelectionTracker != null) {
            mSelectionTracker.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void setAdapter(final RecyclerView.Adapter<?> adapter) {
        Timber.d("Set adapter from fragment");
        boolean isTablet = getResources().getBoolean(R.bool.mp_isTablet);
        int spanCount;

        int rot = getWindowManager().getDefaultDisplay().getRotation();
        if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_180) {
            spanCount = (isTablet ? mConfig.getFileColumnsTablet() : mConfig.getFileColumns()) + 1;
        } else {
            spanCount = isTablet ? mConfig.getFileColumnsTablet() : mConfig.getFileColumns();
        }


        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        setGridSpacingItemDecoration(list, spanCount);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        list.setItemAnimator(null);
        layoutManager.onRestoreInstanceState(mListState);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    ((FilesAdapter) adapter).setIsScrolling(false);
                } else {
                    ((FilesAdapter) adapter).setIsScrolling(true);
                }
            }
        });

        FilesAdapter filesAdapter = ((FilesAdapter) adapter);

        if (mSelectionTracker != null) {
            Timber.d("Reusing tracker");
            filesAdapter.setSelectionTracker(mSelectionTracker);
            return;
        }
        Timber.d("Create tracker");
        mSelectionTracker = new SelectionTracker.Builder<>(
                "picker",
                list,
                new MediaFileKeyProvider(() -> {
                    if (list.getAdapter() == null) {
                        return Collections.emptyList();
                    }
                    return ((FilesAdapter) list.getAdapter()).getItems();
                }, ItemKeyProvider.SCOPE_MAPPED),
                new MediaFileDetailsLookup(list),
                StorageStrategy.createParcelableStorage(MediaFile.class)
        )
                .withSelectionPredicate(new SelectionTracker.SelectionPredicate<MediaFile>() {
                    @Override
                    public boolean canSetStateForKey(@NonNull MediaFile mediaFile, boolean nextState) {
                        return mConfig.getLimit() <= 0 || (mSelected.size() != mConfig.getLimit() || mSelected.contains(mediaFile));
                    }

                    @Override
                    public boolean canSetStateAtPosition(int position, boolean nextState) {
                        return true;
                    }

                    @Override
                    public boolean canSelectMultiple() {
                        return true;
                    }
                })
                .build();

        mSelectionTracker.addObserver(new SelectionTracker.SelectionObserver<MediaFile>() {
            @Override
            public void onItemStateChanged(@NonNull MediaFile key, boolean selected) {
                super.onItemStateChanged(key, selected);
                presenter.setSelection(ImmutableList.copyOf(mSelectionTracker.getSelection().iterator()));
                mSelected.clear();
                mSelected.addAll(ImmutableList.copyOf(mSelectionTracker.getSelection().iterator()));
                setSubtitle();
                final boolean hasSelection = mSelectionTracker.hasSelection();
                if (mLastSelectionState != hasSelection) {
                    mLastSelectionState = hasSelection;
                    filesAdapter.notifyItemRangeChanged(0, adapter.getItemCount());
                }
            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
            }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
                final boolean hasSelection = mSelectionTracker.hasSelection();
                presenter.setSelection(ImmutableList.copyOf(mSelectionTracker.getSelection().iterator()));

                if (mLastSelectionState != hasSelection) {
                    mLastSelectionState = hasSelection;
                    filesAdapter.notifyItemRangeChanged(0, adapter.getItemCount());

                }

            }
        });

        filesAdapter.setSelectionTracker(mSelectionTracker);
    }

    @Override
    public void setSelectionTitle(CharSequence title) {
        selectionTitle.setText(title);
    }

    @Override
    public void setOnSelectionClearListener(View.OnClickListener listener) {
        selectionClearAction.setOnClickListener(listener);
    }

    @Override
    public void setOnSelectionDoneListener(View.OnClickListener listener) {
        selectionDoneAction.setOnClickListener(listener);
    }

    @Override
    public void setSelectionObserver(SelectionTracker.SelectionObserver<MediaFile> observer) {
        mSelectionTracker.addObserver(observer);
    }

    @Override
    public void showEmpty() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode ==getConfig().getRequestCode() && resultCode == RESULT_ADD_FILE_TO_SELECTION) {
            presenter.onActivityResult(requestCode, resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void startPreview(MediaFile file, View sharedView) {
        mLastPreview = file;
        PreviewerActivity.Builder pb = new PreviewerActivity.Builder(this, getConfig(), mDir, file);
        pb.start(getConfig().getRequestCode());
    }

    @Override
    public void clearSelection() {
        if (mSelectionTracker != null) {
            mSelectionTracker.clearSelection();
            mLastSelectionState = false;
        }
    }

    @Override
    public void finishWithResult() {
        submitResult(ImmutableList.copyOf(mSelectionTracker.getSelection()));
    }

    @Override
    public void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progress.setVisibility(View.GONE);
    }

    @Override
    public void setSelectionSubmitEnabled(boolean enabling) {
        runOnUiThread(() -> {
            Timber.d("Set selection submit: %b", enabling);
            ConstraintSet set = new ConstraintSet();
            set.clone(rootSub);
            set.setAlpha(R.id.mp_selection_action_clear, enabling ? 1f : 0f);
            set.setAlpha(R.id.mp_selection_action_done, enabling ? 1f : 0f);
            set.setMargin(R.id.mp_selection_title, ConstraintSet.START, enabling ? DisplayHelper.dpToPx(this, 48) : DisplayHelper.dpToPx(this, 16));
            TransitionSet transitionSet = new TransitionSet();
            Transition shortTransition = new AutoTransition();
            shortTransition.setInterpolator(new DecelerateInterpolator());
//                shortTransition.setDuration(300);
            Transition longTransition = new AutoTransition();
            longTransition.setInterpolator(new AccelerateDecelerateInterpolator());
//                longTransition.setDuration(600);
            transitionSet.addTransition(shortTransition);
            transitionSet.addTransition(longTransition);
            transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);

            if (enabling) {
                longTransition.addTarget(R.id.mp_selection_action_clear);
                longTransition.addTarget(R.id.mp_selection_action_done);
                shortTransition.addTarget(R.id.mp_selection_title);
            } else {
                longTransition.addTarget(R.id.mp_selection_title);
                shortTransition.addTarget(R.id.mp_selection_action_clear);
                shortTransition.addTarget(R.id.mp_selection_action_done);
            }

            TransitionManager.beginDelayedTransition(rootSub, transitionSet);
            set.applyTo(rootSub);
        });
    }

    @Override
    public void scrollTo(int position) {
//        if(list != null) {
//            list.post(()->{
//                list.smoothScrollToPosition(position);
//            });
//        }
    }

    @Override
    public void removeFileFromMediaDB(File file) {
        Timber.d("Remove file: %s as it doesn't exists", file.getAbsoluteFile());
        try {
            getContentResolver().delete(Uri.fromFile(file), null, null);
            file.delete();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void selectFile(MediaFile mediaFile) {
        if(mSelectionTracker != null && mediaFile != null) {
            mSelectionTracker.select(mediaFile);
        }
    }

    public void addSelection(MediaFile selection) {
        if (selection != null && mSelectionTracker != null) {
            mSelectionTracker.select(selection);
        }
    }

    @Override
    public void startUpdateFiles() {
        presenter.updateFiles(new MediaFileLoader(this));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (list != null && list.getLayoutManager() != null) {
            mListState = list.getLayoutManager().onSaveInstanceState();
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mSelectionTracker != null) {
            mSelectionTracker.onSaveInstanceState(outState);
        }
        if (presenter != null) {
            presenter.onSaveInstanceState(outState);
        }

        if (mLastPreview != null) {
            outState.putParcelable(LAST_PREVIEW_STATE, mLastPreview);
        }


        if (list.getLayoutManager() != null) {
            mListState = list.getLayoutManager().onSaveInstanceState();
            outState.putParcelable(LIST_STATE, mListState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        toolbar.setSubtitle(null);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (list != null && list.getLayoutManager() != null) {
            list.getLayoutManager().onRestoreInstanceState(mListState);
        }
        if (mConfig.getLimit() > 0) {
            setSubtitle();
        }
    }

    @Override
    public FilesPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(LAST_PREVIEW_STATE)) {
            mLastPreview = savedInstanceState.getParcelable(LAST_PREVIEW_STATE);
        }

        if (mSelectionTracker != null && savedInstanceState != null) {
            mSelectionTracker.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onDestroy() {
        Timber.d("Delete selection tracker");
        mSelectionTracker = null;
        super.onDestroy();
    }

    @Override
    protected PickerSavePath getCapturedSavePath() {
        if (mDir != null) {
            return new PickerSavePath(mDir.getName());
        }
        return super.getCapturedSavePath();
    }

    private void setSubtitle() {
        if (mConfig.getLimit() > 0) {
            toolbar.setSubtitle(String.format(Locale.getDefault(), "%d / %d", mSelectionTracker.getSelection().size(), mConfig.getLimit()));
        }
    }

    static final class Builder extends ActivityBuilder {
        private final PickerConfig mConfig;
        private final Dir mDir;

        public Builder(@NonNull Activity from, PickerConfig config, Dir dir) {
            super(from);
            mConfig = config;
            mDir = dir;
        }

        @Override
        protected void onBeforeStart(Intent intent) {
            super.onBeforeStart(intent);
            intent.putExtra(EXTRA_DIR, mDir);
            intent.putExtra(EXTRA_CONFIG, mConfig);
        }

        @Override
        protected Class<?> getActivityClass() {
            return FilesActivity.class;
        }
    }


}
