package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IWallView : IAttachmentsPlacesView, IAccountDependencyView, IMvpView, ISnackbarView,
    IErrorView, IToastView {
    fun displayWallData(data: MutableList<Post>)
    fun notifyWallDataSetChanged()
    fun updateStory(stories: MutableList<Story>?)
    fun notifyWallItemChanged(position: Int)
    fun notifyWallDataAdded(position: Int, count: Int)
    fun setupLoadMoreFooter(@LoadMoreState state: Int)
    fun showRefreshing(refreshing: Boolean)
    fun openPhotoAlbums(accountId: Int, ownerId: Int, owner: Owner?)
    fun openAudios(accountId: Int, ownerId: Int, owner: Owner?)
    fun openArticles(accountId: Int, ownerId: Int, owner: Owner?)
    fun openVideosLibrary(accountId: Int, ownerId: Int, owner: Owner?)
    fun goToPostCreation(accountId: Int, ownerId: Int, @EditingPostType postType: Int)
    fun copyToClipboard(label: String?, body: String?)
    fun openPhotoAlbum(
        accountId: Int,
        ownerId: Int,
        albumId: Int,
        photos: ArrayList<Photo>,
        position: Int
    )

    fun goToWallSearch(accountId: Int, ownerId: Int)
    fun openPostEditor(accountId: Int, post: Post)
    fun notifyWallItemRemoved(index: Int)
    fun goToConversationAttachments(accountId: Int, ownerId: Int)
    fun goNarratives(accountId: Int, ownerId: Int)
    interface IOptionView {
        fun setIsMy(my: Boolean)
        fun setIsBlacklistedByMe(blocked: Boolean)
        fun setIsFavorite(favorite: Boolean)
        fun setIsSubscribed(subscribed: Boolean)
    }
}