package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Owner

interface IFollowersView : ISimpleOwnersView {
    fun notifyRemoved(position: Int)
    fun showModFollowers(add: List<Owner>?, remove: List<Owner>?, accountId: Int)
}