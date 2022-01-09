package dev.ragnarok.fenrir.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils
import dev.ragnarok.fenrir.adapter.MessagesAdapter
import dev.ragnarok.fenrir.adapter.MessagesAdapter.OnMessageActionListener
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.Option
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Keyboard
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.LocalJsonToChatPresenter
import dev.ragnarok.fenrir.mvp.view.ILocalJsonToChatView
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils

class LocalJsonToChatFragment :
    PlaceSupportMvpFragment<LocalJsonToChatPresenter, ILocalJsonToChatView>(), ILocalJsonToChatView,
    OnMessageActionListener {
    private var mEmpty: TextView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAdapter: MessagesAdapter? = null
    private var recyclerView: RecyclerView? = null

    private var Title: TextView? = null
    private var SubTitle: TextView? = null
    private var Avatar: ImageView? = null
    private var EmptyAvatar: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_json_chat, container, false)
        root.background = CurrentTheme.getChatBackground(activity)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        mEmpty = root.findViewById(R.id.empty)
        val mAttachment: FloatingActionButton = root.findViewById(R.id.goto_button)
        mAttachment.setOnClickListener { presenter?.toggleAttachment() }
        mAttachment.setOnLongClickListener {
            val my = presenter?.updateMessages(true) ?: return@setOnLongClickListener false
            mAttachment.setImageResource(if (my) R.drawable.account_circle else R.drawable.attachment)
            true
        }

        Title = root.findViewById(R.id.dialog_title)
        SubTitle = root.findViewById(R.id.dialog_subtitle)
        Avatar = root.findViewById(R.id.toolbar_avatar)
        EmptyAvatar = root.findViewById(R.id.empty_avatar_text)

        recyclerView = root.findViewById(android.R.id.list)
        recyclerView?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, true)
        recyclerView?.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.isEnabled = false
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        mAdapter = MessagesAdapter(requireActivity(), emptyList(), this, true)
        recyclerView?.adapter = mAdapter
        resolveEmptyText()
        return root
    }

    @DrawableRes
    private fun is_select(@DrawableRes res: Int, id: Int, selected: Int): Int {
        if (id == selected) {
            return R.drawable.check
        }
        return res
    }

    override fun attachments_mode(accountId: Int, last_selected: Int) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                0,
                getString(R.string.json_all_messages),
                is_select(R.drawable.close, 0, last_selected),
                false
            )
        )
        menus.add(
            OptionRequest(
                1,
                getString(R.string.photos),
                is_select(R.drawable.photo_album, 1, last_selected),
                true
            )
        )
        menus.add(
            OptionRequest(
                2,
                getString(R.string.videos),
                is_select(R.drawable.video, 2, last_selected),
                true
            )
        )
        menus.add(
            OptionRequest(
                3,
                getString(R.string.documents),
                is_select(R.drawable.book, 3, last_selected),
                true
            )
        )
        menus.add(
            OptionRequest(
                4,
                getString(R.string.music),
                is_select(R.drawable.song, 4, last_selected),
                true
            )
        )
        menus.add(
            OptionRequest(
                5,
                getString(R.string.links),
                is_select(R.drawable.web, 5, last_selected),
                true
            )
        )
        menus.add(
            OptionRequest(
                6,
                getString(R.string.photo_album),
                is_select(R.drawable.album_photo, 6, last_selected),
                false
            )
        )
        menus.add(
            OptionRequest(
                7,
                getString(R.string.playlist),
                is_select(R.drawable.audio_player, 7, last_selected),
                true
            )
        )
        menus.add(
            OptionRequest(
                8,
                getString(R.string.json_attachments_forward),
                is_select(R.drawable.ic_outline_forward, 8, last_selected),
                true
            )
        )
        menus.add(
            OptionRequest(
                9,
                getString(R.string.posts),
                is_select(R.drawable.about_writed, 9, last_selected),
                true
            )
        )
        menus.add(
            OptionRequest(
                10,
                getString(R.string.json_all_attachments),
                is_select(R.drawable.attachment, 10, last_selected),
                false
            )
        )

        menus.show(childFragmentManager, "json_attachments_select",
            object : ModalBottomSheetDialogFragment.Listener {
                override fun onModalOptionSelected(option: Option) {
                    presenter?.uAttachmentType = option.id
                    presenter?.updateMessages(false)
                }
            })
    }

    override fun scroll_pos(pos: Int) {
        recyclerView?.scrollToPosition(pos)
    }

    private fun resolveEmptyText() {
        mEmpty?.visibility = if (mAdapter?.itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun displayData(posts: List<Message>) {
        mAdapter?.items = posts
        resolveEmptyText()
    }

    override fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
        resolveEmptyText()
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mAdapter?.notifyItemRangeInserted(position, count)
        resolveEmptyText()
    }

    override fun showRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<LocalJsonToChatPresenter> =
        object : IPresenterFactory<LocalJsonToChatPresenter> {
            override fun create(): LocalJsonToChatPresenter {
                return LocalJsonToChatPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireActivity(),
                    saveInstanceState
                )
            }
        }

    override fun displayToolbarAvatar(peer: Peer) {
        Avatar?.setOnClickListener {
            presenter?.fireOwnerClick(peer.id)
        }
        if (Utils.nonEmpty(peer.avaUrl)) {
            EmptyAvatar?.visibility = View.GONE
            Avatar?.let {
                PicassoInstance.with()
                    .load(peer.avaUrl)
                    .transform(RoundTransformation())
                    .into(it)
            }
        } else {
            Avatar?.let { PicassoInstance.with().cancelRequest(it) }
            if (!Utils.isEmpty(peer.title)) {
                EmptyAvatar?.visibility = View.VISIBLE
                var name: String = peer.title
                if (name.length > 2) name = name.substring(0, 2)
                name = name.trim { it <= ' ' }
                EmptyAvatar?.text = name
            } else {
                EmptyAvatar?.visibility = View.GONE
            }
            Avatar?.setImageBitmap(
                RoundTransformation().localTransform(
                    Utils.createGradientChatImage(
                        200,
                        200,
                        peer.id
                    )
                )
            )
        }
    }

    override fun setToolbarTitle(title: String?) {
        ActivityUtils.supportToolbarFor(this)?.title = null
        Title?.text = title
    }

    override fun setToolbarSubtitle(subtitle: String?) {
        ActivityUtils.supportToolbarFor(this)?.subtitle = null
        SubTitle?.text = subtitle
    }

    override fun onResume() {
        super.onResume()
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun onAvatarClick(message: Message, userId: Int, position: Int) {
        presenter?.fireOwnerClick(userId)
    }

    override fun onLongAvatarClick(message: Message, userId: Int, position: Int) {}
    override fun onRestoreClick(message: Message, position: Int) {}
    override fun onBotKeyboardClick(button: Keyboard.Button) {}

    override fun onMessageLongClick(message: Message, position: Int): Boolean {
        return false
    }

    override fun onMessageClicked(message: Message, position: Int) {}
    override fun onMessageDelete(message: Message) {}

    companion object {
        @JvmStatic
        fun newInstance(accountId: Int): LocalJsonToChatFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            val fragment = LocalJsonToChatFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
