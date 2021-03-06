package com.edwardstock.multipicker.internal;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.annimon.stream.Stream;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.data.MediaSize;
import com.edwardstock.multipicker.data.VideoInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;


public class MediaFileLoader {

    private final String[] mPhotoProjection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
    };
    private final String[] mPhotoAndVideoProjection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
    };
    private final String[] mVideoProjection = new String[]{
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION,
    };
    private Context mContext;
    private ExecutorService mExecutorService;

    public MediaFileLoader(Context context) {
        mContext = context;
    }

    @Nullable
    private static File makeSafeFile(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        try {
            return new File(path);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void loadDeviceImages(PickerConfig config, final OnLoadListener listener) {
        getExecutorService().execute(new ImageLoadRunnable(config, listener));
    }

    public void abortLoadImages() {
        if (mExecutorService != null) {
            mExecutorService.shutdown();
            mExecutorService = null;
        }
    }

    private ExecutorService getExecutorService() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
        return mExecutorService;
    }

    private Bitmap createVideoThumbnail(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        //api time unit is microseconds
        try {
            return mmr.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } finally {
            mmr.release();
        }
    }

    public interface OnLoadListener {
        void onFilesLoadFailed(Throwable t);
        void onFilesLoadedSuccess(List<MediaFile> images, List<Dir> dirList);
    }

    private class ImageLoadRunnable implements Runnable {
        private PickerConfig mConfig;
        private OnLoadListener mImageLoadListener;
        private String mRoot = "external";

        public ImageLoadRunnable(final PickerConfig config, OnLoadListener imageLoadListener) {
            mConfig = config;
            mImageLoadListener = imageLoadListener;
        }

        @Override
        public void run() {
            Cursor cursor;
            final String[] projection;
            String selection;

            if (mConfig.isShowVideos() && mConfig.isShowPhotos()) {
                projection = mPhotoAndVideoProjection;
                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                        " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

                cursor = mContext.getContentResolver().query(MediaStore.Files.getContentUri(mRoot), projection,
                        selection, null, MediaStore.Files.FileColumns.DATE_ADDED);
            } else if (!mConfig.isShowPhotos() && mConfig.isShowVideos()) {
                projection = mVideoProjection;
                cursor = mContext.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                        null, null, MediaStore.Files.FileColumns.DATE_ADDED);
            } else {
                projection = mPhotoProjection;
                cursor = mContext.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                        null, null, MediaStore.Files.FileColumns.DATE_ADDED);
            }


            if (cursor == null) {
                mImageLoadListener.onFilesLoadFailed(new NullPointerException());
                return;
            }

            List<MediaFile> files = new ArrayList<>();
            Map<String, Dir> dirMap = new HashMap<>();

            int cntVideos = 0;

            if (cursor.moveToLast()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));
                    String videoWidth = null;
                    String videoHeight = null;
                    String videoDuration = null;
                    String imageWidth = null;
                    String imageHeight = null;

                    if (mConfig.isShowPhotos() && mConfig.isShowVideos()) {
                        videoWidth = cursor.getString(cursor.getColumnIndex(projection[4]));
                        videoHeight = cursor.getString(cursor.getColumnIndex(projection[5]));
                        videoDuration = cursor.getString(cursor.getColumnIndex(projection[6]));
                        imageWidth = cursor.getString(cursor.getColumnIndex(projection[7]));
                        imageHeight = cursor.getString(cursor.getColumnIndex(projection[8]));
                    } else if (!mConfig.isShowPhotos() && mConfig.isShowVideos()) {
                        videoWidth = cursor.getString(cursor.getColumnIndex(projection[4]));
                        videoHeight = cursor.getString(cursor.getColumnIndex(projection[5]));
                        videoDuration = cursor.getString(cursor.getColumnIndex(projection[6]));
                    } else {
                        imageWidth = cursor.getString(cursor.getColumnIndex(projection[4]));
                        imageHeight = cursor.getString(cursor.getColumnIndex(projection[5]));
                    }

                    File file = makeSafeFile(path);
                    if (file == null) {
                        continue;
                    }

                    if (mConfig.getExcludedFiles().contains(file.getAbsolutePath())) {
                        continue;
                    }

                    if (file.isFile()) {
                        MediaFile mediaFile;
                        if (PickerUtils.isVideoFormat(path)) {
                            cntVideos++;
                            mediaFile = new MediaFile(id, name, path,
                                    new VideoInfo(new MediaSize(videoWidth, videoHeight), videoDuration)
                            );
                        } else {
                            mediaFile = new MediaFile(id, name, path, new MediaSize(imageWidth, imageHeight));
                        }

                        files.add(mediaFile);

                        Dir folder = dirMap.get(bucket);
                        if (folder == null) {
                            folder = new Dir(bucket);
                            dirMap.put(bucket, folder);
                        }
                        folder.getFiles().add(mediaFile);
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (cntVideos > 0) {
                getExecutorService().execute(new VideoThumbResolver(files));
            }

            /* Convert HashMap to ArrayList if not null */
            List<Dir> dirs = new ArrayList<>(dirMap.values());

            mImageLoadListener.onFilesLoadedSuccess(files, dirs);
        }
    }

    private final class VideoThumbResolver implements Runnable {
        private final List<MediaFile> mFiles;

        VideoThumbResolver(List<MediaFile> files) {
            mFiles = Stream.of(files).withoutNulls().filter(MediaFile::isVideo).toList();
        }

        @Override
        public void run() {
            for (final MediaFile item : mFiles) {
                final File f = PickerUtils.createVideoThumbFile(item);
                if (f == null) {
                    return;
                }

                final Bitmap bmp = createVideoThumbnail(item.getPath());
                try (FileOutputStream o = new FileOutputStream(f)) {
                    Timber.d("Write thumbnail for video %s: %s", item.getPath(), f.getAbsolutePath());
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, o);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bmp.recycle();
                }
            }
        }
    }

}
