package com.edwardstock.multipicker.picker.views;

import android.view.View;

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.edwardstock.multipicker.data.MediaFile;
import com.edwardstock.multipicker.internal.views.ErrorView;

import java.util.List;

import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@StateStrategyType(OneExecutionStateStrategy.class)
public interface FilesView extends ErrorView, FileSystemView {
    void setAdapter(RecyclerView.Adapter<?> adapter);
    void setSelectionTitle(CharSequence title);
    void setOnSelectionClearListener(View.OnClickListener listener);
    void setOnSelectionDoneListener(View.OnClickListener listener);
    void setSelectionObserver(SelectionTracker.SelectionObserver<MediaFile> observer);
    void showEmpty();
    void startPreview(MediaFile file, View sharedView);
    void clearSelection();
    void finishWithResult();
    void showProgress();
    void hideProgress();
    void setSelectionSubmitEnabled(boolean enabled);
}
