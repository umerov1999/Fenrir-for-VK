package dev.ragnarok.fenrir.domain.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.Includes.provideMainThreadScheduler
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.VkRetrofitProvider.Companion.vkgson
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.local_json.ChatJsonResponse
import dev.ragnarok.fenrir.api.model.longpoll.*
import dev.ragnarok.fenrir.api.model.response.*
import dev.ragnarok.fenrir.api.model.server.VkApiDocsUploadServer
import dev.ragnarok.fenrir.api.model.upload.UploadDocDto
import dev.ragnarok.fenrir.crypt.CryptHelper.encryptWithAes
import dev.ragnarok.fenrir.crypt.KeyLocationPolicy
import dev.ragnarok.fenrir.crypt.KeyPairDoesNotExistException
import dev.ragnarok.fenrir.db.PeerStateEntity
import dev.ragnarok.fenrir.db.interfaces.IStorages
import dev.ragnarok.fenrir.db.model.MessageEditEntity
import dev.ragnarok.fenrir.db.model.MessagePatch
import dev.ragnarok.fenrir.db.model.MessagePatch.Important
import dev.ragnarok.fenrir.db.model.PeerPatch
import dev.ragnarok.fenrir.db.model.entity.DialogEntity
import dev.ragnarok.fenrir.db.model.entity.MessageEntity
import dev.ragnarok.fenrir.db.model.entity.SimpleDialogEntity
import dev.ragnarok.fenrir.db.model.entity.StickerEntity
import dev.ragnarok.fenrir.domain.IMessagesDecryptor
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.IOwnersRepository
import dev.ragnarok.fenrir.domain.InteractorFactory.createAccountInteractor
import dev.ragnarok.fenrir.domain.Mode
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapConversation
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapDialog
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapMessage
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapOwners
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transform
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transformMessages
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transformOwners
import dev.ragnarok.fenrir.domain.mappers.Entity2Dto.createToken
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.buildDialogFromDbo
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.buildKeyboardFromDbo
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.fillOwnerIds
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.message
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll
import dev.ragnarok.fenrir.domain.mappers.Model2Dto.createTokens
import dev.ragnarok.fenrir.domain.mappers.Model2Entity.buildDboAttachments
import dev.ragnarok.fenrir.domain.mappers.Model2Entity.buildDialog
import dev.ragnarok.fenrir.domain.mappers.Model2Entity.buildKeyboardEntity
import dev.ragnarok.fenrir.domain.mappers.Model2Entity.buildMessageEntity
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.exception.UploadNotResolvedException
import dev.ragnarok.fenrir.longpoll.NotificationHelper.tryCancelNotificationForPeer
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.model.MessageUpdate.ImportantUpdate
import dev.ragnarok.fenrir.model.MessageUpdate.StatusUpdate
import dev.ragnarok.fenrir.model.criteria.DialogsCriteria
import dev.ragnarok.fenrir.model.criteria.MessagesCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.push.OwnerInfo
import dev.ragnarok.fenrir.push.OwnerInfo.Companion.getRx
import dev.ragnarok.fenrir.settings.ISettings.IAccountsSettings
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.upload.IUploadManager
import dev.ragnarok.fenrir.upload.Method
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.upload.UploadDestination.Companion.forMessage
import dev.ragnarok.fenrir.upload.UploadResult
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.empty
import dev.ragnarok.fenrir.util.Optional.Companion.wrap
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.ignore
import dev.ragnarok.fenrir.util.RxUtils.safelyCloseAction
import dev.ragnarok.fenrir.util.Unixtime.now
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.Utils.hasFlag
import dev.ragnarok.fenrir.util.Utils.isHiddenAccount
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNullMutable
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import dev.ragnarok.fenrir.util.Utils.safelyClose
import dev.ragnarok.fenrir.util.VKOwnIds
import dev.ragnarok.fenrir.util.WeakMainLooperHandler
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.Executors

class MessagesRepository(
    private val accountsSettings: IAccountsSettings,
    private val networker: INetworker,
    private val ownersRepository: IOwnersRepository,
    private val storages: IStorages,
    uploadManager: IUploadManager
) : IMessagesRepository {
    private val decryptor: IMessagesDecryptor
    private val uploadManager: IUploadManager
    private val peerUpdatePublisher = PublishProcessor.create<List<PeerUpdate>>()
    private val peerDeletingPublisher = PublishProcessor.create<PeerDeleting>()
    private val messageUpdatesPublisher = PublishProcessor.create<List<MessageUpdate>>()
    private val writeTextPublisher = PublishProcessor.create<List<WriteText>>()
    private val sentMessagesPublisher = PublishProcessor.create<SentMsg>()
    private val sendErrorsPublisher = PublishProcessor.create<Throwable>()
    private val compositeDisposable = CompositeDisposable()
    private val senderScheduler = Schedulers.from(Executors.newFixedThreadPool(1))
    private val handler = InternalHandler(this)
    private var nowSending = false
    private var registeredAccounts: List<Int>? = null
    override fun observeMessagesSendErrors(): Flowable<Throwable> {
        return sendErrorsPublisher.onBackpressureBuffer()
    }

    override fun observeTextWrite(): Flowable<List<WriteText>> {
        return writeTextPublisher.onBackpressureBuffer()
    }

    private fun onAccountsChanged() {
        registeredAccounts = accountsSettings.registered
    }

    override fun runSendingQueue() {
        handler.runSend()
    }

    /**
     * Отправить первое неотправленное сообщение
     */
    @MainThread
    private fun send() {
        if (nowSending) {
            return
        }
        nowSending = true
        sendMessage(registeredAccounts())
    }

    private fun registeredAccounts(): List<Int>? {
        if (registeredAccounts == null) {
            registeredAccounts = accountsSettings.registered
        }
        return registeredAccounts
    }

    private fun onMessageSent(msg: SentMsg) {
        nowSending = false
        sentMessagesPublisher.onNext(msg)
        send()
    }

    private fun onMessageSendError(t: Throwable) {
        val cause = getCauseIfRuntime(t)
        nowSending = false
        if (cause is NotFoundException) {
            val accountId = Settings.get().accounts().current
            if (!Settings.get().other().isBe_online || isHiddenAccount(accountId)) {
                compositeDisposable.add(
                    createAccountInteractor().setOffline(accountId)
                        .subscribeOn(senderScheduler)
                        .observeOn(provideMainThreadScheduler())
                        .subscribe(ignore(), ignore())
                )
            }
            // no unsent messages
            return
        }
        sendErrorsPublisher.onNext(t)
    }

    private fun sendMessage(accountIds: Collection<Int>?) {
        nowSending = true
        compositeDisposable.add(sendUnsentMessage(accountIds ?: return)
            .subscribeOn(senderScheduler)
            .observeOn(provideMainThreadScheduler())
            .subscribe({ msg: SentMsg -> onMessageSent(msg) }) { t: Throwable ->
                onMessageSendError(
                    t
                )
            })
    }

    private fun onUpdloadSuccess(upload: Upload) {
        val accountId = upload.accountId
        val messagesId = upload.destination.id
        compositeDisposable.add(uploadManager[accountId, upload.destination]
            .flatMap { uploads: List<Upload?> ->
                if (uploads.isNotEmpty()) {
                    return@flatMap Single.just(false)
                }
                storages.messages().getMessageStatus(accountId, messagesId)
                    .flatMap { status: Int ->
                        if (status != MessageStatus.WAITING_FOR_UPLOAD) {
                            Single.just(false)
                        } else {
                            changeMessageStatus(
                                accountId,
                                messagesId,
                                MessageStatus.QUEUE,
                                null
                            ).andThen(Single.just(true))
                        }
                    }
            }
            .subscribe({ needStartSendingQueue: Boolean ->
                if (needStartSendingQueue) {
                    runSendingQueue()
                }
            }, ignore())
        )
    }

    override fun handleFlagsUpdates(
        accountId: Int,
        setUpdates: List<MessageFlagsSetUpdate>?,
        resetUpdates: List<MessageFlagsResetUpdate>?
    ): Completable {
        val patches: MutableList<MessagePatch> = ArrayList()
        if (setUpdates.nonNullNoEmpty()) {
            for (update in setUpdates) {
                if (!hasFlag(update.mask, VKApiMessage.FLAG_DELETED)
                    && !hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)
                    && !hasFlag(update.mask, VKApiMessage.FLAG_DELETED_FOR_ALL)
                ) continue
                val patch = MessagePatch(update.message_id, update.peer_id)
                if (hasFlag(update.mask, VKApiMessage.FLAG_DELETED)) {
                    val forAll = hasFlag(update.mask, VKApiMessage.FLAG_DELETED_FOR_ALL)
                    patch.deletion = MessagePatch.Deletion(true, forAll)
                }
                if (hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)) {
                    patch.important = Important(true)
                }
                patches.add(patch)
            }
        }
        if (resetUpdates.nonNullNoEmpty()) {
            for (update in resetUpdates) {
                if (!hasFlag(update.mask, VKApiMessage.FLAG_DELETED) && !hasFlag(
                        update.mask,
                        VKApiMessage.FLAG_IMPORTANT
                    )
                ) continue
                val patch = MessagePatch(update.message_id, update.peer_id)
                if (hasFlag(update.mask, VKApiMessage.FLAG_DELETED)) {
                    patch.deletion = MessagePatch.Deletion(deleted = false, deletedForAll = false)
                }
                if (hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)) {
                    patch.important = Important(false)
                }
                patches.add(patch)
            }
        }
        return applyMessagesPatchesAndPublish(accountId, patches)
    }

    override fun handleWriteUpdates(
        accountId: Int,
        updates: List<WriteTextInDialogUpdate>
    ): Completable {
        return Completable.fromAction {
            val list: MutableList<WriteText> = ArrayList()
            for (update in updates) {
                list.add(WriteText(accountId, update.peer_id, update.from_ids, update.is_text))
            }
            writeTextPublisher.onNext(list)
        }
    }

    override fun updateDialogKeyboard(
        accountId: Int,
        peerId: Int,
        keyboard: Keyboard?
    ): Completable {
        return storages.dialogs()
            .updateDialogKeyboard(accountId, peerId, buildKeyboardEntity(keyboard))
    }

    override fun handleUnreadBadgeUpdates(
        accountId: Int,
        updates: List<BadgeCountChangeUpdate>?
    ): Completable {
        return Completable.fromAction {
            if (updates.nonNullNoEmpty()) {
                for (update in updates) {
                    storages.dialogs().setUnreadDialogsCount(accountId, update.count)
                }
            }
        }
    }

    @StringRes
    private fun GetTypeUser(ownr: OwnerInfo): Int {
        return if (ownr.owner.ownerType == OwnerType.USER) {
            when (ownr.user.sex) {
                Sex.MAN -> R.string.user_readed_yor_message_man
                Sex.WOMAN -> R.string.user_readed_yor_message_woman
                Sex.UNKNOWN -> R.string.user_readed_yor_message
                else -> R.string.user_readed_yor_message
            }
        } else R.string.user_readed_yor_message
    }

    override fun handleReadUpdates(
        accountId: Int,
        setUpdates: List<OutputMessagesSetReadUpdate>?,
        resetUpdates: List<InputMessagesSetReadUpdate>?
    ): Completable {
        val patches: MutableList<PeerPatch> = ArrayList()
        if (setUpdates.nonNullNoEmpty()) {
            for (update in setUpdates) {
                if (!Settings.get().other().isDisable_notifications && Settings.get()
                        .other().isInfo_reading && update.peer_id < VKApiMessage.CHAT_PEER
                ) {
                    compositeDisposable.add(
                        getRx(
                            provideApplicationContext(),
                            Settings.get().accounts().current,
                            update.peer_id
                        )
                            .compose(applySingleIOToMainSchedulers())
                            .subscribe({ userInfo: OwnerInfo ->
                                CreateCustomToast(
                                    provideApplicationContext()
                                ).setBitmap(userInfo.avatar).showToastInfo(
                                    userInfo.owner.fullName + " " + provideApplicationContext().getString(
                                        GetTypeUser(userInfo)
                                    )
                                )
                            }) { })
                }
                patches.add(PeerPatch(update.peer_id).withOutRead(update.local_id))
            }
        }
        if (resetUpdates.nonNullNoEmpty()) {
            for (update in resetUpdates) {
                patches.add(
                    PeerPatch(update.peer_id).withInRead(update.local_id)
                        .withUnreadCount(update.unread_count)
                )
                tryCancelNotificationForPeer(provideApplicationContext(), accountId, update.peerId)
            }
        }
        return applyPeerUpdatesAndPublish(accountId, patches)
    }

    override fun observeSentMessages(): Flowable<SentMsg> {
        return sentMessagesPublisher.onBackpressureBuffer()
    }

    override fun observeMessageUpdates(): Flowable<List<MessageUpdate>> {
        return messageUpdatesPublisher.onBackpressureBuffer()
    }

    override fun observePeerUpdates(): Flowable<List<PeerUpdate>> {
        return peerUpdatePublisher.onBackpressureBuffer()
    }

    override fun observePeerDeleting(): Flowable<PeerDeleting> {
        return peerDeletingPublisher.onBackpressureBuffer()
    }

    override fun getConversationSingle(
        accountId: Int,
        peerId: Int,
        mode: Mode
    ): Single<Conversation> {
        val cached = getCachedConversation(accountId, peerId)
        val actual = getActualConversaction(accountId, peerId)
        when (mode) {
            Mode.ANY -> return cached.flatMap { optional: Optional<Conversation> ->
                if (optional.isEmpty) actual else Single.just(
                    optional.requareNonEmpty()
                )
            }
            Mode.NET -> return actual
            Mode.CACHE -> return cached
                .flatMap { optional: Optional<Conversation> ->
                    if (optional.isEmpty) Single.error(
                        NotFoundException()
                    ) else Single.just(optional.requareNonEmpty())
                }
            else -> {}
        }
        throw IllegalArgumentException("Unsupported mode: $mode")
    }

    private fun getCachedConversation(accountId: Int, peerId: Int): Single<Optional<Conversation>> {
        return storages.dialogs()
            .findSimple(accountId, peerId)
            .flatMap { optional: Optional<SimpleDialogEntity> ->
                if (optional.isEmpty) {
                    return@flatMap Single.just(empty<Conversation>())
                } else {
                    Single.just(optional.requareNonEmpty())
                        .compose(simpleEntity2Conversation(accountId, emptyList()))
                        .map { wrap(it) }
                }
            }
    }

    private fun getActualConversaction(accountId: Int, peerId: Int): Single<Conversation> {
        return networker.vkDefault(accountId)
            .messages()
            .getConversations(listOf(peerId), true, Constants.MAIN_OWNER_FIELDS)
            .flatMap { response: ItemsProfilesGroupsResponse<VkApiConversation> ->
                if (response.items.isNullOrEmpty()) {
                    return@flatMap Single.error<Conversation>(NotFoundException())
                }
                val dto = response.items[0]
                val entity = mapConversation(dto)
                val existsOwners = transformOwners(response.profiles, response.groups)
                val ownerEntities = mapOwners(response.profiles, response.groups)
                ownersRepository.insertOwners(accountId, ownerEntities)
                    .andThen(storages.dialogs().saveSimple(accountId, entity))
                    .andThen(Single.just(entity))
                    .compose(simpleEntity2Conversation(accountId, existsOwners))
            }
    }

    override fun getConversation(accountId: Int, peerId: Int, mode: Mode): Flowable<Conversation> {
        val cached = getCachedConversation(accountId, peerId)
        val actual = getActualConversaction(accountId, peerId)
        return when (mode) {
            Mode.ANY -> cached
                .flatMap { optional: Optional<Conversation> ->
                    if (optional.isEmpty) actual else Single.just(
                        optional.requareNonEmpty()
                    )
                }
                .toFlowable()
            Mode.NET -> actual.toFlowable()
            Mode.CACHE -> cached
                .flatMap { optional: Optional<Conversation> ->
                    if (optional.isEmpty) Single.error(
                        NotFoundException()
                    ) else Single.just(optional.requareNonEmpty())
                }
                .toFlowable()
            Mode.CACHE_THEN_ACTUAL -> {
                val cachedFlowable = cached.toFlowable()
                    .filter { obj: Optional<Conversation> -> obj.nonEmpty() }
                    .map { obj: Optional<Conversation> -> obj.requareNonEmpty() }
                Flowable.concat(cachedFlowable, actual.toFlowable())
            }
        }
    }

    private fun simpleEntity2Conversation(
        accountId: Int,
        existingOwners: Collection<Owner>
    ): SingleTransformer<SimpleDialogEntity, Conversation> {
        return SingleTransformer { single: Single<SimpleDialogEntity> ->
            single
                .flatMap { entity: SimpleDialogEntity ->
                    val owners = VKOwnIds()
                    if (Peer.isGroup(entity.peerId) || Peer.isUser(
                            entity.peerId
                        )
                    ) {
                        owners.append(entity.peerId)
                    }
                    if (entity.pinned != null) {
                        fillOwnerIds(owners, listOf(entity.pinned))
                    }
                    ownersRepository.findBaseOwnersDataAsBundle(
                        accountId,
                        owners.all,
                        IOwnersRepository.MODE_ANY,
                        existingOwners
                    )
                        .map { bundle: IOwnersBundle -> entity2Model(accountId, entity, bundle) }
                }
        }
    }

    override fun edit(
        accountId: Int,
        message: Message,
        body: String?,
        attachments: List<AbsModel>,
        keepForwardMessages: Boolean
    ): Single<Message> {
        val attachmentTokens = createTokens(attachments)
        return networker.vkDefault(accountId)
            .messages()
            .edit(message.peerId, message.id, body, attachmentTokens, keepForwardMessages, null)
            .andThen(getById(accountId, message.id))
    }

    override fun getCachedPeerMessages(
        accountId: Int,
        peerId: Int
    ): Single<List<Message>> {
        val criteria = MessagesCriteria(accountId, peerId)
        return storages.messages()
            .getByCriteria(criteria, withAtatchments = true, withForwardMessages = true)
            .compose(entities2Models(accountId))
            .compose(decryptor.withMessagesDecryption(accountId))
    }

    override fun getMessagesFromLocalJSon(
        accountId: Int,
        context: Context
    ): Single<Pair<Peer, List<Message>>> {
        val gson = vkgson
        return try {
            val b = InputStreamReader(
                (context as Activity).intent.data?.let {
                    context.contentResolver.openInputStream(
                        it
                    )
                }, StandardCharsets.UTF_8
            )
            val resp = gson.fromJson(b, ChatJsonResponse::class.java)
            b.close()
            if (resp == null || resp.page_title.isNullOrEmpty()) {
                return Single.error(Throwable("parsing error"))
            }
            val ids = VKOwnIds().append(resp.messages)
            ownersRepository.findBaseOwnersDataAsBundle(
                accountId,
                ids.all,
                IOwnersRepository.MODE_ANY,
                emptyList()
            )
                .map {
                    Pair(
                        Peer(resp.page_id).setAvaUrl(resp.page_avatar).setTitle(resp.page_title),
                        transformMessages(resp.page_id, resp.messages, it)
                    )
                }
        } catch (e: Throwable) {
            e.printStackTrace()
            Single.error(e)
        }
    }

    override fun getCachedDialogs(accountId: Int): Single<List<Dialog>> {
        val criteria = DialogsCriteria(accountId)
        return storages.dialogs()
            .getDialogs(criteria)
            .flatMap { dbos: List<DialogEntity> ->
                val ownIds = VKOwnIds()
                for (dbo in dbos) {
                    when (Peer.getType(dbo.peerId)) {
                        Peer.GROUP, Peer.USER -> ownIds.append(dbo.peerId)
                        Peer.CHAT -> ownIds.append(dbo.message.fromId)
                    }
                }
                ownersRepository
                    .findBaseOwnersDataAsBundle(accountId, ownIds.all, IOwnersRepository.MODE_ANY)
                    .flatMap { owners: IOwnersBundle ->
                        val messages: MutableList<Message> = ArrayList(0)
                        val dialogs: MutableList<Dialog> = ArrayList(dbos.size)
                        for (dbo in dbos) {
                            val dialog = buildDialogFromDbo(accountId, dbo, owners)
                            dialogs.add(dialog)
                            if (dbo.message.isEncrypted) {
                                messages.add(dialog.message)
                            }
                        }
                        if (messages.nonNullNoEmpty()) {
                            Single.just<List<Message>>(
                                messages
                            )
                                .compose(decryptor.withMessagesDecryption(accountId))
                                .map<List<Dialog>> { dialogs }
                        } else {
                            Single.just<List<Dialog>>(dialogs)
                        }
                    }
            }
    }

    private fun getById(accountId: Int, messageId: Int): Single<Message> {
        return networker.vkDefault(accountId)
            .messages()
            .getById(listOf(messageId))
            .map { dtos: List<VKApiMessage>? ->
                mapAll(dtos) {
                    mapMessage(
                        it
                    )
                }
            }
            .compose(entities2Models(accountId))
            .flatMap { messages: List<Message> ->
                if (messages.isEmpty()) {
                    return@flatMap Single.error<Message>(NotFoundException())
                }
                Single.just(messages[0])
            }
    }

    private fun entities2Models(accountId: Int): SingleTransformer<List<MessageEntity>, List<Message>> {
        return SingleTransformer { single: Single<List<MessageEntity>> ->
            single
                .flatMap { dbos: List<MessageEntity?> ->
                    val ownIds = VKOwnIds()
                    fillOwnerIds(ownIds, dbos)
                    ownersRepository
                        .findBaseOwnersDataAsBundle(
                            accountId,
                            ownIds.all,
                            IOwnersRepository.MODE_ANY
                        )
                        .map<List<Message>> { owners: IOwnersBundle? ->
                            val messages: MutableList<Message> =
                                ArrayList(dbos.size)
                            for (dbo in dbos) {
                                messages.add(message(accountId, dbo!!, owners!!))
                            }
                            messages
                        }
                }
        }
    }

    private fun insertPeerMessages(
        accountId: Int,
        peerId: Int,
        messages: List<VKApiMessage>,
        clearBefore: Boolean
    ): Completable {
        return Single.just(messages)
            .compose(DTO_TO_DBO)
            .flatMapCompletable { dbos: List<MessageEntity> ->
                storages.messages().insertPeerDbos(accountId, peerId, dbos, clearBefore)
            }
    }

    override fun insertMessages(accountId: Int, messages: List<VKApiMessage>): Completable {
        return Single.just(messages)
            .compose(DTO_TO_DBO)
            .flatMap { dbos: List<MessageEntity> -> storages.messages().insert(accountId, dbos) }
            .flatMapCompletable {
                val peers: MutableSet<Int> = HashSet()
                for (m in messages) {
                    peers.add(m.peer_id)
                }
                storages.dialogs()
                    .findPeerStates(accountId, peers)
                    .flatMapCompletable { peerStates: List<PeerStateEntity> ->
                        val patches: MutableList<PeerPatch> = ArrayList(peerStates.size)
                        for (state in peerStates) {
                            var unread = state.unreadCount
                            var messageId = state.lastMessageId
                            for (m in messages) {
                                if (m.peer_id != state.peerId) continue
                                if (m.out) {
                                    unread = 0
                                } else {
                                    unread++
                                }
                                if (m.id > messageId) {
                                    messageId = m.id
                                }
                            }
                            patches.add(
                                PeerPatch(state.peerId)
                                    .withUnreadCount(unread)
                                    .withLastMessage(messageId)
                            )
                        }
                        applyPeerUpdatesAndPublish(accountId, patches)
                    }
            }
    }

    private fun applyPeerUpdatesAndPublish(accountId: Int, patches: List<PeerPatch>): Completable {
        val updates: MutableList<PeerUpdate> = ArrayList()
        for (p in patches) {
            val update = PeerUpdate(accountId, p.id)
            if (p.inRead != null) {
                update.readIn = PeerUpdate.Read(p.inRead.id)
            }
            if (p.outRead != null) {
                update.readOut = PeerUpdate.Read(p.outRead.id)
            }
            if (p.lastMessage != null) {
                update.lastMessage = PeerUpdate.LastMessage(p.lastMessage.id)
            }
            if (p.unread != null) {
                update.unread = PeerUpdate.Unread(p.unread.count)
            }
            if (p.title != null) {
                update.title = PeerUpdate.Title(p.title.title)
            }
            updates.add(update)
        }
        return storages.dialogs().applyPatches(accountId, patches)
            .doOnComplete { peerUpdatePublisher.onNext(updates) }
    }

    override fun getImportantMessages(
        accountId: Int, count: Int, offset: Int?,
        startMessageId: Int?
    ): Single<List<Message>> {
        return networker.vkDefault(accountId)
            .messages()
            .getImportantMessages(offset, count, startMessageId, true, Constants.MAIN_OWNER_FIELDS)
            .flatMap { response: MessageImportantResponse ->
                val dtos: MutableList<VKApiMessage> =
                    if (response.messages == null) mutableListOf() else listEmptyIfNullMutable(
                        response.messages.items
                    )
                if (startMessageId != null && dtos.nonNullNoEmpty() && startMessageId == dtos[0].id) {
                    dtos.removeAt(0)
                }
                val completable = Completable.complete()
                val ownerIds = VKOwnIds()
                ownerIds.append(dtos)
                val existsOwners = transformOwners(response.profiles, response.groups)
                val ownerEntities = mapOwners(response.profiles, response.groups)
                completable
                    .andThen(
                        ownersRepository
                            .findBaseOwnersDataAsBundle(
                                accountId,
                                ownerIds.all,
                                IOwnersRepository.MODE_ANY,
                                existsOwners
                            )
                            .flatMap {
                                val insertCompletable =
                                    ownersRepository.insertOwners(accountId, ownerEntities)
                                val messages: MutableList<Message> =
                                    ArrayList(dtos.size)
                                for (dto in dtos) {
                                    messages.add(transform(accountId, dto, it))
                                }
                                insertCompletable.andThen(
                                    Single.just<List<Message>>(messages)
                                        .compose(decryptor.withMessagesDecryption(accountId))
                                )
                            })
            }
    }

    override fun getJsonHistory(
        accountId: Int,
        offset: Int?,
        count: Int?,
        peerId: Int
    ): Single<List<String>> {
        return networker.vkDefault(accountId)
            .messages()
            .getJsonHistory(offset, count, peerId)
            .flatMap { response: Items<VkApiJsonString> ->
                val dtos = listEmptyIfNull<VkApiJsonString>(
                    response.items
                )
                val messages: MutableList<String> = ArrayList(dtos.size)
                for (i in dtos) {
                    if (i.json_data.nonNullNoEmpty()) {
                        messages.add(i.json_data)
                    }
                }
                Single.just<List<String>>(messages)
            }
    }

    override fun getPeerMessages(
        accountId: Int, peerId: Int, count: Int, offset: Int?,
        startMessageId: Int?, cacheData: Boolean, rev: Boolean
    ): Single<List<Message>> {
        var pCount = count
        if (rev) pCount = 200
        return networker.vkDefault(accountId)
            .messages()
            .getHistory(
                offset,
                pCount,
                peerId,
                startMessageId,
                rev,
                true,
                Constants.MAIN_OWNER_FIELDS
            )
            .flatMap { response: MessageHistoryResponse ->
                val dtos: MutableList<VKApiMessage> = listEmptyIfNullMutable(response.messages)
                var patch: PeerPatch? = null
                if (startMessageId == null && cacheData && response.conversations.nonNullNoEmpty()) {
                    val conversation = response.conversations[0]
                    patch = PeerPatch(peerId)
                        .withOutRead(conversation.outRead)
                        .withInRead(conversation.inRead)
                        .withLastMessage(conversation.lastMessageId)
                        .withUnreadCount(conversation.unreadCount)
                }
                if (startMessageId != null && dtos.nonNullNoEmpty() && startMessageId == dtos[0].id) {
                    dtos.removeAt(0)
                }
                var completable: Completable
                if (cacheData) {
                    completable =
                        insertPeerMessages(accountId, peerId, dtos, startMessageId == null)
                    if (patch != null) {
                        completable = completable.andThen(
                            applyPeerUpdatesAndPublish(
                                accountId,
                                listOf(patch)
                            )
                        )
                    }
                } else {
                    completable = Completable.complete()
                }
                val ownerIds = VKOwnIds()
                ownerIds.append(dtos)
                val existsOwners = transformOwners(response.profiles, response.groups)
                val ownerEntities = mapOwners(response.profiles, response.groups)
                completable
                    .andThen(
                        ownersRepository
                            .findBaseOwnersDataAsBundle(
                                accountId,
                                ownerIds.all,
                                IOwnersRepository.MODE_ANY,
                                existsOwners
                            )
                            .flatMap {
                                val insertCompletable =
                                    ownersRepository.insertOwners(accountId, ownerEntities)
                                if (startMessageId == null && cacheData) {
                                    // Это важно !!!
                                    // Если мы получаем сообщения сначала и кэшируем их в базу,
                                    // то нельзя отдать этот список в ответ (как сделано чуть ниже)
                                    // Так как мы теряем сообщения со статусами, отличными от SENT
                                    insertCompletable.andThen(
                                        getCachedPeerMessages(
                                            accountId,
                                            peerId
                                        )
                                    )
                                } else {
                                    val messages: MutableList<Message> =
                                        ArrayList(dtos.size)
                                    for (dto in dtos) {
                                        messages.add(transform(accountId, dto, it))
                                    }
                                    insertCompletable.andThen(
                                        Single.just<List<Message>>(messages)
                                            .compose(decryptor.withMessagesDecryption(accountId))
                                    )
                                }
                            })
            }
    }

    override fun insertDialog(accountId: Int, dialog: Dialog): Completable {
        val dialogsStore = storages.dialogs()
        return dialogsStore.insertDialogs(accountId, listOf(buildDialog(dialog)), false)
    }

    override fun getDialogs(
        accountId: Int,
        count: Int,
        startMessageId: Int?
    ): Single<List<Dialog>> {
        val clear = startMessageId == null
        val dialogsStore = storages.dialogs()
        return networker.vkDefault(accountId)
            .messages()
            .getDialogs(null, count, startMessageId, true, Constants.MAIN_OWNER_FIELDS)
            .map { response: DialogsResponse ->
                if (startMessageId != null && safeCountOf(response.dialogs) > 0) {
                    // remove first item, because we will have duplicate with previous response
                    response.dialogs.removeAt(0)
                }
                response
            }
            .flatMap { response: DialogsResponse ->
                val apiDialogs: List<VkApiDialog?> = listEmptyIfNull(response.dialogs)
                val ownerIds: Collection<Int> = if (apiDialogs.nonNullNoEmpty()) {
                    val vkOwnIds = VKOwnIds()
                    vkOwnIds.append(accountId) // добавляем свой профайл на всякий случай
                    for (dialog in apiDialogs) {
                        vkOwnIds.append(dialog!!)
                    }
                    vkOwnIds.all
                } else {
                    emptyList()
                }
                val existsOwners = transformOwners(response.profiles, response.groups)
                val ownerEntities = mapOwners(response.profiles, response.groups)
                ownersRepository
                    .findBaseOwnersDataAsBundle(
                        accountId,
                        ownerIds,
                        IOwnersRepository.MODE_ANY,
                        existsOwners
                    )
                    .flatMap { owners: IOwnersBundle? ->
                        val entities: MutableList<DialogEntity> = ArrayList(apiDialogs.size)
                        val dialogs: MutableList<Dialog> = ArrayList(apiDialogs.size)
                        val encryptedMessages: MutableList<Message> =
                            ArrayList(0)
                        for (dto in apiDialogs) {
                            val entity = mapDialog(dto!!)
                            entities.add(entity)
                            val dialog = transform(accountId, dto, owners!!)
                            dialogs.add(dialog)
                            if (entity.message.isEncrypted) {
                                encryptedMessages.add(dialog.message)
                            }
                        }
                        val insertCompletable = dialogsStore
                            .insertDialogs(accountId, entities, clear)
                            .andThen(ownersRepository.insertOwners(accountId, ownerEntities))
                            .doOnComplete {
                                dialogsStore.setUnreadDialogsCount(
                                    accountId,
                                    response.unreadCount
                                )
                            }
                        if (encryptedMessages.nonNullNoEmpty()) {
                            insertCompletable.andThen(
                                Single.just<List<Message>>(
                                    encryptedMessages
                                )
                                    .compose(decryptor.withMessagesDecryption(accountId))
                                    .map<List<Dialog>> { dialogs })
                        } else {
                            insertCompletable.andThen(Single.just<List<Dialog>>(dialogs))
                        }
                    }
            }
    }

    override fun findCachedMessages(
        accountId: Int,
        ids: List<Int>
    ): Single<List<Message>> {
        return storages.messages()
            .findMessagesByIds(accountId, ids, withAtatchments = true, withForwardMessages = true)
            .compose(entities2Models(accountId))
            .compose(decryptor.withMessagesDecryption(accountId))
    }

    @SuppressLint("UseSparseArrays")
    override fun put(builder: SaveMessageBuilder?): Single<Message> {
        val accountId = builder!!.accountId
        val draftMessageId = builder.draftMessageId
        val peerId = builder.peerId
        return getTargetMessageStatus(builder)
            .flatMap { status: Int? ->
                val patch = MessageEditEntity(status!!, accountId)
                patch.isEncrypted = builder.isRequireEncryption
                patch.payload = builder.payload
                patch.keyboard = builder.keyboard
                patch.date = now()
                patch.isRead = false
                patch.isOut = true
                patch.isDeleted = false
                patch.isImportant = false
                val voice = builder.voiceMessageFile
                if (voice != null) {
                    val extras: MutableMap<Int, String> = HashMap(1)
                    extras[Message.Extra.VOICE_RECORD] =
                        voice.absolutePath
                    patch.extras = extras
                }
                if (builder.attachments.nonNullNoEmpty()) {
                    patch.attachments = buildDboAttachments(builder.attachments)
                }
                val fwds = builder.forwardMessages
                if (fwds.nonNullNoEmpty()) {
                    val fwddbos: MutableList<MessageEntity> = ArrayList(fwds.size)
                    for (message in fwds) {
                        val fwddbo = buildMessageEntity(message!!)
                        fwddbo.originalId =
                            message.id // сохранить original_id необходимо, так как при вставке в таблицу _ID потеряется

                        // fixes
                        if (fwddbo.isOut) {
                            fwddbo.fromId = accountId
                        }
                        fwddbos.add(fwddbo)
                    }
                    patch.forward = fwddbos
                } else {
                    patch.forward = emptyList()
                }
                getFinalMessagesBody(builder)
                    .flatMap { body: Optional<String> ->
                        patch.body = body.get()
                        val storeSingle: Single<Int> = if (draftMessageId != null) {
                            storages.messages().applyPatch(accountId, draftMessageId, patch)
                        } else {
                            storages.messages().insert(accountId, peerId, patch)
                        }
                        storeSingle
                            .flatMap { resultMid: Int ->
                                storages.messages()
                                    .findMessagesByIds(
                                        accountId, listOf(resultMid),
                                        withAtatchments = true, withForwardMessages = true
                                    )
                                    .compose(entities2Models(accountId))
                                    .map { messages: List<Message> ->
                                        if (messages.isEmpty()) {
                                            throw NotFoundException()
                                        }
                                        val message = messages[0]
                                        if (builder.isRequireEncryption) {
                                            message.decryptedBody = builder.body
                                            message.cryptStatus = CryptStatus.DECRYPTED
                                        }
                                        message
                                    }
                            }
                    }
            }
    }

    private fun changeMessageStatus(
        accountId: Int,
        messageId: Int,
        @MessageStatus status: Int,
        vkid: Int?
    ): Completable {
        val update = MessageUpdate(accountId, messageId)
        update.statusUpdate = StatusUpdate(status, vkid)
        return storages.messages()
            .changeMessageStatus(accountId, messageId, status, vkid)
            .onErrorComplete()
            .doOnComplete { messageUpdatesPublisher.onNext(listOf(update)) }
    }

    override fun enqueueAgainList(accountId: Int, ids: Collection<Int>): Completable {
        val updates = ArrayList<MessageUpdate>(ids.size)
        for (i in ids) {
            val update = MessageUpdate(accountId, i)
            update.statusUpdate = StatusUpdate(MessageStatus.QUEUE, null)
            updates.add(update)
        }
        return storages.messages()
            .changeMessagesStatus(accountId, ids, MessageStatus.QUEUE)
            .onErrorComplete()
            .doOnComplete { messageUpdatesPublisher.onNext(updates) }
    }

    override fun enqueueAgain(accountId: Int, messageId: Int): Completable {
        return changeMessageStatus(accountId, messageId, MessageStatus.QUEUE, null)
    }

    override fun sendUnsentMessage(accountIds: Collection<Int>): Single<SentMsg> {
        val store = storages.messages()
        return store
            .findFirstUnsentMessage(accountIds, withAtatchments = true, withForwardMessages = false)
            .flatMap { optional: Optional<Pair<Int, MessageEntity>> ->
                if (optional.isEmpty) {
                    return@flatMap Single.error<SentMsg>(NotFoundException())
                }
                val entity = optional.get()!!.second
                val accountId = optional.get()!!.first
                val dbid = entity.id
                val peerId = entity.peerId
                changeMessageStatus(accountId, dbid, MessageStatus.SENDING, null)
                    .andThen(internalSend(accountId, entity)
                        .flatMap { vkid: Int? ->
                            val patch = PeerPatch(entity.peerId)
                                .withLastMessage(vkid!!)
                                .withUnreadCount(0)
                            changeMessageStatus(accountId, dbid, MessageStatus.SENT, vkid)
                                .andThen(applyPeerUpdatesAndPublish(accountId, listOf(patch)))
                                .andThen(Single.just(SentMsg(dbid, vkid, peerId, accountId)))
                        }
                        .onErrorResumeNext {
                            changeMessageStatus(
                                accountId,
                                dbid,
                                MessageStatus.ERROR,
                                null
                            ).andThen(
                                Single.error(it)
                            )
                        })
            }
    }

    override fun searchConversations(
        accountId: Int,
        count: Int,
        q: String?
    ): Single<List<Conversation>> {
        return networker.vkDefault(accountId)
            .messages()
            .searchConversations(q, count, 1, Constants.MAIN_OWNER_FIELDS)
            .flatMap { chattables: ConversationsResponse ->
                val conversations: List<VkApiConversation> =
                    listEmptyIfNull(chattables.conversations)
                val ownerIds: Collection<Int> = if (conversations.nonNullNoEmpty()) {
                    val vkOwnIds = VKOwnIds()
                    vkOwnIds.append(accountId)
                    for (dialog in conversations) {
                        vkOwnIds.append(dialog)
                    }
                    vkOwnIds.all
                } else {
                    emptyList()
                }
                val existsOwners = transformOwners(chattables.profiles, chattables.groups)
                ownersRepository
                    .findBaseOwnersDataAsBundle(
                        accountId,
                        ownerIds,
                        IOwnersRepository.MODE_ANY,
                        existsOwners
                    )
                    .flatMap { bundle: IOwnersBundle ->
                        val models: MutableList<Conversation> = ArrayList(conversations.size)
                        for (dialog in conversations) {
                            models.add(transform(accountId, dialog, bundle))
                        }
                        Single.just<List<Conversation>>(models)
                    }
            }
    }

    override fun searchMessages(
        accountId: Int,
        peerId: Int?,
        count: Int,
        offset: Int,
        q: String?
    ): Single<List<Message>> {
        return networker.vkDefault(accountId)
            .messages()
            .search(q, peerId, null, null, offset, count)
            .map { items: Items<VKApiMessage> ->
                listEmptyIfNull<VKApiMessage>(
                    items.getItems()
                )
            }
            .flatMap { dtos: List<VKApiMessage> ->
                val ids = VKOwnIds().append(dtos)
                ownersRepository
                    .findBaseOwnersDataAsBundle(accountId, ids.all, IOwnersRepository.MODE_ANY)
                    .map<List<Message>> { bundle: IOwnersBundle? ->
                        val data: MutableList<Message> =
                            ArrayList(dtos.size)
                        for (dto in dtos) {
                            val message = transform(accountId, dto, bundle!!)
                            data.add(message)
                        }
                        data
                    }
                    .compose(decryptor.withMessagesDecryption(accountId))
            }
    }

    override fun getChatUsers(accountId: Int, chatId: Int): Single<List<AppChatUser>> {
        return networker.vkDefault(accountId)
            .messages()
            .getConversationMembers(Peer.fromChatId(chatId), Constants.MAIN_OWNER_FIELDS)
            .flatMap { chatDto: ConversationMembersResponse ->
                val dtos: List<VKApiConversationMembers?> =
                    listEmptyIfNull(chatDto.conversationMembers)
                val ownerIds: Collection<Int> = if (dtos.nonNullNoEmpty()) {
                    val vkOwnIds = VKOwnIds()
                    vkOwnIds.append(accountId)
                    for (dto in dtos) {
                        vkOwnIds.append(dto!!.member_id)
                        vkOwnIds.append(dto.invited_by)
                    }
                    vkOwnIds.all
                } else {
                    emptyList()
                }
                val existsOwners = transformOwners(chatDto.profiles, chatDto.groups)
                ownersRepository.findBaseOwnersDataAsBundle(
                    accountId,
                    ownerIds,
                    IOwnersRepository.MODE_ANY,
                    existsOwners
                )
                    .map<List<AppChatUser>> { ownersBundle: IOwnersBundle ->
                        val models: MutableList<AppChatUser> = ArrayList(dtos.size)
                        for (dto in dtos) {
                            val user =
                                AppChatUser(ownersBundle.getById(dto!!.member_id), dto.invited_by)
                            user.isCanRemove = dto.can_kick
                            user.join_date = dto.join_date
                            user.isAdmin = dto.is_admin
                            user.isOwner = dto.is_owner
                            if (user.invitedBy != 0) {
                                user.inviter = ownersBundle.getById(user.invitedBy)
                            }
                            models.add(user)
                        }
                        models
                    }
            }
    }

    override fun removeChatMember(accountId: Int, chatId: Int, userId: Int): Completable {
        return networker.vkDefault(accountId)
            .messages()
            .removeChatMember(chatId, userId)
            .ignoreElement()
    }

    override fun deleteChatPhoto(accountId: Int, chatId: Int): Completable {
        return networker.vkDefault(accountId)
            .messages()
            .deleteChatPhoto(chatId)
            .ignoreElement()
    }

    override fun addChatUsers(
        accountId: Int,
        chatId: Int,
        users: List<User>
    ): Single<List<AppChatUser>> {
        val api = networker.vkDefault(accountId).messages()
        return ownersRepository.getBaseOwnerInfo(accountId, accountId, IOwnersRepository.MODE_ANY)
            .flatMap { iam: Owner? ->
                var completable = Completable.complete()
                val data: MutableList<AppChatUser> = ArrayList()
                for (user in users) {
                    completable =
                        completable.andThen(api.addChatUser(chatId, user.id).ignoreElement())
                    val chatUser = AppChatUser(user, accountId)
                        .setCanRemove(true)
                        .setInviter(iam)
                    data.add(chatUser)
                }
                completable.andThen(Single.just<List<AppChatUser>>(data))
            }
    }

    override fun deleteDialog(accountId: Int, peedId: Int): Completable {
        return networker.vkDefault(accountId)
            .messages()
            .deleteDialog(peedId)
            .flatMapCompletable {
                storages.dialogs()
                    .removePeerWithId(accountId, peedId)
                    .andThen(
                        storages.messages().insertPeerDbos(accountId, peedId, emptyList(), true)
                    )
            }
            .doOnComplete { peerDeletingPublisher.onNext(PeerDeleting(accountId, peedId)) }
    }

    override fun deleteMessages(
        accountId: Int,
        peerId: Int,
        ids: Collection<Int>,
        forAll: Boolean,
        spam: Boolean
    ): Completable {
        return networker.vkDefault(accountId)
            .messages()
            .delete(ids, forAll, spam)
            .flatMapCompletable { result: Map<String, Int> ->
                val patches: MutableList<MessagePatch> = ArrayList(result.size)
                for ((key, value) in result) {
                    val removed = value == 1
                    val removedId = key.toInt()
                    if (removed) {
                        val patch = MessagePatch(removedId, peerId)
                        patch.deletion = MessagePatch.Deletion(true, forAll)
                        patches.add(patch)
                    }
                }
                applyMessagesPatchesAndPublish(accountId, patches)
            }
    }

    override fun pinUnPinConversation(accountId: Int, peerId: Int, peen: Boolean): Completable {
        return networker.vkDefault(accountId)
            .messages()
            .pinUnPinConversation(peerId, peen)
    }

    override fun markAsImportant(
        accountId: Int,
        peerId: Int,
        ids: Collection<Int>,
        important: Int?
    ): Completable {
        return networker.vkDefault(accountId)
            .messages()
            .markAsImportant(ids, important)
            .flatMapCompletable { result: List<Int?> ->
                val patches: MutableList<MessagePatch> = ArrayList(result.size)
                for (entry in result) {
                    val marked = important == 1
                    val patch = MessagePatch(entry!!, peerId)
                    patch.important = Important(marked)
                    patches.add(patch)
                }
                applyMessagesPatchesAndPublish(accountId, patches)
            }
    }

    private fun applyMessagesPatchesAndPublish(
        accountId: Int,
        patches: List<MessagePatch>
    ): Completable {
        val updates: MutableList<MessageUpdate> = ArrayList(patches.size)
        val requireInvalidate: MutableSet<PeerId> = HashSet(0)
        for (patch in patches) {
            updates.add(patch2Update(accountId, patch))
            if (patch.deletion != null) {
                requireInvalidate.add(PeerId(accountId, patch.peerId))
            }
        }
        var afterApply = Completable.complete()
        val invalidatePeers: MutableList<Completable> = LinkedList()
        for (pair in requireInvalidate) {
            invalidatePeers.add(invalidatePeerLastMessage(pair.accountId, pair.peerId))
        }
        if (invalidatePeers.isNotEmpty()) {
            afterApply = Completable.merge(invalidatePeers)
        }
        return storages.messages()
            .applyPatches(accountId, patches)
            .andThen(afterApply)
            .doOnComplete { messageUpdatesPublisher.onNext(updates) }
    }

    private fun invalidatePeerLastMessage(accountId: Int, peerId: Int): Completable {
        return storages.messages()
            .findLastSentMessageIdForPeer(accountId, peerId)
            .flatMapCompletable {
                if (it.isEmpty) {
                    val deleting = PeerDeleting(accountId, peerId)
                    return@flatMapCompletable storages.dialogs().removePeerWithId(accountId, peerId)
                        .doOnComplete { peerDeletingPublisher.onNext(deleting) }
                } else {
                    val patch = PeerPatch(peerId).withLastMessage(it.requareNonEmpty())
                    return@flatMapCompletable applyPeerUpdatesAndPublish(accountId, listOf(patch))
                }
            }
    }

    override fun restoreMessage(accountId: Int, peerId: Int, messageId: Int): Completable {
        return networker.vkDefault(accountId)
            .messages()
            .restore(messageId)
            .flatMapCompletable {
                val patch = MessagePatch(messageId, peerId)
                patch.deletion = MessagePatch.Deletion(deleted = false, deletedForAll = false)
                applyMessagesPatchesAndPublish(accountId, listOf(patch))
            }
    }

    override fun editChat(accountId: Int, chatId: Int, title: String?): Completable {
        val patch = PeerPatch(Peer.fromChatId(chatId)).withTitle(title)
        return networker.vkDefault(accountId)
            .messages()
            .editChat(chatId, title)
            .flatMapCompletable {
                applyPeerUpdatesAndPublish(
                    accountId,
                    listOf(patch)
                )
            }
    }

    override fun createGroupChat(
        accountId: Int,
        users: Collection<Int>,
        title: String?
    ): Single<Int> {
        return networker.vkDefault(accountId)
            .messages()
            .createChat(users, title)
    }

    override fun recogniseAudioMessage(
        accountId: Int,
        message_id: Int?,
        audio_message_id: String?
    ): Single<Int> {
        return networker.vkDefault(accountId)
            .messages()
            .recogniseAudioMessage(message_id, audio_message_id)
    }

    override fun setMemberRole(
        accountId: Int,
        chat_id: Int,
        member_id: Int,
        isAdmin: Boolean
    ): Completable {
        return networker.vkDefault(accountId)
            .messages()
            .setMemberRole(Peer.fromChatId(chat_id), member_id, if (isAdmin) "admin" else "member")
            .ignoreElement()
    }

    override fun markAsRead(accountId: Int, peerId: Int, toId: Int): Completable {
        val patch = PeerPatch(peerId).withInRead(toId).withUnreadCount(0)
        return networker.vkDefault(accountId)
            .messages()
            .markAsRead(peerId, toId)
            .flatMapCompletable {
                applyPeerUpdatesAndPublish(
                    accountId,
                    listOf(patch)
                )
            }
    }

    override fun pin(
        accountId: Int,
        peerId: Int,
        message: Message?
    ): Completable {
        val update = PeerUpdate(accountId, peerId)
        update.pin = PeerUpdate.Pin(message)
        val apiCompletable: Completable = if (message == null) {
            networker.vkDefault(accountId)
                .messages()
                .unpin(peerId)
        } else {
            networker.vkDefault(accountId)
                .messages()
                .pin(peerId, message.id)
        }
        val patch = PeerPatch(peerId)
            .withPin(if (message == null) null else buildMessageEntity(message))
        return apiCompletable
            .andThen(storages.dialogs().applyPatches(accountId, listOf(patch)))
            .doOnComplete { peerUpdatePublisher.onNext(listOf(update)) }
    }

    private fun internalSend(accountId: Int, dbo: MessageEntity): Single<Int> {
        if (dbo.extras.isNullOrEmpty() && dbo.attachments.isNullOrEmpty() && dbo.forwardCount == 0) {
            return networker.vkDefault(accountId)
                .messages()
                .send(
                    dbo.id,
                    dbo.peerId,
                    null,
                    dbo.body,
                    null,
                    null,
                    null,
                    null,
                    null,
                    dbo.payload,
                    null
                )
        }
        val attachments: MutableCollection<IAttachmentToken> = LinkedList()
        try {
            if (dbo.attachments.nonNullNoEmpty()) {
                for (a in dbo.attachments!!) {
                    if (a is StickerEntity) {
                        val stickerId = a.id
                        return checkForwardMessages(accountId, dbo)
                            .flatMap {
                                if (it.first) {
                                    return@flatMap networker.vkDefault(accountId)
                                        .messages()
                                        .send(
                                            dbo.id,
                                            dbo.peerId,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            stickerId,
                                            null,
                                            it.second.requareNonEmpty()[0]
                                        )
                                }
                                networker.vkDefault(accountId)
                                    .messages()
                                    .send(
                                        dbo.id,
                                        dbo.peerId,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        stickerId,
                                        null,
                                        null
                                    )
                            }
                    }
                    attachments.add(createToken(a))
                }
            }
        } catch (e: Exception) {
            return Single.error(e)
        }
        return checkVoiceMessage(accountId, dbo)
            .flatMap { optionalToken: Optional<IAttachmentToken> ->
                if (optionalToken.nonEmpty()) {
                    attachments.add(optionalToken.requareNonEmpty())
                }
                checkForwardMessages(accountId, dbo)
                    .flatMap {
                        if (it.first && (!dbo.body.isNullOrEmpty() || dbo.isHasAttachmens)) {
                            networker.vkDefault(accountId)
                                .messages()
                                .send(
                                    dbo.id,
                                    dbo.peerId,
                                    null,
                                    dbo.body,
                                    null,
                                    null,
                                    attachments,
                                    null,
                                    null,
                                    null,
                                    it.second.requareNonEmpty()[0]
                                )
                        } else {
                            networker.vkDefault(accountId)
                                .messages()
                                .send(
                                    dbo.id,
                                    dbo.peerId,
                                    null,
                                    dbo.body,
                                    null,
                                    null,
                                    attachments,
                                    it.second.get(),
                                    null,
                                    null,
                                    null
                                )
                        }
                    }
            }
    }

    private fun checkForwardMessages(
        accountId: Int,
        dbo: MessageEntity
    ): Single<Pair<Boolean, Optional<List<Int>>>> {
        return if (dbo.forwardCount == 0) {
            Single.just(Pair(false, empty()))
        } else storages.messages()
            .getForwardMessageIds(accountId, dbo.id, dbo.peerId)
            .map {
                Pair(
                    it.first,
                    wrap(it.second)
                )
            }
    }

    private fun checkVoiceMessage(
        accountId: Int,
        dbo: MessageEntity
    ): Single<Optional<IAttachmentToken>> {
        val extras = dbo.extras
        if (extras != null && extras.containsKey(Message.Extra.VOICE_RECORD)) {
            val filePath = extras[Message.Extra.VOICE_RECORD]
            val docsApi = networker.vkDefault(accountId).docs()
            return docsApi.getMessagesUploadServer(dbo.peerId, "audio_message")
                .flatMap { server: VkApiDocsUploadServer ->
                    val file = File(filePath!!)
                    val `is` = arrayOfNulls<InputStream>(1)
                    try {
                        `is`[0] = FileInputStream(file)
                        return@flatMap networker.uploads()
                            .uploadDocumentRx(server.url, file.name, `is`[0]!!, null)
                            .doFinally(safelyCloseAction(`is`[0]))
                            .flatMap { uploadDto: UploadDocDto ->
                                docsApi
                                    .save(uploadDto.file, null, null)
                                    .map { dtos: VkApiDoc.Entry ->
                                        if (dtos.type.isEmpty()) {
                                            throw NotFoundException("Unable to save voice message")
                                        }
                                        val dto = dtos.doc
                                        val token = AttachmentsTokenCreator.ofDocument(
                                            dto.id,
                                            dto.ownerId,
                                            dto.accessKey
                                        )
                                        wrap(token)
                                    }
                            }
                    } catch (e: FileNotFoundException) {
                        safelyClose(`is`[0])
                        return@flatMap Single.error<Optional<IAttachmentToken>>(e)
                    }
                }
        }
        return Single.just(empty())
    }

    private fun getFinalMessagesBody(builder: SaveMessageBuilder): Single<Optional<String>> {
        if (builder.body.isNullOrEmpty() || !builder.isRequireEncryption) {
            return Single.just(
                wrap(
                    builder.body
                )
            )
        }
        @KeyLocationPolicy val policy = builder.keyLocationPolicy
        return storages.keys(policy)
            .findLastKeyPair(builder.accountId, builder.peerId)
            .map {
                if (it.isEmpty) {
                    throw KeyPairDoesNotExistException()
                }
                val pair = it.requareNonEmpty()
                val encrypted = encryptWithAes(
                    builder.body,
                    pair.myAesKey,
                    builder.body,
                    pair.sessionId,
                    builder.keyLocationPolicy
                )
                wrap(encrypted)
            }
    }

    private fun getTargetMessageStatus(builder: SaveMessageBuilder?): Single<Int> {
        val accountId = builder!!.accountId
        if (builder.draftMessageId == null) {
            return Single.just(MessageStatus.QUEUE)
        }
        val destination = forMessage(builder.draftMessageId)
        return uploadManager[accountId, destination]
            .map { uploads: List<Upload> ->
                if (uploads.isEmpty()) {
                    return@map MessageStatus.QUEUE
                }
                var uploadingNow = false
                for (o in uploads) {
                    if (o.status == Upload.STATUS_CANCELLING) {
                        continue
                    }
                    if (o.status == Upload.STATUS_ERROR) {
                        throw UploadNotResolvedException()
                    }
                    uploadingNow = true
                }
                if (uploadingNow) MessageStatus.WAITING_FOR_UPLOAD else MessageStatus.QUEUE
            }
    }

    private class InternalHandler(repository: MessagesRepository) :
        WeakMainLooperHandler<MessagesRepository>(repository) {
        fun runSend() {
            sendEmptyMessage(SEND)
        }

        override fun handleMessage(t: MessagesRepository, msg: android.os.Message) {
            if (msg.what == SEND) {
                t.send()
            }
        }

        companion object {
            const val SEND = 1
        }
    }

    private class PeerId(val accountId: Int, val peerId: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val peerId1 = other as PeerId
            return if (accountId != peerId1.accountId) false else peerId == peerId1.peerId
        }

        override fun hashCode(): Int {
            var result = accountId
            result = 31 * result + peerId
            return result
        }
    }

    companion object {
        private val DTO_TO_DBO = SingleTransformer { single: Single<List<VKApiMessage>> ->
            single
                .map<List<MessageEntity>> { dtos: List<VKApiMessage> ->
                    val dbos: MutableList<MessageEntity> = ArrayList(dtos.size)
                    for (dto in dtos) {
                        dbos.add(mapMessage(dto))
                    }
                    dbos
                }
        }

        private fun entity2Model(
            accountId: Int,
            entity: SimpleDialogEntity?,
            owners: IOwnersBundle
        ): Conversation {
            return Conversation(entity!!.peerId)
                .setInRead(entity.inRead)
                .setOutRead(entity.outRead)
                .setPhoto50(entity.photo50)
                .setPhoto100(entity.photo100)
                .setPhoto200(entity.photo200)
                .setUnreadCount(entity.unreadCount)
                .setTitle(entity.title)
                .setInterlocutor(
                    if (Peer.isGroup(entity.peerId) || Peer.isUser(
                            entity.peerId
                        )
                    ) owners.getById(entity.peerId) else null
                )
                .setPinned(
                    if (entity.pinned == null) null else message(
                        accountId,
                        entity.pinned,
                        owners
                    )
                )
                .setAcl(entity.acl)
                .setGroupChannel(entity.isGroupChannel)
                .setCurrentKeyboard(buildKeyboardFromDbo(entity.currentKeyboard))
                .setMajor_id(entity.major_id)
                .setMinor_id(entity.minor_id)
        }

        private fun patch2Update(accountId: Int, patch: MessagePatch): MessageUpdate {
            val update = MessageUpdate(accountId, patch.messageId)
            if (patch.deletion != null) {
                update.deleteUpdate =
                    MessageUpdate.DeleteUpdate(
                        patch.deletion!!.deleted,
                        patch.deletion!!.deletedForAll
                    )
            }
            if (patch.important != null) {
                update.importantUpdate = ImportantUpdate(patch.important!!.important)
            }
            return update
        }
    }

    init {
        decryptor = MessagesDecryptor(storages)
        this.uploadManager = uploadManager
        compositeDisposable.add(
            uploadManager.observeResults()
                .filter { it.first.destination.method == Method.TO_MESSAGE }
                .subscribe({ result: Pair<Upload, UploadResult<*>> ->
                    onUpdloadSuccess(
                        result.first
                    )
                }, ignore())
        )
        compositeDisposable.add(
            accountsSettings.observeRegistered()
                .observeOn(provideMainThreadScheduler())
                .subscribe({ onAccountsChanged() }, ignore())
        )
    }
}