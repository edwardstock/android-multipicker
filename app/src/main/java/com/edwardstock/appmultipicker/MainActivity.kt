package com.edwardstock.appmultipicker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.edwardstock.appmultipicker.databinding.ActivityMainBinding
import com.edwardstock.multipicker.MultiPicker
import com.edwardstock.multipicker.data.MediaFile
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : AppCompatActivity() {
    lateinit var b: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(DebugTree())
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val launch = b.launch
        val picker = MultiPicker.create(this)
                .configure {
                    title("Select photos")
                    showVideos(true)
                    showPhotos(true)
                    limit(2)
                }
                .prepare(this::onResult)


        launch.setOnClickListener {
            picker.start()
        }

    }

    private fun onResult(files: List<MediaFile>) {
        b.files.text = null
        for (item in files) {
            b.files.append(item.toString())
            b.files.append("\n")
        }
    }
}