package dev.ragnarok.fenrir.link;

import static java.lang.Integer.parseInt;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import androidx.annotation.Nullable;

import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ragnarok.fenrir.link.types.AbsLink;
import dev.ragnarok.fenrir.link.types.ArtistsLink;
import dev.ragnarok.fenrir.link.types.AudioPlaylistLink;
import dev.ragnarok.fenrir.link.types.AudioTrackLink;
import dev.ragnarok.fenrir.link.types.AudiosLink;
import dev.ragnarok.fenrir.link.types.AwayLink;
import dev.ragnarok.fenrir.link.types.BoardLink;
import dev.ragnarok.fenrir.link.types.DialogLink;
import dev.ragnarok.fenrir.link.types.DialogsLink;
import dev.ragnarok.fenrir.link.types.DocLink;
import dev.ragnarok.fenrir.link.types.DomainLink;
import dev.ragnarok.fenrir.link.types.FaveLink;
import dev.ragnarok.fenrir.link.types.FeedSearchLink;
import dev.ragnarok.fenrir.link.types.OwnerLink;
import dev.ragnarok.fenrir.link.types.PageLink;
import dev.ragnarok.fenrir.link.types.PhotoAlbumLink;
import dev.ragnarok.fenrir.link.types.PhotoAlbumsLink;
import dev.ragnarok.fenrir.link.types.PhotoLink;
import dev.ragnarok.fenrir.link.types.PollLink;
import dev.ragnarok.fenrir.link.types.TopicLink;
import dev.ragnarok.fenrir.link.types.VideoAlbumLink;
import dev.ragnarok.fenrir.link.types.VideoLink;
import dev.ragnarok.fenrir.link.types.WallCommentLink;
import dev.ragnarok.fenrir.link.types.WallCommentThreadLink;
import dev.ragnarok.fenrir.link.types.WallLink;
import dev.ragnarok.fenrir.link.types.WallPostLink;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.util.Optional;

public class VkLinkParser {

    private static final Pattern PATTERN_PHOTOS = Pattern.compile("vk\\.com/photos(-?\\d*)"); //+
    private static final Pattern PATTERN_PROFILE_ID = Pattern.compile("(m\\.)?vk\\.com/id(\\d+)$"); //+
    private static final Pattern PATTERN_DOMAIN = Pattern.compile("vk\\.com/([\\w.]+)");
    private static final Pattern PATTERN_WALL_POST = Pattern.compile("vk.com/(?:[\\w.\\d]+\\?(?:[\\w=&]+)?w=)?wall(-?\\d*)_(\\d*)");
    private static final Pattern PATTERN_WALL_POST_NOTIFICATION = Pattern.compile("vk\\.com/feed\\?\\S*scroll_to=wall(-?\\d*)_(\\d*)");
    private static final Pattern PATTERN_AWAY = Pattern.compile("vk\\.com/away(\\.php)?\\?(.*)");
    private static final Pattern PATTERN_DIALOG = Pattern.compile("vk\\.com/im\\?sel=(c?)(-?\\d+)");
    private static final Pattern PATTERN_ALBUMS = Pattern.compile("vk\\.com/albums(-?\\d+)");
    private static final Pattern PATTERN_AUDIOS = Pattern.compile("vk\\.com/audios(-?\\d+)");
    private static final Pattern PATTERN_ARTIST = Pattern.compile("vk\\.com/artist/([^&]*)");
    private static final Pattern PATTERN_ALBUM = Pattern.compile("vk\\.com/album(-?\\d*)_(-?\\d*)");
    private static final Pattern PATTERN_WALL = Pattern.compile("vk\\.com/wall(-?\\d*)");
    private static final Pattern PATTERN_POLL = Pattern.compile("vk\\.com/poll(-?\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_PHOTO = Pattern.compile("vk\\.com/(\\w)*(-)?(\\d)*(\\?z=)?photo(-?\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_VIDEO = Pattern.compile("vk\\.com/video(-?\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_PLAYLIST = Pattern.compile("vk\\.com/music/album/(-?\\d*)_(\\d*)_([^&]*)"); //+
    private static final Pattern PATTERN_PLAYLIST_ALT = Pattern.compile("vk\\.com/.+(?:act=|z=)audio_playlist(-?\\d*)_(\\d*)(?:&access_hash=(\\w+))?");
    private static final Pattern PATTERN_DOC = Pattern.compile("vk\\.com/doc(-?\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_TOPIC = Pattern.compile("vk\\.com/topic-(\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_FAVE = Pattern.compile("vk\\.com/fave");
    private static final Pattern PATTERN_GROUP_ID = Pattern.compile("vk\\.com/(club|event|public)(\\d+)$"); //+
    private static final Pattern PATTERN_FAVE_WITH_SECTION = Pattern.compile("vk\\.com/fave\\?section=([\\w.]+)");
    private static final Pattern PATTERN_ACCESS_KEY = Pattern.compile("access_key=(\\w+)");
    private static final Pattern PATTERN_VIDEO_ALBUM = Pattern.compile("vk\\.com/videos(-?\\d*)[?]section=album_(\\d*)");

    //vk.com/wall-2345345_7834545?reply=15345346
    private static final Pattern PATTERN_WALL_POST_COMMENT = Pattern.compile("vk\\.com/wall(-?\\d*)_(\\d*)\\?reply=(\\d*)");
    private static final Pattern PATTERN_WALL_POST_COMMENT_THREAD = Pattern.compile("vk\\.com/wall(-?\\d*)_(\\d*)\\?reply=(\\d*)&thread=(\\d*)");
    private static final Pattern PATTERN_BOARD = Pattern.compile("vk\\.com/board(\\d+)");
    private static final Pattern PATTERN_FEED_SEARCH = Pattern.compile("vk\\.com/feed\\?q=([^&]*)&section=search");
    private static final Pattern PATTERN_FENRIR_TRACK = Pattern.compile("vk\\.com/audio/(-?\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_FENRIR_SERVER_TRACK_HASH = Pattern.compile("hash=([^&]*)");
    private static final List<IParser> PARSERS = new LinkedList<>();

    static {
        PARSERS.add(string -> {
            Matcher matcher = PATTERN_FEED_SEARCH.matcher(string);
            if (matcher.find()) {
                String q = URLDecoder.decode(matcher.group(1), "UTF-8");
                return Optional.wrap(new FeedSearchLink(q));
            }
            return Optional.empty();
        });
    }

    public static AbsLink parse(String string) {
        if (!string.contains("vk.com")) {
            return null;
        }

        AbsLink vkLink = parseWallCommentThreadLink(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseWallCommentLink(string);
        if (vkLink != null) {
            return vkLink;
        }

        if (string.contains("vk.com/images")) {
            return null;
        }

        if (string.contains("vk.com/search")) {
            return null;
        }

        if (string.contains("vk.com/feed") && !string.contains("?z=photo") && !string.contains("w=wall") && !string.contains("?w=page") && !string.contains("?q=") && !string.contains("scroll_to=wall")) {
            return null;
        }

        if (string.contains("vk.com/friends")) {
            return null;
        }

        if (Pattern.matches(".*vk.com/app\\d.*", string)) {
            return null;
        }

        if (string.endsWith("vk.com/support")) {
            return null;
        }

        if (string.endsWith("vk.com/restore")) {
            return null;
        }

        if (string.contains("vk.com/restore?")) {
            return null;
        }

        if (string.endsWith("vk.com/page")) {
            return null;
        }

        if (string.contains("vk.com/login")) {
            return null;
        }

        if (string.contains("vk.com/bugs")) {
            return null;
        }

        if (Pattern.matches(".*vk.com/note\\d.*", string)) {
            return null;
        }

        if (string.contains("vk.com/dev/")) {
            return null;
        }

        if (string.contains("vk.com/wall") && string.contains("?reply=")) {
            return null;
        }

        if (string.endsWith("vk.com/mail")) {
            return new DialogsLink();
        }

        if (string.endsWith("vk.com/im")) {
            return new DialogsLink();
        }

        for (IParser parser : PARSERS) {
            try {
                AbsLink link = parser.parse(string).get();
                if (link != null) {
                    return link;
                }
            } catch (Exception ignored) {
            }
        }

        vkLink = parseBoard(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parsePage(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parsePhoto(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseAlbum(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseProfileById(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseGroupById(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseTopic(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseWallPost(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseWallPostNotif(string);
        if (vkLink != null) {
            return vkLink;
        }

    /*    vkLink = VkLinkParser.parseAway(string);
        if (vkLink != null) {
            return vkLink;
        }
    */

        if (string.contains("/im?sel")) {
            vkLink = parseDialog(string);
            if (vkLink != null) {
                return vkLink;
            }
        }

        vkLink = parseAlbums(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseWall(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parsePoll(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseVideoAlbum(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseVideo(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseAudioPlaylistAlt(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parsePlaylist(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseAudios(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseArtists(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseAudioTrack(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseDoc(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parsePhotos(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseFave(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseDomain(string);
        return vkLink;
    }

    private static AbsLink parseBoard(String string) {
        Matcher matcher = PATTERN_BOARD.matcher(string);

        try {
            if (matcher.find()) {
                String groupId = matcher.group(1);
                return new BoardLink(parseInt(groupId));
            }
        } catch (Exception ignored) {

        }

        return null;
    }

    private static AbsLink parseAlbum(String string) {
        Matcher matcher = PATTERN_ALBUM.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        String ownerId = matcher.group(1);
        String albumId = matcher.group(2);

        if (albumId.equals("0")) {
            albumId = Long.toString(-6);
        }

        if (albumId.equals("00")) {
            albumId = Long.toString(-7);
        }

        if (albumId.equals("000")) {
            albumId = Long.toString(-15);
        }

        return new PhotoAlbumLink(parseInt(ownerId), parseInt(albumId));
    }

    private static AbsLink parseAlbums(String string) {
        Matcher matcher = PATTERN_ALBUMS.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        String ownerId = matcher.group(1);
        return new PhotoAlbumsLink(parseInt(ownerId));
    }

    private static AbsLink parseAway(String string) {
        if (!PATTERN_AWAY.matcher(string).find()) {
            return null;
        }

        return new AwayLink(string);
    }

    private static AbsLink parseDialog(String string) {
        Matcher matcher = PATTERN_DIALOG.matcher(string);

        try {
            if (matcher.find()) {
                String chat = matcher.group(1);
                int id = parseInt(matcher.group(2));
                boolean isChat = nonEmpty(chat);
                return new DialogLink(isChat ? Peer.fromChatId(id) : Peer.fromOwnerId(id));
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static AbsLink parseDomain(String string) {
        Matcher matcher = PATTERN_DOMAIN.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new DomainLink(string, matcher.group(1));
    }

    private static AbsLink parseGroupById(String string) {
        Matcher matcher = PATTERN_GROUP_ID.matcher(string);

        try {
            if (matcher.find()) {
                return new OwnerLink(-Math.abs(parseInt(matcher.group(2))));
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    private static AbsLink parsePage(String string) {
        if (string.contains("vk.com/pages")
                || string.contains("vk.com/page")
                || string.contains("vk.com") && string.contains("w=page")) {
            return new PageLink(string.replace("m.vk.com/", "vk.com/"));
        }

        return null;
    }

    private static @Nullable
    String parseAccessKey(String string) {
        Matcher matcher = PATTERN_ACCESS_KEY.matcher(string);
        try {
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static AbsLink parsePhoto(String string) {
        Matcher matcher = PATTERN_PHOTO.matcher(string);

        try {
            if (matcher.find()) {
                int ownerId = parseInt(matcher.group(5));
                int photoId = parseInt(matcher.group(6));
                return new PhotoLink(photoId, ownerId, parseAccessKey(string));
            }
        } catch (Exception ignored) {

        }

        return null;
    }

    private static AbsLink parsePhotos(String string) {
        Matcher matcher = PATTERN_PHOTOS.matcher(string);
        try {
            if (matcher.find()) {
                return new PhotoAlbumsLink(parseInt(matcher.group(1)));
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static AbsLink parseProfileById(String string) {
        Matcher matcher = PATTERN_PROFILE_ID.matcher(string);
        try {
            if (matcher.find()) {
                return new OwnerLink(parseInt(matcher.group(2)));
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static AbsLink parseTopic(String string) {
        Matcher matcher = PATTERN_TOPIC.matcher(string);
        try {
            if (matcher.find()) {
                return new TopicLink(parseInt(matcher.group(2)), parseInt(matcher.group(1)));
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    private static AbsLink parseVideoAlbum(String string) {
        Matcher matcher = PATTERN_VIDEO_ALBUM.matcher(string);

        try {
            if (matcher.find()) {
                return new VideoAlbumLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)));
            }
        } catch (NumberFormatException ignored) {

        }

        return null;
    }

    private static AbsLink parseVideo(String string) {
        Matcher matcher = PATTERN_VIDEO.matcher(string);

        try {
            if (matcher.find()) {
                return new VideoLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)), parseAccessKey(string));
            }
        } catch (NumberFormatException ignored) {

        }

        return null;
    }

    public static String parseLocalServerURL(String string) {
        Matcher matcher = PATTERN_FENRIR_SERVER_TRACK_HASH.matcher(string);
        try {
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (NumberFormatException ignored) {

        }
        return null;
    }

    private static AbsLink parseAudioTrack(String string) {
        Matcher matcher = PATTERN_FENRIR_TRACK.matcher(string);

        try {
            if (matcher.find()) {
                return new AudioTrackLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)));
            }
        } catch (NumberFormatException ignored) {

        }

        return null;
    }

    private static AbsLink parseAudioPlaylistAlt(String string) {
        Matcher matcher = PATTERN_PLAYLIST_ALT.matcher(string);
        try {
            if (matcher.find()) {
                return new AudioPlaylistLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)), matcher.group(3));
            }
        } catch (NumberFormatException ignored) {

        }

        return null;
    }

    private static AbsLink parsePlaylist(String string) {
        Matcher matcher = PATTERN_PLAYLIST.matcher(string);

        try {
            if (matcher.find()) {
                return new AudioPlaylistLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)), matcher.group(3));
            }
        } catch (NumberFormatException ignored) {

        }

        return null;
    }

    private static AbsLink parseFave(String string) {
        Matcher matcherWithSection = PATTERN_FAVE_WITH_SECTION.matcher(string);
        Matcher matcher = PATTERN_FAVE.matcher(string);

        if (matcherWithSection.find()) {
            return new FaveLink(matcherWithSection.group(1));
        }

        if (matcher.find()) {
            return new FaveLink();
        }

        return null;
    }

    private static AbsLink parseAudios(String string) {
        Matcher matcher = PATTERN_AUDIOS.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new AudiosLink(parseInt(matcher.group(1)));
    }

    private static AbsLink parseArtists(String string) {
        Matcher matcher = PATTERN_ARTIST.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new ArtistsLink(matcher.group(1));
    }

    private static AbsLink parseDoc(String string) {
        Matcher matcher = PATTERN_DOC.matcher(string);

        try {
            if (matcher.find()) {
                return new DocLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)), parseAccessKey(string));
            }
        } catch (Exception ignored) {

        }

        return null;
    }

    private static AbsLink parseWall(String string) {
        Matcher matcher = PATTERN_WALL.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new WallLink(parseInt(matcher.group(1)));
    }

    private static AbsLink parsePoll(String string) {
        Matcher matcher = PATTERN_POLL.matcher(string);

        try {
            if (matcher.find()) {
                return new PollLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)));
            }
        } catch (Exception ignored) {

        }

        return null;
    }

    private static AbsLink parseWallCommentThreadLink(String string) {
        Matcher matcher = PATTERN_WALL_POST_COMMENT_THREAD.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        WallCommentThreadLink link = new WallCommentThreadLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)), parseInt(matcher.group(3)), parseInt(matcher.group(4)));
        return link.isValid() ? link : null;
    }

    private static AbsLink parseWallCommentLink(String string) {
        Matcher matcher = PATTERN_WALL_POST_COMMENT.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        WallCommentLink link = new WallCommentLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)), parseInt(matcher.group(3)));
        return link.isValid() ? link : null;
    }

    private static AbsLink parseWallPost(String string) {
        Matcher matcher = PATTERN_WALL_POST.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new WallPostLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)));
    }

    private static AbsLink parseWallPostNotif(String string) {
        Matcher matcher = PATTERN_WALL_POST_NOTIFICATION.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new WallPostLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)));
    }

    private interface IParser {
        Optional<AbsLink> parse(String string) throws Exception;
    }
}