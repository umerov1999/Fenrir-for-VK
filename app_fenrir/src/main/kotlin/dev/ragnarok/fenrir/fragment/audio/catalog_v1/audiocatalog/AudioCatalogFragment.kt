package dev.ragnarok.fenrir.fragment.audio.catalog_v1.audiocatalog

import android.Manifest
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.fragment.base.core.IPresenterFactory
import dev.ragnarok.fenrir.fragment.navigation.AbsNavigationFragment
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.AudioCatalog
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosInCatalogBlock
import dev.ragnarok.fenrir.place.PlaceFactory.getLinksInCatalogBlock
import dev.ragnarok.fenrir.place.PlaceFactory.getPlaylistsInCatalogBlock
import dev.ragnarok.fenrir.place.PlaceFactory.getVideosInCatalogBlock
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.DoRequestPermissions
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.util.toast.CustomToast.Companion.createCustomToast
import dev.ragnarok.fenrir.view.MySearchView

class AudioCatalogFragment : BaseMvpFragment<AudioCatalogPresenter, IAudioCatalogView>(),
    IAudioCatalogView, AudioCatalogAdapter.ClickListener, MenuProvider {
    private val requestWritePermission: DoRequestPermissions = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) { createCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text) }
    private var mEmpty: TextView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAdapter: AudioCatalogAdapter? = null
    private var inTabsContainer = false
    private var isArtist = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER)
        isArtist = requireArguments().containsKey(Extra.ARTIST) && !requireArguments().getString(
            Extra.ARTIST
        )
            .isNullOrEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireArguments().containsKey(Extra.ARTIST) && !requireArguments().getString(Extra.ARTIST)
                .isNullOrEmpty()
        ) {
            requireActivity().addMenuProvider(this, viewLifecycleOwner)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_catalog_v1_audio, container, false)
        val toolbar: Toolbar = root.findViewById(R.id.toolbar)
        if (!inTabsContainer) {
            toolbar.visibility = View.VISIBLE
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        } else {
            toolbar.visibility = View.GONE
        }
        mEmpty = root.findViewById(R.id.fragment_audio_catalog_empty_text)
        val mySearchView: MySearchView = root.findViewById(R.id.searchview)
        mySearchView.setRightButtonVisibility(false)
        mySearchView.setLeftIcon(R.drawable.magnify)
        if (!isArtist) {
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
        } else {
            mySearchView.visibility = View.GONE
        }
        val manager: RecyclerView.LayoutManager = LinearLayoutManager(requireActivity())
        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = manager
        recyclerView.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        mAdapter = AudioCatalogAdapter(
            emptyList(),
            presenter?.accountId ?: Settings.get().accounts().current,
            requireActivity()
        )
        mAdapter?.setClickListener(this)
        recyclerView.adapter = mAdapter
        resolveEmptyText()
        return root
    }

    private fun resolveEmptyText() {
        if (mEmpty != null && mAdapter != null) {
            mEmpty?.visibility =
                if (mAdapter?.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS)
            val actionBar = supportToolbarFor(this)
            if (actionBar != null) {
                actionBar.setTitle(R.string.audio_catalog)
                actionBar.subtitle = null
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

    override fun displayData(pages: List<AudioCatalog>) {
        if (mAdapter != null) {
            mAdapter?.setData(pages)
            resolveEmptyText()
        }
    }

    override fun notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter?.notifyDataSetChanged()
            resolveEmptyText()
        }
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        if (mAdapter != null) {
            mAdapter?.notifyItemRangeInserted(position, count)
            resolveEmptyText()
        }
    }

    override fun showRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_share) {
            presenter?.fireRepost(
                requireActivity()
            )
            return true
        }
        return false
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_share_main, menu)
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<AudioCatalogPresenter> {
        return object : IPresenterFactory<AudioCatalogPresenter> {
            override fun create(): AudioCatalogPresenter {
                return AudioCatalogPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getString(Extra.ARTIST),
                    saveInstanceState
                )
            }
        }
    }

    override fun onClick(index: Int, value: AudioCatalog) {
        if (!value.getAudios().isNullOrEmpty()) {
            getAudiosInCatalogBlock(
                presenter?.accountId ?: Settings.get().accounts().current,
                value.getId(),
                value.getTitle()
            ).tryOpenWith(requireActivity())
        } else if (!value.getPlaylists().isNullOrEmpty()) {
            getPlaylistsInCatalogBlock(
                presenter?.accountId ?: Settings.get().accounts().current,
                value.getId(),
                value.getTitle()
            ).tryOpenWith(requireActivity())
        } else if (!value.getVideos().isNullOrEmpty()) {
            getVideosInCatalogBlock(
                presenter?.accountId ?: Settings.get().accounts().current,
                value.getId(),
                value.getTitle()
            ).tryOpenWith(requireActivity())
        } else if (!value.getLinks().isNullOrEmpty()) {
            getLinksInCatalogBlock(
                presenter?.accountId ?: Settings.get().accounts().current,
                value.getId(),
                value.getTitle()
            ).tryOpenWith(requireActivity())
        }
    }

    override fun onAddPlayList(index: Int, album: AudioPlaylist) {
        presenter?.onAdd(
            album
        )
    }

    override fun onRequestWritePermissions() {
        requestWritePermission.launch()
    }

    companion object {
        const val EXTRA_IN_TABS_CONTAINER = "in_tabs_container"
        fun newInstance(
            accountId: Int,
            artist_id: String?,
            isHideToolbar: Boolean
        ): AudioCatalogFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putString(Extra.ARTIST, artist_id)
            args.putBoolean(EXTRA_IN_TABS_CONTAINER, isHideToolbar)
            val fragment = AudioCatalogFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(args: Bundle?): Fragment {
            val fragment = AudioCatalogFragment()
            fragment.arguments = args
            return fragment
        }

        fun buildArgs(accountId: Int, id: String?, isHideToolbar: Boolean): Bundle {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putString(Extra.ARTIST, id)
            args.putBoolean(EXTRA_IN_TABS_CONTAINER, isHideToolbar)
            return args
        }
    }
}