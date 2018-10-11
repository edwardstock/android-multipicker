package com.edwardstock.multipicker.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public final class MediaSize implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MediaSize> CREATOR = new Parcelable.Creator<MediaSize>() {
        @Override
        public MediaSize createFromParcel(Parcel in) {
            return new MediaSize(in);
        }

        @Override
        public MediaSize[] newArray(int size) {
            return new MediaSize[size];
        }
    };
    private int mWidth;
    private int mHeight;

    public MediaSize(String width, String height) {
        if (width == null || width.isEmpty() || height == null || height.isEmpty()) {
            mWidth = 0;
            mHeight = 0;
            return;
        }
        mWidth = Integer.parseInt(width);
        mHeight = Integer.parseInt(height);
    }

    public MediaSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    protected MediaSize(Parcel in) {
        mWidth = in.readInt();
        mHeight = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
