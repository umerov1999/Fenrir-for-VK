package dev.ragnarok.fenrir.fragment.audio.local.audioslocal

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.audio.local.localaudioalbums.LocalAudioAlbumsFragment
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.fragment.docs.DocsUploadAdapter
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.canPlayAfterCurrent
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playAfterCurrent
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.menu.options.AudioLocalOption
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.toColor
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.util.AppPerms.DoRequestPermissions
import dev.ragnarok.fenrir.util.AppPerms.hasReadStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsResultAbs
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.util.toast.CustomSnackbars
import dev.ragnarok.fenrir.view.MySearchView
import dev.ragnarok.fenrir.view.MySearchView.OnAdditionalButtonClickListener

class AudiosLocalFragment : BaseMvpFragment<AudiosLocalPresenter, IAudiosLocalView>(),
    MySearchView.OnQueryTextListener, DocsUploadAdapter.ActionListener,
    AudiosLocalAdapter.ClickListener, IAudiosLocalView {
    private val requestReadPermission: DoRequestPermissions = requestPermissionsResultAbs(
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ),
        {
            lazyPresenter {
                firePrepared()
            }
        },
        {
            lazyPresenter {
                firePermissionsCanceled()
            }
        }
    )
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAudiosAdapter: AudiosLocalAdapter? = null
    private var mUploadAdapter: DocsUploadAdapter? = null
    private var mUploadRoot: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_local_music, container, false)
        val searchView: MySearchView = root.findViewById(R.id.searchview)
        searchView.setOnQueryTextListener(this)
        searchView.setRightButtonVisibility(true)
        searchView.setRightIcon(R.drawable.ic_menu_24_white)
        searchView.setLeftIcon(R.drawable.magnify)
        searchView.setOnAdditionalButtonClickListener(object : OnAdditionalButtonClickListener {
            override fun onAdditionalButtonClick() {
                presenter?.fireLocalAudioAlbums()
            }

        })
        val uploadRecyclerView: RecyclerView = root.findViewById(R.id.uploads_recycler_view)
        uploadRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        mUploadAdapter = DocsUploadAdapter(emptyList(), this)
        uploadRecyclerView.adapter = mUploadAdapter
        mUploadRoot = root.findViewById(R.id.uploads_root)
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
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
        mAudiosAdapter = AudiosLocalAdapter(emptyList())
        mAudiosAdapter?.setClickListener(this)
        recyclerView.adapter = mAudiosAdapter
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = AudiosLocalPresenter(
        requireArguments().getLong(Extra.ACCOUNT_ID),
        saveInstanceState
    )

    override fun displayList(audios: List<Audio>) {
        mAudiosAdapter?.setItems(audios)
    }

    override fun notifyListChanged() {
        mAudiosAdapter?.notifyDataSetChanged()
    }

    override fun displayRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    override fun checkPermission() {
        if (!hasReadStoragePermission(requireActivity())) {
            requestReadPermission.launch()
        } else {
            presenter?.firePrepared()
        }
    }

    override fun goToLocalAudioAlbums(selectedId: Int) {
        LocalAudioAlbumsFragment.newInstance(
            selectedId,
            object : LocalAudioAlbumsFragment.Listener {
                override fun onSelected(bucketId: Int) {
                    if (Settings.get().main().isRememberLocalAudioAlbum) {
                        Settings.get().main().currentLocalAudioAlbum = bucketId
                    }
                    presenter?.fireBucketSelected(
                        bucketId
                    )
                }

            }).show(
            childFragmentManager, "audio_albums_local"
        )
    }

    override fun setUploadDataVisible(visible: Boolean) {
        mUploadRoot?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun displayUploads(data: List<Upload>) {
        mUploadAdapter?.setData(data)
    }

    override fun notifyItemChanged(index: Int) {
        mAudiosAdapter?.notifyItemChanged(index)
    }

    override fun notifyItemRemoved(index: Int) {
        mAudiosAdapter?.notifyItemRemoved(index)
    }

    override fun notifyItemMoved(fromPosition: Int, toPosition: Int) {
        mAudiosAdapter?.notifyItemMoved(fromPosition, toPosition)
    }

    override fun notifyUploadItemsAdded(position: Int, count: Int) {
        mUploadAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun notifyUploadItemChanged(position: Int) {
        mUploadAdapter?.notifyItemChanged(position)
    }

    override fun notifyUploadItemRemoved(position: Int) {
        mUploadAdapter?.notifyItemRemoved(position)
    }

    override fun notifyUploadProgressChanged(position: Int, progress: Int, smoothly: Boolean) {
        mUploadAdapter?.changeUploadProgress(position, progress, smoothly)
    }

    override fun onRemoveClick(upload: Upload) {
        presenter?.fireRemoveClick(
            upload
        )
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        presenter?.fireQuery(
            query
        )
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        presenter?.fireQuery(
            newText
        )
        return false
    }

    override fun onClick(position: Int, audio: Audio) {
        presenter?.firePlayAudio(
            requireActivity(),
            position
        )
    }

    override fun openMenu(position: Int, audio: Audio) {
        presenter?.fireOpenMenu(position, audio)
    }

    override fun onOpenMenu(accountId: Long, position: Int, audio: Audio) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                AudioLocalOption.upload_item_audio,
                getString(R.string.upload),
                R.drawable.web,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalOption.play_item_audio,
                getString(R.string.play),
                R.drawable.play,
                true
            )
        )
        if (Settings.get().main().localServer.enabled) {
            menus.add(
                OptionRequest(
                    AudioLocalOption.play_via_local_server,
                    getString(R.string.play_remote),
                    R.drawable.remote_cloud,
                    false
                )
            )
        }
        if (canPlayAfterCurrent()) {
            menus.add(
                OptionRequest(
                    AudioLocalOption.play_item_after_current_audio,
                    getString(R.string.play_audio_after_current),
                    R.drawable.play_next,
                    false
                )
            )
        }
        menus.add(
            OptionRequest(
                AudioLocalOption.bitrate_item_audio,
                getString(R.string.get_bitrate),
                R.drawable.high_quality,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalOption.delete_item_audio,
                getString(R.string.delete),
                R.drawable.ic_outline_delete,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalOption.share_button,
                getString(R.string.share),
                R.drawable.ic_outline_share,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalOption.strip_metadata_item_audio,
                getString(R.string.strip_metadata),
                R.drawable.ic_outline_delete,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalOption.update_time_item_audio,
                getString(R.string.update_time),
                R.drawable.ic_recent,
                false
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
                AudioLocalOption.upload_item_audio -> {
                    presenter?.fireFileForUploadSelected(
                        audio.url
                    )
                }

                AudioLocalOption.play_via_local_server -> {
                    presenter?.fireFileForRemotePlaySelected(
                        audio.url
                    )
                }

                AudioLocalOption.play_item_audio -> {
                    presenter?.firePlayAudio(requireActivity(), position)
                }

                AudioLocalOption.play_item_after_current_audio -> playAfterCurrent(
                    audio
                )

                AudioLocalOption.bitrate_item_audio -> presenter?.fireGetLocalBitrate(
                    requireActivity(),
                    audio.url
                )

                AudioLocalOption.share_button -> {
                    val intentSend = Intent(Intent.ACTION_SEND)
                    intentSend.type = "audio/*"
                    intentSend.putExtra(
                        Intent.EXTRA_STREAM, audio.url?.toUri()
                    )
                    intentSend.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(
                        Intent.createChooser(
                            intentSend,
                            getString(R.string.share_using)
                        )
                    )
                }

                AudioLocalOption.strip_metadata_item_audio -> {
                    presenter?.fireStripMetadata(requireActivity(), audio.url)
                }

                AudioLocalOption.delete_item_audio -> try {
                    audio.url?.toUri()?.let { ur ->
                        if (requireActivity().contentResolver
                                .delete(ur, null, null) == 1
                        ) {
                            CustomSnackbars.createCustomSnackbars(view)
                                ?.setDurationSnack(Snackbar.LENGTH_LONG)
                                ?.coloredSnack(R.string.success, "#48BE2D".toColor(), false)
                                ?.show()
                            presenter?.fireDelete(
                                position
                            )
                        }
                    }
                } catch (e: Exception) {
                    customToast?.showToastThrowable(e)
                }

                AudioLocalOption.update_time_item_audio -> {
                    presenter?.fireUpdateAudioTime(requireActivity(), audio, position)
                }

                else -> {}
            }
        }
    }

    override fun onShowBitrate(bitrate: Int, size: Long) {
        CustomSnackbars.createCustomSnackbars(view)
            ?.setDurationSnack(Snackbar.LENGTH_LONG)
            ?.themedSnack(
                getString(
                    R.string.bitrate,
                    bitrate,
                    Utils.BytesToSize(size)
                )
            )
            ?.show()
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

    override fun onServiceBindEvent(@PlayerStatus status: Int) {
        mAudiosAdapter?.onServiceBindEvent(status)
    }

    companion object {
        fun newInstance(accountId: Long): AudiosLocalFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            val fragment = AudiosLocalFragment()
            fragment.arguments = args
            return fragment
        }
    }
}