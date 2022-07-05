package dev.ragnarok.fenrir.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.adapter.AudioRecyclerAdapter
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.AudiosInCatalogPresenter
import dev.ragnarok.fenrir.mvp.view.IAudiosInCatalogView
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleURLPhotoPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.DoRequestPermissions
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.util.toast.CustomToast.Companion.createCustomToast

class AudiosInCatalogFragment : BaseMvpFragment<AudiosInCatalogPresenter, IAudiosInCatalogView>(),
    IAudiosInCatalogView {
    private val requestWritePermission: DoRequestPermissions = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) { createCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text) }
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAudioRecyclerAdapter: AudioRecyclerAdapter? = null
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
        val root = inflater.inflate(R.layout.fragment_catalog_music, container, false)
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
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        val Goto: FloatingActionButton = root.findViewById(R.id.goto_button)
        Goto.setImageResource(R.drawable.audio_player)
        Goto.setOnLongClickListener {
            val curr = currentAudio
            if (curr != null) {
                getPlayerPlace(Settings.get().accounts().current).tryOpenWith(requireActivity())
            } else createCustomToast(requireActivity()).showToastError(R.string.null_audio)
            false
        }
        Goto.setOnClickListener {
            val curr = currentAudio
            if (curr != null) {
                val index =
                    presenter?.getAudioPos(curr) ?: -1
                if (index >= 0) {
                    recyclerView.scrollToPosition(
                        index + (mAudioRecyclerAdapter?.headersCount ?: 0)
                    )
                } else createCustomToast(requireActivity()).showToast(R.string.audio_not_found)
            } else createCustomToast(requireActivity()).showToastError(R.string.null_audio)
        }
        mAudioRecyclerAdapter =
            AudioRecyclerAdapter(
                requireActivity(), mutableListOf(),
                not_show_my = false,
                iSSelectMode = false,
                iCatalogBlock = 0,
                playlist_id = null
            )
        mAudioRecyclerAdapter?.setClickListener(object : AudioRecyclerAdapter.ClickListener {
            override fun onClick(position: Int, catalog: Int, audio: Audio) {
                presenter?.playAudio(
                    requireActivity(),
                    position
                )
            }

            override fun onEdit(position: Int, audio: Audio) {}
            override fun onDelete(position: Int) {}
            override fun onUrlPhotoOpen(url: String, prefix: String, photo_prefix: String) {
                getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(requireActivity())
            }

            override fun onRequestWritePermissions() {
                requestWritePermission.launch()
            }
        })
        recyclerView.adapter = mAudioRecyclerAdapter
        return root
    }

    override fun onResume() {
        super.onResume()
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS)
            val actionBar = supportToolbarFor(this)
            if (actionBar != null) {
                actionBar.title = Header
                actionBar.setSubtitle(R.string.music)
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

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<AudiosInCatalogPresenter> {
        return object : IPresenterFactory<AudiosInCatalogPresenter> {
            override fun create(): AudiosInCatalogPresenter {
                return AudiosInCatalogPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getString(Extra.ID)!!,
                    saveInstanceState
                )
            }
        }
    }

    override fun displayList(audios: MutableList<Audio>) {
        mAudioRecyclerAdapter?.setData(audios)
    }

    override fun notifyListChanged() {
        mAudioRecyclerAdapter?.notifyDataSetChanged()
    }

    override fun notifyItemChanged(index: Int) {
        mAudioRecyclerAdapter?.notifyItemBindableChanged(index)
    }

    override fun displayRefreshing(refresing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refresing
    }

    companion object {
        const val EXTRA_IN_TABS_CONTAINER = "in_tabs_container"
        fun newInstance(
            accountId: Int,
            block_id: String?,
            title: String?
        ): AudiosInCatalogFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putString(Extra.ID, block_id)
            args.putString(Extra.TITLE, title)
            val fragment = AudiosInCatalogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}