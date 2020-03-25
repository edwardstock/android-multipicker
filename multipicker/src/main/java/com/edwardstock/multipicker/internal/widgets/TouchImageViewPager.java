package com.edwardstock.multipicker.internal.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import timber.log.Timber;

/**
 * Dogsy. 2017
 *
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */

public class TouchImageViewPager extends ViewPager {
    public final static String VIEW_PAGER_OBJECT_TAG = "image#";
    private int mPrevPosition = 0;

    public TouchImageViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public TouchImageViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                // zoom out on page change
                if (state == SCROLL_STATE_SETTLING && mPrevPosition != getCurrentItem()) {
                    try {
                        final ViewGroup parent = ((ViewGroup) getChildAt(0));

                        Object prevImage = parent.findViewWithTag(VIEW_PAGER_OBJECT_TAG + getCurrentItem());
                        if (prevImage instanceof PhotoView) {
                            ((PhotoView) prevImage).setScale(1f);
                        }

                        mPrevPosition = getCurrentItem();
                    } catch (ClassCastException ex) {
                        Timber.d(ex, "Unable to findStream PhotoView");
                    }
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
