package dev.ragnarok.fenrir.media.story

import android.view.SurfaceHolder
import android.view.TextureView
import dev.ragnarok.fenrir.model.VideoSize

interface IStoryPlayer {
    fun updateSource(url: String?)
    val videoSize: VideoSize?
    fun play()
    fun pause()
    fun setVideoSurfaceHolder(holder: SurfaceHolder?)
    fun setVideoTextureView(view: TextureView?)
    fun release()
    fun addVideoSizeChangeListener(listener: IVideoSizeChangeListener)
    fun addStatusChangeListener(listener: IStatusChangeListener)
    fun addPositionChangeListener(listener: IPositionChangeListener)
    fun removeVideoSizeChangeListener(listener: IVideoSizeChangeListener)
    fun removeStatusChangeListener(listener: IStatusChangeListener)
    fun removePositionChangeListener(listener: IPositionChangeListener)
    var playerStatus: Int
    var playbackSpeed: Float
    fun togglePlaybackSpeed()

    interface IStatus {
        companion object {
            const val INIT = 1
            const val PREPARING = 2
            const val PREPARED = 3
            const val ENDED = 4
        }
    }

    interface IVideoSizeChangeListener {
        fun onVideoSizeChanged(player: IStoryPlayer, size: VideoSize)
    }

    interface IStatusChangeListener {
        fun onPlayerStatusChange(player: IStoryPlayer, previousStatus: Int, currentStatus: Int)
    }

    interface IPositionChangeListener {
        fun onPlayerPositionChange(
            player: IStoryPlayer,
            previousPosition: Long,
            currentPosition: Long
        )
    }
}