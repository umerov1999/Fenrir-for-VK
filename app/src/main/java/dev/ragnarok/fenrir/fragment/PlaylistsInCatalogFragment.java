package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.AudioPlaylistsAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.PlaylistsInCatalogPresenter;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IPlaylistsInCatalogView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class PlaylistsInCatalogFragment extends BaseMvpFragment<PlaylistsInCatalogPresenter, IPlaylistsInCatalogView>
        implements IPlaylistsInCatalogView, AudioPlaylistsAdapter.ClickListener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioPlaylistsAdapter mAdapter;
    private String Header;
    private boolean inTabsContainer;

    public static PlaylistsInCatalogFragment newInstance(int accountId, String block_id, String title) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putString(Extra.ID, block_id);
        args.putString(Extra.TITLE, title);
        PlaylistsInCatalogFragment fragment = new PlaylistsInCatalogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
        Header = requireArguments().getString(Extra.TITLE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalog_block, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);

        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(PlaylistsInCatalogPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        int columnCount = getResources().getInteger(R.integer.photos_albums_column_count);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), columnCount));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(PlaylistsInCatalogPresenter::fireScrollToEnd);
            }
        });
        mAdapter = new AudioPlaylistsAdapter(Collections.emptyList(), requireActivity(), false);
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
            ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
            if (actionBar != null) {
                actionBar.setTitle(Header);
                actionBar.setSubtitle(R.string.playlists);
            }

            if (requireActivity() instanceof OnSectionResumeCallback) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_AUDIOS);
            }

            new ActivityFeatures.Builder()
                    .begin()
                    .setHideNavigationMenu(false)
                    .setBarsColored(requireActivity(), true)
                    .build()
                    .apply(requireActivity());
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<PlaylistsInCatalogPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new PlaylistsInCatalogPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getString(Extra.ID),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<AudioPlaylist> audios) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(audios);
        }
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayRefreshing(boolean refresing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refresing);
        }
    }

    @Override
    public void onAlbumClick(int index, AudioPlaylist album) {
        if (Utils.isEmpty(album.getOriginal_access_key()) || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0)
            PlaceFactory.getAudiosInAlbumPlace(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
        else
            PlaceFactory.getAudiosInAlbumPlace(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), album.getOriginal_owner_id(), album.getOriginal_id(), album.getOriginal_access_key()).tryOpenWith(requireActivity());
    }

    @Override
    public void onOpenClick(int index, AudioPlaylist album) {
        if (Utils.isEmpty(album.getOriginal_access_key()) || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0)
            PlaceFactory.getAudiosInAlbumPlace(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
        else
            PlaceFactory.getAudiosInAlbumPlace(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), album.getOriginal_owner_id(), album.getOriginal_id(), album.getOriginal_access_key()).tryOpenWith(requireActivity());
    }

    @Override
    public void onDelete(int index, AudioPlaylist album) {

    }

    @Override
    public void onShare(int index, AudioPlaylist album) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), album);
    }

    @Override
    public void onEdit(int index, AudioPlaylist album) {

    }

    @Override
    public void onAddAudios(int index, AudioPlaylist album) {

    }

    @Override
    public void onAdd(int index, AudioPlaylist album, boolean clone) {
        callPresenter(p -> p.onAdd(album, clone));
    }
}
