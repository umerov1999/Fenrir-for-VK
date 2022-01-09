package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.dummy;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.indexOf;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.crypt.KeyLocationPolicy;
import dev.ragnarok.fenrir.domain.IAccountsInteractor;
import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.exception.UnauthorizedException;
import dev.ragnarok.fenrir.longpoll.ILongpollManager;
import dev.ragnarok.fenrir.longpoll.LongpollInstance;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Dialog;
import dev.ragnarok.fenrir.model.FwdMessages;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.ModelsBundle;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.PeerUpdate;
import dev.ragnarok.fenrir.model.SaveMessageBuilder;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IDialogsView;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.PersistentLogger;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.ShortcutUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class DialogsPresenter extends AccountDependencyPresenter<IDialogsView> {

    private static final int COUNT = 30;

    private static final String SAVE_DIALOGS_OWNER_ID = "save-dialogs-owner-id";
    private static final Comparator<Dialog> COMPARATOR = new DialogByIdMajorID();
    private final ArrayList<Dialog> dialogs;
    private final IMessagesRepository messagesInteractor;
    private final IAccountsInteractor accountsInteractor;
    private final ILongpollManager longpollManager;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private final CompositeDisposable cacheLoadingDisposable = new CompositeDisposable();
    private final ModelsBundle models;
    private int dialogsOwnerId;
    private boolean endOfContent;
    private boolean netLoadingNow;
    private boolean cacheNowLoading;

    public DialogsPresenter(int accountId, int initialDialogsOwnerId, @Nullable ModelsBundle models, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        setSupportAccountHotSwap(true);
        this.models = models;

        dialogs = new ArrayList<>();

        if (nonNull(savedInstanceState)) {
            dialogsOwnerId = savedInstanceState.getInt(SAVE_DIALOGS_OWNER_ID);
        } else {
            dialogsOwnerId = initialDialogsOwnerId;
        }

        messagesInteractor = Repository.INSTANCE.getMessages();
        accountsInteractor = InteractorFactory.createAccountInteractor();
        longpollManager = LongpollInstance.get();

        appendDisposable(messagesInteractor
                .observePeerUpdates()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPeerUpdate, ignore()));

        appendDisposable(messagesInteractor.observePeerDeleting()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(dialog -> onDialogDeleted(dialog.getAccountId(), dialog.getPeerId()), ignore()));

        appendDisposable(longpollManager.observeKeepAlive()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(ignore -> checkLongpoll(), ignore()));

        loadCachedThenActualData();
    }

    private static String getTitleIfEmpty(@NonNull Collection<User> users) {
        return Utils.join(users, ", ", User::getFirstName);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_DIALOGS_OWNER_ID, dialogsOwnerId);
    }

    @Override
    public void onGuiCreated(@NonNull IDialogsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(dialogs, getAccountId());

        // only for user dialogs
        viewHost.setCreateGroupChatButtonVisible(dialogsOwnerId > 0);
    }

    private void onDialogsFirstResponse(List<Dialog> data) {
        if (!Settings.get().other().isBe_online() || Utils.isHiddenAccount(getAccountId())) {
            netDisposable.add(accountsInteractor.setOffline(getAccountId())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(ignore(), ignore()));
        }
        setNetLoadingNow(false);

        endOfContent = false;
        dialogs.clear();
        dialogs.addAll(data);

        safeNotifyDataSetChanged();

        if (Utils.needReloadStickers(getAccountId())) {
            receiveStickers();
        }
    }

    private void onDialogsGetError(Throwable t) {
        Throwable cause = getCauseIfRuntime(t);

        cause.printStackTrace();

        setNetLoadingNow(false);

        if (cause instanceof UnauthorizedException) {
            return;
        }
        PersistentLogger.logThrowable("Dialogs issues", cause);
        callView(v -> showError(v, cause));
    }

    private void setNetLoadingNow(boolean netLoadingNow) {
        this.netLoadingNow = netLoadingNow;
        resolveRefreshingView();
    }

    private void requestAtLast() {
        if (netLoadingNow) {
            return;
        }

        setNetLoadingNow(true);

        netDisposable.add(messagesInteractor.getDialogs(dialogsOwnerId, COUNT, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDialogsFirstResponse, this::onDialogsGetError));

        resolveRefreshingView();
    }

    private void requestNext() {
        if (netLoadingNow) {
            return;
        }

        Integer lastMid = getLastDialogMessageId();
        if (isNull(lastMid)) {
            return;
        }

        setNetLoadingNow(true);
        netDisposable.add(messagesInteractor.getDialogs(dialogsOwnerId, COUNT, lastMid)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onNextDialogsResponse,
                        throwable -> onDialogsGetError(getCauseIfRuntime(throwable))));
    }

    private void onNextDialogsResponse(List<Dialog> data) {
        if (!Settings.get().other().isBe_online() || Utils.isHiddenAccount(getAccountId())) {
            netDisposable.add(accountsInteractor.setOffline(getAccountId())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(ignore(), ignore()));
        }

        setNetLoadingNow(false);
        endOfContent = isEmpty(dialogs);

        int startSize = dialogs.size();
        dialogs.addAll(data);
        callView(v -> v.notifyDataAdded(startSize, data.size()));
    }

    private void onDialogRemovedSuccessfully(int accountId, int peeId) {
        callView(v -> v.showSnackbar(R.string.deleted, true));
        onDialogDeleted(accountId, peeId);
    }

    private void removeDialog(int peeId) {
        int accountId = dialogsOwnerId;

        appendDisposable(messagesInteractor.deleteDialog(accountId, peeId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onDialogRemovedSuccessfully(accountId, peeId), t -> callView(v -> showError(v, t))));
    }

    private void resolveRefreshingView() {
        // on resume only !!!
        callResumedView(v -> v.showRefreshing(cacheNowLoading || netLoadingNow));
    }

    private void loadCachedThenActualData() {
        cacheNowLoading = true;
        resolveRefreshingView();

        cacheLoadingDisposable.add(messagesInteractor.getCachedDialogs(dialogsOwnerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, ignored -> {
                    ignored.printStackTrace();
                    onCachedDataReceived(Collections.emptyList());
                }));
    }

    public void fireRepost(@NonNull Dialog dialog) {
        if (models == null) {
            return;
        }
        ArrayList<Message> fwds = new ArrayList<>();
        SaveMessageBuilder builder = new SaveMessageBuilder(getAccountId(), dialog.getPeerId());
        for (AbsModel model : models) {
            if (model instanceof FwdMessages) {
                fwds.addAll(((FwdMessages) model).fwds);
            } else {
                builder.attach(model);
            }
        }
        builder.setForwardMessages(fwds);
        boolean encryptionEnabled = Settings.get().security().isMessageEncryptionEnabled(getAccountId(), dialog.getPeerId());

        @KeyLocationPolicy
        int keyLocationPolicy = KeyLocationPolicy.PERSIST;
        if (encryptionEnabled) {
            keyLocationPolicy = Settings.get().security().getEncryptionLocationPolicy(getAccountId(), dialog.getPeerId());
        }
        builder.setRequireEncryption(encryptionEnabled).setKeyLocationPolicy(keyLocationPolicy);
        appendDisposable(messagesInteractor.put(builder)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .doOnSuccess(v -> messagesInteractor.runSendingQueue())
                .subscribe(t -> callView(v -> v.showSnackbar(R.string.success, false)), this::onDialogsGetError));
    }

    private void receiveStickers() {
        if (getAccountId() <= 0) {
            return;
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            InteractorFactory.createStickersInteractor()
                    .getAndStore(getAccountId())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(dummy(), ignore());
        } catch (Exception ignored) {
            /*ignore*/
        }
    }

    private void onCachedDataReceived(List<Dialog> data) {
        cacheNowLoading = false;

        dialogs.clear();
        dialogs.addAll(data);

        safeNotifyDataSetChanged();
        resolveRefreshingView();
        callView(v -> v.notifyHasAttachments(models != null));

        if (Settings.get().other().isNot_update_dialogs() || Utils.isHiddenCurrent()) {
            if (Utils.needReloadStickers(getAccountId())) {
                receiveStickers();
            }
            if (Utils.needReloadDialogs(getAccountId())) {
                callView(IDialogsView::askToReload);
            }
        } else {
            requestAtLast();
        }
    }

    private void onPeerUpdate(List<PeerUpdate> updates) {
        for (PeerUpdate update : updates) {
            if (update.getAccountId() == dialogsOwnerId) {
                onDialogUpdate(update);
            }
        }
    }

    private void onDialogUpdate(PeerUpdate update) {
        if (dialogsOwnerId != update.getAccountId()) {
            return;
        }

        int accountId = update.getAccountId();
        int peerId = update.getPeerId();

        if (update.getLastMessage() != null) {
            List<Integer> id = Collections.singletonList(update.getLastMessage().getMessageId());
            appendDisposable(messagesInteractor.findCachedMessages(accountId, id)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(messages -> {
                        if (messages.isEmpty()) {
                            onDialogDeleted(accountId, peerId);
                        } else {
                            onActualMessagePeerMessageReceived(accountId, peerId, update, Optional.wrap(messages.get(0)));
                        }
                    }, ignore()));
        } else {
            onActualMessagePeerMessageReceived(accountId, peerId, update, Optional.empty());
        }
    }

    private void onActualMessagePeerMessageReceived(int accountId, int peerId, PeerUpdate update, Optional<Message> messageOptional) {
        if (accountId != dialogsOwnerId) {
            return;
        }

        int index = indexOf(dialogs, peerId);
        Dialog dialog = index == -1 ? new Dialog().setPeerId(peerId) : dialogs.get(index);

        if (update.getReadIn() != null) {
            dialog.setInRead(update.getReadIn().getMessageId());
        }

        if (update.getReadOut() != null) {
            dialog.setOutRead(update.getReadOut().getMessageId());
        }

        if (update.getUnread() != null) {
            dialog.setUnreadCount(update.getUnread().getCount());
        }

        if (messageOptional.nonEmpty()) {
            Message message = messageOptional.get();
            dialog.setLastMessageId(message.getId());
            dialog.setMinor_id(message.getId());
            dialog.setMessage(message);

            if (dialog.isChat()) {
                dialog.setInterlocutor(message.getSender());
            }
        }

        if (update.getTitle() != null) {
            dialog.setTitle(update.getTitle().getTitle());
        }
        if (index != -1) {
            Collections.sort(dialogs, COMPARATOR);
            safeNotifyDataSetChanged();
        } else {
            if (Peer.isGroup(peerId) || Peer.isUser(peerId)) {
                appendDisposable(Repository.INSTANCE.getOwners().getBaseOwnerInfo(accountId, peerId, IOwnersRepository.MODE_ANY)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(o -> {
                            dialog.setInterlocutor(o);
                            appendDisposable(Repository.INSTANCE.getMessages().insertDialog(accountId, dialog)
                                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                                    .subscribe(() -> {
                                        dialogs.add(dialog);
                                        Collections.sort(dialogs, COMPARATOR);
                                        safeNotifyDataSetChanged();
                                    }, ignore()));
                        }, ignore()));
            } else {
                dialogs.add(dialog);
                Collections.sort(dialogs, COMPARATOR);
                safeNotifyDataSetChanged();
            }
        }
    }

    private void onDialogDeleted(int accountId, int peerId) {
        if (dialogsOwnerId != accountId) {
            return;
        }

        int index = indexOf(dialogs, peerId);
        if (index != -1) {
            dialogs.remove(index);
            safeNotifyDataSetChanged();
        }
    }

    private void safeNotifyDataSetChanged() {
        callView(IDialogsView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheLoadingDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        checkLongpoll();
    }

    private void checkLongpoll() {
        if (getAccountId() != ISettings.IAccountsSettings.INVALID_ID) {
            longpollManager.keepAlive(dialogsOwnerId);
        }
    }

    public void fireRefresh() {
        cacheLoadingDisposable.clear();
        cacheNowLoading = false;

        netDisposable.clear();
        netLoadingNow = false;

        requestAtLast();
    }

    public void fireSearchClick() {
        AssertUtils.assertPositive(dialogsOwnerId);
        callView(v -> v.goToSearch(getAccountId()));
    }

    public void fireImportantClick() {
        AssertUtils.assertPositive(dialogsOwnerId);
        callView(v -> v.goToImportant(getAccountId()));
    }

    public void fireDialogClick(Dialog dialog) {
        openChat(dialog);
    }

    private void openChat(Dialog dialog) {
        callView(v -> v.goToChat(getAccountId(),
                dialogsOwnerId,
                dialog.getPeerId(),
                dialog.getDisplayTitle(getApplicationContext()),
                dialog.getImageUrl()));
    }

    public void fireDialogAvatarClick(Dialog dialog) {
        if (Peer.isUser(dialog.getPeerId()) || Peer.isGroup(dialog.getPeerId())) {
            callView(v -> v.goToOwnerWall(getAccountId(), Peer.toOwnerId(dialog.getPeerId()), dialog.getInterlocutor()));
        } else {
            openChat(dialog);
        }
    }

    private boolean canLoadMore() {
        return !cacheNowLoading && !endOfContent && !netLoadingNow && !dialogs.isEmpty();
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }

    private Integer getLastDialogMessageId() {
        try {
            return dialogs.get(dialogs.size() - 1).getLastMessageId();
        } catch (Exception e) {
            return null;
        }
    }

    public void fireNewGroupChatTitleEntered(List<User> users, String title) {
        String targetTitle = safeIsEmpty(title) ? getTitleIfEmpty(users) : title;
        int accountId = getAccountId();

        appendDisposable(messagesInteractor.createGroupChat(accountId, Utils.idsListOf(users), targetTitle)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(chatid -> onGroupChatCreated(chatid, targetTitle), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onGroupChatCreated(int chatId, String title) {
        callView(view -> view.goToChat(getAccountId(), dialogsOwnerId, Peer.fromChatId(chatId), title, null));
    }

    public void fireUsersForChatSelected(@NonNull ArrayList<Owner> owners) {
        ArrayList<User> users = new ArrayList<>();
        for (Owner i : owners) {
            if (i instanceof User) {
                users.add((User) i);
            }
        }
        if (users.size() == 1) {
            User user = users.get(0);
            // Post?
            callView(v -> v.goToChat(getAccountId(), dialogsOwnerId, Peer.fromUserId(user.getId()), user.getFullName(), user.getMaxSquareAvatar()));
        } else if (users.size() > 1) {
            callView(v -> v.showEnterNewGroupChatTitle(users));
        }
    }

    public void fireRemoveDialogClick(Dialog dialog) {
        removeDialog(dialog.getPeerId());
    }

    public void fireCreateShortcutClick(Dialog dialog) {
        AssertUtils.assertPositive(dialogsOwnerId);

        Context app = getApplicationContext();

        appendDisposable(ShortcutUtils
                .createChatShortcutRx(app, dialog.getImageUrl(), getAccountId(),
                        dialog.getPeerId(), dialog.getDisplayTitle(app))
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onShortcutCreated, throwable -> callView(v -> v.showError(throwable.getMessage()))));
    }

    private void onShortcutCreated() {
        callView(v -> v.showSnackbar(R.string.success, true));
    }

    public void fireNotificationsSettingsClick(Dialog dialog) {
        AssertUtils.assertPositive(dialogsOwnerId);
        callView(v -> v.showNotificationSettings(getAccountId(), dialog.getPeerId()));
    }

    @Override
    protected void afterAccountChange(int oldAid, int newAid) {
        super.afterAccountChange(oldAid, newAid);

        // если на экране диалоги группы, то ничего не трогаем
        if (dialogsOwnerId < 0 && dialogsOwnerId != ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        cacheLoadingDisposable.clear();
        cacheNowLoading = false;

        netDisposable.clear();
        netLoadingNow = false;
        dialogsOwnerId = newAid;

        loadCachedThenActualData();

        longpollManager.forceDestroy(oldAid);
        checkLongpoll();
    }

    public void fireUnPin(Dialog dialog) {
        appendDisposable(messagesInteractor.pinUnPinConversation(getAccountId(), dialog.getPeerId(), false)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                    callView(v -> v.showToast(R.string.success, false));
                    fireRefresh();
                }, throwable -> callView(v -> v.showError(throwable.getMessage()))));
    }

    public void firePin(Dialog dialog) {
        appendDisposable(messagesInteractor.pinUnPinConversation(getAccountId(), dialog.getPeerId(), true)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                    callView(v -> v.showToast(R.string.success, false));
                    fireRefresh();
                }, throwable -> callView(v -> v.showError(throwable.getMessage()))));
    }

    public void fireAddToLauncherShortcuts(Dialog dialog) {
        AssertUtils.assertPositive(dialogsOwnerId);

        Peer peer = new Peer(dialog.getId())
                .setAvaUrl(dialog.getImageUrl())
                .setTitle(dialog.getDisplayTitle(getApplicationContext()));

        Completable completable = ShortcutUtils.addDynamicShortcut(getApplicationContext(), dialogsOwnerId, peer);

        appendDisposable(completable
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> callView(v -> v.showToast(R.string.success, false)), Analytics::logUnexpectedError));
    }

    public void fireRead(Dialog dialog) {
        appendDisposable(messagesInteractor.markAsRead(getAccountId(), dialog.getPeerId(), dialog.getLastMessageId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                    callView(v -> v.showToast(R.string.success, false));
                    dialog.setInRead(dialog.getLastMessageId());
                    callView(IDialogsView::notifyDataSetChanged);
                }, throwable -> callView(v -> v.showError(throwable.getMessage()))));
    }

    public void fireContextViewCreated(IDialogsView.IContextView contextView, Dialog dialog) {
        boolean isHide = Settings.get().security().isHiddenDialog(dialog.getId());
        contextView.setCanDelete(true);
        contextView.setCanRead(!Utils.isHiddenCurrent() && !dialog.isLastMessageOut() && dialog.getLastMessageId() != dialog.getInRead());
        contextView.setCanAddToHomescreen(dialogsOwnerId > 0 && !isHide);
        contextView.setCanAddToShortcuts(dialogsOwnerId > 0 && !isHide);
        contextView.setCanConfigNotifications(dialogsOwnerId > 0);
        contextView.setPinned(dialog.getMajor_id() > 0);
        contextView.setIsHidden(isHide);
    }

    public void fireOptionViewCreated(IDialogsView.IOptionView view) {
        view.setCanSearch(dialogsOwnerId > 0);
    }

    private static class DialogByIdMajorID implements Comparator<Dialog> {
        @Override
        public int compare(Dialog o1, Dialog o2) {
            int res = Integer.compare(o2.getMajor_id(), o1.getMajor_id());
            return res == 0 ? Integer.compare(o2.getMinor_id(), o1.getMinor_id()) : res;
        }
    }
}
