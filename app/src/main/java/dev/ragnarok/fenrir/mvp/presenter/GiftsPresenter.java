package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Gift;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IGiftsView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class GiftsPresenter extends AccountDependencyPresenter<IGiftsView> {

    private static final int COUNT_PER_REQUEST = 25;
    private final IOwnersRepository ownersRepository;
    private final ArrayList<Gift> mGifts;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private final int owner_id;
    private boolean mEndOfContent;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;

    public GiftsPresenter(int accountId, int owner_id, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.owner_id = owner_id;

        ownersRepository = Repository.INSTANCE.getOwners();
        mGifts = new ArrayList<>();

        requestAtLast();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(netLoadingNow));
    }

    @Override
    public void onDestroyed() {
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void request(int offset) {
        netLoadingNow = true;
        resolveRefreshingView();

        int accountId = getAccountId();

        netDisposable.add(ownersRepository.getGifts(accountId, owner_id, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(gifts -> onNetDataReceived(offset, gifts), this::onNetDataGetError));
    }

    private void onNetDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        callView(v -> showError(v, t));
    }

    private void onNetDataReceived(int offset, List<Gift> gifts) {
        cacheLoadingNow = false;

        mEndOfContent = gifts.isEmpty();
        netLoadingNow = false;

        if (offset == 0) {
            mGifts.clear();
            mGifts.addAll(gifts);
            callView(IGiftsView::notifyDataSetChanged);
        } else {
            int startSize = mGifts.size();
            mGifts.addAll(gifts);
            callView(view -> view.notifyDataAdded(startSize, gifts.size()));
        }

        resolveRefreshingView();
    }

    private void requestAtLast() {
        request(0);
    }

    private void requestNext() {
        request(mGifts.size());
    }

    @Override
    public void onGuiCreated(@NonNull IGiftsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mGifts);

        resolveRefreshingView();
    }

    private boolean canLoadMore() {
        return !mGifts.isEmpty() && !cacheLoadingNow && !netLoadingNow && !mEndOfContent;
    }

    public void fireRefresh() {
        netDisposable.clear();
        netLoadingNow = false;

        requestAtLast();
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }

    public void fireOpenWall(int ownerId) {
        callView(v -> v.onOpenWall(getAccountId(), ownerId));
    }
}
