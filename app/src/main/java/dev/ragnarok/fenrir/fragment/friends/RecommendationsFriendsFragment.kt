package dev.ragnarok.fenrir.fragment.friends

import android.os.Bundle
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.fragment.AbsOwnersListFragment
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.RecommendationsFriendsPresenter
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView

class RecommendationsFriendsFragment :
    AbsOwnersListFragment<RecommendationsFriendsPresenter, ISimpleOwnersView>() {
    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<RecommendationsFriendsPresenter> {
        return object : IPresenterFactory<RecommendationsFriendsPresenter> {
            override fun create(): RecommendationsFriendsPresenter {
                return RecommendationsFriendsPresenter(
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
        fun newInstance(accountId: Int, userId: Int): RecommendationsFriendsFragment {
            val bundle = Bundle()
            bundle.putInt(Extra.USER_ID, userId)
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            val friendsFragment = RecommendationsFriendsFragment()
            friendsFragment.arguments = bundle
            return friendsFragment
        }
    }
}