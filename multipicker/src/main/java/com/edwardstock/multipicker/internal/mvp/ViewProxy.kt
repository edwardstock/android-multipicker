package com.edwardstock.multipicker.internal.mvp

import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

/**
 * endbox. 2017
 * @author Eduard Maximovich <edward.vstock></edward.vstock>@gmail.com>
 */
class ViewProxy<View : MvpView?> : MvpView {
    private var mView: WeakReference<View>? = null
    private val mTasks: Queue<() -> Unit> = LinkedList()
    fun attachView(view: View) {
        mView = WeakReference(view)
    }

    fun destroyView() {
        if (mView != null) {
            mView!!.clear()
        }
    }

    fun hasView(): Boolean {
        return mView != null && mView!!.get() != null && !mView!!.isEnqueued
    }

    fun defer(task: () -> Unit) {
        mTasks.add(task)
        Timber.d("Deferring task %s", task.toString())
    }

    val view: View?
        get() = mView!!.get()

    fun callDeferred() {
        while (!mTasks.isEmpty()) {
            val task = mTasks.poll()
            task!!.invoke()
        }
    }
}