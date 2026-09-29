package dev.ragnarok.fenrir.fragment.search.abssearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.MainActivity
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment
import dev.ragnarok.fenrir.fragment.search.criteria.BaseSearchCriteria
import dev.ragnarok.fenrir.fragment.search.filteredit.FilterEditFragment
import dev.ragnarok.fenrir.fragment.search.options.BaseOption
import dev.ragnarok.fenrir.getParcelableCompat
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.view.MySearchView
import dev.ragnarok.fenrir.view.MySearchView.OnBackButtonClickListener
import kotlin.math.max

abstract class AbsSearchFragment<P : AbsSearchPresenter<V, *, T, *>, V : IBaseSearchView<T>, T, A : RecyclerView.Adapter<*>> :
    PlaceSupportMvpFragment<P, V>(), IBaseSearchView<T> {
    var mAdapter: A? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mEmptyText: TextView? = null
    protected var recyclerView: RecyclerView? = null
    protected var inTabsContainer = false
    private fun onSearchOptionsChanged() {
        presenter?.fireOptionsChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inTabsContainer = requireArguments().getBoolean(Extra.IN_TABS_CONTAINER)
    }

    open fun createViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    open fun onCreateInsetListener(root: View) {
        if (requireActivity() is MainActivity) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
                WindowInsetsCompat.CONSUMED
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                val imeFixedBottom =
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                        windowInsets.getInsets(
                            WindowInsetsCompat.Type.ime()
                        ).bottom, insets.bottom
                    ) else insets.bottom
                root.findViewById<View>(R.id.actionbar)
                    ?.setPadding(insets.left, 0, insets.right, 0)
                root.findViewById<View>(R.id.toolbar)
                    ?.setPadding(0, insets.top, 0, 0)
                recyclerView?.setPadding(insets.left, 0, insets.right, imeFixedBottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = createViewLayout(inflater, container)
        val toolbar: Toolbar = root.findViewById(R.id.toolbar)
        if (!inTabsContainer) {
            toolbar.visibility = View.VISIBLE
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
            onCreateInsetListener(root)
        } else {
            toolbar.visibility = View.GONE
        }

        val searchView: MySearchView = root.findViewById(R.id.searchview)
        if (savedInstanceState == null) {
            searchView.setQuery(
                arguments?.getParcelableCompat<BaseSearchCriteria>(Extra.CRITERIA)?.query,
                quietly = true,
                isInitial = true
            )
        }
        searchView.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                lazyPresenter {
                    fireTextQueryEdit(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                lazyPresenter {
                    fireTextQueryEdit(newText)
                }
                return false
            }
        })
        searchView.setOnBackButtonClickListener(object : OnBackButtonClickListener {
            override fun onBackButtonClick() {
                if (searchView.text.nonNullNoEmpty() && searchView.text.trimmedNonNullNoEmpty()) {
                    lazyPresenter {
                        fireTextQueryEdit(searchView.text.toString())
                    }
                }
            }
        })
        searchView.setOnAdditionalButtonClickListener(object :
            MySearchView.OnAdditionalButtonClickListener {
            override fun onAdditionalButtonClick() {
                lazyPresenter {
                    fireOpenFilterClick()
                }
            }
        })
        searchView.setLeftIcon(R.drawable.magnify)
        searchView.setLeftIconTint(CurrentTheme.getColorPrimary(requireActivity()))

        recyclerView = root.findViewById(R.id.list)
        recyclerView?.layoutManager = createLayoutManager()
        recyclerView?.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        mAdapter = createAdapter(mutableListOf())
        recyclerView?.adapter = mAdapter
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        mEmptyText = root.findViewById(R.id.empty)
        mEmptyText?.setText(emptyText)
        postCreate(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragmentManager.setFragmentResultListener(
            FilterEditFragment.REQUEST_FILTER_EDIT,
            this
        ) { _: String?, _: Bundle? -> onSearchOptionsChanged() }
    }

    @get:StringRes
    val emptyText: Int
        get() = R.string.list_is_empty

    override fun displayData(data: MutableList<T>) {
        mAdapter?.let {
            setAdapterData(it, data)
        }
    }

    override fun notifyItemChanged(index: Int) {
        mAdapter?.notifyItemChanged(index)
    }

    override fun setEmptyTextVisible(visible: Boolean) {
        mEmptyText?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun showLoading(loading: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = loading
    }

    override fun displayFilter(accountId: Long, options: ArrayList<BaseOption>) {
        val fragment = FilterEditFragment.newInstance(accountId, options)
        fragment.show(parentFragmentManager, "filter-edit")
    }

    override fun onResume() {
        super.onResume()
        if (!inTabsContainer) {
            ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .build()
                .apply(requireActivity())
            if (requireActivity() is OnSectionResumeCallback) {
                (requireActivity() as OnSectionResumeCallback).onClearSelection()
            }
        }
    }

    abstract fun setAdapterData(adapter: A, data: MutableList<T>)
    abstract fun postCreate(root: View)
    abstract fun createAdapter(data: MutableList<T>): A
    abstract fun createLayoutManager(): RecyclerView.LayoutManager
}