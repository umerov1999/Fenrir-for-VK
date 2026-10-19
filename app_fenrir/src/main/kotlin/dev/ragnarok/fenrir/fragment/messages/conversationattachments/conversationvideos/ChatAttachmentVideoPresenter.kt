package dev.ragnarok.fenrir.fragment.messages.conversationattachments.conversationvideos

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiVideo
import dev.ragnarok.fenrir.api.model.interfaces.VKApiAttachment
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.fragment.messages.conversationattachments.abschatattachments.BaseChatAttachmentsPresenter
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatAttachmentVideoPresenter(peerId: Long, accountId: Long, savedInstanceState: Bundle?) :
    BaseChatAttachmentsPresenter<Video, IChatAttachmentVideoView>(
        peerId,
        accountId,
        savedInstanceState
    ) {
    override fun onDataChanged() {
        super.onDataChanged()
        resolveToolbar()
    }

    override fun requestAttachments(
        peerId: Long,
        nextFrom: String?
    ): Flow<Pair<String?, List<Video>>> {
        return messageRepository.getHistoryAttachments(
            accountId = accountId,
            peerId = peerId,
            mediaType = VKApiAttachment.TYPE_VIDEO,
            startFrom = nextFrom,
            count = 50,
            photoSizes = true,
            preserveOrder = true,
            maxForwardsLevel = 45
        )
            .map { response ->
                val videos: MutableList<Video> = ArrayList(
                    Utils.safeCountOf(response.items)
                )
                response.items.nonNullNoEmpty {
                    for (one in it) {
                        if (one.entry != null && one.entry?.attachment is VKApiVideo) {
                            val dto = one.entry?.attachment as VKApiVideo
                            videos.add(
                                Dto2Model.transform(dto).setMsgCmId(one.cmId)
                                    .setMsgPeerId(peerId)
                            )
                        }
                    }
                }
                create(response.next_from, videos)
            }
    }

    override fun onGuiCreated(viewHost: IChatAttachmentVideoView) {
        super.onGuiCreated(viewHost)
        resolveToolbar()
    }

    private fun resolveToolbar() {
        view?.setToolbarTitleString(getString(R.string.attachments_in_chat))
        view?.setToolbarTitleString(getString(R.string.videos_count, Utils.safeCountOf(data)))
    }
}