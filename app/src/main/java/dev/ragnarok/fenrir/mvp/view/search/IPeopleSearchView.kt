package dev.ragnarok.fenrir.mvp.view.search

import dev.ragnarok.fenrir.model.User

interface IPeopleSearchView : IBaseSearchView<User> {
    fun openUserWall(accountId: Int, user: User)
}