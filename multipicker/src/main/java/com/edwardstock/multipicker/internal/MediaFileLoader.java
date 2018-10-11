package com.edwardstock.multipicker.internal;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import timber.log.Timber;

import com.annimon.stream.Stream;
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
import java.util.concurrent.ThreadPoolExecutor;


public class MediaFileLoader {

    private Context mContext;
    private ExecutorService mExecutorService;

    public MediaFileLoader(Context context) {
        mContext = context;
    }

    private final String[] projection = new String[]{
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

    public void loadDeviceImages(final boolean isFolderMode, final boolean includeVideo, final boolean includePhotos, final List<File> excludedImages, final OnLoadListener listener) {
        getExecutorService().execute(new ImageLoadRunnable(isFolderMode, includeVideo, includePhotos, excludedImages, listener));
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
    
    public interface OnLoadListener {
        void onFilesLoadFailed(Throwable t);
        void onFilesLoadedSuccess(List<MediaFile> images, List<Dir> dirList);
    }

    private class ImageLoadRunnable implements Runnable {
        private boolean mIncludePhotos;
        private boolean mIsFolderMode;
        private boolean mIncludeVideo;
        private List<File> mExcludedImages;
        private OnLoadListener mImageLoadListener;
        private String mRoot = "external";

        public ImageLoadRunnable(boolean isFolderMode, boolean includeVideo, boolean includePhotos, List<File> excludedImages, OnLoadListener imageLoadListener) {
            mIsFolderMode = isFolderMode;
            mIncludeVideo = includeVideo;
            mIncludePhotos = includePhotos;
            mExcludedImages = excludedImages;
            mImageLoadListener = imageLoadListener;
        }

        @Override
        public void run() {
            Cursor cursor;
            if (mIncludeVideo && mIncludePhotos) {
                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

                cursor = mContext.getContentResolver().query(MediaStore.Files.getContentUri(mRoot), projection,
                        selection, null, MediaStore.Images.Media.DATE_ADDED);
            } else if (!mIncludePhotos && mIncludeVideo) {
                cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                        null, null, MediaStore.Video.Media.DATE_ADDED);
            } else {
                cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                        null, null, MediaStore.Images.Media.DATE_ADDED);
            }

            if (cursor == null) {
                mImageLoadListener.onFilesLoadFailed(new NullPointerException());
                return;
            }

            List<MediaFile> temp = new ArrayList<>();
            Map<String, Dir> folderMap = null;
            if (mIsFolderMode) {
                folderMap = new HashMap<>();
            }

            int cntVideos = 0;

            if (cursor.moveToLast()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));
                    String videoWidth = cursor.getString(cursor.getColumnIndex(projection[4]));
                    String videoHeight = cursor.getString(cursor.getColumnIndex(projection[5]));
                    String videoDuration = cursor.getString(cursor.getColumnIndex(projection[6]));
                    String imageWidth = cursor.getString(cursor.getColumnIndex(projection[7]));
                    String imageHeight = cursor.getString(cursor.getColumnIndex(projection[8]));

                    File file = makeSafeFile(path);
                    if (file != null) {
                        if (mExcludedImages != null && mExcludedImages.contains(file))
                            continue;

                        final File f = new File(path);
                        if(f.isFile()) {
                            MediaFile mediaFile;
                            if(PickerUtils.isVideoFormat(path)) {
                                cntVideos++;
                                mediaFile = new MediaFile(id, name, path,
                                        new VideoInfo(new MediaSize(videoWidth, videoHeight), videoDuration)
                                );
                            } else {
                                mediaFile = new MediaFile(id, name, path, new MediaSize(imageWidth, imageHeight));
                            }

                            temp.add(mediaFile);

                            if (folderMap != null) {
                                Dir folder = folderMap.get(bucket);
                                if (folder == null) {
                                    folder = new Dir(bucket);
                                    folderMap.put(bucket, folder);
                                }
                                folder.getFiles().add(mediaFile);
                            }
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if(cntVideos > 0) {
                new Thread(()->{
                    Stream.of(temp)
                            .filter(MediaFile::isVideo)
                            .forEach(item -> {
                                final File f = PickerUtils.createVideoThumbFile(item);
                                if(f == null) {
                                    return;
                                }

                                final Bitmap bmp = createVideoThumbnail(item.getPath());
                                try(FileOutputStream o = new FileOutputStream(f)) {
                                    Timber.d("Write thumbnail for video %s: %s", item.getPath(), f.getAbsolutePath());
                                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, o);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    bmp.recycle();
                                }
                            });
                }).start();
            }

            /* Convert HashMap to ArrayList if not null */
            List<Dir> folders = null;
            if (folderMap != null) {
                folders = new ArrayList<>(folderMap.values());
            } else {
                folders = new ArrayList<>(0);
            }

            mImageLoadListener.onFilesLoadedSuccess(temp, folders);
        }
    }

    private Bitmap createVideoThumbnail(String filePath) {
        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        mMMR.setDataSource(filePath);
        //api time unit is microseconds
        return mMMR.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
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

}
