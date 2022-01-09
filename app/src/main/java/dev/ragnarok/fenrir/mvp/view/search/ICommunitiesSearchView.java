package dev.ragnarok.fenrir.mvp.view.search;

import dev.ragnarok.fenrir.model.Community;


public interface ICommunitiesSearchView extends IBaseSearchView<Community> {
    void openCommunityWall(int accountId, Community community);
}