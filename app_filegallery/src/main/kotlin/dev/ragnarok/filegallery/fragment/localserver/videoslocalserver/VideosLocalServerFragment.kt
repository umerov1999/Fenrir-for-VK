package dev.ragnarok.filegallery.fragment.localserver.videoslocalserver

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.fragment.base.BaseMvpFragment
import dev.ragnarok.filegallery.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.filegallery.listener.PicassoPauseOnScrollListener
import dev.ragnarok.filegallery.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.filegallery.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.filegallery.model.Video
import dev.ragnarok.filegallery.model.menu.options.VideoLocalServerOption
import dev.ragnarok.filegallery.place.PlaceFactory.getInternalPlayerPlace
import dev.ragnarok.filegallery.toColor
import dev.ragnarok.filegallery.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.filegallery.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.filegallery.util.DownloadWorkUtils.doDownloadVideo
import dev.ragnarok.filegallery.util.Utils
import dev.ragnarok.filegallery.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.filegallery.util.toast.CustomSnackbars
import dev.ragnarok.filegallery.view.MySearchView

class VideosLocalServerFragment :
    BaseMvpFragment<VideosLocalServerPresenter, IVideosLocalServerView>(),
    MySearchView.OnQueryTextListener, LocalServerVideosAdapter.VideoOnClickListener,
    IVideosLocalServerView {
    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        presenter?.continueDownload()
    }
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mVideosAdapter: LocalServerVideosAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_local_server_video, container, false)
        val searchView: MySearchView = root.findViewById(R.id.searchview)
        searchView.setOnQueryTextListener(this)
        searchView.setRightButtonVisibility(true)
        searchView.setRightIcon(R.drawable.ic_recent)
        searchView.setLeftIcon(R.drawable.magnify)
        searchView.setOnAdditionalButtonClickListener(object :
            MySearchView.OnAdditionalButtonClickListener {
            override fun onAdditionalButtonClick() {
                presenter?.toggleReverse()
            }
        })
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh(
                false
            )
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        val columns = resources.getInteger(R.integer.videos_column_count)
        val manager = StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = manager
        PicassoPauseOnScrollListener.addListener(recyclerView)
        recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        mVideosAdapter = LocalServerVideosAdapter(emptyList())
        mVideosAdapter?.setVideoOnClickListener(this)
        recyclerView.adapter = mVideosAdapter
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = VideosLocalServerPresenter()

    override fun displayList(videos: List<Video>) {
        mVideosAdapter?.setData(videos)
    }

    override fun notifyListChanged() {
        mVideosAdapter?.notifyDataSetChanged()
    }

    override fun displayLoading(loading: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = loading
    }

    override fun notifyItemChanged(index: Int) {
        mVideosAdapter?.notifyItemChanged(index)
    }

    override fun notifyItemRemoved(index: Int) {
        mVideosAdapter?.notifyItemRemoved(index)
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mVideosAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        presenter?.fireSearchRequestChanged(
            query
        )
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        presenter?.fireSearchRequestChanged(
            newText
        )
        return false
    }

    override fun onVideoClick(position: Int, video: Video) {
        getInternalPlayerPlace(video).tryOpenWith(requireActivity())
    }

    override fun onOpenVideoMenu(position: Int, video: Video) {
        presenter?.fireOpenVideoMenu(position, video)
    }

    override fun doVideoMenu(position: Int, video: Video) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                VideoLocalServerOption.save_item_video,
                getString(R.string.download),
                R.drawable.save,
                true
            )
        )
        menus.add(
            OptionRequest(
                VideoLocalServerOption.play_item_video,
                getString(R.string.play),
                R.drawable.play,
                true
            )
        )
        menus.add(
            OptionRequest(
                VideoLocalServerOption.update_time_item_video,
                getString(R.string.update_time),
                R.drawable.ic_recent,
                false
            )
        )
        menus.add(
            OptionRequest(
                VideoLocalServerOption.delete_item_video,
                getString(R.string.delete),
                R.drawable.ic_outline_delete,
                true
            )
        )
        menus.add(
            OptionRequest(
                VideoLocalServerOption.edit_item_video,
                getString(R.string.edit),
                R.drawable.about_writed,
                true
            )
        )
        menus.header(
            Utils.firstNonEmptyString(video.description, " ") + " - " + video.title,
            R.drawable.video,
            null
        )
        menus.columns(2)
        menus.show(
            childFragmentManager,
            "server_video_options"
        ) { _, option ->
            when (option.id) {
                VideoLocalServerOption.save_item_video -> {
                    presenter?.firePrepareDownload(position, video)
                }

                VideoLocalServerOption.play_item_video -> {
                    getInternalPlayerPlace(video).tryOpenWith(requireActivity())
                }

                VideoLocalServerOption.update_time_item_video -> {
                    presenter?.fireUpdateTime(video)
                }

                VideoLocalServerOption.edit_item_video -> {
                    presenter?.firePrepareEditFileName(video)
                }

                VideoLocalServerOption.delete_item_video -> {
                    MaterialAlertDialogBuilder(
                        requireActivity()
                    )
                        .setMessage(R.string.do_delete)
                        .setTitle(R.string.confirmation)
                        .setCancelable(true)
                        .setPositiveButton(R.string.button_yes) { _, _ ->
                            presenter?.fireDeleteVideo(position, video)
                        }
                        .setNegativeButton(R.string.button_cancel, null)
                        .show()
                }

                else -> {}
            }
        }
    }

    override fun onDownloadVideoNextStage() {
        if (!hasReadWriteStoragePermission(requireActivity())) {
            requestWritePermission.launch()
            return
        }
        presenter?.continueDownload()
    }

    override fun onStartVideoDownload(
        position: Int,
        video: Video
    ) {
        video.link?.let { it1 ->
            doDownloadVideo(
                requireActivity(), video,
                it1, "Local"
            )
        }
    }

    override fun showSuccess() {
        CustomSnackbars.createCustomSnackbars(view)
            ?.setDurationSnack(Snackbar.LENGTH_LONG)
            ?.coloredSnack(
                R.string.success,
                "#48BE2D".toColor(), false
            )
            ?.show()
    }

    override fun displayEditFileNameDialog(
        hash: String,
        fileName: String
    ) {
        val root =
            View.inflate(requireActivity(), R.layout.entry_file_name, null)
        root.findViewById<TextInputEditText>(R.id.edit_file_name).setText(fileName)
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.change_name)
            .setCancelable(true)
            .setView(root)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                val newFileName =
                    root.findViewById<TextInputEditText>(R.id.edit_file_name).text?.toString()
                        ?.trim()
                if (newFileName.isNullOrEmpty()) {
                    return@setPositiveButton
                }
                presenter?.firEditFileName(hash, newFileName)
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }
}