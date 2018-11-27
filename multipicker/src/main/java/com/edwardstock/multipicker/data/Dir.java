package com.edwardstock.multipicker.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.ObjectsCompat;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public final class Dir implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Dir> CREATOR = new Parcelable.Creator<Dir>() {
        @Override
        public Dir createFromParcel(Parcel in) {
            return new Dir(in);
        }

        @Override
        public Dir[] newArray(int size) {
            return new Dir[size];
        }
    };
    @Expose
    private String mName;
    @Expose
    private List<MediaFile> mFiles;
    @Expose
    private int mFilesCount = 0;

    public Dir(String name) {
        mName = name;
    }

    protected Dir(Parcel in) {
        mName = in.readString();
        if (in.readByte() == 0x01) {
            mFiles = new ArrayList<>();
            in.readList(mFiles, MediaFile.class.getClassLoader());
        } else {
            mFiles = null;
        }
        mFilesCount = in.readInt();
    }

    @NonNull
    @Override
    public String toString() {
        return mName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dir dir = (Dir) o;
        return ObjectsCompat.equals(mName, dir.mName);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(mName);
    }

    public String getName() {
        return mName;
    }

    public List<MediaFile> getFiles() {
        if (mFiles == null) {
            mFiles = new ArrayList<>();
        }
        return mFiles;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        if (mFiles == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mFiles);
        }
        dest.writeInt(mFilesCount);
    }

    public int getFilesCount() {
        return mFilesCount;
    }

    public void addFile(MediaFile mediaFile) {
        getFiles().add(mediaFile);
        mFilesCount++;
    }
}
