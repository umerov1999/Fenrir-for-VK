package dev.ragnarok.fenrir.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VkApiArtist;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.criteria.ArtistSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.view.search.IArtistSearchView;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class ArtistSearchPresenter extends AbsSearchPresenter<IArtistSearchView, ArtistSearchCriteria, VkApiArtist, IntNextFrom> {

    private final IAudioInteractor audioInteractor;

    public ArtistSearchPresenter(int accountId, @Nullable ArtistSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
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
    Single<Pair<List<VkApiArtist>, IntNextFrom>> doSearch(int accountId, ArtistSearchCriteria criteria, IntNextFrom startFrom) {
        IntNextFrom nextFrom = new IntNextFrom(startFrom.getOffset() + 50);
        return audioInteractor.searchArtists(accountId, criteria, startFrom.getOffset(), 50)
                .map(audio -> Pair.Companion.create(audio, nextFrom));
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        appendDisposable(audioInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> callView(v -> v.getCustomToast().showToast(R.string.success)), throwable ->
                        callView(v -> showError(v, throwable))));
    }

    @Override
    boolean canSearch(ArtistSearchCriteria criteria) {
        return true;
    }

    @Override
    ArtistSearchCriteria instantiateEmptyCriteria() {
        return new ArtistSearchCriteria("");
    }
}
