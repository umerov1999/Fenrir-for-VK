package dev.ragnarok.fenrir.mvp.view.search;

import dev.ragnarok.fenrir.model.User;


public interface IPeopleSearchView extends IBaseSearchView<User> {
    void openUserWall(int accountId, User user);
}