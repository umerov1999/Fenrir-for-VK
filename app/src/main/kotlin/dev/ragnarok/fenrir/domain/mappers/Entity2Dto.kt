package dev.ragnarok.fenrir.domain.mappers

import dev.ragnarok.fenrir.api.model.AttachmentsTokenCreator
import dev.ragnarok.fenrir.api.model.IAttachmentToken
import dev.ragnarok.fenrir.db.model.entity.*
import dev.ragnarok.fenrir.util.Utils.safeCountOf

object Entity2Dto {
    fun createTokens(dbos: Collection<DboEntity>?): List<IAttachmentToken> {
        val tokens: MutableList<IAttachmentToken> = ArrayList(safeCountOf(dbos))
        if (dbos != null) {
            for (entity in dbos) {
                tokens.add(createToken(entity))
            }
        }
        return tokens
    }


    fun createToken(dboEntity: DboEntity?): IAttachmentToken {
        if (dboEntity is DocumentDboEntity) {
            return AttachmentsTokenCreator.ofDocument(
                dboEntity.id,
                dboEntity.ownerId,
                dboEntity.accessKey
            )
        }
        if (dboEntity is AudioDboEntity) {
            return AttachmentsTokenCreator.ofAudio(
                dboEntity.id,
                dboEntity.ownerId,
                dboEntity.accessKey
            )
        }
        if (dboEntity is LinkDboEntity) {
            return AttachmentsTokenCreator.ofLink(dboEntity.url)
        }
        if (dboEntity is ArticleDboEntity) {
            return AttachmentsTokenCreator.ofArticle(
                dboEntity.id,
                dboEntity.ownerId,
                dboEntity.accessKey
            )
        }
        if (dboEntity is StoryDboEntity) {
            return AttachmentsTokenCreator.ofStory(
                dboEntity.id,
                dboEntity.ownerId,
                dboEntity.accessKey
            )
        }
        if (dboEntity is PhotoAlbumDboEntity) {
            return AttachmentsTokenCreator.ofPhotoAlbum(dboEntity.id, dboEntity.ownerId)
        }
        if (dboEntity is GraffitiDboEntity) {
            return AttachmentsTokenCreator.ofGraffity(
                dboEntity.id,
                dboEntity.owner_id,
                dboEntity.access_key
            )
        }
        if (dboEntity is CallDboEntity) {
            return AttachmentsTokenCreator.ofCall(
                dboEntity.initiator_id,
                dboEntity.receiver_id,
                dboEntity.state,
                dboEntity.time
            )
        }
        if (dboEntity is AudioArtistDboEntity) {
            return AttachmentsTokenCreator.ofArtist(dboEntity.id)
        }
        if (dboEntity is WallReplyDboEntity) {
            return AttachmentsTokenCreator.ofWallReply(dboEntity.id, dboEntity.ownerId)
        }
        if (dboEntity is NotSupportedDboEntity) {
            return AttachmentsTokenCreator.ofError(dboEntity.type ?: "error", dboEntity.body)
        }
        if (dboEntity is EventDboEntity) {
            return AttachmentsTokenCreator.ofEvent(dboEntity.id, dboEntity.button_text)
        }
        if (dboEntity is MarketDboEntity) {
            return AttachmentsTokenCreator.ofMarket(
                dboEntity.id,
                dboEntity.owner_id,
                dboEntity.access_key
            )
        }
        if (dboEntity is MarketAlbumDboEntity) {
            return AttachmentsTokenCreator.ofMarketAlbum(
                dboEntity.id,
                dboEntity.owner_id,
                dboEntity.access_key
            )
        }
        if (dboEntity is AudioPlaylistDboEntity) {
            return if (dboEntity.original_access_key.isNullOrEmpty() || dboEntity.original_id == 0 || dboEntity.original_owner_id == 0) {
                AttachmentsTokenCreator.ofAudioPlaylist(
                    dboEntity.id,
                    dboEntity.ownerId,
                    dboEntity.access_key
                )
            } else {
                AttachmentsTokenCreator.ofAudioPlaylist(
                    dboEntity.original_id,
                    dboEntity.original_owner_id,
                    dboEntity.original_access_key
                )
            }
        }
        if (dboEntity is PhotoDboEntity) {
            return AttachmentsTokenCreator.ofPhoto(
                dboEntity.id,
                dboEntity.ownerId,
                dboEntity.accessKey
            )
        }
        if (dboEntity is PollDboEntity) {
            return AttachmentsTokenCreator.ofPoll(dboEntity.id, dboEntity.ownerId)
        }
        if (dboEntity is PostDboEntity) {
            return AttachmentsTokenCreator.ofPost(dboEntity.id, dboEntity.ownerId)
        }
        if (dboEntity is VideoDboEntity) {
            return AttachmentsTokenCreator.ofVideo(
                dboEntity.id,
                dboEntity.ownerId,
                dboEntity.accessKey
            )
        }
        throw UnsupportedOperationException("Token for class " + dboEntity?.javaClass + " not supported")
    }
}