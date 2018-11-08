package com.edwardstock.multipicker.picker.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.internal.ActivityBuilder;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.picker.views.DirsPresenter;
import com.edwardstock.multipicker.picker.views.DirsView;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.edwardstock.multipicker.picker.PickerConst.EXTRA_CONFIG;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RuntimePermissions
public class DirsActivity extends PickerActivity implements DirsView {

    DirsPresenter presenter;
    @BindView(R2.id.list) RecyclerView list;
    @BindView(R2.id.mp_selection_title) TextView selectionTitle;
    @BindView(R2.id.mp_selection_action_clear) ImageView selectionClearAction;
    @BindView(R2.id.mp_selection_action_done) ImageView selectionDoneAction;
    @BindView(R2.id.mp_empty_text) TextView emptyText;
    @BindView(R2.id.selection_root) View selectionRoot;
    @BindView(R2.id.progress) ProgressBar progress;
    private PickerConfig mConfig;

    @Override
    public void setAdapter(final RecyclerView.Adapter<?> adapter) {
        boolean isTablet = getResources().getBoolean(R.bool.mp_isTablet);
        int spanCount = isTablet ? mConfig.getDirColumnsTablet() : mConfig.getDirColumns();

        int rot = getWindowManager().getDefaultDisplay().getRotation();
        if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_180) {
            spanCount = (isTablet ? mConfig.getDirColumnsTablet() : mConfig.getDirColumns()) + 1;
        } else {
            spanCount = isTablet ? mConfig.getDirColumnsTablet() : mConfig.getDirColumns();
        }


        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        setGridSpacingItemDecoration(list, spanCount);
        list.setLayoutManager(layoutManager);
        list.setHasFixedSize(true);
        list.setAdapter(adapter);
    }

    @Override
    public void startFiles(Dir dir) {
        new FilesActivity.Builder(this, getConfig(), dir)
                .start(getConfig().getRequestCode());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
    public void showEmpty(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DirsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void startUpdateFiles() {
        presenter.updateFiles(new MediaFileLoader(this));
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void setLoader() {
        presenter.updateFiles(new MediaFileLoader(this));
    }

    @Override
    public DirsPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new DirsPresenter();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_activity_filesystem);
        ButterKnife.bind(this);
        setupToolbar(toolbar);
        if (getConfig().getTitle() != null) {
            toolbar.setTitle(getConfig().getTitle());
        }
        presenter.attachView(this);
        selectionRoot.setVisibility(View.GONE);
        mConfig = getIntent().getParcelableExtra(EXTRA_CONFIG);

        presenter.handleExtras(getIntent().getExtras());

        DirsActivityPermissionsDispatcher.setLoaderWithPermissionCheck(this);
    }

    public static final class Builder extends ActivityBuilder {
        private PickerConfig mConfig;

        public Builder(@NonNull Activity from, PickerConfig config) {
            super(from);
            mConfig = config;
        }

        @Override
        protected void onBeforeStart(Intent intent) {
            super.onBeforeStart(intent);
            intent.putExtra(EXTRA_CONFIG, mConfig);
        }

        @Override
        protected Class<?> getActivityClass() {
            return DirsActivity.class;
        }
    }


}
