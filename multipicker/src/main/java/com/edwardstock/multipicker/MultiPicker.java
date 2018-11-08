package com.edwardstock.multipicker;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.ui.DirsActivity;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public final class MultiPicker {
    private final static int REQUEST_CODE_MULTIPICKER_DEFAULT = 1000;
    private PickerConfig mConfig;
    private WeakReference<Activity> mActivity;

    MultiPicker(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public static MultiPicker create(Activity activity) {
        checkNotNull(activity);
        return new MultiPicker(activity);
    }

    public static MultiPicker create(Fragment fragment) {
        checkNotNull(fragment);
        checkNotNull(fragment.getActivity());
        return new MultiPicker(fragment.getActivity());
    }

    /**
     * Call this for default request code after starting picker with {@link #start()}
     * @param requestCode Activity request code
     * @param resultCode Activity result code
     * @param data Intent data
     * @param callback callback to get data
     */
    public static void handleActivityResult(int requestCode, int resultCode, Intent data, ResultCallback callback) {
        handleActivityResult(REQUEST_CODE_MULTIPICKER_DEFAULT, requestCode, resultCode, data, callback);
    }

    /**
     * Using for yourself request code set. Method does not call callback if user did chosen nothing
     * @param userRequestCode Your own request code
     * @param incomingRequestCode Incoming request code passed from {@link Activity#onActivityResult(int, int, Intent)}
     * @param resultCode Activity result code
     * @param data Intent data
     * @param callback callback to get result data
     */
    public static void handleActivityResult(int userRequestCode, int incomingRequestCode, int resultCode, Intent data, ResultCallback callback) {
        if (userRequestCode != incomingRequestCode || resultCode != Activity.RESULT_OK) {
            return;
        }

        callback.onResult(data.getParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES));
    }

    /**
     * Handle result by yourself with this method.
     * @param resultCode
     * @param data
     * @return Empty list if user did chosen nothing
     */
    public static List<MediaFile> handleActivityResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return Collections.emptyList();
        }

        return data.getParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES);
    }

    public void start() {
        mConfig.requestCode(REQUEST_CODE_MULTIPICKER_DEFAULT);
        start(REQUEST_CODE_MULTIPICKER_DEFAULT);
    }

    public PickerConfig configure() {
        return new PickerConfig(this);
    }

    public void start(int requestCode) {
        mConfig.requestCode(REQUEST_CODE_MULTIPICKER_DEFAULT);
        new DirsActivity.Builder(mActivity.get(), mConfig)
                .start(requestCode);
    }

    MultiPicker withConfig(PickerConfig pickerConfig) {
        mConfig = pickerConfig;
        return this;
    }

    public interface ResultCallback {
        void onResult(List<MediaFile> files);
    }
}
