package com.edwardstock.multipicker;

import android.os.Parcel;
import android.os.Parcelable;

import com.annimon.stream.Stream;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.PickerSavePath;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public final class PickerConfig implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PickerConfig> CREATOR = new Parcelable.Creator<PickerConfig>() {
        @Override
        public PickerConfig createFromParcel(Parcel in) {
            return new PickerConfig(in);
        }

        @Override
        public PickerConfig[] newArray(int size) {
            return new PickerConfig[size];
        }
    };

    private boolean mShowPhotos = true;
    private boolean mShowVideos = false;
    private boolean mEnableCamera = true;
    private PickerSavePath mVideoSavePath = PickerSavePath.DEFAULT;
    private PickerSavePath mPhotoSavePath = PickerSavePath.DEFAULT;
    private int mDirColumns = 2;
    private int mDirColumnsTablet = 3;
    private int mFileColumns = 3;
    private int mFileColumnsTablet = 5;
    private boolean mEnableSelectionAnimation = true;
    private String mTitle = null;
    private int mLimit = 0; // 0 - no limit
    private ArrayList<String> mExcludedFiles = new ArrayList<>(0);
    private MultiPicker mPicker;
    private int mRequestCode;

    PickerConfig(MultiPicker picker) {
        mPicker = picker;
    }

    PickerConfig(Parcel in) {
        mShowPhotos = in.readByte() != 0x00;
        mShowVideos = in.readByte() != 0x00;
        mEnableCamera = in.readByte() != 0x00;
        mVideoSavePath = in.readParcelable(PickerSavePath.class.getClassLoader());
        mPhotoSavePath = in.readParcelable(PickerSavePath.class.getClassLoader());
        mDirColumns = in.readInt();
        mDirColumnsTablet = in.readInt();
        mFileColumns = in.readInt();
        mFileColumnsTablet = in.readInt();
        mEnableSelectionAnimation = in.readByte() != 0x00;
        mTitle = in.readString();
        mLimit = in.readInt();
        in.readList(mExcludedFiles, String.class.getClassLoader());
        mRequestCode = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mShowPhotos ? 0x01 : 0x00));
        dest.writeByte((byte) (mShowVideos ? 0x01 : 0x00));
        dest.writeByte((byte) (mEnableCamera ? 0x01 : 0x00));
        dest.writeParcelable(mVideoSavePath, 0);
        dest.writeParcelable(mPhotoSavePath, 0);
        dest.writeInt(mDirColumns);
        dest.writeInt(mDirColumnsTablet);
        dest.writeInt(mFileColumns);
        dest.writeInt(mFileColumnsTablet);
        dest.writeByte((byte) (mEnableSelectionAnimation ? 0x01 : 0x00));
        dest.writeString(mTitle);
        dest.writeInt(mLimit);
        if (mExcludedFiles == null) {
            mExcludedFiles = new ArrayList<>(0);
        }
        dest.writeList(mExcludedFiles);
        dest.writeInt(mRequestCode);
    }

    public PickerConfig excludeFile(MediaFile file) {
        if (file == null) return this;

        mExcludedFiles.add(file.getPath());
        return this;
    }

    public ArrayList<String> getExcludedFiles() {
        return mExcludedFiles;
    }

    public PickerConfig excludeFile(String absPath) {
        if (absPath == null) return this;

        if (absPath.startsWith("file://")) {
            mExcludedFiles.add(absPath.replace("file://", ""));
        }
        mExcludedFiles.add(absPath);
        return this;
    }

    public PickerConfig excludeFile(File file) {
        return excludeFile(file.getAbsolutePath());
    }

    public PickerConfig excludeFilesStrings(Collection<String> filePaths) {
        if (filePaths == null) {
            return this;
        }

        mExcludedFiles.addAll(filePaths);
        return this;
    }

    public PickerConfig excludeFiles(Collection<MediaFile> files) {
        if (files == null) return this;
        mExcludedFiles.addAll(Stream.of(files).withoutNulls().map(MediaFile::getPath).toList());
        return this;
    }

    public int getLimit() {
        return mLimit;
    }

    public PickerConfig limit(int limit) {
        mLimit = limit;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public PickerConfig title(String title) {
        mTitle = title;
        return this;
    }

    public boolean isEnableSelectionAnimation() {
        return mEnableSelectionAnimation;
    }

    public PickerConfig enableSelectionAnimation(boolean enableSelectionAnimation) {
        mEnableSelectionAnimation = enableSelectionAnimation;
        return this;
    }

    public int getDirColumns() {
        return mDirColumns;
    }

    public PickerConfig dirColumns(int dirColumns) {
        mDirColumns = dirColumns;
        return this;
    }

    public int getDirColumnsTablet() {
        return mDirColumnsTablet;
    }

    public PickerConfig dirColumnsTablet(int dirColumnsTablet) {
        mDirColumnsTablet = dirColumnsTablet;
        return this;
    }

    public int getFileColumns() {
        return mFileColumns;
    }

    public PickerConfig fileColumns(int fileColumns) {
        mFileColumns = fileColumns;
        return this;
    }

    public int getFileColumnsTablet() {
        return mFileColumnsTablet;
    }

    public PickerConfig fileColumnsTablet(int fileColumnsTablet) {
        mFileColumnsTablet = fileColumnsTablet;
        return this;
    }

    public PickerConfig photoSavePath(PickerSavePath photoSavePath) {
        mPhotoSavePath = photoSavePath;
        return this;
    }

    public PickerConfig videoSavePath(PickerSavePath videoSavePath) {
        mVideoSavePath = videoSavePath;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public PickerSavePath getVideoSavePath() {
        return mVideoSavePath;
    }

    public PickerConfig videoSavePath(String pathName) {
        mVideoSavePath = new PickerSavePath(pathName, false);
        return this;
    }

    public PickerSavePath getPhotoSavePath() {
        return mPhotoSavePath;
    }

    public PickerConfig photoSavePath(String pathName) {
        mPhotoSavePath = new PickerSavePath(pathName, false);
        return this;
    }

    public boolean isEnableCamera() {
        return mEnableCamera;
    }

    public PickerConfig enableCamera(boolean capturePhoto) {
        mEnableCamera = capturePhoto;
        return this;
    }

    public boolean isShowPhotos() {
        return mShowPhotos;
    }

    public PickerConfig showPhotos(boolean showPhotos) {
        mShowPhotos = showPhotos;
        return this;
    }

    public boolean isShowVideos() {
        return mShowVideos;
    }

    public PickerConfig showVideos(boolean showVideos) {
        mShowVideos = showVideos;
        return this;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public MultiPicker build() {
        return mPicker.withConfig(this);
    }

    public PickerConfig requestCode(int requestCode) {
        mRequestCode = requestCode;
        return this;
    }
}
