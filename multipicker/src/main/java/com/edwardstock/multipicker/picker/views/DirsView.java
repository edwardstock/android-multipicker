package com.edwardstock.multipicker.picker.views;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import com.edwardstock.multipicker.data.Dir;


/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public interface DirsView extends PickerView {
    void setAdapter(RecyclerView.Adapter<?> adapter);
    void startFiles(Dir dir);
    void showProgress();
    void hideProgress();
    void showEmpty(boolean show);
    void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener);
    void showRefreshProgress();
    void hideRefreshProgress();
}
