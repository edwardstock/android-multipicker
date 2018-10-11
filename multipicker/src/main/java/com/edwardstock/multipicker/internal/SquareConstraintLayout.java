package com.edwardstock.multipicker.internal;

import android.content.Context;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class SquareConstraintLayout extends ConstraintLayout {
    public SquareConstraintLayout(Context context) {
        super(context);
    }

    public SquareConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
