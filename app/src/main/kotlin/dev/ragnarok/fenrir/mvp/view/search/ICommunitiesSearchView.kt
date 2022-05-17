package dev.ragnarok.fenrir.mvp.view.search

import dev.ragnarok.fenrir.model.Community

interface ICommunitiesSearchView : IBaseSearchView<Community> {
    fun openCommunityWall(accountId: Int, community: Community)
}