package dev.ragnarok.fenrir.link

import dev.ragnarok.fenrir.link.types.*
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.empty
import dev.ragnarok.fenrir.util.Optional.Companion.wrap
import java.net.URLDecoder
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

object VkLinkParser {
    private val PATTERN_PHOTOS = Pattern.compile("vk\\.com/photos(-?\\d*)") //+
    private val PATTERN_PROFILE_ID = Pattern.compile("(m\\.)?vk\\.com/id(\\d+)$") //+
    private val PATTERN_DOMAIN = Pattern.compile("vk\\.com/([\\w.]+)")
    private val PATTERN_WALL_POST =
        Pattern.compile("vk.com/(?:[\\w.\\d]+\\?(?:[\\w=&]+)?w=)?wall(-?\\d*)_(\\d*)")
    private val PATTERN_WALL_POST_NOTIFICATION =
        Pattern.compile("vk\\.com/feed\\?\\S*scroll_to=wall(-?\\d*)_(\\d*)")
    private val PATTERN_AWAY = Pattern.compile("vk\\.com/away(\\.php)?\\?(.*)")
    private val PATTERN_DIALOG = Pattern.compile("vk\\.com/im\\?sel=(c?)(-?\\d+)")
    private val PATTERN_ALBUMS = Pattern.compile("vk\\.com/albums(-?\\d+)")
    private val PATTERN_AUDIOS = Pattern.compile("vk\\.com/audios(-?\\d+)")
    private val PATTERN_ARTIST = Pattern.compile("vk\\.com/artist/([^&]*)")
    private val PATTERN_ALBUM = Pattern.compile("vk\\.com/album(-?\\d*)_(-?\\d*)")
    private val PATTERN_WALL = Pattern.compile("vk\\.com/wall(-?\\d*)")
    private val PATTERN_POLL = Pattern.compile("vk\\.com/poll(-?\\d*)_(\\d*)") //+
    private val PATTERN_PHOTO =
        Pattern.compile("vk\\.com/(\\w)*(-)?(\\d)*(\\?z=)?photo(-?\\d*)_(\\d*)") //+
    private val PATTERN_VIDEO = Pattern.compile("vk\\.com/video(-?\\d*)_(\\d*)") //+
    private val PATTERN_PLAYLIST =
        Pattern.compile("vk\\.com/music/album/(-?\\d*)_(\\d*)_([^&]*)") //+
    private val PATTERN_PLAYLIST_ALT =
        Pattern.compile("vk\\.com/.+(?:act=|z=)audio_playlist(-?\\d*)_(\\d*)(?:&access_hash=(\\w+))?")
    private val PATTERN_DOC = Pattern.compile("vk\\.com/doc(-?\\d*)_(\\d*)") //+
    private val PATTERN_TOPIC = Pattern.compile("vk\\.com/topic-(\\d*)_(\\d*)") //+
    private val PATTERN_FAVE = Pattern.compile("vk\\.com/fave")
    private val PATTERN_GROUP_ID = Pattern.compile("vk\\.com/(club|event|public)(\\d+)$") //+
    private val PATTERN_FAVE_WITH_SECTION = Pattern.compile("vk\\.com/fave\\?section=([\\w.]+)")
    private val PATTERN_ACCESS_KEY = Pattern.compile("access_key=(\\w+)")
    private val PATTERN_VIDEO_ALBUM =
        Pattern.compile("vk\\.com/videos(-?\\d*)[?]section=album_(\\d*)")

    //vk.com/wall-2345345_7834545?reply=15345346
    private val PATTERN_WALL_POST_COMMENT =
        Pattern.compile("vk\\.com/wall(-?\\d*)_(\\d*)\\?reply=(\\d*)")
    private val PATTERN_WALL_POST_COMMENT_THREAD =
        Pattern.compile("vk\\.com/wall(-?\\d*)_(\\d*)\\?reply=(\\d*)&thread=(\\d*)")
    private val PATTERN_BOARD = Pattern.compile("vk\\.com/board(\\d+)")
    private val PATTERN_FEED_SEARCH = Pattern.compile("vk\\.com/feed\\?q=([^&]*)&section=search")
    private val PATTERN_FENRIR_TRACK = Pattern.compile("vk\\.com/audio/(-?\\d*)_(\\d*)") //+
    private val PATTERN_FENRIR_SERVER_TRACK_HASH = Pattern.compile("hash=([^&]*)")
    private val PARSERS: MutableList<IParser> = LinkedList()

    @JvmStatic
    fun parse(string: String): AbsLink? {
        if (!string.contains("vk.com")) {
            return null
        }
        var vkLink = parseWallCommentThreadLink(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseWallCommentLink(string)
        if (vkLink != null) {
            return vkLink
        }
        if (string.contains("vk.com/images")) {
            return null
        }
        if (string.contains("vk.com/search")) {
            return null
        }
        if (string.contains("vk.com/feed") && !string.contains("?z=photo") && !string.contains("w=wall") && !string.contains(
                "?w=page"
            ) && !string.contains("?q=") && !string.contains("scroll_to=wall")
        ) {
            return null
        }
        if (string.contains("vk.com/friends")) {
            return null
        }
        if (Pattern.matches(".*vk.com/app\\d.*", string)) {
            return null
        }
        if (string.endsWith("vk.com/support")) {
            return null
        }
        if (string.endsWith("vk.com/restore")) {
            return null
        }
        if (string.contains("vk.com/restore?")) {
            return null
        }
        if (string.endsWith("vk.com/page")) {
            return null
        }
        if (string.contains("vk.com/login")) {
            return null
        }
        if (string.contains("vk.com/bugs")) {
            return null
        }
        if (Pattern.matches(".*vk.com/note\\d.*", string)) {
            return null
        }
        if (string.contains("vk.com/dev/")) {
            return null
        }
        if (string.contains("vk.com/wall") && string.contains("?reply=")) {
            return null
        }
        if (string.endsWith("vk.com/mail")) {
            return DialogsLink()
        }
        if (string.endsWith("vk.com/im")) {
            return DialogsLink()
        }
        for (parser in PARSERS) {
            try {
                val link = parser.parse(string).get()
                if (link != null) {
                    return link
                }
            } catch (ignored: Exception) {
            }
        }
        vkLink = parseBoard(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parsePage(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parsePhoto(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseAlbum(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseProfileById(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseGroupById(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseTopic(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseWallPost(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseWallPostNotif(string)
        if (vkLink != null) {
            return vkLink
        }

        /*    vkLink = VkLinkParser.parseAway(string);
        if (vkLink != null) {
            return vkLink;
        }
    */if (string.contains("/im?sel")) {
            vkLink = parseDialog(string)
            if (vkLink != null) {
                return vkLink
            }
        }
        vkLink = parseAlbums(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseAway(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseWall(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parsePoll(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseVideoAlbum(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseVideo(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseAudioPlaylistAlt(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parsePlaylist(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseAudios(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseArtists(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseAudioTrack(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseDoc(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parsePhotos(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseFave(string)
        if (vkLink != null) {
            return vkLink
        }
        vkLink = parseDomain(string)
        return vkLink
    }

    private fun parseBoard(string: String): AbsLink? {
        val matcher = PATTERN_BOARD.matcher(string)
        try {
            if (matcher.find()) {
                val groupId = matcher.group(1)
                if (groupId != null) {
                    return BoardLink(groupId.toInt())
                }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parseAlbum(string: String): AbsLink? {
        val matcher = PATTERN_ALBUM.matcher(string)
        if (!matcher.find()) {
            return null
        }
        val ownerId = matcher.group(1)
        var albumId = matcher.group(2)
        if (albumId == "0") {
            albumId = (-6).toString()
        }
        if (albumId == "00") {
            albumId = (-7).toString()
        }
        if (albumId == "000") {
            albumId = (-15).toString()
        }
        if (ownerId != null) {
            if (albumId != null) {
                return PhotoAlbumLink(ownerId.toInt(), albumId.toInt())
            }
        }
        return null
    }

    private fun parseAlbums(string: String): AbsLink? {
        val matcher = PATTERN_ALBUMS.matcher(string)
        if (!matcher.find()) {
            return null
        }
        val ownerId = matcher.group(1)
        if (ownerId != null) {
            return PhotoAlbumsLink(ownerId.toInt())
        }
        return null
    }

    private fun parseAway(string: String): AbsLink? {
        return if (!PATTERN_AWAY.matcher(string).find()) {
            null
        } else AwayLink(string)
    }

    private fun parseDialog(string: String): AbsLink? {
        val matcher = PATTERN_DIALOG.matcher(string)
        try {
            if (matcher.find()) {
                val chat = matcher.group(1)
                val id = matcher.group(2)?.toInt()
                val isChat = chat.nonNullNoEmpty()
                return (if (isChat) id?.let { Peer.fromChatId(it) } else id?.let {
                    Peer.fromOwnerId(
                        it
                    )
                })?.let {
                    DialogLink(
                        it
                    )
                }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parseDomain(string: String): AbsLink? {
        val matcher = PATTERN_DOMAIN.matcher(string)
        return if (!matcher.find()) {
            null
        } else matcher.group(1)?.let { DomainLink(string, it) }
    }

    private fun parseGroupById(string: String): AbsLink? {
        val matcher = PATTERN_GROUP_ID.matcher(string)
        try {
            if (matcher.find()) {
                matcher.group(2)?.let {
                    return OwnerLink(-abs(it.toInt()))
                }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parsePage(string: String): AbsLink? {
        return if (string.contains("vk.com/pages")
            || string.contains("vk.com/page")
            || string.contains("vk.com") && string.contains("w=page")
        ) {
            PageLink(string.replace("m.vk.com/", "vk.com/"))
        } else null
    }

    private fun parseAccessKey(string: String): String? {
        val matcher = PATTERN_ACCESS_KEY.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parsePhoto(string: String): AbsLink? {
        val matcher = PATTERN_PHOTO.matcher(string)
        try {
            if (matcher.find()) {
                val ownerId = matcher.group(5)?.toInt()
                val photoId = matcher.group(6)?.toInt()
                return photoId?.let {
                    if (ownerId != null) {
                        PhotoLink(it, ownerId, parseAccessKey(string))
                    }
                    null
                }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parsePhotos(string: String): AbsLink? {
        val matcher = PATTERN_PHOTOS.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)?.let { PhotoAlbumsLink(it.toInt()) }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parseProfileById(string: String): AbsLink? {
        val matcher = PATTERN_PROFILE_ID.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(2)?.let { OwnerLink(it.toInt()) }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parseTopic(string: String): AbsLink? {
        val matcher = PATTERN_TOPIC.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(2)?.let {
                    matcher.group(1)
                        ?.let { it1 -> TopicLink(it.toInt(), it1.toInt()) }
                }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parseVideoAlbum(string: String): AbsLink? {
        val matcher = PATTERN_VIDEO_ALBUM.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)
                    ?.let {
                        matcher.group(2)?.let { it1 -> VideoAlbumLink(it.toInt(), it1.toInt()) }
                    }
            }
        } catch (ignored: NumberFormatException) {
        }
        return null
    }

    private fun parseVideo(string: String): AbsLink? {
        val matcher = PATTERN_VIDEO.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)?.let {
                    matcher.group(2)?.let { it1 ->
                        VideoLink(
                            it.toInt(),
                            it1.toInt(),
                            parseAccessKey(string)
                        )
                    }
                }
            }
        } catch (ignored: NumberFormatException) {
        }
        return null
    }

    fun parseLocalServerURL(string: String?): String? {
        val matcher = string?.let { PATTERN_FENRIR_SERVER_TRACK_HASH.matcher(it) }
        try {
            if (matcher?.find() == true) {
                return matcher.group(1)
            }
        } catch (ignored: NumberFormatException) {
        }
        return null
    }

    private fun parseAudioTrack(string: String): AbsLink? {
        val matcher = PATTERN_FENRIR_TRACK.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)
                    ?.let {
                        matcher.group(2)?.let { it1 -> AudioTrackLink(it.toInt(), it1.toInt()) }
                    }
            }
        } catch (ignored: NumberFormatException) {
        }
        return null
    }

    private fun parseAudioPlaylistAlt(string: String): AbsLink? {
        val matcher = PATTERN_PLAYLIST_ALT.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)?.let {
                    matcher.group(2)?.let { it1 ->
                        AudioPlaylistLink(
                            it.toInt(),
                            it1.toInt(),
                            matcher.group(3)
                        )
                    }
                }
            }
        } catch (ignored: NumberFormatException) {
        }
        return null
    }

    private fun parsePlaylist(string: String): AbsLink? {
        val matcher = PATTERN_PLAYLIST.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)?.let {
                    matcher.group(2)?.let { it1 ->
                        AudioPlaylistLink(
                            it.toInt(),
                            it1.toInt(),
                            matcher.group(3)
                        )
                    }
                }
            }
        } catch (ignored: NumberFormatException) {
        }
        return null
    }

    private fun parseFave(string: String): AbsLink? {
        val matcherWithSection = PATTERN_FAVE_WITH_SECTION.matcher(string)
        val matcher = PATTERN_FAVE.matcher(string)
        if (matcherWithSection.find()) {
            return FaveLink(matcherWithSection.group(1))
        }
        return if (matcher.find()) {
            FaveLink()
        } else null
    }

    private fun parseAudios(string: String): AbsLink? {
        val matcher = PATTERN_AUDIOS.matcher(string)
        return if (!matcher.find()) {
            null
        } else matcher.group(1)?.let { AudiosLink(it.toInt()) }
    }

    private fun parseArtists(string: String): AbsLink? {
        val matcher = PATTERN_ARTIST.matcher(string)
        return if (!matcher.find()) {
            null
        } else matcher.group(1)?.let { ArtistsLink(it) }
    }

    private fun parseDoc(string: String): AbsLink? {
        val matcher = PATTERN_DOC.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)?.let {
                    matcher.group(2)?.let { it1 ->
                        DocLink(
                            it.toInt(),
                            it1.toInt(),
                            parseAccessKey(string)
                        )
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parseWall(string: String): AbsLink? {
        val matcher = PATTERN_WALL.matcher(string)
        return if (!matcher.find()) {
            null
        } else matcher.group(1)?.let { WallLink(it.toInt()) }
    }

    private fun parsePoll(string: String): AbsLink? {
        val matcher = PATTERN_POLL.matcher(string)
        try {
            if (matcher.find()) {
                return matcher.group(1)?.let {
                    matcher.group(2)
                        ?.let { it1 -> PollLink(it.toInt(), it1.toInt()) }
                }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    private fun parseWallCommentThreadLink(string: String): AbsLink? {
        val matcher = PATTERN_WALL_POST_COMMENT_THREAD.matcher(string)
        if (!matcher.find()) {
            return null
        }
        val link = matcher.group(1)?.let {
            matcher.group(2)?.let { it1 ->
                matcher.group(3)?.let { it2 ->
                    matcher.group(4)?.let { it3 ->
                        WallCommentThreadLink(
                            it.toInt(),
                            it1.toInt(),
                            it2.toInt(),
                            it3.toInt()
                        )
                    }
                }
            }
        }
        return if (link?.isValid == true) link else null
    }

    private fun parseWallCommentLink(string: String): AbsLink? {
        val matcher = PATTERN_WALL_POST_COMMENT.matcher(string)
        if (!matcher.find()) {
            return null
        }
        val link = matcher.group(1)?.let {
            matcher.group(2)?.let { it1 ->
                matcher.group(3)?.let { it2 ->
                    WallCommentLink(
                        it.toInt(),
                        it1.toInt(),
                        it2.toInt()
                    )
                }
            }
        }
        return if (link?.isValid == true) link else null
    }

    private fun parseWallPost(string: String): AbsLink? {
        val matcher = PATTERN_WALL_POST.matcher(string)
        return if (!matcher.find()) {
            null
        } else matcher.group(1)?.let {
            matcher.group(2)
                ?.let { it1 -> WallPostLink(it.toInt(), it1.toInt()) }
        }
    }

    private fun parseWallPostNotif(string: String): AbsLink? {
        val matcher = PATTERN_WALL_POST_NOTIFICATION.matcher(string)
        return if (!matcher.find()) {
            null
        } else matcher.group(1)?.let {
            matcher.group(2)
                ?.let { it1 -> WallPostLink(it.toInt(), it1.toInt()) }
        }
    }

    private interface IParser {
        @Throws(Exception::class)
        fun parse(string: String?): Optional<AbsLink>
    }

    init {
        PARSERS.add(object : IParser {
            override fun parse(string: String?): Optional<AbsLink> {
                val matcher = string?.let { PATTERN_FEED_SEARCH.matcher(it) }
                if (matcher?.find() == true) {
                    val q = URLDecoder.decode(matcher.group(1), "UTF-8")
                    return wrap(FeedSearchLink(q))
                }
                return empty()
            }
        })
    }
}