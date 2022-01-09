package dev.ragnarok.fenrir.fragment.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.AudioPlaylistsAdapter;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioPlaylistSearchCriteria;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.presenter.search.AudioPlaylistSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IAudioPlaylistSearchView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class AudioPlaylistSearchFragment extends AbsSearchFragment<AudioPlaylistSearchPresenter, IAudioPlaylistSearchView, AudioPlaylist, AudioPlaylistsAdapter>
        implements AudioPlaylistsAdapter.ClickListener, IAudioPlaylistSearchView {

    public static final String ACTION_SELECT = "AudioPlaylistSearchFragment.ACTION_SELECT";
    private boolean isSelectMode;

    public static AudioPlaylistSearchFragment newInstance(int accountId, @Nullable AudioPlaylistSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        AudioPlaylistSearchFragment fragment = new AudioPlaylistSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AudioPlaylistSearchFragment newInstanceSelect(int accountId, @Nullable AudioPlaylistSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putBoolean(ACTION_SELECT, true);
        AudioPlaylistSearchFragment fragment = new AudioPlaylistSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSelectMode = requireArguments().getBoolean(ACTION_SELECT);
    }

    @Override
    void setAdapterData(AudioPlaylistsAdapter adapter, List<AudioPlaylist> data) {
        adapter.setData(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    AudioPlaylistsAdapter createAdapter(List<AudioPlaylist> data) {
        AudioPlaylistsAdapter ret = new AudioPlaylistsAdapter(data, requireActivity(), isSelectMode);
        ret.setClickListener(this);
        return ret;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columnCount = getResources().getInteger(R.integer.photos_albums_column_count);
        return new GridLayoutManager(requireActivity(), columnCount);
    }

    @NonNull
    @Override
    public IPresenterFactory<AudioPlaylistSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudioPlaylistSearchPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }

    @Override
    public void onAlbumClick(int index, AudioPlaylist album) {
        if (isSelectMode) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, new ArrayList<>(Collections.singleton(album)));
            requireActivity().setResult(Activity.RESULT_OK, intent);
            requireActivity().finish();
        } else {
            if (Utils.isEmpty(album.getOriginal_access_key()) || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0)
                PlaceFactory.getAudiosInAlbumPlace(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
            else
                PlaceFactory.getAudiosInAlbumPlace(callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), album.getOriginal_owner_id(), album.getOriginal_id(), album.getOriginal_access_key()).tryOpenWith(requireActivity());
        }
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
