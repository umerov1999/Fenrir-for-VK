package dev.ragnarok.filegallery.media.video

import android.content.Context
import android.view.SurfaceHolder
import android.view.TextureView

interface IVideoPlayer {
    fun updateSource(
        context: Context,
        url: String?
    )

    fun play()
    fun pause()
    fun release()
    val duration: Long
    val currentPosition: Long
    fun seekTo(position: Long)
    val isPlaying: Boolean
    val bufferPercentage: Int
    val bufferPosition: Long
    fun setSurfaceHolder(holder: SurfaceHolder?)
    fun setTextureView(view: TextureView?)
    var playbackSpeed: Float
    fun togglePlaybackSpeed()
    fun addVideoSizeChangeListener(listener: IVideoSizeChangeListener)
    fun removeVideoSizeChangeListener(listener: IVideoSizeChangeListener)
    interface IVideoSizeChangeListener {
        fun onVideoSizeChanged(player: IVideoPlayer, w: Int, h: Int)
    }

    interface IUpdatePlayListener {
        fun onPlayChanged(isPause: Boolean)
    }
}
