package dev.ragnarok.fenrir.realtime;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.collectIds;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.removeIf;

import android.content.Context;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate;
import dev.ragnarok.fenrir.crypt.KeyExchangeService;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.longpoll.FullAndNonFullUpdates;
import dev.ragnarok.fenrir.longpoll.LongPollNotificationHelper;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.push.NotificationScheduler;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.PersistentLogger;
import dev.ragnarok.fenrir.util.VKOwnIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.subjects.PublishSubject;

class RealtimeMessagesProcessor implements IRealtimeMessagesProcessor {

    @SuppressWarnings("unused")
    private static final String TAG = "RealtimeMessagesProcessor";
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private final PublishSubject<TmpResult> publishSubject;
    private final IStorages repositories;
    private final INetworker networker;
    private final Object stateLock = new Object();
    private final List<Entry> queue;
    private final Context app;

    private final SparseArray<Pair<Integer, Integer>> notificationsInterceptors;
    private final IOwnersRepository ownersRepository;
    private final IMessagesRepository messagesInteractor;
    private volatile Entry current;

    RealtimeMessagesProcessor() {
        app = Injection.provideApplicationContext();
        repositories = Injection.provideStores();
        networker = Injection.provideNetworkInterfaces();
        publishSubject = PublishSubject.create();
        queue = new LinkedList<>();
        notificationsInterceptors = new SparseArray<>(3);
        ownersRepository = Repository.INSTANCE.getOwners();
        messagesInteractor = Repository.INSTANCE.getMessages();
    }

    private static Set<Integer> getChatIds(TmpResult result) {
        Set<Integer> peersIds = null;
        for (TmpResult.Msg msg : result.getData()) {
            VKApiMessage dto = msg.getDto();
            if (isNull(dto)) {
                continue;
            }

            if (Peer.isGroupChat(dto.peer_id)) {
                if (peersIds == null) {
                    peersIds = new HashSet<>(1);
                }

                peersIds.add(dto.peer_id);
            }
        }

        return peersIds;
    }

    private static VKOwnIds getOwnIds(TmpResult result) {
        VKOwnIds vkOwnIds = new VKOwnIds();
        for (TmpResult.Msg msg : result.getData()) {
            if (nonNull(msg.getDto())) {
                vkOwnIds.append(msg.getDto());
            }
        }

        return vkOwnIds;
    }

    private static Single<TmpResult> init(Single<Entry> single) {
        return single.map(entry -> {
            TmpResult result = new TmpResult(entry.getId(), entry.getAccountId(), entry.count());
            FullAndNonFullUpdates updates = entry.getUpdates();

            if (updates.hasFullMessages()) {
                for (AddMessageUpdate update : updates.getFullMessages()) {
                    result.add(update.getMessageId())
                            .setDto(Dto2Model.transform(entry.getAccountId(), update));
                }
            }

            if (updates.hasNonFullMessages()) {
                for (AddMessageUpdate update : updates.getNonFull()) {
                    result.add(update.getMessageId()).setBackup(Dto2Model.transform(entry.getAccountId(), update));
                }
            }
            return result;
        });
    }

    @Override
    public Observable<TmpResult> observeResults() {
        return publishSubject;
    }

    @Override
    public int process(int accountId, List<AddMessageUpdate> updates) {
        int id = ID_GENERATOR.incrementAndGet();
        Entry entry = new Entry(accountId, id, false);
        for (AddMessageUpdate update : updates) {
            entry.append(update);
        }

        addToQueue(entry);
        startIfNotStarted();
        return id;
    }

    private boolean hasInQueueOrCurrent(int id) {
        synchronized (stateLock) {
            Entry c = current;

            if (nonNull(c) && c.has(id)) {
                return true;
            }

            for (Entry q : queue) {
                if (q.has(id)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int process(int accountId, int messageId, boolean ignoreIfExists) throws QueueContainsException {
        if (hasInQueueOrCurrent(messageId)) {
            throw new QueueContainsException();
        }

        Logger.d(TAG, "Register entry, aid: " + accountId + ", mid: " + messageId + ", ignoreIfExists: " + ignoreIfExists);

        int id = ID_GENERATOR.incrementAndGet();

        Entry entry = new Entry(accountId, id, ignoreIfExists);
        entry.append(messageId);
        addToQueue(entry);

        startIfNotStarted();
        return id;
    }

    @Override
    public void registerNotificationsInterceptor(int interceptorId, Pair<Integer, Integer> aidPeerPair) {
        notificationsInterceptors.put(interceptorId, aidPeerPair);
    }

    @Override
    public void unregisterNotificationsInterceptor(int interceptorId) {
        notificationsInterceptors.remove(interceptorId);
    }

    @Override
    public boolean isNotificationIntercepted(int accountId, int peerId) {
        for (int i = 0; i < notificationsInterceptors.size(); i++) {
            int key = notificationsInterceptors.keyAt(i);
            Pair<Integer, Integer> pair = notificationsInterceptors.get(key);

            if (pair.getFirst() == accountId && pair.getSecond() == peerId) {
                return false;
            }
        }

        return true;
    }

    private void addToQueue(Entry entry) {
        synchronized (stateLock) {
            queue.add(entry);
        }
    }

    private boolean prepareForStartFirst() {
        synchronized (stateLock) {
            if (nonNull(current) || queue.isEmpty()) {
                return false;
            }

            current = queue.remove(0);
            return true;
        }
    }

    /*private Completable refreshChangedDialogs(TmpResult result) {
        Set<Integer> peers = new HashSet<>();

        for (TmpResult.Msg msg : result.getData()) {
            VKApiMessage dto = msg.getDto();
            if (nonNull(dto)) {
                peers.add(dto.peer_id);
            }
        }

        Completable completable = Completable.complete();
        for (int peerId : peers) {
            completable = completable.andThen(messagesInteractor.fixDialogs(result.getAccountId(), peerId));
        }

        return completable;
    }*/

    private void resetCurrent() {
        synchronized (stateLock) {
            current = null;
        }
    }

    private void startIfNotStarted() {
        if (!prepareForStartFirst()) {
            return;
        }

        Entry entry;
        synchronized (stateLock) {
            entry = current;
        }

        long start = System.currentTimeMillis();
        boolean ignoreIfExists = entry.isIgnoreIfExists();

        init(Single.just(entry))
                // ищем недостающие сообщения в локальной базе
                .flatMap(result -> repositories
                        .messages()
                        .getMissingMessages(result.getAccountId(), result.getAllIds())
                        .map(result::setMissingIds))
                .flatMap(result -> {
                    // отсеиваем сообщения, которые уже есть в локальной базе (если требуется)
                    if (ignoreIfExists) {
                        removeIf(result.getData(), TmpResult.Msg::isAlreadyExists);
                    }

                    if (result.getData().isEmpty()) {
                        return Single.just(result);
                    }

                    // получаем необходимую информацию о сообщениях и сохраняем в локальную базу
                    return Single.just(result)
                            .compose(getAndStore());
                })
                .compose(NotificationScheduler.fromNotificationThreadToMain())
                .subscribe(result -> onResultReceived(start, result), this::onProcessError);
    }

    private SingleTransformer<TmpResult, TmpResult> getAndStore() {
        return single -> single
                .flatMap(result -> {
                    // если в исходных данных недостаточно инфы - получаем нужные данные с api
                    List<Integer> needGetFromNet = collectIds(result.getData(), msg -> isNull(msg.getDto()));
                    if (needGetFromNet.isEmpty()) {
                        return Single.just(result);
                    }

                    return networker.vkDefault(result.getAccountId())
                            .messages()
                            .getById(needGetFromNet)
                            .map(result::appendDtos);
                })
                .map(result -> {
                    // отсеиваем сообщения, которые имеют отношение к обмену ключами
                    removeIf(result.getData(), msg -> KeyExchangeService.intercept(app, result.getAccountId(), msg.getDto()));
                    return result;
                })
                .flatMap(result -> {
                    if (result.getData().isEmpty()) {
                        return Single.just(result);
                    }

                    // идентифицируем доолнительные необходимые данные, которых не хватает в локальной базе
                    // например, информация о пользователях, группах или чатах
                    // получаем и сохраняем, если необходимо
                    return identifyMissingObjectsGetAndStore(result)
                            .andThen(Single.just(result))
                            // сохраняем сообщения в локальную базу и получаем оттуда "тяжелые" обьекты сообщений
                            .compose(storeToCacheAndReturn());
                });
    }

    private SingleTransformer<TmpResult, TmpResult> storeToCacheAndReturn() {
        return single -> single
                // собственно, вставка
                .flatMap(result -> messagesInteractor
                        .insertMessages(result.getAccountId(), result.collectDtos())
                        //.andThen(refreshChangedDialogs(result))
                        .andThen(Single.just(result)))
                .flatMap(result -> {
                    // собственно, получение из локальной базы
                    List<Integer> ids = collectIds(result.getData(), msg -> true);

                    return messagesInteractor
                            .findCachedMessages(result.getAccountId(), ids)
                            .map(result::appendModel);
                });
    }

    private void onResultReceived(long startTime, TmpResult result) {
        long lastEnryProcessTime = System.currentTimeMillis() - startTime;

        Logger.d(TAG, "SUCCESS, data: " + result + ", time: " + lastEnryProcessTime);

        sendNotifications(result);

        publishSubject.onNext(result);

        resetCurrent();
        startIfNotStarted();
    }

    private void sendNotifications(TmpResult result) {
        for (TmpResult.Msg msg : result.getData()) {
            if (msg.isAlreadyExists()) {
                continue;
            }

            Message message = msg.getMessage();

            if (isNull(message) || !isNotificationIntercepted(result.getAccountId(), message.getPeerId())) {
                continue;
            }

            LongPollNotificationHelper.notifyAbountNewMessage(app, message);
        }
    }

    private void onProcessError(Throwable throwable) {
        throwable.printStackTrace();

        PersistentLogger.logThrowable(RealtimeMessagesProcessor.class.getSimpleName(), throwable);

        resetCurrent();
        startIfNotStarted();
    }

    private Completable identifyMissingObjectsGetAndStore(TmpResult result) {
        VKOwnIds ownIds = getOwnIds(result);
        Collection<Integer> chatIds = getChatIds(result);
        int accountId = result.getAccountId();

        Completable completable = Completable.complete();
        if (ownIds.nonEmpty()) {
            // проверяем на недостающих пользователей и групп
            completable = completable.andThen(findMissingOwnersGetAndStore(accountId, ownIds));
        }

        if (nonEmpty(chatIds)) {
            // проверяем на отсутствие чатов
            completable = completable.andThen(findMissingChatsGetAndStore(accountId, chatIds));
        }

        return completable;
    }

    private Completable findMissingChatsGetAndStore(int accountId, Collection<Integer> ids) {
        return repositories.dialogs()
                .getMissingGroupChats(accountId, ids)
                .flatMapCompletable(integers -> {
                    if (integers.isEmpty()) {
                        return Completable.complete();
                    }

                    return networker.vkDefault(accountId)
                            .messages()
                            .getChat(null, integers, null, null)
                            .flatMapCompletable(chats -> repositories.dialogs()
                                    .insertChats(accountId, chats));
                });
    }

    private Completable findMissingOwnersGetAndStore(int accountId, VKOwnIds ids) {
        return findMissingOwnerIds(accountId, ids)
                .flatMapCompletable(integers -> {
                    if (integers.isEmpty()) {
                        return Completable.complete();
                    }

                    return ownersRepository.cacheActualOwnersData(accountId, integers);
                });
    }

    private Single<List<Integer>> findMissingOwnerIds(int accountId, VKOwnIds ids) {
        return repositories.owners()
                .getMissingUserIds(accountId, ids.getUids())
                .zipWith(repositories.owners().getMissingCommunityIds(accountId, ids.getGids()), (integers, integers2) -> {
                    if (integers.isEmpty() && integers2.isEmpty()) {
                        return Collections.emptyList();
                    }

                    List<Integer> result = new ArrayList<>(integers.size() + integers2.size());
                    result.addAll(integers);
                    result.addAll(integers2);
                    return result;
                });
    }
}
