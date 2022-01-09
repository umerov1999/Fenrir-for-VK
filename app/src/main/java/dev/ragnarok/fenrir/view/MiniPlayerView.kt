package dev.ragnarok.fenrir.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.Injection
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.player.MusicPlaybackController
import dev.ragnarok.fenrir.player.MusicPlaybackController.PlayerStatus
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Objects
import dev.ragnarok.fenrir.util.RxUtils
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView
import dev.ragnarok.fenrir.view.seek.DefaultTimeBar
import dev.ragnarok.fenrir.view.seek.TimeBar
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.ref.WeakReference

class MiniPlayerView : FrameLayout, TimeBar.OnScrubListener {
    private var mPlayerDisposable = Disposable.disposed()
    private var mAccountDisposable = Disposable.disposed()
    private var mAccountId = 0
    private lateinit var visual: RLottieImageView
    private lateinit var playCover: ImageView
    private lateinit var title: TextView
    private lateinit var mProgress: DefaultTimeBar
    private var mFromTouch = false
    private var mPosOverride: Long = -1
    private lateinit var root: View
    private var mTimeHandler: TimeHandler? = null
    private var mLastSeekEventTime: Long = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        root = LayoutInflater.from(context).inflate(R.layout.mini_player, this)
        val play = root.findViewById<View>(R.id.item_audio_play)
        playCover = root.findViewById(R.id.item_audio_play_cover)
        visual = root.findViewById(R.id.item_audio_visual)
        root.visibility = if (MusicPlaybackController.getMiniPlayerVisibility()) VISIBLE else GONE
        val mPClosePlay = root.findViewById<ImageButton>(R.id.close_player)
        mPClosePlay.setOnClickListener {
            MusicPlaybackController.closeMiniPlayer()
            root.visibility = GONE
        }
        mPClosePlay.setOnLongClickListener {
            MusicPlaybackController.stop()
            true
        }
        play.setOnClickListener {
            MusicPlaybackController.playOrPause()
            if (MusicPlaybackController.isPlaying()) {
                Utils.doWavesLottie(visual, true)
                playCover.setColorFilter(Color.parseColor("#44000000"))
            } else {
                Utils.doWavesLottie(visual, false)
                playCover.clearColorFilter()
            }
        }
        play.setOnLongClickListener {
            MusicPlaybackController.next()
            true
        }
        val mOpenPlayer = root.findViewById<ImageButton>(R.id.open_player)
        mOpenPlayer.setOnClickListener {
            PlaceFactory.getPlayerPlace(mAccountId).tryOpenWith(
                context
            )
        }
        title = root.findViewById(R.id.mini_artist)
        title.isSelected = true
        mProgress = root.findViewById(R.id.seek_player_pos)
        mProgress.addListener(this)
    }

    private fun queueNextRefresh(delay: Long) {
        mTimeHandler?.let {
            val message = it.obtainMessage(REFRESH_TIME)
            it.removeMessages(REFRESH_TIME)
            it.sendMessageDelayed(message, delay)
        }
    }

    private fun transformCover(): Transformation {
        return if (Settings.get()
                .main().isAudio_round_icon
        ) RoundTransformation() else PolyTransformation()
    }

    @get:DrawableRes
    private val audioCoverSimple: Int
        get() = if (Settings.get()
                .main().isAudio_round_icon
        ) R.drawable.audio_button else R.drawable.audio_button_material

    private fun updatePlaybackControls() {
        if (Objects.nonNull(playCover)) {
            if (MusicPlaybackController.isPlaying()) {
                Utils.doWavesLottie(visual, true)
                playCover.setColorFilter(Color.parseColor("#44000000"))
            } else {
                Utils.doWavesLottie(visual, false)
                playCover.clearColorFilter()
            }
        }
    }

    private fun receiveFullAudioInfo() {
        updateVisibility()
        updateNowPlayingInfo()
        updatePlaybackControls()
        resolveControlViews()
    }

    private fun onServiceBindEvent(@PlayerStatus status: Int) {
        when (status) {
            PlayerStatus.UPDATE_TRACK_INFO -> {
                updateVisibility()
                updateNowPlayingInfo()
                updatePlaybackControls()
                resolveControlViews()
            }
            PlayerStatus.UPDATE_PLAY_PAUSE -> {
                updateVisibility()
                updatePlaybackControls()
                resolveControlViews()
            }
            PlayerStatus.SERVICE_KILLED -> {
                updateVisibility()
                updatePlaybackControls()
                updateNowPlayingInfo()
                resolveControlViews()
            }
            PlayerStatus.REPEATMODE_CHANGED, PlayerStatus.SHUFFLEMODE_CHANGED, PlayerStatus.UPDATE_PLAY_LIST -> {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNowPlayingInfo() {
        val artist = MusicPlaybackController.getArtistName()
        val trackName = MusicPlaybackController.getTrackName()
        title.text = Utils.firstNonEmptyString(
            artist,
            " "
        ) + " - " + Utils.firstNonEmptyString(trackName, " ")
        if (Objects.nonNull(playCover)) {
            val audio = MusicPlaybackController.getCurrentAudio()
            val placeholder = AppCompatResources.getDrawable(context, audioCoverSimple)
            if (audio != null && placeholder != null && !Utils.isEmpty(audio.thumb_image_little)) {
                with()
                    .load(audio.thumb_image_little)
                    .placeholder(placeholder)
                    .transform(transformCover())
                    .into(playCover)
            } else {
                with().cancelRequest(playCover)
                playCover.setImageResource(audioCoverSimple)
            }
        }
        //queueNextRefresh(1);
    }

    private fun updateVisibility() {
        root.visibility =
            if (MusicPlaybackController.getMiniPlayerVisibility()) VISIBLE else GONE
    }

    private fun resolveControlViews() {
        val preparing = MusicPlaybackController.isPreparing()
        val initialized = MusicPlaybackController.isInitialized()
        mProgress.isEnabled = !preparing && initialized
        //mProgress.setIndeterminate(preparing);
    }

    private fun refreshCurrentTime(): Long {
        if (!MusicPlaybackController.isInitialized()) {
            mProgress.setDuration(DefaultTimeBar.TIME_UNSET)
            mProgress.setPosition(DefaultTimeBar.TIME_UNSET)
            mProgress.setBufferedPosition(DefaultTimeBar.TIME_UNSET)
            return 500
        }
        try {
            val pos = if (mPosOverride < 0) MusicPlaybackController.position() else mPosOverride
            val duration = MusicPlaybackController.duration()
            if (pos >= 0 && duration > 0) {
                mProgress.setDuration(duration)
                mProgress.setPosition(pos)
                mProgress.setBufferedPosition(MusicPlaybackController.bufferPosition())
                if (mFromTouch) {
                    return 500
                } else if (!MusicPlaybackController.isPlaying()) {
                    return 500
                }
            } else {
                mProgress.setDuration(DefaultTimeBar.TIME_UNSET)
                mProgress.setPosition(DefaultTimeBar.TIME_UNSET)
                mProgress.setBufferedPosition(DefaultTimeBar.TIME_UNSET)
                return 500
            }
            return 500
        } catch (ignored: Exception) {
        }
        return 500
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        receiveFullAudioInfo()
        mTimeHandler = TimeHandler(this)
        mAccountId = Settings.get()
            .accounts()
            .current
        mAccountDisposable = Settings.get()
            .accounts()
            .observeChanges()
            .observeOn(Injection.provideMainThreadScheduler())
            .subscribe { v: Int -> mAccountId = v }
        val next = refreshCurrentTime()
        queueNextRefresh(next)
        mPlayerDisposable = MusicPlaybackController.observeServiceBinding()
            .compose(RxUtils.applyObservableIOToMainSchedulers())
            .subscribe { status: Int -> onServiceBindEvent(status) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPlayerDisposable.dispose()
        mAccountDisposable.dispose()
        mTimeHandler?.removeMessages(REFRESH_TIME)
        mTimeHandler = null
    }

    private class TimeHandler(player: MiniPlayerView?) :
        Handler(Looper.getMainLooper()) {
        private val mAudioPlayer: WeakReference<MiniPlayerView?> = WeakReference(player)
        override fun handleMessage(msg: Message) {
            if (msg.what == REFRESH_TIME) {
                mAudioPlayer.get()?.let { it.queueNextRefresh(it.refreshCurrentTime()) }
            }
        }

    }

    companion object {
        private const val REFRESH_TIME = 1
    }
}