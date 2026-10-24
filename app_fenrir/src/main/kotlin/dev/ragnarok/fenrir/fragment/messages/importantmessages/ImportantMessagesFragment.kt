package dev.ragnarok.fenrir.fragment.messages.importantmessages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.fragment.base.AttachmentsViewBinder.VoiceActionListener
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment
import dev.ragnarok.fenrir.fragment.messages.chat.MessagesAdapter
import dev.ragnarok.fenrir.fragment.messages.chat.MessagesAdapter.OnMessageActionListener
import dev.ragnarok.fenrir.listener.BackPressCallback
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.FwdMessages
import dev.ragnarok.fenrir.model.Keyboard
import dev.ragnarok.fenrir.model.LastReadId
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.VoiceMessage
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.place.PlaceFactory.getMessagesLookupPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.MessagesReplyItemCallback
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import java.lang.ref.WeakReference

class ImportantMessagesFragment :
    PlaceSupportMvpFragment<ImportantMessagesPresenter, IImportantMessagesView>(),
    OnMessageActionListener, IImportantMessagesView, VoiceActionListener, BackPressCallback {
    private var mAdapter: MessagesAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var toolbarRootView: FrameLayout? = null
    private var downMenuGroup: FrameLayout? = null
    private var mActionView: ActionModeHolder? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_important_msgs, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.findViewById<View>(R.id.toolbar_root)?.setPadding(0, insets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        PicassoPauseOnScrollListener.addListener(recyclerView)
        recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        downMenuGroup = root.findViewById(R.id.down_menu)
        toolbarRootView = root.findViewById(R.id.toolbar_root)

        ItemTouchHelper(MessagesReplyItemCallback { o ->
            MaterialAlertDialogBuilder(requireActivity())
                .setMessage(R.string.do_unimportant)
                .setTitle(R.string.confirmation)
                .setCancelable(true)
                .setPositiveButton(R.string.button_yes) { _, _ ->
                    presenter?.fireRemoveImportant(
                        o
                    )
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
        }).attachToRecyclerView(recyclerView)

        mAdapter = MessagesAdapter(
            Settings.get().accounts().current,
            requireActivity(),
            mutableListOf(),
            LastReadId(0, 0),
            this,
            true
        )
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
            .build()
            .apply(requireActivity())
    }

    override fun onAvatarClick(message: Message, userId: Long, position: Int) {
        onOpenOwner(userId)
    }

    override fun onLongAvatarClick(message: Message, userId: Long, position: Int) {
        onOpenOwner(userId)
    }

    override fun onRestoreClick(message: Message, position: Int) {
        presenter?.fireMessageRestoreClick(
            message
        )
    }

    override fun onBotKeyboardClick(button: Keyboard.Button) {}
    override fun goToMessagesLookup(accountId: Long, peerId: Long, cmId: Int) {
        getMessagesLookupPlace(accountId, peerId, cmId, null).tryOpenWith(requireActivity())
    }

    override fun onMessageLongClick(message: Message, position: Int): Boolean {
        presenter?.fireMessageLongClick(
            message,
            position
        )
        return true
    }

    override fun onMessageClicked(message: Message, position: Int, x: Int, y: Int) {
        presenter?.fireImportantMessageClick(
            message, position
        )
    }

    override fun onMessageDelete(message: Message) {}
    override fun displayMessages(
        accountId: Long,
        messages: MutableList<Message>,
        lastReadId: LastReadId
    ) {
        mAdapter?.setItems(messages, lastReadId)
    }

    override fun showPopupOptions(
        position: Int,
        x: Int,
        y: Int,
        canEdit: Boolean,
        canPin: Boolean,
        canStar: Boolean,
        doStar: Boolean,
        canSpam: Boolean,
        canViewMessageViewed: Boolean
    ) {
        //"Not yet implemented"
    }

    override fun refetchReactionCache(accountId: Long) {
        mAdapter?.refetchReactionCache(accountId)
    }

    override fun showMessageReadPeers(
        accountId: Long,
        peerId: Long,
        cmId: Int
    ) {
        PlaceFactory.getMessagesReadPeersPlace(accountId, peerId, cmId)
            .tryOpenWith(requireActivity())
    }

    override fun forwardMessages(accountId: Long, messages: ArrayList<Message>) {
        startForSendAttachments(requireActivity(), accountId, FwdMessages(messages))
    }

    override fun notifyItemsAdded(position: Int, count: Int) {
        mAdapter?.notifyItemBindableRangeInserted(position, count)
    }

    override fun notifyItemsRemoved(position: Int, count: Int) {
        mAdapter?.notifyItemBindableRangeRemoved(position, count)
    }

    override fun notifyItemsChanged(index: Int, count: Int) {
        mAdapter?.notifyItemBindableRangeChanged(index, count)
    }

    override fun notifyDataChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun configNowVoiceMessagePlaying(
        id: Int,
        progress: Float,
        duration: Long,
        paused: Boolean,
        amin: Boolean,
        speed: Boolean
    ) {
        mAdapter?.configNowVoiceMessagePlaying(id, progress, duration, paused, amin, speed)
    }

    override fun bindVoiceHolderById(
        holderId: Int,
        play: Boolean,
        paused: Boolean,
        progress: Float,
        duration: Long,
        amin: Boolean,
        speed: Boolean
    ) {
        mAdapter?.bindVoiceHolderById(holderId, play, paused, progress, duration, amin, speed)
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
        canSpam: Boolean,
        canViewMessageViewed: Boolean
    ) {
        val isDown = Settings.get().main().isMessages_menu_down
        if (if (isDown) downMenuGroup == null else toolbarRootView == null) {
            return
        }
        if (!isDown) {
            if (toolbarRootView?.childCount == Constants.FRAGMENT_CHAT_APP_BAR_VIEW_COUNT) {
                mActionView = ActionModeHolder(
                    LayoutInflater.from(requireActivity())
                        .inflate(R.layout.view_action_mode, toolbarRootView, false), this
                )
                toolbarRootView?.addView(mActionView?.rootView)
            }
        } else {
            downMenuGroup?.visibility = View.VISIBLE
            if (downMenuGroup?.childCount == Constants.FRAGMENT_CHAT_DOWN_MENU_VIEW_COUNT) {
                mActionView = ActionModeHolder(
                    LayoutInflater.from(requireActivity())
                        .inflate(R.layout.view_action_mode, downMenuGroup, false), this
                )
                downMenuGroup?.addView(mActionView?.rootView)
            }
        }
        mActionView?.run {
            show()
            titleView.text = title
            buttonSpam.visibility = if (canSpam) View.VISIBLE else View.GONE
            buttonEdit.visibility = if (canEdit) View.VISIBLE else View.GONE
            buttonPin.visibility = if (canPin) View.VISIBLE else View.GONE
            buttonStar.visibility = if (canStar) View.VISIBLE else View.GONE
            buttonStar.setImageResource(if (doStar) R.drawable.star_add else R.drawable.star_none)
            buttonMessageReadPeer.visibility = if (canViewMessageViewed) View.VISIBLE else View.GONE
        }
    }

    override fun finishActionMode() {
        mActionView?.hide()
        if (Settings.get().main().isMessages_menu_down && downMenuGroup != null) {
            downMenuGroup?.visibility = View.GONE
        }
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): ImportantMessagesPresenter {
        val accountId = requireArguments().getLong(Extra.ACCOUNT_ID)
        return ImportantMessagesPresenter(accountId, saveInstanceState)
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
        peerId: Long,
        cmId: Int,
        voiceMessage: VoiceMessage
    ) {
        presenter?.fireVoicePlayButtonClick(
            voiceHolderId,
            voiceMessageId,
            peerId,
            cmId,
            voiceMessage
        )
    }

    override fun onVoiceTogglePlaybackSpeed() {
        presenter?.fireVoicePlaybackSpeed()
    }

    override fun onTranscript(voiceMessageId: String, peerId: Long, cmId: Int) {
        presenter?.fireTranscript(
            voiceMessageId,
            peerId,
            cmId
        )
    }

    override fun onPlayPositionChanged(position: Long) {
        presenter?.firePlayPositionChanged(position)
    }

    override fun showRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    override fun onBackPressed(): Boolean {
        if (mActionView?.isVisible == true) {
            mActionView?.hide()
            return false
        }
        return true
    }

    private inner class ActionModeHolder(val rootView: View, fragment: ImportantMessagesFragment) {
        val buttonClose: View = rootView.findViewById(R.id.buttonClose)
        val buttonEdit: View = rootView.findViewById(R.id.buttonEdit)
        val buttonForward: View = rootView.findViewById(R.id.buttonForward)
        val buttonCopy: View = rootView.findViewById(R.id.buttonCopy)
        val buttonDelete: View = rootView.findViewById(R.id.buttonDelete)
        val buttonPin: View = rootView.findViewById(R.id.buttonPin)
        val buttonSpam: View = rootView.findViewById(R.id.buttonSpam)
        val buttonStar: ImageView = rootView.findViewById(R.id.buttonStar)
        val buttonReaction: ImageView = rootView.findViewById(R.id.buttonReaction)
        val buttonMessageReadPeer: ImageView = rootView.findViewById(R.id.buttonMessageReadPeer)
        val titleView: TextView = rootView.findViewById(R.id.actionModeTitle)
        val reference: WeakReference<ImportantMessagesFragment> = WeakReference(fragment)
        fun show() {
            rootView.visibility = View.VISIBLE
        }

        val isVisible: Boolean
            get() = rootView.visibility == View.VISIBLE

        fun hide() {
            rootView.visibility = View.GONE
            if (Settings.get().main().isMessages_menu_down) {
                reference.get()?.downMenuGroup?.visibility = View.GONE
            }
            reference.get()?.presenter?.fireActionModeDestroy()
        }

        init {
            buttonClose.setOnClickListener {
                hide()
            }
            buttonForward.setOnClickListener {
                reference.get()?.presenter?.fireForwardClick()
                hide()
            }
            buttonCopy.setOnClickListener {
                reference.get()?.presenter?.fireActionModeCopyClick()
                hide()
            }
            buttonDelete.setOnClickListener {
                reference.get()?.presenter?.fireActionModeDeleteClick()
                hide()
            }
            buttonReaction.setOnClickListener {
                reference.get()?.presenter?.fireReactionModeClick()
                hide()
            }
            buttonSpam.setOnClickListener {
                MaterialAlertDialogBuilder(requireActivity())
                    .setIcon(R.drawable.report_red)
                    .setMessage(R.string.do_report)
                    .setTitle(R.string.select)
                    .setPositiveButton(R.string.button_yes) { _, _ ->
                        reference.get()?.presenter?.fireActionModeSpamClick()
                        hide()
                    }
                    .setNeutralButton(R.string.delete) { _, _ ->
                        reference.get()?.presenter?.fireActionModeDeleteClick()
                        hide()
                    }
                    .setCancelable(true)
                    .show()
            }
            buttonMessageReadPeer.setOnClickListener {
                reference.get()?.presenter?.fireMessageReadPeersClick()
                hide()
            }
        }
    }

    companion object {
        fun newInstance(accountId: Long): ImportantMessagesFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            val mFragment = ImportantMessagesFragment()
            mFragment.arguments = args
            return mFragment
        }
    }
}