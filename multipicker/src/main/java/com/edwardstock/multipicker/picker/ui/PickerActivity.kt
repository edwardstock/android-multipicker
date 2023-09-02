package com.edwardstock.multipicker.picker.ui


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.ChangeClipBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.edwardstock.multipicker.PickerConfig
import com.edwardstock.multipicker.R
import com.edwardstock.multipicker.data.Dir
import com.edwardstock.multipicker.data.MediaFile
import com.edwardstock.multipicker.databinding.MpActivityPickerBinding
import com.edwardstock.multipicker.internal.MultiPickerFileProvider
import com.edwardstock.multipicker.internal.PickerSavePath
import com.edwardstock.multipicker.internal.PickerUtils
import com.edwardstock.multipicker.internal.views.ErrorView
import com.edwardstock.multipicker.picker.PickerConst
import com.edwardstock.multipicker.picker.getParcelableArrayListExtraCompat
import com.edwardstock.multipicker.picker.getParcelableCompat
import com.edwardstock.multipicker.picker.views.PickerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

class PickerActivity : AppCompatActivity(), PickerView, ErrorView {
    lateinit var b: MpActivityPickerBinding
    private var config: PickerConfig? = null
    private var fragment: PickerFileSystemFragment? = null

    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null
    private var takePictureFile: PickerSavePath? = null
    private var takeVideoLauncher: ActivityResultLauncher<Uri>? = null
    private var takeVideoFile: PickerSavePath? = null

    private enum class CaptureType {
        Photo, Video
    }

    private var captureType: CaptureType? = null

    private val launchPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            when (captureType) {
                CaptureType.Photo -> capturePhotoInternal()
                CaptureType.Video -> captureVideoInternal()
                else -> Unit
            }
        } else {
            when (captureType) {
                CaptureType.Photo -> Toast.makeText(this, R.string.mp_error_create_image_file, Toast.LENGTH_LONG).show()
                CaptureType.Video -> Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }
    }

    override fun capturePhotoWithPermissions() {
        captureType = CaptureType.Photo
        launchPermissionRequest.launch(capturePerms)
    }

    override fun captureVideoWithPermissions() {
        captureType = CaptureType.Video
        launchPermissionRequest.launch(capturePerms)
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

    private fun captureVideoInternal() {
        try {
            takeVideoFile = PickerSavePath.newTimestampTmpFile(this, "mp4")
            takeVideoLauncher?.launch(takeVideoFile!!.toUriProvider(this))
        } catch (t: Throwable) {
            Timber.e(t, "Unable to take video")
            Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show()
        }
    }

    private fun onCapturedPhoto(saved: Boolean) {
        if (!saved) {
            Toast.makeText(this, R.string.mp_error_create_image_file, Toast.LENGTH_LONG).show()
        } else {
            val file = takePictureFile!!.toFile(this)!!
            val uri = FileProvider.getUriForFile(this, MultiPickerFileProvider.getAuthority(this), file)
            val mf = MediaFile(
                    0,
                    file.name,
                    file.absolutePath,
                    uri
            )
            try {
                PickerUtils.writeCapturedMedia(this, mf)
                lifecycleScope.launch {
                    takePictureFile = null
                    startUpdateFiles()
                }
            } catch (e: Throwable) {
                Timber.e(e)
                Toast.makeText(this, R.string.mp_error_create_image_file, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onCapturedVideo(captured: Boolean) {
        if (captured) {
            val file = takeVideoFile!!.toFile(this)!!
            val uri = FileProvider.getUriForFile(this, MultiPickerFileProvider.getAuthority(this), file)

            val mf = MediaFile(
                    0,
                    file.name,
                    file.absolutePath,
                    uri
            )
            if (!mf.uri.exists() || mf.uri.length() == 0L) {
                Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show()
                Timber.w("Unable to handle recorded video: file not exists or is empty")
                return
            }

            try {
                PickerUtils.writeCapturedMedia(this, mf)
                lifecycleScope.launch {
                    takeVideoFile = null
                    startUpdateFiles()
                }
            } catch (e: Throwable) {
                Timber.e(e)
                Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, R.string.mp_error_create_video_file, Toast.LENGTH_LONG).show()
        }

    }

    override fun scanMedia(file: MediaFile, listener: MediaScannerConnection.OnScanCompletedListener) {
        val paths = arrayOf(file.uri)
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
        if (fragment != null && fragment?.isAdded == true) {
            fragment?.updateFiles()
        }
    }

    fun setupToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        if (item.itemId == R.id.menu_camera) {
            capturePhotoWithPermissions()
            return true
        } else if (item.itemId == R.id.menu_video) {
            captureVideoWithPermissions()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (config == null) {
            return true
        }
        if (config!!.isEnableCamera) {
            if (config!!.isShowPhotos && config!!.isShowVideos) {
                menuInflater.inflate(R.menu.mp_image_picker_menu_all_main, menu)
            } else if (config!!.isShowVideos) {
                menuInflater.inflate(R.menu.mp_image_picker_menu_video_main, menu)
            } else if (config!!.isShowPhotos) {
                menuInflater.inflate(R.menu.mp_image_picker_menu_photo_main, menu)
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, true) {
            handleBackPress()
        }
        config = intent?.getParcelableCompat(PickerConst.EXTRA_CONFIG)
        b = MpActivityPickerBinding.inflate(layoutInflater)
        setContentView(b.root)
        setResult(RESULT_CANCELED)
        setupToolbar(b.toolbar)
        b.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }

        if (config?.title != null) {
            b.toolbar.title = config?.title
        }

        if (config?.isShowPhotos == true && config?.isShowVideos == true) {
            takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture(), this::onCapturedPhoto)
            takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo(), this::onCapturedVideo)
        } else if (config?.isShowVideos == true) {
            takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo(), this::onCapturedVideo)
        } else if (config?.isShowPhotos == true) {
            takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture(), this::onCapturedPhoto)
        }

        if (savedInstanceState == null) {
            // If there is no saved instance state, create a new Fragment
            fragment = DirsFragment.newInstance(config)
            Timber.d("Show DirsFragment")
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mp_container, fragment!!, "files_fragment")
                    .addToBackStack(null)
                    .commit()
        } else {
            // If there is a saved instance state, get the Fragment by its tag
            fragment = supportFragmentManager.findFragmentByTag("files_fragment") as PickerFileSystemFragment
            Timber.d("Show ${fragment!!::class.simpleName} fragment from saved instance state")
        }
    }

    private fun handleBackPress() {
        fragment?.onBackPressed()
        var addedFile: MediaFile? = null
        if (fragment is ImageViewerFragment) {
            addedFile = (fragment as ImageViewerFragment).addedFile
        }
        if (supportFragmentManager.backStackEntryCount <= 1) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            resolveLastFragment()
            if (fragment is FilesFragment) {
                (fragment as FilesFragment).addSelection(addedFile)
            }
            supportFragmentManager.popBackStack()
        }
    }

    fun startFiles(dir: Dir?) {
        val fragment = FilesFragment.newInstance(config, dir)
        Timber.d("Show FilesFragment")
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.mp_container, fragment, "files_fragment")
                .addToBackStack(null)
                .commit()
        this.fragment = fragment
    }

    fun startPreview(dir: Dir?, file: MediaFile, sharedView: View?) {
        if (file.isVideo) {
            val intent = Intent(Intent.ACTION_VIEW, file.uri)
            intent.setDataAndType(file.uri, "video/*")
            startActivity(intent)
        } else {
            val fragment = ImageViewerFragment.newInstance(dir, file)
            val tx: FragmentTransaction = supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mp_container, fragment, fragment.javaClass.simpleName)

            if (sharedView != null) {
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
            this.fragment = fragment
        }
    }

    fun submitResult(files: List<MediaFile>) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && config?.copyUnreadableFilesToCache == true) {
            lifecycleScope.launch(Dispatchers.IO) {
                files
                        .filter { !it.uri.canRead() }
                        .forEach { source ->
                            val mediaPath = File(externalCacheDir, MultiPickerFileProvider.EXT_CACHE_DIR).also { it.mkdirs() }
                            val mediaFile = File(mediaPath, source.name)

                            // if target file does not exists OR it does but length isn't the same
                            if (mediaFile.exists().not() || mediaFile.length() != source.size) {
                                mediaFile.delete()
                                Timber.d("Copy ${source.uri} to local cache")

                                mediaFile.createNewFile()
                                contentResolver.openInputStream(source.uri)?.use { inputStream ->
                                    mediaFile.outputStream().use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }

                                val contentUri: Uri = FileProvider.getUriForFile(this@PickerActivity, MultiPickerFileProvider.getAuthority(this@PickerActivity), mediaFile)
                                MultiPickerFileProvider.grantFilePermissions(this@PickerActivity, contentUri, true)
                                source.uri = mediaFile.absolutePath
                            }
                        }

                withContext(Dispatchers.Main) {
                    intent.putParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES, ArrayList(files))
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        } else {
            intent.putParcelableArrayListExtra(PickerConst.EXTRA_RESULT_FILES, ArrayList(files))
            setResult(RESULT_OK, intent)
            finish()
        }

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
                fragment = supportFragmentManager.findFragmentByTag(tag) as PickerFileSystemFragment
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

                return intent.getParcelableArrayListExtraCompat(PickerConst.EXTRA_RESULT_FILES) ?: emptyRes
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