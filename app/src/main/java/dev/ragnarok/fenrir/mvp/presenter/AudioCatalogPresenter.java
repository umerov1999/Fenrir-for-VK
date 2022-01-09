package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioCatalog;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioCatalogView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;


public class AudioCatalogPresenter extends AccountDependencyPresenter<IAudioCatalogView> {

    private final List<AudioCatalog> pages;

    private final IAudioInteractor fInteractor;
    private final String artist_id;
    private Disposable actualDataDisposable = Disposable.disposed();
    private String query;
    private boolean doAudioLoadTabs;
    private boolean actualDataLoading;

    public AudioCatalogPresenter(int accountId, String artist_id, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        pages = new ArrayList<>();
        this.artist_id = artist_id;
        fInteractor = InteractorFactory.createAudioInteractor();
    }

    private boolean do_compare(String q) {
        if (Utils.isEmpty(q) && Utils.isEmpty(query)) {
            return true;
        } else return !Utils.isEmpty(query) && !Utils.isEmpty(q) && query.equalsIgnoreCase(q);
    }

    public void fireSearchRequestChanged(String q) {
        if (do_compare(q)) {
            return;
        }
        query = q == null ? null : q.trim();

        actualDataDisposable.dispose();
        if (Utils.isEmpty(query)) {
            fireRefresh();
            return;
        }
        actualDataDisposable = (Single.just(new Object())
                .delay(1, TimeUnit.SECONDS)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> fireRefresh(), this::onActualDataGetError));
    }

    @Override
    public void onGuiCreated(@NonNull IAudioCatalogView view) {
        super.onGuiCreated(view);
        view.displayData(pages);
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
        loadActualData();
    }

    private void loadActualData() {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        appendDisposable(fInteractor.getCatalog(accountId, artist_id, query)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onActualDataReceived, this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void onActualDataReceived(List<AudioCatalog> data) {

        actualDataLoading = false;

        pages.clear();
        pages.addAll(data);
        callView(IAudioCatalogView::notifyDataSetChanged);

        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.showRefreshing(actualDataLoading));
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        appendDisposable(fInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> callView(v -> v.getCustomToast().showToast(R.string.success)), throwable -> callView(v -> showError(v, throwable))));
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRepost(Context context) {
        if (Utils.isEmpty(artist_id)) {
            return;
        }
        SendAttachmentsActivity.startForSendAttachments(context, getAccountId(), new AudioArtist(artist_id));
    }

    public void fireRefresh() {
        if (actualDataLoading) {
            return;
        }
        loadActualData();
    }
}
