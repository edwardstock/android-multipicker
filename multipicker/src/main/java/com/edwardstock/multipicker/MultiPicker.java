package com.edwardstock.multipicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.ActivityBuilder;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.ui.PickerActivity;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public final class MultiPicker{
    private PickerConfig mConfig;

    public static MultiPicker create(Context context) {
        return new MultiPicker(activity);
    }

    MultiPicker(Context context) {
        try {
            Class.forName("androidx.fragment.app.Fragment");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<MediaFile> handleActivityResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return Collections.emptyList();
        }

        return data.getParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES);
    }

    public PickerConfig configure() {
        return mConfig;
    }

    public void start(int requestCode) {
//        new PickerActivity.Builder(, mConfig)
//                .start(requestCode);
    }

    public MultiPicker withConfig(PickerConfig pickerConfig) {
        mConfig = pickerConfig;
        return this;
    }
}
