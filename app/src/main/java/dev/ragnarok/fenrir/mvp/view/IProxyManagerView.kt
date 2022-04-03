package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.ProxyConfig
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface IProxyManagerView : IMvpView, IErrorView {
    fun displayData(configs: MutableList<ProxyConfig>, active: ProxyConfig?)
    fun notifyItemAdded(position: Int)
    fun notifyItemRemoved(position: Int)
    fun setActiveAndNotifyDataSetChanged(config: ProxyConfig?)
    fun goToAddingScreen()
}