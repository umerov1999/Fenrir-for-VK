package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.IdOption;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunityBanEditView extends IMvpView, IAccountDependencyView, IErrorView, IProgressView, IToastView {
    void displayUserInfo(Owner user);

    void displayBanStatus(int adminId, String adminName, long endDate);

    void displayBlockFor(String blockFor);

    void displayReason(String reason);

    void diplayComment(String comment);

    void setShowCommentChecked(boolean checked);

    void goBack();

    void displaySelectOptionDialog(int requestCode, List<IdOption> options);

    void openProfile(int accountId, Owner owner);
}