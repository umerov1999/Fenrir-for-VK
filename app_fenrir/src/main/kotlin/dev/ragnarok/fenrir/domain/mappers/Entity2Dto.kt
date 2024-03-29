package dev.ragnarok.fenrir.domain.mappers

import dev.ragnarok.fenrir.api.model.AttachmentsTokenCreator
import dev.ragnarok.fenrir.api.model.interfaces.IAttachmentToken
import dev.ragnarok.fenrir.db.model.entity.ArticleDboEntity
import dev.ragnarok.fenrir.db.model.entity.AudioArtistDboEntity
import dev.ragnarok.fenrir.db.model.entity.AudioDboEntity
import dev.ragnarok.fenrir.db.model.entity.AudioPlaylistDboEntity
import dev.ragnarok.fenrir.db.model.entity.CallDboEntity
import dev.ragnarok.fenrir.db.model.entity.DboEntity
import dev.ragnarok.fenrir.db.model.entity.DocumentDboEntity
import dev.ragnarok.fenrir.db.model.entity.EventDboEntity
import dev.ragnarok.fenrir.db.model.entity.GeoDboEntity
import dev.ragnarok.fenrir.db.model.entity.GraffitiDboEntity
import dev.ragnarok.fenrir.db.model.entity.LinkDboEntity
import dev.ragnarok.fenrir.db.model.entity.MarketAlbumDboEntity
import dev.ragnarok.fenrir.db.model.entity.MarketDboEntity
import dev.ragnarok.fenrir.db.model.entity.NarrativesDboEntity
import dev.ragnarok.fenrir.db.model.entity.NotSupportedDboEntity
import dev.ragnarok.fenrir.db.model.entity.PhotoAlbumDboEntity
import dev.ragnarok.fenrir.db.model.entity.PhotoDboEntity
import dev.ragnarok.fenrir.db.model.entity.PollDboEntity
import dev.ragnarok.fenrir.db.model.entity.PostDboEntity
import dev.ragnarok.fenrir.db.model.entity.StoryDboEntity
import dev.ragnarok.fenrir.db.model.entity.VideoDboEntity
import dev.ragnarok.fenrir.db.model.entity.WallReplyDboEntity

object Entity2Dto {
    fun createToken(dboEntity: DboEntity?): IAttachmentToken {
        when (dboEntity) {
            is DocumentDboEntity -> {
                return AttachmentsTokenCreator.ofDocument(
                    dboEntity.id,
                    dboEntity.ownerId,
                    dboEntity.accessKey
                )
            }

            is AudioDboEntity -> {
                return AttachmentsTokenCreator.ofAudio(
                    dboEntity.id,
                    dboEntity.ownerId,
                    dboEntity.accessKey
                )
            }

            is LinkDboEntity -> {
                return AttachmentsTokenCreator.ofLink(dboEntity.url)
            }

            is ArticleDboEntity -> {
                return AttachmentsTokenCreator.ofArticle(
                    dboEntity.id,
                    dboEntity.ownerId,
                    dboEntity.accessKey
                )
            }

            is StoryDboEntity -> {
                return AttachmentsTokenCreator.ofStory(
                    dboEntity.id,
                    dboEntity.ownerId,
                    dboEntity.accessKey
                )
            }

            is NarrativesDboEntity -> {
                return AttachmentsTokenCreator.ofNarrative(
                    dboEntity.id,
                    dboEntity.owner_id,
                    dboEntity.accessKey
                )
            }

            is PhotoAlbumDboEntity -> {
                return AttachmentsTokenCreator.ofPhotoAlbum(dboEntity.id, dboEntity.ownerId)
            }

            is GraffitiDboEntity -> {
                return AttachmentsTokenCreator.ofGraffiti(
                    dboEntity.id,
                    dboEntity.owner_id,
                    dboEntity.access_key
                )
            }

            is CallDboEntity -> {
                return AttachmentsTokenCreator.ofCall(
                    dboEntity.initiator_id,
                    dboEntity.receiver_id,
                    dboEntity.state,
                    dboEntity.time
                )
            }

            is GeoDboEntity -> {
                return AttachmentsTokenCreator.ofGeo(
                    dboEntity.latitude,
                    dboEntity.longitude
                )
            }

            is AudioArtistDboEntity -> {
                return AttachmentsTokenCreator.ofArtist(dboEntity.id)
            }

            is WallReplyDboEntity -> {
                return AttachmentsTokenCreator.ofWallReply(dboEntity.id, dboEntity.ownerId)
            }

            is NotSupportedDboEntity -> {
                return AttachmentsTokenCreator.ofError(dboEntity.type ?: "error", dboEntity.body)
            }

            is EventDboEntity -> {
                return AttachmentsTokenCreator.ofEvent(dboEntity.id, dboEntity.button_text)
            }

            is MarketDboEntity -> {
                return AttachmentsTokenCreator.ofMarket(
                    dboEntity.id,
                    dboEntity.owner_id,
                    dboEntity.access_key
                )
            }

            is MarketAlbumDboEntity -> {
                return AttachmentsTokenCreator.ofMarketAlbum(
                    dboEntity.id,
                    dboEntity.owner_id,
                    dboEntity.access_key
                )
            }

            is AudioPlaylistDboEntity -> {
                return if (dboEntity.original_access_key.isNullOrEmpty() || dboEntity.original_id == 0 || dboEntity.original_owner_id == 0L) {
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

            is PhotoDboEntity -> {
                return AttachmentsTokenCreator.ofPhoto(
                    dboEntity.id,
                    dboEntity.ownerId,
                    dboEntity.accessKey
                )
            }

            is PollDboEntity -> {
                return AttachmentsTokenCreator.ofPoll(dboEntity.id, dboEntity.ownerId)
            }

            is PostDboEntity -> {
                return AttachmentsTokenCreator.ofPost(dboEntity.id, dboEntity.ownerId)
            }

            is VideoDboEntity -> {
                return AttachmentsTokenCreator.ofVideo(
                    dboEntity.id,
                    dboEntity.ownerId,
                    dboEntity.accessKey
                )
            }

            else -> throw UnsupportedOperationException("Token for class " + dboEntity?.javaClass + " not supported")
        }
    }
}