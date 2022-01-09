package dev.ragnarok.fenrir.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.*
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso3.BitmapTarget
import com.squareup.picasso3.Callback
import com.squareup.picasso3.Picasso
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extensions.Companion.toMainThread
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.Injection
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria
import dev.ragnarok.fenrir.materialpopupmenu.MaterialPopupMenuBuilder
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.picasso.transforms.BlurTransformation
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.player.MusicPlaybackController
import dev.ragnarok.fenrir.player.MusicPlaybackController.PlayerStatus
import dev.ragnarok.fenrir.player.ui.PlayPauseButton
import dev.ragnarok.fenrir.player.ui.RepeatButton
import dev.ragnarok.fenrir.player.ui.RepeatingImageButton
import dev.ragnarok.fenrir.player.ui.ShuffleButton
import dev.ragnarok.fenrir.service.ErrorLocalizer
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.DownloadWorkUtils.TrackIsDownloaded
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadAudio
import dev.ragnarok.fenrir.util.Objects
import dev.ragnarok.fenrir.util.RxUtils
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.firstNonEmptyString
import dev.ragnarok.fenrir.util.Utils.isEmpty
import dev.ragnarok.fenrir.view.FadeAnimDrawable
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieShapeableImageView
import dev.ragnarok.fenrir.view.pager.WeakPicassoLoadCallback
import dev.ragnarok.fenrir.view.seek.DefaultTimeBar
import dev.ragnarok.fenrir.view.seek.TimeBar
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.math.min

class AudioPlayerFragment : BottomSheetDialogFragment(), TimeBar.OnScrubListener {
    private val PLAYER_TAG = "PicassoPlayerTag"

    // Play and pause button
    private var mPlayPauseButton: PlayPauseButton? = null

    // Repeat button
    private var mRepeatButton: RepeatButton? = null

    // Shuffle button
    private var mShuffleButton: ShuffleButton? = null

    // Current time
    private var mCurrentTime: TextView? = null

    // Total time
    private var mTotalTime: TextView? = null
    private var mGetLyrics: ImageView? = null

    // Progress
    private var mProgress: DefaultTimeBar? = null

    // VK Additional action
    private var ivAdd: ImageView? = null
    private var ivSave: RepeatingImageButton? = null
    private var tvTitle: TextView? = null
    private var tvAlbum: TextView? = null
    private var tvSubtitle: TextView? = null
    private var ivCoverPager: ViewPager2? = null
    private var ivBackground: ImageView? = null
    private var playerGradientFirst: ImageView? = null
    private var playerGradientSecond: ImageView? = null

    // Handler used to update the current time
    private var mTimeHandler: TimeHandler? = null
    private var mPosOverride: Long = -1
    private var mStartSeekPos: Long = 0
    private var mLastSeekEventTime: Long = 0
    private var mFromTouch = false
    private var coverAdapter: CoverAdapter? = null
    private lateinit var mPlayerProgressStrings: Array<String>
    private var currentPage = -1
    private var playDispose = Disposable.disposed()

    private val requestEqualizer = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        view?.let { it1 ->
            Snackbar.make(it1, R.string.equalizer_closed, Snackbar.LENGTH_LONG)
                .setBackgroundTint(CurrentTheme.getColorPrimary(requireActivity()))
                .setAnchorView(mPlayPauseButton).setActionTextColor(
                    if (Utils.isColorDark(
                            CurrentTheme.getColorPrimary(requireActivity())
                        )
                    ) Color.parseColor("#ffffff") else Color.parseColor("#000000")
                )
                .setTextColor(
                    if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity()))) Color.parseColor(
                        "#ffffff"
                    ) else Color.parseColor("#000000")
                ).show()
        }
    }

    /**
     * Used to scan backwards through the track
     */
    private val mRewindListener =
        RepeatingImageButton.RepeatListener { _: View?, howlong: Long, repcnt: Int ->
            scanBackward(
                repcnt,
                howlong
            )
        }

    /**
     * Used to scan ahead through the track
     */
    private val mFastForwardListener =
        RepeatingImageButton.RepeatListener { _: View?, howlong: Long, repcnt: Int ->
            scanForward(
                repcnt,
                howlong
            )
        }
    private var mAudioInteractor: IAudioInteractor = InteractorFactory.createAudioInteractor()
    private var mAccountId = 0
    private val mBroadcastDisposable = CompositeDisposable()
    private val mCompositeDisposable = CompositeDisposable()
    private fun appendDisposable(disposable: Disposable) {
        mCompositeDisposable.add(disposable)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireActivity(), theme)
        val behavior = dialog.behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        return dialog
    }

    private fun onServiceBindEvent(status: Int) {
        when (status) {
            PlayerStatus.UPDATE_TRACK_INFO -> {
                updatePlaybackControls()
                updateNowPlayingInfo()
                resolveControlViews()
            }
            PlayerStatus.UPDATE_PLAY_PAUSE -> {
                updatePlaybackControls()
                resolveTotalTime()
                resolveControlViews()
                ivBackground?.let {
                    if (it.drawable is Animatable) {
                        (it.drawable as Animatable).apply {
                            if (MusicPlaybackController.isPlaying()) {
                                start()
                            } else {
                                stop()
                            }
                        }
                    }
                }
            }
            PlayerStatus.REPEATMODE_CHANGED -> {
                mRepeatButton?.updateRepeatState()
            }
            PlayerStatus.SHUFFLEMODE_CHANGED -> {
                mShuffleButton?.updateShuffleState()
                updateCovers()
            }
            PlayerStatus.UPDATE_PLAY_LIST -> {
                updateCovers()
            }
            PlayerStatus.SERVICE_KILLED -> {
                updatePlaybackControls()
                updateNowPlayingInfo()
                resolveControlViews()
                updateCovers()
            }
        }
    }

    private val requestWriteQRPermission = AppPerms.requestPermissions(
        this,
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) { CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text) }

    @Suppress("DEPRECATION")
    private fun fireAudioQR() {
        val audio = MusicPlaybackController.getCurrentAudio() ?: return
        val qr = Utils.generateQR(
            "https://vk.com/audio/" + audio.ownerId + "_" + audio.id,
            requireActivity()
        )
        val dlgAlert = MaterialAlertDialogBuilder(requireActivity())
        dlgAlert.setCancelable(true)
        dlgAlert.setNegativeButton(R.string.button_cancel, null)
        dlgAlert.setPositiveButton(R.string.save) { _, _ ->
            if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
                requestWriteQRPermission.launch()
            } else {
                val path = Environment.getExternalStorageDirectory().absolutePath
                val fOutputStream: OutputStream
                val file = File(path, "qr_fenrir_audio_" + audio.ownerId + "_" + audio.id + ".png")
                try {
                    fOutputStream = FileOutputStream(file)
                    qr?.compress(Bitmap.CompressFormat.PNG, 100, fOutputStream)
                    fOutputStream.flush()
                    fOutputStream.close()
                    requireActivity().sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(file)
                        )
                    )
                    CreateCustomToast(requireActivity()).showToast(R.string.success)
                } catch (e: IOException) {
                    e.printStackTrace()
                    CreateCustomToast(requireActivity()).showToastError("Save Failed")
                }
            }
        }
        dlgAlert.setIcon(R.drawable.qr_code)
        val view: View = LayoutInflater.from(requireActivity()).inflate(R.layout.qr, null)
        dlgAlert.setTitle(R.string.show_qr)
        val imageView: ShapeableImageView = view.findViewById(R.id.qr)
        imageView.setImageBitmap(qr)
        dlgAlert.setView(view)
        dlgAlert.show()
    }

    private val requestWriteAudioPermission = AppPerms.requestPermissions(
        this,
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) { onSaveButtonClick(requireView()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_audio_player, container, false)
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID)
        mPlayerProgressStrings = resources.getStringArray(R.array.player_progress_state)
        playerGradientFirst = root.findViewById(R.id.cover_gradient_top)
        playerGradientSecond = root.findViewById(R.id.cover_gradient)
        mProgress = root.findViewById(R.id.seek_player_pos)
        mPlayPauseButton = root.findViewById(R.id.action_button_play)
        mShuffleButton = root.findViewById(R.id.action_button_shuffle)
        mRepeatButton = root.findViewById(R.id.action_button_repeat)
        val mAdditional = root.findViewById<ImageView>(R.id.goto_button)
        mAdditional.setOnClickListener {
            val popupMenu = MaterialPopupMenuBuilder()
            popupMenu.section {
                if (isEqualizerAvailable) {
                    item {
                        labelRes = R.string.equalizer
                        icon = R.drawable.preferences
                        iconColor = CurrentTheme.getColorSecondary(requireActivity())
                        callback = {
                            startEffectsPanel()
                        }
                    }
                }
                item {
                    labelRes = R.string.playlist
                    icon = R.drawable.ic_menu_24_white
                    iconColor = CurrentTheme.getColorSecondary(requireActivity())
                    callback = {
                        val tmpList = MusicPlaybackController.getQueue()
                        if (!isEmpty(tmpList)) {
                            PlaylistFragment.newInstance(ArrayList(tmpList))
                                .show(childFragmentManager, "audio_playlist")
                        }
                    }
                }
                item {
                    labelRes = R.string.copy_track_info
                    icon = R.drawable.content_copy
                    iconColor = CurrentTheme.getColorSecondary(requireActivity())
                    callback = {
                        val clipboard =
                            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        var Artist =
                            if (MusicPlaybackController.getArtistName() != null) MusicPlaybackController.getArtistName() else ""
                        if (MusicPlaybackController.getAlbumName() != null) Artist += " (" + MusicPlaybackController.getAlbumName() + ")"
                        val Name =
                            if (MusicPlaybackController.getTrackName() != null) MusicPlaybackController.getTrackName() else ""
                        val clip = ClipData.newPlainText("response", "$Artist - $Name")
                        clipboard.setPrimaryClip(clip)
                        CreateCustomToast(requireActivity()).showToast(R.string.copied_to_clipboard)
                    }
                }
                item {
                    labelRes = R.string.show_qr
                    icon = R.drawable.qr_code
                    iconColor = CurrentTheme.getColorSecondary(requireActivity())
                    callback = {
                        fireAudioQR()
                    }
                }
                item {
                    labelRes = R.string.search_by_artist
                    icon = R.drawable.magnify
                    iconColor = CurrentTheme.getColorSecondary(requireActivity())
                    callback = {
                        PlaceFactory.getSingleTabSearchPlace(
                            mAccountId,
                            SearchContentType.AUDIOS,
                            AudioSearchCriteria(
                                MusicPlaybackController.getArtistName(),
                                true,
                                false
                            )
                        ).tryOpenWith(requireActivity())
                        dismissAllowingStateLoss()
                    }
                }
            }
            popupMenu.build().show(requireActivity(), it)
        }
        val mPreviousButton: RepeatingImageButton = root.findViewById(R.id.action_button_previous)
        val mNextButton: RepeatingImageButton = root.findViewById(R.id.action_button_next)
        ivCoverPager = root.findViewById(R.id.cover_pager)
        ivBackground = root.findViewById(R.id.cover_background)
        mCurrentTime = root.findViewById(R.id.audio_player_current_time)
        mTotalTime = root.findViewById(R.id.audio_player_total_time)
        tvTitle = root.findViewById(R.id.audio_player_title)
        tvAlbum = root.findViewById(R.id.audio_player_album)
        tvSubtitle = root.findViewById(R.id.audio_player_subtitle)
        mGetLyrics = root.findViewById(R.id.audio_player_get_lyrics)
        mGetLyrics?.setOnClickListener { onLyrics() }

        coverAdapter = CoverAdapter()
        ivCoverPager?.adapter = coverAdapter
        ivCoverPager?.setPageTransformer(
            Utils.createPageTransform(
                Settings.get().main().player_cover_transform
            )
        )
        ivCoverPager?.offscreenPageLimit = 1
        ivCoverPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (currentPage != position) {
                    currentPage = position
                    playDispose.dispose()
                    playDispose = Observable.just(Object())
                        .delay(400, TimeUnit.MILLISECONDS)
                        .toMainThread()
                        .subscribe { MusicPlaybackController.skip(position) }
                    ivCoverPager?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }
        })

        //to animate running text
        tvTitle?.isSelected = true
        tvSubtitle?.isSelected = true
        tvAlbum?.isSelected = true
        mPreviousButton.setRepeatListener(mRewindListener)
        mNextButton.setRepeatListener(mFastForwardListener)
        mProgress?.addListener(this)
        ivSave = root.findViewById(R.id.audio_save)
        ivSave?.setOnClickListener {
            run {
                if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
                    requestWriteAudioPermission.launch()
                } else {
                    onSaveButtonClick(it)
                }
            }
        }
        ivAdd = root.findViewById(R.id.audio_add)
        if (Settings.get().main().isPlayer_support_volume) {
            ivAdd?.setImageResource(R.drawable.volume_minus)
            ivAdd?.setOnClickListener {
                val audio =
                    requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audio.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audio.getStreamVolume(AudioManager.STREAM_MUSIC) - 1,
                    0
                )
            }
        } else {
            ivAdd?.setImageResource(R.drawable.plus)
            ivAdd?.setOnClickListener { onAddButtonClick() }
        }
        val ivShare: ImageView = root.findViewById(R.id.audio_share)
        if (Settings.get().main().isPlayer_support_volume) {
            ivShare.setImageResource(R.drawable.volume_plus)
            ivShare.setOnClickListener {
                val audio =
                    requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audio.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    audio.getStreamVolume(AudioManager.STREAM_MUSIC) + 1,
                    0
                )
            }
        } else {
            ivShare.setImageResource(R.drawable.ic_outline_share)
            ivShare.setOnClickListener { shareAudio() }
        }

        resolveAddButton()

        mTimeHandler = TimeHandler(this)
        appendDisposable(MusicPlaybackController.observeServiceBinding()
            .toMainThread()
            .subscribe { onServiceBindEvent(it) })
        return root
    }

    private val isAudioStreaming: Boolean
        get() = Settings.get()
            .other()
            .isAudioBroadcastActive

    @SuppressLint("ShowToast")
    private fun onSaveButtonClick(v: View) {
        val audio = MusicPlaybackController.getCurrentAudio() ?: return
        when (doDownloadAudio(
            requireActivity(),
            audio,
            mAccountId,
            false,
            isLocal = audio.isLocalServer
        )) {
            0 -> {
                CreateCustomToast(requireActivity()).showToastBottom(R.string.saved_audio)
                ivSave?.setImageResource(R.drawable.succ)
            }
            1 -> {
                Snackbar.make(v, R.string.audio_force_download, Snackbar.LENGTH_LONG).setAction(
                    R.string.button_yes
                ) {
                    doDownloadAudio(
                        requireActivity(),
                        audio,
                        mAccountId,
                        true,
                        isLocal = audio.isLocalServer
                    )
                }
                    .setBackgroundTint(CurrentTheme.getColorPrimary(requireActivity()))
                    .setAnchorView(mPlayPauseButton).setActionTextColor(
                        if (Utils.isColorDark(
                                CurrentTheme.getColorPrimary(requireActivity())
                            )
                        ) Color.parseColor("#ffffff") else Color.parseColor("#000000")
                    )
                    .setTextColor(
                        if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity()))) Color.parseColor(
                            "#ffffff"
                        ) else Color.parseColor("#000000")
                    ).show()
            }
            2 -> {
                Snackbar.make(v, R.string.audio_force_download_pc, Snackbar.LENGTH_LONG)
                    .setAnchorView(mPlayPauseButton).setAction(
                        R.string.button_yes
                    ) {
                        doDownloadAudio(
                            requireActivity(),
                            audio,
                            mAccountId,
                            true,
                            isLocal = audio.isLocalServer
                        )
                    }
                    .setBackgroundTint(CurrentTheme.getColorPrimary(requireActivity()))
                    .setActionTextColor(
                        if (Utils.isColorDark(
                                CurrentTheme.getColorPrimary(requireActivity())
                            )
                        ) Color.parseColor("#ffffff") else Color.parseColor("#000000")
                    )
                    .setTextColor(
                        if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity()))) Color.parseColor(
                            "#ffffff"
                        ) else Color.parseColor("#000000")
                    ).show()
                ivSave?.setImageResource(R.drawable.succ)
            }
            else -> CreateCustomToast(requireActivity()).showToastBottom(R.string.error_audio)
        }
    }

    private fun onAddButtonClick() {
        val audio = MusicPlaybackController.getCurrentAudio() ?: return
        if (audio.isLocal || audio.isLocalServer) {
            CreateCustomToast(requireActivity()).showToastError(R.string.not_supported)
            return
        }
        if (audio.ownerId == mAccountId) {
            if (!audio.isDeleted) {
                delete(mAccountId, audio)
            } else {
                restore(mAccountId, audio)
            }
        } else {
            add(mAccountId, audio)
        }
    }

    private fun showErrorInAdapter(throwable: Throwable) {
        if (!isAdded || view == null) {
            return
        }
        val caused = Utils.getCauseIfRuntime(throwable)
        if (Constants.IS_DEBUG) {
            caused.printStackTrace()
        }
        Snackbar.make(
            requireView(),
            ErrorLocalizer.localizeThrowable(Injection.provideApplicationContext(), caused),
            BaseTransientBottomBar.LENGTH_LONG
        ).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#eeff0000"))
            .setAction(R.string.more_info) {
                val Text = StringBuilder()
                for (stackTraceElement in throwable.stackTrace) {
                    Text.append("    ")
                    Text.append(stackTraceElement)
                    Text.append("\r\n")
                }
                val dlgAlert = MaterialAlertDialogBuilder(requireActivity())
                dlgAlert.setIcon(R.drawable.ic_error)
                dlgAlert.setMessage(Text)
                dlgAlert.setTitle(R.string.more_info)
                dlgAlert.setPositiveButton(R.string.button_ok, null)
                dlgAlert.setCancelable(true)
                dlgAlert.create().show()
            }.setActionTextColor(Color.WHITE).show()
    }

    private fun onLyrics() {
        val audio = MusicPlaybackController.getCurrentAudio() ?: return
        get_lyrics(audio)
    }

    private fun add(accountId: Int, audio: Audio) {
        appendDisposable(
            mAudioInteractor.add(accountId, audio, null)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe({ onAudioAdded() }) { showErrorInAdapter(it) })
    }

    private fun onAudioAdded() {
        CreateCustomToast(requireActivity()).showToast(R.string.added)
        resolveAddButton()
    }

    private fun delete(accountId: Int, audio: Audio) {
        val id = audio.id
        val ownerId = audio.ownerId
        appendDisposable(
            mAudioInteractor.delete(accountId, id, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe({
                    onAudioDeletedOrRestored(
                        id,
                        ownerId,
                        true
                    )
                }) { showErrorInAdapter(it) })
    }

    private fun restore(accountId: Int, audio: Audio) {
        val id = audio.id
        val ownerId = audio.ownerId
        appendDisposable(
            mAudioInteractor.restore(accountId, id, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe({
                    onAudioDeletedOrRestored(
                        id,
                        ownerId,
                        false
                    )
                }) { showErrorInAdapter(it) })
    }

    private fun get_lyrics(audio: Audio) {
        appendDisposable(
            mAudioInteractor.getLyrics(mAccountId, audio.lyricsId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe({ Text: String -> onAudioLyricsReceived(Text) }) { showErrorInAdapter(it) })
    }

    private fun onAudioLyricsReceived(Text: String) {
        var title: String? = null
        if (MusicPlaybackController.getCurrentAudio() != null) title =
            MusicPlaybackController.getCurrentAudio()?.artistAndTitle
        val dlgAlert = MaterialAlertDialogBuilder(requireActivity())
        dlgAlert.setIcon(R.drawable.dir_song)
        dlgAlert.setMessage(Text)
        dlgAlert.setTitle(title ?: requireActivity().getString(R.string.get_lyrics))
        dlgAlert.setPositiveButton(R.string.button_ok, null)
        dlgAlert.setNeutralButton(requireActivity().getString(R.string.copy_text)) { _: DialogInterface, _: Int ->
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("response", Text)
            clipboard.setPrimaryClip(clip)
            CreateCustomToast(requireActivity()).showToast(R.string.copied_to_clipboard)
        }
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()
    }

    private fun onAudioDeletedOrRestored(id: Int, ownerId: Int, deleted: Boolean) {
        if (deleted) {
            CreateCustomToast(requireActivity()).showToast(R.string.deleted)
        } else {
            CreateCustomToast(requireActivity()).showToast(R.string.restored)
        }
        val current = MusicPlaybackController.getCurrentAudio()
        if (Objects.nonNull(current) && current?.id == id && current.ownerId == ownerId) {
            current.isDeleted = deleted
        }
        resolveAddButton()
    }

    override fun onScrubStart(timeBar: TimeBar?, position: Long) {
        mFromTouch = true
        if (MusicPlaybackController.mService != null) {
            mPosOverride = position
        }
    }

    override fun onScrubMove(timeBar: TimeBar?, position: Long) {
        if (MusicPlaybackController.mService == null) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (now - mLastSeekEventTime > 100) {
            mLastSeekEventTime = now
            refreshCurrentTime()
            if (!mFromTouch) {
                // refreshCurrentTime();
                mPosOverride = -1
            }
        }
        mPosOverride = position
    }

    override fun onScrubStop(timeBar: TimeBar?, position: Long, canceled: Boolean) {
        if (mPosOverride != -1L) {
            MusicPlaybackController.seek(mPosOverride)
            mPosOverride = -1
        }
        mFromTouch = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set the playback drawables
        updatePlaybackControls()
        // Current info
        updateCovers()
        updateNowPlayingInfo()

        resolveControlViews()
    }

    /**
     * {@inheritDoc}
     */
    override fun onStart() {
        super.onStart()
        // Refresh the current time
        val next = refreshCurrentTime()
        queueNextRefresh(next)
        MusicPlaybackController.notifyForegroundStateChanged(
            requireActivity(),
            MusicPlaybackController.isPlaying()
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun onStop() {
        super.onStop()
        MusicPlaybackController.notifyForegroundStateChanged(requireActivity(), false)
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        playDispose.dispose()
        mCompositeDisposable.dispose()
        mTimeHandler?.removeMessages(REFRESH_TIME)
        mTimeHandler = null
        mBroadcastDisposable.dispose()
        PicassoInstance.with().cancelTag(PLAYER_TAG)
        super.onDestroy()
    }

    private fun updateCovers() {
        coverAdapter?.updateAudios(MusicPlaybackController.getQueue())
    }

    val target = object : BitmapTarget {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            if (isAdded) {
                playerGradientFirst?.visibility = View.VISIBLE
                playerGradientSecond?.visibility = View.VISIBLE
                ivBackground?.let {
                    FadeAnimDrawable.setBitmap(
                        it,
                        requireActivity(),
                        bitmap,
                        MusicPlaybackController.isPlaying()
                    )
                }
            }
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
            if (isAdded) {
                if (ivBackground?.drawable is Animatable) {
                    (ivBackground?.drawable as Animatable).stop()
                }
                ivBackground?.setImageDrawable(null)
                playerGradientFirst?.visibility = View.GONE
                playerGradientSecond?.visibility = View.GONE
            }
        }
    }

    /**
     * Sets the track name, album name, and album art.
     */
    private fun updateNowPlayingInfo() {
        val audioTrack = MusicPlaybackController.getCurrentAudio()
        if (mGetLyrics != null) {
            if (audioTrack != null && audioTrack.lyricsId != 0) mGetLyrics?.visibility =
                View.VISIBLE else mGetLyrics?.visibility = View.GONE
        }
        if (tvAlbum != null) {
            var album = ""
            if (!isEmpty(audioTrack?.album_title)) album += requireActivity().getString(R.string.album) + " " + audioTrack?.album_title
            tvAlbum?.text = album
        }
        tvTitle?.text = audioTrack?.artist
        tvSubtitle?.text = audioTrack?.title

        if (Settings.get().other().isBlur_for_player) {
            val coverUrl =
                firstNonEmptyString(
                    audioTrack?.thumb_image_very_big,
                    audioTrack?.thumb_image_big,
                    audioTrack?.thumb_image_little
                )
            if (coverUrl != null) {
                PicassoInstance.with()
                    .load(coverUrl)
                    .tag(PLAYER_TAG)
                    .transform(
                        BlurTransformation(
                            Settings.get().other().playerCoverBackgroundSettings.blur.toFloat(),
                            requireActivity()
                        )
                    )
                    .into(target)
            } else {
                PicassoInstance.with().cancelRequest(target)
                if (ivBackground?.drawable is Animatable) {
                    (ivBackground?.drawable as Animatable).stop()
                }
                ivBackground?.setImageDrawable(null)
                playerGradientFirst?.visibility = View.GONE
                playerGradientSecond?.visibility = View.GONE
            }
        }

        MusicPlaybackController.getCurrentAudioPos()?.let {
            currentPage = it
            ivCoverPager?.setCurrentItem(it, false)
        }

        resolveAddButton()
        val current = MusicPlaybackController.getCurrentAudio()
        if (current != null) {
            when {
                TrackIsDownloaded(current) == 1 -> {
                    ivSave?.setImageResource(R.drawable.succ)
                }
                TrackIsDownloaded(current) == 2 -> {
                    ivSave?.setImageResource(R.drawable.remote_cloud)
                }
                isEmpty(current.url) -> {
                    ivSave?.setImageResource(R.drawable.audio_died)
                }
                ("https://vk.com/mp3/audio_api_unavailable.mp3" == current.url) -> {
                    ivSave?.setImageResource(R.drawable.report)
                }
                else -> ivSave?.setImageResource(R.drawable.save)
            }
        } else ivSave?.setImageResource(R.drawable.save)

        //handle VK actions
        if (current != null && isAudioStreaming) {
            broadcastAudio()
        }

        // Set the total time
        resolveTotalTime()
    }

    private fun resolveTotalTime() {
        if (!isAdded || mTotalTime == null) {
            return
        }
        if (MusicPlaybackController.isInitialized()) {
            mTotalTime?.text =
                MusicPlaybackController.makeTimeString(
                    requireActivity(),
                    MusicPlaybackController.duration() / 1000
                )
        }
    }

    /**
     * Sets the correct drawable states for the playback controls.
     */
    private fun updatePlaybackControls() {
        if (!isAdded) {
            return
        }

        // Set the play and pause image
        mPlayPauseButton?.updateState()

        // Set the shuffle image
        mShuffleButton?.updateShuffleState()

        // Set the repeat image
        mRepeatButton?.updateRepeatState()
    }

    private fun startEffectsPanel() {
        try {
            val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            effects.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireActivity().packageName)
            effects.putExtra(
                AudioEffect.EXTRA_AUDIO_SESSION,
                MusicPlaybackController.getAudioSessionId()
            )
            effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            requestEqualizer.launch(effects)
        } catch (ignored: ActivityNotFoundException) {
            view?.let {
                Snackbar.make(it, R.string.no_system_equalizer, BaseTransientBottomBar.LENGTH_LONG)
                    .setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#eeff0000"))
                    .setActionTextColor(Color.WHITE).show()
            }
        }
    }

    private val isEqualizerAvailable: Boolean
        get() {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            val manager = requireActivity().packageManager
            val info = manager.queryIntentActivities(intent, 0)
            return info.size > 0
        }

    private fun shareAudio() {
        val current = MusicPlaybackController.getCurrentAudio() ?: return
        if (current.isLocal || current.isLocalServer) {
            CreateCustomToast(requireActivity()).showToastError(R.string.not_supported)
            return
        }
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), mAccountId, current)
    }

    private fun resolveAddButton() {
        if (Settings.get().main().isPlayer_support_volume) return
        if (!isAdded) return
        val currentAudio = MusicPlaybackController.getCurrentAudio() ?: return
        //ivAdd.setVisibility(currentAudio == null ? View.INVISIBLE : View.VISIBLE);
        val myAudio = currentAudio.ownerId == mAccountId
        val icon =
            if (myAudio && !currentAudio.isDeleted) R.drawable.ic_outline_delete else R.drawable.plus
        ivAdd?.setImageResource(icon)
    }

    private fun broadcastAudio() {
        mBroadcastDisposable.clear()
        val currentAudio = MusicPlaybackController.getCurrentAudio() ?: return
        if (currentAudio.isLocal || currentAudio.isLocalServer) {
            return
        }
        val accountId = mAccountId
        val targetIds: Collection<Int> = setOf(accountId)
        val id = currentAudio.id
        val ownerId = currentAudio.ownerId
        mBroadcastDisposable.add(
            mAudioInteractor.sendBroadcast(accountId, ownerId, id, targetIds)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe({}) { })
    }

    /**
     * @param delay When to update
     */
    private fun queueNextRefresh(delay: Long) {
        val message = mTimeHandler?.obtainMessage(REFRESH_TIME)
        mTimeHandler?.removeMessages(REFRESH_TIME)
        if (message != null) {
            mTimeHandler?.sendMessageDelayed(message, delay)
        }
    }

    private fun resolveControlViews() {
        if (!isAdded || mProgress == null) return
        val preparing = MusicPlaybackController.isPreparing()
        val initialized = MusicPlaybackController.isInitialized()
        mProgress?.isEnabled = !preparing && initialized
        //mProgress?.isIndeterminate = preparing
    }

    /**
     * Used to scan backwards in time through the curren track
     *
     * @param repcnt The repeat count
     * @param deltal  The long press duration
     */
    private fun scanBackward(repcnt: Int, deltal: Long) {
        var delta = deltal
        if (MusicPlaybackController.mService == null) {
            return
        }
        if (repcnt == 0) {
            mStartSeekPos = MusicPlaybackController.position()
            mLastSeekEventTime = 0
        } else {
            delta = if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta * 10
            } else {
                // seek at 40x after that
                50000 + (delta - 5000) * 40
            }
            var newpos = mStartSeekPos - delta
            if (newpos < 0) {
                // move to previous track
                MusicPlaybackController.previous(requireActivity())
                val duration = MusicPlaybackController.duration()
                mStartSeekPos += duration
                newpos += duration
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicPlaybackController.seek(newpos)
                mLastSeekEventTime = delta
            }
            mPosOverride = if (repcnt >= 0) {
                newpos
            } else {
                -1
            }
            refreshCurrentTime()
        }
    }

    /**
     * Used to scan forwards in time through the curren track
     *
     * @param repcnt The repeat count
     * @param deltal  The long press duration
     */
    private fun scanForward(repcnt: Int, deltal: Long) {
        var delta = deltal
        if (MusicPlaybackController.mService == null) {
            return
        }
        if (repcnt == 0) {
            mStartSeekPos = MusicPlaybackController.position()
            mLastSeekEventTime = 0
        } else {
            delta = if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta * 10
            } else {
                // seek at 40x after that
                50000 + (delta - 5000) * 40
            }
            var newpos = mStartSeekPos + delta
            val duration = MusicPlaybackController.duration()
            if (newpos >= duration) {
                // move to next track
                MusicPlaybackController.next()
                mStartSeekPos -= duration // is OK to go negative
                newpos -= duration
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicPlaybackController.seek(newpos)
                mLastSeekEventTime = delta
            }
            mPosOverride = if (repcnt >= 0) {
                newpos
            } else {
                -1
            }
            refreshCurrentTime()
        }
    }

    private fun refreshCurrentTimeText(pos: Long) {
        mCurrentTime?.text = MusicPlaybackController.makeTimeString(requireActivity(), pos / 1000)
    }

    private fun refreshCurrentTime(): Long {
        //Logger.d("refreshTime", String.valueOf(mService == null));
        if (!MusicPlaybackController.isInitialized()) {
            mCurrentTime?.text = "--:--"
            mTotalTime?.text = "--:--"
            mProgress?.setDuration(DefaultTimeBar.TIME_UNSET)
            mProgress?.setPosition(DefaultTimeBar.TIME_UNSET)
            mProgress?.setBufferedPosition(DefaultTimeBar.TIME_UNSET)
            return 500
        }
        try {
            val pos = if (mPosOverride < 0) MusicPlaybackController.position() else mPosOverride
            val duration = MusicPlaybackController.duration()
            if (pos >= 0 && duration > 0) {
                refreshCurrentTimeText(pos)
                mProgress?.setDuration(duration)
                mProgress?.setPosition(pos)
                mProgress?.setBufferedPosition(MusicPlaybackController.bufferPosition())
                when {
                    mFromTouch -> {
                        return 500
                    }
                    MusicPlaybackController.isPlaying() -> {
                        mCurrentTime?.visibility = View.VISIBLE
                    }
                    else -> {
                        // blink the counter
                        val vis = mCurrentTime?.visibility
                        mCurrentTime?.visibility =
                            if (vis == View.INVISIBLE) View.VISIBLE else View.INVISIBLE
                        return 500
                    }
                }
            } else {
                mCurrentTime?.text = "--:--"
                mProgress?.setDuration(DefaultTimeBar.TIME_UNSET)
                mProgress?.setPosition(DefaultTimeBar.TIME_UNSET)
                mProgress?.setBufferedPosition(DefaultTimeBar.TIME_UNSET)
                val current = if (mTotalTime?.tag == null) 0 else mTotalTime?.tag as Int
                val next = if (current == mPlayerProgressStrings.size - 1) 0 else current + 1
                mTotalTime?.tag = next
                mTotalTime?.text = mPlayerProgressStrings[next]
                return 500
            }

            // calculate the number of milliseconds until the next full second,
            // so
            // the counter can be updated at just the right time
            val remaining = duration - pos % duration

            // approximate how often we would need to refresh the slider to
            // move it smoothly
            var width = mProgress?.width ?: 0
            if (width == 0) {
                width = 320
            }
            val smoothrefreshtime = duration / width
            if (smoothrefreshtime > remaining) {
                return min(remaining, 500)
            }
            return if (smoothrefreshtime < 20) {
                20
            } else min(smoothrefreshtime, 500)
        } catch (ignored: Exception) {
        }
        return 500
    }

    /**
     * Used to update the current time string
     */
    private class TimeHandler(player: AudioPlayerFragment) : Handler(Looper.getMainLooper()) {
        private val mAudioPlayer: WeakReference<AudioPlayerFragment> = WeakReference(player)
        override fun handleMessage(msg: Message) {
            if (msg.what == REFRESH_TIME) {
                mAudioPlayer.get()?.let { it.queueNextRefresh(it.refreshCurrentTime()) }
            }
        }

    }

    private inner class CoverViewHolder(view: View) : RecyclerView.ViewHolder(view), Callback {
        val ivCover: RLottieShapeableImageView = view.findViewById(R.id.cover)
        val mPicassoLoadCallback = WeakPicassoLoadCallback(this)

        fun bind(audioTrack: Audio) {
            val coverUrl =
                firstNonEmptyString(
                    audioTrack.thumb_image_big,
                    audioTrack.thumb_image_very_big,
                    audioTrack.thumb_image_little
                )
            if (coverUrl != null) {
                PicassoInstance.with()
                    .load(coverUrl)
                    .tag(PLAYER_TAG)
                    .into(ivCover, mPicassoLoadCallback)
            } else {
                PicassoInstance.with().cancelRequest(ivCover)
                ivCover.scaleType = ImageView.ScaleType.CENTER
                if (FenrirNative.isNativeLoaded()) {
                    ivCover.fromRes(
                        R.raw.auidio_no_cover,
                        450,
                        450,
                        intArrayOf(
                            0x333333,
                            CurrentTheme.getColorSurface(requireActivity()),
                            0x777777,
                            CurrentTheme.getColorOnSurface(requireActivity())
                        )
                    )
                    ivCover.playAnimation()
                } else {
                    ivCover.setImageResource(R.drawable.itunes)
                    ivCover.drawable.setTint(CurrentTheme.getColorOnSurface(requireActivity()))
                }
            }
        }

        override fun onSuccess() {
            ivCover.scaleType = ImageView.ScaleType.FIT_START
        }

        override fun onError(t: Throwable) {
            ivCover.scaleType = ImageView.ScaleType.CENTER
            if (FenrirNative.isNativeLoaded()) {
                ivCover.fromRes(
                    R.raw.auidio_no_cover, 450, 450, intArrayOf(
                        0x333333,
                        CurrentTheme.getColorSurface(requireActivity()),
                        0x777777,
                        CurrentTheme.getColorOnSurface(requireActivity())
                    )
                )
                ivCover.playAnimation()
            } else {
                ivCover.setImageResource(R.drawable.itunes)
                ivCover.drawable?.setTint(
                    CurrentTheme.getColorOnSurface(
                        requireActivity()
                    )
                )
            }
        }
    }

    private inner class CoverAdapter : RecyclerView.Adapter<CoverViewHolder>() {
        private val mAudios = ArrayList<Audio>()

        @SuppressLint("ClickableViewAccessibility")
        override fun onCreateViewHolder(container: ViewGroup, viewType: Int): CoverViewHolder {
            return CoverViewHolder(
                LayoutInflater.from(container.context)
                    .inflate(R.layout.player_cover_picture, container, false)
            )
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateAudios(audios: List<Audio>?) {
            mAudios.clear()
            if (!isEmpty(audios)) {
                if (audios != null) {
                    mAudios.addAll(audios)
                }
            }
            notifyDataSetChanged()
        }

        override fun onViewDetachedFromWindow(holder: CoverViewHolder) {
            super.onViewDetachedFromWindow(holder)
            PicassoInstance.with().cancelRequest(holder.ivCover)
            if (holder.ivCover.drawable is Animatable) {
                (holder.ivCover.drawable as Animatable).stop()
            }
        }

        override fun onViewAttachedToWindow(holder: CoverViewHolder) {
            super.onViewAttachedToWindow(holder)
            if (holder.ivCover.drawable is Animatable) {
                (holder.ivCover.drawable as Animatable).start()
            }
        }

        override fun onBindViewHolder(holder: CoverViewHolder, position: Int) {
            val audio = mAudios[position]
            holder.bind(audio)
        }

        override fun getItemCount(): Int {
            return mAudios.size
        }
    }

    companion object {
        // Message to refresh the time
        private const val REFRESH_TIME = 1

        @JvmStatic
        fun buildArgs(accountId: Int): Bundle {
            val bundle = Bundle()
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            return bundle
        }

        fun newInstance(accountId: Int): AudioPlayerFragment {
            return newInstance(buildArgs(accountId))
        }

        @JvmStatic
        fun newInstance(args: Bundle?): AudioPlayerFragment {
            val fragment = AudioPlayerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
