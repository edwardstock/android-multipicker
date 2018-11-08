package com.edwardstock.multipicker.picker.views;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.edwardstock.multipicker.data.MediaFile;

import java.io.File;

import androidx.recyclerview.selection.SelectionTracker;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public interface FilesView extends PickerView {
    void setAdapter(RecyclerView.Adapter<?> adapter);
    void setSelectionTitle(CharSequence title);
    void setOnSelectionClearListener(View.OnClickListener listener);
    void setOnSelectionDoneListener(View.OnClickListener listener);
    void setOnSelectionAllListener(View.OnClickListener listener);
    void setSelectionObserver(SelectionTracker.SelectionObserver<MediaFile> observer);
    void showEmpty();
    void startPreview(MediaFile file, View sharedView);
    void clearSelection();
    void finishWithResult();
    void showProgress();
    void hideProgress();
    void setSelectionSubmitEnabled(boolean enabled);
    void scrollTo(int position);
    void removeFileFromMediaDB(File file);
    void selectFile(MediaFile mediaFile);
    void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener);
    void showRefreshProgress();
    void hideRefreshProgress();
    void selectAll();
}
