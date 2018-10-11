package com.edwardstock.multipicker.picker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.internal.PickerSavePath;
import com.edwardstock.multipicker.internal.helpers.DisplayHelper;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.adapters.FilesAdapter;
import com.edwardstock.multipicker.picker.adapters.MediaFileDetailsLookup;
import com.edwardstock.multipicker.picker.adapters.MediaFileKeyProvider;
import com.edwardstock.multipicker.picker.views.FilesPresenter;
import com.edwardstock.multipicker.picker.views.FilesView;
import com.google.common.collect.ImmutableList;

import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class FilesFragment extends PickerFileSystemFragment implements FilesView {

    @InjectPresenter FilesPresenter presenter;

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

    public static FilesFragment newInstance(PickerConfig config, Dir dir) {
        Bundle args = new Bundle();
        args.putParcelable(PickerConst.EXTRA_DIR, dir);
        args.putParcelable(PickerConst.EXTRA_CONFIG, config);

        FilesFragment fragment = new FilesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setAdapter(final RecyclerView.Adapter<?> adapter) {
        Timber.d("Set adapter from fragment");
        boolean isTablet = getResources().getBoolean(R.bool.mp_isTablet);
        int spanCount = isTablet ? mConfig.getFileColumnsTablet() : mConfig.getFileColumns();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        setGridSpacingItemDecoration(list, spanCount);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        list.setItemAnimator(null);

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
                    public boolean canSetStateForKey(@NonNull MediaFile key, boolean nextState) {
                        if(mConfig.getLimit() < 0) {
                            return true;
                        }
                        return mSelectionTracker.getSelection().size() <= mConfig.getLimit();
                    }

                    @Override
                    public boolean canSetStateAtPosition(int position, boolean nextState) {
                        return true;
                    }

                    @Override
                    public boolean canSelectMultiple() {
                        return mConfig.getLimit() < 0;
                    }
                })
                .build();

        mSelectionTracker.addObserver(new SelectionTracker.SelectionObserver<MediaFile>() {
            @Override
            public void onItemStateChanged(@NonNull MediaFile key, boolean selected) {
                super.onItemStateChanged(key, selected);

                presenter.setSelection(ImmutableList.copyOf(mSelectionTracker.getSelection().iterator()));
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

                if(enabling) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectionTracker != null) {
            mSelectionTracker.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mSelectionTracker != null) {
            mSelectionTracker.onRestoreInstanceState(savedInstanceState);
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

        if (mSelectionTracker != null) {
            mSelectionTracker.onRestoreInstanceState(savedInstanceState);
        }

        startPostponedEnterTransition();

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


}
