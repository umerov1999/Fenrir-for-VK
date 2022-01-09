package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IVideoPreviewView extends IAccountDependencyView, IMvpView, IErrorView {

    void displayLoading();

    void displayLoadingError();

    void displayVideoInfo(Video video);

    void displayLikes(int count, boolean userLikes);

    void setCommentButtonVisible(boolean visible);

    void displayCommentCount(int count);

    void showSuccessToast();

    void showOwnerWall(int accountId, int ownerId);

    void showSubtitle(String subtitle);

    void showComments(int accountId, Commented commented);

    void displayShareDialog(int accountId, Video video, boolean canPostToMyWall);

    void showVideoPlayMenu(int accountId, Video video);

    void doAutoPlayVideo(int accountId, Video video);

    void goToLikes(int accountId, String type, int ownerId, int id);

    void displayOwner(@NonNull Owner owner);

    interface IOptionView {
        void setCanAdd(boolean can);

        void setIsMy(boolean my);
    }
}
