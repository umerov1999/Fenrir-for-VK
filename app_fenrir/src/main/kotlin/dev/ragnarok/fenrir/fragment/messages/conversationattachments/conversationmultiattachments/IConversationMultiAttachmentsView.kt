package dev.ragnarok.fenrir.fragment.messages.conversationattachments.conversationmultiattachments

import dev.ragnarok.fenrir.fragment.base.IAttachmentsPlacesView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.PhotoAlbum
import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.model.TmpSource
import dev.ragnarok.fenrir.model.Video

interface IConversationMultiAttachmentsView : IMvpView, IErrorView, IToastView,
    IAttachmentsPlacesView {
    fun notifyDataRemoved(@AttachmentConversationType type: Int, position: Int, count: Int)
    fun notifyDataChanged(@AttachmentConversationType type: Int, position: Int, count: Int)
    fun notifyDataAdded(@AttachmentConversationType type: Int, position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun toolbarTitle(title: String)
    fun toolbarSubtitle(subtitle: String)
    fun onSetLoadingStatus(isLoad: Int)
    fun openPostEditor(accountId: Long, post: Post)

    fun goToTempPhotosGallery(accountId: Long, source: TmpSource, index: Int)
    fun goToTempPhotosGallery(accountId: Long, ptr: Long, index: Int)

    fun resolveEmptyText(@AttachmentConversationType type: Int)

    fun displayAudioData(posts: MutableList<Audio>)
    fun displayDocsData(docs: MutableList<Document>)
    fun displayLinksData(links: MutableList<Link>)
    fun displayPhotoAlbumsData(photoAlbums: MutableList<PhotoAlbum>)
    fun displayPhotoData(photos: MutableList<Photo>)
    fun displayVideoData(videos: MutableList<Video>)
    fun displayPostsData(posts: MutableList<Post>)

    fun onOpenMenu(accountId: Long, position: Int, audio: Audio)
    fun onAudioLyricsReceived(lyricText: String, audio: Audio)
    fun onDownloadAudioNextStage()
    fun onStartAudioDownload(accountId: Long, position: Int, audio: Audio)
    fun onShowBitrate(duration: Int, size: Long)
    fun onSelectToSendAttachment(audios: ArrayList<Audio>)
    fun onServiceBindEvent(@PlayerStatus status: Int)
    fun notifyAudioItemRemoved(index: Int)
    fun notifyAudioItemChanged(index: Int)
}
