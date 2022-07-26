package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.IdOption
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ICommunityBanEditView : IMvpView, IAccountDependencyView, IErrorView, IProgressView,
    IToastView {
    fun displayUserInfo(user: Owner)
    fun displayBanStatus(adminId: Int, adminName: String?, endDate: Long)
    fun displayBlockFor(blockFor: String?)
    fun displayReason(reason: String?)
    fun diplayComment(comment: String?)
    fun setShowCommentChecked(checked: Boolean)
    fun goBack()
    fun displaySelectOptionDialog(requestCode: Int, options: List<IdOption>)
    fun openProfile(accountId: Int, owner: Owner)
}