package dev.ragnarok.fenrir.fragment.localserver.audioslocalserver

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.isNowPlayingOrPreparingOrPaused
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playOrPause
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playerStatus
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.stop
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.toColor
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter
import dev.ragnarok.fenrir.view.natives.animation.ThorVGLottieView

class AudiosLocalServerAdapter(
    private var data: List<Audio>
) : RecyclerView.Adapter<AudiosLocalServerAdapter.AudioHolder>() {
    private var mClickListener: ClickListener? = null
    private var currAudio: Audio? = currentAudio
    private val isAudioRoundIcon: Boolean = Settings.get().main().isAudio_round_icon
    private val isLongPressDownload: Boolean = Settings.get().main().isUse_long_click_download

    fun setItems(data: List<Audio>) {
        this.data = data
        notifyDataSetChanged()
    }

    @get:DrawableRes
    private val audioCoverSimple: Int
        get() = if (Settings.get()
                .main().isAudio_round_icon
        ) R.drawable.audio_button else R.drawable.audio_button_material

    private val transformCover: Transformation by lazy {
        if (isAudioRoundIcon) RoundTransformation() else PolyTransformation()
    }

    private fun updateAudioStatus(holder: AudioHolder, audio: Audio) {
        if (audio != currAudio) {
            if (holder.visual.drawable is Animatable || holder.visual.drawable == null) {
                holder.visual.setImageResource(audio.songIcon)
                holder.playCover.clearColorFilter()
            }
            return
        }
        when (playerStatus()) {
            1 -> {
                Utils.doWavesLottie(holder.visual, true)
                holder.playCover.setColorFilter("#44000000".toColor())
            }

            2 -> {
                Utils.doWavesLottie(holder.visual, false)
                holder.playCover.setColorFilter("#44000000".toColor())
            }
        }
    }

    fun onServiceBindEvent(@PlayerStatus status: Int) {
        when (status) {
            PlayerStatus.UPDATE_METADATA, PlayerStatus.SERVICE_KILLED, PlayerStatus.UPDATE_PLAY_PAUSE -> {
                val tmpCurrAudio = currAudio
                currAudio = currentAudio

                if (status != PlayerStatus.UPDATE_METADATA || tmpCurrAudio != currAudio) {
                    updateAudio(tmpCurrAudio)
                    updateAudio(currAudio)
                }
            }

            PlayerStatus.REPEAT_MODE_CHANGED, PlayerStatus.SHUFFLE_MODE_CHANGED, PlayerStatus.UPDATE_PLAY_LIST, PlayerStatus.UPDATE_TRACK_INFO -> {}
        }
    }

    private fun indexOf(startPos: Int, audio: Audio): Int {
        var ret = -1
        for (i in startPos..<data.size) {
            if (data[i] == audio) {
                ret = i
                break
            }
        }
        return ret
    }

    private fun updateAudio(audio: Audio?) {
        audio ?: return
        var pos = indexOf(0, audio)
        while (pos != -1) {
            notifyItemRangeChanged(pos, 1)
            pos = indexOf(pos + 1, audio)
        }
    }

    private fun updateDownloadState(holder: AudioHolder, audio: Audio) {
        if (audio.downloadIndicator == 2) {
            holder.saved.setImageResource(R.drawable.remote_cloud)
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorSecondary(holder.saved.context))
        } else {
            holder.saved.setImageResource(R.drawable.save)
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorPrimary(holder.saved.context))
        }
        holder.saved.visibility =
            if (audio.downloadIndicator != 0) View.VISIBLE else View.GONE
    }

    private fun doPlay(position: Int, audio: Audio) {
        if (isNowPlayingOrPreparingOrPaused(audio)) {
            if (!Settings.get().main().isUse_stop_audio) {
                playOrPause()
            } else {
                stop()
            }
        } else {
            mClickListener?.onClick(position, audio)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioHolder {
        return AudioHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_audio_local_server, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AudioHolder, position: Int) {
        val audio = data[position]
        holder.cancelSelectionAnimation()
        if (audio.isAnimationNow) {
            holder.startSelectionAnimation()
            audio.isAnimationNow = false
        }
        holder.artist.text = audio.artist
        holder.title.text = audio.title
        if (audio.duration <= 0) holder.time.visibility = View.INVISIBLE else {
            holder.time.visibility = View.VISIBLE
            holder.time.text = Utils.BytesToSize(audio.duration.toLong())
        }
        updateDownloadState(holder, audio)
        updateAudioStatus(holder, audio)
        if (audio.thumb_image_little.nonNullNoEmpty()) {
            with()
                .load(audio.thumb_image_little)
                .placeholder(
                    ResourcesCompat.getDrawable(
                        holder.playCover.context.resources,
                        audioCoverSimple,
                        holder.playCover.context.theme
                    ) ?: return
                )
                .transform(transformCover)
                .tag(Constants.PICASSO_TAG)
                .into(holder.playCover)
        } else {
            with().cancelRequest(holder.playCover)
            holder.playCover.setImageResource(audioCoverSimple)
        }
        holder.play.setOnLongClickListener {
            if ((audio.thumb_image_very_big.nonNullNoEmpty() || audio.thumb_image_big.nonNullNoEmpty() || audio.thumb_image_little.nonNullNoEmpty()) && audio.artist.nonNullNoEmpty() && audio.title.nonNullNoEmpty()) {
                Utils.firstNonEmptyString(
                    audio.thumb_image_very_big,
                    audio.thumb_image_big,
                    audio.thumb_image_little
                )?.let {
                    audio.artist?.let { it1 ->
                        audio.title?.let { it2 ->
                            mClickListener?.onUrlPhotoOpen(
                                it, it1, it2
                            )
                        }
                    }
                }
            }
            true
        }
        holder.play.setOnClickListener {
            if (Settings.get().main().isRevert_play_audio) {
                mClickListener?.openMenu(
                    holder.bindingAdapterPosition,
                    audio
                )
            } else {
                doPlay(holder.bindingAdapterPosition, audio)
            }
        }
        if (isLongPressDownload) {
            holder.trackOption.setOnLongClickListener {
                mClickListener?.onDownload(holder.bindingAdapterPosition, audio)
                true
            }
        }
        holder.trackOption.setOnClickListener {
            holder.cancelSelectionAnimation()
            holder.startSomeAnimation()
            if (Settings.get().main().isRevert_play_audio) {
                doPlay(holder.bindingAdapterPosition, audio)
            } else {
                mClickListener?.openMenu(
                    holder.bindingAdapterPosition,
                    audio
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setClickListener(clickListener: ClickListener?) {
        mClickListener = clickListener
    }

    interface ClickListener {
        fun onClick(position: Int, audio: Audio)
        fun onUrlPhotoOpen(url: String, prefix: String, photoPrefix: String)
        fun openMenu(position: Int, audio: Audio)
        fun onDownload(position: Int, audio: Audio)
    }

    class AudioHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artist: TextView = itemView.findViewById(R.id.dialog_title)
        val title: TextView = itemView.findViewById(R.id.dialog_message)
        val play: View = itemView.findViewById(R.id.item_audio_play)
        val playCover: ImageView = itemView.findViewById(R.id.item_audio_play_cover)
        val trackOption: View = itemView.findViewById(R.id.track_option)
        val saved: ImageView = itemView.findViewById(R.id.saved)
        val selectionView: MaterialCardView = itemView.findViewById(R.id.item_audio_selection)
        private val animationAdapter: Animator.AnimatorListener =
            object : WeakViewAnimatorAdapter<View>(selectionView) {
                override fun onAnimationEnd(view: View) {
                    view.visibility = View.GONE
                }

                override fun onAnimationStart(view: View) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(view: View) {
                    view.visibility = View.GONE
                }
            }
        val visual: ThorVGLottieView = itemView.findViewById(R.id.item_audio_visual)
        val time: TextView = itemView.findViewById(R.id.item_audio_time)
        var animator: ObjectAnimator? = null
        fun startSelectionAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorPrimary(selectionView.context))
            selectionView.alpha = 0.5f
            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f)
            animator?.duration = 1500
            animator?.addListener(animationAdapter)
            animator?.start()
        }

        fun startSomeAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorSecondary(selectionView.context))
            selectionView.alpha = 0.5f
            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f)
            animator?.duration = 500
            animator?.addListener(animationAdapter)
            animator?.start()
        }

        fun cancelSelectionAnimation() {
            animator?.cancel()
            animator = null
            selectionView.visibility = View.INVISIBLE
        }
    }
}