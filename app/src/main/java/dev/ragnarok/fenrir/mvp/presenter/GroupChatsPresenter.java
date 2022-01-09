package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IUtilsInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.GroupChats;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IGroupChatsView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class GroupChatsPresenter extends AccountDependencyPresenter<IGroupChatsView> {

    private static final int COUNT_PER_REQUEST = 20;

    private final int groupId;
    private final List<GroupChats> chats;
    private final IUtilsInteractor utilsInteractor;
    private final IOwnersRepository owners;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean endOfContent;
    private boolean actualDataReceived;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;
    private int netLoadingNowOffset;

    public GroupChatsPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.groupId = groupId;
        chats = new ArrayList<>();
        utilsInteractor = InteractorFactory.createUtilsInteractor();
        owners = Repository.INSTANCE.getOwners();

        requestActualData(0);
    }

    private void requestActualData(int offset) {
        int accountId = getAccountId();

        netLoadingNow = true;
        netLoadingNowOffset = offset;

        resolveRefreshingView();
        resolveLoadMoreFooter();

        netDisposable.add(owners.getGroupChats(accountId, groupId, offset, COUNT_PER_REQUEST)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(rec_chats -> onActualDataReceived(offset, rec_chats), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        resolveLoadMoreFooter();

        callView(v -> showError(v, t));
    }

    private void onActualDataReceived(int offset, List<GroupChats> rec_chats) {
        cacheLoadingNow = false;

        netLoadingNow = false;
        resolveRefreshingView();
        resolveLoadMoreFooter();

        actualDataReceived = true;
        endOfContent = rec_chats.isEmpty();

        if (offset == 0) {
            chats.clear();
            chats.addAll(rec_chats);
            callView(IGroupChatsView::notifyDataSetChanged);
        } else {
            int startCount = chats.size();
            chats.addAll(rec_chats);
            callView(view -> view.notifyDataAdd(startCount, rec_chats.size()));
        }
    }

    @Override
    public void onGuiCreated(@NonNull IGroupChatsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(chats);

        resolveRefreshingView();
        resolveLoadMoreFooter();
    }

    @Override
    public void onDestroyed() {
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(netLoadingNow));
    }

    private void resolveLoadMoreFooter() {
        if (netLoadingNow && netLoadingNowOffset > 0) {
            callView(v -> v.setupLoadMore(LoadMoreState.LOADING));
            return;
        }

        if (actualDataReceived && !netLoadingNow) {
            callView(v -> v.setupLoadMore(LoadMoreState.CAN_LOAD_MORE));
        }

        callView(v -> v.setupLoadMore(LoadMoreState.END_OF_LIST));
    }

    public void fireLoadMoreClick() {
        if (canLoadMore()) {
            requestActualData(chats.size());
        }
    }

    private boolean canLoadMore() {
        return actualDataReceived && !cacheLoadingNow && !endOfContent && !chats.isEmpty();
    }

    public void fireButtonCreateClick() {
        callView(v -> v.showError(R.string.not_yet_implemented_message));
    }

    public void fireRefresh() {
        netDisposable.clear();
        netLoadingNow = false;

        cacheLoadingNow = false;

        requestActualData(0);
    }

    public void fireGroupChatsClick(@NonNull GroupChats chat) {
        netDisposable.add(utilsInteractor.joinChatByInviteLink(getAccountId(), chat.getInvite_link())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> callView(v -> v.goToChat(getAccountId(), t.chat_id)), this::onActualDataGetError));
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestActualData(chats.size());
        }
    }
}
