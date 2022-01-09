package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.AppChatUser;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IChatMembersView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;


public class ChatMembersPresenter extends AccountDependencyPresenter<IChatMembersView> {

    private final int chatId;

    private final IMessagesRepository messagesInteractor;

    private final List<AppChatUser> users;
    private boolean refreshing;
    private boolean isOwner;

    public ChatMembersPresenter(int accountId, int chatId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.chatId = chatId;
        users = new ArrayList<>();
        messagesInteractor = Repository.INSTANCE.getMessages();

        requestData();
    }

    @Override
    public void onGuiCreated(@NonNull IChatMembersView view) {
        super.onGuiCreated(view);
        view.displayData(users);
    }

    private void resolveRefreshing() {
        callResumedView(v -> v.displayRefreshing(refreshing));
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

        this.users.clear();
        this.users.addAll(users);

        isOwner = false;
        for (AppChatUser i : users) {
            if (i.getId() == getAccountId()) {
                isOwner = i.isOwner();
                break;
            }
        }
        callView(v -> v.setIsOwner(isOwner));
        callView(IChatMembersView::notifyDataSetChanged);
    }

    public void fireRefresh() {
        if (!refreshing) {
            requestData();
        }
    }

    public void fireAddUserClick() {
        callView(v -> v.startSelectUsersActivity(getAccountId()));
    }

    public void fireUserDeteleConfirmed(AppChatUser user) {
        int accountId = getAccountId();
        int userId = user.getMember().getOwnerId();

        appendDisposable(messagesInteractor.removeChatMember(accountId, chatId, userId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onUserRemoved(userId), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onUserRemoved(int id) {
        int index = Utils.findIndexById(users, id);

        if (index != -1) {
            users.remove(index);
            callView(view -> view.notifyItemRemoved(index));
        }
    }

    public void fireUserSelected(ArrayList<Owner> owners) {
        int accountId = getAccountId();
        ArrayList<User> users = new ArrayList<>();
        for (Owner i : owners) {
            if (i instanceof User) {
                users.add((User) i);
            }
        }
        if (nonEmpty(users)) {
            appendDisposable(messagesInteractor.addChatUsers(accountId, chatId, users)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onChatUsersAdded, this::onChatUsersAddError));
        }
    }

    private void onChatUsersAddError(Throwable t) {
        callView(v -> showError(v, getCauseIfRuntime(t)));
        requestData(); // refresh data
    }

    private void onChatUsersAdded(List<AppChatUser> added) {
        int startSize = users.size();
        users.addAll(added);

        callView(view -> view.notifyDataAdded(startSize, added.size()));
    }

    public void fireUserClick(AppChatUser user) {
        callView(v -> v.openUserWall(getAccountId(), user.getMember()));
    }

    public void fireAdminToggleClick(boolean isAdmin, int ownerId) {
        appendDisposable(messagesInteractor.setMemberRole(getAccountId(), chatId, ownerId, isAdmin)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::fireRefresh, this::onChatUsersAddError));
    }
}