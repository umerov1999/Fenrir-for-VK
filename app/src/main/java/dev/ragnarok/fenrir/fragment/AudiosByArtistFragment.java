package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.Manifest;
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
import androidx.fragment.app.Fragment;
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
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AudiosByArtistPresenter;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosByArtistView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class AudiosByArtistFragment extends BaseMvpFragment<AudiosByArtistPresenter, IAudiosByArtistView>
        implements IAudiosByArtistView {

    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text));
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioRecyclerAdapter mAudioRecyclerAdapter;
    private final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            mAudioRecyclerAdapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
            callPresenter(p -> p.playAudio(requireActivity(), mAudioRecyclerAdapter.getItemRawPosition(viewHolder.getBindingAdapterPosition())));
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    };
    private boolean isSaveMode;

    public static Fragment newInstance(Bundle args) {
        AudiosByArtistFragment fragment = new AudiosByArtistFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSaveMode = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);

        toolbar.setVisibility(View.VISIBLE);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(AudiosByArtistPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(AudiosByArtistPresenter::fireScrollToEnd);
            }
        });

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);

        FloatingActionButton save_mode = root.findViewById(R.id.save_mode_button);
        FloatingActionButton Goto = root.findViewById(R.id.goto_button);
        save_mode.setVisibility(Settings.get().other().isAudio_save_mode_button() ? View.VISIBLE : View.GONE);
        save_mode.setOnClickListener(v -> {
            isSaveMode = !isSaveMode;
            Goto.setImageResource(isSaveMode ? R.drawable.check : R.drawable.audio_player);
            save_mode.setImageResource(isSaveMode ? R.drawable.ic_dismiss : R.drawable.save);
            mAudioRecyclerAdapter.toggleSelectMode(isSaveMode);
            callPresenter(AudiosByArtistPresenter::fireUpdateSelectMode);
        });
        Goto.setImageResource(R.drawable.audio_player);
        save_mode.setImageResource(R.drawable.save);

        Goto.setOnLongClickListener(v -> {
            if (!isSaveMode) {
                Audio curr = MusicPlaybackController.getCurrentAudio();
                if (curr != null) {
                    PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
                } else {
                    CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
                }
            } else {
                callPresenter(AudiosByArtistPresenter::fireSelectAll);
            }
            return true;
        });
        Goto.setOnClickListener(v -> {
            if (isSaveMode) {
                List<Audio> tracks = callPresenter(p -> p.getSelected(true), new ArrayList<>());
                isSaveMode = false;
                Goto.setImageResource(R.drawable.audio_player);
                save_mode.setImageResource(R.drawable.save);
                mAudioRecyclerAdapter.toggleSelectMode(isSaveMode);
                callPresenter(AudiosByArtistPresenter::fireUpdateSelectMode);

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
        });
        mAudioRecyclerAdapter = new AudioRecyclerAdapter(requireActivity(), Collections.emptyList(), callPresenter(AudiosByArtistPresenter::isMyAudio, false), false, 0, null);

        mAudioRecyclerAdapter.setClickListener(new AudioRecyclerAdapter.ClickListener() {
            @Override
            public void onClick(int position, int catalog, Audio audio) {
                callPresenter(p -> p.playAudio(requireActivity(), position));
            }

            @Override
            public void onEdit(int position, Audio audio) {

            }

            @Override
            public void onDelete(int position) {

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
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.artists);
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

    @NonNull
    @Override
    public IPresenterFactory<AudiosByArtistPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosByArtistPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getString(Extra.ARTIST),
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
}
