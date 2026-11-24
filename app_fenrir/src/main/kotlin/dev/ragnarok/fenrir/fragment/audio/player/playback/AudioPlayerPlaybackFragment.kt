package dev.ragnarok.fenrir.fragment.audio.player.playback

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.textfield.TextInputEditText
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.fragment.audio.audios.AudiosAdapter
import dev.ragnarok.fenrir.fragment.base.BaseMvpBottomSheetDialogFragment
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria
import dev.ragnarok.fenrir.listener.BackPressCallback
import dev.ragnarok.fenrir.media.music.MusicPlaybackController
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.canPlayAfterCurrent
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playAfterCurrent
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.menu.options.AudioOption
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.place.PlaceFactory.SearchByAudioPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getArtistPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosInAlbumPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleTabSearchPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleURLPhotoPlace
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadAudio
import dev.ragnarok.fenrir.util.MessagesReplyItemCallback
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getBitrate
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.toast.CustomSnackbars
import dev.ragnarok.fenrir.util.toast.CustomToast.Companion.createCustomToast

class AudioPlayerPlaybackFragment :
    BaseMvpBottomSheetDialogFragment<AudioPlayerPlaybackPresenter, IAudioPlayerPlaybackView>(),
    IAudioPlayerPlaybackView,
    BackPressCallback {
    private var mRecyclerView: RecyclerView? = null
    private var mAudiosAdapter: AudiosAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireActivity(), theme)
        val behavior = dialog.behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        return dialog
    }

    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        presenter?.continueDownload()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_audioplayer_playback, container, false)
        mRecyclerView = root.findViewById(R.id.list)
        val manager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = manager
        val goTo: FloatingActionButton = root.findViewById(R.id.goto_button)
        goTo.setOnClickListener {
            val curr = MusicPlaybackController.currentAudio
            if (curr != null) {
                val index = presenter?.getAudioPos(curr) ?: -1
                if (index >= 0) {
                    mRecyclerView?.scrollToPosition(index)
                } else createCustomToast(
                    requireActivity(),
                    view
                )?.showToast(R.string.audio_not_found)
            } else createCustomToast(requireActivity(), view)?.showToastError(R.string.null_audio)
        }

        mAudiosAdapter = AudiosAdapter(
            mutableListOf(),
            notShowMy = false,
            iSSelectMode = false
        )

        mAudiosAdapter?.setClickListener(object : AudiosAdapter.ClickListener {
            override fun onClick(position: Int, audio: Audio) {
                presenter?.firePlayAudio(position)
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

        mRecyclerView?.adapter = mAudiosAdapter

        ItemTouchHelper(MessagesReplyItemCallback {
            presenter?.firePlayAudio(it)
        }).attachToRecyclerView(mRecyclerView)
        return root
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): AudioPlayerPlaybackPresenter {
        return AudioPlayerPlaybackPresenter(
            requireArguments().getLong(Extra.ACCOUNT_ID),
            saveInstanceState
        )
    }

    override fun displayList(audios: MutableList<Audio>, currentPos: Int) {
        mAudiosAdapter?.setItems(audios)
        if (currentPos > 0) {
            mRecyclerView?.scrollToPosition(currentPos)
        }
    }

    override fun notifyListChanged() {
        mAudiosAdapter?.notifyDataSetChanged()
    }

    override fun notifyItemMoved(fromPosition: Int, toPosition: Int) {
        mAudiosAdapter?.notifyItemBindableMoved(fromPosition, toPosition)
    }

    override fun notifyItemRemoved(index: Int) {
        mAudiosAdapter?.notifyItemBindableRemoved(index)
    }

    override fun notifyItemChanged(index: Int) {
        mAudiosAdapter?.notifyItemBindableChanged(index)
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mAudiosAdapter?.notifyItemBindableRangeInserted(position, count)
    }

    override fun onStartForPlayList(
        audios: ArrayList<Audio>,
        position: Int
    ) {
        startForPlayList(requireActivity(), audios, position)
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

    override fun onShowBitrate(duration: Int, size: Long) {
        CustomSnackbars.createCustomSnackbars(view)
            ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
            ?.themedSnack(getBitrate(requireActivity(), duration, size))
            ?.show()
    }

    override fun onSelectToSendAttachment(audios: ArrayList<Audio>) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(
            Extra.ATTACHMENTS, audios
        )
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    override fun onOpenMenu(accountId: Long, position: Int, audio: Audio) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                AudioOption.play_item_audio,
                getString(R.string.play),
                R.drawable.play,
                true
            )
        )
        if (canPlayAfterCurrent()) {
            menus.add(
                OptionRequest(
                    AudioOption.play_item_after_current_audio,
                    getString(R.string.play_audio_after_current),
                    R.drawable.play_next,
                    false
                )
            )
        }
        if (audio.ownerId != accountId) {
            menus.add(
                OptionRequest(
                    AudioOption.add_item_audio,
                    getString(R.string.action_add),
                    R.drawable.list_add,
                    true
                )
            )
            menus.add(
                OptionRequest(
                    AudioOption.add_and_download_button,
                    getString(R.string.add_and_download_button),
                    R.drawable.add_download,
                    false
                )
            )
        } else {
            menus.add(
                OptionRequest(
                    AudioOption.add_item_audio,
                    getString(R.string.delete),
                    R.drawable.ic_outline_delete,
                    true
                )
            )
            menus.add(
                OptionRequest(
                    AudioOption.edit_track,
                    getString(R.string.edit),
                    R.drawable.about_writed,
                    true
                )
            )
        }
        menus.add(
            OptionRequest(
                AudioOption.share_button,
                getString(R.string.share),
                R.drawable.ic_outline_share,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.save_item_audio,
                getString(R.string.save),
                R.drawable.save,
                true
            )
        )
        if (audio.albumId != 0) menus.add(
            OptionRequest(
                AudioOption.open_album,
                getString(R.string.open_album),
                R.drawable.audio_album,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.get_recommendation_by_audio,
                getString(R.string.get_recommendation_by_audio),
                R.drawable.music_mic,
                false
            )
        )
        if (audio.main_artists.nonNullNoEmpty()) menus.add(
            OptionRequest(
                AudioOption.goto_artist,
                getString(R.string.audio_goto_artist),
                R.drawable.artist_icon,
                false
            )
        )
        if (audio.lyricsId != 0) menus.add(
            OptionRequest(
                AudioOption.get_lyrics_menu,
                getString(R.string.get_lyrics_menu),
                R.drawable.lyric,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.bitrate_item_audio,
                getString(R.string.get_bitrate),
                R.drawable.high_quality,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.search_by_artist,
                getString(R.string.search_by_artist),
                R.drawable.magnify,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.copy_url,
                getString(R.string.copy_url),
                R.drawable.content_copy,
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
                AudioOption.play_item_audio -> {
                    presenter?.firePlayAudio(
                        position
                    )
                }

                AudioOption.play_item_after_current_audio -> {
                    playAfterCurrent(
                        audio
                    )
                    presenter?.fireRefresh()
                }

                AudioOption.edit_track -> {
                    val root = View.inflate(requireActivity(), R.layout.entry_audio_info, null)
                    root.findViewById<TextInputEditText>(R.id.edit_artist).setText(audio.artist)
                    root.findViewById<TextInputEditText>(R.id.edit_title).setText(audio.title)
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.enter_audio_info)
                        .setCancelable(true)
                        .setView(root)
                        .setPositiveButton(R.string.button_ok) { _, _ ->
                            presenter?.fireEditTrack(
                                position,
                                root.findViewById<TextInputEditText>(R.id.edit_artist).text?.trim()
                                    .toString(),
                                root.findViewById<TextInputEditText>(R.id.edit_title).text?.trim()
                                    .toString(),
                                audio
                            )
                        }
                        .setNegativeButton(R.string.button_cancel, null)
                        .show()
                }

                AudioOption.share_button -> startForSendAttachments(
                    requireActivity(),
                    accountId,
                    audio
                )

                AudioOption.search_by_artist -> getSingleTabSearchPlace(
                    accountId,
                    SearchContentType.AUDIOS,
                    AudioSearchCriteria(
                        audio.artist, by_artist = true, in_main_page = false
                    )
                ).tryOpenWith(requireActivity())

                AudioOption.get_lyrics_menu -> presenter?.fireGetLyrics(audio)
                AudioOption.get_recommendation_by_audio -> SearchByAudioPlace(
                    accountId, audio.ownerId, audio.id
                ).tryOpenWith(requireActivity())

                AudioOption.open_album -> getAudiosInAlbumPlace(
                    accountId,
                    audio.album_owner_id,
                    audio.albumId,
                    audio.album_access_key
                ).tryOpenWith(requireActivity())

                AudioOption.copy_url -> {
                    val clipboard =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("response", audio.url)
                    clipboard?.setPrimaryClip(clip)
                    customToast?.showToast(R.string.copied)
                }

                AudioOption.add_item_audio -> {
                    val myAudio = audio.ownerId == accountId
                    if (myAudio) {
                        presenter?.fireDelete(position, audio)
                    } else {
                        presenter?.fireAddTrack(audio)
                    }
                }

                AudioOption.add_and_download_button -> {
                    if (audio.ownerId != accountId) {
                        presenter?.fireAddTrack(audio)
                    }
                    presenter?.firePrepareDownload(position, audio)
                }

                AudioOption.save_item_audio -> {
                    presenter?.firePrepareDownload(position, audio)
                }

                AudioOption.bitrate_item_audio -> presenter?.fireGetBitrate(audio)

                AudioOption.goto_artist -> {
                    val artists = Utils.getArrayFromHash(
                        audio.main_artists
                    )
                    if (audio.main_artists?.keys?.size.orZero() > 1) {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setItems(artists[1]) { _, which ->
                                getArtistPlace(
                                    accountId,
                                    artists[0][which]
                                ).tryOpenWith(requireActivity())
                            }
                            .show()
                    } else {
                        getArtistPlace(
                            accountId,
                            artists[0][0]
                        ).tryOpenWith(requireActivity())
                    }
                }
            }
        }
    }

    override fun onAudioLyricsReceived(lyricText: String, audio: Audio) {
        val title = audio.artistAndTitle
        MaterialAlertDialogBuilder(requireActivity())
            .setIcon(R.drawable.dir_song)
            .setMessage(lyricText)
            .setTitle(title)
            .setPositiveButton(R.string.button_ok, null)
            .setNeutralButton(R.string.copy_text) { _, _ ->
                val clipboard = requireActivity().getSystemService(
                    Context.CLIPBOARD_SERVICE
                ) as ClipboardManager?
                val clip = ClipData.newPlainText("response", lyricText)
                clipboard?.setPrimaryClip(clip)
                customToast?.showToast(R.string.copied_to_clipboard)
            }
            .setCancelable(true)
            .show()
    }

    companion object {
        fun newInstance(accountId: Long): AudioPlayerPlaybackFragment {
            val bundle = Bundle()
            bundle.putLong(Extra.ACCOUNT_ID, accountId)
            val fragment = AudioPlayerPlaybackFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}