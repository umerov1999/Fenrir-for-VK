package dev.ragnarok.fenrir.fragment.attachments.commentedit

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.setToolbarSubtitle
import dev.ragnarok.fenrir.activity.ActivityUtils.setToolbarTitle
import dev.ragnarok.fenrir.fragment.attachments.absattachmentsedit.AbsAttachmentsEditFragment
import dev.ragnarok.fenrir.getParcelableCompat
import dev.ragnarok.fenrir.model.Comment
import dev.ragnarok.fenrir.settings.Settings
import kotlin.math.max

class CommentEditFragment : AbsAttachmentsEditFragment<CommentEditPresenter, ICommentEditView>(),
    ICommentEditView, MenuProvider {
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

    override fun getPresenterFactory(saveInstanceState: Bundle?): CommentEditPresenter {
        val aid = requireArguments().getLong(Extra.ACCOUNT_ID)
        val CommentThread: Int? =
            if (requireArguments().containsKey(Extra.COMMENT_ID)) requireArguments().getInt(
                Extra.COMMENT_ID
            ) else null
        val comment: Comment = requireArguments().getParcelableCompat(Extra.COMMENT)!!
        return CommentEditPresenter(comment, aid, CommentThread, saveInstanceState)
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
        setToolbarTitle(this, R.string.comment_editing_title)
        setToolbarSubtitle(this, null)
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(true)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun onBackPressed(): Boolean {
        return presenter?.onBackPressed() == true
    }

    override fun goBackWithResult(comment: Comment?) {
        val data = Bundle()
        data.putParcelable(Extra.COMMENT, comment)
        parentFragmentManager.setFragmentResult(REQUEST_COMMENT_EDIT, data)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun showConfirmWithoutSavingDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.confirmation)
            .setMessage(R.string.save_changes_question)
            .setPositiveButton(R.string.button_yes) { _, _ ->
                presenter?.fireReadyClick()
            }
            .setNegativeButton(R.string.button_no) { _, _ ->
                presenter?.fireSavingCancelClick()
            }
            .setNeutralButton(R.string.button_cancel, null)
            .show()
    }

    override fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onResult() {
        presenter?.fireReadyClick()
    }

    companion object {
        const val REQUEST_COMMENT_EDIT = "request_comment_edit"
        fun newInstance(
            accountId: Long,
            comment: Comment?,
            CommentThread: Int?
        ): CommentEditFragment {
            val args = Bundle()
            args.putParcelable(Extra.COMMENT, comment)
            args.putLong(Extra.ACCOUNT_ID, accountId)
            if (CommentThread != null) {
                args.putInt(Extra.COMMENT_ID, CommentThread)
            }
            val fragment = CommentEditFragment()
            fragment.arguments = args
            return fragment
        }
    }
}