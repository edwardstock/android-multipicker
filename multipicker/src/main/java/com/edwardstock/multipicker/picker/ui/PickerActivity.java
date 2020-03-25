package com.edwardstock.multipicker.picker.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import com.edwardstock.multipicker.internal.PickerUtils;
import com.edwardstock.multipicker.internal.helpers.DisplayHelper;
import com.edwardstock.multipicker.internal.views.ErrorView;
import com.edwardstock.multipicker.internal.widgets.GridSpacingItemDecoration;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.views.PickerPresenter;
import com.edwardstock.multipicker.picker.views.PickerView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
public abstract class PickerActivity extends AppCompatActivity implements PickerView, ErrorView {
    public final static int RESULT_ADD_FILE_TO_SELECTION = Activity.RESULT_FIRST_USER + 1;
    private final static String STATE_SCANNED_FILES_FIRST_TIME = "STATE_SCANNED_FILES_FIRST_TIME";
    public @BindView(R2.id.toolbar) Toolbar toolbar;
    public @Nullable @BindView(R2.id.container_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    private PickerConfig mConfig;
    private GridSpacingItemDecoration mGridSpacingItemDecoration;
    protected static boolean scannedDirs = false;

    public PickerConfig getConfig() {
        if (mConfig == null) {
            final File in = new File(getCacheDir(), DirsActivity.CONFIG_FILE_NAME);

            try (RandomAccessFile raf = new RandomAccessFile(in, "r")) {
                byte[] cfgData = new byte[(int) in.length()];
                raf.readFully(cfgData);
                String cfgJson = new String(cfgData);
                Timber.d("Read json config: %s", cfgJson);
                mConfig = PickerConfig.fromJson(cfgJson);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return mConfig;
    }

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
    public void rescanFiles() {
        rescanFiles(null);
    }

    static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
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
        new FilesActivity.Builder(this, dir)
                .start();
    }

    public void updateFiles() {
        if (getPresenter() != null) {
            getPresenter().updateFiles(new MediaFileLoader(this));
        }
    }

    public final void submitResult(List<MediaFile> files) {
        Intent intent = new Intent();

        final File result = new File(getCacheDir(), PickerConst.RESULT_FILE_NAME);

        try (FileOutputStream fos = new FileOutputStream(result, false)) {
            final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            final String json = gson.toJson(files);
            fos.write(json.getBytes());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        intent.setData(Uri.fromFile(result));

        setResult(RESULT_OK, intent);
        finish();
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean deleteConfig() {
        final File in = new File(getCacheDir(), DirsActivity.CONFIG_FILE_NAME);
        if (in.exists() && in.canWrite()) {
            return in.delete();
        }

        return false;
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
    public void rescanFiles(OnCompleteScan listener) {
        new Thread(() -> {
            Timber.d("Scan files...");
            scanFilesIn(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
            scanFilesIn(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
            scanFilesIn(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                scanFilesIn(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
            }
            if (listener != null) {
                new Handler(Looper.getMainLooper()).post(listener::onComplete);
            }
            scannedDirs = true;
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == getConfig().getRequestCode() && resultCode == RESULT_OK) {
            setResult(resultCode, data);
            finish();
            return;
        }
        getPresenter().handleExtras(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (getPresenter() != null) {
            getPresenter().onRestoreSavedState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getPresenter() != null) {
            getPresenter().onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getPresenter() != null) {
            getPresenter().attachToLifecycle(this);
            getPresenter().onRestoreSavedState(savedInstanceState);
        }

        if (!scannedDirs) {
            rescanFiles();
        }

        setResult(RESULT_CANCELED);
    }

    private List<Pair<String, String>> iterateDirs(String pathName) {
        File folderFile = new File(pathName);
        File[] files = folderFile.listFiles();
        if (files != null) {
            List<Pair<String, String>> out = new ArrayList<>();
            for (File file : files) {
                // skip hidden files or directories
                if (file.getName().startsWith(".")) {
                    file = null;
                    continue;
                }

                // checking the File is file or directory
                if (file.isFile()) {
                    String mime = null;
                    String p = file.getAbsolutePath();
                    if (PickerUtils.isVideoFormat(p)) {
                        mime = PickerUtils.mimeFrom(p);
                    } else if (PickerUtils.isImageFormat(p)) {
                        mime = PickerUtils.mimeFrom(p);
                    }

                    if (mime != null) {
                        out.add(Pair.create(file.getAbsolutePath(), mime));
                    }
                    p = null;
                    file = null;
                    files = null;
                    folderFile = null;

                } else if (file.isDirectory()) {
                    out.addAll(iterateDirs(file.getAbsolutePath()));
                }
            }
            return out;
        }

        return Collections.emptyList();
    }

    private void scanFilesIn(String pathName) {
        List<Pair<String, String>> out = iterateDirs(pathName);
        List<List<Pair<String, String>>> chunks = chopped(out, 10);

        int pn = 0;
        for (List<Pair<String, String>> p : chunks) {
            String[] paths = new String[p.size()];
            String[] mimes = new String[p.size()];

            int i = 0;
            for (Pair<String, String> sp : p) {
                paths[i] = sp.first;
                mimes[i] = sp.second;
                i++;
            }

            MediaScannerConnection.scanFile(getApplicationContext(), paths, mimes, (path, uri) -> {
                Timber.d("Scanned: %s", path);
            });

            pn++;
        }
    }

    public interface OnCompleteScan {
        void onComplete();
    }

}
