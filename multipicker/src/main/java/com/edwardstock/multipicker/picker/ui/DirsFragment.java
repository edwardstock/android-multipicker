package com.edwardstock.multipicker.picker.ui;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;
import com.edwardstock.multipicker.R;
import com.edwardstock.multipicker.R2;
import com.edwardstock.multipicker.data.Dir;
import com.edwardstock.multipicker.internal.MediaFileLoader;
import com.edwardstock.multipicker.PickerConfig;
import com.edwardstock.multipicker.picker.PickerConst;
import com.edwardstock.multipicker.picker.views.DirsPresenter;
import com.edwardstock.multipicker.picker.views.DirsView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * android-multipicker. 2018
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
@RuntimePermissions
public class DirsFragment extends PickerFileSystemFragment implements DirsView {

    @InjectPresenter(type = PresenterType.LOCAL) DirsPresenter presenter;
    @BindView(R2.id.list) RecyclerView list;
    @BindView(R2.id.mp_selection_title) TextView selectionTitle;
    @BindView(R2.id.mp_selection_action_clear) ImageView selectionClearAction;
    @BindView(R2.id.mp_selection_action_done) ImageView selectionDoneAction;
    @BindView(R2.id.selection_root) View selectionRoot;
    @BindView(R2.id.progress) ProgressBar progress;
    private PickerConfig mConfig;

    public static DirsFragment newInstance(PickerConfig config) {
        Bundle args = new Bundle();
        args.putParcelable(PickerConst.EXTRA_CONFIG, config);

        DirsFragment fragment = new DirsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setAdapter(final RecyclerView.Adapter<?> adapter) {
        boolean isTablet = getResources().getBoolean(R.bool.mp_isTablet);
        int spanCount = isTablet ? mConfig.getDirColumnsTablet() : mConfig.getDirColumns();
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        setGridSpacingItemDecoration(list, spanCount);
        list.setLayoutManager(layoutManager);
        list.setHasFixedSize(true);
        list.setAdapter(adapter);
    }

    @Override
    public void startFiles(Dir dir) {
        safeActivity(act -> act.startFiles(dir));
    }

    @Override
    public void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DirsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onError(CharSequence error) {
        safeActivity(act -> act.onError(error));
    }

    @Override
    public void onError(Throwable t) {
        safeActivity(act -> act.onError(t));
    }

    @Override
    public void startUpdateFiles() {
        presenter.updateFiles(new MediaFileLoader(getActivity()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mp_fragment_filesystem, container, false);
        ButterKnife.bind(this, view);
        selectionRoot.setVisibility(View.GONE);
        mConfig = getArguments().getParcelable(PickerConst.EXTRA_CONFIG);

        presenter.handleExtras(getArguments());

        DirsFragmentPermissionsDispatcher.setLoaderWithPermissionCheck(this);

        return view;
    }

    @Override
    public DirsPresenter getPresenter() {
        return presenter;
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void setLoader() {
        presenter.updateFiles(new MediaFileLoader(getActivity()));
    }


}
