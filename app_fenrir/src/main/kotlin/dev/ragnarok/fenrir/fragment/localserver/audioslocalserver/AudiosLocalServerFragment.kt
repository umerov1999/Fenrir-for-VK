package dev.ragnarok.fenrir.fragment.localserver.audioslocalserver

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
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.dialog.DialogLocalServerOptionDialog.Companion.newInstance
import dev.ragnarok.fenrir.dialog.DialogLocalServerOptionDialog.DialogLocalServerOptionListener
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.media.music.MusicPlaybackController
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.canPlayAfterCurrent
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playAfterCurrent
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.menu.options.AudioLocalServerOption
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleURLPhotoPlace
import dev.ragnarok.fenrir.toColor
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadAudio
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doSyncRemoteAudio
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.util.toast.CustomSnackbars
import dev.ragnarok.fenrir.view.MySearchView
import dev.ragnarok.fenrir.view.MySearchView.OnAdditionalButtonClickListener
import java.io.IOException

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
        searchView?.setOnAdditionalButtonClickListener(object : OnAdditionalButtonClickListener {
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
                getPlayerPlace(requireArguments().getLong(Extra.ACCOUNT_ID)).tryOpenWith(
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

            override fun onUrlPhotoOpen(url: String, prefix: String, photoPrefix: String) {
                getSingleURLPhotoPlace(url, prefix, photoPrefix).tryOpenWith(requireActivity())
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

    override fun getPresenterFactory(saveInstanceState: Bundle?) = AudiosLocalServerPresenter(
        requireArguments().getLong(Extra.ACCOUNT_ID),
        saveInstanceState
    )

    override fun displayList(audios: List<Audio>) {
        mAudiosAdapter?.setItems(audios)
    }

    override fun notifyListChanged() {
        mAudiosAdapter?.notifyDataSetChanged()
    }

    override fun displayLoading(loading: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = loading
    }

    override fun displayOptionsDialog(accountId: Long, isReverse: Boolean, isDiscography: Boolean) {
        newInstance(accountId, isDiscography, isReverse, object : DialogLocalServerOptionListener {
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

            override fun onRemoteSync() {
                try {
                    MusicPlaybackController.tracksExist.findRemoteAudios(requireActivity())
                    customToast?.showToastSuccessBottom(R.string.success)
                } catch (e: IOException) {
                    customToast?.showToastThrowable(e)
                }
            }

            override fun onRemoteGet() {
                doSyncRemoteAudio(requireActivity())
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
        accountId: Long,
        position: Int,
        audio: Audio
    ) {
        audio.downloadIndicator = 1
        notifyItemChanged(position)
        val ret = doDownloadAudio(
            requireActivity(),
            audio,
            accountId,
            Force = false,
            isLocal = false
        )
        when (ret) {
            0 -> customToast?.showToastBottom(R.string.saved_audio)

            1, 2 -> {
                CustomSnackbars.createCustomSnackbars(view)
                    ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
                    ?.themedSnack(if (ret == 1) R.string.audio_force_download else R.string.audio_force_download_pc)
                    ?.setAction(
                        R.string.button_yes
                    ) {
                        doDownloadAudio(
                            requireActivity(),
                            audio,
                            accountId,
                            Force = true,
                            isLocal = false
                        )
                    }
                    ?.show()
            }

            else -> {
                audio.downloadIndicator = 0
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

    override fun onOpenMenu(accountId: Long, position: Int, audio: Audio) {
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
        menus.add(
            OptionRequest(
                AudioLocalServerOption.upload_item_audio,
                getString(R.string.upload),
                R.drawable.web,
                true
            )
        )
        menus.header(
            Utils.firstNonEmptyString(audio.artist, " ") + " - " + audio.title,
            R.drawable.song,
            audio.thumb_image_little
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

                AudioLocalServerOption.upload_item_audio -> {
                    presenter?.fireUploadAudio(audio)
                }

                else -> {}
            }
        }
    }

    companion object {
        fun newInstance(accountId: Long): AudiosLocalServerFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            val fragment = AudiosLocalServerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}