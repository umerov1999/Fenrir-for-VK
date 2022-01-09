package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.menu.AdvancedItem;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IUserDetailsView extends IMvpView, IAccountDependencyView, IErrorView {
    void displayData(@NonNull List<AdvancedItem> items);

    void displayToolbarTitle(User user);

    void openOwnerProfile(int accountId, int ownerId, @Nullable Owner owner);

    void onPhotosLoaded(Photo photo);

    void openPhotoAlbum(int accountId, int ownerId, int albumId, ArrayList<Photo> photos, int position);

    void openChatWith(int accountId, int messagesOwnerId, @NonNull Peer peer);

    void openPhotoUser(User user);
}