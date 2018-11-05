package com.edwardstock.multipicker.picker.ui;

import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
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
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.internal.PickerSavePath;
import com.edwardstock.multipicker.internal.helpers.DisplayHelper;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.adapters.FilesAdapter;
import com.edwardstock.multipicker.picker.adapters.MediaFileDetailsLookup;
import com.edwardstock.multipicker.picker.adapters.MediaFileKeyProvider;
import com.edwardstock.multipicker.picker.views.FilesPresenter;
import com.edwardstock.multipicker.picker.views.FilesView;
import com.google.common.collect.ImmutableList;

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

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class FilesFragment extends PickerFileSystemFragment implements FilesView {

    private final static String LIST_STATE = "LIST_STATE";
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

    public static FilesFragment newInstance(PickerConfig config, Dir dir) {
        Bundle args = new Bundle();
        args.putParcelable(PickerConst.EXTRA_DIR, dir);
        args.putParcelable(PickerConst.EXTRA_CONFIG, config);

        FilesFragment fragment = new FilesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        if (presenter != null) {
            presenter.onRestoreSavedState(savedInstanceState);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = new FilesPresenter();
        presenter.attachToLifecycle(this);
        presenter.attachView(this);
    }


    @Override
    public void setAdapter(final RecyclerView.Adapter<?> adapter) {
        Timber.d("Set adapter from fragment");
        boolean isTablet = getResources().getBoolean(R.bool.mp_isTablet);
        int spanCount = isTablet ? mConfig.getFileColumnsTablet() : mConfig.getFileColumns();
        if (getActivity() != null) {
            int rot = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_180) {
                spanCount = (isTablet ? mConfig.getFileColumnsTablet() : mConfig.getFileColumns()) + 1;
            } else {
                spanCount = isTablet ? mConfig.getFileColumnsTablet() : mConfig.getFileColumns();
            }
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
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
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("Delete selection tracker");
        mSelectionTracker = null;
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
    public void startPreview(MediaFile file, View sharedView) {
        mLastPreview = file;
        safeActivity(act -> {
            act.startPreview(mDir, file, sharedView);
        });
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
        safeActivity(act -> act.submitResult(ImmutableList.copyOf(mSelectionTracker.getSelection())));
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
        safeActivity(act -> {
            act.runOnUiThread(() -> {
                Timber.d("Set selection submit: %b", enabling);
                ConstraintSet set = new ConstraintSet();
                set.clone(rootSub);
                set.setAlpha(R.id.mp_selection_action_clear, enabling ? 1f : 0f);
                set.setAlpha(R.id.mp_selection_action_done, enabling ? 1f : 0f);
                set.setMargin(R.id.mp_selection_title, ConstraintSet.START, enabling ? DisplayHelper.dpToPx(getContext(), 48) : DisplayHelper.dpToPx(getContext(), 16));
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

    public void addSelection(MediaFile selection) {
        if (selection != null && mSelectionTracker != null) {
            mSelectionTracker.select(selection);
        }
    }

    @Override
    public void onError(CharSequence error) {
        safeActivity(act -> act.onError(error));
    }

    @Override
    public void onError(Throwable t) {
        safeActivity(act -> act.onError(t));
    }

    @Override
    public void startUpdateFiles() {
        presenter.updateFiles(new MediaFileLoader(getContext()));
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

        if (list.getLayoutManager() != null) {
            mListState = list.getLayoutManager().onSaveInstanceState();
            outState.putParcelable(LIST_STATE, mListState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (mConfig.getLimit() > 0) {
            safeActivity(act -> {
                act.toolbar.setSubtitle(null);
            });
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        list.getLayoutManager().onRestoreInstanceState(mListState);
        if (mConfig.getLimit() > 0) {
            setSubtitle();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mp_fragment_filesystem, container, false);
        postponeEnterTransition();
        ButterKnife.bind(this, view);
        mDir = getArguments().getParcelable(PickerConst.EXTRA_DIR);
        mConfig = getArguments().getParcelable(PickerConst.EXTRA_CONFIG);
        presenter.handleExtras(getArguments());
        presenter.updateFiles(new MediaFileLoader(getContext()));

        presenter.setOnFileMeasuredListener(mLastPreview, mf -> startPostponedEnterTransition());

        if (mSelectionTracker != null) {
            mSelectionTracker.onRestoreInstanceState(savedInstanceState);
        }


        return view;
    }

    @Override
    public FilesPresenter getPresenter() {
        return presenter;
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
            safeActivity(act -> {
                act.toolbar.setSubtitle(String.format(Locale.getDefault(), "%d / %d", mSelectionTracker.getSelection().size(), mConfig.getLimit()));
            });
        }
    }


}
