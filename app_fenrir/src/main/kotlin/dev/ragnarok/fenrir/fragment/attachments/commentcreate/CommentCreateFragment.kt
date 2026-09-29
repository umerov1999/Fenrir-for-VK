package dev.ragnarok.fenrir.fragment.attachments.commentcreate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.setToolbarSubtitle
import dev.ragnarok.fenrir.activity.ActivityUtils.setToolbarTitle
import dev.ragnarok.fenrir.fragment.attachments.absattachmentsedit.AbsAttachmentsEditFragment
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.settings.Settings
import kotlin.math.max

class CommentCreateFragment :
    AbsAttachmentsEditFragment<CommentCreatePresenter, ICreateCommentView>(), ICreateCommentView,
    MenuProvider {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
            if (!Settings.get().main().is_side_navigation) {
                val imeFixedBottom =
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                        windowInsets.getInsets(
                            WindowInsetsCompat.Type.ime()
                        ).bottom, insets.bottom
                    ) else insets.bottom
                root.setPadding(0, 0, 0, imeFixedBottom)
            }
            WindowInsetsCompat.CONSUMED
        }
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): CommentCreatePresenter {
        val accountId = requireArguments().getLong(Extra.ACCOUNT_ID)
        val commentDbid = requireArguments().getInt(Extra.COMMENT_ID)
        val sourceOwnerId = requireArguments().getLong(Extra.COMMENT_ID)
        val body = requireArguments().getString(Extra.BODY)
        return CommentCreatePresenter(
            accountId,
            commentDbid,
            sourceOwnerId,
            body,
            saveInstanceState
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_attchments, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.ready) {
            presenter?.fireReadyClick()
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle(this, R.string.new_comment)
        setToolbarSubtitle(this, null)
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(true)
            .build()
            .apply(requireActivity())
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onClearSelection()
        }
    }

    override fun onBackPressed(): Boolean {
        return presenter?.onBackPressed() == true
    }

    override fun returnDataToParent(textBody: String?) {
        val data = Bundle()
        data.putString(Extra.BODY, textBody)
        parentFragmentManager.setFragmentResult(REQUEST_CREATE_COMMENT, data)
    }

    override fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    companion object {
        const val REQUEST_CREATE_COMMENT = "request_create_comment"
        fun newInstance(
            accountId: Long,
            commentDbid: Int,
            sourceOwnerId: Long,
            body: String?
        ): CommentCreateFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putInt(Extra.COMMENT_ID, commentDbid)
            args.putLong(Extra.OWNER_ID, sourceOwnerId)
            args.putString(Extra.BODY, body)
            val fragment = CommentCreateFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResult() {
        presenter?.fireReadyClick()
    }
}