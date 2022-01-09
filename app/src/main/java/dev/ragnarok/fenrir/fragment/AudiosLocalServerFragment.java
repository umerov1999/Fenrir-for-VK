package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.AudioLocalServerRecyclerAdapter;
import dev.ragnarok.fenrir.dialog.DialogLocalServerOptionDialog;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AudiosLocalServerPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosLocalServerView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class AudiosLocalServerFragment extends BaseMvpFragment<AudiosLocalServerPresenter, IAudiosLocalServerView>
        implements MySearchView.OnQueryTextListener, AudioLocalServerRecyclerAdapter.ClickListener, IAudiosLocalServerView {
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text));
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioLocalServerRecyclerAdapter mAudioRecyclerAdapter;
    private MySearchView searchView;

    public static AudiosLocalServerFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        AudiosLocalServerFragment fragment = new AudiosLocalServerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void clearSearch() {
        if (nonNull(searchView)) {
            searchView.setOnQueryTextListener(null);
            searchView.clear();
            searchView.setOnQueryTextListener(this);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_local_server_music, container, false);

        searchView = root.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(this);
        searchView.setRightButtonVisibility(true);
        searchView.setLeftIcon(R.drawable.magnify);
        searchView.setRightIcon(R.drawable.dots_vertical);
        searchView.setQuery("", true);
        searchView.setOnAdditionalButtonLongClickListener(() -> Utils.checkMusicInPC(requireActivity()));
        searchView.setOnAdditionalButtonClickListener(() -> callPresenter(AudiosLocalServerPresenter::fireOptions));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(p -> p.fireRefresh(false)));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(AudiosLocalServerPresenter::fireScrollToEnd);
            }
        });

        FloatingActionButton Goto = root.findViewById(R.id.goto_button);
        Goto.setImageResource(R.drawable.audio_player);

        Goto.setOnLongClickListener(v -> {
            Audio curr = MusicPlaybackController.getCurrentAudio();
            if (curr != null) {
                PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
            } else
                CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
            return false;
        });
        Goto.setOnClickListener(v -> {
            Audio curr = MusicPlaybackController.getCurrentAudio();
            if (curr != null) {
                int index = callPresenter(p -> p.getAudioPos(curr), -1);
                if (index >= 0) {
                    recyclerView.scrollToPosition(index);
                } else
                    CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.audio_not_found);
            } else
                CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
        });
        mAudioRecyclerAdapter = new AudioLocalServerRecyclerAdapter(requireActivity(), Collections.emptyList());
        mAudioRecyclerAdapter.setClickListener(this);
        recyclerView.setAdapter(mAudioRecyclerAdapter);
        return root;
    }

    @NonNull
    @Override
    public IPresenterFactory<AudiosLocalServerPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosLocalServerPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<Audio> audios) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.setItems(audios);
        }
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    @Override
    public void displayOptionsDialog(boolean isReverse, boolean isDiscography) {
        DialogLocalServerOptionDialog.newInstance(isDiscography, isReverse, new DialogLocalServerOptionDialog.DialogLocalServerOptionListener() {
            @Override
            public void onReverse(boolean reverse) {
                callPresenter(p -> p.updateReverse(reverse));
            }

            @Override
            public void onDiscography(boolean discography) {
                clearSearch();
                callPresenter(p -> p.updateDiscography(discography));
            }

            @Override
            public void onSync() {
                DownloadWorkUtils.doSyncRemoteAudio(requireActivity());
            }
        }).show(getChildFragmentManager(), "dialog-local-server-options");
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemRangeInserted(position, count);
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
    public void onClick(int position, Audio audio) {
        callPresenter(p -> p.playAudio(requireActivity(), position));
    }

    @Override
    public void onUrlPhotoOpen(@NonNull String url, @NonNull String prefix, @NonNull String photo_prefix) {
        PlaceFactory.getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(requireActivity());
    }

    @Override
    public void onRequestWritePermissions() {
        requestWritePermission.launch();
    }
}
