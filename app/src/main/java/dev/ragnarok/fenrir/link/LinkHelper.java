package dev.ragnarok.fenrir.link;

import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.singletonArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.fave.FaveTabsFragment;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria;
import dev.ragnarok.fenrir.link.types.AbsLink;
import dev.ragnarok.fenrir.link.types.ArtistsLink;
import dev.ragnarok.fenrir.link.types.AudioPlaylistLink;
import dev.ragnarok.fenrir.link.types.AudioTrackLink;
import dev.ragnarok.fenrir.link.types.AudiosLink;
import dev.ragnarok.fenrir.link.types.BoardLink;
import dev.ragnarok.fenrir.link.types.DialogLink;
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
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackService;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class LinkHelper {
    public static void openUrl(Activity context, int accountId, String link) {
        openUrl(context, accountId, link, false);
    }

    public static void openUrl(Activity context, int accountId, String link, boolean isMain) {
        if (link == null || link.length() <= 0) {
            CustomToast.CreateCustomToast(context).showToastError(R.string.empty_clipboard_url);
            return;
        }
        if (link.contains("vk.cc")) {
            InteractorFactory.createUtilsInteractor().checkLink(accountId, link)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> {
                        if ("banned".equals(t.status)) {
                            Utils.showRedTopToast(context, R.string.link_banned);
                        } else {
                            if (!openVKlink(context, accountId, t.link, isMain)) {
                                if (Settings.get().main().isOpenUrlInternal() > 0) {
                                    openLinkInBrowser(context, t.link);
                                } else {
                                    PlaceFactory.getExternalLinkPlace(accountId, t.link).tryOpenWith(context);
                                }
                            }
                        }
                    }, e -> Utils.showErrorInAdapter(context, e));
        } else if (link.contains("vk.me")) {
            InteractorFactory.createUtilsInteractor().joinChatByInviteLink(accountId, link)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> PlaceFactory.getChatPlace(accountId, accountId, new Peer(Peer.fromChatId(t.chat_id))).tryOpenWith(context), e -> Utils.showErrorInAdapter(context, e));
        } else {
            if (!openVKlink(context, accountId, link, isMain)) {
                if (Settings.get().main().isOpenUrlInternal() > 0) {
                    openLinkInBrowser(context, link);
                } else {
                    PlaceFactory.getExternalLinkPlace(accountId, link).tryOpenWith(context);
                }
            }
        }
    }

    public static boolean openVKLink(Activity activity, int accountId, AbsLink link, boolean isMain) {
        switch (link.type) {

            case AbsLink.PLAYLIST:
                AudioPlaylistLink plLink = (AudioPlaylistLink) link;
                PlaceFactory.getAudiosInAlbumPlace(accountId, plLink.ownerId, plLink.playlistId, plLink.access_key).tryOpenWith(activity);
                break;

            case AbsLink.POLL:
                PollLink pollLink = (PollLink) link;
                openLinkInBrowser(activity, "https://vk.com/poll" + pollLink.ownerId + "_" + pollLink.Id);
                break;

            case AbsLink.WALL_COMMENT_THREAD:
                WallCommentThreadLink wallCommentThreadLink = (WallCommentThreadLink) link;
                Commented commentedThread = new Commented(wallCommentThreadLink.getPostId(), wallCommentThreadLink.getOwnerId(), CommentedType.POST, null);
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.info)
                        .setMessage(R.string.open_branch)
                        .setPositiveButton(R.string.button_yes, (dialog, which) -> PlaceFactory.getCommentsThreadPlace(accountId, commentedThread, wallCommentThreadLink.getCommentId(), wallCommentThreadLink.getThreadId()).tryOpenWith(activity))
                        .setNegativeButton(R.string.button_no, (dialog, which) -> PlaceFactory.getCommentsPlace(accountId, commentedThread, wallCommentThreadLink.getThreadId()).tryOpenWith(activity))
                        .show();
                break;

            case AbsLink.WALL_COMMENT:
                WallCommentLink wallCommentLink = (WallCommentLink) link;

                Commented commented = new Commented(wallCommentLink.getPostId(), wallCommentLink.getOwnerId(), CommentedType.POST, null);
                PlaceFactory.getCommentsPlace(accountId, commented, wallCommentLink.getCommentId()).tryOpenWith(activity);
                break;

            case AbsLink.DIALOGS:
                PlaceFactory.getDialogsPlace(accountId, accountId, null).tryOpenWith(activity);
                break;

            case AbsLink.PHOTO:
                PhotoLink photoLink = (PhotoLink) link;

                Photo photo = new Photo()
                        .setId(photoLink.id)
                        .setOwnerId(photoLink.ownerId)
                        .setAccessKey(photoLink.access_key);

                PlaceFactory.getSimpleGalleryPlace(accountId, singletonArrayList(photo), 0, true).setNeedFinishMain(isMain).tryOpenWith(activity);
                break;

            case AbsLink.PHOTO_ALBUM:
                PhotoAlbumLink photoAlbumLink = (PhotoAlbumLink) link;
                PlaceFactory.getVKPhotosAlbumPlace(accountId, photoAlbumLink.ownerId,
                        photoAlbumLink.albumId, null).tryOpenWith(activity);
                break;

            case AbsLink.PROFILE:
            case AbsLink.GROUP:
                OwnerLink ownerLink = (OwnerLink) link;
                PlaceFactory.getOwnerWallPlace(accountId, ownerLink.ownerId, null).tryOpenWith(activity);
                break;

            case AbsLink.TOPIC:
                TopicLink topicLink = (TopicLink) link;
                PlaceFactory.getCommentsPlace(accountId, new Commented(topicLink.topicId, topicLink.ownerId,
                        CommentedType.TOPIC, null), null).tryOpenWith(activity);
                break;

            case AbsLink.WALL_POST:
                WallPostLink wallPostLink = (WallPostLink) link;
                PlaceFactory.getPostPreviewPlace(accountId, wallPostLink.postId, wallPostLink.ownerId)
                        .tryOpenWith(activity);
                break;

            case AbsLink.ALBUMS:
                PhotoAlbumsLink photoAlbumsLink = (PhotoAlbumsLink) link;
                PlaceFactory.getVKPhotoAlbumsPlace(accountId, photoAlbumsLink.ownerId, IVkPhotosView.ACTION_SHOW_PHOTOS, null).tryOpenWith(activity);
                break;

            case AbsLink.DIALOG:
                DialogLink dialogLink = (DialogLink) link;
                Peer peer = new Peer(dialogLink.peerId);
                PlaceFactory.getChatPlace(accountId, accountId, peer).setNeedFinishMain(isMain).tryOpenWith(activity);
                break;

            case AbsLink.WALL:
                WallLink wallLink = (WallLink) link;
                PlaceFactory.getOwnerWallPlace(accountId, wallLink.ownerId, null).tryOpenWith(activity);
                break;

            case AbsLink.VIDEO:
                VideoLink videoLink = (VideoLink) link;
                PlaceFactory.getVideoPreviewPlace(accountId, videoLink.ownerId, videoLink.videoId, videoLink.access_key, null)
                        .tryOpenWith(activity);
                break;

            case AbsLink.VIDEO_ALBUM:
                VideoAlbumLink videoAlbumLink = (VideoAlbumLink) link;
                PlaceFactory.getVideoAlbumPlace(accountId, videoAlbumLink.ownerId, videoAlbumLink.albumId, null, null)
                        .tryOpenWith(activity);
                break;

            case AbsLink.AUDIOS:
                AudiosLink audiosLink = (AudiosLink) link;
                PlaceFactory.getAudiosPlace(accountId, audiosLink.ownerId).tryOpenWith(activity);
                break;

            case AbsLink.DOMAIN:
                DomainLink domainLink = (DomainLink) link;
                PlaceFactory.getResolveDomainPlace(accountId, domainLink.fullLink, domainLink.domain)
                        .tryOpenWith(activity);
                break;

            case AbsLink.PAGE:
                PlaceFactory.getExternalLinkPlace(accountId, ((PageLink) link).getLink()).tryOpenWith(activity);
                break;

            case AbsLink.DOC:
                DocLink docLink = (DocLink) link;
                PlaceFactory.getDocPreviewPlace(accountId, docLink.docId, docLink.ownerId, docLink.access_key, null).setNeedFinishMain(isMain).tryOpenWith(activity);
                break;

            case AbsLink.FAVE:
                FaveLink faveLink = (FaveLink) link;
                int targetTab = FaveTabsFragment.getTabByLinkSection(faveLink.section);
                if (targetTab == FaveTabsFragment.TAB_UNKNOWN) {
                    return false;
                }

                PlaceFactory.getBookmarksPlace(accountId, targetTab).tryOpenWith(activity);
                break;

            case AbsLink.BOARD:
                BoardLink boardLink = (BoardLink) link;
                PlaceFactory.getTopicsPlace(accountId, -Math.abs(boardLink.getGroupId())).tryOpenWith(activity);
                break;

            case AbsLink.FEED_SEARCH:
                FeedSearchLink feedSearchLink = (FeedSearchLink) link;
                NewsFeedCriteria criteria = new NewsFeedCriteria(feedSearchLink.getQ());
                PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.NEWS, criteria).tryOpenWith(activity);
                break;

            case AbsLink.ARTISTS:
                ArtistsLink artistSearchLink = (ArtistsLink) link;
                PlaceFactory.getArtistPlace(accountId, artistSearchLink.Id, false).tryOpenWith(activity);
                break;

            case AbsLink.AUDIO_TRACK:
                AudioTrackLink audioLink = (AudioTrackLink) link;
                InteractorFactory.createAudioInteractor().getById(accountId, Collections.singletonList(new Audio().setId(audioLink.trackId).setOwnerId(audioLink.ownerId)))
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(t -> {
                            MusicPlaybackService.startForPlayList(activity, new ArrayList<>(t), 0, false);
                            PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(activity);
                        }, e -> Utils.showErrorInAdapter(activity, e));
                break;

            default:
                return false;
        }

        return true;
    }

    private static boolean openVKlink(Activity activity, int accountId, String url, boolean isMain) {
        AbsLink link = VkLinkParser.parse(url);
        return link != null && openVKLink(activity, accountId, link, isMain);
    }

    public static ArrayList<ResolveInfo> getCustomTabsPackages(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));

        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        ArrayList<ResolveInfo> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info);
            }
        }
        return packagesSupportingCustomTabs;
    }

    public static void openLinkInBrowser(Context context, String url) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setDefaultColorSchemeParams(new CustomTabColorSchemeParams.Builder()
                .setToolbarColor(CurrentTheme.getColorPrimary(context)).setSecondaryToolbarColor(CurrentTheme.getColorSecondary(context)).build());
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        getCustomTabsPackages(context);
        if (!getCustomTabsPackages(context).isEmpty()) {
            customTabsIntent.intent.setPackage(getCustomTabsPackages(context).get(0).resolvePackageName);
        }
        try {
            customTabsIntent.launchUrl(context, Uri.parse(url));
        } catch (Exception e) {
            e.printStackTrace();
            openLinkInBrowserInternal(context, Settings.get().accounts().getCurrent(), url);
        }
    }

    public static void openLinkInBrowserInternal(Context context, int accountId, String url) {
        if (isEmpty(url))
            return;
        PlaceFactory.getExternalLinkPlace(accountId, url).tryOpenWith(context);
    }

    public static Commented findCommentedFrom(String url) {
        AbsLink link = VkLinkParser.parse(url);
        Commented commented = null;
        if (link != null) {
            switch (link.type) {
                case AbsLink.WALL_POST:
                    WallPostLink wallPostLink = (WallPostLink) link;
                    commented = new Commented(wallPostLink.postId, wallPostLink.ownerId, CommentedType.POST, null);
                    break;
                case AbsLink.PHOTO:
                    PhotoLink photoLink = (PhotoLink) link;
                    commented = new Commented(photoLink.id, photoLink.ownerId, CommentedType.PHOTO, null);
                    break;
                case AbsLink.VIDEO:
                    VideoLink videoLink = (VideoLink) link;
                    commented = new Commented(videoLink.videoId, videoLink.ownerId, CommentedType.VIDEO, null);
                    break;
                case AbsLink.TOPIC:
                    TopicLink topicLink = (TopicLink) link;
                    commented = new Commented(topicLink.topicId, topicLink.ownerId, CommentedType.TOPIC, null);
                    break;
            }
        }

        return commented;
    }
}
