package dev.ragnarok.fenrir.fragment;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.LocalPhotoAlbumsAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.LocalPhotoAlbumsPresenter;
import dev.ragnarok.fenrir.mvp.view.ILocalPhotoAlbumsView;
import dev.ragnarok.fenrir.picasso.Content_Local;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class LocalImageAlbumsFragment extends BaseMvpFragment<LocalPhotoAlbumsPresenter, ILocalPhotoAlbumsView>
        implements LocalPhotoAlbumsAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener, ILocalPhotoAlbumsView {

    private final AppPerms.doRequestPermissions requestReadPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(LocalPhotoAlbumsPresenter::fireReadExternalStoregePermissionResolved));
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyTextView;
    private LocalPhotoAlbumsAdapter mAlbumsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_albums_gallery, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (!hasHideToolbarExtra()) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

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

        int columnCount = getResources().getInteger(R.integer.photos_albums_column_count);
        RecyclerView.LayoutManager manager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView = view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(LocalPhotoAlbumsAdapter.PICASSO_TAG));

        mAlbumsAdapter = new LocalPhotoAlbumsAdapter(requireActivity(), Collections.emptyList(), Content_Local.PHOTO);
        mAlbumsAdapter.setClickListener(this);

        mRecyclerView.setAdapter(mAlbumsAdapter);

        mEmptyTextView = view.findViewById(R.id.empty);
        return view;
    }

    @Override
    public void onClick(LocalImageAlbum album) {
        callPresenter(p -> p.fireAlbumClick(album));
    }

    @Override
    public void onRefresh() {
        callPresenter(LocalPhotoAlbumsPresenter::fireRefresh);
    }

    @Override
    public void displayData(@NonNull List<LocalImageAlbum> data) {
        if (Objects.nonNull(mRecyclerView)) {
            mAlbumsAdapter.setData(data);
        }
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
    public void openAlbum(@NonNull LocalImageAlbum album) {
        PlaceFactory.getLocalImageAlbumPlace(album).tryOpenWith(requireActivity());
    }

    @Override
    public void notifyDataChanged() {
        if (Objects.nonNull(mAlbumsAdapter)) {
            mAlbumsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void requestReadExternalStoragePermission() {
        requestReadPermission.launch();
    }

    @NonNull
    @Override
    public IPresenterFactory<LocalPhotoAlbumsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LocalPhotoAlbumsPresenter(saveInstanceState);
    }
}