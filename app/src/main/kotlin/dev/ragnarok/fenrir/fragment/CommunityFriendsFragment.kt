package dev.ragnarok.fenrir.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.adapter.PeopleAdapter
import dev.ragnarok.fenrir.fragment.base.BaseMvpBottomSheetDialogFragment
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.CommunityFriendsPresenter
import dev.ragnarok.fenrir.mvp.view.ICommunityFriendsView
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.view.MySearchView

class CommunityFriendsFragment :
    BaseMvpBottomSheetDialogFragment<CommunityFriendsPresenter, ICommunityFriendsView>(),
    ICommunityFriendsView, PeopleAdapter.ClickListener {
    private var mAdapter: PeopleAdapter? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = BottomSheetDialog(requireActivity(), theme)
        val behavior = dialog.behavior
        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        behavior.skipCollapsed = true
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_group_friends, container, false) as ViewGroup
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val mySearchView: MySearchView = root.findViewById(R.id.searchview)
        mySearchView.setRightButtonVisibility(false)
        mySearchView.setLeftIcon(R.drawable.magnify)
        mySearchView.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter?.fireQuery(
                    query
                )
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                presenter?.fireQuery(
                    newText
                )
                return false
            }
        })
        mAdapter = PeopleAdapter(requireActivity(), emptyList())
        mAdapter?.setClickListener(this)
        recyclerView.adapter = mAdapter
        return root
    }

    override fun displayData(users: List<Owner>) {
        mAdapter?.setItems(users)
    }

    override fun notifyItemRemoved(position: Int) {
        mAdapter?.notifyItemRemoved(position)
    }

    override fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun openUserWall(accountId: Int, user: Owner) {
        getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity())
    }

    override fun displayRefreshing(refreshing: Boolean) {}
    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<CommunityFriendsPresenter> {
        return object : IPresenterFactory<CommunityFriendsPresenter> {
            override fun create(): CommunityFriendsPresenter {
                return CommunityFriendsPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getInt(Extra.GROUP_ID),
                    saveInstanceState
                )
            }
        }
    }

    override fun onOwnerClick(owner: Owner) {
        presenter?.fireUserClick(
            owner
        )
    }

    companion object {
        private fun buildArgs(accountId: Int, groupId: Int): Bundle {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putInt(Extra.GROUP_ID, groupId)
            return args
        }

        fun newInstance(accountId: Int, groupId: Int): CommunityFriendsFragment {
            val fragment = CommunityFriendsFragment()
            fragment.arguments = buildArgs(accountId, groupId)
            return fragment
        }
    }
}