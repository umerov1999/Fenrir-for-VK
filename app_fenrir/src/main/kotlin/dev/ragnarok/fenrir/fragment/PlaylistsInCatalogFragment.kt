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
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.adapter.AudioPlaylistsAdapter
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.PlaylistsInCatalogPresenter
import dev.ragnarok.fenrir.mvp.view.IPlaylistsInCatalogView
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosInAlbumPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme

class PlaylistsInCatalogFragment :
    BaseMvpFragment<PlaylistsInCatalogPresenter, IPlaylistsInCatalogView>(),
    IPlaylistsInCatalogView, AudioPlaylistsAdapter.ClickListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAdapter: AudioPlaylistsAdapter? = null
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
        val columnCount = resources.getInteger(R.integer.photos_albums_column_count)
        recyclerView.layoutManager = GridLayoutManager(requireActivity(), columnCount)
        recyclerView.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        mAdapter = AudioPlaylistsAdapter(emptyList(), requireActivity(), false)
        mAdapter?.setClickListener(this)
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
                actionBar.setSubtitle(R.string.playlists)
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

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<PlaylistsInCatalogPresenter> {
        return object : IPresenterFactory<PlaylistsInCatalogPresenter> {
            override fun create(): PlaylistsInCatalogPresenter {
                return PlaylistsInCatalogPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getString(Extra.ID)!!,
                    saveInstanceState
                )
            }
        }
    }

    override fun displayList(audios: List<AudioPlaylist>) {
        mAdapter?.setData(audios)
    }

    override fun notifyListChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun displayRefreshing(refresing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refresing
    }

    override fun onAlbumClick(index: Int, album: AudioPlaylist) {
        if (album.getOriginal_access_key()
                .isNullOrEmpty() || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0
        ) getAudiosInAlbumPlace(
            presenter?.accountId ?: Settings.get().accounts().current,
            album.getOwnerId(),
            album.getId(),
            album.getAccess_key()
        ).tryOpenWith(requireActivity()) else getAudiosInAlbumPlace(
            presenter?.accountId ?: Settings.get().accounts().current,
            album.getOriginal_owner_id(),
            album.getOriginal_id(),
            album.getOriginal_access_key()
        ).tryOpenWith(requireActivity())
    }

    override fun onOpenClick(index: Int, album: AudioPlaylist) {
        if (album.getOriginal_access_key()
                .isNullOrEmpty() || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0
        ) getAudiosInAlbumPlace(
            presenter?.accountId ?: Settings.get().accounts().current,
            album.getOwnerId(),
            album.getId(),
            album.getAccess_key()
        ).tryOpenWith(requireActivity()) else getAudiosInAlbumPlace(
            presenter?.accountId ?: Settings.get().accounts().current,
            album.getOriginal_owner_id(),
            album.getOriginal_id(),
            album.getOriginal_access_key()
        ).tryOpenWith(requireActivity())
    }

    override fun onDelete(index: Int, album: AudioPlaylist) {}
    override fun onShare(index: Int, album: AudioPlaylist) {
        startForSendAttachments(
            requireActivity(),
            presenter?.accountId ?: Settings.get().accounts().current,
            album
        )
    }

    override fun onEdit(index: Int, album: AudioPlaylist) {}
    override fun onAddAudios(index: Int, album: AudioPlaylist) {}
    override fun onAdd(index: Int, album: AudioPlaylist, clone: Boolean) {
        presenter?.onAdd(
            album,
            clone
        )
    }

    companion object {
        const val EXTRA_IN_TABS_CONTAINER = "in_tabs_container"
        fun newInstance(
            accountId: Int,
            block_id: String?,
            title: String?
        ): PlaylistsInCatalogFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putString(Extra.ID, block_id)
            args.putString(Extra.TITLE, title)
            val fragment = PlaylistsInCatalogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}