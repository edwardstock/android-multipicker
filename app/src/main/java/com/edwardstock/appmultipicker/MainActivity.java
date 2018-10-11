package com.edwardstock.appmultipicker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.edwardstock.multipicker.MultiPicker;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.ConstraintTransitionAnimator;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    static int REQ = 100;
    private ConstraintLayout mRoot;
    private ImageView selection;
    private ConstraintTransitionAnimator mAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_main);

        Button launch = findViewById(R.id.launch);
        mRoot = findViewById(R.id.layout_anim);
        selection = findViewById(R.id.anim_selection);

        mAnim = new ConstraintTransitionAnimator(mRoot, R.layout.inc_anim_text_off, R.layout.inc_anim_text_on);
        mAnim.setOnBeforeApplyListener((root, set, prevApplied) -> selection.setSelected(prevApplied));


        launch.setOnClickListener(v -> {
            MultiPicker.create(this)
                    .enableCamera()

                    .build()
                    .start(REQ);
        });

        Button animate = findViewById(R.id.animate);
        animate.setOnClickListener(v -> {
            mAnim.toggle();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ) {
            List<MediaFile> files = MultiPicker.handleActivityResult(resultCode, data);
            Timber.d("OnResult: %d", resultCode);
            files.forEach(item -> {
                Timber.d("Selected file: %s", item);
            });


        }
    }
}
