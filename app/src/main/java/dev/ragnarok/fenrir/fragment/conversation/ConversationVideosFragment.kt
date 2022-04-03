package dev.ragnarok.fenrir.fragment.conversation

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.adapter.VideosAdapter
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.conversations.ChatAttachmentVideoPresenter
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentVideoView

class ConversationVideosFragment :
    AbsChatAttachmentsFragment<Video, ChatAttachmentVideoPresenter, IChatAttachmentVideoView>(),
    VideosAdapter.VideoOnClickListener, IChatAttachmentVideoView {
    override fun createLayoutManager(): RecyclerView.LayoutManager {
        val columns = resources.getInteger(R.integer.videos_column_count)
        return StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL)
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        val adapter = VideosAdapter(requireActivity(), emptyList())
        adapter.setVideoOnClickListener(this)
        return adapter
    }

    override fun onVideoClick(position: Int, video: Video) {
        presenter?.fireVideoClick(
            video
        )
    }

    override fun onVideoLongClick(position: Int, video: Video): Boolean {
        presenter?.fireGoToMessagesLookup(
            video.msgPeerId,
            video.msgId
        )
        return true
    }

    override fun displayAttachments(data: MutableList<Video>) {
        val adapter = adapter as VideosAdapter?
        adapter?.setData(data)
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<ChatAttachmentVideoPresenter> {
        return object : IPresenterFactory<ChatAttachmentVideoPresenter> {
            override fun create(): ChatAttachmentVideoPresenter {
                val accountId = requireArguments().getInt(Extra.ACCOUNT_ID)
                val peerId = requireArguments().getInt(Extra.PEER_ID)
                return ChatAttachmentVideoPresenter(peerId, accountId, saveInstanceState)
            }
        }
    }
}