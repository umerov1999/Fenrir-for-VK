package dev.ragnarok.fenrir.fragment.audio.catalog_v1.audiocatalog

import dev.ragnarok.fenrir.fragment.base.IAccountDependencyView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView
import dev.ragnarok.fenrir.model.AudioCatalog

interface IAudioCatalogView : IAccountDependencyView, IMvpView, IErrorView, IToastView {
    fun displayData(pages: List<AudioCatalog>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
}