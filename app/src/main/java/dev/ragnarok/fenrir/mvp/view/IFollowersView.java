package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.model.Owner;


public interface IFollowersView extends ISimpleOwnersView {
    void notifyRemoved(int position);

    void showModFollowers(@Nullable List<Owner> add, @Nullable List<Owner> remove, int accountId);
}
