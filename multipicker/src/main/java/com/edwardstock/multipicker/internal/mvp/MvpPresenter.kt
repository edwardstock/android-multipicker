package com.edwardstock.multipicker.internal.mvp

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
abstract class MvpPresenter<View : MvpView> : LifecycleObserver {
    private val mView: ViewProxy<View> = ViewProxy()
    protected var lifecycle: Lifecycle? = null
        private set
    protected var isViewAttached = false
        private set
    private var mIsFirstAttached = false
    fun callOnView(view: (View) -> Unit) {
        if (mView.hasView()) {
            view(mView.view!!)
        } else {
            mView.defer { view(mView.view!!) }
        }
    }

    fun <T> attachToLifecycle(registryOwner: T) where T : LifecycleOwner?, T : MvpView? {
        lifecycle = registryOwner!!.lifecycle
        lifecycle!!.addObserver(this)
        Timber.d("Attaching lifecycle")
    }

    fun attachView(view: View) {
        if (isViewAttached) return
        mView.attachView(view)
        mView.callDeferred()
        Timber.d("Attaching view %s", mView.javaClass.canonicalName)
    }

    open fun onSaveInstanceState(outState: Bundle?) {}
    open fun onRestoreSavedState(savedState: Bundle?) {}

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun onViewAttach() {
        if (!mIsFirstAttached) {
            onFirstViewAttach()
            mIsFirstAttached = true
        }
        Timber.d("OnViewAttach")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected open fun onViewDetach() {
        Timber.d("OnViewDetach")
        isViewAttached = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected fun onViewDestroy() {
        isViewAttached = false
        mView.destroyView()
        Timber.d("OnViewDestroy")
    }

    protected open fun onFirstViewAttach() {
        Timber.d("OnFirstViewAttach")
    }

}