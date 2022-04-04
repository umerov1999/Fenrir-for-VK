package dev.ragnarok.fenrir.mvp.presenter.conversations

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.Apis.get
import dev.ragnarok.fenrir.api.model.VKApiPhoto
import dev.ragnarok.fenrir.api.model.response.AttachmentsHistoryResponse
import dev.ragnarok.fenrir.db.Stores
import dev.ragnarok.fenrir.db.serialize.Serializers
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.TmpSource
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.module.parcel.ParcelFlags
import dev.ragnarok.fenrir.module.parcel.ParcelNative
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentPhotosView
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Analytics
import dev.ragnarok.fenrir.util.DisposableHolder
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.Utils
import io.reactivex.rxjava3.core.Single

class ChatAttachmentPhotoPresenter(peerId: Int, accountId: Int, savedInstanceState: Bundle?) :
    BaseChatAttachmentsPresenter<Photo, IChatAttachmentPhotosView>(
        peerId,
        accountId,
        savedInstanceState
    ) {
    private val openGalleryDisposableHolder = DisposableHolder<Void>()
    override fun requestAttachments(
        peerId: Int,
        nextFrom: String?
    ): Single<Pair<String, List<Photo>>> {
        return get().vkDefault(accountId)
            .messages()
            .getHistoryAttachments(peerId, "photo", nextFrom, 1, 50, null)
            .map { response: AttachmentsHistoryResponse ->
                val photos: MutableList<Photo> = ArrayList()
                for (one in response.items) {
                    if (one?.entry != null && one.entry.attachment is VKApiPhoto) {
                        val dto = one.entry.attachment as VKApiPhoto
                        photos.add(
                            Dto2Model.transform(dto).setMsgId(one.messageId).setMsgPeerId(peerId)
                        )
                    }
                }
                create(response.next_from, photos)
            }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        resolveToolbar()
    }

    override fun onGuiCreated(viewHost: IChatAttachmentPhotosView) {
        super.onGuiCreated(viewHost)
        resolveToolbar()
    }

    private fun resolveToolbar() {
        view?.setToolbarTitleString(getString(R.string.attachments_in_chat))
        view?.setToolbarTitleString(getString(R.string.photos_count, Utils.safeCountOf(data)))
    }

    override fun onDestroyed() {
        openGalleryDisposableHolder.dispose()
        super.onDestroyed()
    }

    fun firePhotoClick(position: Int) {
        if (FenrirNative.isNativeLoaded && Settings.get().other().isNative_parcel_photo) {
            view?.goToTempPhotosGallery(
                accountId,
                ParcelNative.createParcelableList(data, ParcelFlags.NULL_LIST),
                position
            )
        } else {
            val source = TmpSource(instanceId, 0)
            fireTempDataUsage()
            openGalleryDisposableHolder.append(Stores.instance
                .tempStore()
                .put(source.ownerId, source.sourceId, data, Serializers.PHOTOS_SERIALIZER)
                .fromIOToMain()
                .subscribe({
                    onPhotosSavedToTmpStore(
                        position,
                        source
                    )
                }) { throwable: Throwable? -> Analytics.logUnexpectedError(throwable) })
        }
    }

    private fun onPhotosSavedToTmpStore(index: Int, source: TmpSource) {
        view?.goToTempPhotosGallery(
            accountId,
            source,
            index
        )
    }
}