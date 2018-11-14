package com.edwardstock.multipicker.internal.mvp;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import timber.log.Timber;

/**
 * endbox. 2017
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class ViewProxy<View extends MvpView> implements MvpView {
    private WeakReference<View> mView;
    private Queue<Task> mTasks = new LinkedList<>();

    final public void attachView(View view) {
        mView = new WeakReference<>(view);
    }

    final public void destroyView() {
        if (mView != null) {
            mView.clear();
        }
    }

    final public boolean hasView() {
        if (mView == null || mView.get() == null) {
            return false;
        }

        final View v = mView.get();
        return v != null;
    }

    final public void defer(Task task) {
        mTasks.add(task);
        Timber.d("Deferring task %s", task.toString());
    }

    final public View getView() {
        return mView.get();
    }

    final public void callDeferred() {
        while (!mTasks.isEmpty()) {
            final Task task = mTasks.poll();
            task.call();
            Timber.d("Call deferred task %s", task.toString());
        }
    }

    public interface Task {
        void call();
    }
}
