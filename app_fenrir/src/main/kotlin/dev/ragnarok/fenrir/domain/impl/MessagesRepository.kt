package dev.ragnarok.fenrir.domain.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.Fields
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.AttachmentsTokenCreator
import dev.ragnarok.fenrir.api.model.VKApiConversation
import dev.ragnarok.fenrir.api.model.VKApiConversationMembers
import dev.ragnarok.fenrir.api.model.VKApiDialog
import dev.ragnarok.fenrir.api.model.VKApiMessage
import dev.ragnarok.fenrir.api.model.VKApiReactedMessagesPeers
import dev.ragnarok.fenrir.api.model.VKApiReactionAsset
import dev.ragnarok.fenrir.api.model.interfaces.IAttachmentToken
import dev.ragnarok.fenrir.api.model.local_json.ChatJsonResponse
import dev.ragnarok.fenrir.api.model.longpoll.BadgeCountChangeUpdate
import dev.ragnarok.fenrir.api.model.longpoll.InputMessagesSetReadUpdate
import dev.ragnarok.fenrir.api.model.longpoll.MessageFlagsResetUpdate
import dev.ragnarok.fenrir.api.model.longpoll.MessageFlagsSetUpdate
import dev.ragnarok.fenrir.api.model.longpoll.MinorIdUpdate
import dev.ragnarok.fenrir.api.model.longpoll.OutputMessagesSetReadUpdate
import dev.ragnarok.fenrir.api.model.longpoll.PinConversationUpdate
import dev.ragnarok.fenrir.api.model.longpoll.ReactionMessageChangeUpdate
import dev.ragnarok.fenrir.api.model.longpoll.TypingMessageOrUploadingInDialogUpdate
import dev.ragnarok.fenrir.api.model.response.AttachmentsHistoryResponse
import dev.ragnarok.fenrir.api.model.response.SendMessageResponse
import dev.ragnarok.fenrir.db.interfaces.IStorages
import dev.ragnarok.fenrir.db.model.MessageEditEntity
import dev.ragnarok.fenrir.db.model.MessagePatch
import dev.ragnarok.fenrir.db.model.MessagePatch.Important
import dev.ragnarok.fenrir.db.model.PeerPatch
import dev.ragnarok.fenrir.db.model.entity.DialogDboEntity
import dev.ragnarok.fenrir.db.model.entity.MessageDboEntity
import dev.ragnarok.fenrir.db.model.entity.PeerDialogEntity
import dev.ragnarok.fenrir.db.model.entity.ReactionAssetEntity
import dev.ragnarok.fenrir.db.model.entity.StickerDboEntity
import dev.ragnarok.fenrir.domain.FetchMode
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IMessagesRepository
import dev.ragnarok.fenrir.domain.interfaces.IOwnersRepository
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapDialog
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapMessage
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapOwners
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapPeerDialog
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transform
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transformMessages
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transformOwners
import dev.ragnarok.fenrir.domain.mappers.Entity2Dto.createToken
import dev.ragnarok.fenrir.domain.mappers.Entity2Model
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.buildDialogFromDbo
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.buildKeyboardFromDbo
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.fillOwnerIds
import dev.ragnarok.fenrir.domain.mappers.Entity2Model.message
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll
import dev.ragnarok.fenrir.domain.mappers.Model2Dto.createTokens
import dev.ragnarok.fenrir.domain.mappers.Model2Entity.buildDboAttachments
import dev.ragnarok.fenrir.domain.mappers.Model2Entity.buildDialog
import dev.ragnarok.fenrir.domain.mappers.Model2Entity.buildMessageEntity
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.exception.UploadNotResolvedException
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.kJsonNotPretty
import dev.ragnarok.fenrir.longpoll.NotificationHelper.tryCancelNotificationForPeer
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.model.AppChatUser
import dev.ragnarok.fenrir.model.Chat
import dev.ragnarok.fenrir.model.Conversation
import dev.ragnarok.fenrir.model.Dialog
import dev.ragnarok.fenrir.model.IOwnersBundle
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.MessageForwardSend
import dev.ragnarok.fenrir.model.MessageStatus
import dev.ragnarok.fenrir.model.MessageUpdate
import dev.ragnarok.fenrir.model.MessageUpdate.ImportantUpdate
import dev.ragnarok.fenrir.model.MessageUpdate.StatusUpdate
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.OwnerType
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.model.PeerDeleting
import dev.ragnarok.fenrir.model.PeerType
import dev.ragnarok.fenrir.model.PeerUpdate
import dev.ragnarok.fenrir.model.ReactedMessagesPeers
import dev.ragnarok.fenrir.model.ReactionAsset
import dev.ragnarok.fenrir.model.SaveMessageBuilder
import dev.ragnarok.fenrir.model.SentMsg
import dev.ragnarok.fenrir.model.Sex
import dev.ragnarok.fenrir.model.TypingMessageOrUploadingInDialog
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.model.criteria.DialogsCriteria
import dev.ragnarok.fenrir.model.criteria.MessagesCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.push.OwnerInfo
import dev.ragnarok.fenrir.push.OwnerInfo.Companion.getRx
import dev.ragnarok.fenrir.requireNonNull
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.upload.IUploadManager
import dev.ragnarok.fenrir.upload.Method
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.upload.UploadDestination.Companion.forMessage
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.UnixTime.now
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.Utils.hasFlag
import dev.ragnarok.fenrir.util.Utils.isHiddenAccount
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNullMutable
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import dev.ragnarok.fenrir.util.Utils.safelyClose
import dev.ragnarok.fenrir.util.VKOwnIds
import dev.ragnarok.fenrir.util.WeakMainLooperHandler
import dev.ragnarok.fenrir.util.coroutines.CompositeJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.andThen
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.createPublishSubject
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.dummy
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.emptyTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromScopeToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.ignoreElement
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.mergeFlows
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.myEmit
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.syncSingleSafe
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlowThrowable
import dev.ragnarok.fenrir.util.toast.CustomToast.Companion.createCustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.single
import kotlinx.serialization.json.decodeFromBufferedSource
import okio.Source
import okio.buffer
import okio.source
import java.io.File
import java.io.FileNotFoundException
import java.util.LinkedList
import java.util.concurrent.Executors

class MessagesRepository(
    private val networker: INetworker,
    private val ownersRepository: IOwnersRepository,
    private val storages: IStorages,
    private val uploadManager: IUploadManager
) : IMessagesRepository {
    private val peerUpdatePublisher = createPublishSubject<List<PeerUpdate>>()
    private val peerDeletingPublisher = createPublishSubject<PeerDeleting>()
    private val messageUpdatesPublisher = createPublishSubject<MessageUpdate>()
    private val typingMessageOrUploadingInDialogPublisher =
        createPublishSubject<List<TypingMessageOrUploadingInDialog>>()
    private val sentMessagesPublisher = createPublishSubject<SentMsg>()
    private val sendErrorsPublisher = createPublishSubject<Throwable>()
    private val compositeJob = CompositeJob()
    private val senderScheduler =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    private val handler = InternalHandler(this)
    private var nowSending = false

    //private var registeredAccounts: List<Long>? = null
    override fun observeMessagesSendErrors(): SharedFlow<Throwable> {
        return sendErrorsPublisher
    }

    override fun observeTypingMessageOrUploadingInDialog(): SharedFlow<List<TypingMessageOrUploadingInDialog>> {
        return typingMessageOrUploadingInDialogPublisher
    }

    /*
    private fun onAccountsChanged() {
        registeredAccounts = accountsSettings.registered
    }
     */

    override fun runSendingQueue() {
        handler.runSend()
    }

    /**
     * Отправить первое не отправленное сообщение
     */
    @MainThread
    internal fun send() {
        if (nowSending) {
            return
        }
        nowSending = true
        //sendMessage(registeredAccounts())
        sendMessage(listOf(Settings.get().accounts().current))
    }

    /*
    private fun registeredAccounts(): List<Long>? {
        if (registeredAccounts == null) {
            registeredAccounts = accountsSettings.registered
        }
        return registeredAccounts
    }
     */

    private fun onMessageSent(msg: SentMsg) {
        if (!Settings.get().main().isBe_online || isHiddenAccount(msg.accountId)) {
            compositeJob.add(
                InteractorFactory.account.setOffline(msg.accountId)
                    .fromScopeToMain(senderScheduler, dummy())
            )
        }
        nowSending = false
        sentMessagesPublisher.myEmit(msg)
        send()
    }

    private fun onMessageSendError(t: Throwable) {
        val cause = getCauseIfRuntime(t)
        nowSending = false
        if (cause is NotFoundException) {
            val accountId = Settings.get().accounts().current
            if (!Settings.get().main().isBe_online || !isHiddenAccount(accountId)) {
                compositeJob.add(
                    InteractorFactory.account.setOffline(accountId)
                        .fromScopeToMain(senderScheduler, dummy())
                )
            }
            // no unsent messages
            return
        }
        sendErrorsPublisher.myEmit(t)
    }

    private fun sendMessage(accountIds: Collection<Long>?) {
        nowSending = true
        compositeJob.add(
            sendUnsentMessage(accountIds ?: return)
                .fromScopeToMain(senderScheduler, {
                    onMessageSent(it)
                }, {
                    onMessageSendError(
                        it
                    )
                })
        )
    }

    private fun onUploadSuccess(upload: Upload) {
        val accountId = upload.accountId
        val cmId = upload.destination.id
        val peerId = upload.destination.ownerId
        compositeJob.add(
            uploadManager[accountId, upload.destination]
                .flatMapConcat { uploads ->
                    if (uploads.isNotEmpty()) {
                        toFlow(false)
                    } else {
                        storages.messages().getMessageStatus(accountId, cmId, peerId)
                            .flatMapConcat { status ->
                                if (status != MessageStatus.WAITING_FOR_UPLOAD) {
                                    toFlow(false)
                                } else {
                                    changeMessageStatus(
                                        accountId = accountId,
                                        dbCmId = cmId,
                                        peerId = peerId,
                                        status = MessageStatus.QUEUE,
                                        vkCmId = null,
                                        vkMessageId = null
                                    ).map {
                                        true
                                    }
                                }
                            }
                    }
                }
                .fromScopeToMain(senderScheduler) {
                    if (it) {
                        runSendingQueue()
                    }
                }
        )
    }

    override fun handleFlagsUpdates(
        accountId: Long,
        setUpdates: List<MessageFlagsSetUpdate>?,
        resetUpdates: List<MessageFlagsResetUpdate>?
    ): Flow<Boolean> {
        val patches: MutableList<MessagePatch> = ArrayList()
        if (setUpdates.nonNullNoEmpty()) {
            for (update in setUpdates) {
                if (!hasFlag(update.mask, VKApiMessage.FLAG_DELETED)
                    && !hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)
                    && !hasFlag(update.mask, VKApiMessage.FLAG_DELETED_FOR_ALL)
                ) continue
                val patch = MessagePatch(update.cmId, update.peerId)
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
                val patch = MessagePatch(update.cmId, update.peerId)
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

    override fun handleMessageReactionsChangedUpdates(
        accountId: Long,
        updates: List<ReactionMessageChangeUpdate>?
    ): Flow<Boolean> {
        val patches: MutableList<MessagePatch> = ArrayList()
        if (updates.nonNullNoEmpty()) {
            for (update in updates) {
                val patch = MessagePatch(update.cmId, update.peerId)
                patch.reaction = MessagePatch.ReactionUpdate(
                    !update.myReactionChanged,
                    update.myReaction,
                    mapAll(update.arrayReactionList) { Dto2Entity.mapReaction(it) })
                patches.add(patch)
            }
        }
        return applyMessagesPatchesAndPublish(accountId, patches)
    }

    override fun handleTypingMessageOrUploadingInDialogUpdates(
        accountId: Long,
        updates: List<TypingMessageOrUploadingInDialogUpdate>?
    ): Flow<Boolean> {
        return flow {
            if (updates.nonNullNoEmpty()) {
                val list: MutableList<TypingMessageOrUploadingInDialog> = ArrayList()
                for (update in updates) {
                    list.add(
                        TypingMessageOrUploadingInDialog(
                            accountId,
                            update.peerId,
                            update.fromIds,
                            update.action
                        )
                    )
                }
                typingMessageOrUploadingInDialogPublisher.emit(list)
            }
            emit(true)
        }
    }

    override fun handleUnreadBadgeUpdates(
        accountId: Long,
        updates: List<BadgeCountChangeUpdate>?
    ): Flow<Boolean> {
        return flow {
            if (updates.nonNullNoEmpty()) {
                for (update in updates) {
                    storages.dialogs().setUnreadDialogsCount(accountId, update.count)
                }
            }
            emit(true)
        }
    }

    @StringRes
    private fun getTypeUser(ownr: OwnerInfo): Int {
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
        accountId: Long,
        setUpdates: List<OutputMessagesSetReadUpdate>?,
        resetUpdates: List<InputMessagesSetReadUpdate>?
    ): Flow<Boolean> {
        val patches: MutableList<PeerPatch> = ArrayList()
        if (setUpdates.nonNullNoEmpty()) {
            for (update in setUpdates) {
                if (!Settings.get().main().isDisable_notifications && Settings.get()
                        .main().isInfo_reading && Peer.getType(update.peerId) != PeerType.CHAT
                ) {
                    compositeJob.add(
                        getRx(
                            provideApplicationContext(),
                            Settings.get().accounts().current,
                            update.peerId
                        )
                            .fromIOToMain { userInfo ->
                                createCustomToast(
                                    provideApplicationContext(), null
                                )?.setBitmap(userInfo.avatar)?.showToastInfo(
                                    userInfo.owner.fullName + " " + provideApplicationContext().getString(
                                        getTypeUser(userInfo)
                                    )
                                )
                            })
                }
                patches.add(PeerPatch(update.peerId).withOutReadCmId(update.cmId))
            }
        }
        if (resetUpdates.nonNullNoEmpty()) {
            for (update in resetUpdates) {
                val patch = PeerPatch(update.peerId).withInReadCmId(update.cmId)
                    .withUnreadCount(update.unreadCount)
                if (update.peerId == accountId) {
                    patch.withOutReadCmId(update.cmId)
                }
                patches.add(
                    patch
                )
                tryCancelNotificationForPeer(provideApplicationContext(), accountId, update.peerId)
            }
        }
        return applyPeerUpdatesAndPublish(accountId, patches)
    }

    override fun handlePinConversationUpdates(
        accountId: Long,
        updates: List<PinConversationUpdate>?,
    ): Flow<Boolean> {
        val patches: MutableList<PeerPatch> = ArrayList()
        if (updates.nonNullNoEmpty()) {
            for (update in updates) {
                patches.add(PeerPatch(update.peerId).withMajorId(update.majorId))
            }
        }
        return applyPeerUpdatesAndPublish(accountId, patches)
    }

    override fun handleMinorIdConversationUpdates(
        accountId: Long,
        updates: List<MinorIdUpdate>?
    ): Flow<Boolean> {
        val patches: MutableList<PeerPatch> = ArrayList()
        if (updates.nonNullNoEmpty()) {
            for (update in updates) {
                patches.add(PeerPatch(update.peerId).withMinorId(update.minorId))
            }
        }
        return applyPeerUpdatesAndPublish(accountId, patches)
    }

    override fun observeSentMessages(): SharedFlow<SentMsg> {
        return sentMessagesPublisher
    }

    override fun observeMessageUpdates(): SharedFlow<MessageUpdate> {
        return messageUpdatesPublisher
    }

    override fun observePeerUpdates(): SharedFlow<List<PeerUpdate>> {
        return peerUpdatePublisher
    }

    override fun observePeerDeleting(): SharedFlow<PeerDeleting> {
        return peerDeletingPublisher
    }

    override fun getConversationByIdSingle(
        accountId: Long,
        peerId: Long,
        fetchMode: FetchMode
    ): Flow<Conversation> {
        val cached = getCachedConversationById(accountId, peerId)
        val actual = getActualConversationById(accountId, peerId)
        when (fetchMode) {
            FetchMode.ANY -> return cached.flatMapConcat { optional ->
                if (optional.isEmpty) actual else toFlow(
                    optional.requireNonEmpty()
                )
            }

            FetchMode.NET -> return actual
            FetchMode.CACHE -> return cached
                .map { optional ->
                    if (optional.isEmpty) {
                        throw NotFoundException()
                    } else {
                        optional.requireNonEmpty()
                    }
                }

            else -> {}
        }
        throw IllegalArgumentException("Unsupported mode: $fetchMode")
    }

    private fun getCachedConversationById(
        accountId: Long,
        peerId: Long
    ): Flow<Optional<Conversation>> {
        return storages.dialogs()
            .findPeerDialog(accountId, peerId)
            .flatMapConcat { optional ->
                if (optional.isEmpty) {
                    toFlow(Optional.empty())
                } else {
                    toFlow(optional.requireNonEmpty())
                        .flatMapConcat(simpleEntity2Conversation(accountId, emptyList()))
                        .map { Optional.wrap(it) }
                }
            }
    }

    private fun getActualConversationById(accountId: Long, peerId: Long): Flow<Conversation> {
        return networker.vkDefault(accountId)
            .messages()
            .getConversationsById(listOf(peerId), true, Fields.FIELDS_BASE_OWNER)
            .flatMapConcat { response ->
                if (response.items.isNullOrEmpty()) {
                    toFlowThrowable(NotFoundException())
                } else {
                    val dto = response.items?.get(0)
                    if (dto == null) {
                        toFlowThrowable(NotFoundException())
                    } else {
                        val entity = mapPeerDialog(dto, response.contacts)
                        if (entity == null) {
                            toFlowThrowable(NotFoundException())
                        } else {
                            val existsOwners = transformOwners(response.profiles, response.groups)
                            val ownerEntities = mapOwners(response.profiles, response.groups)
                            ownersRepository.insertOwners(accountId, ownerEntities)
                                .andThen(storages.dialogs().savePeerDialog(accountId, entity))
                                .map {
                                    entity
                                }.flatMapConcat(simpleEntity2Conversation(accountId, existsOwners))
                        }
                    }
                }
            }
    }

    override fun getConversationById(
        accountId: Long,
        peerId: Long,
        fetchMode: FetchMode
    ): Flow<Conversation> {
        val cached = getCachedConversationById(accountId, peerId)
        val actual = getActualConversationById(accountId, peerId)
        return when (fetchMode) {
            FetchMode.ANY -> cached
                .flatMapConcat { optional ->
                    if (optional.isEmpty) actual else toFlow(
                        optional.requireNonEmpty()
                    )
                }

            FetchMode.NET -> actual
            FetchMode.CACHE -> cached
                .map { optional ->
                    if (optional.isEmpty) {
                        throw NotFoundException()
                    } else {
                        optional.requireNonEmpty()
                    }
                }

            FetchMode.CACHE_THEN_ACTUAL -> {
                val cachedFlowable = cached
                    .filter { it.nonEmpty() }
                    .map { it.requireNonEmpty() }
                merge(cachedFlowable, actual)
            }
        }
    }

    private fun simpleEntity2Conversation(
        accountId: Long,
        existingOwners: Collection<Owner>
    ): suspend (PeerDialogEntity) -> Flow<Conversation> = { entity ->
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
            FetchMode.ANY,
            existingOwners
        ).map { bundle -> entity2Model(accountId, entity, bundle) }
    }

    override fun editMessage(
        accountId: Long,
        message: Message,
        text: String?,
        attachments: List<AbsModel>,
        keepForwardMessages: Boolean
    ): Flow<Message> {
        if (isHiddenAccount(accountId) && !Constants.IS_DEBUG) {
            throw UnsupportedOperationException()
        }
        val attachmentTokens = createTokens(attachments)
        return networker.vkDefault(accountId)
            .messages()
            .editMessage(
                message.peerId,
                message.cmId,
                text,
                attachmentTokens,
                keepForwardMessages,
                null
            )
            .andThen(getByConversationMessageId(accountId, message.peerId, message.cmId))
    }

    override fun getCachedPeerMessages(
        accountId: Long,
        peerId: Long
    ): Flow<List<Message>> {
        val criteria = MessagesCriteria(accountId, peerId)
        return storages.messages()
            .getByCriteria(criteria = criteria, withAtatchments = true, withForwardMessages = true)
            .flatMapConcat(entities2Models(accountId))
    }

    override fun getMessagesFromLocalJSon(
        accountId: Long,
        context: Context
    ): Flow<Pair<Peer, List<Message>>> {
        return flow {
            val b =
                (context as Activity).intent.data?.let {
                    context.contentResolver.openInputStream(
                        it
                    )
                }
            val resp = b?.let {
                kJson.decodeFromBufferedSource(
                    ChatJsonResponse.serializer(),
                    it.source().buffer()
                )
            }
            b?.close()
            if (resp == null || resp.page_title.isNullOrEmpty()) {
                throw Throwable("parsing error")
            } else {
                val ids = VKOwnIds().append(resp.messages)
                emit(
                    ownersRepository.findBaseOwnersDataAsBundle(
                        accountId,
                        ids.all,
                        FetchMode.ANY,
                        emptyList()
                    )
                        .map {
                            Pair(
                                Peer(resp.page_id).setAvaUrl(resp.page_avatar)
                                    .setTitle(resp.page_title),
                                transformMessages(resp.page_id, resp.messages.orEmpty(), it)
                            )
                        }.single()
                )
            }
        }
    }

    override fun getCachedConversations(accountId: Long): Flow<List<Dialog>> {
        val criteria = DialogsCriteria(accountId)
        return storages.dialogs()
            .getDialogs(criteria)
            .flatMapConcat { dbos ->
                val ownIds = VKOwnIds()
                for (dbo in dbos) {
                    when (Peer.getType(dbo.peerId)) {
                        PeerType.GROUP, PeerType.USER -> ownIds.append(dbo.peerId)
                        PeerType.CHAT, PeerType.CONTACT -> ownIds.append(dbo.message?.fromId)
                    }
                }
                ownersRepository
                    .findBaseOwnersDataAsBundle(accountId, ownIds.all, FetchMode.ANY)
                    .map { owners ->
                        val dialogs: MutableList<Dialog> = ArrayList(dbos.size)
                        for (dbo in dbos) {
                            dialogs.add(buildDialogFromDbo(accountId, dbo, owners))
                        }
                        dialogs
                    }
            }
    }

    private fun getByConversationMessageId(
        accountId: Long,
        peerId: Long,
        cmId: Int
    ): Flow<Message> {
        return networker.vkDefault(accountId)
            .messages()
            .getByConversationMessageId(peerId, listOf(cmId))
            .map { dtos ->
                mapAll(dtos) {
                    mapMessage(
                        it
                    )
                }
            }
            .flatMapConcat(entities2Models(accountId))
            .map { messages ->
                if (messages.isEmpty()) {
                    throw NotFoundException()
                } else {
                    messages[0]
                }
            }
    }

    private fun entities2Models(accountId: Long): suspend (List<MessageDboEntity>) -> Flow<List<Message>> =
        { dbos ->
            val ownIds = VKOwnIds()
            fillOwnerIds(ownIds, dbos)
            ownersRepository
                .findBaseOwnersDataAsBundle(
                    accountId,
                    ownIds.all,
                    FetchMode.ANY
                )
                .map { owners ->
                    val messages: MutableList<Message> =
                        ArrayList(dbos.size)
                    for (dbo in dbos) {
                        messages.add(message(accountId, dbo, owners))
                    }
                    messages
                }
        }

    private fun insertPeerMessages(
        accountId: Long,
        peerId: Long,
        messages: List<VKApiMessage>,
        clearBefore: Boolean
    ): Flow<Boolean> {
        return toFlow(messages)
            .map(DTO_TO_DBO)
            .flatMapConcat { dbos ->
                storages.messages().insertPeerDbos(accountId, peerId, dbos, clearBefore)
            }
    }

    override fun insertMessages(accountId: Long, messages: List<VKApiMessage>): Flow<Boolean> {
        return toFlow(messages)
            .map(DTO_TO_DBO)
            .flatMapConcat { storages.messages().insert(accountId, it) }
            .flatMapConcat {
                val peers: MutableSet<Long> = HashSet()
                for (m in messages) {
                    peers.add(m.peer_id)
                }
                storages.dialogs()
                    .findPeerStates(accountId, peers)
                    .flatMapConcat { peerStates ->
                        val patches: MutableList<PeerPatch> = ArrayList(peerStates.size)
                        for (state in peerStates) {
                            var unread = state.unreadCount
                            var cmId = state.lastCmId
                            var minorId = state.minorId
                            var keyboard: VKApiConversation.CurrentKeyboard? = null
                            for (m in messages) {
                                if (m.peer_id != state.peerId) {
                                    continue
                                }
                                if (m.out) {
                                    unread = 0
                                } else {
                                    unread++
                                }
                                if (m.conversation_message_id > cmId) {
                                    if (!m.out && m.keyboard != null && m.keyboard?.inline == false && m.keyboard?.buttons.nonNullNoEmpty()) {
                                        keyboard = m.keyboard
                                    }
                                    cmId = m.conversation_message_id
                                    if (m.message_id > minorId) {
                                        minorId = m.message_id
                                    }
                                }
                            }
                            val patch = PeerPatch(state.peerId)
                                .withUnreadCount(unread)
                                .withLastMessage(cmId, minorId)
                            if (keyboard != null) {
                                patch.withKeyboard(Dto2Entity.mapKeyboard(keyboard))
                            }
                            patches.add(patch)
                        }
                        applyPeerUpdatesAndPublish(accountId, patches)
                    }
            }
    }

    private fun applyPeerUpdatesAndPublish(
        accountId: Long,
        patches: List<PeerPatch>
    ): Flow<Boolean> {
        val updates: MutableList<PeerUpdate> = ArrayList()
        for (p in patches) {
            val update = PeerUpdate(accountId, p.id)
            p.inRead.requireNonNull {
                update.readIn = PeerUpdate.Read(it.cmId)
            }
            p.outRead.requireNonNull {
                update.readOut = PeerUpdate.Read(it.cmId)
            }
            p.lastMessage.requireNonNull {
                update.lastMessage = PeerUpdate.LastMessage(it.cmId)
            }
            p.unread.requireNonNull {
                update.unread = PeerUpdate.Unread(it.count)
            }
            p.title.requireNonNull {
                update.title = PeerUpdate.Title(it.title)
            }
            p.majorId.requireNonNull {
                update.majorId = PeerUpdate.MajorId(it.majorId)
            }
            p.minorId.requireNonNull {
                update.minorId = PeerUpdate.MinorId(it.minorId)
            }
            p.botKeyboard.requireNonNull {
                update.botKeyboard = PeerUpdate.BotKeyboard(it.keyboard)
            }
            updates.add(update)
        }
        return storages.dialogs().applyPatches(accountId, patches)
            .map {
                peerUpdatePublisher.emit(updates)
                true
            }
    }

    override fun getImportantMessages(
        accountId: Long,
        count: Int,
        offset: Int?
    ): Flow<List<Message>> {
        return networker.vkDefault(accountId)
            .messages()
            .getImportantMessages(offset, count, true, Fields.FIELDS_BASE_OWNER)
            .flatMapConcat { response ->
                val dtos: MutableList<VKApiMessage> =
                    if (response.messages == null) mutableListOf() else listEmptyIfNullMutable(
                        response.messages?.items
                    )
                val completable = emptyTaskFlow()
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
                                FetchMode.ANY,
                                existsOwners
                            )
                            .flatMapConcat {
                                val insertCompletable =
                                    ownersRepository.insertOwners(accountId, ownerEntities)
                                val messages: MutableList<Message> =
                                    ArrayList(dtos.size)
                                for (dto in dtos) {
                                    messages.add(transform(accountId, dto, it))
                                }
                                insertCompletable.andThen(
                                    toFlow(messages)
                                )
                            })
            }
    }

    override fun getJsonHistory(
        accountId: Long,
        offset: Int?,
        count: Int?,
        peerId: Long
    ): Flow<List<String>> {
        return networker.vkDefault(accountId)
            .messages()
            .getJsonHistory(offset, count, peerId)
            .map { response ->
                val dtos = listEmptyIfNull(
                    response.items
                )
                val messages: MutableList<String> = ArrayList(dtos.size)
                for (i in dtos) {
                    i.json_data.nonNullNoEmpty {
                        messages.add(it)
                    }
                }
                messages
            }
    }

    override fun getPeerMessages(
        accountId: Long, peerId: Long, count: Int, offset: Int?,
        startCmId: Int?, excludeStartMessage: Boolean, cacheData: Boolean, rev: Boolean
    ): Flow<List<Message>> {
        var pCount = count
        if (rev) pCount = 200
        return networker.vkDefault(accountId)
            .messages()
            .getPeerMessages(
                offset,
                pCount,
                peerId,
                startCmId,
                rev,
                true,
                Fields.FIELDS_BASE_OWNER
            )
            .flatMapConcat { response ->
                val dtos: MutableList<VKApiMessage> = listEmptyIfNullMutable(response.messages)
                var patch: PeerPatch? = null
                if (startCmId == null && cacheData && response.conversations.nonNullNoEmpty()) {
                    val conversation =
                        response.conversations?.get(0) ?: throw NullPointerException("WTF!")
                    patch = PeerPatch(peerId)
                        .withOutReadCmId(conversation.outReadCmId)
                        .withInReadCmId(conversation.inReadCmId)
                        .withLastMessage(
                            conversation.lastCmId,
                            conversation.sort_id?.minor_id.orZero()
                        )
                        .withUnreadCount(conversation.unreadCount)
                }
                if (excludeStartMessage && startCmId != null && dtos.nonNullNoEmpty() && startCmId == dtos[0].conversation_message_id) {
                    dtos.removeAt(0)
                }
                var completable: Flow<Boolean>
                if (cacheData) {
                    completable =
                        insertPeerMessages(accountId, peerId, dtos, startCmId == null)
                    if (patch != null) {
                        completable = completable.andThen(
                            applyPeerUpdatesAndPublish(
                                accountId,
                                listOf(patch)
                            )
                        )
                    }
                } else {
                    completable = emptyTaskFlow()
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
                                FetchMode.ANY,
                                existsOwners
                            )
                            .flatMapConcat {
                                val insertCompletable =
                                    ownersRepository.insertOwners(accountId, ownerEntities)
                                if (startCmId == null && cacheData) {
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
                                        toFlow(messages)
                                    )
                                }
                            })
            }
    }

    override fun insertConversation(accountId: Long, dialog: Dialog): Flow<Boolean> {
        val dialogsStore = storages.dialogs()
        return dialogsStore.insertDialogs(accountId, listOf(buildDialog(dialog)), false)
    }

    override fun getConversations(
        accountId: Long,
        count: Int,
        startMessageId: Int?
    ): Flow<List<Dialog>> {
        val clear = startMessageId == null
        val dialogsStore = storages.dialogs()
        return networker.vkDefault(accountId)
            .messages()
            .getConversations(null, count, startMessageId, true, Fields.FIELDS_BASE_OWNER)
            .map { response ->
                if (startMessageId != null && safeCountOf(response.dialogs) > 0) {
                    // remove first item, because we will have duplicate with previous response
                    response.dialogs?.removeAt(0)
                }
                response
            }
            .flatMapConcat { response ->
                val apiDialogs: List<VKApiDialog> = listEmptyIfNull(response.dialogs)
                val ownerIds: Collection<Long> = if (apiDialogs.nonNullNoEmpty()) {
                    val vkOwnIds = VKOwnIds()
                    vkOwnIds.append(accountId) // добавляем свой профайл на всякий случай
                    for (dialog in apiDialogs) {
                        vkOwnIds.append(dialog)
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
                        FetchMode.ANY,
                        existsOwners
                    )
                    .flatMapConcat { owners ->
                        val entities: MutableList<DialogDboEntity> = ArrayList(apiDialogs.size)
                        val dialogs: MutableList<Dialog> = ArrayList(apiDialogs.size)
                        for (dto in apiDialogs) {
                            val entity = mapDialog(dto, response.contacts) ?: continue
                            entities.add(entity)
                            val dialog = transform(accountId, dto, owners, response.contacts)
                            if (dialog != null) {
                                dialogs.add(dialog)
                            }
                        }
                        val insertCompletable = dialogsStore
                            .insertDialogs(accountId, entities, clear)
                            .andThen(ownersRepository.insertOwners(accountId, ownerEntities))
                            .map {
                                dialogsStore.setUnreadDialogsCount(
                                    accountId,
                                    response.unreadCount
                                )
                            }
                        insertCompletable.andThen(toFlow(dialogs))
                    }
            }
    }

    override fun findCachedMessages(
        accountId: Long,
        cmIds: List<Pair<Long, Int>>
    ): Flow<List<Message>> {
        return storages.messages()
            .findMessagesByIds(
                accountId = accountId,
                cmIds = cmIds,
                withAttachments = true,
                withForwardMessages = true
            )
            .flatMapConcat(entities2Models(accountId))
    }

    @SuppressLint("UseSparseArrays")
    override fun put(builder: SaveMessageBuilder): Flow<Message> {
        val accountId = builder.accountId
        val draftCmId = builder.draftCmId
        val peerId = builder.peerId
        return getTargetMessageStatus(builder)
            .flatMapConcat { status ->
                val patch = MessageEditEntity(status, accountId)
                patch.setPayload(builder.payload)
                patch.setDate(now())
                patch.setRead(false)
                patch.setOut(true)
                patch.setDeleted(false)
                patch.setImportant(false)
                val voice = builder.voiceMessageFile
                if (voice != null) {
                    val extras: MutableMap<Int, String> = HashMap(1)
                    extras[Message.Extra.VOICE_RECORD] =
                        voice.absolutePath
                    patch.setExtras(extras)
                }
                builder.attachments.nonNullNoEmpty {
                    patch.setAttachments(buildDboAttachments(it))
                }
                val fwds = builder.forwardMessages
                if (fwds.nonNullNoEmpty()) {
                    val fwddbos: MutableList<MessageDboEntity> = ArrayList(fwds.size)
                    for (message in fwds) {
                        val fwddbo = buildMessageEntity(message)
                        // fixes
                        if (fwddbo.isOut) {
                            fwddbo.setFromId(accountId)
                        }
                        fwddbos.add(fwddbo)
                    }
                    patch.setForward(fwddbos)
                } else {
                    patch.setForward(null)
                }
                patch.setText(builder.text)
                val storeSingle = if (draftCmId != null) {
                    storages.messages().applyPatch(accountId, draftCmId, peerId, patch)
                } else {
                    storages.messages().insert(accountId, peerId, patch)
                }
                storeSingle
                    .flatMapConcat { resultMid ->
                        storages.messages()
                            .findMessagesByIds(
                                accountId, listOf(Pair(peerId, resultMid)),
                                withAttachments = true, withForwardMessages = true
                            )
                            .flatMapConcat(entities2Models(accountId))
                            .map { messages ->
                                if (messages.isEmpty()) {
                                    throw NotFoundException()
                                }
                                messages[0]
                            }
                    }
            }
    }

    private fun changeMessageStatus(
        accountId: Long,
        dbCmId: Int,
        peerId: Long,
        @MessageStatus status: Int,
        vkCmId: Int?,
        vkMessageId: Int?
    ): Flow<Boolean> {
        val update = MessageUpdate(accountId, dbCmId, peerId)
        update.setStatusUpdate(StatusUpdate(status, vkCmId))
        return storages.messages()
            .changeMessageStatus(
                accountId = accountId,
                dbCmId = dbCmId,
                peerId = peerId,
                status = status,
                vkCmId = vkCmId,
                vkMessageId = vkMessageId
            )
            .map {
                messageUpdatesPublisher.emit(update)
                true
            }
    }

    override fun enqueueAgainList(
        accountId: Long,
        peerId: Long,
        cmIds: Collection<Int>
    ): Flow<Boolean> {
        val updates = ArrayList<MessageUpdate>(cmIds.size)
        for (i in cmIds) {
            val update = MessageUpdate(accountId, i, peerId)
            update.setStatusUpdate(StatusUpdate(MessageStatus.QUEUE, null))
            updates.add(update)
        }
        return storages.messages()
            .changeMessagesStatus(accountId, peerId, cmIds, MessageStatus.QUEUE)
            .map {
                for (i in updates) {
                    messageUpdatesPublisher.emit(i)
                }
                true
            }
    }

    override fun enqueueAgain(accountId: Long, peerId: Long, cmId: Int): Flow<Boolean> {
        return changeMessageStatus(
            accountId = accountId,
            dbCmId = cmId,
            peerId = peerId,
            status = MessageStatus.QUEUE,
            vkCmId = null,
            vkMessageId = null
        )
    }

    override fun sendUnsentMessage(accountIds: Collection<Long>): Flow<SentMsg> {
        val store = storages.messages()
        return store
            .findFirstUnsentMessage(accountIds, withAttachments = true, withForwardMessages = true)
            .flatMapConcat { optional ->
                if (optional.isEmpty) {
                    toFlowThrowable(NotFoundException())
                } else {
                    val entity = optional.get()?.second
                    val accountId = optional.get()?.first
                    if (entity == null || accountId == null) {
                        toFlowThrowable(NotFoundException())
                    } else {
                        val dbid = entity.cmId
                        val peerId = entity.peerId
                        if (isHiddenAccount(accountId) && !Constants.IS_DEBUG) {
                            storages.messages().deleteMessage(accountId, dbid, peerId)
                                .flatMapConcat {
                                    toFlowThrowable(UnsupportedOperationException())
                                }
                        } else {
                            changeMessageStatus(
                                accountId = accountId,
                                dbCmId = dbid,
                                peerId = peerId,
                                status = MessageStatus.SENDING,
                                vkCmId = null,
                                vkMessageId = null
                            )
                                .andThen(
                                    internalSend(accountId, entity)
                                        .flatMapConcat { vkid ->
                                            val patch = PeerPatch(entity.peerId)
                                                .withLastMessage(
                                                    vkid.conversation_message_id,
                                                    vkid.message_id
                                                )
                                                .withUnreadCount(0)
                                            changeMessageStatus(
                                                accountId = accountId,
                                                dbCmId = dbid,
                                                peerId = peerId,
                                                status = MessageStatus.SENT,
                                                vkCmId = vkid.conversation_message_id,
                                                vkMessageId = vkid.message_id
                                            )
                                                .andThen(
                                                    applyPeerUpdatesAndPublish(
                                                        accountId,
                                                        listOf(patch)
                                                    )
                                                )
                                                .map {
                                                    SentMsg(
                                                        dbid,
                                                        vkid.conversation_message_id,
                                                        peerId,
                                                        accountId
                                                    )
                                                }
                                        }
                                        .catch {
                                            changeMessageStatus(
                                                accountId = accountId,
                                                dbCmId = dbid,
                                                peerId = peerId,
                                                status = MessageStatus.ERROR,
                                                vkCmId = null,
                                                vkMessageId = null
                                            ).syncSingleSafe()
                                            throw it
                                        })
                        }
                    }
                }
            }
    }

    override fun searchConversations(
        accountId: Long,
        count: Int,
        q: String?
    ): Flow<List<Conversation>> {
        return networker.vkDefault(accountId)
            .messages()
            .searchConversations(q, count, 1, Fields.FIELDS_BASE_OWNER)
            .flatMapConcat { chattables ->
                val conversations: List<VKApiConversation> =
                    listEmptyIfNull(chattables.conversations)
                val ownerIds: Collection<Long> = if (conversations.nonNullNoEmpty()) {
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
                        FetchMode.ANY,
                        existsOwners
                    )
                    .map { bundle ->
                        val models: MutableList<Conversation> = ArrayList(conversations.size)
                        for (dialog in conversations) {
                            transform(
                                accountId,
                                dialog,
                                bundle,
                                chattables.contacts
                            )?.let { models.add(it) }
                        }
                        models
                    }
            }
    }

    override fun searchMessages(
        accountId: Long,
        peerId: Long?,
        date: String?,
        count: Int,
        offset: Int,
        q: String?
    ): Flow<List<Message>> {
        return networker.vkDefault(accountId)
            .messages()
            .searchMessages(q, peerId, date, offset, count)
            .map { items ->
                listEmptyIfNull(
                    items.items
                )
            }
            .flatMapConcat { dtos ->
                val ids = VKOwnIds().append(dtos)
                ownersRepository
                    .findBaseOwnersDataAsBundle(accountId, ids.all, FetchMode.ANY)
                    .map {
                        val data: MutableList<Message> =
                            ArrayList(dtos.size)
                        for (dto in dtos) {
                            val message = transform(accountId, dto, it)
                            data.add(message)
                        }
                        data
                    }
            }
    }

    override fun getChatUsers(accountId: Long, chatId: Long): Flow<List<AppChatUser>> {
        return networker.vkDefault(accountId)
            .messages()
            .getConversationMembers(Peer.fromChatId(chatId), Fields.FIELDS_BASE_OWNER)
            .flatMapConcat { chatDto ->
                val dtos: List<VKApiConversationMembers> =
                    listEmptyIfNull(chatDto.conversationMembers)
                val ownerIds: Collection<Long> = if (dtos.nonNullNoEmpty()) {
                    val vkOwnIds = VKOwnIds()
                    vkOwnIds.append(accountId)
                    for (dto in dtos) {
                        vkOwnIds.append(dto.member_id)
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
                    FetchMode.ANY,
                    existsOwners
                )
                    .map { ownersBundle ->
                        val models: MutableList<AppChatUser> = ArrayList(dtos.size)
                        for (dto in dtos) {
                            val user =
                                AppChatUser(ownersBundle.getById(dto.member_id), dto.invited_by)
                            user.setCanRemove(dto.can_kick)
                            user.setJoin_date(dto.join_date)
                            user.setAdmin(dto.is_admin)
                            user.setOwner(dto.is_owner)
                            if (user.invitedBy != 0L) {
                                user.setInviter(ownersBundle.getById(user.invitedBy))
                            }
                            models.add(user)
                        }
                        models
                    }
            }
    }

    override fun removeChatMember(accountId: Long, chatId: Long, userId: Long): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .messages()
            .removeChatMember(chatId, userId)
            .ignoreElement()
    }

    override fun deleteChatPhoto(accountId: Long, chatId: Long): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .messages()
            .deleteChatPhoto(chatId)
            .ignoreElement()
    }

    override fun addChatUsers(
        accountId: Long,
        chatId: Long,
        users: List<User>
    ): Flow<List<AppChatUser>> {
        val api = networker.vkDefault(accountId).messages()
        return ownersRepository.getBaseOwnerInfo(accountId, accountId, FetchMode.ANY)
            .flatMapConcat { iam ->
                var completable = emptyTaskFlow()
                val data: MutableList<AppChatUser> = ArrayList()
                for (user in users) {
                    completable =
                        completable.andThen(
                            api.addChatUser(chatId, user.getOwnerObjectId()).ignoreElement()
                        )
                    val chatUser = AppChatUser(user, accountId)
                        .setCanRemove(true)
                        .setInviter(iam)
                    data.add(chatUser)
                }
                completable
                    .map {
                        data
                    }
            }
    }

    override fun deleteConversation(accountId: Long, peedId: Long): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .messages()
            .deleteConversation(peedId)
            .flatMapConcat {
                storages.dialogs()
                    .removePeerWithId(accountId, peedId)
                    .andThen(
                        storages.messages().insertPeerDbos(accountId, peedId, emptyList(), true)
                    )
            }
            .map {
                peerDeletingPublisher.emit(PeerDeleting(accountId, peedId))
                true
            }
    }

    override fun deleteMessages(
        accountId: Long,
        peerId: Long,
        cmIds: Collection<Int>,
        deleteForAll: Boolean,
        spam: Boolean?
    ): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .messages()
            .deleteMessages(peerId, cmIds, deleteForAll, spam)
            .flatMapConcat {
                val patches: MutableList<MessagePatch> = ArrayList(it.size)
                for (i in it) {
                    if (i.response == 1) {
                        val patch = MessagePatch(i.conversation_message_id, peerId)
                        patch.deletion = MessagePatch.Deletion(true, deleteForAll)
                        patches.add(patch)
                    }
                }
                applyMessagesPatchesAndPublish(accountId, patches)
            }
    }

    override fun getReactionsAssets(accountId: Long): Flow<List<ReactionAsset>> {
        if (Utils.needFetchReactionAssets(accountId)) {
            return networker.vkDefault(accountId)
                .messages()
                .getReactionsAssets()
                .flatMapConcat { ndtos ->
                    val dtos: List<VKApiReactionAsset> = listEmptyIfNull(ndtos.assets)
                    val dbos: List<ReactionAssetEntity> =
                        mapAll(dtos) { Dto2Entity.mapReactionAsset(it) }
                    val models: List<ReactionAsset> =
                        mapAll(dtos) { Dto2Model.transformReactionAsset(it) }

                    if (dtos.isEmpty()) {
                        Settings.get().main().del_last_reaction_assets_sync(accountId)
                        toFlow(models)
                    } else {
                        storages.tempStore().addReactionsAssets(accountId, dbos)
                            .map {
                                models
                            }
                    }
                }
        } else {
            return storages.tempStore().getReactionsAssets(accountId).map {
                val dbos: List<ReactionAssetEntity> = listEmptyIfNull(it)
                val models: List<ReactionAsset> =
                    mapAll(dbos) { st -> Entity2Model.buildReactionAssetFromDbo(st) }
                models
            }
        }
    }

    override fun sendOrDeleteReaction(
        accountId: Long,
        peerId: Long, cmId: Int, reactionId: Int?
    ): Flow<Boolean> {
        if (isHiddenAccount(accountId) && !Constants.IS_DEBUG) {
            throw UnsupportedOperationException()
        }
        return networker.vkDefault(accountId)
            .messages()
            .sendOrDeleteReaction(peerId, cmId, reactionId)
            .ignoreElement()
    }

    override fun pinUnPinConversation(accountId: Long, peerId: Long, pin: Boolean): Flow<Boolean> {
        if (isHiddenAccount(accountId) && !Constants.IS_DEBUG) {
            throw UnsupportedOperationException()
        }
        return networker.vkDefault(accountId)
            .messages()
            .pinUnPinConversation(peerId, pin)
    }

    override fun markAsListened(accountId: Long, peerId: Long, cmId: Int): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .messages()
            .markAsListened(peerId, cmId)
    }

    override fun markAsImportant(
        accountId: Long,
        peerId: Long,
        ids: Collection<Int>,
        important: Int?
    ): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .messages()
            .markAsImportant(ids, important)
            .ignoreElement()
    }

    private fun applyMessagesPatchesAndPublish(
        accountId: Long,
        patches: List<MessagePatch>
    ): Flow<Boolean> {
        val updates: MutableList<MessageUpdate> = ArrayList(patches.size)
        val requireInvalidate: MutableSet<PeerId> = HashSet(0)
        for (patch in patches) {
            updates.add(patch2Update(accountId, patch))
            if (patch.deletion != null) {
                requireInvalidate.add(PeerId(accountId, patch.peerId))
            }
        }
        var afterApply = emptyTaskFlow()
        val invalidatePeers: MutableList<Flow<Boolean>> = LinkedList()
        for (pair in requireInvalidate) {
            invalidatePeers.add(invalidatePeerLastMessage(pair.accountId, pair.peerId))
        }
        if (invalidatePeers.isNotEmpty()) {
            afterApply = invalidatePeers.mergeFlows()
        }
        return storages.messages()
            .applyPatches(accountId, patches)
            .andThen(afterApply)
            .map {
                for (i in updates) {
                    messageUpdatesPublisher.emit(i)
                }
                true
            }
    }

    private fun invalidatePeerLastMessage(accountId: Long, peerId: Long): Flow<Boolean> {
        return storages.messages()
            .findLastSentCmIdForPeer(accountId, peerId)
            .flatMapConcat {
                if (it == null) {
                    val deleting = PeerDeleting(accountId, peerId)
                    storages.dialogs().removePeerWithId(accountId, peerId)
                        .map {
                            peerDeletingPublisher.emit(deleting)
                            true
                        }
                } else {
                    val patch = PeerPatch(peerId).withLastMessage(it.first, it.second)
                    applyPeerUpdatesAndPublish(accountId, listOf(patch))
                }
            }
    }

    override fun restoreMessage(accountId: Long, peerId: Long, cmId: Int): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .messages()
            .restoreMessage(peerId, cmId)
            .flatMapConcat {
                val patch = MessagePatch(cmId, peerId)
                patch.deletion = MessagePatch.Deletion(deleted = false, deletedForAll = false)
                applyMessagesPatchesAndPublish(accountId, listOf(patch))
            }
    }

    override fun editChat(accountId: Long, chatId: Long, title: String?): Flow<Boolean> {
        val patch = PeerPatch(Peer.fromChatId(chatId)).withTitle(title)
        return networker.vkDefault(accountId)
            .messages()
            .editChat(chatId, title)
            .flatMapConcat {
                applyPeerUpdatesAndPublish(
                    accountId,
                    listOf(patch)
                )
            }
    }

    override fun createGroupChat(
        accountId: Long,
        users: Collection<Long>,
        title: String?
    ): Flow<Long> {
        return networker.vkDefault(accountId)
            .messages()
            .createChat(users, title)
    }

    override fun recogniseAudioMessage(
        accountId: Long,
        audioMessageId: String?,
        cmId: Int,
        peerId: Long
    ): Flow<Int> {
        return networker.vkDefault(accountId)
            .messages()
            .recogniseAudioMessage(audioMessageId, cmId, peerId)
    }

    override fun setMemberRole(
        accountId: Long,
        chatId: Long,
        memberId: Long,
        isAdmin: Boolean
    ): Flow<Boolean> {
        return networker.vkDefault(accountId)
            .messages()
            .setMemberRole(Peer.fromChatId(chatId), memberId, if (isAdmin) "admin" else "member")
            .ignoreElement()
    }

    override fun markAsRead(accountId: Long, peerId: Long, startMessageId: Int): Flow<Boolean> {
        val patch = PeerPatch(peerId).withInReadCmId(startMessageId).withUnreadCount(0)
        return networker.vkDefault(accountId)
            .messages()
            .markAsRead(peerId, startMessageId)
            .flatMapConcat {
                applyPeerUpdatesAndPublish(
                    accountId,
                    listOf(patch)
                )
            }
    }

    override fun pinMessage(
        accountId: Long,
        peerId: Long,
        message: Message?
    ): Flow<Boolean> {
        val update = PeerUpdate(accountId, peerId)
        update.pin = PeerUpdate.Pin(message)
        val apiCompletable = if (message == null) {
            networker.vkDefault(accountId)
                .messages()
                .unpinMessage(peerId)
        } else {
            networker.vkDefault(accountId)
                .messages()
                .pinMessage(peerId, message.cmId)
        }
        val patch = PeerPatch(peerId)
            .withPin(if (message == null) null else buildMessageEntity(message))
        return apiCompletable
            .andThen(storages.dialogs().applyPatches(accountId, listOf(patch)))
            .map {
                peerUpdatePublisher.emit(listOf(update))
                true
            }
    }

    override fun getReactedPeers(
        accountId: Long,
        peerId: Long,
        cmId: Int,
        reactionId: Int?
    ): Flow<List<ReactedMessagesPeers>> {
        return networker.vkDefault(accountId)
            .messages()
            .getReactedPeers(peerId, cmId, reactionId, true, Fields.FIELDS_BASE_OWNER)
            .flatMapConcat {
                val dtos: List<VKApiReactedMessagesPeers> =
                    listEmptyIfNull(it.reactions)
                val ownerIds: Collection<Long> = if (dtos.nonNullNoEmpty()) {
                    val vkOwnIds = VKOwnIds()
                    for (dto in dtos) {
                        vkOwnIds.append(dto.userId)
                    }
                    vkOwnIds.all
                } else {
                    emptyList()
                }
                val existsOwners = transformOwners(it.profiles, it.groups)
                ownersRepository.findBaseOwnersDataAsBundle(
                    accountId,
                    ownerIds,
                    FetchMode.ANY,
                    existsOwners
                )
                    .map { ownersBundle ->
                        val models: MutableList<ReactedMessagesPeers> = ArrayList(dtos.size)
                        for (dto in dtos) {
                            val reacted =
                                ReactedMessagesPeers()
                            reacted.setReactionId(dto.reactionId)
                            reacted.setOwnerId(dto.userId)
                            reacted.setOwner(ownersBundle.findById(dto.userId))
                            models.add(reacted)
                        }
                        models
                    }
            }
    }

    override fun getHistoryAttachments(
        accountId: Long,
        peerId: Long,
        mediaType: String?,
        startFrom: String?,
        count: Int?,
        photoSizes: Boolean?,
        preserveOrder: Boolean?,
        maxForwardsLevel: Int?
    ): Flow<AttachmentsHistoryResponse> {
        return networker.vkDefault(accountId)
            .messages()
            .getHistoryAttachments(
                peerId,
                mediaType,
                startFrom,
                count,
                photoSizes,
                preserveOrder,
                maxForwardsLevel,
                null,
                null
            )
    }

    override fun getChatById(accountId: Long, peerId: Long): Flow<Chat> {
        return storages.dialogs()
            .findChatById(accountId, peerId)
            .flatMapConcat { optional ->
                if (optional.nonEmpty()) {
                    toFlow(optional.requireNonEmpty())
                } else {
                    val chatId = Peer.toChatId(peerId)
                    networker.vkDefault(accountId)
                        .messages()
                        .getChat(chatId, null, null, null)
                        .map { chats ->
                            if (chats.isEmpty()) {
                                throw NotFoundException()
                            }
                            chats[0]
                        }
                        .map { transform(it) }
                }
            }
    }

    private fun internalSend(accountId: Long, dbo: MessageDboEntity): Flow<SendMessageResponse> {
        if (dbo.extras.isNullOrEmpty() && dbo.getAttachments()
                .isNullOrEmpty() && dbo.forwardMessages.isNullOrEmpty()
        ) {
            return networker.vkDefault(accountId)
                .messages()
                .send(
                    dbo.makeRandomId(),
                    dbo.peerId,
                    null,
                    dbo.text,
                    null,
                    null,
                    null,
                    null,
                    null,
                    dbo.payload
                )
        }
        val attachments: MutableCollection<IAttachmentToken> = LinkedList()
        try {
            dbo.getAttachments().nonNullNoEmpty { att ->
                for (a in att) {
                    if (a is StickerDboEntity) {
                        val stickerId = a.id
                        val fwds = checkForwardMessages(dbo)

                        return networker.vkDefault(accountId)
                            .messages()
                            .send(
                                dbo.makeRandomId(),
                                dbo.peerId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                fwds,
                                stickerId,
                                dbo.payload
                            )
                    }
                    attachments.add(createToken(a))
                }
            }
        } catch (e: Exception) {
            return toFlowThrowable(e)
        }
        return checkVoiceMessage(accountId, dbo)
            .flatMapConcat { optionalToken ->
                if (optionalToken.nonEmpty()) {
                    attachments.add(optionalToken.requireNonEmpty())
                }
                val fwds = checkForwardMessages(dbo)
                networker.vkDefault(accountId)
                    .messages()
                    .send(
                        dbo.makeRandomId(),
                        dbo.peerId,
                        null,
                        dbo.text,
                        null,
                        null,
                        attachments,
                        fwds,
                        null,
                        dbo.payload
                    )
            }
    }

    private fun checkForwardMessages(dbo: MessageDboEntity): String? {
        val forwards = dbo.forwardMessages
        if (forwards.isNullOrEmpty()) {
            return null
        }
        val ids = ArrayList<Int>(forwards.size)
        var fromPeer: Long = 0
        var isFirst = true
        for (i in dbo.forwardMessages.orEmpty()) {
            val tmpFromPeer = i.peerId
            if (isFirst) {
                isFirst = false
                fromPeer = tmpFromPeer
            }
            ids.add(i.cmId)
        }
        val resultObj = MessageForwardSend().setIsReply(ids.size == 1 && dbo.peerId == fromPeer)
            .setPeerId(fromPeer)
            .setCmIds(ids)
        return kJsonNotPretty.encodeToString(resultObj)
    }

    private fun checkVoiceMessage(
        accountId: Long,
        dbo: MessageDboEntity
    ): Flow<Optional<IAttachmentToken>> {
        val extras = dbo.extras
        if (extras != null && extras.containsKey(Message.Extra.VOICE_RECORD)) {
            val filePath = extras[Message.Extra.VOICE_RECORD]
            val docsApi = networker.vkDefault(accountId).docs()

            return docsApi.getMessagesUploadServer(dbo.peerId, "audio_message")
                .flatMapConcat { server ->
                    val file = File(filePath ?: throw NotFoundException("filePath is empty"))
                    var inputSource: Source? = null
                    try {
                        inputSource = file.source()
                        networker.uploads()
                            .uploadDocumentRx(
                                server.url ?: throw NotFoundException("Upload url empty!"),
                                file.name,
                                inputSource,
                                file.length(),
                                null
                            )
                            .onCompletion { safelyClose(inputSource) }
                            .flatMapConcat { uploadDto ->
                                docsApi
                                    .save(uploadDto.file, null, null)
                                    .map { dtos ->
                                        if (dtos.type.isEmpty()) {
                                            throw NotFoundException("Unable to save voice message")
                                        } else {
                                            val dto = dtos.doc
                                            val token = AttachmentsTokenCreator.ofDocument(
                                                dto.id,
                                                dto.ownerId,
                                                dto.accessKey
                                            )
                                            Optional.wrap(token)
                                        }
                                    }
                            }
                    } catch (e: FileNotFoundException) {
                        safelyClose(inputSource)
                        toFlowThrowable(e)
                    }
                }
        }
        return toFlow(Optional.empty())
    }

    private fun getTargetMessageStatus(builder: SaveMessageBuilder): Flow<Int> {
        val accountId = builder.accountId
        val destination =
            forMessage(builder.draftCmId ?: return toFlow(MessageStatus.QUEUE), builder.peerId)
        return uploadManager[accountId, destination]
            .map { uploads ->
                if (uploads.isEmpty()) {
                    MessageStatus.QUEUE
                } else {
                    var uploadingNow = false
                    var hasError = false
                    for (o in uploads) {
                        if (o.status == Upload.STATUS_CANCELLING) {
                            continue
                        }
                        if (o.status == Upload.STATUS_ERROR) {
                            hasError = true
                            break
                        } else {
                            uploadingNow = true
                        }
                    }
                    if (hasError) {
                        throw UploadNotResolvedException()
                    } else {
                        if (uploadingNow) MessageStatus.WAITING_FOR_UPLOAD else MessageStatus.QUEUE
                    }
                }
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

    private class PeerId(val accountId: Long, val peerId: Long) {
        override fun equals(other: Any?): Boolean {
            return other is PeerId && accountId == other.accountId && peerId == other.peerId
        }

        override fun hashCode(): Int {
            var result = accountId.hashCode()
            result = 31 * result + peerId.hashCode()
            return result
        }
    }

    companion object {
        private val DTO_TO_DBO: suspend (List<VKApiMessage>) -> List<MessageDboEntity> = {
            val dbos: MutableList<MessageDboEntity> = ArrayList(it.size)
            for (dto in it) {
                dbos.add(mapMessage(dto))
            }
            dbos
        }

        internal fun entity2Model(
            accountId: Long,
            entity: PeerDialogEntity,
            owners: IOwnersBundle
        ): Conversation {
            return Conversation(entity.peerId)
                .setInReadCmId(entity.inReadCmId)
                .setOutReadCmId(entity.outReadCmId)
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
                    entity.pinned?.let {
                        message(
                            accountId,
                            it,
                            owners
                        )
                    }
                )
                .setAcl(entity.acl)
                .setGroupChannel(entity.isGroupChannel)
                .setCurrentKeyboard(buildKeyboardFromDbo(entity.currentKeyboard))
                .setMajorId(entity.majorId)
        }

        internal fun patch2Update(accountId: Long, patch: MessagePatch): MessageUpdate {
            val update = MessageUpdate(accountId, patch.cmId, patch.peerId)
            patch.deletion.requireNonNull {
                update.setDeleteUpdate(MessageUpdate.DeleteUpdate(it.deleted, it.deletedForAll))
            }
            patch.important.requireNonNull {
                update.setImportantUpdate(ImportantUpdate(it.important))
            }
            patch.reaction.requireNonNull {
                update.setReactionUpdate(
                    MessageUpdate.ReactionUpdate(
                        it.keepMyReaction,
                        it.reactionId,
                        it.reactions
                    )
                )
            }
            return update
        }
    }

    init {
        compositeJob.add(
            uploadManager.observeResults()
                .filter { it.first.destination.method == Method.TO_MESSAGE }
                .sharedFlowToMain {
                    onUploadSuccess(
                        it.first
                    )
                }
        )
        /*
        compositeJob.add(
            accountsSettings.observeRegistered
                .sharedFlowToMain {
                    onAccountsChanged()
                }
        )
         */
    }
}