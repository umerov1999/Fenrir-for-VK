package dev.ragnarok.fenrir.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.adapter.AttachmentsHolder
import dev.ragnarok.fenrir.adapter.AttachmentsHolder.Companion.forPost
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria
import dev.ragnarok.fenrir.link.LinkHelper
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper
import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.WallPostPresenter
import dev.ragnarok.fenrir.mvp.view.IWallPostView
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleTabSearchPlace
import dev.ragnarok.fenrir.place.PlaceUtil.goToPostEditor
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppTextUtils.getDateFromUnixTime
import dev.ragnarok.fenrir.util.ViewUtils.displayAvatar
import dev.ragnarok.fenrir.view.CircleCounterButton
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView

class WallPostFragment : PlaceSupportMvpFragment<WallPostPresenter, IWallPostView>(),
    EmojiconTextView.OnHashTagClickListener, IWallPostView, MenuProvider {
    private var mSignerNameText: TextView? = null
    private var mSignerRootView: View? = null
    private var mSignerAvatar: ImageView? = null
    private var mShareButton: CircleCounterButton? = null
    private var mCommentsButton: CircleCounterButton? = null
    private var mLikeButton: CircleCounterButton? = null
    private var mText: EmojiconTextView? = null
    private var attachmentsViewBinder: AttachmentsViewBinder? = null
    private var transformation: Transformation? = null
    private var root: ViewGroup? = null
    private var mAttachmentsViews: AttachmentsHolder? = null
    private var mTextSelectionAllowed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachmentsViewBinder = AttachmentsViewBinder(requireActivity(), this)
        attachmentsViewBinder?.setOnHashTagClickListener(this)
        transformation = CurrentTheme.createTransformationForAvatar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun displayPinComplete(pin: Boolean) {
        Toast.makeText(
            requireActivity(),
            if (pin) R.string.pin_result else R.string.unpin_result,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun displayDeleteOrRestoreComplete(deleted: Boolean) {
        Toast.makeText(
            requireActivity(),
            if (deleted) R.string.delete_result else R.string.restore_result,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onClearSelection()
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val pRoot = inflater.inflate(R.layout.fragment_post, container, false) as ViewGroup
        mAttachmentsViews = forPost(pRoot)
        (requireActivity() as AppCompatActivity).setSupportActionBar(pRoot.findViewById(R.id.toolbar))
        mShareButton = pRoot.findViewById(R.id.share_button)
        mCommentsButton = pRoot.findViewById(R.id.comments_button)
        mLikeButton = pRoot.findViewById(R.id.like_button)
        mText = pRoot.findViewById(R.id.fragment_post_text)
        mText?.movementMethod = LinkMovementMethod.getInstance()
        mText?.setOnHashTagClickListener(this)
        mSignerRootView = pRoot.findViewById(R.id.item_post_signer_root)
        mSignerAvatar = pRoot.findViewById(R.id.item_post_signer_icon)
        mSignerNameText = pRoot.findViewById(R.id.item_post_signer_name)
        mLikeButton?.setOnClickListener {
            presenter?.fireLikeClick()
        }
        mLikeButton?.setOnLongClickListener {
            presenter?.fireLikeLongClick()
            true
        }
        mShareButton?.setOnClickListener {
            presenter?.fireShareClick()
        }
        mShareButton?.setOnLongClickListener {
            presenter?.fireRepostLongClick()
            true
        }
        pRoot.findViewById<View>(R.id.try_again_button).setOnClickListener {
            presenter?.fireTryLoadAgainClick()
        }
        mCommentsButton?.setOnClickListener {
            presenter?.fireCommentClick()
        }
        resolveTextSelection()
        root = pRoot
        return pRoot
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.delete_post -> {
                presenter?.fireDeleteClick()
                return true
            }
            R.id.restore_post -> {
                presenter?.fireRestoreClick()
                return true
            }
            R.id.pin_post -> {
                presenter?.firePinClick()
                return true
            }
            R.id.unpin_post -> {
                presenter?.fireUnpinClick()
                return true
            }
            R.id.goto_user_post -> {
                presenter?.fireGoToOwnerClick()
                return true
            }
            R.id.copy_url_post -> {
                presenter?.fireCopyLinkClink()
                return true
            }
            R.id.report -> {
                presenter?.fireReport()
                return true
            }
            R.id.copy_text -> {
                presenter?.fireCopyTextClick()
                return true
            }
            R.id.action_allow_text_selection -> {
                mTextSelectionAllowed = true
                resolveTextSelection()
                requireActivity().invalidateOptionsMenu()
                return true
            }
            R.id.add_to_bookmarks -> {
                presenter?.fireBookmark()
                return true
            }
            R.id.edit_post -> {
                presenter?.firePostEditClick()
                return true
            }
            R.id.refresh -> {
                presenter?.fireRefresh()
                return true
            }
            else -> return false
        }
    }

    override fun showSuccessToast() {
        Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
    }

    override fun copyLinkToClipboard(link: String?) {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(getString(R.string.link), link)
        clipboard?.setPrimaryClip(clip)
        customToast.showToast(R.string.copied_url)
    }

    override fun copyTextToClipboard(text: String?) {
        val manager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText(getString(R.string.post_text), text)
        manager?.setPrimaryClip(clipData)
        customToast.showToast(R.string.copied_text)
    }

    private fun resolveTextSelection() {
        val copiesRoot = mAttachmentsViews?.vgPosts ?: return
        mText?.setTextIsSelectable(mTextSelectionAllowed)
        for (i in 0 until copiesRoot.childCount) {
            val copyRoot = copiesRoot.getChildAt(i) as ViewGroup
            val textView = copyRoot.findViewById<TextView>(R.id.item_post_copy_text)
            textView?.setTextIsSelectable(mTextSelectionAllowed)
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        val optionView = OptionView()
        presenter?.fireOptionViewCreated(
            optionView
        )
        menu.findItem(R.id.edit_post).isVisible = optionView.pCanEdit
        menu.findItem(R.id.unpin_post).isVisible = optionView.pCanUnpin
        menu.findItem(R.id.pin_post).isVisible = optionView.pCanPin
        menu.findItem(R.id.delete_post).isVisible = optionView.pCanDelete
        menu.findItem(R.id.restore_post).isVisible = optionView.pCanRestore
        menu.findItem(R.id.action_allow_text_selection).isVisible = !mTextSelectionAllowed
        menu.findItem(R.id.add_to_bookmarks)
            .setTitle(if (!optionView.pInFave) R.string.add_to_bookmarks else R.string.remove_from_bookmarks)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.single_post_menu, menu)
    }

    /*private boolean canEdit() {
        return post.isCanEdit();

        boolean canEditAsAdmin = false;

        if(owner != null && owner.admin_level >= VKApiCommunity.AdminLevel.EDITOR){
            if(owner.type == VKApiCommunity.Type.GROUP){
                // нельзя редактировать чужие посты в GROUP
                canEditAsAdmin = post.getCreatorId() == getAccountId() && post.getSignerId() == getAccountId();
            }

            if(owner.type == VKApiCommunity.Type.PAGE){
                canEditAsAdmin = true;
            }
        }

        boolean canEdit = post.getAuthorId() == getAccountId() || canEditAsAdmin;

        if (!canEdit) {
            return false;
        }

        long currentUnixtime = System.currentTimeMillis() / 1000;
        return (currentUnixtime - post.getDate()) < Constants.HOURS_24_IN_SECONDS;
    }*/
    override fun displayDefaultToolbarTitle() {
        setToolbarTitle(getString(R.string.wall_post))
    }

    override fun displayToolbarTitle(title: String?) {
        setToolbarTitle(title)
    }

    override fun displayToolbarSubtitle(subtitleType: Int, datetime: Long) {
        val formattedDate = getDateFromUnixTime(requireActivity(), datetime)
        when (subtitleType) {
            IWallPostView.SUBTITLE_NORMAL -> setToolbarSubtitle(formattedDate)
            IWallPostView.SUBTITLE_STATUS_UPDATE -> setToolbarSubtitle(
                getString(
                    R.string.updated_status_at,
                    formattedDate
                )
            )
            IWallPostView.SUBTITLE_PHOTO_UPDATE -> setToolbarSubtitle(
                getString(
                    R.string.updated_profile_photo_at,
                    formattedDate
                )
            )
        }
    }

    override fun displayDefaultToolbarSubtitle() {
        setToolbarSubtitle(null)
    }

    override fun displayPostInfo(post: Post) {
        val pRoot = root ?: return

        if (post.isDeleted) {
            pRoot.findViewById<View>(R.id.fragment_post_deleted).visibility = View.VISIBLE
            pRoot.findViewById<View>(R.id.post_content).visibility = View.GONE
            pRoot.findViewById<View>(R.id.post_loading_root).visibility = View.GONE
            return
        }
        pRoot.findViewById<View>(R.id.fragment_post_deleted).visibility = View.GONE
        pRoot.findViewById<View>(R.id.post_content).visibility = View.VISIBLE
        pRoot.findViewById<View>(R.id.post_loading_root).visibility = View.GONE
        mText?.visibility = if (post.hasText()) View.VISIBLE else View.GONE
        val spannableText =
            OwnerLinkSpanFactory.withSpans(
                post.text,
                owners = true,
                topics = false,
                listener = object : LinkActionAdapter() {
                    override fun onOwnerClick(ownerId: Int) {
                        onOpenOwner(ownerId)
                    }

                    override fun onOtherClick(URL: String) {
                        LinkHelper.openUrl(
                            requireActivity(),
                            Settings.get().accounts().current,
                            URL, false
                        )
                    }
                })
        mText?.setText(spannableText, TextView.BufferType.SPANNABLE)
        val displaySigner = post.signerId > 0 && post.creator != null
        mSignerRootView?.visibility = if (displaySigner) View.VISIBLE else View.GONE
        if (displaySigner) {
            val creator = post.creator
            mSignerNameText?.text = creator?.fullName
            displayAvatar(
                mSignerAvatar,
                transformation,
                creator?.get100photoOrSmaller(),
                Constants.PICASSO_TAG
            )
            mSignerRootView?.setOnClickListener {
                onOpenOwner(
                    post.signerId
                )
            }
        }
        mAttachmentsViews?.let {
            attachmentsViewBinder?.displayAttachments(
                post.attachments,
                it,
                false,
                null,
                null
            )
        }
        attachmentsViewBinder?.displayCopyHistory(
            post.getCopyHierarchy(), mAttachmentsViews?.vgPosts,
            false, R.layout.item_copy_history_post
        )
    }

    override fun displayLoading() {
        val pRoot = root ?: return
        pRoot.findViewById<View>(R.id.fragment_post_deleted).visibility = View.GONE
        pRoot.findViewById<View>(R.id.post_content).visibility = View.GONE
        pRoot.findViewById<View>(R.id.post_loading_root).visibility = View.VISIBLE
        pRoot.findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
        pRoot.findViewById<View>(R.id.post_loading_text).visibility =
            View.VISIBLE
        pRoot.findViewById<View>(R.id.try_again_button).visibility = View.GONE
    }

    override fun displayLoadingFail() {
        val pRoot = root ?: return
        pRoot.findViewById<View>(R.id.fragment_post_deleted).visibility = View.GONE
        pRoot.findViewById<View>(R.id.post_content).visibility = View.GONE
        pRoot.findViewById<View>(R.id.post_loading_root).visibility =
            View.VISIBLE
        pRoot.findViewById<View>(R.id.progressBar).visibility = View.GONE
        pRoot.findViewById<View>(R.id.post_loading_text).visibility = View.GONE
        pRoot.findViewById<View>(R.id.try_again_button).visibility =
            View.VISIBLE
    }

    override fun displayLikes(count: Int, userLikes: Boolean) {
        mLikeButton?.let {
            it.isActive = userLikes
            it.count = count
            it.setIcon(if (userLikes) R.drawable.heart_filled else R.drawable.heart)
        }
    }

    override fun setCommentButtonVisible(visible: Boolean) {
        mCommentsButton?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    override fun displayCommentCount(count: Int) {
        mCommentsButton?.count = count
    }

    override fun displayReposts(count: Int, userReposted: Boolean) {
        mShareButton?.let {
            it.count = count
            it.isActive = userReposted
        }
    }

    override fun goToPostEditing(accountId: Int, post: Post) {
        goToPostEditor(requireActivity(), accountId, post)
    }

    override fun showPostNotReadyToast() {
        Toast.makeText(
            requireActivity(),
            R.string.wall_post_is_not_yet_initialized,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<WallPostPresenter> {
        return object : IPresenterFactory<WallPostPresenter> {
            override fun create(): WallPostPresenter {
                val wrapper: ParcelableOwnerWrapper? = requireArguments().getParcelable(Extra.OWNER)
                return WallPostPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getInt(Extra.POST_ID),
                    requireArguments().getInt(Extra.OWNER_ID),
                    requireArguments().getParcelable(Extra.POST),
                    wrapper?.get(),
                    requireActivity(),
                    saveInstanceState
                )
            }
        }
    }

    override fun goToNewsSearch(accountId: Int, hashTag: String?) {
        val criteria = NewsFeedCriteria(hashTag)
        getSingleTabSearchPlace(accountId, SearchContentType.NEWS, criteria).tryOpenWith(
            requireActivity()
        )
    }

    override fun onHashTagClicked(hashTag: String) {
        presenter?.fireHasgTagClick(
            hashTag
        )
    }

    private class OptionView : IWallPostView.IOptionView {
        var pCanDelete = false
        var pCanRestore = false
        var pCanPin = false
        var pCanUnpin = false
        var pCanEdit = false
        var pInFave = false
        override fun setCanDelete(can: Boolean) {
            pCanDelete = can
        }

        override fun setCanRestore(can: Boolean) {
            pCanRestore = can
        }

        override fun setCanPin(can: Boolean) {
            pCanPin = can
        }

        override fun setCanUnpin(can: Boolean) {
            pCanUnpin = can
        }

        override fun setCanEdit(can: Boolean) {
            pCanEdit = can
        }

        override fun setInFave(inTo: Boolean) {
            pInFave = inTo
        }
    }

    companion object {
        fun newInstance(args: Bundle?): WallPostFragment {
            val fragment = WallPostFragment()
            fragment.arguments = args
            return fragment
        }

        fun buildArgs(accountId: Int, postId: Int, ownerId: Int, post: Post?): Bundle {
            val bundle = Bundle()
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            bundle.putInt(Extra.POST_ID, postId)
            bundle.putInt(Extra.OWNER_ID, ownerId)
            bundle.putParcelable(Extra.POST, post)
            return bundle
        }
    }
}