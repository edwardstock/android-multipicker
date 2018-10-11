package com.edwardstock.multipicker.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public final class VideoInfo implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
    private MediaSize mSize;
    private long mDurationMs;
    private String mPreviewPath;

    public VideoInfo(MediaSize size, String durationMs) {
        mSize = size;
        if (durationMs == null || durationMs.isEmpty()) {
            mDurationMs = 0;
        } else {
            mDurationMs = Long.parseLong(durationMs);
        }
    }

    public VideoInfo(MediaSize size, long durationMs) {
        mSize = size;
        mDurationMs = durationMs;
    }

    protected VideoInfo(Parcel in) {
        mSize = (MediaSize) in.readValue(MediaSize.class.getClassLoader());
        mDurationMs = in.readLong();
        mPreviewPath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mSize);
        dest.writeLong(mDurationMs);
        dest.writeString(mPreviewPath);
    }

    public String getPreviewPath() {
        return mPreviewPath;
    }

    public void setPreviewPath(String filePath) {
        mPreviewPath = filePath;
    }

    public MediaSize getSize() {
        return mSize;
    }

    public long getDurationMs() {
        return mDurationMs;
    }

    public String getDuration() {
        long seconds = (mDurationMs / 1000L);
        long second = seconds % 60L;
        long minute = (seconds / 60L) % 60L;
        long hour = (seconds / (60L * 60L)) % 24L;

        if (hour > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
        } else {
            return String.format(Locale.getDefault(), "00:%02d:%02d", minute, second);
        }
    }
}
