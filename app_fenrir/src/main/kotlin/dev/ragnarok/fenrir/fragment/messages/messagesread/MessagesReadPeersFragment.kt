package dev.ragnarok.fenrir.fragment.messages.messagesread

import android.os.Bundle
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.fragment.absownerslist.AbsOwnersListFragment
import dev.ragnarok.fenrir.fragment.absownerslist.ISimpleOwnersView

class MessagesReadPeersFragment :
    AbsOwnersListFragment<MessagesReadPeersPresenter, ISimpleOwnersView>() {
    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.read_by)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = MessagesReadPeersPresenter(
        requireArguments().getLong(Extra.ACCOUNT_ID),
        requireArguments().getLong(Extra.PEER_ID),
        requireArguments().getInt(Extra.CM_ID),
        saveInstanceState
    )

    override fun hasToolbar(): Boolean {
        return true
    }

    override fun needShowCount(): Boolean {
        return false
    }

    companion object {
        fun buildArgs(
            accountId: Long,
            peerId: Long,
            cmId: Int
        ): Bundle {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putLong(Extra.PEER_ID, peerId)
            args.putInt(Extra.CM_ID, cmId)
            return args
        }

        fun newInstance(args: Bundle): MessagesReadPeersFragment {
            val fragment = MessagesReadPeersFragment()
            fragment.arguments = args
            return fragment
        }
    }
}