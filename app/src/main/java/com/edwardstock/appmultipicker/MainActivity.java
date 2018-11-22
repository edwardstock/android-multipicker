package com.edwardstock.appmultipicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.edwardstock.multipicker.MultiPicker;
import com.edwardstock.multipicker.data.MediaFile;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    static int REQ = 100;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_main);
        result = findViewById(R.id.result);

        Button launch = findViewById(R.id.launch);

        launch.setOnClickListener(v -> {
            MultiPicker.create(this)
                    .configure()
                    .title("AAA")
                    .showVideos(true)
                    .showPhotos(true)
//                    .limit(1)
                    .build()
                    .start(REQ);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ) {
            List<MediaFile> files;
            try {
                files = MultiPicker.handleActivityResult(resultCode, data);
                Timber.d("OnResult: %d", resultCode);
                StringBuilder sb = new StringBuilder();
                for (MediaFile item : files) {
                    sb.append(item.toString());
                }
                result.setText(sb.toString());
            } catch (IOException e) {
                Timber.e(e);
                result.setText(e.getMessage());
            }

        }
    }
}
