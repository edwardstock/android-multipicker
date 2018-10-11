package com.edwardstock.multipicker.internal;

import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.lang.ref.WeakReference;

import androidx.annotation.LayoutRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class ConstraintTransitionAnimator {
    private WeakReference<ConstraintLayout> mRoot;
    private ConstraintSet mSource;
    private ConstraintSet mTarget;
    private boolean mOn;
    private OnBeforeApplyListener mOnBeforeApplyListener;

    public ConstraintTransitionAnimator(ConstraintLayout root, @LayoutRes int sourceLayoutId, @LayoutRes int targetLayoutId) {
        mRoot = new WeakReference<>(root);
        mTarget = new ConstraintSet();
        mTarget.clone(root.getContext(), targetLayoutId);
        mSource = new ConstraintSet();
        mSource.clone(root.getContext(), sourceLayoutId);
    }

    public void setOnBeforeApplyListener(OnBeforeApplyListener listener) {
        mOnBeforeApplyListener = listener;
    }

    public void toggle(final boolean enable, ConstraintLayout forNewSet) {
        Timber.d("Enable: %b", enable);

        new Handler(Looper.getMainLooper()).post(()->{
            mRoot.clear();
            mRoot = new WeakReference<>(forNewSet);
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(new AutoTransition());
            transitionSet.setDuration(150);
            transitionSet.setInterpolator(new AccelerateDecelerateInterpolator());

            TransitionManager.beginDelayedTransition(mRoot.get(), transitionSet);
            final ConstraintSet newTarget;

            if (enable) {
                newTarget = mTarget;
            } else {
                newTarget = mSource;
            }
            if (mOnBeforeApplyListener != null) {
                mOnBeforeApplyListener.onBeforeApply(mRoot.get(), newTarget, enable);
            }
            Timber.d("Apply transition in thread: %s", Thread.currentThread().getName());
            newTarget.applyTo(mRoot.get());
//            if(enable) {
//                mSource.clone(forNewSet);
//            } else {
//                mTarget.clone(forNewSet);
//            }
        });
    }

    public void toggle(ConstraintLayout root) {
        ConstraintSet from = new ConstraintSet();
        from.clone(root);

        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new AutoTransition());
        transitionSet.setDuration(150);
        transitionSet.setInterpolator(new FastOutSlowInInterpolator());

        TransitionManager.beginDelayedTransition(root, transitionSet);
        ConstraintSet con;

        if (mOn) {
            con = from;
        } else {
            con = mTarget;
        }
        if (mOnBeforeApplyListener != null) {
            mOnBeforeApplyListener.onBeforeApply(root, con, !mOn);
        }
        con.applyTo(root);
        mOn = !mOn;
    }

    public void toggle() {
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new AutoTransition());
        transitionSet.setDuration(150);
        transitionSet.setInterpolator(new FastOutSlowInInterpolator());

        TransitionManager.beginDelayedTransition(mRoot.get(), transitionSet);
        ConstraintSet con;

        if (mOn) {
            con = mSource;
        } else {
            con = mTarget;
        }
        if (mOnBeforeApplyListener != null) {
            mOnBeforeApplyListener.onBeforeApply(mRoot.get(), con, !mOn);
        }
        con.applyTo(mRoot.get());
        mOn = !mOn;
    }

    public interface OnBeforeApplyListener {
        void onBeforeApply(ConstraintLayout root, ConstraintSet set, boolean prevApplied);
    }
}
