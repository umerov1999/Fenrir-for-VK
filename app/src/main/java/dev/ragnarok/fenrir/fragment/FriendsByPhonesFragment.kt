package dev.ragnarok.fenrir.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.adapter.OwnersAdapter
import dev.ragnarok.fenrir.adapter.VideoAlbumsNewAdapter
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.FriendsByPhonesPresenter
import dev.ragnarok.fenrir.mvp.view.IFriendsByPhonesView
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme

class FriendsByPhonesFragment : BaseMvpFragment<FriendsByPhonesPresenter, IFriendsByPhonesView>(),
    OwnersAdapter.ClickListener, IFriendsByPhonesView {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAdapter: OwnersAdapter? = null
    private var mEmpty: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_friends_by_phones, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        mEmpty = root.findViewById(R.id.empty)
        recyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        recyclerView.addOnScrollListener(PicassoPauseOnScrollListener(VideoAlbumsNewAdapter.PICASSO_TAG))
        mAdapter = OwnersAdapter(requireActivity(), emptyList())
        mAdapter?.setClickListener(this)
        recyclerView.adapter = mAdapter
        resolveEmptyTextVisibility()
        return root
    }

    override fun displayData(owners: List<Owner>) {
        if (mAdapter != null) {
            mAdapter?.setItems(owners)
            resolveEmptyTextVisibility()
        }
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        if (mAdapter != null) {
            mAdapter?.notifyItemRangeInserted(position, count)
            resolveEmptyTextVisibility()
        }
    }

    override fun displayLoading(loading: Boolean) {
        mSwipeRefreshLayout?.post { mSwipeRefreshLayout?.isRefreshing = loading }
    }

    private fun resolveEmptyTextVisibility() {
        if (mEmpty != null && mAdapter != null) {
            mEmpty?.visibility =
                if (mAdapter?.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    override fun notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter?.notifyDataSetChanged()
            resolveEmptyTextVisibility()
        }
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.friends_by_phone)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<FriendsByPhonesPresenter> {
        return object : IPresenterFactory<FriendsByPhonesPresenter> {
            override fun create(): FriendsByPhonesPresenter {
                val accountId = requireArguments().getInt(Extra.ACCOUNT_ID)
                return FriendsByPhonesPresenter(accountId, requireActivity(), saveInstanceState)
            }
        }
    }

    override fun onOwnerClick(owner: Owner) {
        presenter?.onUserOwnerClicked(
            owner
        )
    }

    override fun showOwnerWall(accountId: Int, owner: Owner) {
        getOwnerWallPlace(accountId, owner).tryOpenWith(requireActivity())
    }

    companion object {
        fun newInstance(args: Bundle?): FriendsByPhonesFragment {
            val fragment = FriendsByPhonesFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(accountId: Int): FriendsByPhonesFragment {
            return newInstance(buildArgs(accountId))
        }

        fun buildArgs(accountId: Int): Bundle {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            return args
        }
    }
}