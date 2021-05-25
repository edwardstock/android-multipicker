package com.edwardstock.multipicker

import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.picker.ui.PickerActivity
import java.lang.ref.WeakReference

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
class MultiPicker {
    companion object {
        fun create(activity: AppCompatActivity): MultiPicker {
            return MultiPicker(activity)
        }

        fun create(fragment: Fragment): MultiPicker {
            return MultiPicker(fragment)
        }
    }

    private var mConfig: PickerConfig = PickerConfig()
    private var mActivity: WeakReference<AppCompatActivity>? = null
    private var mFragment: WeakReference<Fragment>? = null

    internal constructor(activity: AppCompatActivity) {
        mActivity = WeakReference(activity)
    }

    internal constructor(fragment: Fragment) {
        mFragment = WeakReference(fragment)
    }

    private var onResult: ((List<MediaFile>) -> Unit)? = null
    private var arLauncher: ActivityResultLauncher<PickerConfig>? = null

    fun prepare(onResult: (List<MediaFile>) -> Unit): MultiPicker {
        when {
            mActivity != null && mActivity?.get() != null -> {
                arLauncher = PickerActivity.Builder(mActivity!!.get()!!).prepare(onResult)
            }
            mFragment != null && mFragment?.get() != null -> {
                arLauncher = PickerActivity.Builder(mFragment!!.get()!!).prepare(onResult)
            }
        }
        return this
    }

    fun start() {
        arLauncher?.launch(mConfig)
    }

    fun configure(config: PickerConfig): MultiPicker {
        mConfig = config
        return this
    }

    fun configure(config: PickerConfig.() -> Unit): MultiPicker {
        mConfig.apply(config)
        return this
    }


}