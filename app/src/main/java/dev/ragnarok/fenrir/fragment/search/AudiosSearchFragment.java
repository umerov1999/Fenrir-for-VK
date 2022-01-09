package dev.ragnarok.fenrir.fragment.search;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.AudioRecyclerAdapter;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.search.AudiosSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IAudioSearchView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;


public class AudiosSearchFragment extends AbsSearchFragment<AudiosSearchPresenter, IAudioSearchView, Audio, AudioRecyclerAdapter> implements IAudioSearchView {

    public static final String ACTION_SELECT = "AudiosSearchFragment.ACTION_SELECT";
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text));
    private boolean isSelectMode;

    public static AudiosSearchFragment newInstance(int accountId, AudioSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        AudiosSearchFragment fragment = new AudiosSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AudiosSearchFragment newInstanceSelect(int accountId, AudioSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        args.putBoolean(ACTION_SELECT, true);
        AudiosSearchFragment fragment = new AudiosSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemBindableRangeInserted(position, count);
        }
    }

    @Override
    void setAdapterData(AudioRecyclerAdapter adapter, List<Audio> data) {
        adapter.setData(data);
    }

    @Override
    public View createViewLayout(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return inflater.inflate(R.layout.fragment_search_audio, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSelectMode = requireArguments().getBoolean(ACTION_SELECT);
    }

    @Override
    void postCreate(View root) {
        FloatingActionButton Goto = root.findViewById(R.id.goto_button);
        RecyclerView recyclerView = root.findViewById(R.id.list);
        if (isSelectMode)
            Goto.setImageResource(R.drawable.check);
        else
            Goto.setImageResource(R.drawable.audio_player);
        if (!isSelectMode) {
            Goto.setOnLongClickListener(v -> {
                Audio curr = MusicPlaybackController.getCurrentAudio();
                if (curr != null) {
                    PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
                } else
                    CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
                return false;
            });
        }
        Goto.setOnClickListener(v -> {
            if (isSelectMode) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, callPresenter(AudiosSearchPresenter::getSelected, new ArrayList<>()));
                requireActivity().setResult(Activity.RESULT_OK, intent);
                requireActivity().finish();
            } else {
                Audio curr = MusicPlaybackController.getCurrentAudio();
                if (curr != null) {
                    int index = callPresenter(p -> p.getAudioPos(curr), -1);
                    if (index >= 0) {
                        recyclerView.scrollToPosition(index + mAdapter.getHeadersCount());
                    } else
                        CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.audio_not_found);
                } else
                    CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
            }
        });
    }

    @Override
    AudioRecyclerAdapter createAdapter(List<Audio> data) {
        AudioRecyclerAdapter adapter = new AudioRecyclerAdapter(requireActivity(), Collections.emptyList(), false, isSelectMode, 0, null);
        adapter.setClickListener(new AudioRecyclerAdapter.ClickListener() {
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
        return adapter;
    }

    @Override
    public void notifyAudioChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemBindableChanged(index);
        }
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<AudiosSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosSearchPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }
}