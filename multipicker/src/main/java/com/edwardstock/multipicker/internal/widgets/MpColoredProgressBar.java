package com.edwardstock.multipicker.internal.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;

/**
 * MinterWallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class MpColoredProgressBar extends ProgressBar {

    public MpColoredProgressBar(Context context) {
        super(context);
    }

    public MpColoredProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public MpColoredProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public MpColoredProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.MpColoredProgressBar, defStyleAttr, defStyleRes);
        int color = arr.getColor(R.styleable.MpColoredProgressBar_mp_color, getResources().getColor(R.color.mp_colorPrimary));
        arr.recycle();

        setColor(color);
    }

    public void setColor(int color) {
        getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
