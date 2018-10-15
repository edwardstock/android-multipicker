package com.edwardstock.multipicker.picker.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.AutoTransition;
import android.support.transition.ChangeBounds;
import android.support.transition.ChangeClipBounds;
import android.support.transition.ChangeImageTransform;
import android.support.transition.ChangeTransform;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.ActivityBuilder;
import com.edwardstock.multipicker.internal.CameraHandler;
import com.edwardstock.multipicker.internal.views.ErrorView;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.views.PickerPresenter;
import com.edwardstock.multipicker.picker.views.PickerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RuntimePermissions
public class PickerActivity extends AppCompatActivity implements PickerView, ErrorView {
    public @BindView(R2.id.toolbar) Toolbar toolbar;
    private PickerPresenter presenter;
    private PickerConfig mConfig;
    private PickerFileSystemFragment mFragment;

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
        mFragment.updateFiles();
    }

    @Override
    public void finishCaptureVideo(CameraHandler cameraHandler, Intent intent) {
        mFragment.updateFiles();
    }

    @Override
    public void scanMedia(MediaFile file, MediaScannerConnection.OnScanCompletedListener listener) {
        String[] paths = {file.getPath()};
        String[] mimeTypes = {(file.isVideo() ? "video/mp4" : "image/jpeg")};
        MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, listener);
    }

    @Override
    public void onError(CharSequence error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(Throwable t) {
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void startUpdateFiles() {
        Timber.d("Update files: Fragment: %s", mFragment.toString());
        mFragment.updateFiles();
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
            presenter.handleCapturePhoto();
            return true;
        } else if (item.getItemId() == R.id.menu_video) {
            presenter.handleCaptureVideo();
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

    @Override
    public void onBackPressed() {
        if (mFragment != null) {
            mFragment.onBackPressed();
        }

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        super.onBackPressed();
        resolveLastFragment();
    }

    public void startFiles(Dir dir) {
        FilesFragment fragment = FilesFragment.newInstance(getConfig(), dir);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mp_container, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(null)
                .commit();
        mFragment = fragment;
    }

    public void startPreview(Dir dir, MediaFile file, View sharedView) {
        if (file.isVideo()) {
            final Uri uri = Uri.parse(file.getPath());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setDataAndType(uri, "video/*");
            startActivity(intent);
        } else {
            ImageViewerFragment fragment = ImageViewerFragment.newInstance(dir, file);
            FragmentTransaction tx = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mp_container, fragment, fragment.getClass().getSimpleName());

            if (sharedView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tx.addSharedElement(sharedView, sharedView.getTransitionName());
                TransitionSet sharedSet = new TransitionSet();
                sharedSet.addTransition(new ChangeImageTransform());
                sharedSet.addTransition(new ChangeBounds());
                sharedSet.addTransition(new ChangeClipBounds());
                sharedSet.addTransition(new ChangeTransform());
                TransitionSet commonSet = new TransitionSet();
                commonSet.addTransition(new AutoTransition());

                fragment.setSharedElementEnterTransition(sharedSet);
                fragment.setSharedElementReturnTransition(sharedSet);

                fragment.setEnterTransition(commonSet);
                fragment.setReturnTransition(commonSet);
            }

            tx.addToBackStack(null).commit();
            mFragment = fragment;
        }
    }

    public final void submitResult(List<MediaFile> files) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES, new ArrayList<>(files));

        setResult(RESULT_OK, intent);
        finish();
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void capturePhoto(CameraHandler cameraHandler, int requestCode) {
        Intent intent = cameraHandler.getCameraPhotoIntent(this, getConfig(), mFragment.getCapturedSavePath());
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
    void captureVideo(CameraHandler cameraHandler, int requestCode) {
        Intent intent = cameraHandler.getCameraVideoIntent(this, getConfig(), mFragment.getCapturedSavePath());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new PickerPresenter();
        presenter.attachToLifecycle(this);
        presenter.attachView(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp_activity_picker);
        ButterKnife.bind(this);
        setResult(RESULT_CANCELED);

        setupToolbar(toolbar);

        mFragment = DirsFragment.newInstance(getConfig());

        if (getConfig().getTitle() != null) {
            toolbar.setTitle(getConfig().getTitle());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mp_container, mFragment, mFragment.getClass().getSimpleName())
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // get fragment presenter
        if (mFragment instanceof ImageViewerFragment) {
            getSupportFragmentManager().popBackStack();
        }
        resolveLastFragment();
        presenter.handleExtras(requestCode, resultCode, data);
        mFragment.onActivityResult(requestCode, resultCode, data);
    }

    private void resolveLastFragment() {
        int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (index >= 0) {
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
            String tag = backEntry.getName();
            if (tag != null) {
                mFragment = ((PickerFileSystemFragment) getSupportFragmentManager().findFragmentByTag(tag));
            }
        }
    }

    public static final class Builder extends ActivityBuilder {
        private PickerConfig mConfig;

        public Builder(@NonNull Activity from, PickerConfig config) {
            super(from);
            mConfig = config;
        }

        public Builder(@NonNull Fragment from, PickerConfig config) {
            super(from);
            mConfig = config;
        }

        @Override
        protected void onBeforeStart(Intent intent) {
            super.onBeforeStart(intent);
            intent.putExtra(PickerConst.EXTRA_CONFIG, mConfig);
        }

        @Override
        protected Class<?> getActivityClass() {
            return PickerActivity.class;
        }
    }

}
