package dev.ragnarok.fenrir.mvp.view;


public interface ICreateCommentView extends IBaseAttachmentsEditView {
    void returnDataToParent(String textBody);

    void goBack();
}
