package com.edwardstock.multipicker.picker.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edwardstock.multipicker.BuildConfig;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.internal.ActivityBuilder;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.picker.views.DirsPresenter;
import com.edwardstock.multipicker.picker.views.DirsView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RuntimePermissions
public class DirsActivity extends PickerActivity implements DirsView {

    final static String CONFIG_FILE_NAME = BuildConfig.APPLICATION_ID + ".config.cfg";
    DirsPresenter presenter;
    @BindView(R2.id.list) RecyclerView list;
    @BindView(R2.id.mp_selection_title) TextView selectionTitle;
    @BindView(R2.id.mp_selection_action_clear) ImageView selectionClearAction;
    @BindView(R2.id.mp_selection_action_done) ImageView selectionDoneAction;
    @BindView(R2.id.mp_empty_text) TextView emptyText;
    @BindView(R2.id.selection_root) View selectionRoot;
    @BindView(R2.id.progress) ProgressBar progress;
    @BindView(R2.id.mp_grant_permission) Button buttonGrantPermissions;

    @Override
    public void setAdapter(final RecyclerView.Adapter<?> adapter) {
        boolean isTablet = getResources().getBoolean(R.bool.mp_isTablet);
        int spanCount;

        int rot = getWindowManager().getDefaultDisplay().getRotation();
        if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_180) {
            spanCount = (isTablet ? getConfig().getDirColumnsTablet() : getConfig().getDirColumns()) + 1;
        } else {
            spanCount = isTablet ? getConfig().getDirColumnsTablet() : getConfig().getDirColumns();
        }


        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        setGridSpacingItemDecoration(list, spanCount);
        list.setLayoutManager(layoutManager);
        list.setHasFixedSize(true);
        list.setAdapter(adapter);
    }

    @Override
    public void startFiles(Dir dir) {
        new FilesActivity.Builder(this, dir)
                .start(getConfig().getRequestCode());
    }

    @Override
    public void showProgress() {
        runOnUiThread(() -> progress.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideProgress() {
        runOnUiThread(() -> progress.setVisibility(View.GONE));
    }

    @Override
    public void showEmpty(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    @Override
    public void showRefreshProgress() {
        swipeRefreshLayout.setRefreshing(true);
        hideProgress();
    }

    @Override
    public void hideRefreshProgress() {
        swipeRefreshLayout.setRefreshing(false);
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

    @Override
    public DirsPresenter getPresenter() {
        return presenter;
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void setLoader() {
        buttonGrantPermissions.setVisibility(View.GONE);
        presenter.updateFiles(new MediaFileLoader(this));
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForReadWrite(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.mp_permission_storage_rationale)
                .setPositiveButton(R.string.mp_button_allow, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.mp_button_deny, (dialog, button) -> request.cancel())
                .show();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showDeniedForReadWrite() {
        buttonGrantPermissions.setVisibility(View.VISIBLE);
        buttonGrantPermissions.setOnClickListener(v -> {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getConfig().getApplicationId(), null);
                intent.setData(uri);
                startActivity(intent);
            } catch (Throwable ignore) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.mp_permission_grant_failed)
                        .setPositiveButton(R.string.mp_button_allow, (dialog, button) -> dialog.dismiss())
                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

        presenter.setConfig(getConfig());

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
            if (getActivity() == null) {
                throw new IllegalStateException("Activity can't be null! Please, sure that you have passed activity or fragment to Builder constructor.");
            }

            if (mConfig.getApplicationId() == null) {
                mConfig.applicationId(getActivity().getPackageName());
            }
            scannedDirs = false;
            final File tmp = getActivity().getCacheDir();
            final File out = new File(tmp, CONFIG_FILE_NAME);

            try (FileOutputStream os = new FileOutputStream(out, false)) {
                os.write(mConfig.toJson().getBytes());
            } catch (FileNotFoundException e) {
                Timber.e(e);
            } catch (IOException e) {
                Timber.e(e);
            }

        }

        @Override
        protected Class<?> getActivityClass() {
            return DirsActivity.class;
        }
    }


}
