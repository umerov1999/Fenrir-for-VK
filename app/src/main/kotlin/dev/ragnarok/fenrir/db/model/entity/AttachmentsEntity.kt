package dev.ragnarok.fenrir.db.model.entity

import dev.ragnarok.fenrir.db.model.AttachmentsTypes

object AttachmentsEntity {
    @AttachmentsTypes
    fun typeForInstance(dboEntity: DboEntity): Int {
        when (dboEntity) {
            is PhotoDboEntity -> {
                return AttachmentsTypes.PHOTO
            }
            is VideoDboEntity -> {
                return AttachmentsTypes.VIDEO
            }
            is PostDboEntity -> {
                return AttachmentsTypes.POST
            }
            is DocumentDboEntity -> {
                return AttachmentsTypes.DOC
            }
            is PollDboEntity -> {
                return AttachmentsTypes.POLL
            }
            is AudioDboEntity -> {
                return AttachmentsTypes.AUDIO
            }
            is LinkDboEntity -> {
                return AttachmentsTypes.LINK
            }
            is StickerDboEntity -> {
                return AttachmentsTypes.STICKER
            }
            is PageDboEntity -> {
                return AttachmentsTypes.PAGE
            }
            is TopicDboEntity -> {
                return AttachmentsTypes.TOPIC
            }
            is AudioMessageDboEntity -> {
                return AttachmentsTypes.AUDIO_MESSAGE
            }
            is GiftItemDboEntity -> {
                return AttachmentsTypes.GIFT
            }
            is ArticleDboEntity -> {
                return AttachmentsTypes.ARTICLE
            }
            is StoryDboEntity -> {
                return AttachmentsTypes.STORY
            }
            is CallDboEntity -> {
                return AttachmentsTypes.CALL
            }
            is AudioArtistDboEntity -> {
                return AttachmentsTypes.ARTIST
            }
            is AudioPlaylistDboEntity -> {
                return AttachmentsTypes.AUDIO_PLAYLIST
            }
            is GraffitiDboEntity -> {
                return AttachmentsTypes.GRAFFITY
            }
            is PhotoAlbumDboEntity -> {
                return AttachmentsTypes.ALBUM
            }
            is NotSupportedDboEntity -> {
                return AttachmentsTypes.NOT_SUPPORTED
            }
            is WallReplyDboEntity -> {
                return AttachmentsTypes.WALL_REPLY
            }
            is EventDboEntity -> {
                return AttachmentsTypes.EVENT
            }
            is MarketDboEntity -> {
                return AttachmentsTypes.MARKET
            }
            is MarketAlbumDboEntity -> {
                return AttachmentsTypes.MARKET_ALBUM
            }
            else -> throw UnsupportedOperationException("Unsupported type: " + dboEntity.javaClass)
        }
    }

    fun classForType(@AttachmentsTypes type: Int): Class<out DboEntity> {
        return when (type) {
            AttachmentsTypes.PHOTO -> PhotoDboEntity::class.java
            AttachmentsTypes.VIDEO -> VideoDboEntity::class.java
            AttachmentsTypes.POST -> PostDboEntity::class.java
            AttachmentsTypes.DOC -> DocumentDboEntity::class.java
            AttachmentsTypes.POLL -> PollDboEntity::class.java
            AttachmentsTypes.AUDIO -> AudioDboEntity::class.java
            AttachmentsTypes.LINK -> LinkDboEntity::class.java
            AttachmentsTypes.STICKER -> StickerDboEntity::class.java
            AttachmentsTypes.PAGE -> PageDboEntity::class.java
            AttachmentsTypes.TOPIC -> TopicDboEntity::class.java
            AttachmentsTypes.AUDIO_MESSAGE -> AudioMessageDboEntity::class.java
            AttachmentsTypes.GIFT -> GiftItemDboEntity::class.java
            AttachmentsTypes.ARTICLE -> ArticleDboEntity::class.java
            AttachmentsTypes.STORY -> StoryDboEntity::class.java
            AttachmentsTypes.CALL -> CallDboEntity::class.java
            AttachmentsTypes.ARTIST -> AudioArtistDboEntity::class.java
            AttachmentsTypes.AUDIO_PLAYLIST -> AudioPlaylistDboEntity::class.java
            AttachmentsTypes.GRAFFITY -> GraffitiDboEntity::class.java
            AttachmentsTypes.ALBUM -> PhotoAlbumDboEntity::class.java
            AttachmentsTypes.NOT_SUPPORTED -> NotSupportedDboEntity::class.java
            AttachmentsTypes.WALL_REPLY -> WallReplyDboEntity::class.java
            AttachmentsTypes.EVENT -> EventDboEntity::class.java
            AttachmentsTypes.MARKET -> MarketDboEntity::class.java
            AttachmentsTypes.MARKET_ALBUM -> MarketAlbumDboEntity::class.java
            else -> throw UnsupportedOperationException("Unsupported type: $type")
        }
    }
}