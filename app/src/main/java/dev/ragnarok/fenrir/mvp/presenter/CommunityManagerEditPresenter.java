package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.domain.IGroupSettingsInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.ContactInfo;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityManagerEditView;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;


public class CommunityManagerEditPresenter extends AccountDependencyPresenter<ICommunityManagerEditView> {

    private final List<User> users;

    private final int groupId;
    private final IGroupSettingsInteractor interactor;
    private final boolean creator;
    private final boolean adding;
    private int currentUserIndex;
    private int adminLevel;
    private boolean showAsContact;
    private String position;
    private String email;
    private String phone;
    private boolean savingNow;

    public CommunityManagerEditPresenter(int accountId, int groupId, Manager manager, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        User user = manager.getUser();
        users = Collections.singletonList(user);
        this.groupId = groupId;
        creator = "creator".equalsIgnoreCase(manager.getRole());

        if (!creator) {
            adminLevel = convertRoleToAdminLevel(manager.getRole());
        }

        showAsContact = manager.isDisplayAsContact();
        interactor = InteractorFactory.createGroupSettingsInteractor();
        adding = false;

        if (nonNull(savedInstanceState)) {
            restoreState(savedInstanceState);
        } else {
            ContactInfo info = manager.getContactInfo();

            if (nonNull(info)) {
                position = info.getDescriprion();
                email = info.getEmail();
                phone = info.getPhone();
            }
        }
    }

    public CommunityManagerEditPresenter(int accountId, int groupId, List<User> users, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        creator = false;
        this.users = users;
        this.groupId = groupId;
        adminLevel = VKApiCommunity.AdminLevel.MODERATOR;
        showAsContact = false;
        interactor = InteractorFactory.createGroupSettingsInteractor();
        adding = true;

        if (nonNull(savedInstanceState)) {
            restoreState(savedInstanceState);
        }
    }

    private static int convertRoleToAdminLevel(String role) {
        if ("moderator".equalsIgnoreCase(role)) {
            return VKApiCommunity.AdminLevel.MODERATOR;
        } else if ("editor".equalsIgnoreCase(role)) {
            return VKApiCommunity.AdminLevel.EDITOR;
        } else if ("administrator".equalsIgnoreCase(role)) {
            return VKApiCommunity.AdminLevel.ADMIN;
        } else
            return 0;
    }

    private static String convertAdminLevelToRole(int adminLevel) {
        switch (adminLevel) {
            case VKApiCommunity.AdminLevel.MODERATOR:
                return "moderator";
            case VKApiCommunity.AdminLevel.EDITOR:
                return "editor";
            case VKApiCommunity.AdminLevel.ADMIN:
                return "administrator";
        }

        throw new IllegalArgumentException("Invalid adminLevel");
    }

    private boolean isCreator() {
        return creator;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt("currentUserIndex", currentUserIndex);
        outState.putString("position", position);
        outState.putString("email", email);
        outState.putString("phone", phone);
    }

    private void restoreState(Bundle state) {
        currentUserIndex = state.getInt("currentUserIndex");
        position = state.getString("position");
        email = state.getString("email");
        phone = state.getString("phone");
    }

    @NonNull
    private User getCurrentUser() {
        return users.get(currentUserIndex);
    }

    private boolean canDelete() {
        return !isCreator() && !adding;
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityManagerEditView view) {
        super.onGuiCreated(view);

        resolveRadioButtonsCheckState();
        resolveDeleteOptionVisibility();
        resolveRadioButtonsVisibility();
        resolveProgressView();
        resolveContactBlock();
        resolveUserInfoViews();
    }

    private void resolveRadioButtonsCheckState() {
        if (!isCreator()) {
            switch (adminLevel) {
                case VKApiCommunity.AdminLevel.MODERATOR:
                    callView(ICommunityManagerEditView::checkModerator);
                    break;
                case VKApiCommunity.AdminLevel.EDITOR:
                    callView(ICommunityManagerEditView::checkEditor);
                    break;
                case VKApiCommunity.AdminLevel.ADMIN:
                    callView(ICommunityManagerEditView::checkAdmin);
                    break;
            }
        }
    }

    private void resolveDeleteOptionVisibility() {
        callView(v -> v.setDeleteOptionVisible(canDelete()));
    }

    private void resolveRadioButtonsVisibility() {
        callView(v -> v.configRadioButtons(isCreator()));
    }

    private void setSavingNow(boolean savingNow) {
        this.savingNow = savingNow;
        resolveProgressView();
    }

    private void resolveProgressView() {
        if (savingNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.saving, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    private String getSelectedRole() {
        if (isCreator()) {
            return "creator";
        }

        return convertAdminLevelToRole(adminLevel);
    }

    public void fireButtonSaveClick() {
        int accountId = getAccountId();
        String role = getSelectedRole();
        User user = getCurrentUser();

        setSavingNow(true);
        appendDisposable(interactor.editManager(accountId, groupId, user, role, showAsContact, position, email, phone)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onSavingComplete, throwable -> onSavingError(Utils.getCauseIfRuntime(throwable))));
    }

    public void fireDeleteClick() {
        if (isCreator()) {
            return;
        }

        int accountId = getAccountId();
        User user = getCurrentUser();

        setSavingNow(true);
        appendDisposable(interactor.editManager(accountId, groupId, user, null, false, null, null, null)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onSavingComplete, throwable -> onSavingError(Utils.getCauseIfRuntime(throwable))));
    }

    private void onSavingComplete() {
        setSavingNow(false);
        callView(v -> v.showToast(R.string.success, false));

        if (currentUserIndex == users.size() - 1) {
            callView(ICommunityManagerEditView::goBack);
        } else {
            // switch to next user
            currentUserIndex++;

            resolveUserInfoViews();

            adminLevel = VKApiCommunity.AdminLevel.MODERATOR;
            showAsContact = false;
            position = null;
            email = null;
            phone = null;

            resolveContactBlock();
            resolveRadioButtonsVisibility();
        }
    }

    private void resolveContactBlock() {
        callView(v -> {
            v.setShowAsContactCheched(showAsContact);
            v.setContactInfoVisible(showAsContact);
            v.displayPosition(position);
            v.displayEmail(email);
            v.displayPhone(phone);
        });
    }

    private void resolveUserInfoViews() {
        callView(v -> v.displayUserInfo(getCurrentUser()));
    }

    private void onSavingError(Throwable throwable) {
        throwable.printStackTrace();
        setSavingNow(false);
        callView(v -> showError(v, throwable));
    }

    public void fireAvatarClick() {
        callView(v -> v.showUserProfile(getAccountId(), getCurrentUser()));
    }

    public void fireModeratorChecked() {
        adminLevel = VKApiCommunity.AdminLevel.MODERATOR;
    }

    public void fireEditorChecked() {
        adminLevel = VKApiCommunity.AdminLevel.EDITOR;
    }

    public void fireAdminChecked() {
        adminLevel = VKApiCommunity.AdminLevel.ADMIN;
    }

    public void fireShowAsContactChecked(boolean checked) {
        if (checked != showAsContact) {
            showAsContact = checked;
            callView(v -> v.setContactInfoVisible(checked));
        }
    }

    public void firePositionEdit(CharSequence s) {
        position = s.toString();
    }

    public void fireEmailEdit(CharSequence s) {
        email = s.toString();
    }

    public void firePhoneEdit(CharSequence s) {
        phone = s.toString();
    }
}