package com.edwardstock.multipicker.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.edwardstock.multipicker.BuildConfig

class MultiPickerFileProvider : FileProvider() {

    companion object {
        const val PROVIDER_NAME = BuildConfig.LIBRARY_PACKAGE_NAME + ".internal.MultiPickerFileProvider"
        const val EXT_CACHE_DIR = "cached_shared_files"

        fun getAuthority(context: Context): String {
            return "${context.packageName}.multipicker.provider"
        }

        fun grantFilePermissions(context: Context, uri: Uri, writeAble: Boolean = false) {
            var flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            if (writeAble) {
                flag = flag or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            }
            context.grantUriPermission("com.android.provider.DataSharing", uri, flag)
        }
    }

}