package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.media.music.MusicPlaybackController;
import dev.ragnarok.fenrir.media.music.MusicPlaybackService;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.FindAtWithContent;
import dev.ragnarok.fenrir.util.HelperSimple;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class AudiosPresenter extends AccountDependencyPresenter<IAudiosView> {

    private static final int GET_COUNT = 100;
    private static final int SEARCH_COUNT = 200;
    private static final int SEARCH_VIEW_COUNT = 20;
    private static final int WEB_SEARCH_DELAY = 1000;
    private final IAudioInteractor audioInteractor;
    private final ArrayList<Audio> audios;
    private final int ownerId;
    private final Integer albumId;
    private final boolean iSSelectMode;
    private final String accessKey;
    private final CompositeDisposable audioListDisposable = new CompositeDisposable();
    private final FindAudio searcher;
    private Disposable sleepDataDisposable = Disposable.disposed();
    private Disposable swapDisposable = Disposable.disposed();
    private boolean actualReceived;
    private List<AudioPlaylist> Curr;
    private boolean loadingNow;
    private boolean endOfContent;
    private boolean doAudioLoadTabs;
    private boolean needDeadHelper;

    public AudiosPresenter(int accountId, int ownerId, Integer albumId, String accessKey, boolean iSSelectMode, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        audioInteractor = InteractorFactory.createAudioInteractor();
        audios = new ArrayList<>();
        this.ownerId = ownerId;
        this.iSSelectMode = iSSelectMode;
        this.albumId = albumId;
        this.accessKey = accessKey;
        searcher = new FindAudio(getCompositeDisposable());
        needDeadHelper = HelperSimple.INSTANCE.hasHelp(HelperSimple.AUDIO_DEAD, 1);
    }

    private void loadedPlaylist(AudioPlaylist t) {
        List<AudioPlaylist> ret = new ArrayList<>(1);
        ret.add(t);
        Objects.requireNonNull(getView()).updatePlaylists(ret);
        Curr = ret;
    }

    public boolean isMyAudio() {
        return isNull(albumId) && ownerId == getAccountId();
    }

    public boolean isNotSearch() {
        return !searcher.isSearchMode();
    }

    public Integer getPlaylistId() {
        return albumId;
    }

    public void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
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
        if (audios.isEmpty()) {
            if (!iSSelectMode && isNull(albumId) && MusicPlaybackController.Audios.containsKey(ownerId)) {
                audios.addAll(Objects.requireNonNull(MusicPlaybackController.Audios.get(ownerId)));
                actualReceived = true;
                setLoadingNow(false);
                callView(IAudiosView::notifyListChanged);
            } else
                fireRefresh();
        }
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(loadingNow));
    }

    private void requestNext() {
        setLoadingNow(true);
        int offset = audios.size();
        requestList(offset, albumId);
    }

    public void requestList(int offset, Integer album_id) {
        setLoadingNow(true);
        audioListDisposable.add(audioInteractor.get(getAccountId(), album_id, ownerId, offset, GET_COUNT, accessKey)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onListReceived(offset, data), this::onListGetError));
    }

    private void onListReceived(int offset, List<Audio> data) {
        endOfContent = data.isEmpty();
        actualReceived = true;
        if (offset == 0) {
            if (isNull(albumId) && !iSSelectMode) {
                if (MusicPlaybackController.Audios.containsKey(ownerId)) {
                    Objects.requireNonNull(MusicPlaybackController.Audios.get(ownerId)).clear();
                } else {
                    MusicPlaybackController.Audios.put(ownerId, new ArrayList<>(data.size()));
                }
                Objects.requireNonNull(MusicPlaybackController.Audios.get(ownerId)).addAll(data);
            }
            audios.clear();
            audios.addAll(data);
            callView(IAudiosView::notifyListChanged);
        } else {
            if (isNull(albumId) && !iSSelectMode && MusicPlaybackController.Audios.containsKey(ownerId)) {
                Objects.requireNonNull(MusicPlaybackController.Audios.get(ownerId)).addAll(data);
            }
            int startOwnSize = audios.size();
            audios.addAll(data);
            callView(view -> view.notifyDataAdded(startOwnSize, data.size()));
        }
        setLoadingNow(false);
        if (needDeadHelper) {
            for (Audio i : audios) {
                if (Utils.isEmpty(i.getUrl()) || "https://vk.com/mp3/audio_api_unavailable.mp3".equals(i.getUrl())) {
                    needDeadHelper = false;
                    callView(IAudiosView::showAudioDeadHelper);
                    break;
                }
            }
        }
    }

    public void playAudio(Context context, int position) {
        MusicPlaybackService.startForPlayList(context, audios, position, false);
        if (!Settings.get().other().isShow_mini_player())
            PlaceFactory.getPlayerPlace(getAccountId()).tryOpenWith(context);
    }

    public void fireDelete(int position) {
        audios.remove(position);
        callView(v -> v.notifyItemRemoved(position));
    }

    @Override
    public void onDestroyed() {
        audioListDisposable.dispose();
        swapDisposable.dispose();
        sleepDataDisposable.dispose();
        super.onDestroyed();
    }

    private void onListGetError(Throwable t) {
        setLoadingNow(false);
        callResumedView(v -> showError(v, getCauseIfRuntime(t)));
    }

    public void fireSelectAll() {
        for (Audio i : audios) {
            i.setIsSelected(true);
        }
        callView(IAudiosView::notifyListChanged);
    }

    public ArrayList<Audio> getSelected(boolean noDownloaded) {
        ArrayList<Audio> ret = new ArrayList<>();
        for (Audio i : audios) {
            if (i.isSelected()) {
                if (noDownloaded) {
                    if (DownloadWorkUtils.TrackIsDownloaded(i) == 0 && !Utils.isEmpty(i.getUrl()) && !i.getUrl().contains("file://") && !i.getUrl().contains("content://")) {
                        ret.add(i);
                    }
                } else {
                    ret.add(i);
                }
            }
        }
        return ret;
    }

    public int getAudioPos(Audio audio) {
        if (!Utils.isEmpty(audios) && audio != null) {
            int pos = 0;
            for (Audio i : audios) {
                if (i.getId() == audio.getId() && i.getOwnerId() == audio.getOwnerId()) {
                    i.setAnimationNow(true);
                    int finalPos = pos;
                    callView(v -> v.notifyItemChanged(finalPos));
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }

    public void fireUpdateSelectMode() {
        for (Audio i : audios) {
            if (i.isSelected()) {
                i.setIsSelected(false);
            }
        }
        callView(IAudiosView::notifyListChanged);
    }

    private void sleep_search(String q) {
        if (loadingNow) return;

        sleepDataDisposable.dispose();
        if (Utils.isEmpty(q)) {
            searcher.cancel();
        } else {
            if (!searcher.isSearchMode()) {
                searcher.insertCache(audios, audios.size());
            }
            sleepDataDisposable = (Single.just(new Object())
                    .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(videos -> searcher.do_search(q), this::onListGetError));
        }
    }

    public void fireSearchRequestChanged(String q) {
        sleep_search(q == null ? null : q.trim());
    }

    public void fireRefresh() {
        if (searcher.isSearchMode()) {
            searcher.reset();
        } else {
            if (nonNull(albumId) && albumId != 0) {
                audioListDisposable.add(audioInteractor.getPlaylistById(getAccountId(), albumId, ownerId, accessKey)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(this::loadedPlaylist, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
            }
            requestList(0, albumId);
        }
    }

    public void onDelete(AudioPlaylist album) {
        int accountId = getAccountId();
        audioListDisposable.add(audioInteractor.deletePlaylist(accountId, album.getId(), album.getOwnerId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> callView(v -> v.getCustomToast().showToast(R.string.success)), throwable ->
                        callView(v -> showError(v, throwable))));
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        audioListDisposable.add(audioInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> callView(v -> v.getCustomToast().showToast(R.string.success)), throwable ->
                        callView(v -> showError(v, throwable))));
    }

    public void fireScrollToEnd() {
        if (nonEmpty(audios) && !loadingNow && actualReceived) {
            if (searcher.isSearchMode()) {
                searcher.do_search();
            } else if (!endOfContent) {
                requestNext();
            }
        }
    }

    public void fireEditTrackIn(Context context, Audio audio) {
        audioListDisposable.add(audioInteractor.getLyrics(Settings.get().accounts().getCurrent(), audio.getLyricsId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> fireEditTrack(context, audio, t), v -> fireEditTrack(context, audio, null)));
    }

    public void fireEditTrack(Context context, Audio audio, String lyrics) {
        View root = View.inflate(context, R.layout.entry_audio_info, null);
        ((TextInputEditText) root.findViewById(R.id.edit_artist)).setText(audio.getArtist());
        ((TextInputEditText) root.findViewById(R.id.edit_title)).setText(audio.getTitle());
        ((TextInputEditText) root.findViewById(R.id.edit_lyrics)).setText(lyrics);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.enter_audio_info)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> audioListDisposable.add(audioInteractor.edit(getAccountId(), audio.getOwnerId(), audio.getId(),
                        ((TextInputEditText) root.findViewById(R.id.edit_artist)).getText().toString(), ((TextInputEditText) root.findViewById(R.id.edit_title)).getText().toString(),
                        ((TextInputEditText) root.findViewById(R.id.edit_lyrics)).getText().toString()).compose(RxUtils.applyCompletableIOToMainSchedulers())
                        .subscribe(this::fireRefresh, t -> callView(v -> showError(v, getCauseIfRuntime(t))))))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void tempSwap(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(audios, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(audios, i, i - 1);
            }
        }
        callView(v -> v.notifyItemMoved(fromPosition, toPosition));
    }

    public boolean fireItemMoved(int fromPosition, int toPosition) {
        if (audios.size() < 2) {
            return false;
        }
        Audio audio_from = audios.get(fromPosition);

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(audios, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(audios, i, i - 1);
            }
        }
        callView(v -> v.notifyItemMoved(fromPosition, toPosition));

        Integer before = null;
        Integer after = null;
        if (toPosition == 0) {
            before = audios.get(1).getId();
        } else {
            after = audios.get(toPosition - 1).getId();
        }

        swapDisposable.dispose();
        swapDisposable = audioInteractor.reorder(getAccountId(), ownerId, audio_from.getId(), before, after)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(RxUtils.ignore(), e -> tempSwap(toPosition, fromPosition));
        return true;
    }

    @Override
    public void onGuiCreated(@NonNull IAudiosView view) {
        super.onGuiCreated(view);
        view.displayList(audios);
        if (Curr != null)
            Objects.requireNonNull(getView()).updatePlaylists(Curr);
    }

    private class FindAudio extends FindAtWithContent<Audio> {
        public FindAudio(CompositeDisposable disposable) {
            super(disposable, SEARCH_VIEW_COUNT, SEARCH_COUNT);
        }

        @Override
        protected Single<List<Audio>> search(int offset, int count) {
            return audioInteractor.get(getAccountId(), albumId, ownerId, offset, count, accessKey);
        }

        @Override
        protected void onError(@NonNull Throwable e) {
            onListGetError(e);
        }

        @Override
        protected void onResult(@NonNull List<Audio> data) {
            actualReceived = true;
            int startSize = audios.size();
            audios.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        @Override
        protected void updateLoading(boolean loading) {
            setLoadingNow(loading);
        }

        @Override
        protected void clean() {
            audios.clear();
            callView(IAudiosView::notifyListChanged);
        }

        private boolean checkArtists(@Nullable Map<String, String> data, @NonNull String q) {
            if (Utils.isEmpty(data)) {
                return false;
            }
            for (String i : data.values()) {
                if (i.toLowerCase().contains(q.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        private boolean checkTittleArtists(@NonNull Audio data, @NonNull String q) {
            String[] r = q.split("( - )|( )|( {2})", 2);
            if (r.length >= 2) {
                return (Utils.safeCheck(data.getArtist(), () -> data.getArtist().toLowerCase().contains(r[0].toLowerCase()))
                        || checkArtists(data.getMain_artists(), r[0])) && Utils.safeCheck(data.getTitle(), () -> data.getTitle().toLowerCase().contains(r[1].toLowerCase()));
            }
            return false;
        }

        @Override
        protected boolean compare(@NonNull Audio data, @NonNull String q) {
            if (q.equals("dw")) {
                return DownloadWorkUtils.TrackIsDownloaded(data) == 0;
            }
            return (Utils.safeCheck(data.getTitle(), () -> data.getTitle().toLowerCase().contains(q.toLowerCase()))
                    || Utils.safeCheck(data.getArtist(), () -> data.getArtist().toLowerCase().contains(q.toLowerCase()))
                    || checkArtists(data.getMain_artists(), q) || checkTittleArtists(data, q));
        }

        @Override
        protected void onReset(@NonNull List<Audio> data, int offset, boolean isEnd) {
            if (Utils.isEmpty(data)) {
                fireRefresh();
            } else {
                endOfContent = isEnd;
                audios.clear();
                audios.addAll(data);
                if (isNull(albumId) && !iSSelectMode) {
                    if (MusicPlaybackController.Audios.containsKey(ownerId)) {
                        Objects.requireNonNull(MusicPlaybackController.Audios.get(ownerId)).clear();
                    } else {
                        MusicPlaybackController.Audios.put(ownerId, new ArrayList<>(data.size()));
                    }
                    Objects.requireNonNull(MusicPlaybackController.Audios.get(ownerId)).addAll(data);
                }
                callView(IAudiosView::notifyListChanged);
            }
        }
    }
}