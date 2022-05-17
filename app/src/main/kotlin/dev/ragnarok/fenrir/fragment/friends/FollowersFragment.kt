package dev.ragnarok.fenrir.fragment.friends

import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.adapter.OwnersAdapter
import dev.ragnarok.fenrir.fragment.AbsOwnersListFragment
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.FollowersPresenter
import dev.ragnarok.fenrir.mvp.view.IFollowersView
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.util.Utils.createAlertRecycleFrame
import dev.ragnarok.fenrir.util.Utils.openPlaceWithSwipebleActivity
import java.util.*

class FollowersFragment : AbsOwnersListFragment<FollowersPresenter, IFollowersView>(),
    IFollowersView {
    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<FollowersPresenter> {
        return object : IPresenterFactory<FollowersPresenter> {
            override fun create(): FollowersPresenter {
                return FollowersPresenter(
                    requireArguments().getInt(
                        Extra.ACCOUNT_ID
                    ),
                    requireArguments().getInt(Extra.USER_ID),
                    saveInstanceState
                )
            }
        }
    }

    override fun notifyRemoved(position: Int) {
        mOwnersAdapter?.notifyItemRemoved(position)
    }

    override fun onLongClick(owner: Owner): Boolean {
        presenter?.removeFollower(
            owner
        )
        return true
    }

    private fun showNotFollowers(data: List<Owner>, accountId: Int) {
        val adapter = OwnersAdapter(requireActivity(), data)
        adapter.setClickListener(object : OwnersAdapter.ClickListener {
            override fun onOwnerClick(owner: Owner) {
                openPlaceWithSwipebleActivity(
                    requireActivity(),
                    getOwnerWallPlace(accountId, owner.ownerId, null)
                )
            }
        })
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(requireActivity().getString(R.string.not_follower))
            .setView(createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
            .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                presenter?.clearModificationFollowers(
                    add = false,
                    not = true
                )
            }
            .setCancelable(false)
            .show()
    }

    override fun showModFollowers(add: List<Owner>?, remove: List<Owner>?, accountId: Int) {
        if (add.isNullOrEmpty() && remove.isNullOrEmpty()) {
            return
        }
        if (add.isNullOrEmpty() && !remove.isNullOrEmpty()) {
            showNotFollowers(remove, accountId)
            return
        }
        val adapter = OwnersAdapter(requireActivity(), add ?: Collections.emptyList())
        adapter.setClickListener(object : OwnersAdapter.ClickListener {
            override fun onOwnerClick(owner: Owner) {
                openPlaceWithSwipebleActivity(
                    requireActivity(),
                    getOwnerWallPlace(accountId, owner.ownerId, null)
                )
            }
        })
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(requireActivity().getString(R.string.new_follower))
            .setView(createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
            .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                presenter?.clearModificationFollowers(
                    add = true,
                    not = false
                )
                if (!remove.isNullOrEmpty()) {
                    showNotFollowers(remove, accountId)
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun hasToolbar(): Boolean {
        return false
    }

    override fun needShowCount(): Boolean {
        return true
    }

    companion object {

        fun newInstance(accountId: Int, userId: Int): FollowersFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putInt(Extra.USER_ID, userId)
            val followersFragment = FollowersFragment()
            followersFragment.arguments = args
            return followersFragment
        }
    }
}