package com.edwardstock.multipicker.internal.widgets

import android.annotation.TargetApi
import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.ProgressBar
import com.edwardstock.multipicker.R

/**
 * MinterWallet. 2018
 * @author Eduard Maximovich <edward.vstock></edward.vstock>@gmail.com>
 */
class MpColoredProgressBar : ProgressBar {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, 0)
    }

    @TargetApi(21)
    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr, defStyleRes)
    }

    private fun init(attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val arr = context.obtainStyledAttributes(attrs, R.styleable.MpColoredProgressBar, defStyleAttr, defStyleRes)
        val color = arr.getColor(R.styleable.MpColoredProgressBar_mp_color, resources.getColor(R.color.mp_colorPrimary))
        arr.recycle()
        setColor(color)
    }

    fun setColor(color: Int) {
        indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}