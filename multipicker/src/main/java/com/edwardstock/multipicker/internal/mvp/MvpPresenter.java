package com.edwardstock.multipicker.internal.mvp;

import android.os.Bundle;

import com.edwardstock.multipicker.internal.helpers.Acceptor;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import timber.log.Timber;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public abstract class MvpPresenter<View extends MvpView> implements LifecycleObserver {
    private ViewProxy<View> mView;
    private Lifecycle mLifecycle;
    private boolean mIsViewAttached = false;
    private boolean mIsFirstAttached = false;

    public MvpPresenter() {
        mView = new ViewProxy<>();
    }

    public void callOnView(final Acceptor<View> view) {
        if (mView.hasView()) {
            view.call(mView.getView());
        } else {
            mView.defer(() -> view.call(mView.getView()));
        }
    }

    public View getView() {
        if (mView.hasView()) {
            return mView.getView();
        } else {
            return null;
        }
    }

    public final <T extends LifecycleOwner & MvpView> void attachToLifecycle(T registryOwner) {
        mLifecycle = registryOwner.getLifecycle();
        mLifecycle.addObserver(this);
        Timber.d("Attaching lifecycle");
    }

    public final void attachView(View view) {
        if (mIsViewAttached) return;
        mView.attachView(view);
        mView.callDeferred();
        Timber.d("Attaching view %s", mView.getClass().getCanonicalName());
    }

    public void onSaveInstanceState(Bundle outState) {

    }

    public void onRestoreSavedState(Bundle savedState) {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onViewAttach() {
        if (!mIsFirstAttached) {
            onFirstViewAttach();
            mIsFirstAttached = true;
        }

        Timber.d("OnViewAttach");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected void onViewDetach() {
        Timber.d("OnViewDetach");
        mIsViewAttached = false;

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void onViewDestroy() {
        mIsViewAttached = false;
        mView.destroyView();
        Timber.d("OnViewDestroy");
    }

    protected Lifecycle getLifecycle() {
        return mLifecycle;
    }

    protected boolean isViewAttached() {
        return mIsViewAttached;
    }

    protected void onFirstViewAttach() {
        Timber.d("OnFirstViewAttach");
    }


}
