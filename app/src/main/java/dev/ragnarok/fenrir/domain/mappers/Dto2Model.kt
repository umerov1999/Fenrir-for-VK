package dev.ragnarok.fenrir.domain.mappers

import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.VKApiAudioCatalog.VKApiArtistBlock
import dev.ragnarok.fenrir.api.model.VKApiSticker.VKApiAnimation
import dev.ragnarok.fenrir.api.model.VkApiConversation.CurrentKeyboard
import dev.ragnarok.fenrir.api.model.feedback.Copies
import dev.ragnarok.fenrir.api.model.feedback.UserArray
import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate
import dev.ragnarok.fenrir.api.model.response.CatalogResponse
import dev.ragnarok.fenrir.api.model.response.FavePageResponse
import dev.ragnarok.fenrir.api.util.VKStringUtils.unescape
import dev.ragnarok.fenrir.crypt.CryptHelper.analizeMessageBody
import dev.ragnarok.fenrir.crypt.MessageType
import dev.ragnarok.fenrir.domain.mappers.MapUtil.calculateConversationAcl
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAllMutable
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.model.AudioArtist.AudioArtistImage
import dev.ragnarok.fenrir.model.AudioCatalog.ArtistBlock
import dev.ragnarok.fenrir.model.Document.VideoPreview
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.safeCountOf

object Dto2Model {

    fun transform(dto: VkApiFriendList): FriendList {
        return FriendList(dto.id, dto.name)
    }

    fun transform(dto: VKApiPhotoAlbum): PhotoAlbum {
        return PhotoAlbum(dto.id, dto.owner_id)
            .setSize(dto.size)
            .setTitle(dto.title)
            .setDescription(dto.description)
            .setCanUpload(dto.can_upload)
            .setUpdatedTime(dto.updated)
            .setCreatedTime(dto.created)
            .setSizes(if (dto.photo != null) transform(dto.photo) else PhotoSizes.empty())
            .setUploadByAdminsOnly(dto.upload_by_admins_only)
            .setCommentsDisabled(dto.comments_disabled)
            .setPrivacyView(if (dto.privacy_view != null) transform(dto.privacy_view) else null)
            .setPrivacyComment(if (dto.privacy_comment != null) transform(dto.privacy_comment) else null)
    }


    fun transform(chat: VKApiChat): Chat {
        return Chat(chat.id)
            .setPhoto50(chat.photo_50)
            .setPhoto100(chat.photo_100)
            .setPhoto200(chat.photo_200)
            .setTitle(chat.title)
    }

    fun transform(block: VKApiArtistBlock?): ArtistBlock? {
        if (block == null) return null
        var url: String? = null
        if (!block.images.isNullOrEmpty()) {
            var def = 0
            for (i in block.images) {
                if (i.width * i.height > def) {
                    def = i.width * i.height
                    url = i.url
                }
            }
        }
        return ArtistBlock()
            .setName(block.name)
            .setPhoto(url)
    }

    private fun transformStickerImage(dto: VKApiSticker.Image): Sticker.Image {
        return Sticker.Image(dto.url, dto.width, dto.height)
    }

    private fun transformStickerAnimation(dto: VKApiAnimation): Sticker.Animation {
        return Sticker.Animation(dto.url, dto.type)
    }

    private fun transformSticker(sticker: VKApiSticker): Sticker {
        return Sticker(sticker.sticker_id)
            .setImages(
                mapAll(
                    sticker.images
                ) {
                    transformStickerImage(it)
                }
            )
            .setImagesWithBackground(
                mapAll(
                    sticker.images_with_background
                ) { transformStickerImage(it) })
            .setAnimations(
                mapAll(
                    sticker.animations
                ) {
                    transformStickerAnimation(it)
                }
            )
            .setAnimationUrl(sticker.animation_url)
    }

    fun transformStickers(dto: List<VKApiSticker>?): List<Sticker> {
        return mapAll(
            dto
        ) {
            transformSticker(it)
        }
    }

    private fun transformAudios(dto: List<VKApiAudio>?): List<Audio> {
        return mapAll(
            dto
        ) {
            transform(it)
        }
    }

    private fun transformAudioPlaylists(dto: List<VKApiAudioPlaylist>?): MutableList<AudioPlaylist> {
        return mapAllMutable(
            dto
        ) {
            transform(it)
        }
    }

    private fun transformCatalogLinks(dto: List<VKApiCatalogLink>?): List<Link> {
        return mapAll(
            dto
        ) { transform(it) }
    }


    fun transformOwner(owner: VKApiOwner?): Owner {
        return if (owner is VKApiUser) transformUser(owner) else transformCommunity(
            owner as VKApiCommunity
        )
    }


    fun transformOwners(
        users: Collection<VKApiUser>?,
        communities: Collection<VKApiCommunity>?
    ): List<Owner> {
        val owners: MutableList<Owner> = ArrayList(safeCountOf(users) + safeCountOf(communities))
        if (users != null) {
            for (user in users) {
                owners.add(transformUser(user))
            }
        }
        if (communities != null) {
            for (community in communities) {
                owners.add(transformCommunity(community))
            }
        }
        return owners
    }

    fun transformCommunityDetails(dto: VKApiCommunity): CommunityDetails {
        val details = CommunityDetails()
            .setCanMessage(dto.can_message)
            .setStatus(dto.status)
            .setStatusAudio(if (dto.status_audio != null) transform(dto.status_audio) else null)
            .setFavorite(dto.is_favorite)
            .setSubscribed(dto.is_subscribed)
        if (dto.counters != null) {
            details.setAllWallCount(dto.counters.all_wall)
                .setOwnerWallCount(dto.counters.owner_wall)
                .setPostponedWallCount(dto.counters.postponed_wall)
                .setSuggestedWallCount(dto.counters.suggest_wall)
                .setTopicsCount(dto.counters.topics)
                .setDocsCount(dto.counters.docs)
                .setPhotosCount(dto.counters.photos)
                .setAudiosCount(dto.counters.audios)
                .setVideosCount(dto.counters.videos)
                .setProductsCount(dto.counters.market)
                .setArticlesCount(dto.counters.articles).chatsCount = dto.counters.chats
        }
        if (dto.cover != null) {
            val cover = CommunityDetails.Cover()
                .setEnabled(dto.cover.enabled)
                .setImages(ArrayList(safeCountOf(dto.cover.images)))
            if (dto.cover.images != null) {
                for (imageDto in dto.cover.images) {
                    cover.images.add(
                        CommunityDetails.CoverImage(
                            imageDto.url,
                            imageDto.height,
                            imageDto.width
                        )
                    )
                }
            }
            details.cover = cover
        } else {
            details.cover = CommunityDetails.Cover().setEnabled(false)
        }
        details.description = dto.description
        return details
    }

    private fun transformGroupChat(chats: VKApiGroupChats): GroupChats {
        return GroupChats(chats.id)
            .setInvite_link(chats.invite_link)
            .setIs_closed(chats.is_closed)
            .setPhoto(chats.photo)
            .setTitle(chats.title)
            .setMembers_count(chats.members_count)
            .setLastUpdateTime(chats.last_message_date)
    }


    fun transformCommunity(community: VKApiCommunity?): Community {
        return Community(community!!.id)
            .setName(community.name)
            .setScreenName(community.screen_name)
            .setClosed(community.is_closed)
            .setVerified(community.verified)
            .setAdmin(community.is_admin)
            .setAdminLevel(community.admin_level)
            .setMember(community.is_member)
            .setMemberStatus(community.member_status)
            .setType(community.type)
            .setPhoto50(community.photo_50)
            .setPhoto100(community.photo_100)
            .setPhoto200(community.photo_200)
            .setMembersCount(community.members_count)
    }

    fun transform(dto: VKApiGiftItem): GiftItem {
        return GiftItem(dto.id)
            .setThumb48(dto.thumb_48)
            .setThumb96(dto.thumb_96)
            .setThumb256(dto.thumb_256)
    }

    fun transform(dto: VKApiGift): Gift {
        return Gift(dto.id)
            .setFromId(dto.from_id)
            .setMessage(dto.message)
            .setDate(dto.date)
            .setGiftItem(if (dto.gift != null) transform(dto.gift) else null)
            .setPrivacy(dto.privacy)
    }


    fun transformCommunities(dtos: List<VKApiCommunity>?): List<Community> {
        return mapAll(
            dtos
        ) {
            transformCommunity(it)
        }
    }


    fun transformGroupChats(dtos: List<VKApiGroupChats>?): List<GroupChats> {
        return mapAll(
            dtos
        ) {
            transformGroupChat(it)
        }
    }


    fun transformGifts(dtos: List<VKApiGift>?): List<Gift> {
        return mapAll(
            dtos
        ) { transform(it) }
    }


    fun transformMarketAlbums(dtos: List<VkApiMarketAlbum>?): List<MarketAlbum> {
        return mapAll(
            dtos
        ) {
            transform(it)
        }
    }

    fun transformAudioArtist(dtos: List<VKApiAudioArtist>?): List<AudioArtist> {
        return mapAll(
            dtos
        ) {
            transform(it)
        }
    }


    fun transformMarket(dtos: List<VkApiMarket>?): List<Market> {
        return mapAll(
            dtos
        ) { transform(it) }
    }


    fun transformUsers(dtos: List<VKApiUser>?): List<User> {
        return mapAll(
            dtos
        ) { transformUser(it) }
    }

    private fun transformVideos(dtos: List<VKApiVideo>?): List<Video> {
        return mapAll(
            dtos
        ) { transform(it) }
    }


    fun transformFaveUser(favePage: FavePageResponse): FavePage {
        var id = 0
        when (favePage.type) {
            FavePageType.USER -> id = favePage.user.id
            FavePageType.COMMUNITY -> id = favePage.group.id
        }
        val page = FavePage(id)
            .setDescription(favePage.description)
            .setFaveType(favePage.type)
            .setUpdatedDate(favePage.updated_date)
        if (favePage.user != null) {
            page.user = transformUser(favePage.user)
        }
        if (favePage.group != null) {
            page.group = transformCommunity(favePage.group)
        }
        return page
    }


    fun transformUser(user: VKApiUser?): User {
        return User(user!!.id)
            .setFirstName(user.first_name)
            .setLastName(user.last_name)
            .setOnline(user.online)
            .setOnlineMobile(user.online_mobile)
            .setOnlineApp(user.online_app)
            .setPhoto50(user.photo_50)
            .setPhoto100(user.photo_100)
            .setPhoto200(user.photo_200)
            .setPhotoMax(user.photo_max_orig)
            .setLastSeen(user.last_seen)
            .setPlatform(user.platform)
            .setStatus(user.status)
            .setSex(user.sex)
            .setDomain(user.domain)
            .setFriend(user.is_friend)
            .setFriendStatus(user.friend_status)
            .setCanWritePrivateMessage(user.can_write_private_message)
            .setBlacklisted(user.blacklisted)
            .setBlacklisted_by_me(user.blacklisted_by_me)
            .setCan_access_closed(user.can_access_closed)
            .setVerified(user.verified)
            .setMaiden_name(user.maiden_name)
    }

    fun transform(accountUid: Int, update: AddMessageUpdate): VKApiMessage {
        val message = VKApiMessage()
        message.id = update.message_id
        message.out = update.outbox
        message.important = update.important
        message.deleted = update.deleted
        //message.read_state = !update.unread;
        message.peer_id = update.peer_id
        message.from_id =
            if (message.out) accountUid else if (Peer.isGroupChat(update.peer_id)) update.from else update.peer_id
        message.body = unescape(update.text)
        //message.title = update.subject;
        message.date = update.timestamp
        message.action_mid = update.sourceMid
        message.action_text = update.sourceText
        message.action = update.sourceAct
        message.random_id = update.random_id
        message.keyboard = update.keyboard
        message.payload = update.payload
        message.update_time = update.edit_time
        return message
    }

    fun transform(accountId: Int, dtos: List<VkApiDialog>, owners: IOwnersBundle): List<Dialog> {
        val data: MutableList<Dialog> = ArrayList(dtos.size)
        for (dto in dtos) {
            data.add(transform(accountId, dto, owners))
        }
        return data
    }

    private fun mapKeyboard(keyboard: CurrentKeyboard?): Keyboard? {
        if (keyboard == null || keyboard.buttons.isNullOrEmpty()) {
            return null
        }
        val buttons: MutableList<List<Keyboard.Button>> = ArrayList()
        for (i in keyboard.buttons) {
            val v: MutableList<Keyboard.Button> = ArrayList()
            for (s in i) {
                if (s.action == null || "text" != s.action.type && "open_link" != s.action.type) {
                    continue
                }
                v.add(
                    Keyboard.Button().setType(s.action.type).setColor(s.color)
                        .setLabel(s.action.label).setLink(s.action.link)
                        .setPayload(s.action.payload)
                )
            }
            buttons.add(v)
        }
        return if (!buttons.isNullOrEmpty()) {
            Keyboard().setAuthor_id(keyboard.author_id)
                .setInline(keyboard.inline)
                .setOne_time(keyboard.one_time)
                .setButtons(buttons)
        } else null
    }


    fun transform(accountId: Int, dto: VkApiConversation, bundle: IOwnersBundle): Conversation {
        val entity = Conversation(dto.peer.id)
            .setInRead(dto.inRead)
            .setOutRead(dto.outRead)
            .setUnreadCount(dto.unreadCount)
            .setAcl(calculateConversationAcl(dto))
        if (!Peer.isGroupChat(dto.peer.id)) {
            val own = bundle.getById(dto.peer.id)
            entity.title = own.fullName
            entity.photo50 = own.get100photoOrSmaller()
            entity.photo100 = own.get100photoOrSmaller()
            entity.photo200 = own.maxSquareAvatar
        }
        if (dto.settings != null) {
            entity.title = dto.settings.title
            if (dto.settings.pinnedMesage != null) {
                entity.pinned = transform(accountId, dto.settings.pinnedMesage, bundle)
            }
            if (dto.settings.photo != null) {
                entity.setPhoto50(dto.settings.photo.photo50)
                    .setPhoto100(dto.settings.photo.photo100).photo200 = dto.settings.photo.photo200
            }
        }
        if (dto.sort_id != null) {
            entity.major_id = dto.sort_id.major_id
            entity.minor_id = dto.sort_id.minor_id
        }
        entity.currentKeyboard = mapKeyboard(dto.current_keyboard)
        return entity
    }


    fun transform(accountId: Int, dto: VkApiDialog, bundle: IOwnersBundle): Dialog {
        val message = dto.lastMessage
        val interlocutor: Owner = if (Peer.isGroup(message.peer_id) || Peer.isUser(
                message.peer_id
            )
        ) {
            bundle.getById(message.peer_id)
        } else {
            bundle.getById(message.from_id)
        }
        val dialog = Dialog()
            .setPeerId(message.peer_id)
            .setUnreadCount(dto.conversation.unreadCount)
            .setInRead(dto.conversation.inRead)
            .setOutRead(dto.conversation.outRead)
            .setMessage(transform(accountId, message, bundle))
            .setLastMessageId(message.id)
            .setInterlocutor(interlocutor)
        if (dto.conversation.settings != null) {
            dialog.title = dto.conversation.settings.title
            dialog.isGroupChannel = dto.conversation.settings.is_group_channel
            if (dto.conversation.settings.photo != null) {
                dialog.setPhoto50(dto.conversation.settings.photo.photo50)
                    .setPhoto100(dto.conversation.settings.photo.photo100).photo200 =
                    dto.conversation.settings.photo.photo200
            }
        }
        if (dto.conversation.sort_id != null) {
            dialog.major_id = dto.conversation.sort_id.major_id
            dialog.minor_id = dto.conversation.sort_id.minor_id
        }
        return dialog
    }


    fun transformMessages(
        aid: Int,
        dtos: List<VKApiMessage>,
        owners: IOwnersBundle
    ): List<Message> {
        val data: MutableList<Message> = ArrayList(dtos.size)
        for (dto in dtos) {
            data.add(transform(aid, dto, owners))
        }
        return data
    }

    private fun transformKeyboard(keyboard: CurrentKeyboard?): Keyboard? {
        if (keyboard == null || keyboard.buttons.isNullOrEmpty()) {
            return null
        }
        val buttons: MutableList<List<Keyboard.Button>> = ArrayList()
        for (i in keyboard.buttons) {
            val v: MutableList<Keyboard.Button> = ArrayList()
            for (s in i) {
                if (s.action == null || "text" != s.action.type && "open_link" != s.action.type) {
                    continue
                }
                v.add(
                    Keyboard.Button().setType(s.action.type).setColor(s.color)
                        .setLabel(s.action.label).setLink(s.action.link)
                        .setPayload(s.action.payload)
                )
            }
            buttons.add(v)
        }
        return if (!buttons.isNullOrEmpty()) {
            Keyboard().setAuthor_id(keyboard.author_id)
                .setInline(keyboard.inline)
                .setOne_time(keyboard.one_time)
                .setButtons(buttons)
        } else null
    }


    fun transform(aid: Int, message: VKApiMessage, owners: IOwnersBundle): Message {
        val encrypted = analizeMessageBody(message.body) == MessageType.CRYPTED
        val appMessage = Message(message.id)
            .setAccountId(aid)
            .setBody(message.body) //.setTitle(message.title)
            .setPeerId(message.peer_id)
            .setSenderId(message.from_id) //.setRead(message.read_state)
            .setOut(message.out)
            .setStatus(MessageStatus.SENT)
            .setDate(message.date)
            .setHasAttachments(message.attachments != null && message.attachments.nonEmpty())
            .setForwardMessagesCount(safeCountOf(message.fwd_messages))
            .setDeleted(message.deleted)
            .setDeletedForAll(false) // cant be deleted from api?
            .setOriginalId(message.id)
            .setCryptStatus(if (encrypted) CryptStatus.ENCRYPTED else CryptStatus.NO_ENCRYPTION)
            .setImportant(message.important)
            .setAction(Message.fromApiChatAction(message.action))
            .setActionMid(message.action_mid)
            .setActionEmail(message.action_email)
            .setActionText(message.action_text)
            .setPhoto50(message.action_photo_50)
            .setPhoto100(message.action_photo_100)
            .setPhoto200(message.action_photo_200)
            .setSender(owners.getById(message.from_id))
            .setPayload(message.payload)
            .setKeyboard(transformKeyboard(message.keyboard))
        if (message.action_mid != 0) {
            appMessage.setActionUser(owners.getById(message.action_mid))
        }
        if (message.attachments != null && !message.attachments.isEmpty) {
            appMessage.attachments = buildAttachments(message.attachments, owners)
        }
        if (message.fwd_messages.nonNullNoEmpty()) {
            for (fwd in message.fwd_messages) {
                appMessage.prepareFwd(message.fwd_messages.size).add(transform(aid, fwd, owners))
            }
        }
        if (message.random_id.nonNullNoEmpty()) {
            try {
                appMessage.randomId = message.random_id.toInt()
            } catch (ignored: NumberFormatException) {
            }
        }
        return appMessage
    }

    fun transform(dto: VkApiDoc.Graffiti): Document.Graffiti {
        return Document.Graffiti()
            .setWidth(dto.width)
            .setHeight(dto.height)
            .setSrc(dto.src)
    }

    fun transform(dto: VkApiDoc.Video): VideoPreview {
        return VideoPreview()
            .setHeight(dto.height)
            .setSrc(dto.src)
            .setWidth(dto.width)
    }

    private fun dto2model(dto: PhotoSizeDto): PhotoSizes.Size {
        return PhotoSizes.Size(dto.width, dto.height, dto.url)
    }


    fun transform(dtos: List<PhotoSizeDto>?): PhotoSizes {
        val sizes = PhotoSizes()
        if (dtos != null) {
            for (dto in dtos) {
                when (dto.type) {
                    PhotoSizeDto.Type.S -> sizes.s = dto2model(dto)
                    PhotoSizeDto.Type.M -> sizes.m = dto2model(dto)
                    PhotoSizeDto.Type.X -> sizes.x = dto2model(dto)
                    PhotoSizeDto.Type.Y -> sizes.y = dto2model(dto)
                    PhotoSizeDto.Type.Z -> sizes.z = dto2model(dto)
                    PhotoSizeDto.Type.W -> sizes.w = dto2model(dto)
                    PhotoSizeDto.Type.O -> sizes.o = dto2model(dto)
                    PhotoSizeDto.Type.P -> sizes.p = dto2model(dto)
                    PhotoSizeDto.Type.Q -> sizes.q = dto2model(dto)
                    PhotoSizeDto.Type.R -> sizes.r = dto2model(dto)
                }
            }
        }
        return sizes
    }

    fun transform(orig: VkApiPrivacy): SimplePrivacy {
        val entries = ArrayList<SimplePrivacy.Entry>(safeCountOf(orig.entries))
        if (orig.entries != null) {
            for (entry in orig.entries) {
                entries.add(SimplePrivacy.Entry(entry.type, entry.id, entry.allowed))
            }
        }
        return SimplePrivacy(orig.category, entries)
    }


    fun transform(
        simplePrivacy: SimplePrivacy,
        owners: IOwnersBundle,
        friendListMap: Map<Int, FriendList>
    ): Privacy {
        val privacy = Privacy()
        privacy.type = simplePrivacy.type
        for (entry in simplePrivacy.entries) {
            when (entry.type) {
                VkApiPrivacy.Entry.TYPE_FRIENDS_LIST -> if (entry.isAllowed) {
                    privacy.allowFor(friendListMap[entry.id])
                } else {
                    privacy.disallowFor(friendListMap[entry.id])
                }
                VkApiPrivacy.Entry.TYPE_OWNER -> if (entry.isAllowed) {
                    privacy.allowFor(owners.getById(entry.id) as User)
                } else {
                    privacy.disallowFor(owners.getById(entry.id) as User)
                }
            }
        }
        return privacy
    }

    fun buildUserArray(copies: Copies, owners: IOwnersBundle): List<Owner> {
        val data: MutableList<Owner> = ArrayList(safeCountOf(copies.pairs))
        if (copies.pairs != null) {
            for (pair in copies.pairs) {
                data.add(owners.getById(pair.owner_id))
            }
        }
        return data
    }

    private fun buildUserArray(users: List<Int>, owners: IOwnersBundle): List<User> {
        val data: MutableList<User> = ArrayList(safeCountOf(users))
        for (pair in users) {
            val dt = owners.getById(pair)
            if (dt.ownerType == OwnerType.USER) data.add(
                owners.getById(
                    pair
                ) as User
            )
        }
        return data
    }

    fun buildUserArray(original: UserArray, owners: IOwnersBundle): List<Owner> {
        val data: MutableList<Owner> = ArrayList(if (original.ids == null) 0 else original.ids.size)
        if (original.ids != null) {
            for (id in original.ids) {
                data.add(owners.getById(id))
            }
        }
        return data
    }


    fun buildComment(commented: Commented, dto: VKApiComment, owners: IOwnersBundle): Comment {
        val comment = Comment(commented)
            .setId(dto.id)
            .setFromId(dto.from_id)
            .setDate(dto.date)
            .setText(dto.text)
            .setReplyToComment(dto.reply_to_comment)
            .setReplyToUser(dto.reply_to_user)
            .setLikesCount(dto.likes)
            .setUserLikes(dto.user_likes)
            .setCanLike(dto.can_like)
            .setCanEdit(dto.can_edit)
            .setThreadsCount(dto.threads_count)
            .setThreads(buildComments(commented, dto.threads, owners))
            .setPid(dto.pid)
        if (dto.from_id != 0) {
            comment.author = owners.getById(dto.from_id)
        }
        if (dto.attachments != null) {
            comment.attachments = buildAttachments(dto.attachments, owners)
            //comment.setHasAttachmens(comment.getAttachments().count());
        }
        return comment
    }

    private fun buildComments(
        commented: Commented,
        dtos: List<VKApiComment>?,
        owners: IOwnersBundle
    ): List<Comment>? {
        if (dtos.isNullOrEmpty()) {
            return null
        }
        val o: MutableList<Comment> = ArrayList()
        for (i in dtos) {
            val u = buildComment(commented, i, owners)
            o.add(u)
        }
        return o
    }


    fun transform(dto: VKApiTopic, owners: IOwnersBundle): Topic {
        val topic = Topic(dto.id, dto.owner_id)
            .setTitle(dto.title)
            .setCreationTime(dto.created)
            .setCreatedByOwnerId(dto.created_by)
            .setLastUpdateTime(dto.updated)
            .setUpdatedByOwnerId(dto.updated_by)
            .setClosed(dto.is_closed)
            .setFixed(dto.is_fixed)
            .setCommentsCount(if (dto.comments == null) 0 else dto.comments.count)
            .setFirstCommentBody(dto.first_comment)
            .setLastCommentBody(dto.last_comment)
        if (dto.updated_by != 0) {
            topic.updater = owners.getById(dto.updated_by)
        }
        if (dto.created_by != 0) {
            topic.creator = owners.getById(dto.created_by)
        }
        return topic
    }

    private fun buildPollPhoto(photo: VKApiPoll.Photo?): String? {
        var url: String? = null
        if (photo != null && !photo.images.isNullOrEmpty()) {
            var def = 0
            for (i in photo.images) {
                if (i.width * i.height > def) {
                    def = i.width * i.height
                    url = i.url
                }
            }
        }
        return url
    }


    fun transform(dto: VKApiPoll): Poll {
        val answers: MutableList<Poll.Answer> = ArrayList(safeCountOf(dto.answers))
        if (dto.answers != null) {
            for (answer in dto.answers) {
                answers.add(
                    Poll.Answer(answer.id)
                        .setRate(answer.rate)
                        .setText(answer.text)
                        .setVoteCount(answer.votes)
                )
            }
        }
        return Poll(dto.id, dto.owner_id)
            .setAnonymous(dto.anonymous)
            .setAnswers(answers)
            .setBoard(dto.is_board)
            .setCreationTime(dto.created)
            .setMyAnswerIds(dto.answer_ids)
            .setQuestion(dto.question)
            .setVoteCount(dto.votes)
            .setClosed(dto.closed)
            .setAuthorId(dto.author_id)
            .setCanVote(dto.can_vote)
            .setCanEdit(dto.can_edit)
            .setCanReport(dto.can_report)
            .setCanShare(dto.can_share)
            .setEndDate(dto.end_date)
            .setMultiple(dto.multiple)
            .setPhoto(buildPollPhoto(dto.photo))
    }


    fun transformStory(dto: VKApiStory, owners: IOwnersBundle): Story {
        return Story().setId(dto.id)
            .setOwnerId(dto.owner_id)
            .setDate(dto.date)
            .setExpires(dto.expires_at)
            .setIs_expired(dto.is_expired)
            .setAccessKey(dto.access_key)
            .setTarget_url(dto.target_url)
            .setPhoto(if (dto.photo != null) transform(dto.photo) else null)
            .setVideo(if (dto.video != null) transform(dto.video) else null)
            .setOwner(owners.getById(dto.owner_id))
    }

    fun transform(dto: VKApiCall): Call {
        return Call().setInitiator_id(dto.initiator_id)
            .setReceiver_id(dto.receiver_id)
            .setState(dto.state)
            .setTime(dto.time)
    }

    fun transform(dto: VKApiWallReply, owners: IOwnersBundle): WallReply {
        val comment = WallReply().setId(dto.id)
            .setOwnerId(dto.owner_id)
            .setFromId(dto.from_id)
            .setPostId(dto.post_id)
            .setText(dto.text)
            .setAuthor(owners.getById(dto.from_id))
        if (dto.attachments != null) {
            comment.attachments = buildAttachments(dto.attachments, owners)
            //comment.setHasAttachmens(comment.getAttachments().count());
        }
        return comment
    }

    fun transform(dto: VKApiNotSupported): NotSupported {
        return NotSupported().setType(dto.type).setBody(dto.body)
    }

    private fun transformEvent(dto: VkApiEvent, owners: IOwnersBundle): Event {
        return Event(dto.id).setButton_text(dto.button_text).setText(dto.text)
            .setSubject(owners.getById(if (dto.id >= 0) -dto.id else dto.id))
    }


    fun transform(dto: VkApiMarket): Market {
        return Market(dto.id, dto.owner_id)
            .setAccess_key(dto.access_key)
            .setIs_favorite(dto.is_favorite)
            .setAvailability(dto.availability)
            .setDate(dto.date)
            .setDescription(dto.description)
            .setDimensions(dto.dimensions)
            .setPrice(dto.price)
            .setSku(dto.sku)
            .setTitle(dto.title)
            .setWeight(dto.weight)
            .setThumb_photo(dto.thumb_photo)
    }

    fun transform(dto: VkApiMarketAlbum): MarketAlbum {
        return MarketAlbum(dto.id, dto.owner_id)
            .setAccess_key(dto.access_key)
            .setCount(dto.count)
            .setTitle(dto.title)
            .setUpdated_time(dto.updated_time)
            .setPhoto(if (dto.photo != null) transform(dto.photo) else null)
    }

    fun transform(dto: VKApiGraffiti): Graffiti {
        return Graffiti().setId(dto.id)
            .setOwner_id(dto.owner_id)
            .setAccess_key(dto.access_key)
            .setHeight(dto.height)
            .setWidth(dto.width)
            .setUrl(dto.url)
    }


    fun transform(dto: VKApiShortLink): ShortLink {
        return ShortLink().setKey(dto.key)
            .setShort_url(dto.short_url)
            .setUrl(dto.url)
            .setAccess_key(dto.access_key)
            .setViews(dto.views)
            .setTimestamp(dto.timestamp)
    }


    fun transform(dto: VKApiPhoto): Photo {
        return Photo()
            .setId(dto.id)
            .setAlbumId(dto.album_id)
            .setOwnerId(dto.owner_id)
            .setWidth(dto.width)
            .setHeight(dto.height)
            .setText(dto.text)
            .setDate(dto.date)
            .setUserLikes(dto.user_likes)
            .setCanComment(dto.can_comment)
            .setLikesCount(dto.likes)
            .setCommentsCount(if (dto.comments == null) 0 else dto.comments.count)
            .setTagsCount(dto.tags)
            .setAccessKey(dto.access_key)
            .setDeleted(false)
            .setPostId(dto.post_id)
            .setSizes(if (dto.sizes == null) null else transform(dto.sizes))
    }


    fun transform(dto: VKApiAudio): Audio {
        return Audio()
            .setId(dto.id)
            .setOwnerId(dto.owner_id)
            .setArtist(dto.artist)
            .setTitle(dto.title)
            .setDuration(dto.duration)
            .setUrl(dto.url)
            .setLyricsId(dto.lyrics_id)
            .setAlbumId(dto.album_id)
            .setAlbum_owner_id(dto.album_owner_id)
            .setAlbum_access_key(dto.album_access_key)
            .setGenre(dto.genre_id)
            .setAccessKey(dto.access_key)
            .setDeleted(false)
            .setAlbum_title(dto.album_title)
            .setThumb_image_big(dto.thumb_image_big)
            .setThumb_image_little(dto.thumb_image_little)
            .setThumb_image_very_big(dto.thumb_image_very_big)
            .setIsHq(dto.isHq)
            .setMain_artists(dto.main_artists).updateDownloadIndicator()
    }


    fun transform(dto: VKApiAudioPlaylist): AudioPlaylist {
        return AudioPlaylist()
            .setId(dto.id)
            .setOwnerId(dto.owner_id)
            .setAccess_key(dto.access_key)
            .setArtist_name(dto.artist_name)
            .setCount(dto.count)
            .setDescription(dto.description)
            .setGenre(dto.genre)
            .setYear(dto.Year)
            .setTitle(dto.title)
            .setThumb_image(dto.thumb_image)
            .setUpdate_time(dto.update_time)
            .setOriginal_access_key(dto.original_access_key)
            .setOriginal_id(dto.original_id)
            .setOriginal_owner_id(dto.original_owner_id)
    }


    fun transform(dto: CatalogResponse): CatalogBlock {
        return CatalogBlock()
            .setAudios(transformAudios(dto.audios))
            .setPlaylists(transformAudioPlaylists(dto.playlists))
            .setVideos(transformVideos(dto.videos))
            .setLinks(transformCatalogLinks(dto.items))
            .setNext_from(dto.nextFrom)
    }


    fun transform(dto: VKApiAudioCatalog): AudioCatalog {
        val playlists = transformAudioPlaylists(dto.playlists)
        if (dto.playlist != null) {
            playlists.add(transform(dto.playlist))
        }
        return AudioCatalog()
            .setId(dto.id)
            .setSource(dto.source)
            .setNext_from(dto.next_from)
            .setSubtitle(dto.subtitle)
            .setTitle(dto.title)
            .setType(dto.type)
            .setCount(dto.count)
            .setAudios(transformAudios(dto.audios))
            .setPlaylists(playlists)
            .setVideos(transformVideos(dto.videos))
            .setLinks(transformCatalogLinks(dto.items))
            .setArtist(if (dto.artist != null) transform(dto.artist) else null)
    }


    fun transform(link: VKApiLink): Link {
        return Link()
            .setUrl(link.url)
            .setTitle(link.title)
            .setCaption(link.caption)
            .setDescription(link.description)
            .setPreviewPhoto(link.preview_photo)
            .setPhoto(if (link.photo == null) null else transform(link.photo))
    }


    fun transform(link: VKApiCatalogLink): Link {
        return Link()
            .setUrl(link.url)
            .setTitle(link.title)
            .setDescription(link.subtitle)
            .setPreviewPhoto(link.preview_photo)
            .setPhoto(null)
    }


    fun transform(article: VKApiArticle): Article {
        return Article(article.id, article.owner_id)
            .setAccessKey(article.access_key)
            .setOwnerName(article.owner_name)
            .setPhoto(if (article.photo == null) null else transform(article.photo))
            .setTitle(article.title)
            .setSubTitle(article.subtitle)
            .setURL(article.url)
            .setIsFavorite(article.is_favorite)
    }


    fun map(dto: VKApiAudioArtist.Image): AudioArtistImage {
        return AudioArtistImage(dto.url, dto.width, dto.height)
    }


    fun transform(dto: VKApiAudioArtist): AudioArtist {
        return AudioArtist(dto.id)
            .setName(dto.name)
            .setPhoto(
                mapAll(
                    dto.photo
                ) {
                    map(it)
                }
            )
    }


    fun map(dto: VKApiSticker.Image): Sticker.Image {
        return Sticker.Image(dto.url, dto.width, dto.height)
    }

    fun transform(dto: VKApiSticker): Sticker {
        return Sticker(dto.sticker_id)
            .setImages(
                mapAll(
                    dto.images
                ) {
                    map(it)
                }
            )
            .setImagesWithBackground(
                mapAll(
                    dto.images_with_background
                ) {
                    map(it)
                }
            )
            .setAnimationUrl(dto.animation_url)
    }

    fun transform(dto: FaveLinkDto): FaveLink {
        return FaveLink(dto.id)
            .setUrl(dto.url)
            .setTitle(dto.title)
            .setDescription(dto.description)
            .setPhoto(if (dto.photo != null) transform(dto.photo) else null)
    }

    fun transform(dto: VkApiAudioMessage): VoiceMessage {
        return VoiceMessage(dto.id, dto.owner_id)
            .setDuration(dto.duration)
            .setWaveform(dto.waveform)
            .setLinkOgg(dto.linkOgg)
            .setLinkMp3(dto.linkMp3)
            .setAccessKey(dto.access_key)
            .setTranscript(dto.transcript)
    }


    fun transform(dto: VkApiDoc): Document {
        val document = Document(dto.id, dto.ownerId)
        document.setTitle(dto.title)
            .setSize(dto.size)
            .setExt(dto.ext)
            .setUrl(dto.url)
            .setAccessKey(dto.accessKey)
            .setDate(dto.date).type = dto.type
        if (dto.preview != null) {
            if (dto.preview.photo != null && dto.preview.photo.sizes != null) {
                document.photoPreview = transform(dto.preview.photo.sizes)
            }
            if (dto.preview.video != null) {
                document.videoPreview = VideoPreview()
                    .setWidth(dto.preview.video.width)
                    .setHeight(dto.preview.video.height)
                    .setSrc(dto.preview.video.src)
            }
            if (dto.preview.graffiti != null) {
                document.graffiti = Document.Graffiti()
                    .setHeight(dto.preview.graffiti.height)
                    .setWidth(dto.preview.graffiti.width)
                    .setSrc(dto.preview.graffiti.src)
            }
        }
        return document
    }

    /*public static Document transform(@NonNull VKApiDocument dto) {
        Document document = dto.isVoiceMessage() ? new VoiceMessage(dto.id, dto.owner_id)
                : new Document(dto.id, dto.owner_id);

        document.setTitle(dto.title)
                .setSize(dto.size)
                .setExt(dto.ext)
                .setUrl(dto.url)
                .setAccessKey(dto.access_key)
                .setDate(dto.date)
                .setType(dto.type);

        if (document instanceof VoiceMessage) {
            ((VoiceMessage) document)
                    .setDuration(dto.preview.audio_msg.duration)
                    .setWaveform(dto.preview.audio_msg.waveform)
                    .setLinkOgg(dto.preview.audio_msg.link_ogg)
                    .setLinkMp3(dto.preview.audio_msg.link_mp3);
        }

        if (dto.preview != null) {
            if (dto.preview.photo_sizes != null) {
                document.setPhotoPreview(transform(dto.preview.photo_sizes));
            }

            if (dto.preview.video_preview != null) {
                document.setVideoPreview(new Document.VideoPreview()
                        .setWidth(dto.preview.video_preview.width)
                        .setHeight(dto.preview.video_preview.height)
                        .setSrc(dto.preview.video_preview.src));
            }

            if (dto.preview.graffiti != null) {
                document.setGraffiti(new Document.Graffiti()
                        .setHeight(dto.preview.graffiti.height)
                        .setWidth(dto.preview.graffiti.width)
                        .setSrc(dto.preview.graffiti.src));
            }
        }

        return document;
    }*/

    fun transform(dto: VKApiVideo): Video {
        return Video()
            .setId(dto.id)
            .setOwnerId(dto.owner_id)
            .setAlbumId(dto.album_id)
            .setTitle(dto.title)
            .setDescription(dto.description)
            .setDuration(dto.duration)
            .setLink(dto.link)
            .setDate(dto.date)
            .setAddingDate(dto.adding_date)
            .setViews(dto.views)
            .setPlayer(dto.player)
            .setImage(dto.image)
            .setAccessKey(dto.access_key)
            .setCommentsCount(if (dto.comments == null) 0 else dto.comments.count)
            .setCanComment(dto.can_comment)
            .setCanRepost(dto.can_repost)
            .setUserLikes(dto.user_likes)
            .setRepeat(dto.repeat)
            .setLikesCount(dto.likes)
            .setPrivacyView(if (dto.privacy_view == null) null else transform(dto.privacy_view))
            .setPrivacyComment(if (dto.privacy_comment == null) null else transform(dto.privacy_comment))
            .setMp4link240(dto.mp4_240)
            .setMp4link360(dto.mp4_360)
            .setMp4link480(dto.mp4_480)
            .setMp4link720(dto.mp4_720)
            .setMp4link1080(dto.mp4_1080)
            .setExternalLink(dto.external)
            .setHls(dto.hls)
            .setLive(dto.live)
            .setPlatform(dto.platform)
            .setCanEdit(dto.can_edit)
            .setCanAdd(dto.can_add)
            .setPrivate(dto.is_private)
    }

    fun transform(dto: VKApiWikiPage): WikiPage {
        return WikiPage(dto.id, dto.owner_id)
            .setCreatorId(dto.creator_id)
            .setTitle(dto.title)
            .setSource(dto.source)
            .setEditionTime(dto.edited)
            .setCreationTime(dto.created)
            .setParent(dto.parent)
            .setParent2(dto.parent2)
            .setViews(dto.views)
            .setViewUrl(dto.view_url)
    }

    private fun buildAttachments(
        apiAttachments: VkApiAttachments,
        owners: IOwnersBundle
    ): Attachments {
        val attachments = Attachments()
        val entries = apiAttachments.entryList()
        for (entry in entries) {
            val attachment = entry.attachment
            when (attachment.type) {
                VKApiAttachment.TYPE_AUDIO -> attachments.prepareAudios().add(
                    transform(attachment as VKApiAudio)
                )
                VKApiAttachment.TYPE_STICKER -> attachments.prepareStickers()
                    .add(transform(attachment as VKApiSticker))
                VKApiAttachment.TYPE_PHOTO -> attachments.preparePhotos()
                    .add(transform(attachment as VKApiPhoto))
                VKApiAttachment.TYPE_DOC -> attachments.prepareDocs()
                    .add(transform(attachment as VkApiDoc))
                VKApiAttachment.TYPE_AUDIO_MESSAGE -> attachments.prepareVoiceMessages().add(
                    transform(attachment as VkApiAudioMessage)
                )
                VKApiAttachment.TYPE_VIDEO -> attachments.prepareVideos()
                    .add(transform(attachment as VKApiVideo))
                VKApiAttachment.TYPE_LINK -> attachments.prepareLinks()
                    .add(transform(attachment as VKApiLink))
                VKApiAttachment.TYPE_ARTICLE -> attachments.prepareArticles()
                    .add(transform(attachment as VKApiArticle))
                VKApiAttachment.TYPE_STORY -> attachments.prepareStories()
                    .add(transformStory(attachment as VKApiStory, owners))
                VKApiAttachment.TYPE_ALBUM -> attachments.preparePhotoAlbums().add(
                    transformPhotoAlbum(attachment as VKApiPhotoAlbum)
                )
                VKApiAttachment.TYPE_CALL -> attachments.prepareCalls()
                    .add(transform(attachment as VKApiCall))
                VKApiAttachment.TYPE_WALL_REPLY -> attachments.prepareWallReply()
                    .add(transform(attachment as VKApiWallReply, owners))
                VKApiAttachment.TYPE_NOT_SUPPORT -> attachments.prepareNotSupporteds()
                    .add(transform(attachment as VKApiNotSupported))
                VKApiAttachment.TYPE_EVENT -> attachments.prepareEvents()
                    .add(transformEvent(attachment as VkApiEvent, owners))
                VKApiAttachment.TYPE_MARKET -> attachments.prepareMarkets()
                    .add(transform(attachment as VkApiMarket))
                VKApiAttachment.TYPE_MARKET_ALBUM -> attachments.prepareMarketAlbums()
                    .add(transform(attachment as VkApiMarketAlbum))
                VKApiAttachment.TYPE_ARTIST -> attachments.prepareAudioArtist()
                    .add(transform(attachment as VKApiAudioArtist))
                VKApiAttachment.TYPE_AUDIO_PLAYLIST -> attachments.prepareAudioPlaylists().add(
                    transform(attachment as VKApiAudioPlaylist)
                )
                VKApiAttachment.TYPE_GRAFFITI -> attachments.prepareGraffity()
                    .add(transform(attachment as VKApiGraffiti))
                VKApiAttachment.TYPE_POLL -> attachments.preparePolls()
                    .add(transform(attachment as VKApiPoll))
                VKApiAttachment.TYPE_WIKI_PAGE -> attachments.prepareWikiPages()
                    .add(transform(attachment as VKApiWikiPage))
                VKApiAttachment.TYPE_POST -> attachments.preparePosts()
                    .add(transform(attachment as VKApiPost, owners))
            }
        }
        return attachments
    }


    fun transformPosts(dtos: Collection<VKApiPost>, bundle: IOwnersBundle): List<Post> {
        val posts: MutableList<Post> = ArrayList(safeCountOf(dtos))
        for (dto in dtos) {
            posts.add(transform(dto, bundle))
        }
        return posts
    }


    fun transformAttachmentsPosts(
        dtos: Collection<VkApiAttachments.Entry>,
        bundle: IOwnersBundle
    ): List<Post> {
        val posts: MutableList<Post> = ArrayList(safeCountOf(dtos))
        for (dto in dtos) {
            if (dto.attachment is VKApiPost) posts.add(
                transform(
                    dto.attachment,
                    bundle
                )
            )
        }
        return posts
    }


    fun transform(dto: VKApiPost, owners: IOwnersBundle): Post {
        val post = Post()
            .setDbid(Post.NO_STORED)
            .setVkid(dto.id)
            .setOwnerId(dto.owner_id)
            .setAuthorId(dto.from_id)
            .setDate(dto.date)
            .setText(dto.text)
            .setReplyOwnerId(dto.reply_owner_id)
            .setReplyPostId(dto.reply_post_id)
            .setFriendsOnly(dto.friends_only)
            .setCommentsCount(if (dto.comments == null) 0 else dto.comments.count)
            .setCanPostComment(dto.comments != null && dto.comments.canPost)
            .setLikesCount(dto.likes_count)
            .setUserLikes(dto.user_likes)
            .setCanLike(dto.can_like)
            .setCanRepost(dto.can_publish)
            .setRepostCount(dto.reposts_count)
            .setUserReposted(dto.user_reposted)
            .setPostType(dto.post_type)
            .setSignerId(dto.signer_id)
            .setCreatorId(dto.created_by)
            .setCanEdit(dto.can_edit)
            .setCanPin(dto.can_pin)
            .setPinned(dto.is_pinned)
            .setViewCount(dto.views)
        if (dto.post_source != null) {
            post.source = PostSource(
                dto.post_source.type,
                dto.post_source.platform,
                dto.post_source.data,
                dto.post_source.url
            )
        }
        if (dto.hasAttachments()) {
            post.attachments = buildAttachments(dto.attachments, owners)
        }
        if (dto.hasCopyHistory()) {
            val copyCount = safeCountOf(dto.copy_history)
            for (copy in dto.copy_history) {
                post.prepareCopyHierarchy(copyCount).add(transform(copy, owners))
            }
        }
        fillPostOwners(post, owners)
        if (post.hasCopyHierarchy()) {
            for (copy in post.copyHierarchy) {
                fillPostOwners(copy, owners)
            }
        }
        return post
    }


    fun buildNews(original: VKApiNews, owners: IOwnersBundle): News {
        val news = News()
            .setType(original.type)
            .setSourceId(original.source_id)
            .setPostType(original.post_type)
            .setFinalPost(original.final_post)
            .setCopyOwnerId(original.copy_owner_id)
            .setCopyPostId(original.copy_post_id)
            .setCopyPostDate(original.copy_post_date)
            .setDate(original.date)
            .setPostId(original.post_id)
            .setText(original.text)
            .setCanEdit(original.can_edit)
            .setCanDelete(original.can_delete)
            .setCommentCount(original.comment_count)
            .setCommentCanPost(original.comment_can_post)
            .setLikeCount(original.like_count)
            .setUserLike(original.user_like)
            .setCanLike(original.can_like)
            .setCanPublish(original.can_publish)
            .setRepostsCount(original.reposts_count)
            .setUserReposted(original.user_reposted)
            .setFriends(
                if (original.friends == null) null else buildUserArray(
                    original.friends,
                    owners
                )
            )
            .setSource(owners.getById(original.source_id))
            .setViewCount(original.views)
        if (original.hasCopyHistory()) {
            val copies = ArrayList<Post>(original.copy_history.size)
            for (copy in original.copy_history) {
                copies.add(transform(copy, owners))
            }
            news.copyHistory = copies
        }
        if (original.hasAttachments()) {
            news.attachments = buildAttachments(original.attachments, owners)
        }
        return news
    }


    fun fillPostOwners(post: Post, owners: IOwnersBundle) {
        if (post.authorId != 0) {
            post.author = owners.getById(post.authorId)
        }
        if (post.signerId != 0) {
            post.creator = owners.getById(post.signerId) as User
        } else if (post.creatorId != 0) {
            post.creator = owners.getById(post.creatorId) as User
        }
    }


    fun transformPhotoAlbum(album: VKApiPhotoAlbum): PhotoAlbum {
        return PhotoAlbum(album.id, album.owner_id)
            .setTitle(album.title)
            .setSize(album.size)
            .setDescription(album.description)
            .setCanUpload(album.can_upload)
            .setUpdatedTime(album.updated)
            .setCreatedTime(album.created)
            .setSizes(if (album.photo != null) transform(album.photo) else null)
            .setCommentsDisabled(album.comments_disabled)
            .setUploadByAdminsOnly(album.upload_by_admins_only)
            .setPrivacyView(if (album.privacy_view != null) transform(album.privacy_view) else null)
            .setPrivacyComment(if (album.privacy_comment != null) transform(album.privacy_comment) else null)
    }
}