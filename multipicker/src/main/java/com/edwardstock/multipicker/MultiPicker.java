package com.edwardstock.multipicker;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.ui.PickerActivity;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public final class MultiPicker {
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

    public static List<MediaFile> handleActivityResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return Collections.emptyList();
        }

        return data.getParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES);
    }

    public PickerConfig configure() {
        return new PickerConfig(this);
    }

    public void start(int requestCode) {
        new PickerActivity.Builder(mActivity.get(), mConfig)
                .start(requestCode);
    }

    MultiPicker withConfig(PickerConfig pickerConfig) {
        mConfig = pickerConfig;
        return this;
    }
}
