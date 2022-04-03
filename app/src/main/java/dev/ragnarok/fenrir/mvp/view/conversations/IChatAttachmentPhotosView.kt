package dev.ragnarok.fenrir.mvp.view.conversations

import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.TmpSource

interface IChatAttachmentPhotosView : IBaseChatAttachmentsView<Photo> {
    fun goToTempPhotosGallery(accountId: Int, source: TmpSource, index: Int)
    fun goToTempPhotosGallery(accountId: Int, ptr: Long, index: Int)
}