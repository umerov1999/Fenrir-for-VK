package dev.ragnarok.fenrir.place;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.activity.PhotoPagerActivity;
import dev.ragnarok.fenrir.activity.VideoPlayerActivity;
import dev.ragnarok.fenrir.dialog.ResolveDomainDialog;
import dev.ragnarok.fenrir.fragment.AbsWallFragment;
import dev.ragnarok.fenrir.fragment.AudioCatalogFragment;
import dev.ragnarok.fenrir.fragment.AudioPlayerFragment;
import dev.ragnarok.fenrir.fragment.AudiosFragment;
import dev.ragnarok.fenrir.fragment.BrowserFragment;
import dev.ragnarok.fenrir.fragment.ChatUsersFragment;
import dev.ragnarok.fenrir.fragment.CommentsFragment;
import dev.ragnarok.fenrir.fragment.CreatePhotoAlbumFragment;
import dev.ragnarok.fenrir.fragment.CreatePollFragment;
import dev.ragnarok.fenrir.fragment.DocPreviewFragment;
import dev.ragnarok.fenrir.fragment.DocsFragment;
import dev.ragnarok.fenrir.fragment.FeedFragment;
import dev.ragnarok.fenrir.fragment.FeedbackFragment;
import dev.ragnarok.fenrir.fragment.FriendsByPhonesFragment;
import dev.ragnarok.fenrir.fragment.FwdsFragment;
import dev.ragnarok.fenrir.fragment.GifPagerFragment;
import dev.ragnarok.fenrir.fragment.GroupChatsFragment;
import dev.ragnarok.fenrir.fragment.LikesFragment;
import dev.ragnarok.fenrir.fragment.MarketViewFragment;
import dev.ragnarok.fenrir.fragment.MessagesLookFragment;
import dev.ragnarok.fenrir.fragment.NotReadMessagesFragment;
import dev.ragnarok.fenrir.fragment.PollFragment;
import dev.ragnarok.fenrir.fragment.PreferencesFragment;
import dev.ragnarok.fenrir.fragment.SinglePhotoFragment;
import dev.ragnarok.fenrir.fragment.StoryPagerFragment;
import dev.ragnarok.fenrir.fragment.TopicsFragment;
import dev.ragnarok.fenrir.fragment.VKPhotosFragment;
import dev.ragnarok.fenrir.fragment.VideoAlbumsByVideoFragment;
import dev.ragnarok.fenrir.fragment.VideoPreviewFragment;
import dev.ragnarok.fenrir.fragment.VideosFragment;
import dev.ragnarok.fenrir.fragment.VideosTabsFragment;
import dev.ragnarok.fenrir.fragment.WallPostFragment;
import dev.ragnarok.fenrir.fragment.attachments.PostCreateFragment;
import dev.ragnarok.fenrir.fragment.attachments.RepostFragment;
import dev.ragnarok.fenrir.fragment.conversation.ConversationFragmentFactory;
import dev.ragnarok.fenrir.fragment.fave.FaveTabsFragment;
import dev.ragnarok.fenrir.fragment.friends.FriendsTabsFragment;
import dev.ragnarok.fenrir.fragment.search.AudioSearchTabsFragment;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.SearchTabsFragment;
import dev.ragnarok.fenrir.fragment.search.SingleTabSearchFragment;
import dev.ragnarok.fenrir.fragment.search.criteria.BaseSearchCriteria;
import dev.ragnarok.fenrir.fragment.wallattachments.WallSearchCommentsAttachmentsFragment;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Banned;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.FriendsCounters;
import dev.ragnarok.fenrir.model.GroupSettings;
import dev.ragnarok.fenrir.model.InternalVideoSize;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.ModelsBundle;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoAlbumEditor;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserDetails;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.WallEditorAttrs;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class PlaceFactory {

    private PlaceFactory() {

    }

    public static Place getUserDetailsPlace(int accountId, @NonNull User user, @NonNull UserDetails details) {
        return new Place(Place.USER_DETAILS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.USER, user)
                .withParcelableExtra("details", details);
    }

    public static Place getDrawerEditPlace() {
        return new Place(Place.DRAWER_EDIT);
    }

    public static Place getSideDrawerEditPlace() {
        return new Place(Place.SIDE_DRAWER_EDIT);
    }

    public static Place getProxyAddPlace() {
        return new Place(Place.PROXY_ADD);
    }

    public static Place getUserBlackListPlace(int accountId) {
        return new Place(Place.USER_BLACKLIST)
                .withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getRequestExecutorPlace(int accountId) {
        return new Place(Place.REQUEST_EXECUTOR)
                .withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getCommunityManagerEditPlace(int accountId, int groupId, Manager manager) {
        return new Place(Place.COMMUNITY_MANAGER_EDIT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.GROUP_ID, groupId)
                .withParcelableExtra(Extra.MANAGER, manager);
    }

    public static Place getCommunityManagerAddPlace(int accountId, int groupId, ArrayList<User> users) {
        Place place = new Place(Place.COMMUNITY_MANAGER_ADD)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.GROUP_ID, groupId);

        place.getArgs().putParcelableArrayList(Extra.USERS, users);
        return place;
    }

    public static Place getTmpSourceGalleryPlace(int accountId, @NonNull TmpSource source, int index) {
        return new Place(Place.VK_PHOTO_TMP_SOURCE)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.INDEX, index)
                .withParcelableExtra(Extra.SOURCE, source);
    }

    public static Place getTmpSourceGalleryPlace(int accountId, long ptr, int index) {
        return new Place(Place.VK_PHOTO_TMP_SOURCE)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.INDEX, index)
                .withLongExtra(Extra.SOURCE, ptr);
    }

    public static Place getCommunityAddBanPlace(int accountId, int groupId, ArrayList<User> users) {
        Place place = new Place(Place.COMMUNITY_ADD_BAN)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.GROUP_ID, groupId);
        place.getArgs().putParcelableArrayList(Extra.USERS, users);
        return place;
    }

    public static Place getCommunityBanEditPlace(int accountId, int groupId, Banned banned) {
        return new Place(Place.COMMUNITY_BAN_EDIT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.GROUP_ID, groupId)
                .withParcelableExtra(Extra.BANNED, banned);
    }

    public static Place getCommunityControlPlace(int accountId, Community community, GroupSettings settings) {
        return new Place(Place.COMMUNITY_CONTROL)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.SETTINGS, settings)
                .withParcelableExtra(Extra.OWNER, community);
    }

    public static Place getShowComunityInfoPlace(int accountId, Community community) {
        return new Place(Place.COMMUNITY_INFO)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.OWNER, community);
    }

    public static Place getShowComunityLinksInfoPlace(int accountId, Community community) {
        return new Place(Place.COMMUNITY_INFO_LINKS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.OWNER, community);
    }

    public static Place getNewsfeedCommentsPlace(int accountId) {
        return new Place(Place.NEWSFEED_COMMENTS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getSingleTabSearchPlace(int accountId, @SearchContentType int type, @Nullable BaseSearchCriteria criteria) {
        return new Place(Place.SINGLE_SEARCH)
                .setArguments(SingleTabSearchFragment.buildArgs(accountId, type, criteria));
    }

    public static Place getLogsPlace() {
        return new Place(Place.LOGS);
    }

    public static Place getLocalImageAlbumPlace(LocalImageAlbum album) {
        return new Place(Place.LOCAL_IMAGE_ALBUM)
                .withParcelableExtra(Extra.ALBUM, album);
    }

    public static Place getCommentCreatePlace(int accountId, int commentId, int sourceOwnerId, String body) {
        return new Place(Place.COMMENT_CREATE)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.COMMENT_ID, commentId)
                .withIntExtra(Extra.OWNER_ID, sourceOwnerId)
                .withStringExtra(Extra.BODY, body);
    }

    public static Place getPhotoAlbumGalleryPlace(int accountId, int albumId, int ownerId, long parcelNativePointer, int position, boolean readOnly, boolean invert) {
        return new Place(Place.VK_PHOTO_ALBUM_GALLERY_NATIVE)
                .setArguments(PhotoPagerActivity.buildArgsForAlbum(accountId, albumId, ownerId, parcelNativePointer, position, readOnly, invert));
    }

    public static Place getPhotoAlbumGalleryPlace(int accountId, int albumId, int ownerId, @NonNull ArrayList<Photo> photos, int position, boolean readOnly, boolean invert) {
        return new Place(Place.VK_PHOTO_ALBUM_GALLERY)
                .setArguments(PhotoPagerActivity.buildArgsForAlbum(accountId, albumId, ownerId, photos, position, readOnly, invert));
    }

    public static Place getPhotoAlbumGalleryPlace(int accountId, int albumId, int ownerId, @NonNull TmpSource source, int position, boolean readOnly, boolean invert) {
        return new Place(Place.VK_PHOTO_ALBUM_GALLERY_SAVED)
                .setArguments(PhotoPagerActivity.buildArgsForAlbum(accountId, albumId, ownerId, source, position, readOnly, invert));
    }

    public static Place getSimpleGalleryPlace(int accountId, @NonNull ArrayList<Photo> photos, int position, boolean needRefresh) {
        return new Place(Place.SIMPLE_PHOTO_GALLERY)
                .setArguments(PhotoPagerActivity.buildArgsForSimpleGallery(accountId, position, photos, needRefresh));
    }

    public static Place getFavePhotosGallery(int accountId, @NonNull ArrayList<Photo> photos, int position) {
        return new Place(Place.FAVE_PHOTOS_GALLERY)
                .setArguments(PhotoPagerActivity.buildArgsForFave(accountId, photos, position));
    }

    public static Place getCreatePollPlace(int accountId, int ownerId) {
        return new Place(Place.CREATE_POLL).setArguments(CreatePollFragment.buildArgs(accountId, ownerId));
    }

    public static Place getSettingsThemePlace() {
        return new Place(Place.SETTINGS_THEME);
    }

    public static Place getPollPlace(int accountId, @NonNull Poll poll) {
        return new Place(Place.POLL).setArguments(PollFragment.buildArgs(accountId, poll));
    }

    public static Place getGifPagerPlace(int accountId, @NonNull ArrayList<Document> documents, int index) {
        Place place = new Place(Place.GIF_PAGER);
        place.setArguments(GifPagerFragment.buildArgs(accountId, documents, index));
        return place;
    }

    public static Place getWallAttachmentsPlace(int accountId, int ownerId, String type) {
        return new Place(Place.WALL_ATTACHMENTS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, ownerId)
                .withStringExtra(Extra.TYPE, type);
    }

    public static Place getMessagesLookupPlace(int aid, int peerId, int focusMessageId, @Nullable Message message) {
        return new Place(Place.MESSAGE_LOOKUP)
                .setArguments(MessagesLookFragment.buildArgs(aid, peerId, focusMessageId, message));
    }

    public static Place getWallSearchCommentsAttachmentsPlace(int accountId, int ownerId, @NonNull ArrayList<Integer> posts) {
        return new Place(Place.SEARCH_COMMENTS)
                .setArguments(WallSearchCommentsAttachmentsFragment.buildArgs(accountId, ownerId, posts));
    }

    public static Place getUnreadMessagesPlace(int aid, int focusMessageId, int incoming, int outgoing, int unreadCount, @NonNull Peer peer) {
        return new Place(Place.UNREAD_MESSAGES)
                .setArguments(NotReadMessagesFragment.buildArgs(aid, focusMessageId, incoming, outgoing, unreadCount, peer));
    }

    public static Place getEditPhotoAlbumPlace(int aid, @NonNull PhotoAlbum album, @NonNull PhotoAlbumEditor editor) {
        return new Place(Place.EDIT_PHOTO_ALBUM)
                .setArguments(CreatePhotoAlbumFragment.buildArgsForEdit(aid, album, editor));
    }

    public static Place getCreatePhotoAlbumPlace(int aid, int ownerId) {
        return new Place(Place.CREATE_PHOTO_ALBUM)
                .setArguments(CreatePhotoAlbumFragment.buildArgsForCreate(aid, ownerId));
    }

    public static Place getMarketAlbumPlace(int accountId, int ownerId) {
        return new Place(Place.MARKET_ALBUMS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, ownerId);
    }

    public static Place getMarketPlace(int accountId, int ownerId, int albumId) {
        return new Place(Place.MARKETS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, ownerId)
                .withIntExtra(Extra.ALBUM_ID, albumId);
    }

    public static Place getPhotoAllCommentsPlace(int accountId, int ownerId) {
        return new Place(Place.PHOTO_ALL_COMMENT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, ownerId);
    }

    public static Place getMarketViewPlace(int accountId, @NonNull Market market) {
        return new Place(Place.MARKET_VIEW).setArguments(MarketViewFragment.buildArgs(accountId, market));
    }

    public static Place getNotificationSettingsPlace() {
        return new Place(Place.NOTIFICATION_SETTINGS);
    }

    public static Place getSecuritySettingsPlace() {
        return new Place(Place.SECURITY);
    }

    public static Place getVkInternalPlayerPlace(Video video, @InternalVideoSize int size, boolean isLocal) {
        Place place = new Place(Place.VK_INTERNAL_PLAYER);
        place.prepareArguments().putParcelable(VideoPlayerActivity.EXTRA_VIDEO, video);
        place.prepareArguments().putInt(VideoPlayerActivity.EXTRA_SIZE, size);
        place.prepareArguments().putBoolean(VideoPlayerActivity.EXTRA_LOCAL, isLocal);
        return place;
    }

    public static Place getResolveDomainPlace(int aid, String url, String domain) {
        return new Place(Place.RESOLVE_DOMAIN).setArguments(ResolveDomainDialog.buildArgs(aid, url, domain));
    }

    public static Place getBookmarksPlace(int aid, int tab) {
        return new Place(Place.BOOKMARKS).setArguments(FaveTabsFragment.buildArgs(aid, tab));
    }

    public static Place getNotificationsPlace(int aid) {
        return new Place(Place.NOTIFICATIONS).setArguments(FeedbackFragment.buildArgs(aid));
    }

    public static Place getFeedPlace(int aid) {
        return new Place(Place.FEED).setArguments(FeedFragment.buildArgs(aid));
    }

    public static Place getDocumentsPlace(int aid, int ownerId, String action) {
        return new Place(Place.DOCS).setArguments(DocsFragment.buildArgs(aid, ownerId, action));
    }

    public static Place getPreferencesPlace(int aid) {
        return new Place(Place.PREFERENCES).setArguments(PreferencesFragment.buildArgs(aid));
    }

    public static Place getDialogsPlace(int accountId, int dialogsOwnerId, @Nullable String subtitle) {
        return new Place(Place.DIALOGS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, dialogsOwnerId)
                .withStringExtra(Extra.SUBTITLE, subtitle);
    }

    public static Place getChatPlace(int accountId, int messagesOwnerId, @NonNull Peer peer) {
        return new Place(Place.CHAT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, messagesOwnerId)
                .withParcelableExtra(Extra.PEER, peer);
    }

    public static Place getLocalServerPhotosPlace(int accountId) {
        return new Place(Place.LOCAL_SERVER_PHOTO)
                .withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getVKPhotosAlbumPlace(int accountId, int ownerId, int albumId, String action) {
        return new Place(Place.VK_PHOTO_ALBUM).setArguments(VKPhotosFragment.buildArgs(accountId, ownerId, albumId, action));
    }

    public static Place getVKPhotoAlbumsPlace(int accountId, int ownerId, String action, ParcelableOwnerWrapper ownerWrapper) {
        return new Place(Place.VK_PHOTO_ALBUMS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, ownerId)
                .withStringExtra(Extra.ACTION, action)
                .withParcelableExtra(Extra.OWNER, ownerWrapper);
    }

    public static Place getExternalLinkPlace(int accountId, String url) {
        return new Place(Place.EXTERNAL_LINK).setArguments(BrowserFragment.buildArgs(accountId, url));
    }

    public static Place getRepostPlace(int accountId, Integer gid, Post post) {
        return new Place(Place.REPOST).setArguments(RepostFragment.buildArgs(accountId, gid, post));
    }

    public static Place getEditCommentPlace(int accountId, Comment comment, Integer commemtId) {
        Place ret = new Place(Place.EDIT_COMMENT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.COMMENT, comment);
        if (commemtId != null)
            ret.withIntExtra(Extra.COMMENT_ID, commemtId);
        return ret;
    }

    public static Place getAudiosPlace(int accountId, int ownerId) {
        return new Place(Place.AUDIOS).withIntExtra(Extra.ACCOUNT_ID, accountId).withIntExtra(Extra.OWNER_ID, ownerId);
    }

    public static Place getAudiosInCatalogBlock(int accountId, String block_Id, String title) {
        return new Place(Place.CATALOG_BLOCK_AUDIOS).withIntExtra(Extra.ACCOUNT_ID, accountId).withStringExtra(Extra.ID, block_Id).withStringExtra(Extra.TITLE, title);
    }

    public static Place getPlaylistsInCatalogBlock(int accountId, String block_Id, String title) {
        return new Place(Place.CATALOG_BLOCK_PLAYLISTS).withIntExtra(Extra.ACCOUNT_ID, accountId).withStringExtra(Extra.ID, block_Id).withStringExtra(Extra.TITLE, title);
    }

    public static Place getVideosInCatalogBlock(int accountId, String block_Id, String title) {
        return new Place(Place.CATALOG_BLOCK_VIDEOS).withIntExtra(Extra.ACCOUNT_ID, accountId).withStringExtra(Extra.ID, block_Id).withStringExtra(Extra.TITLE, title);
    }

    public static Place getLinksInCatalogBlock(int accountId, String block_Id, String title) {
        return new Place(Place.CATALOG_BLOCK_LINKS).withIntExtra(Extra.ACCOUNT_ID, accountId).withStringExtra(Extra.ID, block_Id).withStringExtra(Extra.TITLE, title);
    }

    public static Place getShortLinks(int accountId) {
        return new Place(Place.SHORT_LINKS).withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getImportantMessages(int accountId) {
        return new Place(Place.IMPORTANT_MESSAGES).withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getOwnerArticles(int accountId, int ownerId) {
        return new Place(Place.OWNER_ARTICLES).withIntExtra(Extra.ACCOUNT_ID, accountId).withIntExtra(Extra.OWNER_ID, ownerId);
    }

    public static Place getMentionsPlace(int accountId, int ownerId) {
        return new Place(Place.MENTIONS).withIntExtra(Extra.ACCOUNT_ID, accountId).withIntExtra(Extra.OWNER_ID, ownerId);
    }

    public static Place getAudiosInAlbumPlace(int accountId, int ownerId, Integer albumId, String access_key) {
        return new Place(Place.AUDIOS_IN_ALBUM).setArguments(AudiosFragment.buildArgs(accountId, ownerId, albumId, access_key));
    }

    public static Place SearchByAudioPlace(int accountId, int audio_ownerId, int audio_id) {
        return new Place(Place.SEARCH_BY_AUDIO).withIntExtra(Extra.ACCOUNT_ID, accountId).withIntExtra(Extra.OWNER_ID, audio_ownerId).withIntExtra(Extra.ID, audio_id);
    }

    public static Place getPlayerPlace(int accountId) {
        return new Place(Place.PLAYER).setArguments(AudioPlayerFragment.buildArgs(accountId));
    }

    public static Place getVideosPlace(int accountId, int ownerId, String action) {
        return new Place(Place.VIDEOS).setArguments(VideosTabsFragment.buildArgs(accountId, ownerId, action));
    }

    public static Place getVideoAlbumPlace(int accountId, int ownerId, int albumId, String action, @Nullable String albumTitle) {
        return new Place(Place.VIDEO_ALBUM)
                .setArguments(VideosFragment.buildArgs(accountId, ownerId, albumId, action, albumTitle));
    }

    public static Place getVideoPreviewPlace(int accountId, @NonNull Video video) {
        return new Place(Place.VIDEO_PREVIEW)
                .setArguments(VideoPreviewFragment.buildArgs(accountId, video.getOwnerId(), video.getId(), video.getAccessKey(), video));
    }

    public static Place getHistoryVideoPreviewPlace(int accountId, @NonNull ArrayList<Story> stories, int index) {
        return new Place(Place.STORY_PLAYER)
                .setArguments(StoryPagerFragment.buildArgs(accountId, stories, index));
    }

    public static Place getVideoPreviewPlace(int accountId, int ownerId, int videoId, @Nullable String accessKey, @Nullable Video video) {
        return new Place(Place.VIDEO_PREVIEW)
                .setArguments(VideoPreviewFragment.buildArgs(accountId, ownerId, videoId, accessKey, video));
    }

    public static Place getSingleURLPhotoPlace(String url, String prefix, String photo_prefix) {
        return new Place(Place.SINGLE_PHOTO)
                .setArguments(SinglePhotoFragment.buildArgs(url, prefix, photo_prefix));
    }

    public static Place getLikesCopiesPlace(int accountId, String type, int ownerId, int itemId, String filter) {
        return new Place(Place.LIKES_AND_COPIES)
                .setArguments(LikesFragment.buildArgs(accountId, type, ownerId, itemId, filter));
    }

    public static Place getCommunitiesPlace(int accountId, int userId) {
        return new Place(Place.COMMUNITIES)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.USER_ID, userId);
    }

    public static Place getFriendsFollowersPlace(int accountId, int userId, int tab, FriendsCounters counters) {
        return new Place(Place.FRIENDS_AND_FOLLOWERS)
                .setArguments(FriendsTabsFragment.buildArgs(accountId, userId, tab, counters));
    }

    public static Place getChatMembersPlace(int accountId, int chatId) {
        return new Place(Place.CHAT_MEMBERS).setArguments(ChatUsersFragment.buildArgs(accountId, chatId));
    }

    public static Place getOwnerWallPlace(int accountId, @NonNull Owner owner) {
        int ownerId = owner.getOwnerId();
        return getOwnerWallPlace(accountId, ownerId, owner);
    }

    public static Place getOwnerWallPlace(int accountId, int ownerId, @Nullable Owner owner) {
        return new Place(Place.WALL).setArguments(AbsWallFragment.buildArgs(accountId, ownerId, owner));
    }

    public static Place getTopicsPlace(int accountId, int ownerId) {
        return new Place(Place.TOPICS).setArguments(TopicsFragment.buildArgs(accountId, ownerId));
    }

    public static Place getSearchPlace(int accountId, int tab) {
        return new Place(Place.SEARCH).setArguments(SearchTabsFragment.buildArgs(accountId, tab));
    }

    public static Place getAudiosTabsSearchPlace(int accountId) {
        return new Place(Place.AUDIOS_SEARCH_TABS).setArguments(AudioSearchTabsFragment.buildArgs(accountId));
    }

    public static Place getGroupChatsPlace(int accountId, int groupId) {
        return new Place(Place.GROUP_CHATS).setArguments(GroupChatsFragment.buildArgs(accountId, groupId));
    }

    public static Place getCreatePostPlace(int accountId, int ownerId, @EditingPostType int editingType,
                                           @Nullable List<AbsModel> input, @NonNull WallEditorAttrs attrs,
                                           @Nullable ArrayList<Uri> streams, @Nullable String body, @Nullable String mime) {
        ModelsBundle bundle = new ModelsBundle(Utils.safeCountOf(input));
        if (Objects.nonNull(input)) {
            bundle.append(input);
        }

        return new Place(Place.BUILD_NEW_POST)
                .setArguments(PostCreateFragment.buildArgs(accountId, ownerId, editingType, bundle, attrs, streams, body, mime));
    }

    public static Place getForwardMessagesPlace(int accountId, ArrayList<Message> messages) {
        return new Place(Place.FORWARD_MESSAGES).setArguments(FwdsFragment.buildArgs(accountId, messages));
    }

    public static Place getEditPostPlace(int accountId, @NonNull Post post, @NonNull WallEditorAttrs attrs) {
        return new Place(Place.EDIT_POST)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.POST, post)
                .withParcelableExtra(Extra.ATTRS, attrs);
    }

    public static Place getPostPreviewPlace(int accountId, int postId, int ownerId) {
        return getPostPreviewPlace(accountId, postId, ownerId, null);
    }

    public static Place getPostPreviewPlace(int accountId, int postId, int ownerId, Post post) {
        return new Place(Place.WALL_POST)
                .setArguments(WallPostFragment.buildArgs(accountId, postId, ownerId, post));
    }

    public static Place getAlbumsByVideoPlace(int accountId, int ownerId, int video_ownerId, int video_Id) {
        return new Place(Place.ALBUMS_BY_VIDEO)
                .setArguments(VideoAlbumsByVideoFragment.buildArgs(accountId, ownerId, video_ownerId, video_Id));
    }

    public static Place getDocPreviewPlace(int accountId, int docId, int ownerId, @Nullable String accessKey, @Nullable Document document) {
        Place place = new Place(Place.DOC_PREVIEW);
        place.setArguments(DocPreviewFragment.buildArgs(accountId, docId, ownerId, accessKey, document));
        return place;
    }

    public static Place getDocPreviewPlace(int accountId, @NonNull Document document) {
        return getDocPreviewPlace(accountId, document.getId(), document.getOwnerId(), document.getAccessKey(), document);
    }

    public static Place getConversationAttachmentsPlace(int accountId, int peerId, String type) {
        return new Place(Place.CONVERSATION_ATTACHMENTS)
                .setArguments(ConversationFragmentFactory.buildArgs(accountId, peerId, type));
    }

    public static Place getCommentsPlace(int accountId, Commented commented, Integer focusToCommentId) {
        return new Place(Place.COMMENTS)
                .setArguments(CommentsFragment.buildArgs(accountId, commented, focusToCommentId, null));
    }

    public static Place getCommentsThreadPlace(int accountId, Commented commented, Integer focusToCommentId, Integer commemtId) {
        return new Place(Place.COMMENTS)
                .setArguments(CommentsFragment.buildArgs(accountId, commented, focusToCommentId, commemtId));
    }

    public static Place getArtistPlace(int accountId, String id, boolean isHideToolbar) {
        return new Place(Place.ARTIST)
                .setArguments(AudioCatalogFragment.buildArgs(accountId, id, isHideToolbar));
    }

    public static Place getFriendsByPhonesPlace(int accountId) {
        return new Place(Place.FRIENDS_BY_PHONES)
                .setArguments(FriendsByPhonesFragment.buildArgs(accountId));
    }

    public static Place getGiftsPlace(int accountId, int ownerId) {
        return new Place(Place.GIFTS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, ownerId);
    }
}
