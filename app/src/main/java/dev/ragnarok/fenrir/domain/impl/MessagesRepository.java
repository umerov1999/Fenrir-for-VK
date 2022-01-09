package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.longpoll.NotificationHelper.tryCancelNotificationForPeer;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.RxUtils.safelyCloseAction;
import static dev.ragnarok.fenrir.util.Utils.hasFlag;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;
import static dev.ragnarok.fenrir.util.Utils.safelyClose;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.VkRetrofitProvider;
import dev.ragnarok.fenrir.api.interfaces.IDocsApi;
import dev.ragnarok.fenrir.api.interfaces.IMessagesApi;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.AttachmentsTokenCreator;
import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.VKApiConversationMembers;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VkApiConversation;
import dev.ragnarok.fenrir.api.model.VkApiDialog;
import dev.ragnarok.fenrir.api.model.VkApiDoc;
import dev.ragnarok.fenrir.api.model.VkApiJsonString;
import dev.ragnarok.fenrir.api.model.local_json.ChatJsonResponse;
import dev.ragnarok.fenrir.api.model.longpoll.BadgeCountChangeUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.InputMessagesSetReadUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.MessageFlagsResetUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.MessageFlagsSetUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.OutputMessagesSetReadUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.WriteTextInDialogUpdate;
import dev.ragnarok.fenrir.crypt.AesKeyPair;
import dev.ragnarok.fenrir.crypt.CryptHelper;
import dev.ragnarok.fenrir.crypt.KeyLocationPolicy;
import dev.ragnarok.fenrir.crypt.KeyPairDoesNotExistException;
import dev.ragnarok.fenrir.db.PeerStateEntity;
import dev.ragnarok.fenrir.db.interfaces.IDialogsStorage;
import dev.ragnarok.fenrir.db.interfaces.IMessagesStorage;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.MessageEditEntity;
import dev.ragnarok.fenrir.db.model.MessagePatch;
import dev.ragnarok.fenrir.db.model.PeerPatch;
import dev.ragnarok.fenrir.db.model.entity.DialogEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.MessageEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.SimpleDialogEntity;
import dev.ragnarok.fenrir.db.model.entity.StickerEntity;
import dev.ragnarok.fenrir.domain.IMessagesDecryptor;
import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Mode;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Dto;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.domain.mappers.MapUtil;
import dev.ragnarok.fenrir.domain.mappers.Model2Dto;
import dev.ragnarok.fenrir.domain.mappers.Model2Entity;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.exception.UploadNotResolvedException;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AppChatUser;
import dev.ragnarok.fenrir.model.Conversation;
import dev.ragnarok.fenrir.model.CryptStatus;
import dev.ragnarok.fenrir.model.Dialog;
import dev.ragnarok.fenrir.model.IOwnersBundle;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.MessageStatus;
import dev.ragnarok.fenrir.model.MessageUpdate;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.OwnerType;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.PeerDeleting;
import dev.ragnarok.fenrir.model.PeerUpdate;
import dev.ragnarok.fenrir.model.SaveMessageBuilder;
import dev.ragnarok.fenrir.model.SentMsg;
import dev.ragnarok.fenrir.model.Sex;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.WriteText;
import dev.ragnarok.fenrir.model.criteria.DialogsCriteria;
import dev.ragnarok.fenrir.model.criteria.MessagesCriteria;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.IUploadManager;
import dev.ragnarok.fenrir.upload.Method;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Unixtime;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.VKOwnIds;
import dev.ragnarok.fenrir.util.WeakMainLooperHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessagesRepository implements IMessagesRepository {

    private static final SingleTransformer<List<VKApiMessage>, List<MessageEntity>> DTO_TO_DBO = single -> single
            .map(dtos -> {
                List<MessageEntity> dbos = new ArrayList<>(dtos.size());

                for (VKApiMessage dto : dtos) {
                    dbos.add(Dto2Entity.mapMessage(dto));
                }

                return dbos;
            });

    private final ISettings.IAccountsSettings accountsSettings;
    private final IOwnersRepository ownersRepository;
    private final IStorages storages;
    private final INetworker networker;
    private final IMessagesDecryptor decryptor;
    private final IUploadManager uploadManager;

    private final PublishProcessor<List<PeerUpdate>> peerUpdatePublisher = PublishProcessor.create();
    private final PublishProcessor<PeerDeleting> peerDeletingPublisher = PublishProcessor.create();
    private final PublishProcessor<List<MessageUpdate>> messageUpdatesPublisher = PublishProcessor.create();
    private final PublishProcessor<List<WriteText>> writeTextPublisher = PublishProcessor.create();
    private final PublishProcessor<SentMsg> sentMessagesPublisher = PublishProcessor.create();
    private final PublishProcessor<Throwable> sendErrorsPublisher = PublishProcessor.create();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Scheduler senderScheduler = Schedulers.from(Executors.newFixedThreadPool(1));
    private final InternalHandler handler = new InternalHandler(this);
    private boolean nowSending;
    private List<Integer> registeredAccounts;

    public MessagesRepository(ISettings.IAccountsSettings accountsSettings, INetworker networker,
                              IOwnersRepository ownersRepository, IStorages storages, IUploadManager uploadManager) {
        this.accountsSettings = accountsSettings;
        this.ownersRepository = ownersRepository;
        this.networker = networker;
        this.storages = storages;
        decryptor = new MessagesDecryptor(storages);
        this.uploadManager = uploadManager;

        compositeDisposable.add(uploadManager.observeResults()
                .filter(data -> data.getFirst().getDestination().getMethod() == Method.TO_MESSAGE)
                .subscribe(result -> onUpdloadSuccess(result.getFirst()), ignore()));

        compositeDisposable.add(accountsSettings.observeRegistered()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(ignored -> onAccountsChanged(), ignore()));
    }

    private static Conversation entity2Model(int accountId, SimpleDialogEntity entity, IOwnersBundle owners) {
        return new Conversation(entity.getPeerId())
                .setInRead(entity.getInRead())
                .setOutRead(entity.getOutRead())
                .setPhoto50(entity.getPhoto50())
                .setPhoto100(entity.getPhoto100())
                .setPhoto200(entity.getPhoto200())
                .setUnreadCount(entity.getUnreadCount())
                .setTitle(entity.getTitle())
                .setInterlocutor(Peer.isGroup(entity.getPeerId()) || Peer.isUser(entity.getPeerId()) ? owners.getById(entity.getPeerId()) : null)
                .setPinned(isNull(entity.getPinned()) ? null : Entity2Model.message(accountId, entity.getPinned(), owners))
                .setAcl(entity.getAcl())
                .setGroupChannel(entity.isGroupChannel())
                .setCurrentKeyboard(Entity2Model.buildKeyboardFromDbo(entity.getCurrentKeyboard()))
                .setMajor_id(entity.getMajor_id())
                .setMinor_id(entity.getMinor_id());
    }

    private static MessageUpdate patch2Update(int accountId, MessagePatch patch) {
        MessageUpdate update = new MessageUpdate(accountId, patch.getMessageId());
        if (patch.getDeletion() != null) {
            update.setDeleteUpdate(new MessageUpdate.DeleteUpdate(patch.getDeletion().getDeleted(), patch.getDeletion().getDeletedForAll()));
        }
        if (patch.getImportant() != null) {
            update.setImportantUpdate(new MessageUpdate.ImportantUpdate(patch.getImportant().getImportant()));
        }
        return update;
    }

    @Override
    public Flowable<Throwable> observeMessagesSendErrors() {
        return sendErrorsPublisher.onBackpressureBuffer();
    }

    @Override
    public Flowable<List<WriteText>> observeTextWrite() {
        return writeTextPublisher.onBackpressureBuffer();
    }

    private void onAccountsChanged() {
        registeredAccounts = accountsSettings.getRegistered();
    }

    @Override
    public void runSendingQueue() {
        handler.runSend();
    }

    /**
     * Отправить первое неотправленное сообщение
     */
    @MainThread
    private void send() {
        if (nowSending) {
            return;
        }

        nowSending = true;
        sendMessage(registeredAccounts());
    }

    private List<Integer> registeredAccounts() {
        if (registeredAccounts == null) {
            registeredAccounts = accountsSettings.getRegistered();
        }
        return registeredAccounts;
    }

    private void onMessageSent(SentMsg msg) {
        nowSending = false;
        sentMessagesPublisher.onNext(msg);
        send();
    }

    private void onMessageSendError(Throwable t) {
        Throwable cause = Utils.getCauseIfRuntime(t);
        nowSending = false;

        if (cause instanceof NotFoundException) {
            int accountId = Settings.get().accounts().getCurrent();
            if (!Settings.get().other().isBe_online() || Utils.isHiddenAccount(accountId)) {
                compositeDisposable.add(InteractorFactory.createAccountInteractor().setOffline(accountId)
                        .subscribeOn(senderScheduler)
                        .observeOn(Injection.provideMainThreadScheduler())
                        .subscribe(ignore(), ignore()));
            }
            // no unsent messages
            return;
        }

        sendErrorsPublisher.onNext(t);
    }

    private void sendMessage(Collection<Integer> accountIds) {
        nowSending = true;
        compositeDisposable.add(sendUnsentMessage(accountIds)
                .subscribeOn(senderScheduler)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onMessageSent, this::onMessageSendError));
    }

    private void onUpdloadSuccess(Upload upload) {
        int accountId = upload.getAccountId();
        int messagesId = upload.getDestination().getId();

        compositeDisposable.add(uploadManager.get(accountId, upload.getDestination())
                .flatMap(uploads -> {
                    if (!uploads.isEmpty()) {
                        return Single.just(false);
                    }

                    return storages.messages().getMessageStatus(accountId, messagesId)
                            .flatMap(status -> {
                                if (status != MessageStatus.WAITING_FOR_UPLOAD) {
                                    return Single.just(false);
                                }

                                return changeMessageStatus(accountId, messagesId, MessageStatus.QUEUE, null).andThen(Single.just(true));
                            });
                })
                .subscribe(needStartSendingQueue -> {
                    if (needStartSendingQueue) {
                        runSendingQueue();
                    }
                }, ignore()));
    }

    @Override
    public Completable handleFlagsUpdates(int accountId, @Nullable List<MessageFlagsSetUpdate> setUpdates, @Nullable List<MessageFlagsResetUpdate> resetUpdates) {
        List<MessagePatch> patches = new ArrayList<>();

        if (nonEmpty(setUpdates)) {
            for (MessageFlagsSetUpdate update : setUpdates) {
                if (!hasFlag(update.mask, VKApiMessage.FLAG_DELETED)
                        && !hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)
                        && !hasFlag(update.mask, VKApiMessage.FLAG_DELETED_FOR_ALL))
                    continue;

                MessagePatch patch = new MessagePatch(update.message_id, update.peer_id);

                if (hasFlag(update.mask, VKApiMessage.FLAG_DELETED)) {
                    boolean forAll = hasFlag(update.mask, VKApiMessage.FLAG_DELETED_FOR_ALL);
                    patch.setDeletion(new MessagePatch.Deletion(true, forAll));
                }

                if (hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)) {
                    patch.setImportant(new MessagePatch.Important(true));
                }

                patches.add(patch);
            }
        }

        if (nonEmpty(resetUpdates)) {
            for (MessageFlagsResetUpdate update : resetUpdates) {
                if (!hasFlag(update.mask, VKApiMessage.FLAG_DELETED) && !hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT))
                    continue;

                MessagePatch patch = new MessagePatch(update.message_id, update.peer_id);
                if (hasFlag(update.mask, VKApiMessage.FLAG_DELETED)) {
                    patch.setDeletion(new MessagePatch.Deletion(false, false));
                }

                if (hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)) {
                    patch.setImportant(new MessagePatch.Important(false));
                }

                patches.add(patch);
            }
        }

        return applyMessagesPatchesAndPublish(accountId, patches);
    }

    @Override
    public Completable handleWriteUpdates(int accountId, @NonNull List<WriteTextInDialogUpdate> updates) {
        return Completable.fromAction(() -> {
            List<WriteText> list = new ArrayList<>();
            for (WriteTextInDialogUpdate update : updates) {
                list.add(new WriteText(accountId, update.peer_id, update.from_ids, update.is_text));
            }
            writeTextPublisher.onNext(list);
        });
    }

    @Override
    public Completable updateDialogKeyboard(int accountId, int peerId, @Nullable Keyboard keyboard) {
        return storages.dialogs().updateDialogKeyboard(accountId, peerId, Model2Entity.buildKeyboardEntity(keyboard));
    }

    @Override
    public Completable handleUnreadBadgeUpdates(int accountId, @NonNull List<BadgeCountChangeUpdate> updates) {
        return Completable.fromAction(() -> {
            for (BadgeCountChangeUpdate update : updates) {
                storages.dialogs().setUnreadDialogsCount(accountId, update.count);
            }
        });
    }

    @StringRes
    private int GetTypeUser(OwnerInfo ownr) {
        if (ownr.getOwner().getOwnerType() == OwnerType.USER) {
            switch (ownr.getUser().getSex()) {
                case Sex.MAN:
                    return R.string.user_readed_yor_message_man;
                case Sex.WOMAN:
                    return R.string.user_readed_yor_message_woman;
                case Sex.UNKNOWN:
                default:
                    return R.string.user_readed_yor_message;
            }
        }
        return R.string.user_readed_yor_message;
    }

    @Override
    public Completable handleReadUpdates(int accountId, @Nullable List<OutputMessagesSetReadUpdate> outgoing, @Nullable List<InputMessagesSetReadUpdate> incoming) {
        List<PeerPatch> patches = new ArrayList<>();

        if (nonEmpty(outgoing)) {
            for (OutputMessagesSetReadUpdate update : outgoing) {
                if (!Settings.get().other().isDisable_notifications() && Settings.get().other().isInfo_reading() && update.peer_id < VKApiMessage.CHAT_PEER) {
                    compositeDisposable.add(OwnerInfo.getRx(Injection.provideApplicationContext(), Settings.get().accounts().getCurrent(), update.peer_id)
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(userInfo -> CustomToast.CreateCustomToast(Injection.provideApplicationContext()).setBitmap(userInfo.getAvatar()).showToastInfo(userInfo.getOwner().getFullName() + " " + Injection.provideApplicationContext().getString(GetTypeUser(userInfo))), throwable -> {
                            }));
                }
                patches.add(new PeerPatch(update.peer_id).withOutRead(update.local_id));
            }
        }

        if (nonEmpty(incoming)) {
            for (InputMessagesSetReadUpdate update : incoming) {
                patches.add(new PeerPatch(update.peer_id).withInRead(update.local_id).withUnreadCount(update.unread_count));
                tryCancelNotificationForPeer(Injection.provideApplicationContext(), accountId, update.getPeerId());
            }
        }

        return applyPeerUpdatesAndPublish(accountId, patches);
    }

    @Override
    public Flowable<SentMsg> observeSentMessages() {
        return sentMessagesPublisher.onBackpressureBuffer();
    }

    @Override
    public Flowable<List<MessageUpdate>> observeMessageUpdates() {
        return messageUpdatesPublisher.onBackpressureBuffer();
    }

    @Override
    public Flowable<List<PeerUpdate>> observePeerUpdates() {
        return peerUpdatePublisher.onBackpressureBuffer();
    }

    @Override
    public Flowable<PeerDeleting> observePeerDeleting() {
        return peerDeletingPublisher.onBackpressureBuffer();
    }

    @Override
    public Single<Conversation> getConversationSingle(int accountId, int peerId, @NonNull Mode mode) {
        Single<Optional<Conversation>> cached = getCachedConversation(accountId, peerId);
        Single<Conversation> actual = getActualConversaction(accountId, peerId);

        switch (mode) {
            case ANY:
                return cached.flatMap(optional -> optional.isEmpty() ? actual : Single.just(optional.get()));
            case NET:
                return actual;
            case CACHE:
                return cached
                        .flatMap(optional -> optional.isEmpty() ? Single.error(new NotFoundException()) : Single.just(optional.get()));
        }

        throw new IllegalArgumentException("Unsupported mode: " + mode);
    }

    private Single<Optional<Conversation>> getCachedConversation(int accountId, int peerId) {
        return storages.dialogs()
                .findSimple(accountId, peerId)
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Single.just(Optional.empty());
                    }

                    return Single.just(optional.get())
                            .compose(simpleEntity2Conversation(accountId, Collections.emptyList()))
                            .map(Optional::wrap);
                });
    }

    private Single<Conversation> getActualConversaction(int accountId, int peerId) {
        return networker.vkDefault(accountId)
                .messages()
                .getConversations(Collections.singletonList(peerId), true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    if (isEmpty(response.items)) {
                        return Single.error(new NotFoundException());
                    }

                    VkApiConversation dto = response.items.get(0);
                    SimpleDialogEntity entity = Dto2Entity.mapConversation(dto);

                    List<Owner> existsOwners = Dto2Model.transformOwners(response.profiles, response.groups);
                    OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);

                    return ownersRepository.insertOwners(accountId, ownerEntities)
                            .andThen(storages.dialogs().saveSimple(accountId, entity))
                            .andThen(Single.just(entity))
                            .compose(simpleEntity2Conversation(accountId, existsOwners));
                });
    }

    @Override
    public Flowable<Conversation> getConversation(int accountId, int peerId, @NonNull Mode mode) {
        Single<Optional<Conversation>> cached = getCachedConversation(accountId, peerId);
        Single<Conversation> actual = getActualConversaction(accountId, peerId);

        switch (mode) {
            case ANY:
                return cached
                        .flatMap(optional -> optional.isEmpty() ? actual : Single.just(optional.get()))
                        .toFlowable();
            case NET:
                return actual.toFlowable();
            case CACHE:
                return cached
                        .flatMap(optional -> optional.isEmpty() ? Single.error(new NotFoundException()) : Single.just(optional.get()))
                        .toFlowable();
            case CACHE_THEN_ACTUAL:
                Flowable<Conversation> cachedFlowable = cached.toFlowable()
                        .filter(Optional::nonEmpty)
                        .map(Optional::get);

                return Flowable.concat(cachedFlowable, actual.toFlowable());
        }

        throw new IllegalArgumentException("Unsupported mode: " + mode);
    }

    private SingleTransformer<SimpleDialogEntity, Conversation> simpleEntity2Conversation(int accountId, Collection<Owner> existingOwners) {
        return single -> single
                .flatMap(entity -> {
                    VKOwnIds owners = new VKOwnIds();
                    if (Peer.isGroup(entity.getPeerId()) || Peer.isUser(entity.getPeerId())) {
                        owners.append(entity.getPeerId());
                    }

                    if (nonNull(entity.getPinned())) {
                        Entity2Model.fillOwnerIds(owners, Collections.singletonList(entity.getPinned()));
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, owners.getAll(), IOwnersRepository.MODE_ANY, existingOwners)
                            .map(bundle -> entity2Model(accountId, entity, bundle));
                });
    }

    @Override
    public Single<Message> edit(int accountId, @NonNull Message message, String body, @NonNull List<AbsModel> attachments, boolean keepForwardMessages) {
        List<IAttachmentToken> attachmentTokens = Model2Dto.createTokens(attachments);
        return networker.vkDefault(accountId)
                .messages()
                .edit(message.getPeerId(), message.getId(), body, attachmentTokens, keepForwardMessages, null)
                .andThen(getById(accountId, message.getId()));
    }

    @Override
    public Single<List<Message>> getCachedPeerMessages(int accountId, int peerId) {
        MessagesCriteria criteria = new MessagesCriteria(accountId, peerId);
        return storages.messages()
                .getByCriteria(criteria, true, true)
                .compose(entities2Models(accountId))
                .compose(decryptor.withMessagesDecryption(accountId));
    }

    @Override
    public Single<Pair<Peer, List<Message>>> getMessagesFromLocalJSon(int accountId, Context context) {
        Gson gson = VkRetrofitProvider.getVkgson();
        try {
            InputStreamReader b = new InputStreamReader(context.getContentResolver().openInputStream(((Activity) context).getIntent().getData()));
            ChatJsonResponse resp = gson.fromJson(b, ChatJsonResponse.class);
            b.close();
            if (resp == null || isEmpty(resp.page_title)) {
                return Single.error(new Throwable("parsing error"));
            }
            VKOwnIds ids = new VKOwnIds().append(resp.messages);
            return ownersRepository.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY, Collections.emptyList())
                    .map(bundle -> new Pair<>(new Peer(resp.page_id).setAvaUrl(resp.page_avatar).setTitle(resp.page_title), Dto2Model.transformMessages(resp.page_id, resp.messages, bundle)));
        } catch (Throwable e) {
            e.printStackTrace();
            return Single.error(e);
        }
    }

    @Override
    public Single<List<Dialog>> getCachedDialogs(int accountId) {
        DialogsCriteria criteria = new DialogsCriteria(accountId);

        return storages.dialogs()
                .getDialogs(criteria)
                .flatMap(dbos -> {
                    VKOwnIds ownIds = new VKOwnIds();

                    for (DialogEntity dbo : dbos) {
                        switch (Peer.getType(dbo.getPeerId())) {
                            case Peer.GROUP:
                            case Peer.USER:
                                ownIds.append(dbo.getPeerId());
                                break;

                            case Peer.CHAT:
                                ownIds.append(dbo.getMessage().getFromId());
                                break;
                        }
                    }

                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY)
                            .flatMap(owners -> {
                                List<Message> messages = new ArrayList<>(0);
                                List<Dialog> dialogs = new ArrayList<>(dbos.size());

                                for (DialogEntity dbo : dbos) {
                                    Dialog dialog = Entity2Model.buildDialogFromDbo(accountId, dbo, owners);
                                    dialogs.add(dialog);

                                    if (dbo.getMessage().isEncrypted()) {
                                        messages.add(dialog.getMessage());
                                    }
                                }

                                if (nonEmpty(messages)) {
                                    return Single.just(messages)
                                            .compose(decryptor.withMessagesDecryption(accountId))
                                            .map(ignored -> dialogs);
                                }

                                return Single.just(dialogs);
                            });
                });
    }

    private Single<Message> getById(int accountId, int messageId) {
        return networker.vkDefault(accountId)
                .messages()
                .getById(Collections.singletonList(messageId))
                .map(dtos -> MapUtil.mapAll(dtos, Dto2Entity::mapMessage))
                .compose(entities2Models(accountId))
                .flatMap(messages -> {
                    if (messages.isEmpty()) {
                        return Single.error(new NotFoundException());
                    }
                    return Single.just(messages.get(0));
                });
    }

    private SingleTransformer<List<MessageEntity>, List<Message>> entities2Models(int accountId) {
        return single -> single
                .flatMap(dbos -> {
                    VKOwnIds ownIds = new VKOwnIds();
                    Entity2Model.fillOwnerIds(ownIds, dbos);

                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY)
                            .map(owners -> {
                                List<Message> messages = new ArrayList<>(dbos.size());

                                for (MessageEntity dbo : dbos) {
                                    messages.add(Entity2Model.message(accountId, dbo, owners));
                                }

                                return messages;
                            });
                });
    }

    private Completable insertPeerMessages(int accountId, int peerId, List<VKApiMessage> messages, boolean clearBefore) {
        return Single.just(messages)
                .compose(DTO_TO_DBO)
                .flatMapCompletable(dbos -> storages.messages().insertPeerDbos(accountId, peerId, dbos, clearBefore));
    }

    @Override
    public Completable insertMessages(int accountId, List<VKApiMessage> messages) {
        return Single.just(messages)
                .compose(DTO_TO_DBO)
                .flatMap(dbos -> storages.messages().insert(accountId, dbos))
                .flatMapCompletable(ints -> {
                    Set<Integer> peers = new HashSet<>();

                    for (VKApiMessage m : messages) {
                        peers.add(m.peer_id);
                    }

                    return storages.dialogs()
                            .findPeerStates(accountId, peers)
                            .flatMapCompletable(peerStates -> {
                                List<PeerPatch> patches = new ArrayList<>(peerStates.size());

                                for (PeerStateEntity state : peerStates) {
                                    int unread = state.getUnreadCount();
                                    int messageId = state.getLastMessageId();

                                    for (VKApiMessage m : messages) {
                                        if (m.peer_id != state.getPeerId()) continue;

                                        if (m.out) {
                                            unread = 0;
                                        } else {
                                            unread++;
                                        }

                                        if (m.id > messageId) {
                                            messageId = m.id;
                                        }
                                    }

                                    patches.add(new PeerPatch(state.getPeerId())
                                            .withUnreadCount(unread)
                                            .withLastMessage(messageId));
                                }

                                return applyPeerUpdatesAndPublish(accountId, patches);
                            });
                });
    }

    private Completable applyPeerUpdatesAndPublish(int accountId, List<PeerPatch> patches) {
        List<PeerUpdate> updates = new ArrayList<>();
        for (PeerPatch p : patches) {
            PeerUpdate update = new PeerUpdate(accountId, p.getId());
            if (p.getInRead() != null) {
                update.setReadIn(new PeerUpdate.Read(p.getInRead().getId()));
            }

            if (p.getOutRead() != null) {
                update.setReadOut(new PeerUpdate.Read(p.getOutRead().getId()));
            }

            if (p.getLastMessage() != null) {
                update.setLastMessage(new PeerUpdate.LastMessage(p.getLastMessage().getId()));
            }

            if (p.getUnread() != null) {
                update.setUnread(new PeerUpdate.Unread(p.getUnread().getCount()));
            }

            if (p.getTitle() != null) {
                update.setTitle(new PeerUpdate.Title(p.getTitle().getTitle()));
            }

            updates.add(update);
        }

        return storages.dialogs().applyPatches(accountId, patches)
                .doOnComplete(() -> peerUpdatePublisher.onNext(updates));
    }

    @Override
    public Single<List<Message>> getImportantMessages(int accountId, int count, Integer offset,
                                                      Integer startMessageId) {
        return networker.vkDefault(accountId)
                .messages()
                .getImportantMessages(offset, count, startMessageId, true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiMessage> dtos = response.messages == null ? Collections.emptyList() : listEmptyIfNull(response.messages.items);
                    if (nonNull(startMessageId) && nonEmpty(dtos) && startMessageId == dtos.get(0).id) {
                        dtos.remove(0);
                    }
                    Completable completable = Completable.complete();
                    VKOwnIds ownerIds = new VKOwnIds();
                    ownerIds.append(dtos);

                    List<Owner> existsOwners = Dto2Model.transformOwners(response.profiles, response.groups);
                    OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);

                    return completable
                            .andThen(ownersRepository
                                    .findBaseOwnersDataAsBundle(accountId, ownerIds.getAll(), IOwnersRepository.MODE_ANY, existsOwners)
                                    .flatMap(owners -> {
                                        Completable insertCompletable = ownersRepository.insertOwners(accountId, ownerEntities);
                                        List<Message> messages = new ArrayList<>(dtos.size());
                                        for (VKApiMessage dto : dtos) {
                                            messages.add(Dto2Model.transform(accountId, dto, owners));
                                        }

                                        return insertCompletable.andThen(Single.just(messages)
                                                .compose(decryptor.withMessagesDecryption(accountId)));
                                    }));
                });
    }

    @Override
    public Single<List<String>> getJsonHistory(int accountId, Integer offset, Integer count, int peerId) {
        return networker.vkDefault(accountId)
                .messages()
                .getJsonHistory(offset, count, peerId)
                .flatMap(response -> {
                    List<VkApiJsonString> dtos = listEmptyIfNull(response.items);
                    List<String> messages = new ArrayList<>(dtos.size());
                    for (VkApiJsonString i : dtos) {
                        if (!isEmpty(i.json_data)) {
                            messages.add(i.json_data);
                        }
                    }
                    return Single.just(messages);
                });
    }

    @Override
    public Single<List<Message>> getPeerMessages(int accountId, int peerId, int count, Integer offset,
                                                 Integer startMessageId, boolean cacheData, boolean rev) {
        if (rev)
            count = 200;
        return networker.vkDefault(accountId)
                .messages()
                .getHistory(offset, count, peerId, startMessageId, rev, true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiMessage> dtos = listEmptyIfNull(response.messages);

                    PeerPatch patch = null;
                    if (isNull(startMessageId) && cacheData && nonEmpty(response.conversations)) {
                        VkApiConversation conversation = response.conversations.get(0);
                        patch = new PeerPatch(peerId)
                                .withOutRead(conversation.outRead)
                                .withInRead(conversation.inRead)
                                .withLastMessage(conversation.lastMessageId)
                                .withUnreadCount(conversation.unreadCount);
                    }

                    if (nonNull(startMessageId) && nonEmpty(dtos) && startMessageId == dtos.get(0).id) {
                        dtos.remove(0);
                    }

                    Completable completable;
                    if (cacheData) {
                        completable = insertPeerMessages(accountId, peerId, dtos, isNull(startMessageId));
                        if (patch != null) {
                            completable = completable.andThen(applyPeerUpdatesAndPublish(accountId, Collections.singletonList(patch)));
                        }
                    } else {
                        completable = Completable.complete();
                    }

                    VKOwnIds ownerIds = new VKOwnIds();
                    ownerIds.append(dtos);

                    List<Owner> existsOwners = Dto2Model.transformOwners(response.profiles, response.groups);
                    OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);

                    return completable
                            .andThen(ownersRepository
                                    .findBaseOwnersDataAsBundle(accountId, ownerIds.getAll(), IOwnersRepository.MODE_ANY, existsOwners)
                                    .flatMap(owners -> {
                                        Completable insertCompletable = ownersRepository.insertOwners(accountId, ownerEntities);
                                        if (isNull(startMessageId) && cacheData) {
                                            // Это важно !!!
                                            // Если мы получаем сообщения сначала и кэшируем их в базу,
                                            // то нельзя отдать этот список в ответ (как сделано чуть ниже)
                                            // Так как мы теряем сообщения со статусами, отличными от SENT
                                            return insertCompletable.andThen(getCachedPeerMessages(accountId, peerId));
                                        }

                                        List<Message> messages = new ArrayList<>(dtos.size());
                                        for (VKApiMessage dto : dtos) {
                                            messages.add(Dto2Model.transform(accountId, dto, owners));
                                        }

                                        return insertCompletable.andThen(Single.just(messages)
                                                .compose(decryptor.withMessagesDecryption(accountId)));
                                    }));
                });
    }

    @Override
    public Completable insertDialog(int accountId, @NonNull Dialog dialog) {
        IDialogsStorage dialogsStore = storages.dialogs();
        return dialogsStore.insertDialogs(accountId, Collections.singletonList(Model2Entity.buildDialog(dialog)), false);
    }

    @Override
    public Single<List<Dialog>> getDialogs(int accountId, int count, Integer startMessageId) {
        boolean clear = isNull(startMessageId);
        IDialogsStorage dialogsStore = storages.dialogs();

        return networker.vkDefault(accountId)
                .messages()
                .getDialogs(null, count, startMessageId, true, Constants.MAIN_OWNER_FIELDS)
                .map(response -> {
                    if (nonNull(startMessageId) && safeCountOf(response.dialogs) > 0) {
                        // remove first item, because we will have duplicate with previous response
                        response.dialogs.remove(0);
                    }
                    return response;
                })
                .flatMap(response -> {
                    List<VkApiDialog> apiDialogs = listEmptyIfNull(response.dialogs);

                    Collection<Integer> ownerIds;

                    if (nonEmpty(apiDialogs)) {
                        VKOwnIds vkOwnIds = new VKOwnIds();
                        vkOwnIds.append(accountId); // добавляем свой профайл на всякий случай

                        for (VkApiDialog dialog : apiDialogs) {
                            vkOwnIds.append(dialog);
                        }

                        ownerIds = vkOwnIds.getAll();
                    } else {
                        ownerIds = Collections.emptyList();
                    }

                    List<Owner> existsOwners = Dto2Model.transformOwners(response.profiles, response.groups);
                    OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);

                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ownerIds, IOwnersRepository.MODE_ANY, existsOwners)
                            .flatMap(owners -> {
                                List<DialogEntity> entities = new ArrayList<>(apiDialogs.size());
                                List<Dialog> dialogs = new ArrayList<>(apiDialogs.size());
                                List<Message> encryptedMessages = new ArrayList<>(0);

                                for (VkApiDialog dto : apiDialogs) {
                                    DialogEntity entity = Dto2Entity.mapDialog(dto);
                                    entities.add(entity);

                                    Dialog dialog = Dto2Model.transform(accountId, dto, owners);
                                    dialogs.add(dialog);

                                    if (entity.getMessage().isEncrypted()) {
                                        encryptedMessages.add(dialog.getMessage());
                                    }
                                }

                                Completable insertCompletable = dialogsStore
                                        .insertDialogs(accountId, entities, clear)
                                        .andThen(ownersRepository.insertOwners(accountId, ownerEntities))
                                        .doOnComplete(() -> dialogsStore.setUnreadDialogsCount(accountId, response.unreadCount));

                                if (nonEmpty(encryptedMessages)) {
                                    return insertCompletable.andThen(Single.just(encryptedMessages)
                                            .compose(decryptor.withMessagesDecryption(accountId))
                                            .map(ignored -> dialogs));
                                }

                                return insertCompletable.andThen(Single.just(dialogs));
                            });
                });
    }

    @Override
    public Single<List<Message>> findCachedMessages(int accountId, List<Integer> ids) {
        return storages.messages()
                .findMessagesByIds(accountId, ids, true, true)
                .compose(entities2Models(accountId))
                .compose(decryptor.withMessagesDecryption(accountId));
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public Single<Message> put(SaveMessageBuilder builder) {
        int accountId = builder.getAccountId();
        Integer draftMessageId = builder.getDraftMessageId();
        int peerId = builder.getPeerId();

        return getTargetMessageStatus(builder)
                .flatMap(status -> {
                    MessageEditEntity patch = new MessageEditEntity(status, accountId);

                    patch.setEncrypted(builder.isRequireEncryption());
                    patch.setPayload(builder.getPayload());
                    patch.setKeyboard(builder.getKeyboard());
                    patch.setDate(Unixtime.now());
                    patch.setRead(false);
                    patch.setOut(true);
                    patch.setDeleted(false);
                    patch.setImportant(false);

                    File voice = builder.getVoiceMessageFile();

                    if (nonNull(voice)) {
                        Map<Integer, String> extras = new HashMap<>(1);
                        extras.put(Message.Extra.VOICE_RECORD, voice.getAbsolutePath());
                        patch.setExtras(extras);
                    }

                    if (nonEmpty(builder.getAttachments())) {
                        patch.setAttachments(Model2Entity.buildDboAttachments(builder.getAttachments()));
                    }

                    List<Message> fwds = builder.getForwardMessages();
                    if (nonEmpty(fwds)) {
                        List<MessageEntity> fwddbos = new ArrayList<>(fwds.size());

                        for (Message message : fwds) {
                            MessageEntity fwddbo = Model2Entity.buildMessageEntity(message);
                            fwddbo.setOriginalId(message.getId()); // сохранить original_id необходимо, так как при вставке в таблицу _ID потеряется

                            // fixes
                            if (fwddbo.isOut()) {
                                fwddbo.setFromId(accountId);
                            }

                            fwddbos.add(fwddbo);
                        }

                        patch.setForward(fwddbos);
                    } else {
                        patch.setForward(Collections.emptyList());
                    }

                    return getFinalMessagesBody(builder)
                            .flatMap(body -> {
                                patch.setBody(body.get());

                                Single<Integer> storeSingle;
                                if (nonNull(draftMessageId)) {
                                    storeSingle = storages.messages().applyPatch(accountId, draftMessageId, patch);
                                } else {
                                    storeSingle = storages.messages().insert(accountId, peerId, patch);
                                }

                                return storeSingle
                                        .flatMap(resultMid -> storages.messages()
                                                .findMessagesByIds(accountId, Collections.singletonList(resultMid), true, true)
                                                .compose(entities2Models(accountId))
                                                .map(messages -> {
                                                    if (messages.isEmpty()) {
                                                        throw new NotFoundException();
                                                    }

                                                    Message message = messages.get(0);

                                                    if (builder.isRequireEncryption()) {
                                                        message.setDecryptedBody(builder.getBody());
                                                        message.setCryptStatus(CryptStatus.DECRYPTED);
                                                    }

                                                    return message;
                                                }));
                            });
                });
    }

    private Completable changeMessageStatus(int accountId, int messageId, @MessageStatus int status, @Nullable Integer vkid) {
        MessageUpdate update = new MessageUpdate(accountId, messageId);
        update.setStatusUpdate(new MessageUpdate.StatusUpdate(status, vkid));
        return storages.messages()
                .changeMessageStatus(accountId, messageId, status, vkid)
                .onErrorComplete()
                .doOnComplete(() -> messageUpdatesPublisher.onNext(Collections.singletonList(update)));
    }

    @Override
    public Completable enqueueAgainList(int accountId, Collection<Integer> ids) {
        ArrayList<MessageUpdate> updates = new ArrayList<>(ids.size());
        for (Integer i : ids) {
            MessageUpdate update = new MessageUpdate(accountId, i);
            update.setStatusUpdate(new MessageUpdate.StatusUpdate(MessageStatus.QUEUE, null));
            updates.add(update);
        }
        return storages.messages()
                .changeMessagesStatus(accountId, ids, MessageStatus.QUEUE)
                .onErrorComplete()
                .doOnComplete(() -> messageUpdatesPublisher.onNext(updates));
    }

    @Override
    public Completable enqueueAgain(int accountId, int messageId) {
        return changeMessageStatus(accountId, messageId, MessageStatus.QUEUE, null);
    }

    @Override
    public Single<SentMsg> sendUnsentMessage(Collection<Integer> accountIds) {
        IMessagesStorage store = storages.messages();

        return store
                .findFirstUnsentMessage(accountIds, true, false)
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Single.error(new NotFoundException());
                    }

                    MessageEntity entity = optional.get().getSecond();
                    int accountId = optional.get().getFirst();
                    int dbid = entity.getId();
                    int peerId = entity.getPeerId();

                    return changeMessageStatus(accountId, dbid, MessageStatus.SENDING, null)
                            .andThen(internalSend(accountId, entity)
                                    .flatMap(vkid -> {
                                        PeerPatch patch = new PeerPatch(entity.getPeerId())
                                                .withLastMessage(vkid)
                                                .withUnreadCount(0);

                                        return changeMessageStatus(accountId, dbid, MessageStatus.SENT, vkid)
                                                .andThen(applyPeerUpdatesAndPublish(accountId, Collections.singletonList(patch)))
                                                .andThen(Single.just(new SentMsg(dbid, vkid, peerId, accountId)));
                                    })
                                    .onErrorResumeNext(throwable -> changeMessageStatus(accountId, dbid, MessageStatus.ERROR, null).andThen(Single.error(throwable))));
                });
    }

    @Override
    public Single<List<Conversation>> searchConversations(int accountId, int count, String q) {
        return networker.vkDefault(accountId)
                .messages()
                .searchConversations(q, count, 1, Constants.MAIN_OWNER_FIELDS)
                .flatMap(chattables -> {
                    List<VkApiConversation> conversations = listEmptyIfNull(chattables.conversations);
                    Collection<Integer> ownerIds;

                    if (nonEmpty(conversations)) {
                        VKOwnIds vkOwnIds = new VKOwnIds();
                        vkOwnIds.append(accountId);

                        for (VkApiConversation dialog : conversations) {
                            vkOwnIds.append(dialog);
                        }
                        ownerIds = vkOwnIds.getAll();
                    } else {
                        ownerIds = Collections.emptyList();
                    }
                    List<Owner> existsOwners = Dto2Model.transformOwners(chattables.profiles, chattables.groups);
                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ownerIds, IOwnersRepository.MODE_ANY, existsOwners)
                            .flatMap(bundle -> {
                                List<Conversation> models = new ArrayList<>(conversations.size());
                                for (VkApiConversation dialog : conversations) {
                                    models.add(Dto2Model.transform(accountId, dialog, bundle));
                                }
                                return Single.just(models);
                            });
                });
    }

    @Override
    public Single<List<Message>> searchMessages(int accountId, Integer peerId, int count, int offset, String q) {
        return networker.vkDefault(accountId)
                .messages()
                .search(q, peerId, null, null, offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    VKOwnIds ids = new VKOwnIds().append(dtos);

                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY)
                            .map(bundle -> {
                                List<Message> data = new ArrayList<>(dtos.size());
                                for (VKApiMessage dto : dtos) {
                                    Message message = Dto2Model.transform(accountId, dto, bundle);
                                    data.add(message);
                                }

                                return data;
                            })
                            .compose(decryptor.withMessagesDecryption(accountId));
                });
    }

    @Override
    public Single<List<AppChatUser>> getChatUsers(int accountId, int chatId) {
        return networker.vkDefault(accountId)
                .messages()
                .getConversationMembers(Peer.fromChatId(chatId), Constants.MAIN_OWNER_FIELDS)
                .flatMap(chatDto -> {
                    List<VKApiConversationMembers> dtos = listEmptyIfNull(chatDto.conversationMembers);
                    Collection<Integer> ownerIds;

                    if (nonEmpty(dtos)) {
                        VKOwnIds vkOwnIds = new VKOwnIds();
                        vkOwnIds.append(accountId);

                        for (VKApiConversationMembers dto : dtos) {
                            vkOwnIds.append(dto.member_id);
                            vkOwnIds.append(dto.invited_by);
                        }
                        ownerIds = vkOwnIds.getAll();
                    } else {
                        ownerIds = Collections.emptyList();
                    }
                    List<Owner> existsOwners = Dto2Model.transformOwners(chatDto.profiles, chatDto.groups);
                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ownerIds, IOwnersRepository.MODE_ANY, existsOwners)
                            .map(ownersBundle -> {
                                List<AppChatUser> models = new ArrayList<>(dtos.size());

                                for (VKApiConversationMembers dto : dtos) {
                                    AppChatUser user = new AppChatUser(ownersBundle.getById(dto.member_id), dto.invited_by);
                                    user.setCanRemove(dto.can_kick);
                                    user.setJoin_date(dto.join_date);
                                    user.setAdmin(dto.is_admin);
                                    user.setOwner(dto.is_owner);

                                    if (user.getInvitedBy() != 0) {
                                        user.setInviter(ownersBundle.getById(user.getInvitedBy()));
                                    }

                                    models.add(user);
                                }

                                return models;
                            });
                });
    }

    @Override
    public Completable removeChatMember(int accountId, int chatId, int memberId) {
        return networker.vkDefault(accountId)
                .messages()
                .removeChatMember(chatId, memberId)
                .ignoreElement();
    }

    @Override
    public Completable deleteChatPhoto(int accountId, int chatId) {
        return networker.vkDefault(accountId)
                .messages()
                .deleteChatPhoto(chatId)
                .ignoreElement();
    }

    @Override
    public Single<List<AppChatUser>> addChatUsers(int accountId, int chatId, List<User> users) {
        IMessagesApi api = networker.vkDefault(accountId).messages();

        return ownersRepository.getBaseOwnerInfo(accountId, accountId, IOwnersRepository.MODE_ANY)
                .flatMap(iam -> {
                    Completable completable = Completable.complete();

                    List<AppChatUser> data = new ArrayList<>();

                    for (User user : users) {
                        completable = completable.andThen(api.addChatUser(chatId, user.getId()).ignoreElement());

                        AppChatUser chatUser = new AppChatUser(user, accountId)
                                .setCanRemove(true)
                                .setInviter(iam);

                        data.add(chatUser);
                    }

                    return completable.andThen(Single.just(data));
                });
    }

    @Override
    public Completable deleteDialog(int accountId, int peedId) {
        return networker.vkDefault(accountId)
                .messages()
                .deleteDialog(peedId)
                .flatMapCompletable(ignored -> storages.dialogs()
                        .removePeerWithId(accountId, peedId)
                        .andThen(storages.messages().insertPeerDbos(accountId, peedId, Collections.emptyList(), true)))
                .doOnComplete(() -> peerDeletingPublisher.onNext(new PeerDeleting(accountId, peedId)));
    }

    @Override
    public Completable deleteMessages(int accountId, int peerId, @NonNull Collection<Integer> ids, boolean forAll, boolean spam) {
        return networker.vkDefault(accountId)
                .messages()
                .delete(ids, forAll, spam)
                .flatMapCompletable(result -> {
                    List<MessagePatch> patches = new ArrayList<>(result.size());

                    for (Map.Entry<String, Integer> entry : result.entrySet()) {
                        boolean removed = entry.getValue() == 1;
                        int removedId = Integer.parseInt(entry.getKey());

                        if (removed) {
                            MessagePatch patch = new MessagePatch(removedId, peerId);
                            patch.setDeletion(new MessagePatch.Deletion(true, forAll));
                            patches.add(patch);
                        }
                    }

                    return applyMessagesPatchesAndPublish(accountId, patches);
                });
    }

    @Override
    public Completable pinUnPinConversation(int accountId, int peerId, boolean peen) {
        return networker.vkDefault(accountId)
                .messages()
                .pinUnPinConversation(peerId, peen);
    }

    @Override
    public Completable markAsImportant(int accountId, int peerId, @NonNull Collection<Integer> ids, Integer important) {
        return networker.vkDefault(accountId)
                .messages()
                .markAsImportant(ids, important)
                .flatMapCompletable(result -> {
                    List<MessagePatch> patches = new ArrayList<>(result.size());

                    for (Integer entry : result) {
                        boolean marked = important == 1;
                        MessagePatch patch = new MessagePatch(entry, peerId);
                        patch.setImportant(new MessagePatch.Important(marked));
                        patches.add(patch);
                    }

                    return applyMessagesPatchesAndPublish(accountId, patches);
                });
    }

    private Completable applyMessagesPatchesAndPublish(int accountId, List<MessagePatch> patches) {
        List<MessageUpdate> updates = new ArrayList<>(patches.size());
        Set<PeerId> requireInvalidate = new HashSet<>(0);

        for (MessagePatch patch : patches) {
            updates.add(patch2Update(accountId, patch));

            if (patch.getDeletion() != null) {
                requireInvalidate.add(new PeerId(accountId, patch.getPeerId()));
            }
        }

        Completable afterApply = Completable.complete();

        List<Completable> invalidatePeers = new LinkedList<>();
        for (PeerId pair : requireInvalidate) {
            invalidatePeers.add(invalidatePeerLastMessage(pair.accountId, pair.peerId));
        }

        if (!invalidatePeers.isEmpty()) {
            afterApply = Completable.merge(invalidatePeers);
        }

        return storages.messages()
                .applyPatches(accountId, patches)
                .andThen(afterApply)
                .doOnComplete(() -> messageUpdatesPublisher.onNext(updates));
    }

    private Completable invalidatePeerLastMessage(int accountId, int peerId) {
        return storages.messages()
                .findLastSentMessageIdForPeer(accountId, peerId)
                .flatMapCompletable(optionalId -> {
                    if (optionalId.isEmpty()) {
                        PeerDeleting deleting = new PeerDeleting(accountId, peerId);
                        return storages.dialogs().removePeerWithId(accountId, peerId)
                                .doOnComplete(() -> peerDeletingPublisher.onNext(deleting));
                    } else {
                        PeerPatch patch = new PeerPatch(peerId).withLastMessage(optionalId.get());
                        return applyPeerUpdatesAndPublish(accountId, Collections.singletonList(patch));
                    }
                });
    }

    @Override
    public Completable restoreMessage(int accountId, int peerId, int messageId) {
        return networker.vkDefault(accountId)
                .messages()
                .restore(messageId)
                .flatMapCompletable(ignored -> {
                    MessagePatch patch = new MessagePatch(messageId, peerId);
                    patch.setDeletion(new MessagePatch.Deletion(false, false));
                    return applyMessagesPatchesAndPublish(accountId, Collections.singletonList(patch));
                });
    }

    @Override
    public Completable editChat(int accountId, int chatId, String title) {
        PeerPatch patch = new PeerPatch(Peer.fromChatId(chatId)).withTitle(title);
        return networker.vkDefault(accountId)
                .messages()
                .editChat(chatId, title)
                .flatMapCompletable(ignored -> applyPeerUpdatesAndPublish(accountId, Collections.singletonList(patch)));
    }

    @Override
    public Single<Integer> createGroupChat(int accountId, Collection<Integer> users, String title) {
        return networker.vkDefault(accountId)
                .messages()
                .createChat(users, title);
    }

    @Override
    public Single<Integer> recogniseAudioMessage(int accountId, Integer message_id, String audio_message_id) {
        return networker.vkDefault(accountId)
                .messages()
                .recogniseAudioMessage(message_id, audio_message_id);
    }

    @Override
    public Completable setMemberRole(int accountId, int chat_id, int member_id, boolean isAdmin) {
        return networker.vkDefault(accountId)
                .messages()
                .setMemberRole(Peer.fromChatId(chat_id), member_id, isAdmin ? "admin" : "member")
                .ignoreElement();
    }

    @Override
    public Completable markAsRead(int accountId, int peerId, int toId) {
        PeerPatch patch = new PeerPatch(peerId).withInRead(toId).withUnreadCount(0);
        return networker.vkDefault(accountId)
                .messages()
                .markAsRead(peerId, toId)
                .flatMapCompletable(ignored -> applyPeerUpdatesAndPublish(accountId, Collections.singletonList(patch)));
    }

    @Override
    public Completable pin(int accountId, int peerId, @Nullable Message message) {
        PeerUpdate update = new PeerUpdate(accountId, peerId);
        update.setPin(new PeerUpdate.Pin(message));

        Completable apiCompletable;
        if (message == null) {
            apiCompletable = networker.vkDefault(accountId)
                    .messages()
                    .unpin(peerId);
        } else {
            apiCompletable = networker.vkDefault(accountId)
                    .messages()
                    .pin(peerId, message.getId());
        }

        PeerPatch patch = new PeerPatch(peerId)
                .withPin(message == null ? null : Model2Entity.buildMessageEntity(message));

        return apiCompletable
                .andThen(storages.dialogs().applyPatches(accountId, Collections.singletonList(patch)))
                .doOnComplete(() -> peerUpdatePublisher.onNext(Collections.singletonList(update)));
    }

    private Single<Integer> internalSend(int accountId, MessageEntity dbo) {
        if (isEmpty(dbo.getExtras()) && isEmpty(dbo.getAttachments()) && dbo.getForwardCount() == 0) {
            return networker.vkDefault(accountId)
                    .messages()
                    .send(dbo.getId(), dbo.getPeerId(), null, dbo.getBody(), null, null, null, null, null, dbo.getPayload(), null);
        }

        Collection<IAttachmentToken> attachments = new LinkedList<>();

        try {
            if (nonEmpty(dbo.getAttachments())) {
                for (Entity a : dbo.getAttachments()) {
                    if (a instanceof StickerEntity) {
                        int stickerId = ((StickerEntity) a).getId();

                        return checkForwardMessages(accountId, dbo)
                                .flatMap(optionalFwd -> {
                                    if (optionalFwd.getFirst()) {
                                        return networker.vkDefault(accountId)
                                                .messages()
                                                .send(dbo.getId(), dbo.getPeerId(), null, null, null, null, null, null, stickerId, null, optionalFwd.getSecond().get().get(0));
                                    }
                                    return networker.vkDefault(accountId)
                                            .messages()
                                            .send(dbo.getId(), dbo.getPeerId(), null, null, null, null, null, null, stickerId, null, null);
                                });
                    }

                    attachments.add(Entity2Dto.createToken(a));
                }
            }
        } catch (Exception e) {
            return Single.error(e);
        }

        return checkVoiceMessage(accountId, dbo)
                .flatMap(optionalToken -> {
                    if (optionalToken.nonEmpty()) {
                        attachments.add(optionalToken.get());
                    }

                    return checkForwardMessages(accountId, dbo)
                            .flatMap(optionalFwd -> {
                                if (optionalFwd.getFirst() && (!isEmpty(dbo.getBody()) || dbo.isHasAttachmens())) {
                                    return networker.vkDefault(accountId)
                                            .messages()
                                            .send(dbo.getId(), dbo.getPeerId(), null, dbo.getBody(), null, null, attachments, null, null, null, optionalFwd.getSecond().get().get(0));
                                }
                                return networker.vkDefault(accountId)
                                        .messages()
                                        .send(dbo.getId(), dbo.getPeerId(), null, dbo.getBody(), null, null, attachments, optionalFwd.getSecond().get(), null, null, null);
                            });
                });
    }

    private Single<Pair<Boolean, Optional<List<Integer>>>> checkForwardMessages(int accountId, MessageEntity dbo) {
        if (dbo.getForwardCount() == 0) {
            return Single.just(new Pair<>(false, Optional.empty()));
        }

        return storages.messages()
                .getForwardMessageIds(accountId, dbo.getId(), dbo.getPeerId())
                .map(v -> new Pair<>(v.getFirst(), Optional.wrap(v.getSecond())));
    }

    private Single<Optional<IAttachmentToken>> checkVoiceMessage(int accountId, MessageEntity dbo) {
        Map<Integer, String> extras = dbo.getExtras();

        if (nonNull(extras) && extras.containsKey(Message.Extra.VOICE_RECORD)) {
            String filePath = extras.get(Message.Extra.VOICE_RECORD);
            IDocsApi docsApi = networker.vkDefault(accountId).docs();

            return docsApi.getMessagesUploadServer(dbo.getPeerId(), "audio_message")
                    .flatMap(server -> {
                        assert filePath != null;
                        File file = new File(filePath);
                        InputStream[] is = new InputStream[1];

                        try {
                            is[0] = new FileInputStream(file);
                            return networker.uploads()
                                    .uploadDocumentRx(server.getUrl(), file.getName(), is[0], null)
                                    .doFinally(safelyCloseAction(is[0]))
                                    .flatMap(uploadDto -> docsApi
                                            .save(uploadDto.file, null, null)
                                            .map(dtos -> {
                                                if (dtos.type.isEmpty()) {
                                                    throw new NotFoundException("Unable to save voice message");
                                                }

                                                VkApiDoc dto = dtos.doc;
                                                IAttachmentToken token = AttachmentsTokenCreator.ofDocument(dto.id, dto.ownerId, dto.accessKey);
                                                return Optional.wrap(token);
                                            }));
                        } catch (FileNotFoundException e) {
                            safelyClose(is[0]);
                            return Single.error(e);
                        }
                    });
        }

        return Single.just(Optional.empty());
    }

    private Single<Optional<String>> getFinalMessagesBody(SaveMessageBuilder builder) {
        if (isEmpty(builder.getBody()) || !builder.isRequireEncryption()) {
            return Single.just(Optional.wrap(builder.getBody()));
        }

        @KeyLocationPolicy
        int policy = builder.getKeyLocationPolicy();

        return storages.keys(policy)
                .findLastKeyPair(builder.getAccountId(), builder.getPeerId())
                .map(key -> {
                    if (key.isEmpty()) {
                        throw new KeyPairDoesNotExistException();
                    }

                    AesKeyPair pair = key.get();

                    String encrypted = CryptHelper.encryptWithAes(builder.getBody(),
                            pair.getMyAesKey(),
                            builder.getBody(),
                            pair.getSessionId(),
                            builder.getKeyLocationPolicy()
                    );

                    return Optional.wrap(encrypted);
                });
    }

    private Single<Integer> getTargetMessageStatus(SaveMessageBuilder builder) {
        int accountId = builder.getAccountId();

        if (isNull(builder.getDraftMessageId())) {
            return Single.just(MessageStatus.QUEUE);
        }

        UploadDestination destination = UploadDestination.forMessage(builder.getDraftMessageId());
        return uploadManager.get(accountId, destination)
                .map(uploads -> {
                    if (uploads.isEmpty()) {
                        return MessageStatus.QUEUE;
                    }

                    boolean uploadingNow = false;

                    for (Upload o : uploads) {
                        if (o.getStatus() == Upload.STATUS_CANCELLING) {
                            continue;
                        }

                        if (o.getStatus() == Upload.STATUS_ERROR) {
                            throw new UploadNotResolvedException();
                        }

                        uploadingNow = true;
                    }

                    return uploadingNow ? MessageStatus.WAITING_FOR_UPLOAD : MessageStatus.QUEUE;
                });
    }

    private static final class InternalHandler extends WeakMainLooperHandler<MessagesRepository> {

        static final int SEND = 1;

        InternalHandler(MessagesRepository repository) {
            super(repository);
        }

        void runSend() {
            sendEmptyMessage(SEND);
        }

        @Override
        public void handleMessage(@NonNull MessagesRepository repository, @NonNull android.os.Message msg) {
            if (msg.what == SEND) {
                repository.send();
            }
        }
    }

    private static final class PeerId {

        final int accountId;
        final int peerId;

        PeerId(int accountId, int peerId) {
            this.accountId = accountId;
            this.peerId = peerId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PeerId peerId1 = (PeerId) o;
            if (accountId != peerId1.accountId) return false;
            return peerId == peerId1.peerId;
        }

        @Override
        public int hashCode() {
            int result = accountId;
            result = 31 * result + peerId;
            return result;
        }
    }
}
