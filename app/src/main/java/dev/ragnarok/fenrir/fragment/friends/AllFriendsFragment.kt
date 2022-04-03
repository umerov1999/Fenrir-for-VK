package dev.ragnarok.fenrir.fragment.friends

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.adapter.FriendsRecycleAdapter
import dev.ragnarok.fenrir.adapter.OwnersAdapter
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.model.UsersPart
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.AllFriendsPresenter
import dev.ragnarok.fenrir.mvp.view.IAllFriendsView
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.util.Utils.createAlertRecycleFrame
import dev.ragnarok.fenrir.util.Utils.openPlaceWithSwipebleActivity
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.view.MySearchView
import java.util.*

class AllFriendsFragment : BaseMvpFragment<AllFriendsPresenter, IAllFriendsView>(),
    FriendsRecycleAdapter.Listener, IAllFriendsView {
    private var mAdapter: FriendsRecycleAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val root = inflater.inflate(R.layout.fragment_friends, container, false)
        val mRecyclerView: RecyclerView = root.findViewById(R.id.list)
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        val manager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView.layoutManager = manager
        mRecyclerView.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        mRecyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        val mySearchView: MySearchView = root.findViewById(R.id.searchview)
        mySearchView.setRightButtonVisibility(false)
        mySearchView.setLeftIcon(R.drawable.magnify)
        mySearchView.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter?.fireSearchRequestChanged(
                    query
                )
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                presenter?.fireSearchRequestChanged(
                    newText
                )
                return false
            }
        })
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(mySearchView.windowToken, 0)
        mAdapter = FriendsRecycleAdapter(emptyList(), requireActivity())
        mAdapter?.setListener(this)
        mRecyclerView.adapter = mAdapter
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<AllFriendsPresenter> {
        return object : IPresenterFactory<AllFriendsPresenter> {
            override fun create(): AllFriendsPresenter {
                return AllFriendsPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getInt(Extra.USER_ID), saveInstanceState
                )
            }
        }
    }

    override fun notifyDatasetChanged(grouping: Boolean) {
        mAdapter?.setGroup(grouping)
        mAdapter?.notifyDataSetChanged()
    }

    override fun setSwipeRefreshEnabled(enabled: Boolean) {
        mSwipeRefreshLayout?.isEnabled = enabled
    }

    override fun displayData(data: List<UsersPart>, grouping: Boolean) {
        mAdapter?.setData(data, grouping)
    }

    override fun notifyItemRangeInserted(position: Int, count: Int) {
        mAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun showUserWall(accountId: Int, user: User) {
        getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity())
    }

    override fun showRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    private fun showNotFriends(data: List<Owner>, accountId: Int) {
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
            .setTitle(requireActivity().getString(R.string.not_friend))
            .setView(createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
            .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                presenter?.clearModificationFriends(
                    add = false,
                    not = true
                )
            }
            .setCancelable(false)
            .show()
    }

    override fun showModFriends(add: List<Owner>?, remove: List<Owner>?, accountId: Int) {
        if (add.isNullOrEmpty() && remove.isNullOrEmpty()) {
            return
        }
        if (add.isNullOrEmpty() && !remove.isNullOrEmpty()) {
            showNotFriends(remove, accountId)
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
            .setTitle(requireActivity().getString(R.string.new_friend))
            .setView(createAlertRecycleFrame(requireActivity(), adapter, null, accountId))
            .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                presenter?.clearModificationFriends(
                    add = true,
                    not = false
                )
                if (!remove.isNullOrEmpty()) {
                    showNotFriends(remove, accountId)
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onUserClick(user: User) {
        presenter?.fireUserClick(
            user
        )
    }

    companion object {

        fun newInstance(accountId: Int, userId: Int): AllFriendsFragment {
            val args = Bundle()
            args.putInt(Extra.USER_ID, userId)
            args.putInt(Extra.ACCOUNT_ID, accountId)
            val allFriendsFragment = AllFriendsFragment()
            allFriendsFragment.arguments = args
            return allFriendsFragment
        }
    }
}