package dev.ragnarok.fenrir.mvp.view.conversations;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.TmpSource;


public interface IChatAttachmentPhotosView extends IBaseChatAttachmentsView<Photo> {
    void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index);

    void goToTempPhotosGallery(int accountId, long ptr, int index);
}