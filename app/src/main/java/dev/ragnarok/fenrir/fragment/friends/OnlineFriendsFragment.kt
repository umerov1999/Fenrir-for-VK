package dev.ragnarok.fenrir.fragment.friends

import android.os.Bundle
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.fragment.AbsOwnersListFragment
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.OnlineFriendsPresenter
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView

class OnlineFriendsFragment : AbsOwnersListFragment<OnlineFriendsPresenter, ISimpleOwnersView>() {
    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<OnlineFriendsPresenter> {
        return object : IPresenterFactory<OnlineFriendsPresenter> {
            override fun create(): OnlineFriendsPresenter {
                return OnlineFriendsPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getInt(Extra.USER_ID),
                    saveInstanceState
                )
            }
        }
    }

    override fun hasToolbar(): Boolean {
        return false
    }

    override fun needShowCount(): Boolean {
        return false
    }

    companion object {
        fun newInstance(accountId: Int, userId: Int): OnlineFriendsFragment {
            val bundle = Bundle()
            bundle.putInt(Extra.USER_ID, userId)
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            val friendsFragment = OnlineFriendsFragment()
            friendsFragment.arguments = bundle
            return friendsFragment
        }
    }
}