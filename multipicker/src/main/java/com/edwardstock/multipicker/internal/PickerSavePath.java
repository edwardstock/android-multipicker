package com.edwardstock.multipicker.internal;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

public class PickerSavePath implements Parcelable {

    public static final PickerSavePath DEFAULT = new PickerSavePath("Camera", false);
    public static final PickerSavePath THUMBNAILS = new PickerSavePath(".thumbnails", false);
    public static final Creator<PickerSavePath> CREATOR = new Creator<PickerSavePath>() {
        @Override
        public PickerSavePath createFromParcel(Parcel source) {
            return new PickerSavePath(source);
        }

        @Override
        public PickerSavePath[] newArray(int size) {
            return new PickerSavePath[size];
        }
    };
    @Expose
    private final String mPath;
    @Expose
    private final boolean mIsFullPath;

    public PickerSavePath(String path) {
        this(path, false);
    }

    public PickerSavePath(String path, boolean isFullPath) {
        mPath = path;
        mIsFullPath = isFullPath;
    }

    protected PickerSavePath(Parcel in) {
        mPath = in.readString();
        mIsFullPath = in.readByte() != 0;
    }

    public String getPath() {
        return mPath;
    }

    public boolean isFullPath() {
        return mIsFullPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPath);
        dest.writeByte(mIsFullPath ? (byte) 1 : (byte) 0);
    }
}
