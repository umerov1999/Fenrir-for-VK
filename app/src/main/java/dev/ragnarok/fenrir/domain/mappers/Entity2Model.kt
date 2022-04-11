package dev.ragnarok.fenrir.domain.mappers

import dev.ragnarok.fenrir.db.model.entity.*
import dev.ragnarok.fenrir.db.model.entity.AudioArtistEntity.AudioArtistImageEntity
import dev.ragnarok.fenrir.db.model.entity.StickerEntity.AnimationEntity
import dev.ragnarok.fenrir.db.model.entity.UserDetailsEntity.RelativeEntity
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.fillPostOwners
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.model.AudioArtist.AudioArtistImage
import dev.ragnarok.fenrir.model.Document.VideoPreview
import dev.ragnarok.fenrir.model.database.Country
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import dev.ragnarok.fenrir.util.VKOwnIds

object Entity2Model {

    fun buildVideoAlbumFromDbo(dbo: VideoAlbumEntity): VideoAlbum {
        return VideoAlbum(dbo.id, dbo.ownerId)
            .setTitle(dbo.title)
            .setCount(dbo.count)
            .setPrivacy(if (dbo.privacy != null) mapSimplePrivacy(dbo.privacy) else null)
            .setImage(dbo.image)
            .setUpdatedTime(dbo.updateTime)
    }


    fun buildTopicFromDbo(dbo: TopicEntity, owners: IOwnersBundle): Topic {
        val topic = Topic(dbo.id, dbo.ownerId)
            .setTitle(dbo.title)
            .setCreationTime(dbo.createdTime)
            .setCreatedByOwnerId(dbo.creatorId)
            .setLastUpdateTime(dbo.lastUpdateTime)
            .setUpdatedByOwnerId(dbo.updatedBy)
            .setClosed(dbo.isClosed)
            .setFixed(dbo.isFixed)
            .setCommentsCount(dbo.commentsCount)
            .setFirstCommentBody(dbo.firstComment)
            .setLastCommentBody(dbo.lastComment)
        if (dbo.updatedBy != 0) {
            topic.updater = owners.getById(dbo.updatedBy)
        }
        if (dbo.creatorId != 0) {
            topic.creator = owners.getById(dbo.creatorId)
        }
        return topic
    }


    fun buildCommunitiesFromDbos(dbos: List<CommunityEntity>): List<Community> {
        val communities: MutableList<Community> = ArrayList(dbos.size)
        for (dbo in dbos) {
            communities.add(buildCommunityFromDbo(dbo))
        }
        return communities
    }


    fun buildCommunityFromDbo(dbo: CommunityEntity): Community {
        return Community(dbo.id)
            .setName(dbo.name)
            .setScreenName(dbo.screenName)
            .setClosed(dbo.closed)
            .setVerified(dbo.isVerified)
            .setAdmin(dbo.isAdmin)
            .setAdminLevel(dbo.adminLevel)
            .setMember(dbo.isMember)
            .setMemberStatus(dbo.memberStatus)
            .setType(dbo.type)
            .setPhoto50(dbo.photo50)
            .setPhoto100(dbo.photo100)
            .setPhoto200(dbo.photo200)
            .setMembersCount(dbo.membersCount)
    }


    fun buildCommunityDetailsFromDbo(dbo: CommunityDetailsEntity): CommunityDetails {
        val details = CommunityDetails()
            .setCanMessage(dbo.isCanMessage)
            .setFavorite(dbo.isSetFavorite)
            .setSubscribed(dbo.isSetSubscribed)
            .setStatus(dbo.status)
            .setStatusAudio(if (dbo.statusAudio != null) buildAudioFromDbo(dbo.statusAudio) else null)
            .setAllWallCount(dbo.allWallCount)
            .setOwnerWallCount(dbo.ownerWallCount)
            .setPostponedWallCount(dbo.postponedWallCount)
            .setSuggestedWallCount(dbo.suggestedWallCount)
            .setTopicsCount(dbo.topicsCount)
            .setDocsCount(dbo.docsCount)
            .setPhotosCount(dbo.photosCount)
            .setAudiosCount(dbo.audiosCount)
            .setVideosCount(dbo.videosCount)
            .setProductsCount(dbo.productsCount)
            .setArticlesCount(dbo.articlesCount)
            .setChatsCount(dbo.chatsCount)
        if (dbo.cover != null) {
            val cover = CommunityDetails.Cover()
                .setEnabled(dbo.cover.isEnabled)
                .setImages(ArrayList(safeCountOf(dbo.cover.images)))
            if (dbo.cover.images != null) {
                for (imageDto in dbo.cover.images) {
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
        details.description = dbo.description
        return details
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


    fun buildUsersFromDbo(dbos: List<UserEntity>): List<User> {
        val users: MutableList<User> = ArrayList(dbos.size)
        for (dbo in dbos) {
            map(dbo)?.let { users.add(it) }
        }
        return users
    }


    fun buildFaveUsersFromDbo(dbos: List<FavePageEntity>): List<FavePage> {
        val users: MutableList<FavePage> = ArrayList(dbos.size)
        for (dbo in dbos) {
            users.add(map(dbo))
        }
        return users
    }


    fun buildUserDetailsFromDbo(dbo: UserDetailsEntity, owners: IOwnersBundle): UserDetails {
        val details = UserDetails()
            .setPhotoId(
                if (dbo.photoId != null) IdPair(
                    dbo.photoId.id,
                    dbo.photoId.ownerId
                ) else null
            )
            .setStatusAudio(if (dbo.statusAudio != null) buildAudioFromDbo(dbo.statusAudio) else null)
            .setFriendsCount(dbo.friendsCount)
            .setOnlineFriendsCount(dbo.onlineFriendsCount)
            .setMutualFriendsCount(dbo.mutualFriendsCount)
            .setFollowersCount(dbo.followersCount)
            .setGroupsCount(dbo.groupsCount)
            .setPhotosCount(dbo.photosCount)
            .setAudiosCount(dbo.audiosCount)
            .setVideosCount(dbo.videosCount)
            .setArticlesCount(dbo.articlesCount)
            .setProductsCount(dbo.productsCount)
            .setGiftCount(dbo.giftCount)
            .setAllWallCount(dbo.allWallCount)
            .setOwnWallCount(dbo.ownWallCount)
            .setPostponedWallCount(dbo.postponedWallCount)
            .setBdate(dbo.bdate)
            .setCity(if (dbo.city == null) null else map(dbo.city))
            .setCountry(if (dbo.country == null) null else map(dbo.country))
            .setHometown(dbo.homeTown)
            .setPhone(dbo.phone)
            .setHomePhone(dbo.homePhone)
            .setSkype(dbo.skype)
            .setInstagram(dbo.instagram)
            .setTwitter(dbo.twitter)
            .setFacebook(dbo.facebook)
        details.militaries = mapAll(dbo.militaries, Entity2Model::map)
        details.careers = mapAll(dbo.careers) { orig -> map(orig, owners) }
        details.universities = mapAll(dbo.universities, Entity2Model::map)
        details.schools = mapAll(dbo.schools, Entity2Model::map)
        details.relatives = mapAll(dbo.relatives) { orig -> map(orig, owners) }
        details.relation = dbo.relation
        details.relationPartner =
            if (dbo.relationPartnerId != 0) owners.getById(dbo.relationPartnerId) else null
        details.languages = dbo.languages
        details.political = dbo.political
        details.peopleMain = dbo.peopleMain
        details.lifeMain = dbo.lifeMain
        details.smoking = dbo.smoking
        details.alcohol = dbo.alcohol
        details.inspiredBy = dbo.inspiredBy
        details.religion = dbo.religion
        details.site = dbo.site
        details.interests = dbo.interests
        details.music = dbo.music
        details.activities = dbo.activities
        details.movies = dbo.movies
        details.tv = dbo.tv
        details.games = dbo.games
        details.quotes = dbo.quotes
        details.about = dbo.about
        details.books = dbo.books
        details.setFavorite(dbo.isSetFavorite)
        details.setSubscribed(dbo.isSetSubscribed)
        return details
    }

    fun map(entity: RelativeEntity, owners: IOwnersBundle): UserDetails.Relative {
        return UserDetails.Relative()
            .setUser(if (entity.id > 0) owners.getById(entity.id) as User else null)
            .setName(entity.name)
            .setType(entity.type)
    }

    fun map(entity: SchoolEntity): School {
        return School()
            .setCityId(entity.cityId)
            .setCountryId(entity.countryId)
            .setId(entity.id)
            .setClazz(entity.clazz)
            .setName(entity.name)
            .setTo(entity.to)
            .setFrom(entity.from)
            .setYearGraduated(entity.yearGraduated)
    }

    fun map(entity: UniversityEntity): University {
        return University()
            .setName(entity.name)
            .setCityId(entity.cityId)
            .setCountryId(entity.countryId)
            .setStatus(entity.status)
            .setGraduationYear(entity.graduationYear)
            .setId(entity.id)
            .setFacultyId(entity.facultyId)
            .setFacultyName(entity.facultyName)
            .setChairId(entity.chairId)
            .setChairName(entity.chairName)
            .setForm(entity.form)
    }

    fun map(entity: MilitaryEntity): Military {
        return Military()
            .setCountryId(entity.countryId)
            .setFrom(entity.from)
            .setUnit(entity.unit)
            .setUntil(entity.until)
            .setUnitId(entity.unitId)
    }

    fun map(entity: CareerEntity, bundle: IOwnersBundle): Career {
        return Career()
            .setCityId(entity.cityId)
            .setCompany(entity.company)
            .setCountryId(entity.countryId)
            .setFrom(entity.from)
            .setUntil(entity.until)
            .setPosition(entity.position)
            .setGroup(if (entity.groupId == 0) null else bundle.getById(-entity.groupId) as Community)
    }

    fun map(entity: CountryEntity): Country {
        return Country(entity.id, entity.title)
    }

    fun map(entity: CityEntity): City {
        return City(entity.id, entity.title)
            .setArea(entity.area)
            .setImportant(entity.isImportant)
            .setRegion(entity.region)
    }


    fun map(entity: UserEntity?): User? {
        return if (entity == null) {
            null
        } else User(entity.id)
            .setFirstName(entity.firstName)
            .setLastName(entity.lastName)
            .setOnline(entity.isOnline)
            .setOnlineMobile(entity.isOnlineMobile)
            .setOnlineApp(entity.onlineApp)
            .setPhoto50(entity.photo50)
            .setPhoto100(entity.photo100)
            .setPhoto200(entity.photo200)
            .setPhotoMax(entity.photoMax)
            .setLastSeen(entity.lastSeen)
            .setPlatform(entity.platform)
            .setStatus(entity.status)
            .setSex(entity.sex)
            .setDomain(entity.domain)
            .setFriend(entity.isFriend)
            .setFriendStatus(entity.friendStatus)
            .setCanWritePrivateMessage(entity.canWritePrivateMessage)
            .setBlacklisted(entity.blacklisted)
            .setBlacklisted_by_me(entity.blacklisted_by_me)
            .setVerified(entity.isVerified)
            .setCan_access_closed(entity.isCan_access_closed)
            .setMaiden_name(entity.maiden_name)
    }

    fun map(entity: FavePageEntity): FavePage {
        return FavePage(entity.id)
            .setDescription(entity.description)
            .setUpdatedDate(entity.updateDate)
            .setFaveType(entity.faveType)
            .setUser(if (entity.user != null) map(entity.user) else null)
            .setGroup(if (entity.group != null) map(entity.group) else null)
    }


    fun map(entity: CommunityEntity?): Community? {
        return if (entity == null) {
            null
        } else Community(entity.id)
            .setName(entity.name)
            .setScreenName(entity.screenName)
            .setPhoto50(entity.photo50)
            .setPhoto100(entity.photo100)
            .setPhoto200(entity.photo200)
            .setAdmin(entity.isAdmin)
            .setAdminLevel(entity.adminLevel)
            .setClosed(entity.closed)
            .setVerified(entity.isVerified)
            .setMember(entity.isMember)
            .setMemberStatus(entity.memberStatus)
            .setMembersCount(entity.membersCount)
            .setType(entity.type)
    }


    fun mapPhotoAlbum(entity: PhotoAlbumEntity): PhotoAlbum {
        return PhotoAlbum(entity.id, entity.ownerId)
            .setSize(entity.size)
            .setTitle(entity.title)
            .setDescription(entity.description)
            .setCanUpload(entity.isCanUpload)
            .setUpdatedTime(entity.updatedTime)
            .setCreatedTime(entity.createdTime)
            .setSizes(if (entity.sizes != null) buildPhotoSizesFromDbo(entity.sizes) else PhotoSizes.empty())
            .setPrivacyView(if (entity.privacyView != null) mapSimplePrivacy(entity.privacyView) else null)
            .setPrivacyComment(if (entity.privacyComment != null) mapSimplePrivacy(entity.privacyComment) else null)
            .setUploadByAdminsOnly(entity.isUploadByAdminsOnly)
            .setCommentsDisabled(entity.isCommentsDisabled)
    }


    fun buildCommentFromDbo(dbo: CommentEntity?, owners: IOwnersBundle): Comment? {
        dbo ?: return null
        val attachments = if (dbo.attachments.isNullOrEmpty()) null else buildAttachmentsFromDbos(
            dbo.attachments, owners
        )
        return Comment(
            Commented(
                dbo.sourceId,
                dbo.sourceOwnerId,
                dbo.sourceType,
                dbo.sourceAccessKey
            )
        )
            .setId(dbo.id)
            .setFromId(dbo.fromId)
            .setDate(dbo.date)
            .setText(dbo.text)
            .setReplyToUser(dbo.replyToUserId)
            .setReplyToComment(dbo.replyToComment)
            .setLikesCount(dbo.likesCount)
            .setUserLikes(dbo.isUserLikes)
            .setCanLike(dbo.isCanLike)
            .setCanEdit(dbo.isCanEdit)
            .setAttachments(attachments)
            .setAuthor(owners.getById(dbo.fromId))
            .setThreadsCount(dbo.threadsCount)
            .setThreads(buildCommentsFromDbo(dbo.threads, owners))
            .setPid(dbo.pid)
            .setDeleted(dbo.isDeleted)
    }

    private fun buildCommentsFromDbo(
        dbos: List<CommentEntity?>,
        owners: IOwnersBundle
    ): List<Comment>? {
        if (dbos.isNullOrEmpty()) {
            return null
        }
        val o: MutableList<Comment> = ArrayList()
        for (i in dbos) {
            val u = buildCommentFromDbo(i, owners)
            if (u != null) {
                o.add(u)
            }
        }
        return o
    }


    fun buildDialogFromDbo(accountId: Int, entity: DialogEntity, owners: IOwnersBundle): Dialog {
        val message = message(accountId, entity.message, owners)
        val dialog = Dialog()
            .setLastMessageId(entity.lastMessageId)
            .setPeerId(entity.peerId)
            .setPhoto50(entity.photo50)
            .setPhoto100(entity.photo100)
            .setPhoto200(entity.photo200)
            .setTitle(entity.title)
            .setMessage(message)
            .setUnreadCount(entity.unreadCount)
            .setOutRead(entity.outRead)
            .setInRead(entity.inRead)
            .setGroupChannel(entity.isGroupChannel)
            .setMajor_id(entity.major_id)
            .setMinor_id(entity.minor_id)
        when (Peer.getType(entity.peerId)) {
            Peer.GROUP, Peer.USER -> dialog.interlocutor = owners.getById(dialog.peerId)
            Peer.CHAT -> dialog.interlocutor = owners.getById(message.senderId)
            else -> throw IllegalArgumentException("Invalid peer_id")
        }
        return dialog
    }


    fun buildKeyboardFromDbo(keyboard: KeyboardEntity?): Keyboard? {
        if (keyboard == null || keyboard.buttons.isNullOrEmpty()) {
            return null
        }
        val buttons: MutableList<List<Keyboard.Button>> = ArrayList(keyboard.buttons.size)
        for (i in keyboard.buttons) {
            val vt: MutableList<Keyboard.Button> = ArrayList(i.size)
            for (s in i) {
                vt.add(
                    Keyboard.Button().setType(s.type).setColor(s.color).setLabel(s.label)
                        .setLink(s.link).setPayload(s.payload)
                )
            }
            buttons.add(vt)
        }
        return Keyboard().setAuthor_id(
            keyboard.author_id
        ).setInline(keyboard.inline)
            .setOne_time(keyboard.one_time).setButtons(buttons)
    }


    fun message(accountId: Int, dbo: MessageEntity, owners: IOwnersBundle): Message {
        val message = Message(dbo.id)
            .setAccountId(accountId)
            .setBody(dbo.body)
            .setPeerId(dbo.peerId)
            .setSenderId(dbo.fromId)
            .setOut(dbo.isOut)
            .setStatus(dbo.status)
            .setDate(dbo.date)
            .setHasAttachments(dbo.isHasAttachmens)
            .setForwardMessagesCount(dbo.forwardCount)
            .setDeleted(dbo.isDeleted)
            .setDeletedForAll(dbo.isDeletedForAll)
            .setOriginalId(dbo.originalId)
            .setCryptStatus(if (dbo.isEncrypted) CryptStatus.ENCRYPTED else CryptStatus.NO_ENCRYPTION)
            .setImportant(dbo.isImportant)
            .setAction(dbo.action)
            .setActionMid(dbo.actionMemberId)
            .setActionEmail(dbo.actionEmail)
            .setActionText(dbo.actionText)
            .setPhoto50(dbo.photo50)
            .setPhoto100(dbo.photo100)
            .setPhoto200(dbo.photo200)
            .setSender(owners.getById(dbo.fromId))
            .setRandomId(dbo.randomId)
            .setUpdateTime(dbo.updateTime)
            .setPayload(dbo.payload)
            .setKeyboard(buildKeyboardFromDbo(dbo.keyboard))
        if (dbo.actionMemberId != 0) {
            message.setActionUser(owners.getById(dbo.actionMemberId))
        }
        if (dbo.attachments.nonNullNoEmpty()) {
            message.attachments = buildAttachmentsFromDbos(dbo.attachments, owners)
        }
        dbo.forwardMessages.nonNullNoEmpty {
            for (fwdDbo in it) {
                message.prepareFwd(it.size)
                    .add(message(accountId, fwdDbo, owners))
            }
        }
        return message
    }

    private fun buildAttachmentsFromDbos(
        entities: List<Entity>?,
        owners: IOwnersBundle
    ): Attachments {
        val attachments = Attachments()
        if (entities.nonNullNoEmpty()) {
            for (entity in entities) {
                attachments.add(buildAttachmentFromDbo(entity, owners))
            }
        }
        return attachments
    }


    fun buildAttachmentFromDbo(entity: Entity, owners: IOwnersBundle): AbsModel {
        if (entity is PhotoEntity) {
            return map(entity)
        }
        if (entity is VideoEntity) {
            return buildVideoFromDbo(entity)
        }
        if (entity is PostEntity) {
            return buildPostFromDbo(entity, owners)
        }
        if (entity is LinkEntity) {
            return buildLinkFromDbo(entity)
        }
        if (entity is ArticleEntity) {
            return buildArticleFromDbo(entity)
        }
        if (entity is StoryEntity) {
            return buildStoryFromDbo(entity, owners)
        }
        if (entity is PhotoAlbumEntity) {
            return mapPhotoAlbum(entity)
        }
        if (entity is GraffitiEntity) {
            return buildGraffityFromDbo(entity)
        }
        if (entity is AudioPlaylistEntity) {
            return buildAudioPlaylistFromDbo(entity)
        }
        if (entity is CallEntity) {
            return buildCallFromDbo(entity)
        }
        if (entity is WallReplyEntity) {
            return buildWallReplyDbo(entity, owners)
        }
        if (entity is NotSupportedEntity) {
            return buildNotSupportedFromDbo(entity)
        }
        if (entity is EventEntity) {
            return buildEventFromDbo(entity, owners)
        }
        if (entity is MarketEntity) {
            return buildMarketFromDbo(entity)
        }
        if (entity is MarketAlbumEntity) {
            return buildMarketAlbumFromDbo(entity)
        }
        if (entity is AudioArtistEntity) {
            return buildAudioArtistFromDbo(entity)
        }
        if (entity is PollEntity) {
            return buildPollFromDbo(entity)
        }
        if (entity is DocumentEntity) {
            return buildDocumentFromDbo(entity)
        }
        if (entity is PageEntity) {
            return buildWikiPageFromDbo(entity)
        }
        if (entity is StickerEntity) {
            return buildStickerFromDbo(entity)
        }
        if (entity is AudioEntity) {
            return buildAudioFromDbo(entity)
        }
        if (entity is TopicEntity) {
            return buildTopicFromDbo(entity, owners)
        }
        if (entity is AudioMessageEntity) {
            return map(entity)
        }
        if (entity is GiftItemEntity) {
            return buildGiftItemFromDbo(entity)
        }
        throw UnsupportedOperationException("Unsupported DBO class: " + entity.javaClass)
    }

    private fun buildAudioFromDbo(dbo: AudioEntity): Audio {
        return Audio()
            .setAccessKey(dbo.accessKey)
            .setAlbumId(dbo.albumId)
            .setAlbum_owner_id(dbo.album_owner_id)
            .setAlbum_access_key(dbo.album_access_key)
            .setArtist(dbo.artist)
            .setDeleted(dbo.isDeleted)
            .setDuration(dbo.duration)
            .setUrl(dbo.url)
            .setId(dbo.id)
            .setOwnerId(dbo.ownerId)
            .setLyricsId(dbo.lyricsId)
            .setTitle(dbo.title)
            .setGenre(dbo.genre)
            .setAlbum_title(dbo.album_title)
            .setThumb_image_big(dbo.thumb_image_big)
            .setThumb_image_little(dbo.thumb_image_little)
            .setThumb_image_very_big(dbo.thumb_image_very_big)
            .setIsHq(dbo.isHq)
            .setMain_artists(dbo.main_artists).updateDownloadIndicator()
    }

    private fun buildAudioPlaylistFromDbo(dto: AudioPlaylistEntity): AudioPlaylist {
        return AudioPlaylist()
            .setId(dto.id)
            .setOwnerId(dto.ownerId)
            .setAccess_key(dto.access_key)
            .setArtist_name(dto.artist_name)
            .setCount(dto.count)
            .setDescription(dto.description)
            .setGenre(dto.genre)
            .setYear(dto.year)
            .setTitle(dto.title)
            .setThumb_image(dto.thumb_image)
            .setUpdate_time(dto.update_time)
            .setOriginal_access_key(dto.original_access_key)
            .setOriginal_id(dto.original_id)
            .setOriginal_owner_id(dto.original_owner_id)
    }

    private fun buildGiftItemFromDbo(entity: GiftItemEntity): GiftItem {
        return GiftItem(entity.id)
            .setThumb48(entity.thumb48)
            .setThumb96(entity.thumb96)
            .setThumb256(entity.thumb256)
    }


    fun buildStickerFromDbo(entity: StickerEntity): Sticker {
        return Sticker(entity.id)
            .setImages(mapAll(entity.images, Entity2Model::map))
            .setImagesWithBackground(mapAll(entity.imagesWithBackground, Entity2Model::map))
            .setAnimations(mapAll(entity.animations) {
                mapStickerAnimation(
                    it
                )
            })
            .setAnimationUrl(entity.animationUrl)
    }

    private fun mapStickerAnimation(entity: AnimationEntity): Sticker.Animation {
        return Sticker.Animation(entity.url, entity.type)
    }


    fun map(entity: StickerSetEntity): StickerSet {
        return StickerSet(
            mapAll(entity.icon, Entity2Model::map),
            mapAll(entity.stickers) {
                buildStickerFromDbo(it)
            },
            entity.title
        )
    }

    fun map(entity: StickersKeywordsEntity): StickersKeywords {
        return StickersKeywords(
            entity.keywords,
            mapAll(entity.stickers) {
                buildStickerFromDbo(it)
            })
    }

    fun map(entity: StickerEntity.Img): Sticker.Image {
        return Sticker.Image(entity.url, entity.width, entity.height)
    }

    fun map(entity: StickerSetEntity.Img): StickerSet.Image {
        return StickerSet.Image(entity.url, entity.width, entity.height)
    }

    private fun buildWikiPageFromDbo(dbo: PageEntity): WikiPage {
        return WikiPage(dbo.id, dbo.ownerId)
            .setCreatorId(dbo.creatorId)
            .setTitle(dbo.title)
            .setSource(dbo.source)
            .setEditionTime(dbo.editionTime)
            .setCreationTime(dbo.creationTime)
            .setParent(dbo.parent)
            .setParent2(dbo.parent2)
            .setViews(dbo.views)
            .setViewUrl(dbo.viewUrl)
    }

    fun map(entity: AudioMessageEntity): VoiceMessage {
        return VoiceMessage(entity.id, entity.ownerId)
            .setAccessKey(entity.accessKey)
            .setDuration(entity.duration)
            .setLinkMp3(entity.linkMp3)
            .setLinkOgg(entity.linkOgg)
            .setWaveform(entity.waveform)
            .setTranscript(entity.transcript)
    }


    fun buildDocumentFromDbo(dbo: DocumentEntity): Document {
        val document = Document(dbo.id, dbo.ownerId)
        document.setTitle(dbo.title)
            .setSize(dbo.size)
            .setExt(dbo.ext)
            .setUrl(dbo.url)
            .setAccessKey(dbo.accessKey)
            .setDate(dbo.date).type = dbo.type
        if (dbo.photo != null) {
            document.photoPreview = buildPhotoSizesFromDbo(dbo.photo)
        }
        if (dbo.video != null) {
            document.videoPreview = VideoPreview()
                .setWidth(dbo.video.width)
                .setHeight(dbo.video.height)
                .setSrc(dbo.video.src)
        }
        if (dbo.graffiti != null) {
            document.graffiti = Document.Graffiti()
                .setHeight(dbo.graffiti.height)
                .setWidth(dbo.graffiti.width)
                .setSrc(dbo.graffiti.src)
        }
        return document
    }

    fun map(entity: PollEntity.Answer): Poll.Answer {
        return Poll.Answer(entity.id)
            .setRate(entity.rate)
            .setText(entity.text)
            .setVoteCount(entity.voteCount)
    }

    private fun buildPollFromDbo(entity: PollEntity): Poll {
        return Poll(entity.id, entity.ownerId)
            .setAnonymous(entity.isAnonymous)
            .setAnswers(mapAll(entity.answers, Entity2Model::map))
            .setBoard(entity.isBoard)
            .setCreationTime(entity.creationTime)
            .setMyAnswerIds(entity.myAnswerIds)
            .setQuestion(entity.question)
            .setVoteCount(entity.voteCount)
            .setClosed(entity.closed)
            .setAuthorId(entity.authorId)
            .setCanVote(entity.canVote)
            .setCanEdit(entity.canEdit)
            .setCanReport(entity.canReport)
            .setCanShare(entity.canShare)
            .setEndDate(entity.endDate)
            .setMultiple(entity.multiple)
            .setPhoto(entity.photo)
    }

    private fun buildLinkFromDbo(dbo: LinkEntity): Link {
        return Link()
            .setUrl(dbo.url)
            .setTitle(dbo.title)
            .setCaption(dbo.caption)
            .setDescription(dbo.description)
            .setPreviewPhoto(dbo.previewPhoto)
            .setPhoto(if (dbo.photo != null) map(dbo.photo) else null)
    }


    fun buildArticleFromDbo(dbo: ArticleEntity): Article {
        return Article(dbo.id, dbo.ownerId)
            .setAccessKey(dbo.accessKey)
            .setOwnerName(dbo.ownerName)
            .setPhoto(if (dbo.photo != null) map(dbo.photo) else null)
            .setTitle(dbo.title)
            .setSubTitle(dbo.subTitle)
            .setURL(dbo.url)
            .setIsFavorite(dbo.isFavorite)
    }

    private fun buildCallFromDbo(dbo: CallEntity): Call {
        return Call().setInitiator_id(dbo.initiator_id)
            .setReceiver_id(dbo.receiver_id)
            .setState(dbo.state)
            .setTime(dbo.time)
    }

    private fun buildWallReplyDbo(dbo: WallReplyEntity, owners: IOwnersBundle): WallReply {
        val comment = WallReply().setId(dbo.id)
            .setOwnerId(dbo.ownerId)
            .setFromId(dbo.fromId)
            .setPostId(dbo.postId)
            .setText(dbo.text)
            .setAuthor(owners.getById(dbo.fromId))
        val attachments = if (dbo.attachments.nonNullNoEmpty()) buildAttachmentsFromDbos(
            dbo.attachments,
            owners
        ) else null
        comment.attachments = attachments
        return comment
    }

    private fun buildNotSupportedFromDbo(dbo: NotSupportedEntity): NotSupported {
        return NotSupported().setType(dbo.type).setBody(dbo.body)
    }

    private fun buildEventFromDbo(dbo: EventEntity, owners: IOwnersBundle): Event {
        return Event(dbo.id).setButton_text(dbo.button_text).setText(dbo.text)
            .setSubject(owners.getById(if (dbo.id >= 0) -dbo.id else dbo.id))
    }


    fun buildMarketFromDbo(dbo: MarketEntity): Market {
        return Market(dbo.id, dbo.owner_id)
            .setAccess_key(dbo.access_key)
            .setIs_favorite(dbo.isIs_favorite)
            .setAvailability(dbo.availability)
            .setDate(dbo.date)
            .setDescription(dbo.description)
            .setDimensions(dbo.dimensions)
            .setPrice(dbo.price)
            .setSku(dbo.sku)
            .setTitle(dbo.title)
            .setWeight(dbo.weight)
            .setThumb_photo(dbo.thumb_photo)
    }

    private fun buildMarketAlbumFromDbo(dbo: MarketAlbumEntity): MarketAlbum {
        return MarketAlbum(dbo.id, dbo.owner_id)
            .setAccess_key(dbo.access_key)
            .setCount(dbo.count)
            .setTitle(dbo.title)
            .setUpdated_time(dbo.updated_time)
            .setPhoto(if (dbo.photo != null) map(dbo.photo) else null)
    }

    private fun mapArtistImage(dbo: AudioArtistImageEntity): AudioArtistImage {
        return AudioArtistImage(dbo.url, dbo.width, dbo.height)
    }

    private fun buildAudioArtistFromDbo(dbo: AudioArtistEntity): AudioArtist {
        return AudioArtist(dbo.id)
            .setName(dbo.name)
            .setPhoto(mapAll(dbo.photo) {
                mapArtistImage(
                    it
                )
            })
    }

    private fun buildStoryFromDbo(dbo: StoryEntity, owners: IOwnersBundle): Story {
        return Story().setId(dbo.id)
            .setOwnerId(dbo.ownerId)
            .setDate(dbo.date)
            .setExpires(dbo.expires)
            .setIs_expired(dbo.isIs_expired)
            .setAccessKey(dbo.accessKey)
            .setTarget_url(dbo.target_url)
            .setOwner(owners.getById(dbo.ownerId))
            .setPhoto(if (dbo.photo != null) map(dbo.photo) else null)
            .setVideo(if (dbo.video != null) buildVideoFromDbo(dbo.video) else null)
    }

    private fun buildGraffityFromDbo(dto: GraffitiEntity): Graffiti {
        return Graffiti().setId(dto.id)
            .setOwner_id(dto.owner_id)
            .setAccess_key(dto.access_key)
            .setHeight(dto.height)
            .setWidth(dto.width)
            .setUrl(dto.url)
    }


    fun buildNewsFromDbo(dbo: NewsEntity, owners: IOwnersBundle): News {
        val news = News()
            .setType(dbo.type)
            .setSourceId(dbo.sourceId)
            .setSource(owners.getById(dbo.sourceId))
            .setPostType(dbo.postType)
            .setFinalPost(dbo.isFinalPost)
            .setCopyOwnerId(dbo.copyOwnerId)
            .setCopyPostId(dbo.copyPostId)
            .setCopyPostDate(dbo.copyPostDate)
            .setDate(dbo.date)
            .setPostId(dbo.postId)
            .setText(dbo.text)
            .setCanEdit(dbo.isCanEdit)
            .setCanDelete(dbo.isCanDelete)
            .setCommentCount(dbo.commentCount)
            .setCommentCanPost(dbo.isCanPostComment)
            .setLikeCount(dbo.likesCount)
            .setUserLike(dbo.isUserLikes)
            .setCanLike(dbo.isCanLike)
            .setCanPublish(dbo.isCanPublish)
            .setRepostsCount(dbo.repostCount)
            .setUserReposted(dbo.isUserReposted)
            .setFriends(
                if (dbo.friendsTags == null) null else buildUserArray(
                    dbo.friendsTags,
                    owners
                )
            )
            .setViewCount(dbo.views)
        if (dbo.attachments.nonNullNoEmpty()) {
            news.attachments = buildAttachmentsFromDbos(dbo.attachments, owners)
        } else {
            news.attachments = Attachments()
        }
        if (dbo.copyHistory.nonNullNoEmpty()) {
            dbo.copyHistory.nonNullNoEmpty {
                val copies: MutableList<Post> = ArrayList(it.size)
                for (copyDbo in it) {
                    copies.add(buildPostFromDbo(copyDbo, owners))
                }
                news.copyHistory = copies
            }
        } else {
            news.copyHistory = emptyList()
        }
        return news
    }


    fun buildPostFromDbo(dbo: PostEntity, owners: IOwnersBundle): Post {
        val post = Post()
            .setDbid(dbo.dbid)
            .setVkid(dbo.id)
            .setOwnerId(dbo.ownerId)
            .setAuthorId(dbo.fromId)
            .setDate(dbo.date)
            .setText(dbo.text)
            .setReplyOwnerId(dbo.replyOwnerId)
            .setReplyPostId(dbo.replyPostId)
            .setFriendsOnly(dbo.isFriendsOnly)
            .setCommentsCount(dbo.commentsCount)
            .setCanPostComment(dbo.isCanPostComment)
            .setLikesCount(dbo.likesCount)
            .setUserLikes(dbo.isUserLikes)
            .setCanLike(dbo.isCanLike)
            .setCanRepost(dbo.isCanPublish)
            .setRepostCount(dbo.repostCount)
            .setUserReposted(dbo.isUserReposted)
            .setPostType(dbo.postType)
            .setSignerId(dbo.signedId)
            .setCreatorId(dbo.createdBy)
            .setCanEdit(dbo.isCanEdit)
            .setFavorite(dbo.isFavorite)
            .setCanPin(dbo.isCanPin)
            .setPinned(dbo.isPinned)
            .setViewCount(dbo.views)
        val sourceDbo = dbo.source
        if (sourceDbo != null) {
            post.source =
                PostSource(sourceDbo.type, sourceDbo.platform, sourceDbo.data, sourceDbo.url)
        }
        if (dbo.attachments.nonNullNoEmpty()) {
            post.attachments = buildAttachmentsFromDbos(dbo.attachments, owners)
        }
        dbo.copyHierarchy.nonNullNoEmpty {
            val copyCount = safeCountOf(it)
            for (copyDbo in it) {
                post.prepareCopyHierarchy(copyCount).add(buildPostFromDbo(copyDbo, owners))
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

    private fun mapSimplePrivacy(dbo: PrivacyEntity): SimplePrivacy {
        return SimplePrivacy(
            dbo.type,
            mapAll(dbo.entries) { orig ->
                SimplePrivacy.Entry(
                    orig.type,
                    orig.id,
                    orig.isAllowed
                )
            })
    }


    fun buildVideoFromDbo(entity: VideoEntity): Video {
        return Video()
            .setId(entity.id)
            .setOwnerId(entity.ownerId)
            .setAlbumId(entity.albumId)
            .setTitle(entity.title)
            .setDescription(entity.description)
            .setDuration(entity.duration)
            .setLink(entity.link)
            .setDate(entity.date)
            .setAddingDate(entity.addingDate)
            .setViews(entity.views)
            .setPlayer(entity.player)
            .setImage(entity.image)
            .setAccessKey(entity.accessKey)
            .setCommentsCount(entity.commentsCount)
            .setCanComment(entity.isCanComment)
            .setCanRepost(entity.isCanRepost)
            .setUserLikes(entity.isUserLikes)
            .setRepeat(entity.isRepeat)
            .setLikesCount(entity.likesCount)
            .setPrivacyView(if (entity.privacyView != null) mapSimplePrivacy(entity.privacyView) else null)
            .setPrivacyComment(if (entity.privacyComment != null) mapSimplePrivacy(entity.privacyComment) else null)
            .setMp4link240(entity.mp4link240)
            .setMp4link360(entity.mp4link360)
            .setMp4link480(entity.mp4link480)
            .setMp4link720(entity.mp4link720)
            .setMp4link1080(entity.mp4link1080)
            .setMp4link1440(entity.mp4link1440)
            .setMp4link2160(entity.mp4link2160)
            .setExternalLink(entity.externalLink)
            .setHls(entity.hls)
            .setLive(entity.live)
            .setPlatform(entity.platform)
            .setCanEdit(entity.isCanEdit)
            .setCanAdd(entity.isCanAdd)
            .setPrivate(entity.private)
            .setFavorite(entity.isFavorite)
    }


    fun map(dbo: PhotoEntity): Photo {
        return Photo()
            .setId(dbo.id)
            .setAlbumId(dbo.albumId)
            .setOwnerId(dbo.ownerId)
            .setWidth(dbo.width)
            .setHeight(dbo.height)
            .setText(dbo.text)
            .setDate(dbo.date)
            .setUserLikes(dbo.isUserLikes)
            .setCanComment(dbo.isCanComment)
            .setLikesCount(dbo.likesCount)
            .setCommentsCount(dbo.commentsCount)
            .setTagsCount(dbo.tagsCount)
            .setAccessKey(dbo.accessKey)
            .setDeleted(dbo.isDeleted)
            .setPostId(dbo.postId)
            .setSizes(if (dbo.sizes != null) buildPhotoSizesFromDbo(dbo.sizes) else PhotoSizes())
    }

    private fun entity2modelNullable(size: PhotoSizeEntity.Size?): PhotoSizes.Size? {
        return if (size != null) {
            PhotoSizes.Size(size.w, size.h, size.url)
        } else null
    }

    private fun buildPhotoSizesFromDbo(dbo: PhotoSizeEntity): PhotoSizes {
        return PhotoSizes()
            .setS(entity2modelNullable(dbo.s))
            .setM(entity2modelNullable(dbo.m))
            .setX(entity2modelNullable(dbo.x))
            .setO(entity2modelNullable(dbo.o))
            .setP(entity2modelNullable(dbo.p))
            .setQ(entity2modelNullable(dbo.q))
            .setR(entity2modelNullable(dbo.r))
            .setY(entity2modelNullable(dbo.y))
            .setZ(entity2modelNullable(dbo.z))
            .setW(entity2modelNullable(dbo.w))
    }


    fun fillOwnerIds(ids: VKOwnIds, dbos: List<Entity?>?) {
        if (dbos != null) {
            for (entity in dbos) {
                fillOwnerIds(ids, entity)
            }
        }
    }


    fun fillPostOwnerIds(ids: VKOwnIds, dbo: PostEntity?) {
        if (dbo != null) {
            ids.append(dbo.fromId)
            ids.append(dbo.signedId)
            ids.append(dbo.createdBy)
            fillOwnerIds(ids, dbo.attachments)
            fillOwnerIds(ids, dbo.copyHierarchy)
        }
    }

    private fun fillStoryOwnerIds(ids: VKOwnIds, dbo: StoryEntity?) {
        if (dbo != null) {
            ids.append(dbo.ownerId)
        }
    }


    fun fillOwnerIds(ids: VKOwnIds, entity: CommentEntity?) {
        fillCommentOwnerIds(ids, entity)
    }


    fun fillOwnerIds(ids: VKOwnIds, entity: Entity?) {
        when (entity) {
            is MessageEntity -> {
                fillMessageOwnerIds(ids, entity as MessageEntity?)
            }
            is PostEntity -> {
                fillPostOwnerIds(ids, entity as PostEntity?)
            }
            is StoryEntity -> {
                fillStoryOwnerIds(ids, entity as StoryEntity?)
            }
            is WallReplyEntity -> {
                fillWallReplyOwnerIds(ids, entity as WallReplyEntity?)
            }
            is EventEntity -> {
                fillEventIds(ids, entity as EventEntity?)
            }
        }
    }

    private fun fillWallReplyOwnerIds(ids: VKOwnIds, dbo: WallReplyEntity?) {
        if (dbo != null) {
            ids.append(dbo.fromId)
            if (dbo.attachments != null) {
                fillOwnerIds(ids, dbo.attachments)
            }
        }
    }

    private fun fillEventIds(ids: VKOwnIds, dbo: EventEntity?) {
        if (dbo != null) {
            ids.append(if (dbo.id >= 0) -dbo.id else dbo.id)
        }
    }


    fun fillCommentOwnerIds(ids: VKOwnIds, dbo: CommentEntity?) {
        if (dbo != null) {
            if (dbo.fromId != 0) {
                ids.append(dbo.fromId)
            }
            if (dbo.replyToUserId != 0) {
                ids.append(dbo.replyToUserId)
            }
            if (dbo.attachments != null) {
                fillOwnerIds(ids, dbo.attachments)
            }
            if (!dbo.threads.isNullOrEmpty()) {
                for (i in dbo.threads) {
                    fillCommentOwnerIds(ids, i)
                }
            }
        }
    }


    fun fillOwnerIds(ids: VKOwnIds, dbo: NewsEntity?) {
        if (dbo != null) {
            ids.append(dbo.sourceId)
            fillOwnerIds(ids, dbo.attachments)
            fillOwnerIds(ids, dbo.copyHistory)
            if (!dbo.friendsTags.isNullOrEmpty()) {
                ids.appendAll(dbo.friendsTags)
            }
        }
    }

    private fun fillMessageOwnerIds(ids: VKOwnIds, dbo: MessageEntity?) {
        if (dbo == null) {
            return
        }
        ids.append(dbo.fromId)
        ids.append(dbo.actionMemberId) // тут 100% пользователь, нюанс в том, что он может быть < 0, если email
        if (!Peer.isGroupChat(dbo.peerId)) {
            ids.append(dbo.peerId)
        }
        dbo.forwardMessages.nonNullNoEmpty {
            for (fwd in it) {
                fillMessageOwnerIds(ids, fwd)
            }
        }
        dbo.attachments.nonNullNoEmpty {
            for (attachmentEntity in it) {
                fillOwnerIds(ids, attachmentEntity)
            }
        }
    }
}