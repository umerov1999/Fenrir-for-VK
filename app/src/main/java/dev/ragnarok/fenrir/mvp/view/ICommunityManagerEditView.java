package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunityManagerEditView extends IMvpView, IAccountDependencyView, IErrorView, IProgressView, IToastView {
    void displayUserInfo(User user);

    void showUserProfile(int accountId, User user);

    void checkModerator();

    void checkEditor();

    void checkAdmin();

    void setShowAsContactCheched(boolean cheched);

    void setContactInfoVisible(boolean visible);

    void displayPosition(String position);

    void displayEmail(String email);

    void displayPhone(String phone);

    void configRadioButtons(boolean isCreator);

    void goBack();

    void setDeleteOptionVisible(boolean visible);
}