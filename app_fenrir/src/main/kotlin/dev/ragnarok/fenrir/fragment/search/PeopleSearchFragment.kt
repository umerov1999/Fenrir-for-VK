package dev.ragnarok.fenrir.fragment.search

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.adapter.PeopleAdapter
import dev.ragnarok.fenrir.fragment.search.criteria.PeopleSearchCriteria
import dev.ragnarok.fenrir.getParcelableCompat
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.search.PeopleSearchPresenter
import dev.ragnarok.fenrir.mvp.view.search.IPeopleSearchView
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace

class PeopleSearchFragment :
    AbsSearchFragment<PeopleSearchPresenter, IPeopleSearchView, User, PeopleAdapter>(),
    PeopleAdapter.ClickListener, IPeopleSearchView {
    override fun setAdapterData(adapter: PeopleAdapter, data: MutableList<User>) {
        adapter.setItems(data)
    }

    override fun postCreate(root: View) {}
    override fun createAdapter(data: MutableList<User>): PeopleAdapter {
        val adapter = PeopleAdapter(requireActivity(), data)
        adapter.setClickListener(this)
        return adapter
    }

    override fun createLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<PeopleSearchPresenter> {
        return object : IPresenterFactory<PeopleSearchPresenter> {
            override fun create(): PeopleSearchPresenter {
                return PeopleSearchPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getParcelableCompat(Extra.CRITERIA),
                    saveInstanceState
                )
            }
        }
    }

    override fun onOwnerClick(owner: Owner) {
        presenter?.fireUserClick(
            (owner as User)
        )
    }

    override fun openUserWall(accountId: Int, user: User) {
        getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity())
    }

    companion object {

        fun newInstance(
            accountId: Int,
            initialCriteria: PeopleSearchCriteria?
        ): PeopleSearchFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putParcelable(Extra.CRITERIA, initialCriteria)
            val fragment = PeopleSearchFragment()
            fragment.arguments = args
            return fragment
        }
    }
}