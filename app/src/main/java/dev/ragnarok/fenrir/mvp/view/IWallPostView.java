package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IWallPostView extends IAttachmentsPlacesView, IAccountDependencyView, IMvpView, IErrorView {

    int SUBTITLE_NORMAL = 1;
    int SUBTITLE_STATUS_UPDATE = 2;
    int SUBTITLE_PHOTO_UPDATE = 3;

    void displayDefaultToolbaTitle();

    void displayToolbarTitle(String title);

    void displayToolbatSubtitle(int subtitleType, long datetime);

    void displayPostInfo(Post post);

    void displayLoading();

    void displayLoadingFail();

    void displayLikes(int count, boolean userLikes);

    void setCommentButtonVisible(boolean visible);

    void displayCommentCount(int count);

    void displayReposts(int count, boolean userReposted);

    void goToPostEditing(int accountId, @NonNull Post post);

    void showPostNotReadyToast();

    void copyLinkToClipboard(String link);

    void showSuccessToast();

    void copyTextToClipboard(String text);

    void displayDefaultToolbaSubitle();

    void displayPinComplete(boolean pin);

    void displayDeleteOrRestoreComplete(boolean deleted);

    void goToNewsSearch(int accountId, String hashTag);

    interface IOptionView {
        void setCanDelete(boolean can);

        void setCanRestore(boolean can);

        void setCanPin(boolean can);

        void setCanUnpin(boolean can);

        void setCanEdit(boolean can);
    }
}