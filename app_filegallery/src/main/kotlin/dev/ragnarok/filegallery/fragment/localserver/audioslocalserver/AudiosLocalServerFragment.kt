package dev.ragnarok.filegallery.fragment.localserver.audioslocalserver

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.dialog.DialogLocalServerOptionDialog.Companion.newInstance
import dev.ragnarok.filegallery.dialog.DialogLocalServerOptionDialog.DialogLocalServerOptionListener
import dev.ragnarok.filegallery.fragment.base.BaseMvpFragment
import dev.ragnarok.filegallery.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.filegallery.listener.PicassoPauseOnScrollListener
import dev.ragnarok.filegallery.media.music.MusicPlaybackController.canPlayAfterCurrent
import dev.ragnarok.filegallery.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.filegallery.media.music.MusicPlaybackController.playAfterCurrent
import dev.ragnarok.filegallery.media.music.PlayerStatus
import dev.ragnarok.filegallery.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.filegallery.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.filegallery.model.Audio
import dev.ragnarok.filegallery.model.menu.options.AudioLocalServerOption
import dev.ragnarok.filegallery.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.filegallery.toColor
import dev.ragnarok.filegallery.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.filegallery.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.filegallery.util.DownloadWorkUtils.doDownloadAudio
import dev.ragnarok.filegallery.util.Utils
import dev.ragnarok.filegallery.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.filegallery.util.toast.CustomSnackbars
import dev.ragnarok.filegallery.view.MySearchView

class AudiosLocalServerFragment :
    BaseMvpFragment<AudiosLocalServerPresenter, IAudiosLocalServerView>(),
    MySearchView.OnQueryTextListener,
    IAudiosLocalServerView {
    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        presenter?.continueDownload()
    }
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAudiosAdapter: AudiosLocalServerAdapter? = null
    private var searchView: MySearchView? = null
    internal fun clearSearch() {
        searchView?.setOnQueryTextListener(null)
        searchView?.clear()
        searchView?.setOnQueryTextListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_local_server_music, container, false)
        searchView = root.findViewById(R.id.searchview)
        searchView?.setOnQueryTextListener(this)
        searchView?.setRightButtonVisibility(true)
        searchView?.setLeftIcon(R.drawable.magnify)
        searchView?.setRightIcon(R.drawable.dots_vertical)
        searchView?.setOnAdditionalButtonClickListener(object :
            MySearchView.OnAdditionalButtonClickListener {
            override fun onAdditionalButtonClick() {
                presenter?.fireOptions()
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
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        PicassoPauseOnScrollListener.addListener(recyclerView)
        recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        val goTo: FloatingActionButton = root.findViewById(R.id.goto_button)
        goTo.setImageResource(R.drawable.audio_player)
        goTo.setOnLongClickListener {
            val curr = currentAudio
            if (curr != null) {
                getPlayerPlace().tryOpenWith(
                    requireActivity()
                )
            } else customToast?.showToastError(R.string.null_audio)
            false
        }
        goTo.setOnClickListener {
            val curr = currentAudio
            if (curr != null) {
                val index =
                    presenter?.getAudioPos(curr) ?: -1
                if (index >= 0) {
                    recyclerView.scrollToPosition(index)
                } else customToast?.showToast(R.string.audio_not_found)
            } else customToast?.showToastError(R.string.null_audio)
        }
        mAudiosAdapter = AudiosLocalServerAdapter(emptyList())
        mAudiosAdapter?.setClickListener(object : AudiosLocalServerAdapter.ClickListener {
            override fun onClick(position: Int, audio: Audio) {
                presenter?.firePlayAudio(
                    requireActivity(),
                    position
                )
            }

            override fun openMenu(position: Int, audio: Audio) {
                presenter?.fireOpenMenu(position, audio)
            }

            override fun onDownload(position: Int, audio: Audio) {
                presenter?.firePrepareDownload(position, audio)
            }
        })
        recyclerView.adapter = mAudiosAdapter
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = AudiosLocalServerPresenter()

    override fun displayList(audios: List<Audio>) {
        mAudiosAdapter?.setItems(audios)
    }

    override fun notifyListChanged() {
        mAudiosAdapter?.notifyDataSetChanged()
    }

    override fun displayLoading(loading: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = loading
    }

    override fun displayOptionsDialog(isReverse: Boolean, isDiscography: Boolean) {
        newInstance(isDiscography, isReverse, object : DialogLocalServerOptionListener {
            override fun onReverse(reverse: Boolean) {
                presenter?.updateReverse(
                    reverse
                )
            }

            override fun onDiscography(discography: Boolean) {
                clearSearch()
                presenter?.updateDiscography(
                    discography
                )
            }
        }).show(childFragmentManager, "dialog-local-server-options")
    }

    override fun notifyItemChanged(index: Int) {
        mAudiosAdapter?.notifyItemChanged(index)
    }

    override fun notifyItemRemoved(index: Int) {
        mAudiosAdapter?.notifyItemRemoved(index)
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mAudiosAdapter?.notifyItemRangeInserted(position, count)
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

    override fun onServiceBindEvent(@PlayerStatus status: Int) {
        mAudiosAdapter?.onServiceBindEvent(status)
    }

    override fun onDownloadAudioNextStage() {
        if (!hasReadWriteStoragePermission(requireActivity())) {
            requestWritePermission.launch()
            return
        }
        presenter?.continueDownload()
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

    override fun onStartAudioDownload(
        position: Int,
        audio: Audio
    ) {
        audio.downloadIndicator = true
        notifyItemChanged(position)
        val ret = doDownloadAudio(
            requireActivity(),
            audio,
            Force = false
        )
        when (ret) {
            0 -> customToast?.showToastBottom(R.string.saved_audio)

            1 -> {
                CustomSnackbars.createCustomSnackbars(view)
                    ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
                    ?.themedSnack(R.string.audio_force_download)
                    ?.setAction(
                        R.string.button_yes
                    ) {
                        doDownloadAudio(
                            requireActivity(),
                            audio,
                            Force = true
                        )
                    }
                    ?.show()
            }

            else -> {
                audio.downloadIndicator = false
                notifyItemChanged(position)
                customToast?.showToastError(R.string.error_audio)
            }
        }
    }

    override fun onShowBitrate(bitrate: Int, size: Int) {
        CustomSnackbars.createCustomSnackbars(view)
            ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
            ?.themedSnack(getString(R.string.bitrate, bitrate, Utils.BytesToSize(size.toLong())))
            ?.show()
    }

    override fun onOpenMenu(position: Int, audio: Audio) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                AudioLocalServerOption.save_item_audio,
                getString(R.string.download),
                R.drawable.save,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalServerOption.play_item_audio,
                getString(R.string.play),
                R.drawable.play,
                true
            )
        )
        if (canPlayAfterCurrent()) {
            menus.add(
                OptionRequest(
                    AudioLocalServerOption.play_item_after_current_audio,
                    getString(R.string.play_audio_after_current),
                    R.drawable.play_next,
                    false
                )
            )
        }
        menus.add(
            OptionRequest(
                AudioLocalServerOption.bitrate_item_audio,
                getString(R.string.get_bitrate),
                R.drawable.high_quality,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalServerOption.delete_item_audio,
                getString(R.string.delete),
                R.drawable.ic_outline_delete,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalServerOption.update_time_item_audio,
                getString(R.string.update_time),
                R.drawable.ic_recent,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalServerOption.edit_item_audio,
                getString(R.string.edit),
                R.drawable.about_writed,
                true
            )
        )
        menus.header(
            Utils.firstNonEmptyString(audio.artist, " ") + " - " + audio.title,
            R.drawable.song,
            audio.thumb_image
        )
        menus.columns(2)
        menus.show(
            childFragmentManager,
            "audio_options"
        ) { _, option ->
            when (option.id) {
                AudioLocalServerOption.save_item_audio -> {
                    presenter?.firePrepareDownload(position, audio)
                }

                AudioLocalServerOption.play_item_audio -> {
                    presenter?.firePlayAudio(
                        requireActivity(),
                        position
                    )
                }

                AudioLocalServerOption.play_item_after_current_audio -> playAfterCurrent(
                    audio
                )

                AudioLocalServerOption.bitrate_item_audio -> presenter?.fireGetBitrate(audio)

                AudioLocalServerOption.update_time_item_audio -> {
                    presenter?.fireUpdateTime(audio)
                }

                AudioLocalServerOption.edit_item_audio -> {
                    presenter?.firePrepareEditFileName(audio)
                }

                AudioLocalServerOption.delete_item_audio -> MaterialAlertDialogBuilder(
                    requireActivity()
                )
                    .setMessage(R.string.do_delete)
                    .setTitle(R.string.confirmation)
                    .setCancelable(true)
                    .setPositiveButton(R.string.button_yes) { _, _ ->
                        presenter?.fireDeleteAudio(position, audio)
                    }
                    .setNegativeButton(R.string.button_cancel, null)
                    .show()

                else -> {}
            }
        }
    }

    companion object {
        fun newInstance(): AudiosLocalServerFragment {
            return AudiosLocalServerFragment()
        }
    }
}