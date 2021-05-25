package com.edwardstock.multipicker.picker.ui


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.transition.*
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.databinding.MpActivityPickerBinding
import com.edwardstock.multipicker.internal.PickerSavePath
import com.edwardstock.multipicker.internal.PickerUtils
import com.edwardstock.multipicker.internal.views.ErrorView
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.views.PickerPresenter
import com.edwardstock.multipicker.picker.views.PickerView
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */

val capturePerms: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    arrayOf(Manifest.permission.CAMERA)
} else {
    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

@RuntimePermissions
class PickerActivity : AppCompatActivity(), PickerView, ErrorView {
    lateinit var b: MpActivityPickerBinding
    private lateinit var presenter: PickerPresenter
    private var mConfig: PickerConfig? = null
    private var mFragment: PickerFileSystemFragment? = null

    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null
    private var takePictureFile: PickerSavePath? = null
    private var takeVideoLauncher: ActivityResultLauncher<Uri>? = null
    private var takeVideoFile: PickerSavePath? = null

    override fun capturePhotoWithPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            capturePhotoWithPermissionCheck()
        } else {
            capturePhotoPreQWithPermissionCheck()
        }
    }

    override fun captureVideoWithPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            captureVideoWithPermissionCheck()
        } else {
            captureVideoPreQWithPermissionCheck()
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun capturePhotoPreQ() {
        capturePhotoInternal()
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun capturePhoto() {
        capturePhotoInternal()
    }

    private fun capturePhotoInternal() {
        try {
            takePictureFile = PickerSavePath.newTimestampTmpFile(this, "jpg")
            takePictureLauncher?.launch(takePictureFile!!.toUriProvider(this))
        } catch (t: Throwable) {
            Timber.e(t, "Unable to take picture")
            Toast.makeText(this, R.string.mp_error_create_image_file, Toast.LENGTH_LONG).show()
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun captureVideoPreQ() {
        captureVideoInternal()
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun captureVideo() {
        captureVideoInternal()
    }

    private fun captureVideoInternal() {
        try {
            takeVideoFile = PickerSavePath.newTimestampTmpFile(this, "mp4")
            takeVideoLauncher?.launch(takeVideoFile!!.toUriProvider(this))
        } catch (t: Throwable) {
            Timber.e(t, "Unable to take video")
            Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show()
        }
    }

    private fun onTakenPicture(saved: Boolean) {
        if (!saved) {
            Toast.makeText(this, R.string.mp_error_create_image_file, Toast.LENGTH_LONG).show()
        } else {
            val mf = MediaFile(path = takePictureFile!!.toFile(this)!!.absolutePath)
            try {
                PickerUtils.writeCapturedMedia(this, mf)
                Handler(Looper.getMainLooper()).postDelayed({
                    takePictureFile = null
                    presenter.onTakenMediaReady()
                }, 300)
            } catch (e: Throwable) {
                Timber.e(e)
                Toast.makeText(this, R.string.mp_error_create_image_file, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onTakenVideo(preview: Bitmap?) {
        val mf = MediaFile(path = takeVideoFile!!.toFile(this)!!.absolutePath)
        if (mf.file?.exists() == false || mf.file?.length() == 0L) {
            Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show()
            Timber.w("Unable to handle recorded video: file not exists or is empty")
            return
        }

        try {
            PickerUtils.writeCapturedMedia(this, mf)
            Handler(Looper.getMainLooper()).postDelayed({
                takeVideoFile = null
                presenter.onTakenMediaReady()
            }, 300)
        } catch (e: Throwable) {
            Timber.e(e)
            Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show()
        }
    }

    override fun scanMedia(file: MediaFile, listener: MediaScannerConnection.OnScanCompletedListener) {
        val paths = arrayOf(file.path)
        val mimeTypes = arrayOf(if (file.isVideo) "video/mp4" else "image/jpeg")
        MediaScannerConnection.scanFile(applicationContext, paths, mimeTypes, listener)
    }

    override fun onError(error: CharSequence?) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    override fun onError(t: Throwable?) {
        Toast.makeText(this, t?.message, Toast.LENGTH_LONG).show()
    }

    override fun startUpdateFiles() {
        Timber.d("Update files: Fragment: %s", mFragment.toString())
        mFragment?.updateFiles()
    }

    fun setupToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        if (item.itemId == R.id.menu_camera) {
            presenter.handleCapturePhoto()
            return true
        } else if (item.itemId == R.id.menu_video) {
            presenter.handleCaptureVideo()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (mConfig == null) {
            return true
        }
        if (mConfig!!.isEnableCamera) {
            if (mConfig!!.isShowPhotos && mConfig!!.isShowVideos) {
                menuInflater.inflate(R.menu.mp_image_picker_menu_all_main, menu)
            } else if (mConfig!!.isShowVideos) {
                menuInflater.inflate(R.menu.mp_image_picker_menu_video_main, menu)
            } else if (mConfig!!.isShowPhotos) {
                menuInflater.inflate(R.menu.mp_image_picker_menu_photo_main, menu)
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        presenter = PickerPresenter()
        presenter.attachToLifecycle(this)
        presenter.attachView(this)
        super.onCreate(savedInstanceState)
//        checkFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath)
//        checkFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath)
        mConfig = intent?.getParcelableExtra(PickerConst.EXTRA_CONFIG)
        presenter.onRestoreSavedState(savedInstanceState)
        b = MpActivityPickerBinding.inflate(layoutInflater)
        setContentView(b.root)
        setResult(RESULT_CANCELED)
        setupToolbar(b.toolbar)
        mFragment = DirsFragment.newInstance(mConfig)
        if (mConfig!!.title != null) {
            b.toolbar.title = mConfig!!.title
        }

        if (mConfig!!.isShowPhotos && mConfig!!.isShowVideos) {
            takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture(), this::onTakenPicture)
            takeVideoLauncher = registerForActivityResult(ActivityResultContracts.TakeVideo(), this::onTakenVideo)
        } else if (mConfig!!.isShowVideos) {
            takeVideoLauncher = registerForActivityResult(ActivityResultContracts.TakeVideo(), this::onTakenVideo)
        } else if (mConfig!!.isShowPhotos) {
            takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture(), this::onTakenPicture)
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.mp_container, mFragment!!, mFragment!!.javaClass.simpleName)
                .addToBackStack(null)
                .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // get fragment presenter
        if (mFragment is ImageViewerFragment) {
            supportFragmentManager.popBackStack()
        }
        resolveLastFragment()
        presenter.handleExtras(requestCode, resultCode, data)
        mFragment!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onBackPressed() {
        if (mFragment != null) {
            mFragment!!.onBackPressed()
        }
        var addedFile: MediaFile? = null
        if (mFragment is ImageViewerFragment) {
            addedFile = (mFragment as ImageViewerFragment).addedFile
        }
        if (supportFragmentManager.backStackEntryCount <= 1) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        super.onBackPressed()
        resolveLastFragment()
        if (mFragment is FilesFragment) {
            (mFragment as FilesFragment).addSelection(addedFile)
        }
    }

    fun startFiles(dir: Dir?) {
        val fragment = FilesFragment.newInstance(mConfig, dir)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.mp_container, fragment, fragment.javaClass.simpleName)
                .addToBackStack(null)
                .commit()
        mFragment = fragment
    }

    fun startPreview(dir: Dir?, file: MediaFile, sharedView: View?) {
        if (file.isVideo) {
            val intent = Intent(Intent.ACTION_VIEW, file.uri!!)
            intent.setDataAndType(file.uri!!, "video/*")
            startActivity(intent)
        } else {
            val fragment = ImageViewerFragment.newInstance(dir, file)
            val tx: FragmentTransaction = supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mp_container, fragment, fragment.javaClass.simpleName)

            if (sharedView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tx.addSharedElement(sharedView, sharedView.transitionName)
                val sharedSet = TransitionSet()
                sharedSet.addTransition(ChangeImageTransform())
                sharedSet.addTransition(ChangeBounds())
                sharedSet.addTransition(ChangeClipBounds())
                //                sharedSet.addTransition(new ChangeTransform());
                val commonSet = TransitionSet()
                commonSet.addTransition(Fade())
                fragment.sharedElementEnterTransition = sharedSet
                fragment.sharedElementReturnTransition = sharedSet
                fragment.enterTransition = commonSet
                fragment.returnTransition = commonSet
            }
            tx.addToBackStack(null).commit()
            mFragment = fragment
        }
    }

    fun submitResult(files: List<MediaFile>?) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES, ArrayList(files))
        setResult(RESULT_OK, intent)
        finish()
    }


    private fun checkFiles(pathName: String) {
        val folderFile = File(pathName)
        val files = folderFile.listFiles()
        if (files != null) {
            for (file in files) {
                // checking the File is file or directory
                if (file.isFile) {
                    val extension = file.absolutePath.substring(file.absolutePath.lastIndexOf(".") + 1)
                    var mime: String? = null
                    if (extension.equals("mp4", ignoreCase = true)) {
                        mime = "video/mp4"
                    } else if (extension.equals("jpg", ignoreCase = true)) {
                        mime = "image/jpeg"
                    }
                    if (mime != null) {
                        Timber.d("Scan file: %s", file.absolutePath)
                        MediaScannerConnection.scanFile(applicationContext, arrayOf(file.parent), arrayOf(mime), null)
                    }
                } else if (file.isDirectory) {
                    checkFiles(file.absolutePath)
                }
            }
        }
    }

    private fun resolveLastFragment() {
        val index: Int = supportFragmentManager.backStackEntryCount - 1
        if (index >= 0) {
            val backEntry: FragmentManager.BackStackEntry = supportFragmentManager.getBackStackEntryAt(index)
            val tag = backEntry.name
            if (tag != null) {
                mFragment = supportFragmentManager.findFragmentByTag(tag) as PickerFileSystemFragment
            }
        }
    }

    class PickMediaFiles : ActivityResultContract<PickerConfig, List<MediaFile>>() {
        override fun createIntent(context: Context, input: PickerConfig): Intent {
            val intent = Intent(context, PickerActivity::class.java)
            intent.putExtra(PickerConst.EXTRA_CONFIG, input)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): List<MediaFile> {
            val emptyRes = emptyList<MediaFile>()
            if (resultCode == Activity.RESULT_OK) {
                if (intent == null) {
                    return emptyRes
                }

                return intent.getParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES) ?: emptyRes
            }
            return emptyRes
        }

    }


    /**
     * Uses new ActivityResult API, so after prepare, you should take result of {@link Builder#prepare(onResult: (List<MediaFile>) -> Unit)} method and then
     * call {@link ActivityResultLauncher#launch(PickerConfig)}
     */
    class Builder {
        private var activity: WeakReference<ComponentActivity>? = null
        private var fragment: WeakReference<Fragment>? = null

        constructor(from: ComponentActivity) {
            activity = WeakReference(from)
        }

        constructor(from: Fragment) {
            fragment = WeakReference(from)
        }

        fun prepare(onResult: (List<MediaFile>) -> Unit): ActivityResultLauncher<PickerConfig> {
            return when {
                activity != null && activity?.get() != null -> {
                    activity!!.get()!!.registerForActivityResult(PickMediaFiles(), onResult)
                }
                fragment != null && fragment?.get() != null -> {
                    fragment!!.get()!!.registerForActivityResult(PickMediaFiles(), onResult)
                }
                else -> {
                    throw IllegalStateException("Unable to start MultiPicker as source Activity or Fragment is null")
                }
            }
        }
    }
}