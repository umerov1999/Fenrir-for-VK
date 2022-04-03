package dev.ragnarok.fenrir.adapter

import android.content.Context
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppTextUtils
import dev.ragnarok.fenrir.util.Utils
import java.util.*

class DocLink(val attachment: AbsModel) {
    val type: Int = typeOf(attachment)
    val imageUrl: String?
        get() {
            when (type) {
                Types.DOC -> {
                    val doc = attachment as Document
                    return doc.getPreviewWithSize(Settings.get().main().prefPreviewImageSize, true)
                }
                Types.POST -> return (attachment as Post).authorPhoto
                Types.EVENT -> return (attachment as Event).subjectPhoto
                Types.WALL_REPLY -> return (attachment as WallReply).authorPhoto
                Types.GRAFFITY -> return (attachment as Graffiti).url
                Types.STORY -> return (attachment as Story).owner.maxSquareAvatar
                Types.ALBUM -> {
                    val album = attachment as PhotoAlbum
                    if (album.sizes != null) {
                        val sizes = album.sizes
                        return sizes.getUrlForSize(Settings.get().main().prefPreviewImageSize, true)
                    }
                    return null
                }
                Types.MARKET_ALBUM -> {
                    val market_album = attachment as MarketAlbum
                    if (market_album.photo != null && market_album.photo.sizes != null) {
                        val sizes = market_album.photo.sizes
                        return sizes.getUrlForSize(Settings.get().main().prefPreviewImageSize, true)
                    }
                    return null
                }
                Types.ARTIST -> return (attachment as AudioArtist).maxPhoto
                Types.MARKET -> return (attachment as Market).thumb_photo
                Types.AUDIO_PLAYLIST -> return (attachment as AudioPlaylist).thumb_image
                Types.LINK -> {
                    val link = attachment as Link
                    if (link.photo == null && link.previewPhoto != null) return link.previewPhoto
                    if (link.photo != null && link.photo.sizes != null) {
                        val sizes = link.photo.sizes
                        return sizes.getUrlForSize(Settings.get().main().prefPreviewImageSize, true)
                    }
                    return null
                }
            }
            return null
        }

    fun getTitle(context: Context): String? {
        var title: String?
        when (type) {
            Types.DOC -> return (attachment as Document).title
            Types.POST -> return (attachment as Post).authorName
            Types.EVENT -> return (attachment as Event).subjectName
            Types.WALL_REPLY -> return (attachment as WallReply).authorName
            Types.AUDIO_PLAYLIST -> return (attachment as AudioPlaylist).title
            Types.ALBUM -> return (attachment as PhotoAlbum).title
            Types.MARKET -> return (attachment as Market).title
            Types.ARTIST -> return (attachment as AudioArtist).name
            Types.MARKET_ALBUM -> return (attachment as MarketAlbum).title
            Types.LINK -> {
                title = (attachment as Link).title
                if (title.isNullOrEmpty()) {
                    title = "[" + context.getString(R.string.attachment_link)
                        .lowercase(Locale.getDefault()) + "]"
                }
                return title
            }
            Types.NOT_SUPPORTED -> return context.getString(R.string.not_yet_implemented_message)
            Types.POLL -> {
                val poll = attachment as Poll
                return context.getString(if (poll.isAnonymous) R.string.anonymous_poll else R.string.open_poll)
            }
            Types.STORY -> return (attachment as Story).owner.fullName
            Types.WIKI_PAGE -> return context.getString(R.string.wiki_page)
            Types.CALL -> {
                val initiator = (attachment as Call).initiator_id
                return if (initiator == Settings.get()
                        .accounts().current
                ) context.getString(R.string.input_call) else context.getString(R.string.output_call)
            }
        }
        return null
    }

    fun getExt(context: Context): String? {
        when (type) {
            Types.DOC -> return (attachment as Document).ext
            Types.POST, Types.WALL_REPLY -> return null
            Types.LINK -> return URL
            Types.WIKI_PAGE -> return W
            Types.STORY -> return context.getString(R.string.story)
            Types.AUDIO_PLAYLIST -> return context.getString(R.string.playlist)
        }
        return null
    }

    fun getSecondaryText(context: Context): String? {
        when (type) {
            Types.DOC -> return AppTextUtils.getSizeString(
                (attachment as Document).size
            )
            Types.NOT_SUPPORTED -> {
                val ns = attachment as NotSupported
                return ns.type + ": " + ns.body
            }
            Types.POST -> {
                val post = attachment as Post
                return when {
                    post.hasText() -> post.text
                    post.hasAttachments() -> ""
                    else -> context.getString(
                        R.string.wall_post_view
                    )
                }
            }
            Types.EVENT -> {
                val event = attachment as Event
                return Utils.firstNonEmptyString(
                    event.button_text,
                    " "
                ) + ", " + Utils.firstNonEmptyString(event.text)
            }
            Types.WALL_REPLY -> {
                val comment = attachment as WallReply
                return comment.text
            }
            Types.LINK -> return (attachment as Link).url
            Types.ALBUM -> return Utils.firstNonEmptyString(
                (attachment as PhotoAlbum).description,
                " "
            ) +
                    " " + context.getString(R.string.photos_count, attachment.size)
            Types.POLL -> return (attachment as Poll).question
            Types.WIKI_PAGE -> return (attachment as WikiPage).title
            Types.CALL -> return (attachment as Call).getLocalizedState(context)
            Types.MARKET -> return (attachment as Market).price + ", " + AppTextUtils.reduceStringForPost(
                Utils.firstNonEmptyString(
                    attachment.description, " "
                )
            )
            Types.MARKET_ALBUM -> return context.getString(
                R.string.markets_count,
                (attachment as MarketAlbum).count
            )
            Types.AUDIO_PLAYLIST -> return Utils.firstNonEmptyString(
                (attachment as AudioPlaylist).artist_name,
                " "
            ) + " " +
                    attachment.count + " " + context.getString(R.string.audios_pattern_count)
            Types.STORY -> {
                val item = attachment as Story
                return if (item.expires <= 0) null else {
                    if (item.isIs_expired) {
                        context.getString(R.string.is_expired)
                    } else {
                        val exp = (item.expires - Calendar.getInstance().time.time / 1000) / 3600
                        context.getString(
                            R.string.expires,
                            exp.toString(),
                            context.getString(
                                Utils.declOfNum(
                                    exp,
                                    intArrayOf(R.string.hour, R.string.hour_sec, R.string.hours)
                                )
                            )
                        )
                    }
                }
            }
        }
        return null
    }

    companion object {
        private const val URL = "URL"
        private const val W = "WIKI"
        private fun typeOf(model: AbsModel): Int {
            if (model is Document) {
                return Types.DOC
            }
            if (model is Post) {
                return Types.POST
            }
            if (model is Link) {
                return Types.LINK
            }
            if (model is Poll) {
                return Types.POLL
            }
            if (model is WikiPage) {
                return Types.WIKI_PAGE
            }
            if (model is Story) {
                return Types.STORY
            }
            if (model is Call) {
                return Types.CALL
            }
            if (model is AudioArtist) {
                return Types.ARTIST
            }
            if (model is WallReply) {
                return Types.WALL_REPLY
            }
            if (model is AudioPlaylist) {
                return Types.AUDIO_PLAYLIST
            }
            if (model is Graffiti) {
                return Types.GRAFFITY
            }
            if (model is PhotoAlbum) {
                return Types.ALBUM
            }
            if (model is NotSupported) {
                return Types.NOT_SUPPORTED
            }
            if (model is Event) {
                return Types.EVENT
            }
            if (model is Market) {
                return Types.MARKET
            }
            if (model is MarketAlbum) {
                return Types.MARKET_ALBUM
            }
            throw IllegalArgumentException()
        }
    }

}