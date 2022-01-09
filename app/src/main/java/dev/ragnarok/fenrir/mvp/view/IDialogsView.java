package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.List;

import dev.ragnarok.fenrir.model.Dialog;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IDialogsView extends IAccountDependencyView, IMvpView, IErrorView, IToastView {

    void displayData(List<Dialog> data, int accountId);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void goToChat(int accountId, int messagesOwnerId, int peerId, String title, String ava_url);

    void goToSearch(int accountId);

    void goToImportant(int accountId);

    void showSnackbar(@StringRes int res, boolean isLong);

    void askToReload();

    void showEnterNewGroupChatTitle(List<User> users);

    void showNotificationSettings(int accountId, int peerId);

    void goToOwnerWall(int accountId, int ownerId, @Nullable Owner owner);

    void setCreateGroupChatButtonVisible(boolean visible);

    void notifyHasAttachments(boolean has);

    interface IContextView {
        void setCanDelete(boolean can);

        void setCanAddToHomescreen(boolean can);

        void setCanConfigNotifications(boolean can);

        void setCanAddToShortcuts(boolean can);

        void setIsHidden(boolean can);

        void setCanRead(boolean can);

        void setPinned(boolean pinned);
    }

    interface IOptionView {
        void setCanSearch(boolean can);
    }
}
