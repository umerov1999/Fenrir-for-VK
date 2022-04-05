package dev.ragnarok.fenrir.mvp.presenter.conversations

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.Apis.get
import dev.ragnarok.fenrir.api.model.VKApiAttachment
import dev.ragnarok.fenrir.api.model.VKApiLink
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentPostsView
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.Utils
import io.reactivex.rxjava3.core.Single

class ChatAttachmentPostsPresenter(peerId: Int, accountId: Int, savedInstanceState: Bundle?) :
    BaseChatAttachmentsPresenter<Link, IChatAttachmentPostsView>(
        peerId,
        accountId,
        savedInstanceState
    ) {
    override fun onDataChanged() {
        super.onDataChanged()
        resolveToolbar()
    }

    override fun requestAttachments(
        peerId: Int,
        nextFrom: String?
    ): Single<Pair<String, List<Link>>> {
        return get().vkDefault(accountId)
            .messages()
            .getHistoryAttachments(peerId, VKApiAttachment.TYPE_POST, nextFrom, 1, 50, null)
            .map { response ->
                val docs: MutableList<Link> = ArrayList(Utils.safeCountOf(response.items))
                if (response.items != null) {
                    for (one in response.items) {
                        if (one?.entry != null && one.entry.attachment is VKApiLink) {
                            val dto = one.entry.attachment as VKApiLink
                            docs.add(
                                Dto2Model.transform(dto).setMsgId(one.messageId)
                                    .setMsgPeerId(peerId)
                            )
                        }
                    }
                }
                create(response.next_from, docs)
            }
    }

    override fun onGuiCreated(viewHost: IChatAttachmentPostsView) {
        super.onGuiCreated(viewHost)
        resolveToolbar()
    }

    private fun resolveToolbar() {
        view?.setToolbarTitleString(getString(R.string.attachments_in_chat))
        view?.setToolbarTitleString(getString(R.string.posts_count, Utils.safeCountOf(data)))
    }
}