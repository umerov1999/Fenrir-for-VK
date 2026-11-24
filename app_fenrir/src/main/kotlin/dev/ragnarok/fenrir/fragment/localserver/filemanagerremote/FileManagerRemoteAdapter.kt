package dev.ragnarok.fenrir.fragment.localserver.filemanagerremote

import android.content.Context
import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso3.Callback
import com.squareup.picasso3.Picasso
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.media.music.MusicPlaybackController
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playerStatus
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.FileRemote
import dev.ragnarok.fenrir.model.FileType
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.toColor
import dev.ragnarok.fenrir.util.AppTextUtils.pluralNumerical
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.view.natives.animation.ThorVGLottieView

class FileManagerRemoteAdapter(context: Context, private var data: List<FileRemote>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @ColorInt
    private val colorPrimary = CurrentTheme.getColorPrimary(context)

    @ColorInt
    private val colorOnSurface = CurrentTheme.getColorOnSurface(context)

    @ColorInt
    private val messageBubbleColor = CurrentTheme.getMessageBubbleColor(context)
    private var clickListener: ClickListener? = null
    private var currAudio: Audio? = currentAudio

    fun setItems(data: List<FileRemote>) {
        this.data = data
        notifyDataSetChanged()
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

    private fun getIndexAudio(audio: Audio?): Int {
        audio ?: return -1
        for (i in data.indices) {
            if (data[i].id == audio.id && data[i].owner_Id == audio.ownerId) {
                return i
            }
        }
        return -1
    }

    private fun updateAudio(audio: Audio?) {
        val pos = getIndexAudio(audio)
        if (pos != -1) {
            notifyItemChanged(pos)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            FileType.error, FileType.photo, FileType.video -> return FileHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_manager_file, parent, false)
            )

            FileType.folder -> return FileHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_manager_folder, parent, false)
            )

            FileType.audio -> return AudioHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_manager_audio, parent, false)
            )
        }
        return FileHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_manager_file, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            FileType.audio -> onBindAudioHolder(holder as AudioHolder, position)
            else -> onBindFileHolder(holder as FileHolder, position)
        }
    }

    private fun updateAudioStatus(
        holder: AudioHolder,
        audio: FileRemote
    ) {
        if (audio.id != currAudio?.id || audio.owner_Id != currAudio?.ownerId) {
            if (holder.visual.drawable is Animatable || holder.visual.drawable == null) {
                holder.visual.setImageResource(R.drawable.song)
                holder.icon.clearColorFilter()
            }
            return
        }
        when (playerStatus()) {
            1 -> {
                Utils.doWavesLottie(holder.visual, true)
                holder.icon.setColorFilter("#44000000".toColor())
            }

            2 -> {
                Utils.doWavesLottie(holder.visual, false)
                holder.icon.setColorFilter("#44000000".toColor())
            }
        }
    }

    private fun onBindAudioHolder(holder: AudioHolder, position: Int) {
        val item = data[position]

        if (FenrirNative.isNativeLoaded) {
            if (item.isSelected) {
                holder.current.visibility = View.VISIBLE
                holder.current.fromRes(
                    dev.ragnarok.fenrir_common.R.raw.select_fire,
                    intArrayOf(0xFF812E, colorPrimary),
                    true
                )
                holder.current.startAnimation()
            } else {
                holder.current.visibility = View.GONE
                holder.current.releaseAnimation()
            }
        }
        holder.fileInfo.setBackgroundColor(messageBubbleColor)

        PicassoInstance.with()
            .load(item.preview_url).tag(Constants.PICASSO_TAG)
            .priority(Picasso.Priority.LOW)
            .into(holder.icon, object : Callback {
                override fun onSuccess() {
                    holder.visual.clearColorFilter()
                }

                override fun onError(t: Throwable) {
                    holder.visual.setColorFilter(colorOnSurface)
                }
            })
        holder.fileName.text = item.file_name
        holder.fileDetails.text =
            if (item.type != FileType.folder) Utils.BytesToSize(item.size) else pluralNumerical(
                holder.fileDetails.context,
                item.size.toInt(),
                R.string.files_count_a,
                R.string.files_count_b,
                R.string.files_count_c
            )
        holder.itemView.setOnClickListener {
            val t = Audio()
            t.setId(item.id)
            t.setOwnerId(item.owner_Id)
            if (MusicPlaybackController.isNowPlayingOrPreparingOrPaused(t)) {
                if (!Settings.get().main().isUse_stop_audio) {
                    MusicPlaybackController.playOrPause()
                } else {
                    MusicPlaybackController.stop()
                }
            } else {
                clickListener?.onClick(holder.bindingAdapterPosition, item)
            }
        }
        holder.itemView.setOnLongClickListener {
            holder.fileInfo.setBackgroundColor(colorPrimary)
            clickListener?.onOpenAudioMenu(holder.bindingAdapterPosition, item)
            true
        }
        updateAudioStatus(holder, item)
    }

    private fun onBindFileHolder(holder: FileHolder, position: Int) {
        val item = data[position]

        if (FenrirNative.isNativeLoaded) {
            if (item.isSelected) {
                holder.current.visibility = View.VISIBLE
                holder.current.fromRes(
                    dev.ragnarok.fenrir_common.R.raw.select_fire,
                    intArrayOf(0xFF812E, colorPrimary),
                    true
                )
                holder.current.startAnimation()
            } else {
                holder.current.visibility = View.GONE
                holder.current.releaseAnimation()
            }
        }
        holder.fileInfo.setBackgroundColor(messageBubbleColor)

        PicassoInstance.with()
            .load(item.preview_url).tag(Constants.PICASSO_TAG)
            .priority(Picasso.Priority.LOW)
            .into(holder.icon)
        holder.fileName.text = item.file_name
        holder.fileDetails.text =
            if (item.type != FileType.folder) Utils.BytesToSize(item.size) else pluralNumerical(
                holder.fileDetails.context,
                item.size.toInt(),
                R.string.files_count_a,
                R.string.files_count_b,
                R.string.files_count_c
            )
        holder.itemView.setOnClickListener {
            clickListener?.onClick(holder.bindingAdapterPosition, item)
        }
        holder.itemView.setOnLongClickListener {
            if (item.type != FileType.folder) {
                holder.fileInfo.setBackgroundColor(colorPrimary)
                clickListener?.onOpenFileMenu(holder.bindingAdapterPosition, item)
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onClick(position: Int, item: FileRemote)
        fun onOpenAudioMenu(position: Int, audio: FileRemote)
        fun onOpenFileMenu(position: Int, file: FileRemote)
    }

    class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.item_file_name)
        val fileDetails: TextView = itemView.findViewById(R.id.item_file_details)
        val icon: ImageView = itemView.findViewById(R.id.item_file_icon)
        val current: ThorVGLottieView = itemView.findViewById(R.id.current)
        val fileInfo: LinearLayout = itemView.findViewById(R.id.item_file_info)
    }

    class AudioHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.item_file_name)
        val fileDetails: TextView = itemView.findViewById(R.id.item_file_details)
        val icon: ImageView = itemView.findViewById(R.id.item_file_icon)
        val current: ThorVGLottieView = itemView.findViewById(R.id.current)
        val visual: ThorVGLottieView = itemView.findViewById(R.id.item_audio_visual)
        val fileInfo: LinearLayout = itemView.findViewById(R.id.item_file_info)
    }
}