package com.edwardstock.multipicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.picker.ui.DirsActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

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
    public static void handleActivityResult(int requestCode, int resultCode, Intent data, ResultCallback callback) throws IOException {
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
    public static void handleActivityResult(int userRequestCode, int incomingRequestCode, int resultCode, Intent data, ResultCallback callback) throws IOException {
        if (userRequestCode != incomingRequestCode || resultCode != Activity.RESULT_OK) {
            return;
        }

        callback.onResult(readResult(data));
    }

    /**
     * Handle result by yourself with this method.
     * @param resultCode -1 = OK, 0 = Canceled
     * @param data intent data
     * @return Empty list if user did chosen nothing
     */
    public static List<MediaFile> handleActivityResult(int resultCode, Intent data) throws IOException {
        if (resultCode != Activity.RESULT_OK) {
            return Collections.emptyList();
        }

        return readResult(data);
    }

    private static List<MediaFile> readResult(Intent data) throws IOException {
        Uri resultFileName = data.getData();
        if (resultFileName == null) {
            Timber.w("Result file name were not set!");
            return Collections.emptyList();
        }

        final File path = new File(resultFileName.getPath());
        final RandomAccessFile raf = new RandomAccessFile(path, "r");
        final byte[] resultData = new byte[((int) path.length())];
        raf.readFully(resultData);
        raf.close();
        path.delete();

        Gson gson = new GsonBuilder().create();
        return gson.fromJson(new String(resultData), new TypeToken<List<MediaFile>>() {
        }.getType());
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
