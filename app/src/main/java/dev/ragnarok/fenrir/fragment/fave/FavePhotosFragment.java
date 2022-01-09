package dev.ragnarok.fenrir.fragment.fave;

import static android.app.Activity.RESULT_OK;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.fave.FavePhotosAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.FavePhotosPresenter;
import dev.ragnarok.fenrir.mvp.view.IFavePhotosView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.ViewUtils;

public class FavePhotosFragment extends BaseMvpFragment<FavePhotosPresenter, IFavePhotosView>
        implements SwipeRefreshLayout.OnRefreshListener, IFavePhotosView, FavePhotosAdapter.PhotoSelectionListener {

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FavePhotosAdapter mAdapter;
    private RecyclerView recyclerView;
    private final ActivityResultLauncher<Intent> requestPhotoUpdate = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    int ps = result.getData().getExtras().getInt(Extra.POSITION);
                    mAdapter.updateCurrentPosition(ps);
                    recyclerView.scrollToPosition(ps);
                }
            });

    public static FavePhotosFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FavePhotosFragment favePhotosFragment = new FavePhotosFragment();
        favePhotosFragment.setArguments(args);
        return favePhotosFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_photos, container, false);
        recyclerView = root.findViewById(android.R.id.list);
        mEmpty = root.findViewById(R.id.empty);

        int columns = getResources().getInteger(R.integer.photos_column_count);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), columns);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(FavePhotosPresenter::fireScrollToEnd);
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new FavePhotosAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setPhotoSelectionListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onRefresh() {
        callPresenter(FavePhotosPresenter::fireRefresh);
    }

    @Override
    public void onPhotoClicked(int position, Photo photo) {
        callPresenter(p -> p.firePhotoClick(position, photo));
    }

    @Override
    public void displayData(List<Photo> photos) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(photos);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void goToGallery(int accountId, ArrayList<Photo> photos, int position) {
        PlaceFactory.getFavePhotosGallery(accountId, photos, position).setActivityResultLauncher(requestPhotoUpdate)
                .tryOpenWith(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<FavePhotosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FavePhotosPresenter(requireArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }
}