package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.VideosAdapter;
import dev.ragnarok.fenrir.fragment.search.criteria.VideoSearchCriteria;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.search.VideosSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IVideosSearchView;

public class VideoSearchFragment extends AbsSearchFragment<VideosSearchPresenter, IVideosSearchView, Video, VideosAdapter>
        implements VideosAdapter.VideoOnClickListener {

    public static VideoSearchFragment newInstance(int accountId, @Nullable VideoSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        VideoSearchFragment fragment = new VideoSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(VideosAdapter adapter, List<Video> data) {
        adapter.setData(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    VideosAdapter createAdapter(List<Video> data) {
        VideosAdapter adapter = new VideosAdapter(requireActivity(), data);
        adapter.setVideoOnClickListener(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columns = getResources().getInteger(R.integer.videos_column_count);
        return new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onVideoClick(int position, Video video) {
        callPresenter(p -> p.fireVideoClick(video));
    }

    @Override
    public boolean onVideoLongClick(int position, Video video) {
        return false;
    }

    @NonNull
    @Override
    public IPresenterFactory<VideosSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new VideosSearchPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }
}