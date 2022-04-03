package dev.ragnarok.fenrir.media.gif

import android.view.SurfaceHolder
import dev.ragnarok.fenrir.model.VideoSize

interface IGifPlayer {
    val videoSize: VideoSize?
    fun play()
    fun pause()
    fun setDisplay(holder: SurfaceHolder?)
    fun release()
    fun addVideoSizeChangeListener(listener: IVideoSizeChangeListener)
    fun addStatusChangeListener(listener: IStatusChangeListener)
    fun removeVideoSizeChangeListener(listener: IVideoSizeChangeListener)
    fun removeStatusChangeListener(listener: IStatusChangeListener)
    var playerStatus: Int

    interface IStatus {
        companion object {
            const val INIT = 1
            const val PREPARING = 2
            const val PREPARED = 3
            const val ENDED = 4
        }
    }

    interface IVideoSizeChangeListener {
        fun onVideoSizeChanged(player: IGifPlayer, size: VideoSize?)
    }

    interface IStatusChangeListener {
        fun onPlayerStatusChange(player: IGifPlayer, previousStatus: Int, currentStatus: Int)
    }
}