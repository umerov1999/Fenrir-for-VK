package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.AudioCatalogAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.AudioCatalog;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AudioCatalogPresenter;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioCatalogView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class AudioCatalogFragment extends BaseMvpFragment<AudioCatalogPresenter, IAudioCatalogView> implements IAudioCatalogView, AudioCatalogAdapter.ClickListener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text));
    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioCatalogAdapter mAdapter;
    private boolean inTabsContainer;
    private boolean isArtist;

    public static AudioCatalogFragment newInstance(int accountId, String artist_id, boolean isHideToolbar) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putString(Extra.ARTIST, artist_id);
        args.putBoolean(EXTRA_IN_TABS_CONTAINER, isHideToolbar);

        AudioCatalogFragment fragment = new AudioCatalogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(Bundle args) {
        AudioCatalogFragment fragment = new AudioCatalogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle buildArgs(int accountId, String id, boolean isHideToolbar) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putString(Extra.ARTIST, id);
        args.putBoolean(EXTRA_IN_TABS_CONTAINER, isHideToolbar);
        return args;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
        setHasOptionsMenu(requireArguments().containsKey(Extra.ARTIST) && !Utils.isEmpty(requireArguments().getString(Extra.ARTIST)));
        isArtist = requireArguments().containsKey(Extra.ARTIST) && !Utils.isEmpty(requireArguments().getString(Extra.ARTIST));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_audio_catalog, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);

        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }
        mEmpty = root.findViewById(R.id.fragment_audio_catalog_empty_text);

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        if (!isArtist) {
            mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    callPresenter(p -> p.fireSearchRequestChanged(query));
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    callPresenter(p -> p.fireSearchRequestChanged(newText));
                    return false;
                }
            });
        } else {
            mySearchView.setVisibility(View.GONE);
        }

        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity());
        RecyclerView recyclerView = root.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(AudioCatalogPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new AudioCatalogAdapter(Collections.emptyList(), callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), requireActivity());
        mAdapter.setClickListener(this);

        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
            ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
            if (actionBar != null) {
                actionBar.setTitle(R.string.audio_catalog);
                actionBar.setSubtitle(null);
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

    @Override
    public void displayData(List<AudioCatalog> catalogs) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(catalogs);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            callPresenter(p -> p.fireRepost(requireActivity()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_share_main, menu);
    }

    @NonNull
    @Override
    public IPresenterFactory<AudioCatalogPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudioCatalogPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getString(Extra.ARTIST),
                saveInstanceState
        );
    }

    @Override
    public void onClick(int index, AudioCatalog value) {
        if (!Utils.isEmpty(value.getAudios())) {
            PlaceFactory.getAudiosInCatalogBlock(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), value.getId(), value.getTitle()).tryOpenWith(requireActivity());
        } else if (!Utils.isEmpty(value.getPlaylists())) {
            PlaceFactory.getPlaylistsInCatalogBlock(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), value.getId(), value.getTitle()).tryOpenWith(requireActivity());
        } else if (!Utils.isEmpty(value.getVideos())) {
            PlaceFactory.getVideosInCatalogBlock(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), value.getId(), value.getTitle()).tryOpenWith(requireActivity());
        } else if (!Utils.isEmpty(value.getLinks())) {
            PlaceFactory.getLinksInCatalogBlock(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), value.getId(), value.getTitle()).tryOpenWith(requireActivity());
        }
    }

    @Override
    public void onAddPlayList(int index, AudioPlaylist album) {
        callPresenter(p -> p.onAdd(album));
    }

    @Override
    public void onRequestWritePermissions() {
        requestWritePermission.launch();
    }
}
