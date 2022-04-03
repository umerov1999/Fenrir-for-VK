package dev.ragnarok.fenrir.domain.mappers

import dev.ragnarok.fenrir.api.model.AttachmentsTokenCreator
import dev.ragnarok.fenrir.api.model.IAttachmentToken
import dev.ragnarok.fenrir.db.model.entity.*
import dev.ragnarok.fenrir.util.Utils.safeCountOf

object Entity2Dto {
    fun createTokens(dbos: Collection<Entity>?): List<IAttachmentToken> {
        val tokens: MutableList<IAttachmentToken> = ArrayList(safeCountOf(dbos))
        if (dbos != null) {
            for (entity in dbos) {
                tokens.add(createToken(entity))
            }
        }
        return tokens
    }


    fun createToken(entity: Entity?): IAttachmentToken {
        if (entity is DocumentEntity) {
            return AttachmentsTokenCreator.ofDocument(
                entity.id,
                entity.ownerId,
                entity.accessKey
            )
        }
        if (entity is AudioEntity) {
            return AttachmentsTokenCreator.ofAudio(entity.id, entity.ownerId, entity.accessKey)
        }
        if (entity is LinkEntity) {
            return AttachmentsTokenCreator.ofLink(entity.url)
        }
        if (entity is ArticleEntity) {
            return AttachmentsTokenCreator.ofArticle(entity.id, entity.ownerId, entity.accessKey)
        }
        if (entity is StoryEntity) {
            return AttachmentsTokenCreator.ofStory(entity.id, entity.ownerId, entity.accessKey)
        }
        if (entity is PhotoAlbumEntity) {
            return AttachmentsTokenCreator.ofPhotoAlbum(entity.id, entity.ownerId)
        }
        if (entity is GraffitiEntity) {
            return AttachmentsTokenCreator.ofGraffity(
                entity.id,
                entity.owner_id,
                entity.access_key
            )
        }
        if (entity is CallEntity) {
            return AttachmentsTokenCreator.ofCall(
                entity.initiator_id,
                entity.receiver_id,
                entity.state,
                entity.time
            )
        }
        if (entity is AudioArtistEntity) {
            return AttachmentsTokenCreator.ofArtist(entity.id)
        }
        if (entity is WallReplyEntity) {
            return AttachmentsTokenCreator.ofWallReply(entity.id, entity.ownerId)
        }
        if (entity is NotSupportedEntity) {
            return AttachmentsTokenCreator.ofError(entity.type, entity.body)
        }
        if (entity is EventEntity) {
            return AttachmentsTokenCreator.ofEvent(entity.id, entity.button_text)
        }
        if (entity is MarketEntity) {
            return AttachmentsTokenCreator.ofMarket(entity.id, entity.owner_id, entity.access_key)
        }
        if (entity is MarketAlbumEntity) {
            return AttachmentsTokenCreator.ofMarketAlbum(
                entity.id,
                entity.owner_id,
                entity.access_key
            )
        }
        if (entity is AudioPlaylistEntity) {
            return if (entity.original_access_key.isNullOrEmpty() || entity.original_id == 0 || entity.original_owner_id == 0) {
                AttachmentsTokenCreator.ofAudioPlaylist(
                    entity.id,
                    entity.ownerId,
                    entity.access_key
                )
            } else {
                AttachmentsTokenCreator.ofAudioPlaylist(
                    entity.original_id,
                    entity.original_owner_id,
                    entity.original_access_key
                )
            }
        }
        if (entity is PhotoEntity) {
            return AttachmentsTokenCreator.ofPhoto(entity.id, entity.ownerId, entity.accessKey)
        }
        if (entity is PollEntity) {
            return AttachmentsTokenCreator.ofPoll(entity.id, entity.ownerId)
        }
        if (entity is PostEntity) {
            return AttachmentsTokenCreator.ofPost(entity.id, entity.ownerId)
        }
        if (entity is VideoEntity) {
            return AttachmentsTokenCreator.ofVideo(entity.id, entity.ownerId, entity.accessKey)
        }
        throw UnsupportedOperationException("Token for class " + entity?.javaClass + " not supported")
    }
}