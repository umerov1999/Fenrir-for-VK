package dev.ragnarok.fenrir.fragment.audio.audios

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.drawable.Animatable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.base.RecyclerBindableAdapter
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
import dev.ragnarok.fenrir.util.AppTextUtils
import dev.ragnarok.fenrir.util.DownloadWorkUtils.TrackIsDownloaded
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter
import dev.ragnarok.fenrir.view.natives.animation.ThorVGLottieView

class AudiosAdapter(
    data: MutableList<Audio>,
    private val notShowMy: Boolean,
    private var iSSelectMode: Boolean
) : RecyclerBindableAdapter<Audio, AudiosAdapter.AudioHolder>(data) {
    private val isLongPressDownload: Boolean = Settings.get().main().isUse_long_click_download
    private val isAudioRoundIcon: Boolean = Settings.get().main().isAudio_round_icon
    private var mClickListener: ClickListener? = null
    private var currAudio: Audio? = currentAudio

    fun toggleSelectMode(iSSelectMode: Boolean) {
        this.iSSelectMode = iSSelectMode
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

    private fun updateAudio(audio: Audio?) {
        audio ?: return
        var pos = indexOf(0, audio)
        while (pos != -1) {
            notifyItemBindableRangeChanged(pos, 1)
            pos = indexOf(pos + 1, audio)
        }
    }

    @get:DrawableRes
    private val audioCoverSimple: Int
        get() = if (isAudioRoundIcon) R.drawable.audio_button else R.drawable.audio_button_material

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

    override fun onBindItemViewHolder(viewHolder: AudioHolder, position: Int, type: Int) {
        val audio = getItem(position)
        viewHolder.cancelSelectionAnimation()
        if (audio.isAnimationNow) {
            viewHolder.startSelectionAnimation()
            audio.isAnimationNow = false
        }
        viewHolder.artist.text = audio.artist
        if (!audio.isLocal && !audio.isLocalServer && Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID && !audio.isHLS) {
            viewHolder.quality.visibility = View.VISIBLE
            if (audio.isHq) {
                viewHolder.quality.setImageResource(R.drawable.high_quality)
            } else {
                viewHolder.quality.setImageResource(R.drawable.low_quality)
            }
        } else {
            viewHolder.quality.visibility = View.GONE
        }
        viewHolder.title.text = audio.title
        if (audio.duration <= 0) viewHolder.time.visibility = View.INVISIBLE else {
            viewHolder.time.visibility = View.VISIBLE
            if (audio.isLocalServer) {
                viewHolder.time.text = Utils.BytesToSize(audio.duration.toLong())
            } else {
                viewHolder.time.text = AppTextUtils.getDurationString(audio.duration)
            }
        }
        viewHolder.lyric.visibility = if (audio.lyricsId != 0) View.VISIBLE else View.GONE
        viewHolder.isSelectedView.visibility = if (audio.isSelected) View.VISIBLE else View.GONE
        if (audio.isSelected) {
            when {
                audio.url.isNullOrEmpty() -> {
                    viewHolder.isSelectedView.setCardBackgroundColor("#ff0000".toColor())
                }

                TrackIsDownloaded(audio) != 0 -> {
                    viewHolder.isSelectedView.setCardBackgroundColor("#00aa00".toColor())
                }

                else -> {
                    viewHolder.isSelectedView.setCardBackgroundColor(
                        CurrentTheme.getColorPrimary(
                            viewHolder.isSelectedView.context
                        )
                    )
                }
            }
        }
        if (notShowMy) viewHolder.my.visibility = View.GONE else viewHolder.my.visibility =
            if (audio.ownerId == Settings.get().accounts().current) View.VISIBLE else View.GONE
        viewHolder.saved.visibility = View.GONE
        updateDownloadState(viewHolder, audio)
        updateAudioStatus(viewHolder, audio)
        if (audio.thumb_image_little.nonNullNoEmpty()) {
            with()
                .load(audio.thumb_image_little)
                .placeholder(
                    ResourcesCompat.getDrawable(
                        viewHolder.playCover.context.resources,
                        audioCoverSimple,
                        viewHolder.playCover.context.theme
                    ) ?: return
                )
                .transform(transformCover)
                .tag(Constants.PICASSO_TAG)
                .into(viewHolder.playCover)
        } else {
            with().cancelRequest(viewHolder.playCover)
            viewHolder.playCover.setImageResource(audioCoverSimple)
        }
        viewHolder.play.setOnLongClickListener {
            if ((audio.thumb_image_very_big.nonNullNoEmpty()
                        || audio.thumb_image_big.nonNullNoEmpty() || audio.thumb_image_little.nonNullNoEmpty()) && audio.artist.nonNullNoEmpty() && audio.title.nonNullNoEmpty()
            ) {
                Utils.firstNonEmptyString(
                    audio.thumb_image_very_big,
                    audio.thumb_image_big, audio.thumb_image_little
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
        viewHolder.play.setOnClickListener {
            if (Settings.get().main().isRevert_play_audio) {
                mClickListener?.openMenu(
                    getItemRawPosition(viewHolder.bindingAdapterPosition),
                    audio
                )
            } else {
                doPlay(getItemRawPosition(viewHolder.bindingAdapterPosition), audio)
            }
        }
        if (!iSSelectMode) {
            if (isLongPressDownload) {
                viewHolder.trackOption.setOnLongClickListener {
                    mClickListener?.onDownload(
                        getItemRawPosition(viewHolder.bindingAdapterPosition),
                        audio
                    )
                    true
                }
            }
            viewHolder.trackOption.setOnClickListener {
                viewHolder.cancelSelectionAnimation()
                viewHolder.startSomeAnimation()
                if (Settings.get().main().isRevert_play_audio) {
                    doPlay(getItemRawPosition(viewHolder.bindingAdapterPosition), audio)
                } else {
                    mClickListener?.openMenu(
                        getItemRawPosition(viewHolder.bindingAdapterPosition),
                        audio
                    )
                }
            }
        } else {
            viewHolder.trackOption.setOnClickListener {
                audio.isSelected = !audio.isSelected
                viewHolder.isSelectedView.visibility =
                    if (audio.isSelected) View.VISIBLE else View.GONE
                when {
                    audio.url.isNullOrEmpty() -> {
                        viewHolder.isSelectedView.setCardBackgroundColor("#ff0000".toColor())
                    }

                    TrackIsDownloaded(audio) != 0 -> {
                        viewHolder.isSelectedView.setCardBackgroundColor("#00aa00".toColor())
                    }

                    else -> {
                        viewHolder.isSelectedView.setCardBackgroundColor(
                            CurrentTheme.getColorPrimary(
                                viewHolder.isSelectedView.context
                            )
                        )
                    }
                }
            }
        }
    }

    override fun viewHolder(view: View, type: Int): AudioHolder {
        return AudioHolder(view)
    }

    override fun layoutId(type: Int): Int {
        return R.layout.item_audio
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
        val visual: ThorVGLottieView = itemView.findViewById(R.id.item_audio_visual)
        val time: TextView = itemView.findViewById(R.id.item_audio_time)
        val saved: ImageView = itemView.findViewById(R.id.saved)
        val lyric: ImageView = itemView.findViewById(R.id.lyric)
        val my: ImageView = itemView.findViewById(R.id.my)
        val quality: ImageView = itemView.findViewById(R.id.quality)
        val trackOption: View = itemView.findViewById(R.id.track_option)
        val selectionView: MaterialCardView = itemView.findViewById(R.id.item_audio_selection)
        val isSelectedView: MaterialCardView = itemView.findViewById(R.id.item_audio_select_add)
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