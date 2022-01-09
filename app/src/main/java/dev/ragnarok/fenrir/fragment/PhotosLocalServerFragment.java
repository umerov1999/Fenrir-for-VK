package dev.ragnarok.fenrir.fragment;

import static android.app.Activity.RESULT_OK;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.LocalServerPhotosAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.PhotosLocalServerPresenter;
import dev.ragnarok.fenrir.mvp.view.IPhotosLocalServerView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class PhotosLocalServerFragment extends BaseMvpFragment<PhotosLocalServerPresenter, IPhotosLocalServerView>
        implements MySearchView.OnQueryTextListener, LocalServerPhotosAdapter.PhotoSelectionListener, IPhotosLocalServerView {
    private final ActivityResultLauncher<Intent> requestPhotoUpdate = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    postPresenterReceive(p -> p.updateInfo(result.getData().getExtras().getInt(Extra.POSITION), result.getData().getExtras().getLong(Extra.PTR)));
                }
            });
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LocalServerPhotosAdapter mPhotoRecyclerAdapter;
    private RecyclerView recyclerView;

    public static PhotosLocalServerFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        PhotosLocalServerFragment fragment = new PhotosLocalServerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void scrollTo(int position) {
        mPhotoRecyclerAdapter.updateCurrentPosition(position);
        recyclerView.scrollToPosition(position);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_local_server_photo, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        MySearchView searchView = root.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(this);
        searchView.setRightButtonVisibility(true);
        searchView.setRightIcon(R.drawable.ic_recent);
        searchView.setLeftIcon(R.drawable.magnify);
        searchView.setOnAdditionalButtonClickListener(() -> callPresenter(PhotosLocalServerPresenter::toggleReverse));
        searchView.setQuery("", true);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(p -> p.fireRefresh(false)));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        recyclerView = root.findViewById(R.id.recycler_view);
        int columns = requireActivity().getResources().getInteger(R.integer.photos_column_count);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(PhotosLocalServerPresenter::fireScrollToEnd);
            }
        });
        mPhotoRecyclerAdapter = new LocalServerPhotosAdapter(requireActivity(), Collections.emptyList());
        mPhotoRecyclerAdapter.setPhotoSelectionListener(this);
        recyclerView.setAdapter(mPhotoRecyclerAdapter);
        return root;
    }

    @NonNull
    @Override
    public IPresenterFactory<PhotosLocalServerPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new PhotosLocalServerPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<Photo> photos) {
        if (nonNull(mPhotoRecyclerAdapter)) {
            mPhotoRecyclerAdapter.setData(photos);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.on_server);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_PHOTOS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mPhotoRecyclerAdapter)) {
            mPhotoRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    @Override
    public void displayGallery(int accountId, int albumId, int ownerId, @NonNull TmpSource source, int position, boolean reversed) {
        PlaceFactory.getPhotoAlbumGalleryPlace(accountId, albumId, ownerId, source, position, true, reversed).tryOpenWith(requireActivity());
    }

    @Override
    public void displayGalleryUnSafe(int accountId, int albumId, int ownerId, long parcelNativePointer, int position, boolean reversed) {
        PlaceFactory.getPhotoAlbumGalleryPlace(accountId, albumId, ownerId, parcelNativePointer, position, true, reversed).setActivityResultLauncher(requestPhotoUpdate).tryOpenWith(requireActivity());
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mPhotoRecyclerAdapter)) {
            mPhotoRecyclerAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mPhotoRecyclerAdapter)) {
            mPhotoRecyclerAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        callPresenter(p -> p.fireSearchRequestChanged(query));
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        callPresenter(p -> p.fireSearchRequestChanged(newText));
        return false;
    }

    @Override
    public void onPhotoClicked(int position, Photo photo) {
        callPresenter(p -> p.firePhotoClick(photo));
    }
}
