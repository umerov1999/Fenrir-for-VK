package dev.ragnarok.fenrir.fragment.base;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder;
import dev.ragnarok.fenrir.adapter.listener.OwnerClickListener;
import dev.ragnarok.fenrir.dialog.PostShareDialog;
import dev.ragnarok.fenrir.domain.ILikesInteractor;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.BaseSearchCriteria;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
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
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackService;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;

public abstract class PlaceSupportMvpFragment<P extends PlaceSupportPresenter<V>, V extends IMvpView & IAttachmentsPlacesView & IAccountDependencyView>
        extends BaseMvpFragment<P, V> implements AttachmentsViewBinder.OnAttachmentsActionCallback, IAttachmentsPlacesView, OwnerClickListener {

    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text));

    @Override
    public void onOwnerClick(int ownerId) {
        callPresenter(p -> p.fireOwnerClick(ownerId));
    }

    @Override
    public void openChatWith(int accountId, int messagesOwnerId, @NonNull Peer peer) {
        PlaceFactory.getChatPlace(accountId, messagesOwnerId, peer).tryOpenWith(requireActivity());
    }

    @Override
    public void onPollOpen(@NonNull Poll apiPoll) {
        callPresenter(p -> p.firePollClick(apiPoll));
    }

    @Override
    public void onVideoPlay(@NonNull Video video) {
        callPresenter(p -> p.fireVideoClick(video));
    }

    @Override
    public void onAudioPlay(int position, @NonNull ArrayList<Audio> apiAudio) {
        callPresenter(p -> p.fireAudioPlayClick(position, apiAudio));
    }

    @Override
    public void onForwardMessagesOpen(@NonNull ArrayList<Message> messages) {
        callPresenter(p -> p.fireForwardMessagesClick(messages));
    }

    @Override
    public void onOpenOwner(int ownerId) {
        callPresenter(p -> p.fireOwnerClick(ownerId));
    }

    @Override
    public void onGoToMessagesLookup(@NonNull Message message) {
        callPresenter(p -> p.fireGoToMessagesLookup(message));
    }

    @Override
    public void goToMessagesLookupFWD(int accountId, int peerId, int messageId) {
        PlaceFactory.getMessagesLookupPlace(accountId, peerId, messageId, null).tryOpenWith(requireActivity());
    }

    @Override
    public void goWallReplyOpen(int accountId, @NonNull WallReply reply) {
        PlaceFactory.getCommentsPlace(accountId, new Commented(reply.getPostId(), reply.getOwnerId(), CommentedType.POST, null), reply.getId())
                .tryOpenWith(requireActivity());
    }

    @Override
    public void onDocPreviewOpen(@NonNull Document document) {
        callPresenter(p -> p.fireDocClick(document));
    }

    @Override
    public void onPostOpen(@NonNull Post post) {
        callPresenter(p -> p.firePostClick(post));
    }

    @Override
    public void onLinkOpen(@NonNull Link link) {
        callPresenter(p -> p.fireLinkClick(link));
    }

    @Override
    public void onUrlOpen(@NonNull String url) {
        callPresenter(p -> p.fireUrlClick(url));
    }

    @Override
    public void onFaveArticle(@NonNull Article article) {
        callPresenter(p -> p.fireFaveArticleClick(article));
    }

    @Override
    public void onShareArticle(@NonNull Article article) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), Settings.get().accounts().getCurrent(), article);
    }

    @Override
    public void onWikiPageOpen(@NonNull WikiPage page) {
        callPresenter(p -> p.fireWikiPageClick(page));
    }

    @Override
    public void onStoryOpen(@NonNull Story story) {
        callPresenter(p -> p.fireStoryClick(story));
    }

    @Override
    public void onUrlPhotoOpen(@NonNull String url, @NonNull String prefix, @NonNull String photo_prefix) {
        PlaceFactory.getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(requireActivity());
    }

    @Override
    public void openStory(int accountId, @NonNull Story story) {
        PlaceFactory.getHistoryVideoPreviewPlace(accountId, new ArrayList<>(Collections.singleton(story)), 0).tryOpenWith(requireActivity());
    }

    @Override
    public void onAudioPlaylistOpen(@NonNull AudioPlaylist playlist) {
        callPresenter(p -> p.fireAudioPlaylistClick(playlist));
    }

    @Override
    public void onWallReplyOpen(@NonNull WallReply reply) {
        callPresenter(p -> p.fireWallReplyOpen(reply));
    }

    @Override
    public void openAudioPlaylist(int accountId, @NonNull AudioPlaylist playlist) {
        PlaceFactory.getAudiosInAlbumPlace(accountId, playlist.getOwnerId(), playlist.getId(), playlist.getAccess_key()).tryOpenWith(requireActivity());
    }

    @Override
    public void onPhotosOpen(@NonNull ArrayList<Photo> photos, int index, boolean refresh) {
        callPresenter(p -> p.firePhotoClick(photos, index, refresh));
    }

    @Override
    public void openPhotoAlbum(int accountId, @NonNull PhotoAlbum album) {
        PlaceFactory.getVKPhotosAlbumPlace(accountId, album.getOwnerId(), album.getId(), null).tryOpenWith(requireActivity());
    }

    @Override
    public void onPhotoAlbumOpen(@NonNull PhotoAlbum album) {
        callPresenter(p -> p.firePhotoAlbumClick(album));
    }

    @Override
    public void onMarketAlbumOpen(@NonNull MarketAlbum market_album) {
        callPresenter(p -> p.fireMarketAlbumClick(market_album));
    }

    @Override
    public void onMarketOpen(@NonNull Market market) {
        callPresenter(p -> p.fireMarketClick(market));
    }

    @Override
    public void onArtistOpen(@NonNull AudioArtist artist) {
        callPresenter(p -> p.fireArtistClick(artist));
    }

    @Override
    public void openLink(int accountId, @NonNull Link link) {
        LinkHelper.openLinkInBrowser(requireActivity(), link.getUrl());
    }

    @Override
    public void openUrl(int accountId, @NonNull String url) {
        PlaceFactory.getExternalLinkPlace(accountId, url)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openWikiPage(int accountId, @NonNull WikiPage page) {
        PlaceFactory.getExternalLinkPlace(accountId, page.getViewUrl())
                .tryOpenWith(requireActivity());
    }

    @Override
    public void toMarketAlbumOpen(int accountId, @NonNull MarketAlbum market_album) {
        PlaceFactory.getMarketPlace(accountId, market_album.getOwner_id(), market_album.getId()).tryOpenWith(requireActivity());
    }

    @Override
    public void toArtistOpen(int accountId, @NonNull AudioArtist artist) {
        PlaceFactory.getArtistPlace(accountId, artist.getId(), false).tryOpenWith(requireActivity());
    }

    @Override
    public void toMarketOpen(int accountId, @NonNull Market market) {
        PlaceFactory.getMarketViewPlace(accountId, market).tryOpenWith(requireActivity());
    }

    @Override
    public void openSimplePhotoGallery(int accountId, @NonNull ArrayList<Photo> photos, int index, boolean needUpdate) {
        PlaceFactory.getSimpleGalleryPlace(accountId, photos, index, needUpdate).tryOpenWith(requireActivity());
    }

    @Override
    public void openPost(int accountId, @NonNull Post post) {
        PlaceFactory.getPostPreviewPlace(accountId, post.getVkid(), post.getOwnerId(), post).tryOpenWith(requireActivity());
    }

    @Override
    public void openDocPreview(int accountId, @NonNull Document document) {
        PlaceFactory.getDocPreviewPlace(accountId, document).tryOpenWith(requireActivity());
    }

    @Override
    public void openOwnerWall(int accountId, int ownerId) {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, null).tryOpenWith(requireActivity());
    }

    @Override
    public void openForwardMessages(int accountId, @NonNull ArrayList<Message> messages) {
        PlaceFactory.getForwardMessagesPlace(accountId, messages).tryOpenWith(requireActivity());
    }

    @Override
    public void playAudioList(int accountId, int position, @NonNull ArrayList<Audio> apiAudio) {
        MusicPlaybackService.startForPlayList(requireActivity(), apiAudio, position, false);
        if (!Settings.get().other().isShow_mini_player())
            PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
    }

    @Override
    public void openVideo(int accountId, @NonNull Video apiVideo) {
        PlaceFactory.getVideoPreviewPlace(accountId, apiVideo).tryOpenWith(requireActivity());
    }

    @Override
    public void openHistoryVideo(int accountId, @NonNull ArrayList<Story> stories, int index) {
        PlaceFactory.getHistoryVideoPreviewPlace(accountId, stories, index).tryOpenWith(requireActivity());
    }

    @Override
    public void openPoll(int accountId, @NonNull Poll poll) {
        PlaceFactory.getPollPlace(accountId, poll)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openComments(int accountId, Commented commented, Integer focusToCommentId) {
        PlaceFactory.getCommentsPlace(accountId, commented, focusToCommentId)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openSearch(int accountId, @SearchContentType int type, @Nullable BaseSearchCriteria criteria) {
        PlaceFactory.getSingleTabSearchPlace(accountId, type, criteria).tryOpenWith(requireActivity());
    }

    @Override
    public void goToLikes(int accountId, String type, int ownerId, int id) {
        PlaceFactory.getLikesCopiesPlace(accountId, type, ownerId, id, ILikesInteractor.FILTER_LIKES)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void goToReposts(int accountId, String type, int ownerId, int id) {
        PlaceFactory.getLikesCopiesPlace(accountId, type, ownerId, id, ILikesInteractor.FILTER_COPIES)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void repostPost(int accountId, @NonNull Post post) {
        PostShareDialog dialog = PostShareDialog.newInstance(accountId, post);
        getParentFragmentManager().setFragmentResultListener(PostShareDialog.REQUEST_POST_SHARE, dialog, (requestKey, result) -> {
            int method = PostShareDialog.extractMethod(result);
            int accountId1 = PostShareDialog.extractAccountId(result);
            Post post1 = PostShareDialog.extractPost(result);

            AssertUtils.requireNonNull(post1);

            switch (method) {
                case PostShareDialog.Methods.SHARE_LINK:
                    Utils.shareLink(requireActivity(), post1.generateVkPostLink(), post1.getText());
                    break;
                case PostShareDialog.Methods.REPOST_YOURSELF:
                    PlaceFactory.getRepostPlace(accountId1, null, post1).tryOpenWith(requireActivity());
                    break;
                case PostShareDialog.Methods.SEND_MESSAGE:
                    SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId1, post1);
                    break;
                case PostShareDialog.Methods.REPOST_GROUP:
                    int ownerId = PostShareDialog.extractOwnerId(result);
                    PlaceFactory.getRepostPlace(accountId1, Math.abs(ownerId), post1).tryOpenWith(requireActivity());
                    break;
            }
        });
        dialog.show(getParentFragmentManager(), "post-sharing");
    }

    @Override
    public void onRequestWritePermissions() {
        requestWritePermission.launch();
    }
}
