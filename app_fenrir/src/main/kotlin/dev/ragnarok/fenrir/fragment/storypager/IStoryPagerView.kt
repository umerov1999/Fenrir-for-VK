package dev.ragnarok.fenrir.fragment.storypager

import androidx.annotation.StringRes
import dev.ragnarok.fenrir.fragment.base.IAccountDependencyView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView
import dev.ragnarok.fenrir.media.gif.IGifPlayer
import dev.ragnarok.fenrir.model.Story

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