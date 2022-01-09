package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.findIndexById;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.domain.IAccountsInteractor;
import dev.ragnarok.fenrir.domain.IBlacklistRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.BannedPart;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IUserBannedView;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;


public class UserBannedPresenter extends AccountDependencyPresenter<IUserBannedView> {

    private final IAccountsInteractor interactor;
    private final List<User> users;

    private boolean endOfContent;
    private boolean loadinNow;

    public UserBannedPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        interactor = InteractorFactory.createAccountInteractor();

        users = new ArrayList<>();

        loadNextPart(0);

        IBlacklistRepository repository = Injection.provideBlacklistRepository();

        appendDisposable(repository.observeAdding()
                .filter(pair -> pair.getFirst() == getAccountId())
                .map(Pair::getSecond)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUserAdded));

        appendDisposable(repository.observeRemoving()
                .filter(pair -> pair.getFirst() == getAccountId())
                .map(Pair::getSecond)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUserRemoved));
    }

    private void onUserRemoved(int id) {
        int index = findIndexById(users, id);
        if (index != -1) {
            users.remove(index);

            callView(view -> view.notifyItemRemoved(index));
        }
    }

    private void onUserAdded(User user) {
        users.add(0, user);

        callView(view -> {
            view.notifyItemsAdded(0, 1);
            view.scrollToPosition(0);
        });
    }

    @Override
    public void onGuiCreated(@NonNull IUserBannedView view) {
        super.onGuiCreated(view);
        view.displayUserList(users);
    }

    private void onBannedPartReceived(int offset, BannedPart part) {
        setLoadinNow(false);

        endOfContent = part.getUsers().isEmpty();

        if (offset == 0) {
            users.clear();
            users.addAll(part.getUsers());
            callView(IUserBannedView::notifyDataSetChanged);
        } else {
            int startSize = users.size();
            users.addAll(part.getUsers());
            callView(view -> view.notifyItemsAdded(startSize, part.getUsers().size()));
        }

        endOfContent = endOfContent || part.getTotalCount() == users.size();
    }

    private void onBannedPartGetError(Throwable throwable) {
        setLoadinNow(false);
        callView(v -> showError(v, throwable));
    }

    private void setLoadinNow(boolean loadinNow) {
        this.loadinNow = loadinNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(loadinNow));
    }

    private void loadNextPart(int offset) {
        if (loadinNow) return;

        int accountId = getAccountId();

        setLoadinNow(true);
        appendDisposable(interactor.getBanned(accountId, 50, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(part -> onBannedPartReceived(offset, part),
                        throwable -> onBannedPartGetError(getCauseIfRuntime(throwable))));
    }

    public void fireRefresh() {
        loadNextPart(0);
    }

    public void fireButtonAddClick() {
        callView(v -> v.startUserSelection(getAccountId()));
    }

    private void onAddingComplete() {
        callView(IUserBannedView::showSuccessToast);
    }

    private void onAddError(Throwable throwable) {
        callView(v -> showError(v, throwable));
    }

    public void fireUsersSelected(ArrayList<Owner> owners) {
        int accountId = getAccountId();

        ArrayList<User> users = new ArrayList<>();
        for (Owner i : owners) {
            if (i instanceof User) {
                users.add((User) i);
            }
        }
        if (nonEmpty(users)) {
            appendDisposable(interactor.banUsers(accountId, users)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(this::onAddingComplete, throwable -> onAddError(getCauseIfRuntime(throwable))));
        }
    }

    public void fireScrollToEnd() {
        if (!loadinNow && !endOfContent) {
            loadNextPart(users.size());
        }
    }

    private void onRemoveComplete() {
        callView(IUserBannedView::showSuccessToast);
    }

    private void onRemoveError(Throwable throwable) {
        callView(v -> showError(v, throwable));
    }

    public void fireRemoveClick(User user) {
        int accountId = getAccountId();

        appendDisposable(interactor.unbanUser(accountId, user.getId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onRemoveComplete, throwable -> onRemoveError(getCauseIfRuntime(throwable))));
    }

    public void fireUserClick(User user) {
        callView(v -> v.showUserProfile(getAccountId(), user));
    }
}