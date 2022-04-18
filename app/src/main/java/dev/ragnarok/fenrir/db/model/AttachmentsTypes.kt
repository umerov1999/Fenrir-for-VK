package dev.ragnarok.fenrir.db.model

import dev.ragnarok.fenrir.db.model.entity.*

object AttachmentsTypes {
    const val PHOTO = 1
    const val VIDEO = 2
    const val AUDIO = 4
    const val DOC = 8
    const val POST = 16
    const val ARTICLE = 32
    const val LINK = 64
    const val STORY = 128
    const val CALL = 256
    const val POLL = 512
    const val PAGE = 1024
    const val AUDIO_PLAYLIST = 2048
    const val STICKER = 4096
    const val TOPIC = 8192
    const val AUDIO_MESSAGE = 16384
    const val GIFT = 32768
    const val GRAFFITY = 65536
    const val ALBUM = 131072
    const val NOT_SUPPORTED = 262144
    const val WALL_REPLY = 524288
    const val EVENT = 1048576
    const val MARKET = 2097152
    const val MARKET_ALBUM = 4194304
    const val ARTIST = 8388608
    fun typeForInstance(entity: Entity): Int {
        when (entity) {
            is PhotoEntity -> {
                return PHOTO
            }
            is VideoEntity -> {
                return VIDEO
            }
            is PostEntity -> {
                return POST
            }
            is DocumentEntity -> {
                return DOC
            }
            is PollEntity -> {
                return POLL
            }
            is AudioEntity -> {
                return AUDIO
            }
            is LinkEntity -> {
                return LINK
            }
            is StickerEntity -> {
                return STICKER
            }
            is PageEntity -> {
                return PAGE
            }
            is TopicEntity -> {
                return TOPIC
            }
            is AudioMessageEntity -> {
                return AUDIO_MESSAGE
            }
            is GiftItemEntity -> {
                return GIFT
            }
            is ArticleEntity -> {
                return ARTICLE
            }
            is StoryEntity -> {
                return STORY
            }
            is CallEntity -> {
                return CALL
            }
            is AudioArtistEntity -> {
                return ARTIST
            }
            is AudioPlaylistEntity -> {
                return AUDIO_PLAYLIST
            }
            is GraffitiEntity -> {
                return GRAFFITY
            }
            is PhotoAlbumEntity -> {
                return ALBUM
            }
            is NotSupportedEntity -> {
                return NOT_SUPPORTED
            }
            is WallReplyEntity -> {
                return WALL_REPLY
            }
            is EventEntity -> {
                return EVENT
            }
            is MarketEntity -> {
                return MARKET
            }
            is MarketAlbumEntity -> {
                return MARKET_ALBUM
            }
            else -> throw UnsupportedOperationException("Unsupported type: " + entity.javaClass)
        }
    }

    fun classForType(type: Int): Class<out Entity> {
        return when (type) {
            PHOTO -> PhotoEntity::class.java
            VIDEO -> VideoEntity::class.java
            POST -> PostEntity::class.java
            DOC -> DocumentEntity::class.java
            POLL -> PollEntity::class.java
            AUDIO -> AudioEntity::class.java
            LINK -> LinkEntity::class.java
            STICKER -> StickerEntity::class.java
            PAGE -> PageEntity::class.java
            TOPIC -> TopicEntity::class.java
            AUDIO_MESSAGE -> AudioMessageEntity::class.java
            GIFT -> GiftItemEntity::class.java
            ARTICLE -> ArticleEntity::class.java
            STORY -> StoryEntity::class.java
            CALL -> CallEntity::class.java
            ARTIST -> AudioArtistEntity::class.java
            AUDIO_PLAYLIST -> AudioPlaylistEntity::class.java
            GRAFFITY -> GraffitiEntity::class.java
            ALBUM -> PhotoAlbumEntity::class.java
            NOT_SUPPORTED -> NotSupportedEntity::class.java
            WALL_REPLY -> WallReplyEntity::class.java
            EVENT -> EventEntity::class.java
            MARKET -> MarketEntity::class.java
            MARKET_ALBUM -> MarketAlbumEntity::class.java
            else -> throw UnsupportedOperationException("Unsupported type: $type")
        }
    }
}