package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.model.Comment;


public interface ICommentEditView extends IBaseAttachmentsEditView, IProgressView {
    void goBackWithResult(@Nullable Comment comment);

    void showConfirmWithoutSavingDialog();

    void goBack();
}
