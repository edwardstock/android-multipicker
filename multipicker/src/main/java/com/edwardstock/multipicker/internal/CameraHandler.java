package com.edwardstock.multipicker.internal;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.RestrictTo;
import android.support.v4.content.FileProvider;

import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.data.MediaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RestrictTo({RestrictTo.Scope.LIBRARY})
public final class CameraHandler {

    private String mCurrentMediaPath;

    static List<MediaFile> singleListFromPath(String path) {
        List<MediaFile> images = new ArrayList<>();
        images.add(new MediaFile(0, PickerUtils.getNameFromFilePath(path), path));
        return images;
    }

    public Intent getCameraPhotoIntent(Context context, PickerConfig config, PickerSavePath savePath) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = PickerUtils.createImageFile(firstNonNull(savePath, config.getPhotoSavePath()));
        if (imageFile != null) {
            Context appContext = context.getApplicationContext();

            Uri uri = FileProvider.getUriForFile(appContext, getProviderName(context), imageFile);
            mCurrentMediaPath = "file:" + imageFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            PickerUtils.grantAppPermission(context, intent, uri);

            return intent;
        }
        return null;
    }

    public Intent getCameraVideoIntent(Context context, PickerConfig config, PickerSavePath savePath) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File videoFile = PickerUtils.createVideoFile(firstNonNull(savePath, config.getVideoSavePath()));
        if (videoFile != null) {
            Context appContext = context.getApplicationContext();
            Uri uri = FileProvider.getUriForFile(appContext, getProviderName(context), videoFile);
            mCurrentMediaPath = "file:" + videoFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            PickerUtils.grantAppPermission(context, intent, uri);

            return intent;
        }
        return null;
    }

    public void getVideo(final Context context, final OnImageReadyListener imageReadyListener) {
        if (imageReadyListener == null) {
            throw new IllegalStateException("OnImageReadyListener must not be null");
        }

        if (mCurrentMediaPath == null) {
            Timber.w("mCurrentMediaPath null. " +
                    "This happen if you haven't call #getCameraVideoIntent() or the activity is being recreated");
            imageReadyListener.onImageReady(null);
            return;
        }

        final Uri videoUri = Uri.parse(mCurrentMediaPath);
        if (videoUri != null) {
            MediaScannerConnection.scanFile(context.getApplicationContext(),
                    new String[]{videoUri.getPath()}, null, (path, uri) -> {

                        Timber.d("File " + path + " was scanned successfully: " + uri);

                        if (path == null) {
                            Timber.d("This should not happen, go back to Immediate implemenation");
                            path = mCurrentMediaPath;
                        }

                        imageReadyListener.onImageReady(singleListFromPath(path));
                        PickerUtils.revokeAppPermission(context, videoUri);
                    });
        }
    }

    public void getImage(final Context context, final OnImageReadyListener imageReadyListener) {
        if (imageReadyListener == null) {
            throw new IllegalStateException("OnImageReadyListener must not be null");
        }

        if (mCurrentMediaPath == null) {
            Timber.w("mCurrentMediaPath null. " +
                    "This happen if you haven't call #getCameraPhotoIntent() or the activity is being recreated");
            imageReadyListener.onImageReady(null);
            return;
        }

        final Uri imageUri = Uri.parse(mCurrentMediaPath);
        if (imageUri != null) {
            MediaScannerConnection.scanFile(context.getApplicationContext(),
                    new String[]{imageUri.getPath()}, null, (path, uri) -> {

                        Timber.d("File " + path + " was scanned successfully: " + uri);

                        if (path == null) {
                            Timber.d("This should not happen, go back to Immediate implemenation");
                            path = mCurrentMediaPath;
                        }

                        imageReadyListener.onImageReady(singleListFromPath(path));
                        PickerUtils.revokeAppPermission(context, imageUri);
                    });
        }
    }

    public void removeCaptured() {
        if (mCurrentMediaPath != null) {
            File file = new File(mCurrentMediaPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public String getCurrentMediaPath() {
        return mCurrentMediaPath.replace("file:", "");
    }

    private String getProviderName(Context context) {
        return String.format(Locale.getDefault(), "%s%s", context.getPackageName(), ".multipicker.provider");
    }

    public interface OnImageReadyListener {
        void onImageReady(List<MediaFile> files);
    }
}
