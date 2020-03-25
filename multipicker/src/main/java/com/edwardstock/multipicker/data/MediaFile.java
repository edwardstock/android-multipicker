package com.edwardstock.multipicker.data;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.edwardstock.multipicker.internal.PickerUtils;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class MediaFile implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MediaFile> CREATOR = new Parcelable.Creator<MediaFile>() {
        @Override
        public MediaFile createFromParcel(Parcel in) {
            return new MediaFile(in);
        }

        @Override
        public MediaFile[] newArray(int size) {
            return new MediaFile[size];
        }
    };
    @Expose
    private long mId;
    @Expose
    private String mName;
    @Expose
    private String mPath;
    @Expose
    private File mFile;
    @Expose
    private VideoInfo mVideoInfo;
    @Expose
    private MediaSize mMediaSize;

    public MediaFile(String path) {
        mPath = path;
    }

    public MediaFile(long id, String name, String path) {
        mId = id;
        mName = name;
        mPath = path;
        mFile = new File(mPath);
    }

    public MediaFile(long id, String name, String path, MediaSize size) {
        this(id, name, path);
        mMediaSize = size;
    }


    public MediaFile(long id, String name, String path, VideoInfo videoInfo) {
        this(id, name, path);
        mVideoInfo = videoInfo;
        if (mVideoInfo != null) {
            mMediaSize = videoInfo.getSize();
        }
    }

    protected MediaFile(Parcel in) {
        mId = in.readLong();
        mName = in.readString();
        mPath = in.readString();
        mFile = new File(in.readString());
        mVideoInfo = in.readParcelable(VideoInfo.class.getClassLoader());
    }

    public MediaSize getMediaSize() {
        return mMediaSize;
    }

    public VideoInfo getVideoInfo() {
        return mVideoInfo;
    }

    public long getSize() {
        return new File(getPath()).length();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s{id=%d, name=%s, path=%s}", getClass().getSimpleName(), mId, mName, mPath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaFile mediaFile = (MediaFile) o;
        return mId == mediaFile.mId &&
                ObjectsCompat.equals(mName, mediaFile.mName) &&
                ObjectsCompat.equals(mPath, mediaFile.mPath);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(mId, mName, mPath);
    }

    public boolean isVideo() {
        return PickerUtils.isVideoFormat(this);
    }

    public String getPath() {
        return mPath;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Path getPathFS() {
        return FileSystems.getDefault().getPath(getPath());
    }

    public File getFile() {
        return mFile;
    }

    public Uri getUri() {
        return Uri.fromFile(getFile());
    }

    public boolean exists() {
        return getFile().exists();
    }

    public long length() {
        return getFile().length();
    }

    public boolean delete() {
        return getFile().delete();
    }

    public String getName() {
        return mName;
    }

    public long getId() {
        return mId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
        dest.writeString(mPath);
        dest.writeString(mFile.getAbsolutePath());
        dest.writeParcelable(mVideoInfo, 0);
    }


}
