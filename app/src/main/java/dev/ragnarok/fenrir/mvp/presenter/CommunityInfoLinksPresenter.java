package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityInfoLinksView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.functions.Function;


public class CommunityInfoLinksPresenter extends AccountDependencyPresenter<ICommunityInfoLinksView> {

    private final Community groupId;
    private final INetworker networker;
    private final List<VKApiCommunity.Link> links;
    private boolean loadingNow;

    public CommunityInfoLinksPresenter(int accountId, Community groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        networker = Injection.provideNetworkInterfaces();
        this.groupId = groupId;
        links = new ArrayList<>();

        requestLinks();
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityInfoLinksView view) {
        super.onGuiCreated(view);
        view.displayData(links);
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(loadingNow));
    }

    private void requestLinks() {
        int accountId = getAccountId();

        setLoadingNow(true);
        appendDisposable(networker.vkDefault(accountId)
                .groups()
                .getById(Collections.singletonList(groupId.getId()), null, null, "links")
                .map((Function<List<VKApiCommunity>, List<VKApiCommunity.Link>>) dtos -> {
                    if (dtos.size() != 1) {
                        throw new NotFoundException();
                    }

                    List<VKApiCommunity.Link> links = dtos.get(0).links;
                    return Objects.nonNull(links) ? links : Collections.emptyList();
                })
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onLinksReceived, this::onRequestError));
    }

    private void onRequestError(Throwable throwable) {
        setLoadingNow(false);
        callView(v -> showError(v, throwable));
    }

    private void onLinksReceived(List<VKApiCommunity.Link> links) {
        setLoadingNow(false);

        this.links.clear();
        this.links.addAll(links);

        callView(ICommunityInfoLinksView::notifyDataSetChanged);
    }

    public void fireRefresh() {
        requestLinks();
    }

    public void fireLinkClick(VKApiCommunity.Link link) {
        callView(v -> v.openLink(link.url));
    }
}
