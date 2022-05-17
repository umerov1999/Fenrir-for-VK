package dev.ragnarok.fenrir.mvp.view

import androidx.annotation.StringRes
import dev.ragnarok.fenrir.media.gif.IGifPlayer
import dev.ragnarok.fenrir.model.Story
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IStoryPagerView : IMvpView, IErrorView, IToastView, IAccountDependencyView {
    fun displayData(pageCount: Int, selectedIndex: Int)
    fun setAspectRatioAt(position: Int, w: Int, h: Int)
    fun setPreparingProgressVisible(position: Int, preparing: Boolean)
    fun attachDisplayToPlayer(adapterPosition: Int, gifPlayer: IGifPlayer?)
    fun setToolbarTitle(@StringRes titleRes: Int, vararg params: Any?)
    fun setToolbarSubtitle(story: Story, account_id: Int)
    fun onShare(story: Story, account_id: Int)
    fun configHolder(adapterPosition: Int, progress: Boolean, aspectRatioW: Int, aspectRatioH: Int)
    fun onNext()
    fun requestWriteExternalStoragePermission()
}