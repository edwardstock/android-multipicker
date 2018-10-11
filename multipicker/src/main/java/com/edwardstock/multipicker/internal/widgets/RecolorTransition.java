package com.edwardstock.multipicker.internal.widgets;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.transition.Transition;
import androidx.transition.TransitionValues;

/**
 * This transition tracks changes during scene changes to the
 * {@link View#setBackground(android.graphics.drawable.Drawable) background}
 * property of its target views (when the background is a
 * {@link ColorDrawable}, as well as the
 * {@link TextView#setTextColor(android.content.res.ColorStateList)
 * color} of the text for target TextViews. If the color changes between
 * scenes, the color change is animated.
 *
 * @hide
 */
public class RecolorTransition extends Transition {
    private static final String PROPNAME_BACKGROUND = "android:recolor:background";
    private static final String PROPNAME_TEXT_COLOR = "android:recolor:textColor";
    public RecolorTransition() {}
    public RecolorTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private void captureValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_BACKGROUND, transitionValues.view.getBackground());
        if (transitionValues.view instanceof TextView) {
            transitionValues.values.put(PROPNAME_TEXT_COLOR,
                    ((TextView)transitionValues.view).getCurrentTextColor());
        }
    }
    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }
    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }
    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues,
                                   TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        final View view = endValues.view;
        Drawable startBackground = (Drawable) startValues.values.get(PROPNAME_BACKGROUND);
        Drawable endBackground = (Drawable) endValues.values.get(PROPNAME_BACKGROUND);
        boolean changed = false;
        if (startBackground instanceof ColorDrawable && endBackground instanceof ColorDrawable) {
            ColorDrawable startColor = (ColorDrawable) startBackground;
            ColorDrawable endColor = (ColorDrawable) endBackground;
            if (startColor.getColor() != endColor.getColor()) {
                endColor.setColor(startColor.getColor());
                changed = true;
                return ObjectAnimator.ofArgb(endBackground, "color", startColor.getColor(),
                        endColor.getColor());
            }
        }
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            int start = (Integer) startValues.values.get(PROPNAME_TEXT_COLOR);
            int end = (Integer) endValues.values.get(PROPNAME_TEXT_COLOR);
            if (start != end) {
                textView.setTextColor(end);
                changed = true;
                return ObjectAnimator.ofArgb(textView, "textColor", start, end);
            }
        }
        return null;
    }
}
