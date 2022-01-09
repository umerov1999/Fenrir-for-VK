package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.LocalPhotosAdapter;
import dev.ragnarok.fenrir.adapter.LocalVideosAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.InternalVideoSize;
import dev.ragnarok.fenrir.model.LocalVideo;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.LocalVideosPresenter;
import dev.ragnarok.fenrir.mvp.view.ILocalVideosView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class LocalVideosFragment extends BaseMvpFragment<LocalVideosPresenter, ILocalVideosView>
        implements ILocalVideosView, LocalVideosAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener {


    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LocalVideosAdapter mAdapter;
    private TextView mEmptyTextView;
    private FloatingActionButton fabAttach;

    public static LocalVideosFragment newInstance() {
        return new LocalVideosFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_gallery, container, false);
        view.findViewById(R.id.toolbar).setVisibility(View.GONE);

        MySearchView mySearchView = view.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callPresenter(p -> p.fireSearchRequestChanged(query, false));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callPresenter(p -> p.fireSearchRequestChanged(newText, false));
                return false;
            }
        });

        mSwipeRefreshLayout = view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        int columnCount = getResources().getInteger(R.integer.local_gallery_column_count);
        RecyclerView.LayoutManager manager = new GridLayoutManager(requireActivity(), columnCount);

        mRecyclerView = view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(LocalPhotosAdapter.TAG));

        mEmptyTextView = view.findViewById(R.id.empty);

        fabAttach = view.findViewById(R.id.fr_video_gallery_attach);
        fabAttach.setOnClickListener(v -> callPresenter(LocalVideosPresenter::fireFabClick));

        return view;
    }

    @Override
    public void onVideoClick(LocalVideosAdapter.ViewHolder holder, LocalVideo video) {
        callPresenter(p -> p.fireVideoClick(video));
    }

    @Override
    public void onVideoLongClick(LocalVideosAdapter.ViewHolder holder, LocalVideo video) {

        Video target = new Video().setOwnerId(Settings.get().accounts().getCurrent()).setId((int) video.getId())
                .setMp4link1080(video.getData().toString()).setTitle(video.getTitle());
        PlaceFactory.getVkInternalPlayerPlace(target, InternalVideoSize.SIZE_1080, true).tryOpenWith(requireActivity());
    }

    @Override
    public void onRefresh() {
        callPresenter(LocalVideosPresenter::fireRefresh);
    }

    @Override
    public void displayData(@NonNull List<LocalVideo> data) {
        mAdapter = new LocalVideosAdapter(requireActivity(), data);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if (Objects.nonNull(mEmptyTextView)) {
            mEmptyTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayProgress(boolean loading) {
        if (Objects.nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(loading));
        }
    }

    @Override
    public void returnResultToParent(ArrayList<LocalVideo> videos) {

        Intent intent = new Intent();
        intent.putExtra(Extra.VIDEO, videos.get(0));

        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void updateSelectionAndIndexes() {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.updateHoldersSelectionAndIndexes();
        }
    }

    @Override
    public void setFabVisible(boolean visible, boolean anim) {
        if (visible && !fabAttach.isShown()) {
            fabAttach.show();
        }

        if (!visible && fabAttach.isShown()) {
            fabAttach.hide();
        }
    }

    @Override
    public void showError(String text) {
        if (isAdded()) Toast.makeText(requireActivity(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showError(@StringRes int titleRes, Object... params) {
        if (isAdded()) showError(getString(titleRes, params));
    }

    @NonNull
    @Override
    public IPresenterFactory<LocalVideosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LocalVideosPresenter(saveInstanceState);
    }
}
