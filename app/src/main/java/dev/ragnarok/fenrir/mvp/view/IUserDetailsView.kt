package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.model.menu.AdvancedItem
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IUserDetailsView : IMvpView, IAccountDependencyView, IErrorView {
    fun displayData(items: List<AdvancedItem>)
    fun displayToolbarTitle(user: User?)
    fun openOwnerProfile(accountId: Int, ownerId: Int, owner: Owner?)
    fun onPhotosLoaded(photo: Photo)
    fun openPhotoAlbum(
        accountId: Int,
        ownerId: Int,
        albumId: Int,
        photos: ArrayList<Photo>,
        position: Int
    )

    fun openChatWith(accountId: Int, messagesOwnerId: Int, peer: Peer)
    fun openPhotoUser(user: User)
}