package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioPlaylistsView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.FindAtWithContent;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class AudioPlaylistsPresenter extends AccountDependencyPresenter<IAudioPlaylistsView> {

    private static final int SEARCH_COUNT = 50;
    private static final int SEARCH_VIEW_COUNT = 10;
    private static final int GET_COUNT = 50;
    private static final int WEB_SEARCH_DELAY = 1000;
    private final List<AudioPlaylist> addon;
    private final List<AudioPlaylist> playlists;
    private final IAudioInteractor fInteractor;
    private final int owner_id;
    private final FindPlaylist searcher;
    private Disposable actualDataDisposable = Disposable.disposed();
    private AudioPlaylist pending_to_add;
    private int Foffset;
    private boolean endOfContent;
    private boolean actualDataLoading;
    private boolean doAudioLoadTabs;
    private boolean actualDataReceived;

    public AudioPlaylistsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        owner_id = ownerId;
        playlists = new ArrayList<>();
        addon = new ArrayList<>();
        fInteractor = InteractorFactory.createAudioInteractor();
        searcher = new FindPlaylist(getCompositeDisposable());
    }

    public int getOwner_id() {
        return owner_id;
    }

    @Override
    public void onGuiCreated(@NonNull IAudioPlaylistsView view) {
        super.onGuiCreated(view);
        view.displayData(playlists);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        appendDisposable(fInteractor.getPlaylists(accountId, owner_id, offset, GET_COUNT)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<AudioPlaylist> data) {
        actualDataReceived = true;
        Foffset = offset + GET_COUNT;
        actualDataLoading = false;
        endOfContent = data.isEmpty();

        if (offset == 0) {
            playlists.clear();
            playlists.addAll(addon);
            playlists.addAll(data);
            callView(IAudioPlaylistsView::notifyDataSetChanged);
        } else {
            int startSize = playlists.size();
            playlists.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        if (doAudioLoadTabs) {
            return;
        } else {
            doAudioLoadTabs = true;
        }
        callResumedView(IAudioPlaylistsView::showHelper);
        if (getAccountId() == owner_id) {
            List<Integer> ids = Settings.get().other().getServicePlaylist();
            if (!Utils.isEmpty(ids)) {
                if (ids.size() == 1) {
                    appendDisposable(fInteractor.getPlaylistById(getAccountId(), ids.get(0), owner_id, null)
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(pl -> {
                                addon.clear();
                                addon.add(pl);
                                loadActualData(0);
                            }, i -> loadActualData(0)));
                } else {
                    StringBuilder code = new StringBuilder();
                    StringBuilder code_addon = new StringBuilder("return [");
                    boolean code_first = true;
                    int tick = 0;
                    for (Integer i : ids) {
                        code.append("var playlist_").append(tick).append(" = API.audio.getPlaylistById({\"v\":\"" + Constants.API_VERSION + "\", \"owner_id\":").append(owner_id).append(", \"playlist_id\": ").append(i).append("});");
                        if (code_first) {
                            code_first = false;
                        } else {
                            code_addon.append(", ");
                        }
                        code_addon.append("playlist_").append(tick);
                        tick++;
                    }
                    code_addon.append("];");
                    code.append(code_addon);
                    appendDisposable(fInteractor.getPlaylistsCustom(getAccountId(), code.toString())
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(pl -> {
                                addon.clear();
                                addon.addAll(pl);
                                loadActualData(0);
                            }, i -> loadActualData(0)));
                }
            } else {
                loadActualData(0);
            }
        } else {
            loadActualData(0);
        }
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.showRefreshing(actualDataLoading));
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (nonEmpty(playlists) && actualDataReceived && !actualDataLoading) {
            if (searcher.isSearchMode()) {
                searcher.do_search();
            } else if (!endOfContent) {
                loadActualData(Foffset);
            }
            return false;
        }
        return true;
    }

    private void sleep_search(String q) {
        if (actualDataLoading) return;

        actualDataDisposable.dispose();
        if (Utils.isEmpty(q)) {
            searcher.cancel();
        } else {
            actualDataDisposable = (Single.just(new Object())
                    .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(videos -> searcher.do_search(q), this::onActualDataGetError));
        }
    }

    public void fireSearchRequestChanged(String q) {
        sleep_search(q == null ? null : q.trim());
    }

    public void onDelete(int index, AudioPlaylist album) {
        int accountId = getAccountId();
        appendDisposable(fInteractor.deletePlaylist(accountId, album.getId(), album.getOwnerId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> {
                    playlists.remove(index);
                    callView(v -> v.notifyItemRemoved(index));
                    callView(v -> v.getCustomToast().showToast(R.string.success));
                }, throwable -> callView(v -> showError(v, throwable))));
    }

    public void onEdit(Context context, AudioPlaylist album) {
        View root = View.inflate(context, R.layout.entry_playlist_info, null);
        ((TextInputEditText) root.findViewById(R.id.edit_title)).setText(album.getTitle());
        ((TextInputEditText) root.findViewById(R.id.edit_description)).setText(album.getDescription());
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.edit)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> appendDisposable(fInteractor.editPlaylist(getAccountId(), album.getOwnerId(), album.getId(),
                        ((TextInputEditText) root.findViewById(R.id.edit_title)).getText().toString(),
                        ((TextInputEditText) root.findViewById(R.id.edit_description)).getText().toString()).compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(v -> fireRefresh(), t -> callView(v -> showError(v, getCauseIfRuntime(t))))))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void doInsertPlaylist(AudioPlaylist playlist) {
        int offset = addon.size();
        playlists.add(offset, playlist);
        callView(v -> v.notifyDataAdded(offset, 1));
    }

    public void fireCreatePlaylist(Context context) {
        View root = View.inflate(context, R.layout.entry_playlist_info, null);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.create_playlist)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> appendDisposable(fInteractor.createPlaylist(getAccountId(), owner_id,
                        ((TextInputEditText) root.findViewById(R.id.edit_title)).getText().toString(),
                        ((TextInputEditText) root.findViewById(R.id.edit_description)).getText().toString()).compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(this::doInsertPlaylist, t -> callView(v -> showError(v, getCauseIfRuntime(t))))))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    public void onAdd(AudioPlaylist album, boolean clone) {
        int accountId = getAccountId();
        appendDisposable((clone ? fInteractor.clonePlaylist(accountId, album.getId(), album.getOwnerId()) : fInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key()))
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> {
                    callView(v -> v.getCustomToast().showToast(R.string.success));
                    if (clone && (Utils.isValueAssigned(album.getId(), Settings.get().other().getServicePlaylist()))) {
                        fireRefresh();
                    }
                }, throwable ->
                        callView(v -> showError(v, throwable))));
    }

    public void fireAudiosSelected(List<Audio> audios) {
        List<AccessIdPair> targets = new ArrayList<>(audios.size());
        for (Audio i : audios) {
            targets.add(new AccessIdPair(i.getId(), i.getOwnerId(), i.getAccessKey()));
        }
        int accountId = getAccountId();
        appendDisposable(fInteractor.addToPlaylist(accountId, pending_to_add.getOwnerId(), pending_to_add.getId(), targets)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> callView(v -> v.getCustomToast().showToast(R.string.success)), throwable ->
                        callView(v -> showError(v, throwable))));
        pending_to_add = null;
    }

    public void onPlaceToPending(AudioPlaylist album) {
        pending_to_add = album;
        callView(v -> v.doAddAudios(getAccountId()));
    }

    public void fireRefresh() {
        if (actualDataLoading) {
            return;
        }

        if (searcher.isSearchMode()) {
            searcher.reset();
        } else {
            loadActualData(0);
        }
    }

    private class FindPlaylist extends FindAtWithContent<AudioPlaylist> {
        public FindPlaylist(CompositeDisposable disposable) {
            super(disposable, SEARCH_VIEW_COUNT, SEARCH_COUNT);
        }

        @Override
        protected Single<List<AudioPlaylist>> search(int offset, int count) {
            return fInteractor.getPlaylists(getAccountId(), owner_id, offset, count);
        }

        @Override
        protected void onError(@NonNull Throwable e) {
            onActualDataGetError(e);
        }

        @Override
        protected void onResult(@NonNull List<AudioPlaylist> data) {
            actualDataReceived = true;
            int startSize = playlists.size();
            playlists.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        @Override
        protected void updateLoading(boolean loading) {
            actualDataLoading = loading;
            resolveRefreshingView();
        }

        @Override
        protected void clean() {
            playlists.clear();
            callView(IAudioPlaylistsView::notifyDataSetChanged);
        }

        @Override
        protected boolean compare(@NonNull AudioPlaylist data, @NonNull String q) {
            return (Utils.safeCheck(data.getTitle(), () -> data.getTitle().toLowerCase().contains(q.toLowerCase()))
                    || Utils.safeCheck(data.getArtist_name(), () -> data.getArtist_name().toLowerCase().contains(q.toLowerCase()))
                    || Utils.safeCheck(data.getDescription(), () -> data.getDescription().toLowerCase().contains(q.toLowerCase())));
        }

        @Override
        protected void onReset(@NonNull List<AudioPlaylist> data, int offset, boolean isEnd) {
            if (Utils.isEmpty(playlists)) {
                fireRefresh();
            } else {
                Foffset = offset;
                endOfContent = isEnd;
                playlists.clear();
                playlists.addAll(addon);
                playlists.addAll(data);
                callView(IAudioPlaylistsView::notifyDataSetChanged);
            }
        }
    }
}
