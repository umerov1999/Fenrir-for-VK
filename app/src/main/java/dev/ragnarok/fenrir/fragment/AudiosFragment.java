package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.AudioRecyclerAdapter;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalPlaylistAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AudiosPresenter;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class AudiosFragment extends BaseMvpFragment<AudiosPresenter, IAudiosView>
        implements IAudiosView, HorizontalPlaylistAdapter.Listener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    public static final String ACTION_SELECT = "AudiosFragment.ACTION_SELECT";
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text));
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioRecyclerAdapter mAudioRecyclerAdapter;
    private boolean inTabsContainer;
    private final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return callPresenter(p -> p.fireItemMoved(mAudioRecyclerAdapter.getItemRawPosition(viewHolder.getBindingAdapterPosition()), mAudioRecyclerAdapter.getItemRawPosition(target.getBindingAdapterPosition())), false);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            mAudioRecyclerAdapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
            callPresenter(p -> p.playAudio(requireActivity(), mAudioRecyclerAdapter.getItemRawPosition(viewHolder.getBindingAdapterPosition())));
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return !inTabsContainer;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return !Settings.get().main().isUse_long_click_download() && callPresenter(p -> p.isMyAudio() && p.isNotSearch(), false);
        }
    };
    private boolean isSelectMode;
    private boolean isSaveMode;
    private View headerPlaylist;
    private HorizontalPlaylistAdapter mPlaylistAdapter;

    public static Bundle buildArgs(int accountId, int ownerId, Integer albumId, String access_key) {
        Bundle args = new Bundle();
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        if (nonNull(albumId)) {
            args.putInt(Extra.ALBUM_ID, albumId);
        }
        if (!Utils.isEmpty(access_key)) {
            args.putString(Extra.ACCESS_KEY, access_key);
        }
        return args;
    }

    public static AudiosFragment newInstance(@NonNull Bundle args) {
        AudiosFragment fragment = new AudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AudiosFragment newInstance(@NonNull Bundle args, boolean isSelect) {
        args.putBoolean(ACTION_SELECT, isSelect);
        AudiosFragment fragment = new AudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
        isSelectMode = requireArguments().getBoolean(ACTION_SELECT);
        isSaveMode = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music_main, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        MySearchView searchView = root.findViewById(R.id.searchview);
        searchView.setRightButtonVisibility(false);
        searchView.setLeftIcon(R.drawable.magnify);
        searchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                postPresenterReceive(p -> p.fireSearchRequestChanged(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                postPresenterReceive(p -> p.fireSearchRequestChanged(newText));
                return false;
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(AudiosPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(AudiosPresenter::fireScrollToEnd);
            }
        });

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);

        FloatingActionButton save_mode = root.findViewById(R.id.save_mode_button);
        FloatingActionButton Goto = root.findViewById(R.id.goto_button);
        save_mode.setVisibility(isSelectMode ? View.GONE : (Settings.get().other().isAudio_save_mode_button() ? View.VISIBLE : View.GONE));
        save_mode.setOnClickListener(v -> {
            isSaveMode = !isSaveMode;
            Goto.setImageResource(isSaveMode ? R.drawable.check : R.drawable.audio_player);
            save_mode.setImageResource(isSaveMode ? R.drawable.ic_dismiss : R.drawable.save);
            mAudioRecyclerAdapter.toggleSelectMode(isSaveMode);
            callPresenter(AudiosPresenter::fireUpdateSelectMode);
        });

        if (isSelectMode) {
            Goto.setImageResource(R.drawable.check);
            save_mode.setImageResource(R.drawable.ic_dismiss);
        } else {
            Goto.setImageResource(R.drawable.audio_player);
            save_mode.setImageResource(R.drawable.save);
        }

        Goto.setOnLongClickListener(v -> {
            if (!isSelectMode && !isSaveMode) {
                Audio curr = MusicPlaybackController.getCurrentAudio();
                if (curr != null) {
                    PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
                } else {
                    CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
                }
            } else {
                callPresenter(AudiosPresenter::fireSelectAll);
            }
            return true;
        });
        Goto.setOnClickListener(v -> {
            if (isSelectMode) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, callPresenter(p -> p.getSelected(false), new ArrayList<>()));
                requireActivity().setResult(Activity.RESULT_OK, intent);
                requireActivity().finish();
            } else {
                if (isSaveMode) {
                    List<Audio> tracks = callPresenter(p -> p.getSelected(true), new ArrayList<>());
                    isSaveMode = false;
                    Goto.setImageResource(R.drawable.audio_player);
                    save_mode.setImageResource(R.drawable.save);
                    mAudioRecyclerAdapter.toggleSelectMode(isSaveMode);
                    callPresenter(AudiosPresenter::fireUpdateSelectMode);

                    if (!Utils.isEmpty(tracks)) {
                        DownloadWorkUtils.CheckDirectory(Settings.get().other().getMusicDir());
                        int account_id = callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent());
                        WorkContinuation object = WorkManager.getInstance(requireActivity()).beginWith(DownloadWorkUtils.makeDownloadRequestAudio(tracks.get(0), account_id));
                        if (tracks.size() > 1) {
                            List<OneTimeWorkRequest> Requests = new ArrayList<>(tracks.size() - 1);
                            boolean is_first = true;
                            for (Audio i : tracks) {
                                if (is_first) {
                                    is_first = false;
                                    continue;
                                }
                                Requests.add(DownloadWorkUtils.makeDownloadRequestAudio(i, account_id));
                            }
                            object = object.then(Requests);
                        }
                        object.enqueue();
                    }
                } else {
                    Audio curr = MusicPlaybackController.getCurrentAudio();
                    if (curr != null) {
                        int index = callPresenter(p -> p.getAudioPos(curr), -1);
                        if (index >= 0) {
                            recyclerView.scrollToPosition(index + mAudioRecyclerAdapter.getHeadersCount());
                        } else
                            CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.audio_not_found);
                    } else
                        CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
                }
            }
        });

        mAudioRecyclerAdapter = new AudioRecyclerAdapter(requireActivity(), Collections.emptyList(), callPresenter(AudiosPresenter::isMyAudio, false), isSelectMode, 0, callPresenter(AudiosPresenter::getPlaylistId, null));

        headerPlaylist = inflater.inflate(R.layout.header_audio_playlist, recyclerView, false);
        RecyclerView headerPlaylistRecyclerView = headerPlaylist.findViewById(R.id.header_audio_playlist);
        headerPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        mPlaylistAdapter = new HorizontalPlaylistAdapter(Collections.emptyList());
        mPlaylistAdapter.setListener(this);
        headerPlaylistRecyclerView.setAdapter(mPlaylistAdapter);

        mAudioRecyclerAdapter.setClickListener(new AudioRecyclerAdapter.ClickListener() {
            @Override
            public void onClick(int position, int catalog, Audio audio) {
                callPresenter(p -> p.playAudio(requireActivity(), position));
            }

            @Override
            public void onEdit(int position, Audio audio) {
                callPresenter(p -> p.fireEditTrackIn(requireActivity(), audio));
            }

            @Override
            public void onDelete(int position) {
                callPresenter(p -> p.fireDelete(position));
            }

            @Override
            public void onUrlPhotoOpen(@NonNull String url, @NonNull String prefix, @NonNull String photo_prefix) {
                PlaceFactory.getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(requireActivity());
            }

            @Override
            public void onRequestWritePermissions() {
                requestWritePermission.launch();
            }
        });

        recyclerView.setAdapter(mAudioRecyclerAdapter);
        return root;
    }

    @Override
    public void updatePlaylists(List<AudioPlaylist> playlists) {
        if (nonNull(mPlaylistAdapter)) {
            mPlaylistAdapter.setItems(playlists);
            mPlaylistAdapter.notifyDataSetChanged();
            mAudioRecyclerAdapter.addHeader(headerPlaylist);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
            ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
            if (actionBar != null) {
                actionBar.setTitle(R.string.music);
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

    @NonNull
    @Override
    public IPresenterFactory<AudiosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        String accessKey = requireArguments().containsKey(Extra.ACCESS_KEY)
                ? requireArguments().getString(Extra.ACCESS_KEY) : null;
        Integer albumId = requireArguments().containsKey(Extra.ALBUM_ID)
                ? requireArguments().getInt(Extra.ALBUM_ID) : null;
        return () -> new AudiosPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.OWNER_ID),
                albumId,
                accessKey,
                requireArguments().getBoolean(ACTION_SELECT),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<Audio> audios) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.setData(audios);
        }
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyItemMoved(int fromPosition, int toPosition) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemBindableMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void notifyItemRemoved(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemBindableRemoved(index);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemBindableChanged(index);
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemBindableRangeInserted(position, count);
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void onPlayListClick(AudioPlaylist item, int pos) {
        if (item.getOwnerId() == Settings.get().accounts().getCurrent())
            callPresenter(p -> p.onDelete(item));
        else
            callPresenter(p -> p.onAdd(item));
    }
}
