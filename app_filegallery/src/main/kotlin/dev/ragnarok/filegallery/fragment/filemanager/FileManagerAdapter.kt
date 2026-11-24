package dev.ragnarok.filegallery.fragment.filemanager

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
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.filegallery.Constants
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.filePathToUrl
import dev.ragnarok.filegallery.media.music.MusicPlaybackController
import dev.ragnarok.filegallery.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.filegallery.media.music.PlayerStatus
import dev.ragnarok.filegallery.model.Audio
import dev.ragnarok.filegallery.model.FileItem
import dev.ragnarok.filegallery.model.FileType
import dev.ragnarok.filegallery.picasso.PicassoInstance
import dev.ragnarok.filegallery.settings.CurrentTheme
import dev.ragnarok.filegallery.settings.Settings
import dev.ragnarok.filegallery.toColor
import dev.ragnarok.filegallery.util.AppTextUtils.pluralNumerical
import dev.ragnarok.filegallery.util.Utils
import dev.ragnarok.filegallery.view.natives.animation.ThorVGLottieView

class FileManagerAdapter(context: Context, private var data: List<FileItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @ColorInt
    private val colorPrimary = CurrentTheme.getColorPrimary(context)

    @ColorInt
    private val colorOnSurface = CurrentTheme.getColorOnSurface(context)

    @ColorInt
    private val messageBubbleColor = CurrentTheme.getMessageBubbleColor(context)
    private var clickListener: ClickListener? = null
    private var currAudio: Audio? = currentAudio
    private var isSelectMode = false

    fun updateSelectedMode(show: Boolean) {
        isSelectMode = show
    }

    fun setItems(data: List<FileItem>) {
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
            if (data[i].fileNameHash == audio.id && data[i].filePathHash == audio.ownerId) {
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
        audio: FileItem
    ) {
        if (audio.fileNameHash != currAudio?.id && audio.filePathHash != currAudio?.ownerId) {
            if (holder.visual.drawable is Animatable || holder.visual.drawable == null) {
                holder.visual.setImageResource(R.drawable.song)
                holder.icon.clearColorFilter()
            }
            return
        }
        when (MusicPlaybackController.playerStatus()) {
            1 -> {
                Utils.doWavesLottieBig(holder.visual, true)
                holder.icon.setColorFilter("#44000000".toColor())
            }

            2 -> {
                Utils.doWavesLottieBig(holder.visual, false)
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
                    R.raw.select_fire,
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
            .load(item.file_path?.filePathToUrl("thumb_file")).tag(Constants.PICASSO_TAG)
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
        holder.tagged.visibility = if (item.isHasTag) View.VISIBLE else View.GONE
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
            t.setId(item.fileNameHash)
            t.setOwnerId(item.filePathHash)
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
                    R.raw.select_fire,
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
            .load(item.file_path?.filePathToUrl("thumb_file")).tag(Constants.PICASSO_TAG)
            .priority(Picasso.Priority.LOW)
            .into(holder.icon)
        holder.fileName.text = item.file_name
        holder.tagged.visibility = if (item.isHasTag) View.VISIBLE else View.GONE
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
            } else {
                if (isSelectMode) {
                    clickListener?.onToggleDirTag(item)
                } else {
                    holder.fileInfo.setBackgroundColor(colorPrimary)
                    clickListener?.onOpenFolderMenu(holder.bindingAdapterPosition, item)
                }
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
        fun onClick(position: Int, item: FileItem)
        fun onToggleDirTag(item: FileItem)
        fun onOpenFolderMenu(position: Int, item: FileItem)
        fun onOpenFileMenu(position: Int, item: FileItem)
        fun onOpenAudioMenu(position: Int, item: FileItem)
    }

    class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.item_file_name)
        val fileDetails: TextView = itemView.findViewById(R.id.item_file_details)
        val icon: ImageView = itemView.findViewById(R.id.item_file_icon)
        val current: ThorVGLottieView = itemView.findViewById(R.id.current)
        val tagged: ImageView = itemView.findViewById(R.id.item_tagged)
        val fileInfo: LinearLayout = itemView.findViewById(R.id.item_file_info)
    }

    class AudioHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.item_file_name)
        val fileDetails: TextView = itemView.findViewById(R.id.item_file_details)
        val icon: ImageView = itemView.findViewById(R.id.item_file_icon)
        val current: ThorVGLottieView = itemView.findViewById(R.id.current)
        val visual: ThorVGLottieView = itemView.findViewById(R.id.item_audio_visual)
        val tagged: ImageView = itemView.findViewById(R.id.item_tagged)
        val fileInfo: LinearLayout = itemView.findViewById(R.id.item_file_info)
    }
}