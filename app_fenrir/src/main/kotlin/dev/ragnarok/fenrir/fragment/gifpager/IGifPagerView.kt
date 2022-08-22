package dev.ragnarok.fenrir.fragment.gifpager

import androidx.annotation.StringRes
import dev.ragnarok.fenrir.fragment.absdocumentpreview.IBasicDocumentView
import dev.ragnarok.fenrir.media.gif.IGifPlayer

interface IGifPagerView : IBasicDocumentView {
    fun displayData(pageCount: Int, selectedIndex: Int)
    fun setAspectRatioAt(position: Int, w: Int, h: Int)
    fun setPreparingProgressVisible(position: Int, preparing: Boolean)
    fun setupAddRemoveButton(addEnable: Boolean)
    fun attachDisplayToPlayer(adapterPosition: Int, gifPlayer: IGifPlayer?)
    fun toolbarTitle(@StringRes titleRes: Int, vararg params: Any?)
    fun toolbarSubtitle(@StringRes titleRes: Int, vararg params: Any?)
    fun configHolder(adapterPosition: Int, progress: Boolean, aspectRatioW: Int, aspectRatioH: Int)
}