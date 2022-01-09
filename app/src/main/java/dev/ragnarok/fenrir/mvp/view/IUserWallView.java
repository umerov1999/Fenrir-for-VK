package dev.ragnarok.fenrir.mvp.view;

import android.net.Uri;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.model.FriendsCounters;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.PostFilter;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserDetails;

public interface IUserWallView extends IWallView, IProgressView {

    void displayWallFilters(List<PostFilter> filters);

    void notifyWallFiltersChanged();

    void setupPrimaryActionButton(@DrawableRes Integer resourceId);

    void openFriends(int accountId, int userId, int tab, FriendsCounters counters);

    void openGroups(int accountId, int userId, @Nullable User user);

    void openProducts(int accountId, int ownerId, @Nullable Owner owner);

    void openGifts(int accountId, int ownerId, @Nullable Owner owner);

    void showEditStatusDialog(String initialValue);

    void showAddToFriendsMessageDialog();

    void showDeleteFromFriendsMessageDialog();

    void showUnbanMessageDialog();

    void showAvatarContextMenu(boolean canUploadAvatar);

    void showMention(int accountId, int ownerId);

    void displayCounters(int friends, int mutual, int followers, int groups, int photos, int audios, int videos, int articles, int products, int gifts);

    void displayUserStatus(String statusText, boolean swAudioIcon);

    void InvalidateOptionsMenu();

    void displayBaseUserInfo(User user);

    void openUserDetails(int accountId, @NonNull User user, @NonNull UserDetails details);

    void showAvatarUploadedMessage(int accountId, Post post);

    void doEditPhoto(@NonNull Uri uri);
}
