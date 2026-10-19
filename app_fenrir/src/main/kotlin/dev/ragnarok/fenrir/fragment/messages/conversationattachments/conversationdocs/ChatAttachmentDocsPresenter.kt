package dev.ragnarok.fenrir.fragment.messages.conversationattachments.conversationdocs

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiDoc
import dev.ragnarok.fenrir.api.model.interfaces.VKApiAttachment
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.fragment.messages.conversationattachments.abschatattachments.BaseChatAttachmentsPresenter
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatAttachmentDocsPresenter(peerId: Long, accountId: Long, savedInstanceState: Bundle?) :
    BaseChatAttachmentsPresenter<Document, IChatAttachmentDocsView>(
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
    ): Flow<Pair<String?, List<Document>>> {
        return messageRepository.getHistoryAttachments(
            accountId = accountId,
            peerId = peerId,
            mediaType = VKApiAttachment.TYPE_DOC,
            startFrom = nextFrom,
            count = 50,
            photoSizes = true,
            preserveOrder = true,
            maxForwardsLevel = 45
        )
            .map { response ->
                val docs: MutableList<Document> = ArrayList(
                    Utils.safeCountOf(response.items)
                )
                response.items.nonNullNoEmpty {
                    for (one in it) {
                        if (one.entry != null && one.entry?.attachment is VKApiDoc) {
                            val dto = one.entry?.attachment as VKApiDoc
                            docs.add(
                                Dto2Model.transform(dto).setMsgCmId(one.cmId)
                                    .setMsgPeerId(peerId)
                            )
                        }
                    }
                }
                create(response.next_from, docs)
            }
    }

    override fun onGuiCreated(viewHost: IChatAttachmentDocsView) {
        super.onGuiCreated(viewHost)
        resolveToolbar()
    }

    private fun resolveToolbar() {
        view?.setToolbarTitleString(getString(R.string.attachments_in_chat))
        view?.setToolbarTitleString(getString(R.string.documents_count, Utils.safeCountOf(data)))
    }
}