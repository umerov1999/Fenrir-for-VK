package dev.ragnarok.fenrir.media.story

import android.view.SurfaceHolder
import android.view.TextureView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dev.ragnarok.fenrir.App.Companion.instance
import dev.ragnarok.fenrir.UserAgentTool
import dev.ragnarok.fenrir.media.story.IStoryPlayer.IStatus
import dev.ragnarok.fenrir.media.story.IStoryPlayer.IStatusChangeListener
import dev.ragnarok.fenrir.model.ProxyConfig
import dev.ragnarok.fenrir.model.VideoSize
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.util.Logger.d
import dev.ragnarok.fenrir.util.Utils.getExoPlayerFactory
import dev.ragnarok.fenrir.util.Utils.makeMediaItem
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.hiddenIO
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.repeatUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class ExoStoryPlayer(
    private val url: String,
    private val proxyConfig: ProxyConfig?,
    private val isRepeat: Boolean
) : IStoryPlayer {
    private val videoSizeChangeListeners: MutableList<IStoryPlayer.IVideoSizeChangeListener> =
        ArrayList(1)
    private val statusChangeListeners: MutableList<IStatusChangeListener> = ArrayList(1)
    private val positionChangeListeners: MutableList<IStoryPlayer.IPositionChangeListener> =
        ArrayList(1)
    override var videoSize: VideoSize? = null
    override var playbackSpeed = 1f
        set(value) {
            field = value
            internalPlayer?.setPlaybackSpeed(value)
        }

    override fun togglePlaybackSpeed() {
        when {
            playbackSpeed in 1f..<2f -> playbackSpeed = 2f
            playbackSpeed in 2f..<2.5f -> playbackSpeed = 2.5f
            playbackSpeed in 2.5f..<3f -> playbackSpeed = 3f
            playbackSpeed in 3f..<3.5f -> playbackSpeed = 3.5f
            playbackSpeed in 3.5f..<4f -> playbackSpeed = 4f
            playbackSpeed >= 4f -> playbackSpeed = 1f
        }
    }

    private val videoListener: Player.Listener = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
            this@ExoStoryPlayer.videoSize = VideoSize(videoSize.width, videoSize.height)
            this@ExoStoryPlayer.onVideoSizeChanged()
        }

        override fun onRenderedFirstFrame() {}
        override fun onPlaybackStateChanged(state: @Player.State Int) {
            d("FenrirExoStoryPlayer", "onPlaybackStateChanged, state: $state")
            onInternalPlayerStateChanged(state)
        }
    }
    override var playerStatus: Int = IStatus.INIT
    private var internalPlayer: ExoPlayer? = null
    private var supposedToBePlaying = false
    private var playingPosition: Long = 0

    private var mRefreshPlaybackDisposable = CancelableJob()

    override fun play() {
        if (supposedToBePlaying) return
        supposedToBePlaying = true
        when (playerStatus) {
            IStatus.PREPARED -> startPlayer(internalPlayer)
            IStatus.INIT -> preparePlayer()
            IStatus.PREPARING -> {}
        }
    }

    override fun updateSource(url: String?) {
        mRefreshPlaybackDisposable.cancel()
        onVideoPositionChanged(0)
        internalPlayer?.stop()
        setStatus(IStatus.PREPARING)
        val userAgent = UserAgentTool.USER_AGENT_CURRENT_ACCOUNT
        val mediaSource: MediaSource =
            ProgressiveMediaSource.Factory(getExoPlayerFactory(userAgent, proxyConfig))
                .createMediaSource(
                    makeMediaItem(
                        url
                    )
                )
        internalPlayer?.playWhenReady = true
        internalPlayer?.setMediaSource(mediaSource)
        internalPlayer?.prepare()
    }

    private fun preparePlayer() {
        setStatus(IStatus.PREPARING)
        internalPlayer = ExoPlayer.Builder(instance).build()
        val userAgent = UserAgentTool.USER_AGENT_CURRENT_ACCOUNT

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:
        val mediaSource: MediaSource =
            ProgressiveMediaSource.Factory(getExoPlayerFactory(userAgent, proxyConfig))
                .createMediaSource(
                    makeMediaItem(
                        url
                    )
                )
        internalPlayer?.repeatMode =
            if (isRepeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        internalPlayer?.addListener(videoListener)
        internalPlayer?.playWhenReady = true
        internalPlayer?.setMediaSource(mediaSource)
        internalPlayer?.setPlaybackSpeed(playbackSpeed)
        internalPlayer?.prepare()
    }

    internal fun onInternalPlayerStateChanged(state: @Player.State Int) {
        if (state == Player.STATE_READY) {
            setStatus(IStatus.PREPARED)
            mRefreshPlaybackDisposable.cancel()
            mRefreshPlaybackDisposable.set(
                flow {
                    withContext(Dispatchers.Main) {
                        val tmp = internalPlayer?.currentPosition.orZero() / 1000
                        if (tmp != playingPosition) {
                            onVideoPositionChanged(tmp)
                            playingPosition = tmp
                        }
                    }
                    emit(true)
                }.repeatUntil({ true }, 500).hiddenIO()
            )
        } else if (state == Player.STATE_ENDED && !isRepeat) {
            setStatus(IStatus.ENDED)
        }
    }

    internal fun onVideoSizeChanged() {
        for (listener in videoSizeChangeListeners) {
            listener.onVideoSizeChanged(this, videoSize ?: VideoSize(1, 1))
        }
    }

    internal fun onVideoPositionChanged(newPosition: Long) {
        for (listener in positionChangeListeners) {
            listener.onPlayerPositionChange(
                this@ExoStoryPlayer,
                playingPosition,
                newPosition
            )
        }
    }

    override fun pause() {
        if (!supposedToBePlaying) return
        supposedToBePlaying = false
        if (internalPlayer != null) {
            try {
                internalPlayer?.let { pausePlayer(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun setVideoSurfaceHolder(holder: SurfaceHolder?) {
        internalPlayer?.setVideoSurfaceHolder(holder)
    }

    override fun setVideoTextureView(view: TextureView?) {
        internalPlayer?.setVideoTextureView(view)
    }

    override fun release() {
        mRefreshPlaybackDisposable.cancel()
        if (internalPlayer != null) {
            try {
                internalPlayer?.removeListener(videoListener)
                internalPlayer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setStatus(newStatus: Int) {
        val oldStatus = playerStatus
        if (playerStatus == newStatus) {
            return
        }
        playerStatus = newStatus
        for (listener in statusChangeListeners) {
            listener.onPlayerStatusChange(this, oldStatus, newStatus)
        }
    }

    override fun addVideoSizeChangeListener(listener: IStoryPlayer.IVideoSizeChangeListener) {
        videoSizeChangeListeners.add(listener)
    }

    override fun addStatusChangeListener(listener: IStatusChangeListener) {
        statusChangeListeners.add(listener)
    }

    override fun addPositionChangeListener(listener: IStoryPlayer.IPositionChangeListener) {
        positionChangeListeners.add(listener)
    }

    override fun removeVideoSizeChangeListener(listener: IStoryPlayer.IVideoSizeChangeListener) {
        videoSizeChangeListeners.remove(listener)
    }

    override fun removeStatusChangeListener(listener: IStatusChangeListener) {
        statusChangeListeners.remove(listener)
    }

    override fun removePositionChangeListener(listener: IStoryPlayer.IPositionChangeListener) {
        positionChangeListeners.remove(listener)
    }

    companion object {
        internal fun pausePlayer(internalPlayer: ExoPlayer) {
            internalPlayer.playWhenReady = false
            internalPlayer.playbackState
        }

        internal fun startPlayer(internalPlayer: ExoPlayer?) {
            internalPlayer?.playWhenReady = true
            internalPlayer?.playbackState
        }
    }
}
