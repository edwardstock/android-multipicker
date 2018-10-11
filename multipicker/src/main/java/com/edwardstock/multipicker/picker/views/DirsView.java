package com.edwardstock.multipicker.picker.views;

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.internal.views.ErrorView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@StateStrategyType(AddToEndSingleStrategy.class)
public interface DirsView extends ErrorView, FileSystemView {
    void setAdapter(RecyclerView.Adapter<?> adapter);
    void startFiles(Dir dir);
    void showProgress();
    void hideProgress();
}
