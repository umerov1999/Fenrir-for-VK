package dev.ragnarok.fenrir.fragment.audio.audiosrecommendation

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.textfield.TextInputEditText
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.fragment.audio.audios.AudiosAdapter
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.canPlayAfterCurrent
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playAfterCurrent
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.menu.options.AudioOption
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceFactory.SearchByAudioPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getArtistPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosInAlbumPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleTabSearchPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleURLPhotoPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadAudio
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getBitrate
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.util.toast.CustomSnackbars
import dev.ragnarok.fenrir.view.navigation.AbsNavigationView

class AudiosRecommendationFragment :
    BaseMvpFragment<AudiosRecommendationPresenter, IAudiosRecommendationView>(),
    IAudiosRecommendationView {
    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        presenter?.continueDownload()
    }
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAudiosAdapter: AudiosAdapter? = null
    private var isSaveMode = false
    private var inTabsContainer = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inTabsContainer = requireArguments().getBoolean(Extra.IN_TABS_CONTAINER)
        isSaveMode = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_music, container, false)
        val toolbar: Toolbar = root.findViewById(R.id.toolbar)
        if (!inTabsContainer) {
            toolbar.visibility = View.VISIBLE
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
                WindowInsetsCompat.CONSUMED
            }
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
        PicassoPauseOnScrollListener.addListener(recyclerView)
        val saveMode: FloatingActionButton = root.findViewById(R.id.save_mode_button)
        val goTo: FloatingActionButton = root.findViewById(R.id.goto_button)
        saveMode.visibility =
            if (Settings.get().main().isAudio_save_mode_button) View.VISIBLE else View.GONE
        saveMode.setOnClickListener {
            isSaveMode = !isSaveMode
            goTo.setImageResource(if (isSaveMode) R.drawable.check else R.drawable.audio_player)
            saveMode.setImageResource(if (isSaveMode) R.drawable.ic_dismiss else R.drawable.save)
            mAudiosAdapter?.toggleSelectMode(isSaveMode)
            presenter?.fireUpdateSelectMode()
        }
        goTo.setImageResource(R.drawable.audio_player)
        saveMode.setImageResource(R.drawable.save)
        goTo.setOnLongClickListener {
            if (!isSaveMode) {
                val curr = currentAudio
                if (curr != null) {
                    getPlayerPlace(requireArguments().getLong(Extra.ACCOUNT_ID)).tryOpenWith(
                        requireActivity()
                    )
                } else {
                    customToast?.showToastError(R.string.null_audio)
                }
            } else {
                presenter?.fireSelectAll()
            }
            true
        }
        goTo.setOnClickListener {
            if (isSaveMode) {
                presenter?.fireMultipleDownload(requireActivity())
                isSaveMode = false
                goTo.setImageResource(R.drawable.audio_player)
                saveMode.setImageResource(R.drawable.save)
                mAudiosAdapter?.toggleSelectMode(isSaveMode)
            } else {
                val curr = currentAudio
                if (curr != null) {
                    val index =
                        presenter?.getAudioPos(curr) ?: -1
                    if (index >= 0) {
                        recyclerView.scrollToPosition(
                            index + mAudiosAdapter?.headersCount.orZero()
                        )
                    } else customToast?.showToast(R.string.audio_not_found)
                } else customToast?.showToastError(R.string.null_audio)
            }
        }
        mAudiosAdapter =
            AudiosAdapter(
                mutableListOf(),
                notShowMy = true,
                iSSelectMode = false
            )
        mAudiosAdapter?.setClickListener(object : AudiosAdapter.ClickListener {
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
                        requireActivity(),
                        position
                    )
                }

                AudioOption.play_item_after_current_audio -> playAfterCurrent(
                    audio
                )

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

    override fun onResume() {
        super.onResume()
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS)
            val actionBar = supportToolbarFor(this)
            if (actionBar != null) {
                actionBar.setTitle(R.string.music)
                actionBar.subtitle = null
            }
            if (requireActivity() is OnSectionResumeCallback) {
                (requireActivity() as OnSectionResumeCallback).onSectionResume(AbsNavigationView.SECTION_ITEM_AUDIOS)
            }
            ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .build()
                .apply(requireActivity())
        }
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = AudiosRecommendationPresenter(
        requireArguments().getLong(Extra.ACCOUNT_ID),
        requireArguments().getLong(Extra.OWNER_ID),
        requireArguments().getBoolean(Extra.TOP),
        requireArguments().getInt(Extra.ID),
        saveInstanceState
    )

    override fun displayList(audios: MutableList<Audio>) {
        mAudiosAdapter?.setItems(audios)
    }

    override fun notifyListChanged() {
        mAudiosAdapter?.notifyDataSetChanged()
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

    override fun displayRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    companion object {
        fun newInstance(
            accountId: Long,
            ownerId: Long,
            top: Boolean,
            optionMenuId: Int
        ): AudiosRecommendationFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putLong(Extra.OWNER_ID, ownerId)
            args.putBoolean(Extra.TOP, top)
            args.putInt(Extra.ID, optionMenuId)
            val fragment = AudiosRecommendationFragment()
            fragment.arguments = args
            return fragment
        }
    }
}