package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.AppChatUser;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IChatUsersDomainView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;


public class ChatUsersDomainPresenter extends AccountDependencyPresenter<IChatUsersDomainView> {

    private final int chatId;

    private final IMessagesRepository messagesInteractor;

    private final List<AppChatUser> users;
    private final List<AppChatUser> original;
    private boolean refreshing;
    private String query;

    public ChatUsersDomainPresenter(int accountId, int chatId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.chatId = chatId;
        users = new ArrayList<>();
        original = new ArrayList<>();
        messagesInteractor = Repository.INSTANCE.getMessages();

        requestData();
    }

    public void setLoadingNow(boolean loadingNow) {
        refreshing = loadingNow;
        resolveRefreshing();
    }

    public void updateCriteria() {
        setLoadingNow(true);
        users.clear();
        if (Utils.isEmpty(query)) {
            users.addAll(original);
            setLoadingNow(false);
            callView(IChatUsersDomainView::notifyDataSetChanged);
            return;
        }
        for (AppChatUser i : original) {
            Owner user = i.getMember();
            if (user.getFullName().toLowerCase().contains(query.toLowerCase()) || user.getDomain().toLowerCase().contains(query.toLowerCase())) {
                users.add(i);
            }
        }
        setLoadingNow(false);
        callView(IChatUsersDomainView::notifyDataSetChanged);
    }

    public void fireQuery(String q) {
        if (Utils.isEmpty(q))
            query = null;
        else {
            query = q;
        }
        updateCriteria();
    }

    @Override
    public void onGuiCreated(@NonNull IChatUsersDomainView view) {
        super.onGuiCreated(view);
        view.displayData(users);
    }

    private void resolveRefreshing() {
        callView(v -> v.displayRefreshing(refreshing));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshing();
    }

    private void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        resolveRefreshing();
    }

    private void requestData() {
        int accountId = getAccountId();

        setRefreshing(true);
        appendDisposable(messagesInteractor.getChatUsers(accountId, chatId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        setRefreshing(false);
        callView(v -> showError(v, t));
    }

    private void onDataReceived(List<AppChatUser> users) {
        setRefreshing(false);

        original.clear();
        original.addAll(users);
        updateCriteria();
    }

    public void fireRefresh() {
        if (!refreshing) {
            requestData();
        }
    }

    public void fireUserClick(AppChatUser user) {
        callView(v -> v.addDomain(getAccountId(), user.getMember()));
    }

    public void fireUserLongClick(AppChatUser user) {
        callView(v -> v.openUserWall(getAccountId(), user.getMember()));
    }
}
