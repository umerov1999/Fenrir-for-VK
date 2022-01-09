package dev.ragnarok.fenrir.mvp.presenter.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import dev.ragnarok.fenrir.domain.ILikesInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;
import dev.ragnarok.fenrir.util.RxUtils;

public abstract class PlaceSupportPresenter<V extends IMvpView & IAttachmentsPlacesView & IAccountDependencyView>
        extends AccountDependencyPresenter<V> {

    public PlaceSupportPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
    }

    public void fireLinkClick(@NonNull Link link) {
        callView(v -> v.openLink(getAccountId(), link));
    }

    public void fireUrlClick(@NonNull String url) {
        callView(v -> v.openUrl(getAccountId(), url));
    }

    public void fireWikiPageClick(@NonNull WikiPage page) {
        callView(v -> v.openWikiPage(getAccountId(), page));
    }

    public void fireStoryClick(@NonNull Story story) {
        callView(v -> v.openStory(getAccountId(), story));
    }

    public void firePhotoClick(@NonNull ArrayList<Photo> photos, int index, boolean refresh) {
        callView(v -> v.openSimplePhotoGallery(getAccountId(), photos, index, refresh));
    }

    public void firePostClick(@NonNull Post post) {
        callView(v -> v.openPost(getAccountId(), post));
    }

    public void fireDocClick(@NonNull Document document) {
        callView(v -> v.openDocPreview(getAccountId(), document));
    }

    public void fireOwnerClick(int ownerId) {
        callView(v -> v.openOwnerWall(getAccountId(), ownerId));
    }

    public void fireGoToMessagesLookup(@NonNull Message message) {
        callView(v -> v.goToMessagesLookupFWD(getAccountId(), message.getPeerId(), message.getOriginalId()));
    }

    public void fireGoToMessagesLookup(int peerId, int msgId) {
        callView(v -> v.goToMessagesLookupFWD(getAccountId(), peerId, msgId));
    }

    public void fireForwardMessagesClick(@NonNull ArrayList<Message> messages) {
        callView(v -> v.openForwardMessages(getAccountId(), messages));
    }

    public void fireAudioPlayClick(int position, @NonNull ArrayList<Audio> apiAudio) {
        callView(v -> v.playAudioList(getAccountId(), position, apiAudio));
    }

    public void fireVideoClick(@NonNull Video apiVideo) {
        callView(v -> v.openVideo(getAccountId(), apiVideo));
    }

    public void fireAudioPlaylistClick(@NonNull AudioPlaylist playlist) {
        callView(v -> v.openAudioPlaylist(getAccountId(), playlist));
    }

    public void fireWallReplyOpen(@NonNull WallReply reply) {
        callView(v -> v.goWallReplyOpen(getAccountId(), reply));
    }

    public void firePollClick(@NonNull Poll poll) {
        callView(v -> v.openPoll(getAccountId(), poll));
    }

    public void fireHashtagClick(String hashTag) {
        callView(v -> v.openSearch(getAccountId(), SearchContentType.NEWS, new NewsFeedCriteria(hashTag)));
    }

    public void fireShareClick(Post post) {
        callView(v -> v.repostPost(getAccountId(), post));
    }

    public void fireCommentsClick(Post post) {
        callView(v -> v.openComments(getAccountId(), Commented.from(post), null));
    }

    public void firePhotoAlbumClick(@NonNull PhotoAlbum album) {
        callView(v -> v.openPhotoAlbum(getAccountId(), album));
    }

    public void fireMarketAlbumClick(@NonNull MarketAlbum market_album) {
        callView(v -> v.toMarketAlbumOpen(getAccountId(), market_album));
    }

    public void fireMarketClick(@NonNull Market market) {
        callView(v -> v.toMarketOpen(getAccountId(), market));
    }

    public void fireArtistClick(@NonNull AudioArtist artist) {
        callView(v -> v.toArtistOpen(getAccountId(), artist));
    }

    public void fireFaveArticleClick(@NonNull Article article) {
        if (!article.getIsFavorite()) {
            appendDisposable(InteractorFactory.createFaveInteractor().addArticle(getAccountId(), article.getURL())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(RxUtils.dummy(), t -> {
                    }));
        } else {
            appendDisposable(InteractorFactory.createFaveInteractor().removeArticle(getAccountId(), article.getOwnerId(), article.getId())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(i -> {
                    }, t -> {
                    }));
        }
    }

    public final void fireCopiesLikesClick(String type, int ownerId, int itemId, String filter) {
        if (ILikesInteractor.FILTER_LIKES.equals(filter)) {
            callView(v -> v.goToLikes(getAccountId(), type, ownerId, itemId));
        } else if (ILikesInteractor.FILTER_COPIES.equals(filter)) {
            callView(v -> v.goToReposts(getAccountId(), type, ownerId, itemId));
        }
    }
}