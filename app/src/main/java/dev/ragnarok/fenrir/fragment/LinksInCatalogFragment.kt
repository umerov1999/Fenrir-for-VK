package dev.ragnarok.fenrir.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.adapter.CatalogLinksAdapter
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.link.LinkHelper.openUrl
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.LinksInCatalogPresenter
import dev.ragnarok.fenrir.mvp.view.ILinksInCatalogView
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme

class LinksInCatalogFragment : BaseMvpFragment<LinksInCatalogPresenter, ILinksInCatalogView>(),
    ILinksInCatalogView, CatalogLinksAdapter.ActionListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAdapter: CatalogLinksAdapter? = null
    private var Header: String? = null
    private var inTabsContainer = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER)
        Header = requireArguments().getString(Extra.TITLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_catalog_block, container, false)
        val toolbar: Toolbar = root.findViewById(R.id.toolbar)
        if (!inTabsContainer) {
            toolbar.visibility = View.VISIBLE
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        } else {
            toolbar.visibility = View.GONE
        }
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
        recyclerView.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        mAdapter = CatalogLinksAdapter(mutableListOf())
        mAdapter?.setActionListener(this)
        recyclerView.adapter = mAdapter
        return root
    }

    override fun onResume() {
        super.onResume()
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS)
            val actionBar = supportToolbarFor(this)
            if (actionBar != null) {
                actionBar.title = Header
                actionBar.setSubtitle(R.string.links)
            }
            if (requireActivity() is OnSectionResumeCallback) {
                (requireActivity() as OnSectionResumeCallback).onSectionResume(AbsNavigationFragment.SECTION_ITEM_AUDIOS)
            }
            ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity())
        }
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<LinksInCatalogPresenter> {
        return object : IPresenterFactory<LinksInCatalogPresenter> {
            override fun create(): LinksInCatalogPresenter {
                return LinksInCatalogPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getString(Extra.ID)!!,
                    saveInstanceState
                )
            }
        }
    }

    override fun displayList(links: MutableList<Link>) {
        mAdapter?.setItems(links)
    }

    override fun notifyListChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun displayRefreshing(refresing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refresing
    }

    override fun onLinkClick(index: Int, doc: Link) {
        openUrl(requireActivity(), Settings.get().accounts().current, doc.url)
    }

    companion object {
        const val EXTRA_IN_TABS_CONTAINER = "in_tabs_container"
        fun newInstance(accountId: Int, block_id: String?, title: String?): LinksInCatalogFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putString(Extra.ID, block_id)
            args.putString(Extra.TITLE, title)
            val fragment = LinksInCatalogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}