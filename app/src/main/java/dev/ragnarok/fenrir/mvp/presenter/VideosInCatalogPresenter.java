package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.CatalogBlock;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IVideosInCatalogView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class VideosInCatalogPresenter extends AccountDependencyPresenter<IVideosInCatalogView> {

    private final IAudioInteractor audioInteractor;
    private final ArrayList<Video> videos;
    private final String block_id;
    private final CompositeDisposable audioListDisposable = new CompositeDisposable();
    private boolean actualReceived;
    private String next_from;
    private boolean loadingNow;
    private boolean endOfContent;
    private boolean doAudioLoadTabs;

    public VideosInCatalogPresenter(int accountId, String block_id, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        audioInteractor = InteractorFactory.createAudioInteractor();
        videos = new ArrayList<>();
        this.block_id = block_id;
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
        fireRefresh();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(loadingNow));
    }

    public void requestList() {
        setLoadingNow(true);
        audioListDisposable.add(audioInteractor.getCatalogBlockById(getAccountId(), block_id, next_from)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onListReceived, this::onListGetError));
    }

    private void onListReceived(CatalogBlock data) {
        if (data == null || Utils.isEmpty(data.getVideos())) {
            actualReceived = true;
            setLoadingNow(false);
            endOfContent = true;
            return;
        }
        if (Utils.isEmpty(next_from)) {
            videos.clear();
        }
        next_from = data.getNext_from();
        endOfContent = Utils.isEmpty(next_from);
        actualReceived = true;
        setLoadingNow(false);
        videos.addAll(data.getVideos());
        callView(IVideosInCatalogView::notifyListChanged);
    }

    @Override
    public void onDestroyed() {
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    private void onListGetError(Throwable t) {
        setLoadingNow(false);
        callResumedView(v -> showError(v, Utils.getCauseIfRuntime(t)));
    }

    public void fireRefresh() {
        audioListDisposable.clear();
        next_from = null;
        requestList();
    }

    public void fireScrollToEnd() {
        if (actualReceived && !endOfContent) {
            requestList();
        }
    }

    @Override
    public void onGuiCreated(@NonNull IVideosInCatalogView view) {
        super.onGuiCreated(view);
        view.displayList(videos);
    }

}
