package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IWallView extends IAttachmentsPlacesView,
        IAccountDependencyView, IMvpView, ISnackbarView, IErrorView, IToastView {

    void displayWallData(List<Post> data);

    void notifyWallDataSetChanged();

    void updateStory(List<Story> stories);

    void notifyWallItemChanged(int position);

    void notifyWallDataAdded(int position, int count);

    void setupLoadMoreFooter(@LoadMoreState int state);

    void showRefreshing(boolean refreshing);

    void openPhotoAlbums(int accountId, int ownerId, @Nullable Owner owner);

    void openAudios(int accountId, int ownerId, @Nullable Owner owner);

    void openArticles(int accountId, int ownerId, @Nullable Owner owner);

    void openVideosLibrary(int accountId, int ownerId, @Nullable Owner owner);

    void goToPostCreation(int accountId, int ownerId, @EditingPostType int postType);

    void copyToClipboard(String label, String body);

    void openPhotoAlbum(int accountId, int ownerId, int albumId, ArrayList<Photo> photos, int position);

    void goToWallSearch(int accountId, int ownerId);

    void openPostEditor(int accountId, Post post);

    void notifyWallItemRemoved(int index);

    void goToConversationAttachments(int accountId, int ownerId);

    interface IOptionView {
        void setIsMy(boolean my);

        void setIsBlacklistedByMe(boolean blocked);

        void setIsFavorite(boolean favorite);

        void setIsSubscribed(boolean subscribed);
    }
}