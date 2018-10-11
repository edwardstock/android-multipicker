package com.edwardstock.multipicker;

import android.os.Parcel;
import android.os.Parcelable;

import com.edwardstock.multipicker.internal.PickerSavePath;

import androidx.annotation.RestrictTo;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RestrictTo({RestrictTo.Scope.LIBRARY})
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

    private boolean mShowPhotos;
    private boolean mShowVideos;
    private boolean mEnableCamera;
    private int mFileColumns = 3;
    private int mFileColumnsTablet = 5;
    private int mDirColumns = 2;
    private int mDirColumnsTablet = 3;
    private boolean mEnableSelectionAnimation = true;
    private String mTitle = null;
    private int mLimit = -1;

    private PickerSavePath mVideoSavePath = PickerSavePath.DEFAULT;
    private PickerSavePath mPhotoSavePath = PickerSavePath.DEFAULT;

    private MultiPicker mPicker;

    PickerConfig(MultiPicker picker) {
        mPicker = picker;
    }

    protected PickerConfig(Parcel in) {
        mShowPhotos = in.readByte() != 0x00;
        mShowVideos = in.readByte() != 0x00;
        mEnableCamera = in.readByte() != 0x00;
        mVideoSavePath = in.readParcelable(PickerSavePath.class.getClassLoader());
        mPhotoSavePath = in.readParcelable(PickerSavePath.class.getClassLoader());
        mDirColumns = in.readInt();
        mDirColumnsTablet = in.readInt();
        mFileColumns = in.readInt();
        mFileColumnsTablet = in.readInt();
    }

    public int getLimit() {
        return mLimit;
    }

    public PickerConfig setLimit(int limit) {
        mLimit = limit;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public PickerConfig setTitle(String title) {
        mTitle = title;
        return this;
    }

    public boolean isEnableSelectionAnimation() {
        return mEnableSelectionAnimation;
    }

    public PickerConfig setEnableSelectionAnimation(boolean enableSelectionAnimation) {
        mEnableSelectionAnimation = enableSelectionAnimation;
        return this;
    }

    public int getDirColumns() {
        return mDirColumns;
    }

    public PickerConfig setDirColumns(int dirColumns) {
        mDirColumns = dirColumns;
        return this;
    }

    public int getDirColumnsTablet() {
        return mDirColumnsTablet;
    }

    public PickerConfig setDirColumnsTablet(int dirColumnsTablet) {
        mDirColumnsTablet = dirColumnsTablet;
        return this;
    }

    public int getFileColumns() {
        return mFileColumns;
    }

    public PickerConfig setFileColumns(int fileColumns) {
        mFileColumns = fileColumns;
        return this;
    }

    public int getFileColumnsTablet() {
        return mFileColumnsTablet;
    }

    public PickerConfig setFileColumnsTablet(int fileColumnsTablet) {
        mFileColumnsTablet = fileColumnsTablet;
        return this;
    }

    public PickerConfig setPhotoSavePath(PickerSavePath photoSavePath) {
        mPhotoSavePath = photoSavePath;
        return this;
    }

    public PickerConfig setVideoSavePath(PickerSavePath videoSavePath) {
        mVideoSavePath = videoSavePath;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
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
    }

    public PickerSavePath getVideoSavePath() {
        return mVideoSavePath;
    }

    public void setVideoSavePath(String pathName) {
        mVideoSavePath = new PickerSavePath(pathName, false);
    }

    public PickerSavePath getPhotoSavePath() {
        return mPhotoSavePath;
    }

    public PickerConfig setPhotoSavePath(String pathName) {
        mPhotoSavePath = new PickerSavePath(pathName, false);
        return this;
    }

    public boolean isEnableCamera() {
        return mEnableCamera;
    }

    public void setEnableCamera(boolean capturePhoto) {
        mEnableCamera = capturePhoto;
    }

    public boolean isShowPhotos() {
        return mShowPhotos;
    }

    public void setShowPhotos(boolean showPhotos) {
        mShowPhotos = showPhotos;
    }

    public boolean isShowVideos() {
        return mShowVideos;
    }

    public void setShowVideos(boolean showVideos) {
        mShowVideos = showVideos;
    }

    public MultiPicker build() {
        return mPicker.withConfig(this);
    }
}
