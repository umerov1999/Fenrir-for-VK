package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.interfaces.IPhotosApi;
import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoAlbumEditor;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IEditPhotoAlbumView;
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView;
import dev.ragnarok.fenrir.mvp.view.base.ISteppersView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.view.steppers.impl.CreatePhotoAlbumStepsHost;

public class EditPhotoAlbumPresenter extends AccountDependencyPresenter<IEditPhotoAlbumView> {

    private final INetworker networker;
    private final boolean editing;
    private final int ownerId;
    private final Context context;
    private final PhotoAlbumEditor editor;
    private PhotoAlbum album;
    private CreatePhotoAlbumStepsHost stepsHost;

    public EditPhotoAlbumPresenter(int accountId, int ownerId, Context context, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        networker = Injection.provideNetworkInterfaces();
        this.ownerId = ownerId;
        editor = PhotoAlbumEditor.create();
        editing = false;
        this.context = context;

        init(savedInstanceState);
    }

    public EditPhotoAlbumPresenter(int accountId, @NonNull PhotoAlbum album, @NonNull PhotoAlbumEditor editor, Context context, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        networker = Injection.provideNetworkInterfaces();
        this.album = album;
        ownerId = album.getOwnerId();
        this.editor = editor;
        editing = true;
        this.context = context;

        init(savedInstanceState);
    }

    private void init(@Nullable Bundle savedInstanceState) {
        stepsHost = new CreatePhotoAlbumStepsHost();
        stepsHost.setAdditionalOptionsEnable(ownerId < 0); // только в группе
        stepsHost.setPrivacySettingsEnable(ownerId > 0); // только у пользователя

        if (savedInstanceState != null) {
            stepsHost.restoreState(savedInstanceState);
        } else {
            stepsHost.setState(createInitialState());
        }
    }

    @Override
    public void onGuiCreated(@NonNull IEditPhotoAlbumView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.attachSteppersHost(stepsHost);
    }

    @NonNull
    private CreatePhotoAlbumStepsHost.PhotoAlbumState createInitialState() {
        return new CreatePhotoAlbumStepsHost.PhotoAlbumState()
                .setPrivacyComment(editor.getPrivacyComment())
                .setPrivacyView(editor.getPrivacyView())
                .setCommentsDisabled(editor.isCommentsDisabled())
                .setUploadByAdminsOnly(editor.isUploadByAdminsOnly())
                .setDescription(editor.getDescription())
                .setTitle(editor.getTitle());
    }

    public void fireStepNegativeButtonClick(int clickAtStep) {
        if (clickAtStep > 0) {
            stepsHost.setCurrentStep(clickAtStep - 1);
            callView(view -> view.moveSteppers(clickAtStep, clickAtStep - 1));
        } else {
            onBackOnFirstStepClick();
        }
    }

    private void onBackOnFirstStepClick() {
        callView(ISteppersView::goBack);
    }

    public void fireStepPositiveButtonClick(int clickAtStep) {
        boolean last = clickAtStep == stepsHost.getStepsCount() - 1;
        if (!last) {
            int targetStep = clickAtStep + 1;
            stepsHost.setCurrentStep(targetStep);

            callView(view -> view.moveSteppers(clickAtStep, targetStep));
        } else {
            callView(ISteppersView::hideKeyboard);
            onFinalButtonClick();
        }
    }

    private void onFinalButtonClick() {
        int accountId = getAccountId();

        IPhotosApi api = networker.vkDefault(accountId).photos();

        String title = state().getTitle();
        String description = state().getDescription();

        boolean uploadsByAdminsOnly = state().isUploadByAdminsOnly();
        boolean commentsDisabled = state().isCommentsDisabled();

        if (editing) {
            appendDisposable(api.editAlbum(album.getId(), title, description, ownerId, null,
                    null, uploadsByAdminsOnly, commentsDisabled)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> goToEditedAlbum(album, t), l -> callView(v -> showError(v, getCauseIfRuntime(l)))));
        } else {
            Integer groupId = ownerId < 0 ? Math.abs(ownerId) : null;
            appendDisposable(api.createAlbum(title, groupId, description, null, null, uploadsByAdminsOnly, commentsDisabled)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::goToAlbum, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
        }
    }

    private void goToAlbum(VKApiPhotoAlbum album) {
        PlaceFactory.getVKPhotosAlbumPlace(getAccountId(), album.owner_id, album.id,
                IVkPhotosView.ACTION_SHOW_PHOTOS)
                .withParcelableExtra(Extra.ALBUM, new PhotoAlbum(album.id, album.owner_id))
                .tryOpenWith(context);
    }

    private void goToEditedAlbum(PhotoAlbum album, Boolean ret) {
        if (ret == null || !ret)
            return;
        PlaceFactory.getVKPhotosAlbumPlace(getAccountId(), album.getOwnerId(), album.getId(),
                IVkPhotosView.ACTION_SHOW_PHOTOS)
                .withParcelableExtra(Extra.ALBUM, album)
                .tryOpenWith(context);
    }

    public boolean fireBackButtonClick() {
        int currentStep = stepsHost.getCurrentStep();

        if (currentStep > 0) {
            fireStepNegativeButtonClick(currentStep);
            return false;
        } else {
            return true;
        }
    }

    public void firePrivacyCommentClick() {

    }

    public void firePrivacyViewClick() {

    }

    public void fireUploadByAdminsOnlyChecked(boolean checked) {
        state().setUploadByAdminsOnly(checked);
    }

    public void fireDisableCommentsClick(boolean checked) {
        state().setCommentsDisabled(checked);
    }

    private CreatePhotoAlbumStepsHost.PhotoAlbumState state() {
        return stepsHost.getState();
    }

    public void fireTitleEdit(CharSequence text) {
        state().setTitle(text.toString());
        callView(view -> view.updateStepButtonsAvailability(CreatePhotoAlbumStepsHost.STEP_TITLE_AND_DESCRIPTION));
    }
}