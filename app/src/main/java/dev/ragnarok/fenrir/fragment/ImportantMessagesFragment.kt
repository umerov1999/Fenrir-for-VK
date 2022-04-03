package dev.ragnarok.fenrir.fragment

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder.VoiceActionListener
import dev.ragnarok.fenrir.adapter.MessagesAdapter
import dev.ragnarok.fenrir.adapter.MessagesAdapter.OnMessageActionListener
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.ImportantMessagesPresenter
import dev.ragnarok.fenrir.mvp.view.IImportantMessagesView
import dev.ragnarok.fenrir.place.PlaceFactory.getMessagesLookupPlace
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme

class ImportantMessagesFragment :
    PlaceSupportMvpFragment<ImportantMessagesPresenter, IImportantMessagesView>(),
    OnMessageActionListener, IImportantMessagesView, VoiceActionListener {
    private val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                presenter?.fireRemoveImportant(
                    viewHolder.bindingAdapterPosition
                )
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }
        }
    private var mAdapter: MessagesAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_important_msgs, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView)
        mAdapter = MessagesAdapter(requireActivity(), mutableListOf(), this, true)
        mAdapter?.setOnMessageActionListener(this)
        mAdapter?.setVoiceActionListener(this)
        recyclerView.adapter = mAdapter
        return root
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onClearSelection()
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.subtitle = null
            actionBar.setTitle(R.string.important)
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun notifyItemChanged(index: Int) {
        mAdapter?.notifyItemBindableChanged(index)
    }

    override fun onAvatarClick(message: Message, userId: Int, position: Int) {
        onOpenOwner(userId)
    }

    override fun onLongAvatarClick(message: Message, userId: Int, position: Int) {
        onOpenOwner(userId)
    }

    override fun onRestoreClick(message: Message, position: Int) {}
    override fun onBotKeyboardClick(button: Keyboard.Button) {}
    override fun goToMessagesLookup(accountId: Int, peerId: Int, messageId: Int) {
        getMessagesLookupPlace(accountId, peerId, messageId, null).tryOpenWith(requireActivity())
    }

    override fun onMessageLongClick(message: Message, position: Int): Boolean {
        presenter?.fireForwardClick()
        return true
    }

    override fun onMessageClicked(message: Message, position: Int) {
        presenter?.fireMessagesLookup(
            message
        )
    }

    override fun onMessageDelete(message: Message) {}
    override fun displayMessages(messages: MutableList<Message>, lastReadId: LastReadId) {
        mAdapter?.setItems(messages, lastReadId)
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun forwardMessages(accountId: Int, messages: ArrayList<Message>) {
        startForSendAttachments(requireActivity(), accountId, FwdMessages(messages))
    }

    override fun notifyMessagesUpAdded(position: Int, count: Int) {
        // not supported
    }

    override fun notifyDataChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun notifyMessagesDownAdded(count: Int) {
        // not supported
    }

    override fun configNowVoiceMessagePlaying(
        id: Int,
        progress: Float,
        paused: Boolean,
        amin: Boolean,
        speed: Boolean
    ) {
        mAdapter?.configNowVoiceMessagePlaying(id, progress, paused, amin, speed)
    }

    override fun bindVoiceHolderById(
        holderId: Int,
        play: Boolean,
        paused: Boolean,
        progress: Float,
        amin: Boolean,
        speed: Boolean
    ) {
        mAdapter?.bindVoiceHolderById(holderId, play, paused, progress, amin, speed)
    }

    override fun disableVoicePlaying() {
        mAdapter?.disableVoiceMessagePlaying()
    }

    override fun showActionMode(
        title: String,
        canEdit: Boolean,
        canPin: Boolean,
        canStar: Boolean,
        doStar: Boolean,
        canSpam: Boolean
    ) {
        // not supported
    }

    override fun finishActionMode() {
        // not supported
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<ImportantMessagesPresenter> {
        return object : IPresenterFactory<ImportantMessagesPresenter> {
            override fun create(): ImportantMessagesPresenter {
                val accountId = requireArguments().getInt(Extra.ACCOUNT_ID)
                return ImportantMessagesPresenter(accountId, saveInstanceState)
            }
        }
    }

    override fun onVoiceHolderBinded(voiceMessageId: Int, voiceHolderId: Int) {
        presenter?.fireVoiceHolderCreated(
            voiceMessageId,
            voiceHolderId
        )
    }

    override fun onVoicePlayButtonClick(
        voiceHolderId: Int,
        voiceMessageId: Int,
        voiceMessage: VoiceMessage
    ) {
        presenter?.fireVoicePlayButtonClick(
            voiceHolderId,
            voiceMessageId,
            voiceMessage
        )
    }

    override fun onVoiceTogglePlaybackSpeed() {
        presenter?.fireVoicePlaybackSpeed()
    }

    override fun onTranscript(voiceMessageId: String, messageId: Int) {
        presenter?.fireTranscript(
            voiceMessageId,
            messageId
        )
    }

    override fun showRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    companion object {
        fun newInstance(accountId: Int): ImportantMessagesFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            val mFragment = ImportantMessagesFragment()
            mFragment.arguments = args
            return mFragment
        }
    }
}