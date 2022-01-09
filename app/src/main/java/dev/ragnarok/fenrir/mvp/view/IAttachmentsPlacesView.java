package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.BaseSearchCriteria;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;


public interface IAttachmentsPlacesView {

    void openChatWith(int accountId, int messagesOwnerId, @NonNull Peer peer);

    void openLink(int accountId, @NonNull Link link);

    void openUrl(int accountId, @NonNull String url);

    void openWikiPage(int accountId, @NonNull WikiPage page);

    void openSimplePhotoGallery(int accountId, @NonNull ArrayList<Photo> photos, int index, boolean needUpdate);

    void openPost(int accountId, @NonNull Post post);

    void goToMessagesLookupFWD(int accountId, int peerId, int messageId);

    void goWallReplyOpen(int accountId, WallReply reply);

    void openDocPreview(int accountId, @NonNull Document document);

    void openOwnerWall(int accountId, int ownerId);

    void openForwardMessages(int accountId, @NonNull ArrayList<Message> messages);

    void playAudioList(int accountId, int position, @NonNull ArrayList<Audio> apiAudio);

    void openVideo(int accountId, @NonNull Video apiVideo);

    void openHistoryVideo(int accountId, @NonNull ArrayList<Story> stories, int index);

    void openPoll(int accoountId, @NonNull Poll apiPoll);

    void openSearch(int accountId, @SearchContentType int type, @Nullable BaseSearchCriteria criteria);

    void openComments(int accountId, Commented commented, Integer focusToCommentId);

    void goToLikes(int accountId, String type, int ownerId, int id);

    void goToReposts(int accountId, String type, int ownerId, int id);

    void repostPost(int accountId, @NonNull Post post);

    void openStory(int accountId, @NonNull Story story);

    void openAudioPlaylist(int accountId, @NonNull AudioPlaylist playlist);

    void openPhotoAlbum(int accountId, @NonNull PhotoAlbum album);

    void toMarketAlbumOpen(int accountId, @NonNull MarketAlbum market_album);

    void toMarketOpen(int accountId, @NonNull Market market);

    void toArtistOpen(int accountId, @NonNull AudioArtist artist);
}
