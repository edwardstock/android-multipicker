package com.edwardstock.multipicker.internal;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;

import com.edwardstock.multipicker.data.MediaFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class PickerUtils {

    public static File createVideoThumbFile(MediaFile file) {
        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PickerSavePath.THUMBNAILS.getPath());

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
//                Timber.d("Oops! Failed create " + path);
                return null;
            }
        }

        // Create a media file name
        String srcName = new File(file.getPath()).getName();
        srcName = srcName.substring(0, srcName.lastIndexOf('.'));
        String imageFileName = "thumb_" + srcName;

        File imageFile = null;
        try {
            File f = new File(mediaStorageDir, imageFileName+".jpg");
            if(f.exists() && f.length() > 0) {
                file.getVideoInfo().setPreviewPath(f.getAbsolutePath());
                return null;
            }
            if(!f.createNewFile()) {
                Timber.d("Can't create new file");
                return null;
            }
            imageFile = f;
            file.getVideoInfo().setPreviewPath(imageFile.getAbsolutePath());
        } catch (IOException e) {
            Timber.d("Oops! Failed create " + imageFileName + " file");
        }
        return imageFile;
    }


    public static File createImageFile(PickerSavePath savePath) {
        // External sdcard location
        final String path = savePath.getPath();
        File mediaStorageDir = savePath.isFullPath()
                ? new File(path)
                : new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("Oops! Failed create " + path);
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", mediaStorageDir);
        } catch (IOException e) {
            Timber.d("Oops! Failed create " + imageFileName + " file");
        }
        return imageFile;
    }

    public static String getNameFromFilePath(String path) {
        if (path.contains(File.separator)) {
            return path.substring(path.lastIndexOf(File.separator) + 1);
        }
        return path;
    }

    public static void grantAppPermission(Context context, Intent intent, Uri fileUri) {
        List<ResolveInfo> resolvedIntentActivities = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public static void revokeAppPermission(Context context, Uri fileUri) {
        context.revokeUriPermission(fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public static boolean isGifFormat(MediaFile image) {
        String extension = image.getPath().substring(image.getPath().lastIndexOf(".") + 1, image.getPath().length());
        return extension.equalsIgnoreCase("gif");
    }

    public static String mimeFrom(String path) {
        return URLConnection.guessContentTypeFromName(path);
    }

    public static boolean isImageFormat(String path) {
        if (path == null) {
            return false;
        }
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && !mimeType.isEmpty() && mimeType.startsWith("image");
        } catch (StringIndexOutOfBoundsException e) {
            Timber.e(e, "Unable to resolve content type for path: %s", path);
            return false;
        }
    }

    public static boolean isVideoFormat(String path) {
        if (path == null) {
            return false;
        }
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && !mimeType.isEmpty() && mimeType.startsWith("video");
        } catch (StringIndexOutOfBoundsException e) {
            Timber.e(e, "Unable to resolve content type for path: %s", path);
            return false;
        }
    }

    public static boolean isVideoFormat(MediaFile file) {
        return isVideoFormat(file.getPath());
    }
}
