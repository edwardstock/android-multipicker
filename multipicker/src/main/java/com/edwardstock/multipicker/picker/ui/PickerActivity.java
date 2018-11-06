package com.edwardstock.multipicker.picker.ui;

import android.Manifest;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.CameraHandler;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.internal.PickerSavePath;
import com.edwardstock.multipicker.internal.helpers.DisplayHelper;
import com.edwardstock.multipicker.internal.views.ErrorView;
import com.edwardstock.multipicker.internal.widgets.GridSpacingItemDecoration;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.views.PickerPresenter;
import com.edwardstock.multipicker.picker.views.PickerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RuntimePermissions
abstract class PickerActivity extends AppCompatActivity implements PickerView, ErrorView {
    private final static String STATE_SCANNED_FILES_FIRST_TIME = "STATE_SCANNED_FILES_FIRST_TIME";
    public @BindView(R2.id.toolbar) Toolbar toolbar;
    private PickerConfig mConfig;
    private GridSpacingItemDecoration mGridSpacingItemDecoration;
    private boolean mScannedDirs = false;

    @Override
    public void capturePhotoWithPermissions(CameraHandler cameraHandler, int requestCode) {
        PickerActivityPermissionsDispatcher.capturePhotoWithPermissionCheck(this, cameraHandler, requestCode);
    }

    @Override
    public void captureVideoWithPermissions(CameraHandler cameraHandler, int requestCode) {
        PickerActivityPermissionsDispatcher.captureVideoWithPermissionCheck(this, cameraHandler, requestCode);
    }

    @Override
    public void finishCapturePhoto(CameraHandler cameraHandler, Intent intent) {
//        mFragment.updateFiles();
    }

    @Override
    public void finishCaptureVideo(CameraHandler cameraHandler, Intent intent) {
//        mFragment.updateFiles();
    }

    @Override
    public void scanMedia(MediaFile file, MediaScannerConnection.OnScanCompletedListener listener) {
        String[] paths = {file.getPath()};
        String[] mimeTypes = {(file.isVideo() ? "video/mp4" : "image/jpeg")};
        MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, listener);
    }

    @Override
    public void startUpdateFiles() {
        getPresenter().updateFiles(new MediaFileLoader(this));
    }

    @Override
    public void onError(CharSequence error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(Throwable t) {
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
    }

    public void setupToolbar(@NonNull final Toolbar toolbar) {
        checkNotNull(toolbar, "Toolbar can't be null!");
        setSupportActionBar(toolbar);

        assert (getSupportActionBar() != null);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public PickerConfig getConfig() {
        if (mConfig == null && getIntent().hasExtra(PickerConst.EXTRA_CONFIG)) {
            mConfig = getIntent().getParcelableExtra(PickerConst.EXTRA_CONFIG);
        }
        return mConfig;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.menu_camera) {
            getPresenter().handleCapturePhoto();
            return true;
        } else if (item.getItemId() == R.id.menu_video) {
            getPresenter().handleCaptureVideo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getConfig() == null) {
            return true;
        }

        if (getConfig().isEnableCamera()) {
            if (getConfig().isShowPhotos() && getConfig().isShowVideos()) {
                getMenuInflater().inflate(R.menu.mp_image_picker_menu_all_main, menu);
            } else if (getConfig().isShowVideos()) {
                getMenuInflater().inflate(R.menu.mp_image_picker_menu_video_main, menu);
            } else if (getConfig().isShowPhotos()) {
                getMenuInflater().inflate(R.menu.mp_image_picker_menu_photo_main, menu);
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PickerActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void startFiles(Dir dir) {
        new FilesActivity.Builder(this, getConfig(), dir)
                .start();
    }

    public void updateFiles() {
        if (getPresenter() != null) {
            getPresenter().updateFiles(new MediaFileLoader(this));
        }
    }

    public final void submitResult(List<MediaFile> files) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES, new ArrayList<>(files));

        setResult(RESULT_OK, intent);
        finish();
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void capturePhoto(CameraHandler cameraHandler, int requestCode) {
        Intent intent = cameraHandler.getCameraPhotoIntent(this, getConfig(), getCapturedSavePath());
        if (intent == null) {
            Toast.makeText(this, R.string.mp_error_create_image_file, Toast.LENGTH_LONG).show();
            return;
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        } else {
            Toast.makeText(this, R.string.mp_error_create_image_file, Toast.LENGTH_LONG).show();
        }
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void captureVideo(CameraHandler cameraHandler, int requestCode) {
        Intent intent = cameraHandler.getCameraVideoIntent(this, getConfig(), getCapturedSavePath());
        if (intent == null) {
            Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show();
            return;
        }
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        } else {
            Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show();
        }
    }

    protected void setGridSpacingItemDecoration(RecyclerView list, int spanCount) {
        Timber.d("Set grid spacing");
        if (mGridSpacingItemDecoration != null) {
            list.removeItemDecoration(mGridSpacingItemDecoration);
        }
        mGridSpacingItemDecoration = new GridSpacingItemDecoration(spanCount, DisplayHelper.dpToPx(this, 1), false);
        list.addItemDecoration(mGridSpacingItemDecoration);
    }

    protected PickerSavePath getCapturedSavePath() {
        return null;
    }

    abstract protected PickerPresenter getPresenter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getPresenter() != null) {
            getPresenter().attachToLifecycle(this);
            getPresenter().onRestoreSavedState(savedInstanceState);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SCANNED_FILES_FIRST_TIME)) {
            mScannedDirs = savedInstanceState.getBoolean(STATE_SCANNED_FILES_FIRST_TIME);
        }

        if (!mScannedDirs) {
            new Thread(() -> {
                Timber.d("Scan files...");
                checkFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                checkFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
                checkFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            }).start();
        }


        setResult(RESULT_CANCELED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // get fragment presenter
        getPresenter().handleExtras(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (getPresenter() != null) {
            getPresenter().onRestoreSavedState(savedInstanceState);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SCANNED_FILES_FIRST_TIME)) {
            mScannedDirs = savedInstanceState.getBoolean(STATE_SCANNED_FILES_FIRST_TIME);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getPresenter() != null) {
            getPresenter().onSaveInstanceState(outState);
        }
        outState.putBoolean(STATE_SCANNED_FILES_FIRST_TIME, mScannedDirs);
    }

    private void checkFiles(String pathName) {
        File folderFile = new File(pathName);
        File[] files = folderFile.listFiles();
        if (files != null) {
            for (File file : files) {
                // skip hidden files or directories
                if (file.getName().startsWith(".")) {
                    continue;
                }

                // checking the File is file or directory
                if (file.isFile()) {
                    String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1);
                    String mime = null;
                    if (extension.equalsIgnoreCase("mp4")) {
                        mime = "video/mp4";
                    } else if (extension.equalsIgnoreCase("jpg")) {
                        mime = "image/jpeg";
                    }
                    if (mime != null) {
//                        Timber.d("Scan file: %s", file.getAbsolutePath());
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()}, new String[]{mime}, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
//                                Timber.d("Scan file: %s (%s) - completed", path, uri.toString());
                            }
                        });
                    }

                } else if (file.isDirectory()) {
                    checkFiles(file.getAbsolutePath());
                }
            }
        }
    }

}
