package dev.ragnarok.fenrir.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioPlaylistSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.view.search.IAudioPlaylistSearchView;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class AudioPlaylistSearchPresenter extends AbsSearchPresenter<IAudioPlaylistSearchView, AudioPlaylistSearchCriteria, AudioPlaylist, IntNextFrom> {

    private final IAudioInteractor audioInteractor;

    public AudioPlaylistSearchPresenter(int accountId, @Nullable AudioPlaylistSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        audioInteractor = InteractorFactory.createAudioInteractor();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    void onSearchError(Throwable throwable) {
        super.onSearchError(throwable);
        callResumedView(v -> showError(v, Utils.getCauseIfRuntime(throwable)));
    }

    @Override
    Single<Pair<List<AudioPlaylist>, IntNextFrom>> doSearch(int accountId, AudioPlaylistSearchCriteria criteria, IntNextFrom startFrom) {
        IntNextFrom nextFrom = new IntNextFrom(startFrom.getOffset() + 50);
        return audioInteractor.searchPlaylists(accountId, criteria, startFrom.getOffset(), 50)
                .map(audio -> Pair.Companion.create(audio, nextFrom));
    }

    public void onAdd(AudioPlaylist album, boolean clone) {
        int accountId = getAccountId();
        appendDisposable((clone ? audioInteractor.clonePlaylist(accountId, album.getId(), album.getOwnerId()) : audioInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key()))
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> callView(v -> v.getCustomToast().showToast(R.string.success)), throwable ->
                        callView(v -> showError(v, throwable))));
    }

    @Override
    boolean canSearch(AudioPlaylistSearchCriteria criteria) {
        return true;
    }

    @Override
    AudioPlaylistSearchCriteria instantiateEmptyCriteria() {
        return new AudioPlaylistSearchCriteria("");
    }
}
