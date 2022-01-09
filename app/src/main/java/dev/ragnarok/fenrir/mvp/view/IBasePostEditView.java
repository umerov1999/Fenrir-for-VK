package dev.ragnarok.fenrir.mvp.view;


public interface IBasePostEditView extends IBaseAttachmentsEditView, IProgressView {
    void displaySignerInfo(String fullName, String photo);

    void setShowAuthorChecked(boolean checked);

    void setSignerInfoVisible(boolean visible);

    void setAddSignatureOptionVisible(boolean visible);

    void setFromGroupOptionVisible(boolean visible);

    void setFriendsOnlyOptionVisible(boolean visible);

    void setFromGroupChecked(boolean checked);

    void setFriendsOnlyCheched(boolean cheched);
}
