# MultiPicker for Android

Library that helps to select photos and videos by your app using MediaStore API, also it can take photos and videos and save it to external storage

## Usage

**Please note, library fully optimized for Kotlin projects but it does not uses kotlin-specific features as coroutines etc..**

```kotlin
package com.example.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.edwardstock.multipicker.MultiPicker
import com.edwardstock.multipicker.data.MediaFile
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : AppCompatActivity() {
    lateinit var b: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Install logger
        Timber.plant(DebugTree())
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Get button from viewBinding
        val launch = b.launch

        // Configure MultiPicker and prepare it to launch and getting result
        // MultiPicker.create(`this`) means children of ComponentActivity or Fragment
        val picker = MultiPicker.create(this)
                .configure {
                    title("Select photos")
                    showVideos(true)
                    showPhotos(true)
                }
                // Pass callback to this function. Signature: (List<MediaFile>)->Unit
                .prepare(this::onResult)


        // Launch MultiPicker on button click
        launch.setOnClickListener {
            picker.start()
        }
    }

    // Then just get list of selected files
    private fun onResult(files: List<MediaFile>) {
        b.files.text = null
        for (item in files) {
            b.files.append(item.toString())
            b.files.append("\n")
        }
    }
}
```

## Add to project

1. Add repository to *allProjects*
```groovy
allprojects {
    repositories {
        // bla bla bla
        maven { url "https://edwardstock.jfrog.io/artifactory/android/" }

    }
}
```

2. Add gradle dependency
```groovy
dependencies {

    implementation "com.edwardstock.android:multipicker:3.1.0"

}
```

3. Done!