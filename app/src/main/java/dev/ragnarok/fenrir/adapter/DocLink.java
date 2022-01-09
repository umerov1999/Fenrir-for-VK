package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.Calendar;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Call;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Event;
import dev.ragnarok.fenrir.model.Graffiti;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.NotSupported;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoSizes;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Types;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class DocLink {

    private static final String URL = "URL";
    private static final String W = "WIKI";
    public final AbsModel attachment;
    private final int type;

    public DocLink(AbsModel attachment) {
        this.attachment = attachment;
        type = typeOf(attachment);
    }

    private static int typeOf(AbsModel model) {
        if (model instanceof Document) {
            return Types.DOC;
        }

        if (model instanceof Post) {
            return Types.POST;
        }

        if (model instanceof Link) {
            return Types.LINK;
        }

        if (model instanceof Poll) {
            return Types.POLL;
        }

        if (model instanceof WikiPage) {
            return Types.WIKI_PAGE;
        }

        if (model instanceof Story) {
            return Types.STORY;
        }

        if (model instanceof Call) {
            return Types.CALL;
        }

        if (model instanceof AudioArtist) {
            return Types.ARTIST;
        }

        if (model instanceof WallReply) {
            return Types.WALL_REPLY;
        }

        if (model instanceof AudioPlaylist) {
            return Types.AUDIO_PLAYLIST;
        }

        if (model instanceof Graffiti) {
            return Types.GRAFFITY;
        }

        if (model instanceof PhotoAlbum) {
            return Types.ALBUM;
        }

        if (model instanceof NotSupported) {
            return Types.NOT_SUPPORTED;
        }

        if (model instanceof Event) {
            return Types.EVENT;
        }

        if (model instanceof Market) {
            return Types.MARKET;
        }

        if (model instanceof MarketAlbum) {
            return Types.MARKET_ALBUM;
        }

        throw new IllegalArgumentException();
    }

    public int getType() {
        return type;
    }

    public String getImageUrl() {
        switch (type) {
            case Types.DOC:
                Document doc = (Document) attachment;
                return doc.getPreviewWithSize(Settings.get().main().getPrefPreviewImageSize(), true);

            case Types.POST:
                return ((Post) attachment).getAuthorPhoto();

            case Types.EVENT:
                return ((Event) attachment).getSubjectPhoto();

            case Types.WALL_REPLY:
                return ((WallReply) attachment).getAuthorPhoto();

            case Types.GRAFFITY:
                return ((Graffiti) attachment).getUrl();

            case Types.STORY:
                return ((Story) attachment).getOwner().getMaxSquareAvatar();

            case Types.ALBUM:
                PhotoAlbum album = (PhotoAlbum) attachment;
                if (Objects.nonNull(album.getSizes())) {
                    PhotoSizes sizes = album.getSizes();
                    return sizes.getUrlForSize(Settings.get().main().getPrefPreviewImageSize(), true);
                }
                return null;

            case Types.MARKET_ALBUM:
                MarketAlbum market_album = (MarketAlbum) attachment;
                if (Objects.nonNull(market_album.getPhoto()) && Objects.nonNull(market_album.getPhoto().getSizes())) {
                    PhotoSizes sizes = market_album.getPhoto().getSizes();
                    return sizes.getUrlForSize(Settings.get().main().getPrefPreviewImageSize(), true);
                }
                return null;

            case Types.ARTIST:
                return ((AudioArtist) attachment).getMaxPhoto();

            case Types.MARKET:
                return ((Market) attachment).getThumb_photo();

            case Types.AUDIO_PLAYLIST:
                return ((AudioPlaylist) attachment).getThumb_image();

            case Types.LINK:
                Link link = (Link) attachment;

                if (link.getPhoto() == null && link.getPreviewPhoto() != null)
                    return link.getPreviewPhoto();

                if (Objects.nonNull(link.getPhoto()) && Objects.nonNull(link.getPhoto().getSizes())) {
                    PhotoSizes sizes = link.getPhoto().getSizes();
                    return sizes.getUrlForSize(Settings.get().main().getPrefPreviewImageSize(), true);
                }

                return null;
        }

        return null;
    }

    public String getTitle(@NonNull Context context) {
        String title;
        switch (type) {
            case Types.DOC:
                return ((Document) attachment).getTitle();

            case Types.POST:
                return ((Post) attachment).getAuthorName();

            case Types.EVENT:
                return ((Event) attachment).getSubjectName();

            case Types.WALL_REPLY:
                return ((WallReply) attachment).getAuthorName();

            case Types.AUDIO_PLAYLIST:
                return ((AudioPlaylist) attachment).getTitle();

            case Types.ALBUM:
                return ((PhotoAlbum) attachment).getTitle();

            case Types.MARKET:
                return ((Market) attachment).getTitle();

            case Types.ARTIST:
                return ((AudioArtist) attachment).getName();

            case Types.MARKET_ALBUM:
                return ((MarketAlbum) attachment).getTitle();

            case Types.LINK:
                title = ((Link) attachment).getTitle();
                if (TextUtils.isEmpty(title)) {
                    title = "[" + context.getString(R.string.attachment_link).toLowerCase() + "]";
                }
                return title;

            case Types.NOT_SUPPORTED:
                return context.getString(R.string.not_yet_implemented_message);

            case Types.POLL:
                Poll poll = (Poll) attachment;
                return context.getString(poll.isAnonymous() ? R.string.anonymous_poll : R.string.open_poll);

            case Types.STORY:
                return ((Story) attachment).getOwner().getFullName();

            case Types.WIKI_PAGE:
                return context.getString(R.string.wiki_page);

            case Types.CALL:
                int initiator = ((Call) attachment).getInitiator_id();
                return initiator == Settings.get().accounts().getCurrent() ? context.getString(R.string.input_call) : context.getString(R.string.output_call);
        }
        return null;
    }

    public String getExt(@NonNull Context context) {
        switch (type) {
            case Types.DOC:
                return ((Document) attachment).getExt();
            case Types.POST:
            case Types.WALL_REPLY:
                return null;
            case Types.LINK:
                return URL;
            case Types.WIKI_PAGE:
                return W;
            case Types.STORY:
                return context.getString(R.string.story);
            case Types.AUDIO_PLAYLIST:
                return context.getString(R.string.playlist);
        }
        return null;
    }

    public String getSecondaryText(@NonNull Context context) {
        switch (type) {
            case Types.DOC:
                return AppTextUtils.getSizeString((int) ((Document) attachment).getSize());

            case Types.NOT_SUPPORTED:
                NotSupported ns = (NotSupported) attachment;
                return ns.getType() + ": " + ns.getBody();

            case Types.POST:
                Post post = (Post) attachment;
                return post.hasText() ? post.getText() : (post.hasAttachments() ? "" : context.getString(R.string.wall_post_view));

            case Types.EVENT:
                Event event = (Event) attachment;
                return Utils.firstNonEmptyString(event.getButton_text(), " ") + ", " + Utils.firstNonEmptyString(event.getText());

            case Types.WALL_REPLY:
                WallReply comment = (WallReply) attachment;
                return comment.getText();

            case Types.LINK:
                return ((Link) attachment).getUrl();

            case Types.ALBUM:
                return Utils.firstNonEmptyString(((PhotoAlbum) attachment).getDescription(), " ") +
                        " " + context.getString(R.string.photos_count, ((PhotoAlbum) attachment).getSize());

            case Types.POLL:
                return ((Poll) attachment).getQuestion();

            case Types.WIKI_PAGE:
                return ((WikiPage) attachment).getTitle();

            case Types.CALL:
                return ((Call) attachment).getLocalizedState(context);

            case Types.MARKET:
                return ((Market) attachment).getPrice() + ", " + AppTextUtils.reduceStringForPost(Utils.firstNonEmptyString(((Market) attachment).getDescription(), " "));

            case Types.MARKET_ALBUM:
                return context.getString(R.string.markets_count, ((MarketAlbum) attachment).getCount());

            case Types.AUDIO_PLAYLIST:
                return Utils.firstNonEmptyString(((AudioPlaylist) attachment).getArtist_name(), " ") + " " +
                        ((AudioPlaylist) attachment).getCount() + " " + context.getString(R.string.audios_pattern_count);

            case Types.STORY: {
                Story item = ((Story) attachment);
                if (item.getExpires() <= 0)
                    return null;
                else {
                    if (item.isIs_expired()) {
                        return context.getString(R.string.is_expired);
                    } else {
                        long exp = (item.getExpires() - Calendar.getInstance().getTime().getTime() / 1000) / 3600;
                        return (context.getString(R.string.expires, String.valueOf(exp), context.getString(Utils.declOfNum(exp, new int[]{R.string.hour, R.string.hour_sec, R.string.hours}))));
                    }
                }
            }
        }
        return null;
    }
}
