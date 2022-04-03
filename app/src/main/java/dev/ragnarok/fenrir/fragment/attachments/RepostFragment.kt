package dev.ragnarok.fenrir.fragment.attachments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.RepostPresenter
import dev.ragnarok.fenrir.mvp.view.IRepostView

class RepostFragment : AbsAttachmentsEditFragment<RepostPresenter, IRepostView>(), IRepostView {

    override fun goBack() {
        requireActivity().onBackPressed()
    }

    override fun onResult() {
        presenter?.fireReadyClick()
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.share)
            actionBar.subtitle = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_attchments, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.ready) {
            presenter?.fireReadyClick()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<RepostPresenter> {
        return object : IPresenterFactory<RepostPresenter> {
            override fun create(): RepostPresenter {
                val post: Post = requireArguments().getParcelable(EXTRA_POST)!!
                val groupId =
                    if (requireArguments().containsKey(EXTRA_GROUP_ID)) requireArguments().getInt(
                        EXTRA_GROUP_ID
                    ) else null
                val accountId = requireArguments().getInt(Extra.ACCOUNT_ID)
                return RepostPresenter(accountId, post, groupId, saveInstanceState)
            }
        }
    }

    companion object {
        private const val EXTRA_POST = "post"
        private const val EXTRA_GROUP_ID = "group_id"
        fun newInstance(args: Bundle?): RepostFragment {
            val fragment = RepostFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(accountId: Int, gid: Int?, post: Post?): RepostFragment {
            val fragment = RepostFragment()
            fragment.arguments = buildArgs(accountId, gid, post)
            return fragment
        }

        fun buildArgs(accountId: Int, groupId: Int?, post: Post?): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_POST, post)
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            if (groupId != null) {
                bundle.putInt(EXTRA_GROUP_ID, groupId)
            }
            return bundle
        }
    }
}