package dev.ragnarok.fenrir.fragment.likes.reactions

import android.os.Bundle
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.fragment.absownerslist.AbsOwnersListFragment
import dev.ragnarok.fenrir.fragment.absownerslist.ISimpleOwnersView
import dev.ragnarok.fenrir.readObjectInteger
import dev.ragnarok.fenrir.writeObjectInteger

class ReactedPeersFragment : AbsOwnersListFragment<ReactedPeersPresenter, ISimpleOwnersView>() {
    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.reacted_peers)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = ReactedPeersPresenter(
        requireArguments().getLong(Extra.ACCOUNT_ID),
        requireArguments().getLong(Extra.PEER_ID),
        requireArguments().getInt(Extra.CMID),
        requireArguments().readObjectInteger(Extra.REACTION_ID),
        saveInstanceState
    )

    override fun hasToolbar(): Boolean {
        return true
    }

    override fun needShowCount(): Boolean {
        return true
    }

    companion object {
        fun buildArgs(
            accountId: Long,
            peerId: Long,
            cmid: Int,
            reactionId: Int?
        ): Bundle {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putLong(Extra.PEER_ID, peerId)
            args.putInt(Extra.CMID, cmid)
            args.writeObjectInteger(Extra.REACTION_ID, reactionId)
            return args
        }

        fun newInstance(args: Bundle): ReactedPeersFragment {
            val fragment = ReactedPeersFragment()
            fragment.arguments = args
            return fragment
        }
    }
}