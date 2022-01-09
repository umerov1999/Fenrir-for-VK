package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.mvp.view.IBasePostEditView;
import dev.ragnarok.fenrir.util.BooleanValue;


public abstract class AbsPostEditPresenter<V extends IBasePostEditView> extends AbsAttachmentsEditPresenter<V> {

    final BooleanValue fromGroup = new BooleanValue();
    final BooleanValue friendsOnly = new BooleanValue();
    final BooleanValue addSignature = new BooleanValue();

    private final BooleanValue friendsOnlyOptionAvailable = new BooleanValue();
    private final BooleanValue fromGroupOptionAvailable = new BooleanValue();
    private final BooleanValue addSignatureOptionAvailable = new BooleanValue();

    AbsPostEditPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
    }

    public final void fireShowAuthorChecked(boolean checked) {
        if (addSignature.setValue(checked)) {
            onShowAuthorChecked(checked);
        }
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        super.onGuiCreated(view);

        view.setFriendsOnlyOptionVisible(friendsOnlyOptionAvailable.get());
        view.setFromGroupOptionVisible(fromGroupOptionAvailable.get());
        view.setAddSignatureOptionVisible(addSignatureOptionAvailable.get());

        view.setShowAuthorChecked(addSignature.get());
        view.setFriendsOnlyCheched(friendsOnly.get());
        view.setFromGroupChecked(fromGroup.get());
    }

    void checkFriendsOnly(boolean checked) {
        if (friendsOnly.setValue(checked)) {
            callView(view -> view.setFriendsOnlyCheched(checked));
        }
    }

    void onShowAuthorChecked(boolean checked) {

    }

    public final void fireFromGroupChecked(boolean checked) {
        if (fromGroup.setValue(checked)) {
            onFromGroupChecked(checked);
        }
    }


    void onFromGroupChecked(boolean checked) {

    }

    void setFromGroupOptionAvailable(boolean available) {
        if (fromGroupOptionAvailable.setValue(available)) {
            callView(view -> view.setFromGroupOptionVisible(available));
        }
    }

    public final void fireFriendsOnlyCheched(boolean checked) {
        if (friendsOnly.setValue(checked)) {
            onFriendsOnlyCheched(checked);
        }
    }

    boolean isAddSignatureOptionAvailable() {
        return addSignatureOptionAvailable.get();
    }

    void setAddSignatureOptionAvailable(boolean available) {
        if (addSignatureOptionAvailable.setValue(available)) {
            callView(view -> view.setAddSignatureOptionVisible(available));
        }
    }

    boolean isFriendsOnlyOptionAvailable() {
        return friendsOnlyOptionAvailable.get();
    }

    void setFriendsOnlyOptionAvailable(boolean available) {
        if (friendsOnlyOptionAvailable.setValue(available)) {
            callView(view -> view.setFriendsOnlyOptionVisible(available));
        }
    }

    void onFriendsOnlyCheched(boolean checked) {

    }
}
