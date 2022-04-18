package dev.ragnarok.fenrir.domain.mappers

import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.VKApiConversation.CurrentKeyboard
import dev.ragnarok.fenrir.api.model.VKApiSticker.VKApiAnimation
import dev.ragnarok.fenrir.api.model.VKApiStickerSet.Product
import dev.ragnarok.fenrir.api.model.feedback.*
import dev.ragnarok.fenrir.api.model.response.FavePageResponse
import dev.ragnarok.fenrir.crypt.CryptHelper.analizeMessageBody
import dev.ragnarok.fenrir.crypt.MessageType
import dev.ragnarok.fenrir.db.model.IdPairEntity
import dev.ragnarok.fenrir.db.model.entity.*
import dev.ragnarok.fenrir.db.model.entity.AudioArtistEntity.AudioArtistImageEntity
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity.GraffitiDbo
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity.VideoPreviewDbo
import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity.ButtonEntity
import dev.ragnarok.fenrir.db.model.entity.PostEntity.SourceDbo
import dev.ragnarok.fenrir.db.model.entity.StickerEntity.AnimationEntity
import dev.ragnarok.fenrir.db.model.entity.UserDetailsEntity.RelativeEntity
import dev.ragnarok.fenrir.db.model.entity.feedback.*
import dev.ragnarok.fenrir.domain.mappers.MapUtil.calculateConversationAcl
import dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll
import dev.ragnarok.fenrir.model.CommentedType
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.MessageStatus
import dev.ragnarok.fenrir.model.feedback.FeedbackType
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.requireNonNull
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import dev.ragnarok.fenrir.util.Utils.safeCountOf

object Dto2Entity {

    fun buildFeedbackDbo(feedback: VKApiBaseFeedback): FeedbackEntity {
        when (@FeedbackType val type = FeedbackEntity2Model.transformType(feedback.type)) {
            FeedbackType.FOLLOW, FeedbackType.FRIEND_ACCEPTED -> {
                val usersNotifcation = feedback as VKApiUsersFeedback
                val usersDbo = UsersEntity(type)
                usersDbo.owners = usersNotifcation.users?.ids
                usersDbo.date = feedback.date
                return usersDbo
            }
            FeedbackType.MENTION -> {
                val mentionWallFeedback = feedback as VKApiMentionWallFeedback
                val mentionDbo = MentionEntity(type)
                val post =
                    mapPost(mentionWallFeedback.post ?: throw NullPointerException("Feedback"))
                mentionDbo.where = post
                feedback.reply.requireNonNull {
                    mentionDbo.reply =
                        mapComment(post.id, post.ownerId, CommentedType.POST, null, it)
                }
                mentionDbo.date = feedback.date
                return mentionDbo
            }
            FeedbackType.MENTION_COMMENT_POST, FeedbackType.MENTION_COMMENT_PHOTO, FeedbackType.MENTION_COMMENT_VIDEO -> {
                val mentionCommentFeedback = feedback as VKApiMentionCommentFeedback
                val entity = createFromCommentable(
                    mentionCommentFeedback.comment_of ?: throw NullPointerException("Feedback")
                )
                val mentionCommentDbo = MentionCommentEntity(type)
                mentionCommentDbo.date = feedback.date
                mentionCommentDbo.commented = entity.entity
                mentionCommentDbo.where = mapComment(
                    entity.id,
                    entity.ownerId,
                    entity.type,
                    entity.accessKey,
                    mentionCommentFeedback.where ?: throw NullPointerException("Feedback")
                )
                feedback.reply.requireNonNull {
                    mentionCommentDbo.reply = mapComment(
                        entity.id,
                        entity.ownerId,
                        entity.type,
                        entity.accessKey,
                        it
                    )
                }
                return mentionCommentDbo
            }
            FeedbackType.WALL, FeedbackType.WALL_PUBLISH -> {
                val wallFeedback = feedback as VKApiWallFeedback
                val postEntity =
                    mapPost(wallFeedback.post ?: throw NullPointerException("Feedback"))
                val postFeedbackEntity = PostFeedbackEntity(type)
                postFeedbackEntity.date = feedback.date
                postFeedbackEntity.post = postEntity
                feedback.reply.requireNonNull {
                    postFeedbackEntity.reply = mapComment(
                        postEntity.id,
                        postEntity.ownerId,
                        CommentedType.POST,
                        null,
                        it
                    )
                }
                return postFeedbackEntity
            }
            FeedbackType.COMMENT_POST, FeedbackType.COMMENT_PHOTO, FeedbackType.COMMENT_VIDEO -> {
                val commentFeedback = feedback as VKApiCommentFeedback
                val commented = createFromCommentable(
                    commentFeedback.comment_of ?: throw NullPointerException("Feedback")
                )
                val commentEntity = NewCommentEntity(type)
                commentEntity.comment = mapComment(
                    commented.id,
                    commented.ownerId,
                    commented.type,
                    commented.accessKey,
                    commentFeedback.comment ?: throw NullPointerException("Feedback")
                )
                commentEntity.commented = commented.entity
                commentEntity.date = feedback.date
                feedback.reply.requireNonNull {
                    commentEntity.reply = mapComment(
                        commented.id,
                        commented.ownerId,
                        commented.type,
                        commented.accessKey,
                        it
                    )
                }
                return commentEntity
            }
            FeedbackType.REPLY_COMMENT, FeedbackType.REPLY_COMMENT_PHOTO, FeedbackType.REPLY_COMMENT_VIDEO, FeedbackType.REPLY_TOPIC -> {
                val replyCommentFeedback = feedback as VKApiReplyCommentFeedback
                val c = createFromCommentable(
                    replyCommentFeedback.comments_of ?: throw NullPointerException("Feedback")
                )
                val replyCommentEntity = ReplyCommentEntity(type)
                replyCommentEntity.date = feedback.date
                replyCommentEntity.commented = c.entity
                replyCommentEntity.feedbackComment = mapComment(
                    c.id,
                    c.ownerId,
                    c.type,
                    c.accessKey,
                    replyCommentFeedback.feedback_comment ?: throw NullPointerException("Feedback")
                )
                replyCommentFeedback.own_comment.requireNonNull {
                    replyCommentEntity.ownComment = mapComment(
                        c.id,
                        c.ownerId,
                        c.type,
                        c.accessKey,
                        it
                    )
                }
                feedback.reply.requireNonNull {
                    replyCommentEntity.reply =
                        mapComment(c.id, c.ownerId, c.type, c.accessKey, it)
                }
                return replyCommentEntity
            }
            FeedbackType.LIKE_POST, FeedbackType.LIKE_PHOTO, FeedbackType.LIKE_VIDEO -> {
                val likeFeedback = feedback as VKApiLikeFeedback
                val likeEntity = LikeEntity(type)
                likeEntity.liked =
                    createFromLikeable(likeFeedback.liked ?: throw NullPointerException("Feedback"))
                likeEntity.likesOwnerIds = likeFeedback.users?.ids
                likeEntity.date = feedback.date
                return likeEntity
            }
            FeedbackType.LIKE_COMMENT_POST, FeedbackType.LIKE_COMMENT_PHOTO, FeedbackType.LIKE_COMMENT_VIDEO, FeedbackType.LIKE_COMMENT_TOPIC -> {
                val likeCommentFeedback = feedback as VKApiLikeCommentFeedback
                val ce = createFromCommentable(
                    likeCommentFeedback.commented ?: throw NullPointerException("Feedback")
                )
                val likeCommentEntity = LikeCommentEntity(type)
                likeCommentEntity.commented = ce.entity
                likeCommentEntity.liked = mapComment(
                    ce.id,
                    ce.ownerId,
                    ce.type,
                    ce.accessKey,
                    likeCommentFeedback.comment ?: throw NullPointerException("Feedback")
                )
                likeCommentEntity.date = feedback.date
                likeCommentEntity.likesOwnerIds = likeCommentFeedback.users?.ids
                return likeCommentEntity
            }
            FeedbackType.COPY_POST, FeedbackType.COPY_PHOTO, FeedbackType.COPY_VIDEO -> {
                val copyFeedback = feedback as VKApiCopyFeedback
                val copyEntity = CopyEntity(type)
                copyEntity.date = feedback.date
                when (type) {
                    FeedbackType.COPY_POST -> {
                        copyEntity.copied = mapPost(copyFeedback.what as VKApiPost)
                    }
                    FeedbackType.COPY_PHOTO -> {
                        copyEntity.copied = mapPhoto(copyFeedback.what as VKApiPhoto)
                    }
                    else -> {
                        copyEntity.copied = mapVideo(copyFeedback.what as VKApiVideo)
                    }
                }
                val copyPairs = listEmptyIfNull(copyFeedback.copies?.pairs)
                val copiesEntity = CopiesEntity()
                copiesEntity.pairDbos = ArrayList(copyPairs.size)
                for (idPair in copyPairs) {
                    copiesEntity.pairDbos.add(IdPairEntity().set(idPair.id, idPair.owner_id))
                }
                copyEntity.copies = copiesEntity
                return copyEntity
            }
            FeedbackType.NULL -> {}
        }
        throw UnsupportedOperationException("Unsupported feedback type: " + feedback.type)
    }

    private fun createFromLikeable(likeable: Likeable): Entity {
        if (likeable is VKApiPost) {
            return mapPost(likeable)
        }
        if (likeable is VKApiPhoto) {
            return mapPhoto(likeable)
        }
        if (likeable is VKApiVideo) {
            return mapVideo(likeable)
        }
        throw UnsupportedOperationException("Unsupported commentable type: $likeable")
    }

    private fun createFromCommentable(commentable: Commentable): CEntity {
        if (commentable is VKApiPost) {
            val entity = mapPost(commentable)
            return CEntity(entity.id, entity.ownerId, CommentedType.POST, null, entity)
        }
        if (commentable is VKApiPhoto) {
            val entity = mapPhoto(commentable)
            return CEntity(entity.id, entity.ownerId, CommentedType.PHOTO, entity.accessKey, entity)
        }
        if (commentable is VKApiVideo) {
            val entity = mapVideo(commentable)
            return CEntity(entity.id, entity.ownerId, CommentedType.VIDEO, entity.accessKey, entity)
        }
        if (commentable is VKApiTopic) {
            val entity = buildTopicDbo(commentable)
            return CEntity(entity.id, entity.ownerId, CommentedType.TOPIC, null, entity)
        }
        throw UnsupportedOperationException("Unsupported commentable type: $commentable")
    }


    fun buildVideoAlbumDbo(dto: VKApiVideoAlbum): VideoAlbumEntity {
        return VideoAlbumEntity(dto.id, dto.owner_id)
            .setUpdateTime(dto.updated_time)
            .setCount(dto.count)
            .setImage(dto.image)
            .setTitle(dto.title)
            .setPrivacy(if (dto.privacy != null) mapPrivacy(dto.privacy) else null)
    }


    fun buildTopicDbo(dto: VKApiTopic): TopicEntity {
        return TopicEntity().set(dto.id, dto.owner_id)
            .setTitle(dto.title)
            .setCreatedTime(dto.created)
            .setCreatorId(dto.created_by)
            .setLastUpdateTime(dto.updated)
            .setUpdatedBy(dto.updated_by)
            .setClosed(dto.is_closed)
            .setFixed(dto.is_fixed)
            .setCommentsCount(if (dto.comments != null) dto.comments.count else 0)
            .setFirstComment(dto.first_comment)
            .setLastComment(dto.last_comment)
            .setPoll(null)
    }


    fun buildPhotoAlbumDbo(dto: VKApiPhotoAlbum): PhotoAlbumEntity {
        return PhotoAlbumEntity().set(dto.id, dto.owner_id)
            .setTitle(dto.title)
            .setSize(dto.size)
            .setDescription(dto.description)
            .setCanUpload(dto.can_upload)
            .setUpdatedTime(dto.updated)
            .setCreatedTime(dto.created)
            .setSizes(if (dto.photo != null) mapPhotoSizes(dto.photo) else null)
            .setCommentsDisabled(dto.comments_disabled)
            .setUploadByAdminsOnly(dto.upload_by_admins_only)
            .setPrivacyView(if (dto.privacy_view != null) mapPrivacy(dto.privacy_view) else null)
            .setPrivacyComment(if (dto.privacy_comment != null) mapPrivacy(dto.privacy_comment) else null)
    }


    fun mapOwners(users: List<VKApiUser>?, communities: List<VKApiCommunity>?): OwnerEntities {
        return OwnerEntities(mapUsers(users), mapCommunities(communities))
    }


    fun mapCommunities(communities: List<VKApiCommunity>?): List<CommunityEntity> {
        return mapAll(
            communities
        ) {
            mapCommunity(it)
        }
    }


    fun mapUsers(users: List<VKApiUser>?): List<UserEntity> {
        return mapAll(
            users
        ) {
            mapUser(it)
        }
    }


    fun mapCommunity(community: VKApiCommunity): CommunityEntity {
        return CommunityEntity(community.id)
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


    fun mapFavePage(favePage: FavePageResponse): FavePageEntity {
        var id = 0
        favePage.user.requireNonNull {
            id = it.id
        }
        favePage.group.requireNonNull {
            id = -it.id
        }
        return FavePageEntity(id)
            .setDescription(favePage.description)
            .setUpdateDate(favePage.updated_date)
            .setFaveType(favePage.type)
            .setGroup(favePage.group?.let { mapCommunity(it) })
            .setUser(favePage.user?.let { mapUser(it) })
    }


    fun mapUser(user: VKApiUser): UserEntity {
        return UserEntity(user.id)
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
            .setBlacklisted_by_me(user.blacklisted_by_me)
            .setBlacklisted(user.blacklisted)
            .setCan_access_closed(user.can_access_closed)
            .setVerified(user.verified)
            .setMaiden_name(user.maiden_name)
    }


    fun mapCommunityDetails(dto: VKApiCommunity): CommunityDetailsEntity {
        val details = CommunityDetailsEntity()
            .setCanMessage(dto.can_message)
            .setStatus(dto.status)
            .setStatusAudio(if (dto.status_audio != null) mapAudio(dto.status_audio) else null)
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
            val cover = CommunityDetailsEntity.Cover()
                .setEnabled(dto.cover.enabled)
                .setImages(ArrayList(safeCountOf(dto.cover.images)))
            if (dto.cover.images != null) {
                for (imageDto in dto.cover.images) {
                    cover.images.add(
                        CommunityDetailsEntity.CoverImage()
                            .set(imageDto.url, imageDto.height, imageDto.width)
                    )
                }
            }
            details.cover = cover
        } else {
            details.cover = CommunityDetailsEntity.Cover().setEnabled(false)
        }
        details.description = dto.description
        return details
    }


    fun mapUserDetails(user: VKApiUser): UserDetailsEntity {
        val dbo = UserDetailsEntity()
        try {
            if (user.photo_id.nonNullNoEmpty()) {
                val dividerIndex = user.photo_id.indexOf('_')
                if (dividerIndex != -1) {
                    val photoId = user.photo_id.substring(dividerIndex + 1).toInt()
                    dbo.photoId = IdPairEntity().set(photoId, user.id)
                }
            }
        } catch (ignored: Exception) {
        }
        dbo.statusAudio = if (user.status_audio != null) mapAudio(user.status_audio) else null
        dbo.bdate = user.bdate
        dbo.city = if (user.city == null) null else mapCity(user.city)
        dbo.country = if (user.country == null) null else mapCountry(user.country)
        dbo.homeTown = user.home_town
        dbo.phone = user.mobile_phone
        dbo.homePhone = user.home_phone
        dbo.skype = user.skype
        dbo.instagram = user.instagram
        dbo.facebook = user.facebook
        dbo.twitter = user.twitter
        val counters = user.counters
        if (counters != null) {
            dbo.setFriendsCount(counters.friends)
                .setOnlineFriendsCount(counters.online_friends)
                .setMutualFriendsCount(counters.mutual_friends)
                .setFollowersCount(counters.followers)
                .setGroupsCount(counters.groups.coerceAtLeast(counters.pages + counters.subscriptions))
                .setPhotosCount(counters.photos)
                .setAudiosCount(counters.audios)
                .setVideosCount(counters.videos)
                .setArticlesCount(counters.articles)
                .setProductsCount(counters.market)
                .setGiftCount(counters.gifts)
                .setAllWallCount(counters.all_wall)
                .setOwnWallCount(counters.owner_wall).postponedWallCount = counters.postponed_wall
        }
        dbo.militaries = mapAll(
            user.militaries
        ) {
            mapMilitary(
                it
            )
        }
        dbo.careers = mapAll(
            user.careers
        ) {
            mapCareer(it)
        }
        dbo.universities = mapAll(
            user.universities
        ) {
            mapUniversity(
                it
            )
        }
        dbo.schools = mapAll(
            user.schools
        ) {
            mapSchool(it)
        }
        dbo.relatives = mapAll(
            user.relatives
        ) {
            mapUserRelative(
                it
            )
        }
        dbo.relation = user.relation
        dbo.relationPartnerId = if (user.relation_partner == null) 0 else user.relation_partner.id
        dbo.languages = user.langs
        dbo.political = user.political
        dbo.peopleMain = user.people_main
        dbo.lifeMain = user.life_main
        dbo.smoking = user.smoking
        dbo.alcohol = user.alcohol
        dbo.inspiredBy = user.inspired_by
        dbo.religion = user.religion
        dbo.site = user.site
        dbo.interests = user.interests
        dbo.music = user.music
        dbo.activities = user.activities
        dbo.movies = user.movies
        dbo.tv = user.tv
        dbo.games = user.games
        dbo.quotes = user.quotes
        dbo.about = user.about
        dbo.books = user.books
        dbo.setFavorite(user.is_favorite)
        dbo.setSubscribed(user.is_subscribed)
        return dbo
    }

    private fun mapUserRelative(relative: VKApiUser.Relative): RelativeEntity {
        return RelativeEntity()
            .setId(relative.id)
            .setType(relative.type)
            .setName(relative.name)
    }

    private fun mapSchool(dto: VKApiSchool): SchoolEntity {
        return SchoolEntity()
            .setCityId(dto.city_id)
            .setClazz(dto.clazz)
            .setCountryId(dto.country_id)
            .setFrom(dto.year_from)
            .setTo(dto.year_to)
            .setYearGraduated(dto.year_graduated)
            .setId(dto.id)
            .setName(dto.name)
    }

    private fun mapUniversity(dto: VKApiUniversity): UniversityEntity {
        return UniversityEntity()
            .setId(dto.id)
            .setCityId(dto.city_id)
            .setCountryId(dto.country_id)
            .setName(dto.name)
            .setStatus(dto.education_status)
            .setForm(dto.education_form)
            .setFacultyId(dto.faculty)
            .setFacultyName(dto.faculty_name)
            .setChairId(dto.chair)
            .setChairName(dto.chair_name)
            .setGraduationYear(dto.graduation)
    }

    private fun mapMilitary(dto: VKApiMilitary): MilitaryEntity {
        return MilitaryEntity()
            .setCountryId(dto.country_id)
            .setFrom(dto.from)
            .setUnit(dto.unit)
            .setUnitId(dto.unit_id)
            .setUntil(dto.until)
    }

    private fun mapCareer(dto: VKApiCareer): CareerEntity {
        return CareerEntity()
            .setCityId(dto.city_id)
            .setCompany(dto.company)
            .setCountryId(dto.country_id)
            .setFrom(dto.from)
            .setUntil(dto.until)
            .setPosition(dto.position)
            .setGroupId(dto.group_id)
    }

    private fun mapCountry(dto: VKApiCountry): CountryEntity {
        return CountryEntity().set(dto.id, dto.title)
    }

    private fun mapCity(dto: VKApiCity): CityEntity {
        return CityEntity()
            .setArea(dto.area)
            .setId(dto.id)
            .setImportant(dto.important)
            .setTitle(dto.title)
            .setRegion(dto.region)
    }


    fun mapNews(news: VKApiNews): NewsEntity {
        val entity = NewsEntity()
            .setType(news.type)
            .setSourceId(news.source_id)
            .setDate(news.date)
            .setPostId(news.post_id)
            .setPostType(news.post_type)
            .setFinalPost(news.final_post)
            .setCopyOwnerId(news.copy_owner_id)
            .setCopyPostId(news.copy_post_id)
            .setCopyPostDate(news.copy_post_date)
            .setText(news.text)
            .setCanEdit(news.can_edit)
            .setCanDelete(news.can_delete)
            .setCommentCount(news.comment_count)
            .setCanPostComment(news.comment_can_post)
            .setLikesCount(news.like_count)
            .setUserLikes(news.user_like)
            .setCanLike(news.can_like)
            .setCanPublish(news.can_publish)
            .setRepostCount(news.reposts_count)
            .setUserReposted(news.user_reposted)
            .setGeoId(if (news.geo != null) news.geo.id else 0)
            .setFriendsTags(news.friends)
            .setViews(news.views)
        if (news.hasAttachments()) {
            entity.attachments = mapAttachemntsList(news.attachments)
        } else {
            entity.attachments = emptyList()
        }
        entity.copyHistory = mapAll(
            news.copy_history
        ) {
            mapPost(it)
        }
        return entity
    }


    fun mapComment(
        sourceId: Int,
        sourceOwnerId: Int,
        sourceType: Int,
        sourceAccessKey: String?,
        comment: VKApiComment
    ): CommentEntity {
        var attachmentsEntities: List<Entity>? = null
        if (comment.attachments != null) {
            attachmentsEntities = mapAttachemntsList(comment.attachments)
        }
        return CommentEntity().set(sourceId, sourceOwnerId, sourceType, sourceAccessKey, comment.id)
            .setFromId(comment.from_id)
            .setDate(comment.date)
            .setText(comment.text)
            .setReplyToUserId(comment.reply_to_user)
            .setReplyToComment(comment.reply_to_comment)
            .setLikesCount(comment.likes)
            .setUserLikes(comment.user_likes)
            .setCanLike(comment.can_like)
            .setCanEdit(comment.can_edit)
            .setDeleted(false)
            .setAttachmentsCount(comment.attachmentsCount)
            .setAttachments(attachmentsEntities)
            .setThreadsCount(comment.threads_count)
            .setThreads(
                mapComments(
                    sourceId,
                    sourceOwnerId,
                    sourceType,
                    sourceAccessKey,
                    comment.threads
                )
            )
            .setPid(comment.pid)
    }

    private fun mapComments(
        sourceId: Int,
        sourceOwnerId: Int,
        sourceType: Int,
        sourceAccessKey: String?,
        comments: List<VKApiComment>?
    ): List<CommentEntity>? {
        if (comments.isNullOrEmpty()) {
            return null
        }
        val o: MutableList<CommentEntity> = ArrayList(comments.size)
        for (i in comments) {
            o.add(mapComment(sourceId, sourceOwnerId, sourceType, sourceAccessKey, i))
        }
        return o
    }


    fun mapConversation(dto: VKApiConversation): SimpleDialogEntity {
        val entity = SimpleDialogEntity(dto.peer.id)
            .setInRead(dto.inRead)
            .setOutRead(dto.outRead)
            .setUnreadCount(dto.unreadCount)
            .setLastMessageId(dto.lastMessageId)
            .setAcl(calculateConversationAcl(dto))
        if (dto.settings != null) {
            entity.title = dto.settings.title
            if (dto.settings.pinnedMesage != null) {
                entity.pinned = mapMessage(dto.settings.pinnedMesage)
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


    fun mapDialog(dto: VKApiDialog): DialogEntity {
        val messageEntity = mapMessage(dto.lastMessage)
        val entity = DialogEntity(messageEntity.peerId)
            .setLastMessageId(messageEntity.id)
            .setMessage(messageEntity)
            .setInRead(dto.conversation.inRead)
            .setOutRead(dto.conversation.outRead)
            .setUnreadCount(dto.conversation.unreadCount)
            .setAcl(calculateConversationAcl(dto.conversation))
        if (dto.conversation.settings != null) {
            entity.title = dto.conversation.settings.title
            entity.isGroupChannel = dto.conversation.settings.is_group_channel
            if (dto.conversation.settings.pinnedMesage != null) {
                entity.pinned = mapMessage(dto.conversation.settings.pinnedMesage)
            }
            if (dto.conversation.settings.photo != null) {
                entity.setPhoto50(dto.conversation.settings.photo.photo50)
                    .setPhoto100(dto.conversation.settings.photo.photo100).photo200 =
                    dto.conversation.settings.photo.photo200
            }
        }
        if (dto.conversation.sort_id != null) {
            entity.major_id = dto.conversation.sort_id.major_id
            entity.minor_id = dto.conversation.sort_id.minor_id
        }
        entity.currentKeyboard = mapKeyboard(dto.conversation.current_keyboard)
        return entity
    }

    private fun mapKeyboard(keyboard: CurrentKeyboard?): KeyboardEntity? {
        if (keyboard == null || keyboard.buttons.isNullOrEmpty()) {
            return null
        }
        val buttons: MutableList<List<ButtonEntity>> = ArrayList()
        for (i in keyboard.buttons) {
            val v: MutableList<ButtonEntity> = ArrayList()
            for (s in i) {
                if (s.action == null || "text" != s.action.type && "open_link" != s.action.type) {
                    continue
                }
                v.add(
                    ButtonEntity().setType(s.action.type).setColor(s.color).setLabel(s.action.label)
                        .setLink(s.action.link).setPayload(s.action.payload)
                )
            }
            if (v.isNotEmpty()) {
                buttons.add(v)
            }
        }
        return if (buttons.nonNullNoEmpty()) {
            KeyboardEntity().setAuthor_id(keyboard.author_id)
                .setInline(keyboard.inline)
                .setOne_time(keyboard.one_time)
                .setButtons(buttons)
        } else null
    }

    private fun mapAttachemntsList(attachments: VKApiAttachments): List<Entity>? {
        val entries = attachments.entryList()
        if (entries.isEmpty()) {
            return null
        }
        if (entries.size == 1) {
            return listOf(mapAttachment(entries[0].attachment))
        }
        val entities: MutableList<Entity> = ArrayList(entries.size)
        for (entry in entries) {
            entities.add(mapAttachment(entry.attachment))
        }
        return entities
    }

    private fun mapAttachment(dto: VKApiAttachment): Entity {
        if (dto is VKApiPhoto) {
            return mapPhoto(dto)
        }
        if (dto is VKApiVideo) {
            return mapVideo(dto)
        }
        if (dto is VKApiDoc) {
            return mapDoc(dto)
        }
        if (dto is VKApiLink) {
            return mapLink(dto)
        }
        if (dto is VKApiArticle) {
            return mapArticle(dto)
        }
        if (dto is VKApiAudioPlaylist) {
            return mapAudioPlaylist(dto)
        }
        if (dto is VKApiStory) {
            return mapStory(dto)
        }
        if (dto is VKApiGraffiti) {
            return mapGraffity(dto)
        }
        if (dto is VKApiPhotoAlbum) {
            return buildPhotoAlbumDbo(dto)
        }
        if (dto is VKApiCall) {
            return mapCall(dto)
        }
        if (dto is VKApiWallReply) {
            return mapWallReply(dto)
        }
        if (dto is VKApiNotSupported) {
            return mapNotSupported(dto)
        }
        if (dto is VKApiEvent) {
            return mapEvent(dto)
        }
        if (dto is VKApiMarket) {
            return mapMarket(dto)
        }
        if (dto is VKApiMarketAlbum) {
            return mapMarketAlbum(dto)
        }
        if (dto is VKApiAudioArtist) {
            return mapAudioArtist(dto)
        }
        if (dto is VKApiWikiPage) {
            return mapWikiPage(dto)
        }
        if (dto is VKApiSticker) {
            return mapSticker(dto)
        }
        if (dto is VKApiPost) {
            return mapPost(dto)
        }
        if (dto is VKApiPoll) {
            return buildPollEntity(dto)
        }
        if (dto is VKApiAudio) {
            return mapAudio(dto)
        }
        if (dto is VKApiAudioMessage) {
            return mapAudioMessage(dto)
        }
        if (dto is VKApiGiftItem) {
            return mapGiftItem(dto)
        }
        throw UnsupportedOperationException("Unsupported attachment, class: " + dto.javaClass)
    }

    private fun mapAudio(dto: VKApiAudio): AudioEntity {
        return AudioEntity().set(dto.id, dto.owner_id)
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
            .setAlbum_title(dto.album_title)
            .setThumb_image_big(dto.thumb_image_big)
            .setThumb_image_little(dto.thumb_image_little)
            .setThumb_image_very_big(dto.thumb_image_very_big)
            .setIsHq(dto.isHq)
            .setMain_artists(dto.main_artists)
    }

    private fun mapPollAnswer(dto: VKApiPoll.Answer): PollEntity.Answer {
        return PollEntity.Answer().set(dto.id, dto.text, dto.votes, dto.rate)
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

    private fun buildPollEntity(dto: VKApiPoll): PollEntity {
        return PollEntity().set(dto.id, dto.owner_id)
            .setAnonymous(dto.anonymous)
            .setAnswers(
                mapAll(
                    dto.answers
                ) {
                    mapPollAnswer(it)
                }
            )
            .setBoard(dto.is_board)
            .setCreationTime(dto.created)
            .setMyAnswerIds(dto.answer_ids)
            .setVoteCount(dto.votes)
            .setQuestion(dto.question)
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


    fun mapPost(dto: VKApiPost): PostEntity {
        val dbo = PostEntity().set(dto.id, dto.owner_id)
            .setFromId(dto.from_id)
            .setDate(dto.date)
            .setText(dto.text)
            .setReplyOwnerId(dto.reply_owner_id)
            .setReplyPostId(dto.reply_post_id)
            .setFriendsOnly(dto.friends_only)
            .setCommentsCount(if (dto.comments != null) dto.comments.count else 0)
            .setCanPostComment(dto.comments != null && dto.comments.canPost)
            .setLikesCount(dto.likes_count)
            .setUserLikes(dto.user_likes)
            .setCanLike(dto.can_like)
            .setCanEdit(dto.can_edit)
            .setFavorite(dto.is_favorite)
            .setCanPublish(dto.can_publish)
            .setRepostCount(dto.reposts_count)
            .setUserReposted(dto.user_reposted)
            .setPostType(dto.post_type)
            .setAttachmentsCount(dto.attachmentsCount)
            .setSignedId(dto.signer_id)
            .setCreatedBy(dto.created_by)
            .setCanPin(dto.can_pin)
            .setPinned(dto.is_pinned)
            .setDeleted(false) // cant be deleted
            .setViews(dto.views)
        val source = dto.post_source
        if (source != null) {
            dbo.source = SourceDbo().set(source.type, source.platform, source.data, source.url)
        }
        if (dto.hasAttachments()) {
            dbo.attachments = mapAttachemntsList(dto.attachments)
        } else {
            dbo.attachments = null
        }
        if (dto.hasCopyHistory()) {
            dbo.copyHierarchy = mapAll(
                dto.copy_history
            ) {
                mapPost(it)
            }
        } else {
            dbo.copyHierarchy = null
        }
        return dbo
    }


    fun mapSticker(sticker: VKApiSticker): StickerEntity {
        return StickerEntity().setId(sticker.sticker_id)
            .setImages(
                mapAll(
                    sticker.images
                ) {
                    mapStickerImage(it)
                }
            )
            .setImagesWithBackground(
                mapAll(
                    sticker.images_with_background
                ) {
                    mapStickerImage(it)
                }
            )
            .setAnimations(
                mapAll(
                    sticker.animations
                ) {
                    mapStickerAnimation(it)
                }
            )
            .setAnimationUrl(sticker.animation_url)
    }


    fun mapStikerSet(dto: Product): StickerSetEntity {
        return StickerSetEntity(dto.id)
            .setTitle(dto.title)
            .setPromoted(dto.promoted)
            .setActive(dto.active)
            .setPurchased(dto.purchased)
            .setIcon(
                mapAll(
                    dto.icon
                ) {
                    map(it)
                }
            )
            .setStickers(
                mapAll(
                    dto.stickers
                ) {
                    mapSticker(it)
                }
            )
    }

    private fun mapStickerImage(dto: VKApiSticker.Image): StickerEntity.Img {
        return StickerEntity.Img().set(dto.url, dto.width, dto.height)
    }

    private fun mapArtistImage(dto: VKApiAudioArtist.Image): AudioArtistImageEntity {
        return AudioArtistImageEntity().set(dto.url, dto.width, dto.height)
    }

    fun map(dto: VKApiStickerSet.Image): StickerSetEntity.Img {
        return StickerSetEntity.Img().set(dto.url, dto.width, dto.height)
    }

    private fun mapStickerAnimation(dto: VKApiAnimation): AnimationEntity {
        return AnimationEntity().set(dto.url, dto.type)
    }

    private fun mapWikiPage(dto: VKApiWikiPage): PageEntity {
        return PageEntity().set(dto.id, dto.owner_id)
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

    private fun mapLink(link: VKApiLink): LinkEntity {
        return LinkEntity().setUrl(link.url)
            .setCaption(link.caption)
            .setDescription(link.description)
            .setTitle(link.title)
            .setPreviewPhoto(link.preview_photo)
            .setPhoto(if (link.photo != null) mapPhoto(link.photo) else null)
    }


    fun mapArticle(article: VKApiArticle): ArticleEntity {
        return ArticleEntity().set(article.id, article.owner_id)
            .setAccessKey(article.access_key)
            .setOwnerName(article.owner_name)
            .setPhoto(article.photo?.let { mapPhoto(it) })
            .setTitle(article.title)
            .setSubTitle(article.subtitle)
            .setURL(article.url)
            .setIsFavorite(article.is_favorite)
    }

    private fun mapStory(dto: VKApiStory): StoryEntity {
        return StoryEntity().setId(dto.id)
            .setOwnerId(dto.owner_id)
            .setDate(dto.date)
            .setExpires(dto.expires_at)
            .setIs_expired(dto.is_expired)
            .setAccessKey(dto.access_key)
            .setTarget_url(dto.target_url)
            .setPhoto(if (dto.photo != null) mapPhoto(dto.photo) else null)
            .setVideo(if (dto.video != null) mapVideo(dto.video) else null)
    }

    private fun mapWallReply(dto: VKApiWallReply): WallReplyEntity {
        val attachmentsEntities: List<Entity>? = if (dto.attachments != null) {
            mapAttachemntsList(dto.attachments)
        } else {
            null
        }
        return WallReplyEntity().setId(dto.id)
            .setOwnerId(dto.owner_id)
            .setAttachments(attachmentsEntities)
            .setFromId(dto.from_id)
            .setPostId(dto.post_id)
            .setText(dto.text)
    }

    private fun mapGraffity(dto: VKApiGraffiti): GraffitiEntity {
        return GraffitiEntity().setId(dto.id)
            .setOwner_id(dto.owner_id)
            .setAccess_key(dto.access_key)
            .setHeight(dto.height)
            .setWidth(dto.width)
            .setUrl(dto.url)
    }

    private fun mapCall(dto: VKApiCall): CallEntity {
        return CallEntity().setInitiator_id(dto.initiator_id)
            .setReceiver_id(dto.receiver_id)
            .setState(dto.state)
            .setTime(dto.time)
    }

    private fun mapNotSupported(dto: VKApiNotSupported): NotSupportedEntity {
        return NotSupportedEntity().setType(dto.type).setBody(dto.body)
    }

    private fun mapEvent(dto: VKApiEvent): EventEntity {
        return EventEntity().setId(dto.id).setButton_text(dto.button_text).setText(dto.text)
    }


    fun mapMarket(dto: VKApiMarket): MarketEntity {
        return MarketEntity().set(dto.id, dto.owner_id)
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

    private fun mapMarketAlbum(dto: VKApiMarketAlbum): MarketAlbumEntity {
        return MarketAlbumEntity().set(dto.id, dto.owner_id)
            .setAccess_key(dto.access_key)
            .setCount(dto.count)
            .setTitle(dto.title)
            .setUpdated_time(dto.updated_time)
            .setPhoto(if (dto.photo != null) mapPhoto(dto.photo) else null)
    }

    private fun mapAudioArtist(dto: VKApiAudioArtist): AudioArtistEntity {
        return AudioArtistEntity()
            .setId(dto.id)
            .setName(dto.name)
            .setPhoto(
                mapAll(
                    dto.photo
                ) {
                    mapArtistImage(it)
                }
            )
    }

    private fun mapAudioMessage(dto: VKApiAudioMessage): AudioMessageEntity {
        return AudioMessageEntity().set(dto.id, dto.owner_id)
            .setAccessKey(dto.access_key)
            .setDuration(dto.duration)
            .setLinkMp3(dto.linkMp3)
            .setLinkOgg(dto.linkOgg)
            .setWaveform(dto.waveform)
            .setTranscript(dto.transcript)
    }


    fun mapDoc(dto: VKApiDoc): DocumentEntity {
        val dbo = DocumentEntity().set(dto.id, dto.ownerId)
            .setTitle(dto.title)
            .setSize(dto.size)
            .setExt(dto.ext)
            .setUrl(dto.url)
            .setDate(dto.date)
            .setType(dto.type)
            .setAccessKey(dto.accessKey)
        if (dto.preview != null) {
            if (dto.preview.photo != null && dto.preview.photo.sizes != null) {
                dbo.photo = mapPhotoSizes(dto.preview.photo.sizes)
            }
            if (dto.preview.video != null) {
                val video = dto.preview.video
                dbo.video =
                    VideoPreviewDbo().set(video.src, video.width, video.height, video.fileSize)
            }
            if (dto.preview.graffiti != null) {
                val graffiti = dto.preview.graffiti
                dbo.graffiti = GraffitiDbo().set(graffiti.src, graffiti.width, graffiti.height)
            }
        }
        return dbo
    }


    fun mapMessage(dto: VKApiMessage): MessageEntity {
        val encrypted = analizeMessageBody(dto.body) == MessageType.CRYPTED
        var randomId = 0
        dto.random_id.nonNullNoEmpty {
            try {
                randomId = it.toInt()
            } catch (ignored: NumberFormatException) {
            }
        }
        val entity = MessageEntity().set(dto.id, dto.peer_id, dto.from_id)
            .setDate(dto.date)
            .setOut(dto.out)
            .setBody(dto.body)
            .setEncrypted(encrypted)
            .setImportant(dto.important)
            .setKeyboard(mapKeyboard(dto.keyboard))
            .setDeleted(dto.deleted)
            .setDeletedForAll(false) // cant be deleted for all?
            .setForwardCount(safeCountOf(dto.fwd_messages))
            .setHasAttachmens(dto.attachments != null && !dto.attachments.isEmpty)
            .setStatus(MessageStatus.SENT) // only sent can be
            .setOriginalId(dto.id)
            .setAction(Message.fromApiChatAction(dto.action))
            .setActionMemberId(dto.action_mid)
            .setActionEmail(dto.action_email)
            .setActionText(dto.action_text)
            .setPhoto50(dto.action_photo_50)
            .setPhoto100(dto.action_photo_100)
            .setPhoto200(dto.action_photo_200)
            .setRandomId(randomId)
            .setUpdateTime(dto.update_time)
            .setPayload(dto.payload)
        if (entity.isHasAttachmens) {
            entity.attachments = mapAttachemntsList(dto.attachments)
        } else {
            entity.attachments = null
        }
        if (dto.fwd_messages.nonNullNoEmpty()) {
            if (dto.fwd_messages.size == 1) {
                entity.forwardMessages = listOf(mapMessage(dto.fwd_messages[0]))
            } else {
                val fwds: MutableList<MessageEntity> = ArrayList(dto.fwd_messages.size)
                for (f in dto.fwd_messages) {
                    fwds.add(mapMessage(f))
                }
                entity.forwardMessages = fwds
            }
        } else {
            entity.forwardMessages = null
        }
        return entity
    }


    fun mapVideo(dto: VKApiVideo): VideoEntity {
        return VideoEntity().set(dto.id, dto.owner_id)
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
            .setPrivacyView(if (dto.privacy_view != null) mapPrivacy(dto.privacy_view) else null)
            .setPrivacyComment(if (dto.privacy_comment != null) mapPrivacy(dto.privacy_comment) else null)
            .setMp4link240(dto.mp4_240)
            .setMp4link360(dto.mp4_360)
            .setMp4link480(dto.mp4_480)
            .setMp4link720(dto.mp4_720)
            .setMp4link1080(dto.mp4_1080)
            .setMp4link1440(dto.mp4_1440)
            .setMp4link2160(dto.mp4_2160)
            .setExternalLink(dto.external)
            .setHls(dto.hls)
            .setLive(dto.live)
            .setPlatform(dto.platform)
            .setCanEdit(dto.can_edit)
            .setCanAdd(dto.can_add)
            .setPrivate(dto.is_private)
            .setFavorite(dto.is_favorite)
    }

    private fun mapPrivacy(dto: VKApiPrivacy): PrivacyEntity {
        return PrivacyEntity().set(
            dto.category,
            mapAll(
                dto.entries
            ) {
                PrivacyEntity.Entry().set(it.type, it.id, it.allowed)
            }
        )
    }

    private fun mapAudioPlaylist(dto: VKApiAudioPlaylist): AudioPlaylistEntity {
        return AudioPlaylistEntity()
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

    private fun mapGiftItem(dto: VKApiGiftItem): GiftItemEntity {
        return GiftItemEntity().setId(dto.id)
            .setThumb48(dto.thumb_48)
            .setThumb96(dto.thumb_96)
            .setThumb256(dto.thumb_256)
    }


    fun mapPhoto(dto: VKApiPhoto): PhotoEntity {
        return PhotoEntity().set(dto.id, dto.owner_id)
            .setAlbumId(dto.album_id)
            .setWidth(dto.width)
            .setHeight(dto.height)
            .setText(dto.text)
            .setDate(dto.date)
            .setUserLikes(dto.user_likes)
            .setCanComment(dto.can_comment)
            .setLikesCount(dto.likes)
            .setCommentsCount(if (dto.comments != null) dto.comments.count else 0)
            .setTagsCount(dto.tags)
            .setAccessKey(dto.access_key)
            .setPostId(dto.post_id)
            .setDeleted(false) //cant bee deleted
            .setSizes(mapPhotoSizes(dto.sizes))
    }

    private fun mapPhotoSize(dto: PhotoSizeDto): PhotoSizeEntity.Size {
        return PhotoSizeEntity.Size()
            .setH(dto.height)
            .setW(dto.width)
            .setUrl(dto.url)
    }

    private fun mapPhotoSizes(dtos: List<PhotoSizeDto>?): PhotoSizeEntity {
        val sizes = PhotoSizeEntity()
        if (dtos != null) {
            for (dto in dtos) {
                when (dto.type) {
                    PhotoSizeDto.Type.S -> sizes.s = mapPhotoSize(dto)
                    PhotoSizeDto.Type.M -> sizes.m = mapPhotoSize(dto)
                    PhotoSizeDto.Type.X -> sizes.x = mapPhotoSize(dto)
                    PhotoSizeDto.Type.Y -> sizes.y = mapPhotoSize(dto)
                    PhotoSizeDto.Type.Z -> sizes.z = mapPhotoSize(dto)
                    PhotoSizeDto.Type.W -> sizes.w = mapPhotoSize(dto)
                    PhotoSizeDto.Type.O -> sizes.o = mapPhotoSize(dto)
                    PhotoSizeDto.Type.P -> sizes.p = mapPhotoSize(dto)
                    PhotoSizeDto.Type.Q -> sizes.q = mapPhotoSize(dto)
                    PhotoSizeDto.Type.R -> sizes.r = mapPhotoSize(dto)
                }
            }
        }
        return sizes
    }

    private class CEntity(
        val id: Int,
        val ownerId: Int,
        val type: Int,
        val accessKey: String?,
        val entity: Entity
    )
}