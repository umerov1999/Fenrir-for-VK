package dev.ragnarok.fenrir.link

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fragment.fave.FaveTabsFragment
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.link.types.*
import dev.ragnarok.fenrir.link.types.FaveLink
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView
import dev.ragnarok.fenrir.place.PlaceFactory.getArtistPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosInAlbumPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getBookmarksPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getChatPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getCommentsPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getCommentsThreadPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getDialogsPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getDocPreviewPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getExternalLinkPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getPostPreviewPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getResolveDomainPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSimpleGalleryPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleTabSearchPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getTopicsPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getVKPhotoAlbumsPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getVKPhotosAlbumPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getVideoAlbumPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getVideoPreviewPlace
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.Utils.showErrorInAdapter
import dev.ragnarok.fenrir.util.Utils.showRedTopToast
import dev.ragnarok.fenrir.util.Utils.singletonArrayList
import kotlin.math.abs

object LinkHelper {

    @JvmOverloads
    fun openUrl(context: Activity, accountId: Int, link: String?, isMain: Boolean = false) {
        if (link == null || link.isEmpty()) {
            CreateCustomToast(context).showToastError(R.string.empty_clipboard_url)
            return
        }
        if (link.contains("vk.cc")) {
            InteractorFactory.createUtilsInteractor().checkLink(accountId, link)
                .fromIOToMain()
                .subscribe({ t ->
                    if ("banned" == t.status) {
                        showRedTopToast(context, R.string.link_banned)
                    } else {
                        if (!openVKlink(context, accountId, t.link, isMain)) {
                            if (Settings.get().main().isOpenUrlInternal > 0) {
                                openLinkInBrowser(context, t.link)
                            } else {
                                getExternalLinkPlace(accountId, t.link).tryOpenWith(context)
                            }
                        }
                    }
                }) { e -> showErrorInAdapter(context, e) }
        } else if (link.contains("vk.me")) {
            InteractorFactory.createUtilsInteractor().joinChatByInviteLink(accountId, link)
                .fromIOToMain()
                .subscribe({ t ->
                    getChatPlace(
                        accountId,
                        accountId,
                        Peer(Peer.fromChatId(t.chat_id))
                    ).tryOpenWith(context)
                }) { e -> showErrorInAdapter(context, e) }
        } else {
            if (!openVKlink(context, accountId, link, isMain)) {
                if (Settings.get().main().isOpenUrlInternal > 0) {
                    openLinkInBrowser(context, link)
                } else {
                    getExternalLinkPlace(accountId, link).tryOpenWith(context)
                }
            }
        }
    }


    fun openVKLink(activity: Activity, accountId: Int, link: AbsLink, isMain: Boolean): Boolean {
        when (link.type) {
            AbsLink.PLAYLIST -> {
                val plLink = link as AudioPlaylistLink
                getAudiosInAlbumPlace(
                    accountId,
                    plLink.ownerId,
                    plLink.playlistId,
                    plLink.access_key
                ).tryOpenWith(activity)
            }
            AbsLink.POLL -> {
                val pollLink = link as PollLink
                openLinkInBrowser(
                    activity,
                    "https://vk.com/poll" + pollLink.ownerId + "_" + pollLink.Id
                )
            }
            AbsLink.WALL_COMMENT_THREAD -> {
                val wallCommentThreadLink = link as WallCommentThreadLink
                val commentedThread = Commented(
                    wallCommentThreadLink.postId,
                    wallCommentThreadLink.ownerId,
                    CommentedType.POST,
                    null
                )
                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.info)
                    .setMessage(R.string.open_branch)
                    .setPositiveButton(R.string.button_yes) { _: DialogInterface?, _: Int ->
                        getCommentsThreadPlace(
                            accountId,
                            commentedThread,
                            wallCommentThreadLink.commentId,
                            wallCommentThreadLink.threadId
                        ).tryOpenWith(activity)
                    }
                    .setNegativeButton(R.string.button_no) { _: DialogInterface?, _: Int ->
                        getCommentsPlace(
                            accountId,
                            commentedThread,
                            wallCommentThreadLink.threadId
                        ).tryOpenWith(activity)
                    }
                    .show()
            }
            AbsLink.WALL_COMMENT -> {
                val wallCommentLink = link as WallCommentLink
                val commented = Commented(
                    wallCommentLink.postId,
                    wallCommentLink.ownerId,
                    CommentedType.POST,
                    null
                )
                getCommentsPlace(accountId, commented, wallCommentLink.commentId).tryOpenWith(
                    activity
                )
            }
            AbsLink.DIALOGS -> getDialogsPlace(accountId, accountId, null).tryOpenWith(activity)
            AbsLink.PHOTO -> {
                val photoLink = link as PhotoLink
                val photo = Photo()
                    .setId(photoLink.id)
                    .setOwnerId(photoLink.ownerId)
                    .setAccessKey(photoLink.access_key)
                getSimpleGalleryPlace(
                    accountId,
                    singletonArrayList(photo),
                    0,
                    true
                ).setNeedFinishMain(isMain).tryOpenWith(activity)
            }
            AbsLink.PHOTO_ALBUM -> {
                val photoAlbumLink = link as PhotoAlbumLink
                getVKPhotosAlbumPlace(
                    accountId, photoAlbumLink.ownerId,
                    photoAlbumLink.albumId, null
                ).tryOpenWith(activity)
            }
            AbsLink.PROFILE, AbsLink.GROUP -> {
                val ownerLink = link as OwnerLink
                var ownId = ownerLink.ownerId
                if (ownId == 0) {
                    ownId = Settings.get().accounts().current
                }
                getOwnerWallPlace(accountId, ownId, null).tryOpenWith(activity)
            }
            AbsLink.TOPIC -> {
                val topicLink = link as TopicLink
                getCommentsPlace(
                    accountId, Commented(
                        topicLink.topicId, topicLink.ownerId,
                        CommentedType.TOPIC, null
                    ), null
                ).tryOpenWith(activity)
            }
            AbsLink.WALL_POST -> {
                val wallPostLink = link as WallPostLink
                getPostPreviewPlace(accountId, wallPostLink.postId, wallPostLink.ownerId)
                    .tryOpenWith(activity)
            }
            AbsLink.ALBUMS -> {
                val photoAlbumsLink = link as PhotoAlbumsLink
                getVKPhotoAlbumsPlace(
                    accountId,
                    photoAlbumsLink.ownerId,
                    IVkPhotosView.ACTION_SHOW_PHOTOS,
                    null
                ).tryOpenWith(activity)
            }
            AbsLink.DIALOG -> {
                val dialogLink = link as DialogLink
                val peer = Peer(dialogLink.peerId)
                getChatPlace(accountId, accountId, peer).setNeedFinishMain(isMain)
                    .tryOpenWith(activity)
            }
            AbsLink.WALL -> {
                val wallLink = link as WallLink
                getOwnerWallPlace(accountId, wallLink.ownerId, null).tryOpenWith(activity)
            }
            AbsLink.VIDEO -> {
                val videoLink = link as VideoLink
                getVideoPreviewPlace(
                    accountId,
                    videoLink.ownerId,
                    videoLink.videoId,
                    videoLink.access_key,
                    null
                )
                    .tryOpenWith(activity)
            }
            AbsLink.VIDEO_ALBUM -> {
                val videoAlbumLink = link as VideoAlbumLink
                getVideoAlbumPlace(
                    accountId,
                    videoAlbumLink.ownerId,
                    videoAlbumLink.albumId,
                    null,
                    null
                )
                    .tryOpenWith(activity)
            }
            AbsLink.AUDIOS -> {
                val audiosLink = link as AudiosLink
                getAudiosPlace(accountId, audiosLink.ownerId).tryOpenWith(activity)
            }
            AbsLink.DOMAIN -> {
                val domainLink = link as DomainLink
                getResolveDomainPlace(accountId, domainLink.fullLink, domainLink.domain)
                    .tryOpenWith(activity)
            }
            AbsLink.PAGE -> getExternalLinkPlace(accountId, (link as PageLink).link).tryOpenWith(
                activity
            )
            AbsLink.DOC -> {
                val docLink = link as DocLink
                getDocPreviewPlace(
                    accountId,
                    docLink.docId,
                    docLink.ownerId,
                    docLink.access_key,
                    null
                ).setNeedFinishMain(isMain).tryOpenWith(activity)
            }
            AbsLink.FAVE -> {
                val faveLink = link as FaveLink
                val targetTab = FaveTabsFragment.getTabByLinkSection(faveLink.section)
                if (targetTab == FaveTabsFragment.TAB_UNKNOWN) {
                    return false
                }
                getBookmarksPlace(accountId, targetTab).tryOpenWith(activity)
            }
            AbsLink.BOARD -> {
                val boardLink = link as BoardLink
                getTopicsPlace(accountId, -abs(boardLink.groupId)).tryOpenWith(activity)
            }
            AbsLink.FEED_SEARCH -> {
                val feedSearchLink = link as FeedSearchLink
                val criteria = NewsFeedCriteria(feedSearchLink.q)
                getSingleTabSearchPlace(accountId, SearchContentType.NEWS, criteria).tryOpenWith(
                    activity
                )
            }
            AbsLink.ARTISTS -> {
                val artistSearchLink = link as ArtistsLink
                getArtistPlace(accountId, artistSearchLink.Id, false).tryOpenWith(activity)
            }
            AbsLink.AUDIO_TRACK -> {
                val audioLink = link as AudioTrackLink
                InteractorFactory.createAudioInteractor().getById(
                    accountId,
                    listOf(Audio().setId(audioLink.trackId).setOwnerId(audioLink.ownerId))
                )
                    .fromIOToMain()
                    .subscribe({
                        startForPlayList(activity, ArrayList(it), 0, false)
                        getPlayerPlace(Settings.get().accounts().current).tryOpenWith(activity)
                    }) { e -> showErrorInAdapter(activity, e) }
            }
            else -> return false
        }
        return true
    }

    private fun openVKlink(
        activity: Activity,
        accountId: Int,
        url: String,
        isMain: Boolean
    ): Boolean {
        val link = VkLinkParser.parse(url)
        return link != null && openVKLink(activity, accountId, link, isMain)
    }

    private fun getCustomTabsPackages(context: Context): ArrayList<ResolveInfo> {
        val pm = context.packageManager
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs = ArrayList<ResolveInfo>()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info)
            }
        }
        return packagesSupportingCustomTabs
    }


    fun openLinkInBrowser(context: Context, url: String?) {
        val intentBuilder = CustomTabsIntent.Builder()
        intentBuilder.setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                .setToolbarColor(CurrentTheme.getColorPrimary(context))
                .setSecondaryToolbarColor(CurrentTheme.getColorSecondary(context)).build()
        )
        val customTabsIntent = intentBuilder.build()
        getCustomTabsPackages(context)
        if (getCustomTabsPackages(context).isNotEmpty()) {
            customTabsIntent.intent.setPackage(getCustomTabsPackages(context)[0].resolvePackageName)
        }
        try {
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            e.printStackTrace()
            openLinkInBrowserInternal(context, Settings.get().accounts().current, url)
        }
    }

    private fun openLinkInBrowserInternal(context: Context, accountId: Int, url: String?) {
        if (url.isNullOrEmpty()) return
        getExternalLinkPlace(accountId, url).tryOpenWith(context)
    }

    @JvmStatic
    fun findCommentedFrom(url: String): Commented? {
        val link = VkLinkParser.parse(url)
        var commented: Commented? = null
        if (link != null) {
            when (link.type) {
                AbsLink.WALL_POST -> {
                    val wallPostLink = link as WallPostLink
                    commented = Commented(
                        wallPostLink.postId,
                        wallPostLink.ownerId,
                        CommentedType.POST,
                        null
                    )
                }
                AbsLink.PHOTO -> {
                    val photoLink = link as PhotoLink
                    commented =
                        Commented(photoLink.id, photoLink.ownerId, CommentedType.PHOTO, null)
                }
                AbsLink.VIDEO -> {
                    val videoLink = link as VideoLink
                    commented =
                        Commented(videoLink.videoId, videoLink.ownerId, CommentedType.VIDEO, null)
                }
                AbsLink.TOPIC -> {
                    val topicLink = link as TopicLink
                    commented =
                        Commented(topicLink.topicId, topicLink.ownerId, CommentedType.TOPIC, null)
                }
            }
        }
        return commented
    }
}