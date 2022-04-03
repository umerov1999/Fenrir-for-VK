package dev.ragnarok.fenrir.domain.mappers

import dev.ragnarok.fenrir.db.model.entity.*
import dev.ragnarok.fenrir.db.model.entity.AudioArtistEntity.AudioArtistImageEntity
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity.GraffitiDbo
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity.VideoPreviewDbo
import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity.ButtonEntity
import dev.ragnarok.fenrir.db.model.entity.PostEntity.SourceDbo
import dev.ragnarok.fenrir.db.model.entity.StickerEntity.AnimationEntity
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAndAdd
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.model.AudioArtist.AudioArtistImage

object Model2Entity {

    fun buildKeyboardEntity(keyboard: Keyboard?): KeyboardEntity? {
        if (keyboard == null || keyboard.buttons.isNullOrEmpty()) {
            return null
        }
        val buttons: MutableList<List<ButtonEntity>> = ArrayList(keyboard.buttons.size)
        for (i in keyboard.buttons) {
            val vt: MutableList<ButtonEntity> = ArrayList(i.size)
            for (s in i) {
                vt.add(
                    ButtonEntity().setType(s.type).setColor(s.color).setLabel(s.label)
                        .setLink(s.link).setPayload(s.payload)
                )
            }
            buttons.add(vt)
        }
        return KeyboardEntity().setAuthor_id(
            keyboard.author_id
        ).setInline(keyboard.inline)
            .setOne_time(keyboard.one_time).setButtons(buttons)
    }

    fun buildSimpleDialog(entity: Conversation): SimpleDialogEntity {
        return SimpleDialogEntity(entity.id)
            .setInRead(entity.inRead)
            .setOutRead(entity.outRead)
            .setPhoto50(entity.photo50)
            .setPhoto100(entity.photo100)
            .setPhoto200(entity.photo200)
            .setUnreadCount(entity.unreadCount)
            .setTitle(entity.title)
            .setPinned(if (entity.pinned == null) null else buildMessageEntity(entity.pinned))
            .setAcl(entity.acl)
            .setGroupChannel(entity.isGroupChannel)
            .setCurrentKeyboard(buildKeyboardEntity(entity.currentKeyboard))
            .setMajor_id(entity.major_id)
            .setMinor_id(entity.minor_id)
    }


    fun buildDialog(model: Dialog): DialogEntity {
        return DialogEntity(model.peerId)
            .setUnreadCount(model.unreadCount)
            .setInRead(model.inRead)
            .setOutRead(model.outRead)
            .setMessage(buildMessageEntity(model.message))
            .setLastMessageId(model.lastMessageId)
            .setTitle(model.title)
            .setGroupChannel(model.isGroupChannel)
            .setPhoto50(model.photo50)
            .setPhoto100(model.photo100)
            .setPhoto200(model.photo200)
            .setMajor_id(model.major_id)
            .setMinor_id(model.minor_id)
    }


    fun buildMessageEntity(message: Message): MessageEntity {
        return MessageEntity().set(message.id, message.peerId, message.senderId)
            .setDate(message.date)
            .setOut(message.isOut)
            .setBody(message.body)
            .setEncrypted(message.cryptStatus != CryptStatus.NO_ENCRYPTION)
            .setImportant(message.isImportant)
            .setDeleted(message.isDeleted)
            .setDeletedForAll(message.isDeletedForAll)
            .setForwardCount(message.forwardMessagesCount)
            .setHasAttachmens(message.isHasAttachments)
            .setStatus(message.status)
            .setOriginalId(message.originalId)
            .setAction(message.action)
            .setActionMemberId(message.actionMid)
            .setActionEmail(message.actionEmail)
            .setActionText(message.actionText)
            .setPhoto50(message.photo50)
            .setPhoto100(message.photo100)
            .setPhoto200(message.photo200)
            .setRandomId(message.randomId)
            .setExtras(message.extras)
            .setAttachments(if (message.attachments != null) buildEntityAttachments(message.attachments) else null)
            .setForwardMessages(
                mapAll(
                    message.fwd
                ) { buildMessageEntity(it) }
            )
            .setUpdateTime(message.updateTime)
            .setPayload(message.payload)
            .setKeyboard(buildKeyboardEntity(message.keyboard))
    }

    private fun buildEntityAttachments(attachments: Attachments): List<Entity> {
        val entities: MutableList<Entity> = ArrayList(attachments.size())
        mapAndAdd(
            attachments.audios,
            { buildAudioEntity(it) },
            entities
        )
        mapAndAdd(
            attachments.stickers,
            { buildStickerEntity(it) },
            entities
        )
        mapAndAdd(
            attachments.photos,
            { buildPhotoEntity(it) },
            entities
        )
        mapAndAdd(
            attachments.docs,
            { buildDocumentDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.voiceMessages,
            { mapAudio(it) },
            entities
        )
        mapAndAdd(
            attachments.videos,
            { buildVideoDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.posts,
            { buildPostDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.links,
            { buildLinkDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.articles,
            { buildArticleDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.stories,
            { buildStoryDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.calls,
            { buildCallDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.wallReplies,
            { buildWallReplyDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.notSupported,
            { buildNotSupportedDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.events,
            { buildEventDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.markets,
            { buildMarketDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.marketAlbums,
            { buildMarketAlbumDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.audioArtists,
            { buildAudioArtistDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.graffity,
            { buildGraffityDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.audioPlaylists,
            { buildAudioPlaylistEntity(it) },
            entities
        )
        mapAndAdd(
            attachments.polls,
            { buildPollDbo(it) },
            entities
        )
        mapAndAdd(
            attachments.pages,
            { buildPageEntity(it) },
            entities
        )
        mapAndAdd(
            attachments.photoAlbums,
            { buildPhotoAlbumEntity(it) },
            entities
        )
        mapAndAdd(
            attachments.gifts,
            { buildGiftItemEntity(it) },
            entities
        )
        return entities
    }


    fun buildDboAttachments(models: List<AbsModel>): List<Entity> {
        val entities: MutableList<Entity> = ArrayList(models.size)
        for (model in models) {
            when (model) {
                is Audio -> {
                    entities.add(buildAudioEntity(model))
                }
                is Sticker -> {
                    entities.add(buildStickerEntity(model))
                }
                is Photo -> {
                    entities.add(buildPhotoEntity(model))
                }
                is Document -> {
                    entities.add(buildDocumentDbo(model))
                }
                is Video -> {
                    entities.add(buildVideoDbo(model))
                }
                is Post -> {
                    entities.add(buildPostDbo(model))
                }
                is Link -> {
                    entities.add(buildLinkDbo(model))
                }
                is Article -> {
                    entities.add(buildArticleDbo(model))
                }
                is PhotoAlbum -> {
                    entities.add(buildPhotoAlbumEntity(model))
                }
                is Story -> {
                    entities.add(buildStoryDbo(model))
                }
                is AudioPlaylist -> {
                    entities.add(buildAudioPlaylistEntity(model))
                }
                is Call -> {
                    entities.add(buildCallDbo(model))
                }
                is NotSupported -> {
                    entities.add(buildNotSupportedDbo(model))
                }
                is Event -> {
                    entities.add(buildEventDbo(model))
                }
                is Market -> {
                    entities.add(buildMarketDbo(model))
                }
                is MarketAlbum -> {
                    entities.add(buildMarketAlbumDbo(model))
                }
                is AudioArtist -> {
                    entities.add(buildAudioArtistDbo(model))
                }
                is WallReply -> {
                    entities.add(buildWallReplyDbo(model))
                }
                is Graffiti -> {
                    entities.add(buildGraffityDbo(model))
                }
                is Poll -> {
                    entities.add(buildPollDbo(model))
                }
                is WikiPage -> {
                    entities.add(buildPageEntity(model))
                }
                is GiftItem -> {
                    entities.add(buildGiftItemEntity(model))
                }
                else -> {
                    throw UnsupportedOperationException("Unsupported model")
                }
            }
        }
        return entities
    }

    private fun buildGiftItemEntity(giftItem: GiftItem): GiftItemEntity {
        return GiftItemEntity().setId(giftItem.id)
            .setThumb256(giftItem.thumb256)
            .setThumb96(giftItem.thumb96)
            .setThumb48(giftItem.thumb48)
    }

    private fun buildPageEntity(page: WikiPage): PageEntity {
        return PageEntity().set(page.id, page.ownerId)
            .setViewUrl(page.viewUrl)
            .setViews(page.views)
            .setParent2(page.parent2)
            .setParent(page.parent)
            .setCreationTime(page.creationTime)
            .setEditionTime(page.editionTime)
            .setCreatorId(page.creatorId)
            .setSource(page.source)
    }

    private fun mapAnswer(answer: Poll.Answer): PollEntity.Answer {
        return PollEntity.Answer().set(answer.id, answer.text, answer.voteCount, answer.rate)
    }

    private fun buildPollDbo(poll: Poll): PollEntity {
        return PollEntity().set(poll.id, poll.ownerId)
            .setAnswers(
                mapAll(
                    poll.answers
                ) { mapAnswer(it) }
            )
            .setQuestion(poll.question)
            .setVoteCount(poll.voteCount)
            .setMyAnswerIds(poll.myAnswerIds)
            .setCreationTime(poll.creationTime)
            .setAnonymous(poll.isAnonymous)
            .setBoard(poll.isBoard)
            .setClosed(poll.isClosed)
            .setAuthorId(poll.authorId)
            .setCanVote(poll.isCanVote)
            .setCanEdit(poll.isCanEdit)
            .setCanReport(poll.isCanReport)
            .setCanShare(poll.isCanShare)
            .setEndDate(poll.endDate)
            .setMultiple(poll.isMultiple)
            .setPhoto(poll.photo)
    }

    private fun buildLinkDbo(link: Link): LinkEntity {
        return LinkEntity().setUrl(link.url)
            .setPhoto(if (link.photo == null) null else buildPhotoEntity(link.photo))
            .setTitle(link.title)
            .setDescription(link.description)
            .setCaption(link.caption)
            .setPreviewPhoto(link.previewPhoto)
    }

    private fun buildArticleDbo(dbo: Article): ArticleEntity {
        return ArticleEntity().set(dbo.id, dbo.ownerId)
            .setAccessKey(dbo.accessKey)
            .setOwnerName(dbo.ownerName)
            .setPhoto(if (dbo.photo == null) null else buildPhotoEntity(dbo.photo))
            .setTitle(dbo.title)
            .setSubTitle(dbo.subTitle)
            .setURL(dbo.url)
            .setIsFavorite(dbo.isFavorite)
    }

    private fun buildStoryDbo(dbo: Story): StoryEntity {
        return StoryEntity().setId(dbo.id)
            .setOwnerId(dbo.ownerId)
            .setDate(dbo.date)
            .setExpires(dbo.expires)
            .setIs_expired(dbo.isIs_expired)
            .setAccessKey(dbo.accessKey)
            .setTarget_url(dbo.target_url)
            .setPhoto(if (dbo.photo == null) null else buildPhotoEntity(dbo.photo))
            .setVideo(if (dbo.video != null) buildVideoDbo(dbo.video) else null)
    }

    private fun buildCallDbo(dbo: Call): CallEntity {
        return CallEntity().setInitiator_id(dbo.initiator_id)
            .setReceiver_id(dbo.receiver_id)
            .setState(dbo.state)
            .setTime(dbo.time)
    }

    private fun buildWallReplyDbo(dbo: WallReply): WallReplyEntity {
        val comment = WallReplyEntity().setId(dbo.id)
            .setOwnerId(dbo.ownerId)
            .setFromId(dbo.fromId)
            .setPostId(dbo.postId)
            .setText(dbo.text)
        if (dbo.attachments != null) {
            comment.attachments = buildEntityAttachments(dbo.attachments)
        } else {
            comment.attachments = null
        }
        return comment
    }

    private fun buildNotSupportedDbo(dbo: NotSupported): NotSupportedEntity {
        return NotSupportedEntity().setType(dbo.type).setBody(dbo.body)
    }

    private fun buildEventDbo(dbo: Event): EventEntity {
        return EventEntity().setId(dbo.id).setButton_text(dbo.button_text).setText(dbo.text)
    }

    private fun buildMarketDbo(dbo: Market): MarketEntity {
        return MarketEntity().set(dbo.id, dbo.owner_id)
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

    private fun buildMarketAlbumDbo(dbo: MarketAlbum): MarketAlbumEntity {
        return MarketAlbumEntity().set(dbo.id, dbo.owner_id)
            .setAccess_key(dbo.access_key)
            .setCount(dbo.count)
            .setTitle(dbo.title)
            .setUpdated_time(dbo.updated_time)
            .setPhoto(if (dbo.photo != null) buildPhotoEntity(dbo.photo) else null)
    }

    private fun mapArtistImage(dbo: AudioArtistImage): AudioArtistImageEntity {
        return AudioArtistImageEntity().set(dbo.url, dbo.width, dbo.height)
    }

    private fun buildAudioArtistDbo(dbo: AudioArtist): AudioArtistEntity {
        return AudioArtistEntity()
            .setId(dbo.id)
            .setName(dbo.name)
            .setPhoto(mapAll(dbo.photo) {
                mapArtistImage(
                    it
                )
            })
    }

    private fun buildGraffityDbo(dbo: Graffiti): GraffitiEntity {
        return GraffitiEntity().setId(dbo.id)
            .setOwner_id(dbo.owner_id)
            .setAccess_key(dbo.access_key)
            .setHeight(dbo.height)
            .setWidth(dbo.width)
            .setUrl(dbo.url)
    }


    fun buildPostDbo(post: Post): PostEntity {
        val dbo = PostEntity().set(post.vkid, post.ownerId)
            .setFromId(post.authorId)
            .setDate(post.date)
            .setText(post.text)
            .setReplyOwnerId(post.replyOwnerId)
            .setReplyPostId(post.replyPostId)
            .setFriendsOnly(post.isFriendsOnly)
            .setCommentsCount(post.commentsCount)
            .setCanPostComment(post.isCanPostComment)
            .setLikesCount(post.likesCount)
            .setUserLikes(post.isUserLikes)
            .setCanLike(post.isCanLike)
            .setCanEdit(post.isCanEdit)
            .setCanPublish(post.isCanRepost)
            .setRepostCount(post.repostCount)
            .setUserReposted(post.isUserReposted)
            .setPostType(post.postType)
            .setAttachmentsCount(if (post.attachments != null) post.attachments.size() else 0)
            .setSignedId(post.signerId)
            .setCreatedBy(post.creatorId)
            .setCanPin(post.isCanPin)
            .setPinned(post.isPinned)
            .setDeleted(post.isDeleted)
            .setViews(post.viewCount)
            .setDbid(post.dbid)
        val source = post.source
        if (source != null) {
            dbo.source = SourceDbo().set(source.type, source.platform, source.data, source.url)
        }
        if (post.attachments != null) {
            dbo.attachments = buildEntityAttachments(post.attachments)
        } else {
            dbo.attachments = null
        }
        dbo.copyHierarchy = mapAll(
            post.copyHierarchy
        ) { buildPostDbo(it) }
        return dbo
    }

    private fun buildVideoDbo(video: Video): VideoEntity {
        return VideoEntity().set(video.id, video.ownerId)
            .setAlbumId(video.albumId)
            .setTitle(video.title)
            .setDescription(video.description)
            .setLink(video.link)
            .setDate(video.date)
            .setAddingDate(video.addingDate)
            .setViews(video.views)
            .setPlayer(video.player)
            .setImage(video.image)
            .setAccessKey(video.accessKey)
            .setCommentsCount(video.commentsCount)
            .setUserLikes(video.isUserLikes)
            .setLikesCount(video.likesCount)
            .setMp4link240(video.mp4link240)
            .setMp4link360(video.mp4link360)
            .setMp4link480(video.mp4link480)
            .setMp4link720(video.mp4link720)
            .setMp4link1080(video.mp4link1080)
            .setExternalLink(video.externalLink)
            .setPlatform(video.platform)
            .setRepeat(video.isRepeat)
            .setDuration(video.duration)
            .setPrivacyView(if (video.privacyView == null) null else mapPrivacy(video.privacyView))
            .setPrivacyComment(if (video.privacyComment == null) null else mapPrivacy(video.privacyComment))
            .setCanEdit(video.isCanEdit)
            .setCanAdd(video.isCanAdd)
            .setCanComment(video.isCanComment)
            .setCanRepost(video.isCanRepost)
            .setPrivate(video.private)
    }

    private fun mapPrivacy(privacy: SimplePrivacy): PrivacyEntity {
        return PrivacyEntity().set(
            privacy.type,
            mapAll(privacy.entries) { orig ->
                PrivacyEntity.Entry().set(orig.type, orig.id, orig.isAllowed)
            })
    }

    private fun mapAudio(message: VoiceMessage): AudioMessageEntity {
        return AudioMessageEntity().set(message.id, message.ownerId)
            .setWaveform(message.waveform)
            .setLinkOgg(message.linkOgg)
            .setLinkMp3(message.linkMp3)
            .setDuration(message.duration)
            .setAccessKey(message.accessKey)
            .setTranscript(message.transcript)
    }

    private fun buildDocumentDbo(document: Document): DocumentEntity {
        val dbo = DocumentEntity().set(document.id, document.ownerId)
            .setTitle(document.title)
            .setSize(document.size)
            .setExt(document.ext)
            .setUrl(document.url)
            .setDate(document.date)
            .setType(document.type)
            .setAccessKey(document.accessKey)
        if (document.graffiti != null) {
            val graffiti = document.graffiti
            dbo.graffiti = GraffitiDbo().set(graffiti.src, graffiti.width, graffiti.height)
        }
        if (document.videoPreview != null) {
            val video = document.videoPreview
            dbo.video = VideoPreviewDbo().set(video.src, video.width, video.height, video.fileSize)
        }
        return dbo
    }

    private fun buildStickerEntity(sticker: Sticker): StickerEntity {
        return StickerEntity().setId(sticker.id)
            .setImagesWithBackground(mapAll(sticker.imagesWithBackground) {
                map(
                    it
                )
            })
            .setImages(mapAll(sticker.images) {
                map(
                    it
                )
            })
            .setAnimations(mapAll(sticker.animations) {
                mapStickerAnimation(
                    it
                )
            })
            .setAnimationUrl(sticker.animationUrl)
    }

    fun map(image: Sticker.Image): StickerEntity.Img {
        return StickerEntity.Img().set(image.url, image.width, image.height)
    }

    private fun mapStickerAnimation(dto: Sticker.Animation): AnimationEntity {
        return AnimationEntity().set(dto.url, dto.type)
    }

    private fun buildAudioEntity(audio: Audio): AudioEntity {
        return AudioEntity().set(audio.id, audio.ownerId)
            .setArtist(audio.artist)
            .setTitle(audio.title)
            .setDuration(audio.duration)
            .setUrl(audio.url)
            .setLyricsId(audio.lyricsId)
            .setAlbumId(audio.albumId)
            .setAlbum_owner_id(audio.album_owner_id)
            .setAlbum_access_key(audio.album_access_key)
            .setGenre(audio.genre)
            .setAccessKey(audio.accessKey)
            .setAlbum_title(audio.album_title)
            .setThumb_image_big(audio.thumb_image_big)
            .setThumb_image_little(audio.thumb_image_little)
            .setThumb_image_very_big(audio.thumb_image_very_big)
            .setIsHq(audio.isHq)
            .setMain_artists(audio.main_artists)
    }

    private fun buildAudioPlaylistEntity(dto: AudioPlaylist): AudioPlaylistEntity {
        return AudioPlaylistEntity()
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

    private fun buildPhotoEntity(photo: Photo): PhotoEntity {
        return PhotoEntity().set(photo.id, photo.ownerId)
            .setAlbumId(photo.albumId)
            .setWidth(photo.width)
            .setHeight(photo.height)
            .setText(photo.text)
            .setDate(photo.date)
            .setUserLikes(photo.isUserLikes)
            .setCanComment(photo.isCanComment)
            .setLikesCount(photo.likesCount)
            .setCommentsCount(photo.commentsCount)
            .setTagsCount(photo.tagsCount)
            .setAccessKey(photo.accessKey)
            .setPostId(photo.postId)
            .setDeleted(photo.isDeleted)
            .setSizes(if (photo.sizes == null) null else buildPhotoSizeEntity(photo.sizes))
    }

    private fun buildPhotoAlbumEntity(album: PhotoAlbum): PhotoAlbumEntity {
        return PhotoAlbumEntity().set(album.id, album.ownerId)
            .setSize(album.size)
            .setTitle(album.title)
            .setDescription(album.description)
            .setCanUpload(album.isCanUpload)
            .setUpdatedTime(album.updatedTime)
            .setCreatedTime(album.createdTime)
            .setSizes(if (album.sizes != null) buildPhotoSizeEntity(album.sizes) else null)
            .setPrivacyView(if (album.privacyView != null) mapPrivacy(album.privacyView) else null)
            .setPrivacyComment(if (album.privacyComment != null) mapPrivacy(album.privacyComment) else null)
            .setUploadByAdminsOnly(album.isUploadByAdminsOnly)
            .setCommentsDisabled(album.isCommentsDisabled)
    }

    private fun model2entityNullable(size: PhotoSizes.Size?): PhotoSizeEntity.Size? {
        return if (size != null) {
            PhotoSizeEntity.Size()
                .setUrl(size.url)
                .setW(size.w)
                .setH(size.h)
        } else null
    }

    private fun buildPhotoSizeEntity(sizes: PhotoSizes): PhotoSizeEntity {
        return PhotoSizeEntity()
            .setS(model2entityNullable(sizes.s))
            .setM(model2entityNullable(sizes.m))
            .setX(model2entityNullable(sizes.x))
            .setO(model2entityNullable(sizes.o))
            .setP(model2entityNullable(sizes.p))
            .setQ(model2entityNullable(sizes.q))
            .setR(model2entityNullable(sizes.r))
            .setY(model2entityNullable(sizes.y))
            .setZ(model2entityNullable(sizes.z))
            .setW(model2entityNullable(sizes.w))
    }
}