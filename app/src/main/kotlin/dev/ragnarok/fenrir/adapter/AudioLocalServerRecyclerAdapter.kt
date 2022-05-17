package dev.ragnarok.fenrir.adapter

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.*
import dev.ragnarok.fenrir.domain.ILocalServerInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.link.VkLinkParser
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.canPlayAfterCurrent
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.isNowPlayingOrPreparingOrPaused
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playAfterCurrent
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playOrPause
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playerStatus
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.stop
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.Option
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.menu.options.AudioLocalServerOption
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadAudio
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.disposables.Disposable

class AudioLocalServerRecyclerAdapter(
    private val mContext: Context,
    private var data: List<Audio>
) : RecyclerView.Adapter<AudioLocalServerRecyclerAdapter.AudioHolder>() {
    private val mAudioInteractor: ILocalServerInteractor
    private var mClickListener: ClickListener? = null
    private var mPlayerDisposable = Disposable.disposed()
    private var audioListDisposable = Disposable.disposed()
    private var currAudio: Audio?
    fun setItems(data: List<Audio>) {
        this.data = data
        notifyDataSetChanged()
    }

    private fun doBitrate(url: String): Single<Int> {
        return Single.create { v: SingleEmitter<Int> ->
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(url, HashMap())
                val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                if (bitrate != null) {
                    v.onSuccess((bitrate.toLong() / 1000).toInt())
                } else {
                    v.onError(Throwable("Can't receipt bitrate "))
                }
            } catch (e: RuntimeException) {
                v.onError(e)
            }
        }
    }

    private fun getBitrate(url: String?, size: Int) {
        if (url.isNullOrEmpty()) {
            return
        }
        audioListDisposable = doBitrate(url).fromIOToMain()
            .subscribe(
                { r: Int? ->
                    CreateCustomToast(mContext).showToast(
                        mContext.resources.getString(
                            R.string.bitrate,
                            r,
                            Utils.BytesToSize(size.toLong())
                        )
                    )
                }
            ) { e -> Utils.showErrorInAdapter(mContext as Activity, e) }
    }

    @get:DrawableRes
    private val audioCoverSimple: Int
        get() = if (Settings.get()
                .main().isAudio_round_icon
        ) R.drawable.audio_button else R.drawable.audio_button_material

    private fun TransformCover(): Transformation {
        return if (Settings.get()
                .main().isAudio_round_icon
        ) RoundTransformation() else PolyTransformation()
    }

    private fun updateAudioStatus(holder: AudioHolder, audio: Audio) {
        if (audio != currAudio) {
            holder.visual.setImageResource(if (audio.url.isNullOrEmpty()) R.drawable.audio_died else R.drawable.song)
            holder.play_cover.clearColorFilter()
            return
        }
        when (playerStatus()) {
            1 -> {
                Utils.doWavesLottie(holder.visual, true)
                holder.play_cover.setColorFilter(Color.parseColor("#44000000"))
            }
            2 -> {
                Utils.doWavesLottie(holder.visual, false)
                holder.play_cover.setColorFilter(Color.parseColor("#44000000"))
            }
        }
    }

    private fun updateDownloadState(holder: AudioHolder, audio: Audio) {
        if (audio.downloadIndicator == 2) {
            holder.saved.setImageResource(R.drawable.remote_cloud)
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorSecondary(mContext))
        } else {
            holder.saved.setImageResource(R.drawable.save)
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorPrimary(mContext))
        }
        holder.saved.visibility =
            if (audio.downloadIndicator != 0) View.VISIBLE else View.GONE
    }

    private fun doMenu(holder: AudioHolder, position: Int, view: View, audio: Audio) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                AudioLocalServerOption.save_item_audio,
                mContext.getString(R.string.download),
                R.drawable.save,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalServerOption.play_item_audio,
                mContext.getString(R.string.play),
                R.drawable.play,
                true
            )
        )
        if (canPlayAfterCurrent(audio)) {
            menus.add(
                OptionRequest(
                    AudioLocalServerOption.play_item_after_current_audio,
                    mContext.getString(R.string.play_audio_after_current),
                    R.drawable.play_next,
                    false
                )
            )
        }
        menus.add(
            OptionRequest(
                AudioLocalServerOption.bitrate_item_audio,
                mContext.getString(R.string.get_bitrate),
                R.drawable.high_quality,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalServerOption.delete_item_audio,
                mContext.getString(R.string.delete),
                R.drawable.ic_outline_delete,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalServerOption.update_time_item_audio,
                mContext.getString(R.string.update_time),
                R.drawable.ic_recent,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioLocalServerOption.edit_item_audio,
                mContext.getString(R.string.edit),
                R.drawable.about_writed,
                true
            )
        )
        menus.header(
            Utils.firstNonEmptyString(audio.artist, " ") + " - " + audio.title,
            R.drawable.song,
            audio.thumb_image_little
        )
        menus.columns(2)
        menus.show(
            (mContext as FragmentActivity).supportFragmentManager,
            "audio_options",
            object : ModalBottomSheetDialogFragment.Listener {
                override fun onModalOptionSelected(option: Option) {
                    when (option.id) {
                        AudioLocalServerOption.save_item_audio -> {
                            if (!hasReadWriteStoragePermission(mContext)) {
                                mClickListener?.onRequestWritePermissions()
                                return
                            }
                            audio.downloadIndicator = 1
                            updateDownloadState(holder, audio)
                            val ret = doDownloadAudio(
                                mContext,
                                audio,
                                Settings.get().accounts().current,
                                Force = false,
                                isLocal = true
                            )
                            when (ret) {
                                0 -> CreateCustomToast(mContext).showToastBottom(R.string.saved_audio)
                                1, 2 -> {
                                    Utils.ThemedSnack(
                                        view,
                                        if (ret == 1) R.string.audio_force_download else R.string.audio_force_download_pc,
                                        BaseTransientBottomBar.LENGTH_LONG
                                    ).setAction(
                                        R.string.button_yes
                                    ) {
                                        doDownloadAudio(
                                            mContext,
                                            audio,
                                            Settings.get().accounts().current,
                                            Force = true,
                                            isLocal = true
                                        )
                                    }
                                        .show()
                                }
                                else -> {
                                    audio.downloadIndicator = 0
                                    updateDownloadState(holder, audio)
                                    CreateCustomToast(mContext).showToastBottom(R.string.error_audio)
                                }
                            }
                        }
                        AudioLocalServerOption.play_item_audio -> {
                            mClickListener?.onClick(position, audio)
                            if (Settings.get().other().isShow_mini_player) getPlayerPlace(
                                Settings.get().accounts().current
                            ).tryOpenWith(mContext)
                        }
                        AudioLocalServerOption.play_item_after_current_audio -> playAfterCurrent(
                            audio
                        )
                        AudioLocalServerOption.bitrate_item_audio -> getBitrate(
                            audio.url,
                            audio.duration
                        )
                        AudioLocalServerOption.update_time_item_audio -> {
                            val hash = VkLinkParser.parseLocalServerURL(audio.url)
                            if (hash.isNullOrEmpty()) {
                                return
                            }
                            audioListDisposable =
                                mAudioInteractor.update_time(hash).fromIOToMain().subscribe(
                                    { CreateCustomToast(mContext).showToast(R.string.success) }) { t ->
                                    Utils.showErrorInAdapter(
                                        mContext as Activity,
                                        t
                                    )
                                }
                        }
                        AudioLocalServerOption.edit_item_audio -> {
                            val hash2 = VkLinkParser.parseLocalServerURL(audio.url)
                            if (hash2.isNullOrEmpty()) {
                                return
                            }
                            audioListDisposable =
                                mAudioInteractor.get_file_name(hash2).fromIOToMain().subscribe(
                                    { t: String? ->
                                        val root =
                                            View.inflate(mContext, R.layout.entry_file_name, null)
                                        (root.findViewById<View>(R.id.edit_file_name) as TextInputEditText).setText(
                                            t
                                        )
                                        MaterialAlertDialogBuilder(mContext)
                                            .setTitle(R.string.change_name)
                                            .setCancelable(true)
                                            .setView(root)
                                            .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                                                audioListDisposable =
                                                    mAudioInteractor.update_file_name(
                                                        hash2,
                                                        (root.findViewById<View>(R.id.edit_file_name) as TextInputEditText).text.toString()
                                                            .trim { it <= ' ' })
                                                        .fromIOToMain()
                                                        .subscribe({
                                                            CreateCustomToast(mContext).showToast(
                                                                R.string.success
                                                            )
                                                        }) { o ->
                                                            Utils.showErrorInAdapter(
                                                                mContext as Activity,
                                                                o
                                                            )
                                                        }
                                            }
                                            .setNegativeButton(R.string.button_cancel, null)
                                            .show()
                                    }) { t ->
                                    Utils.showErrorInAdapter(
                                        mContext as Activity,
                                        t
                                    )
                                }
                        }
                        AudioLocalServerOption.delete_item_audio -> MaterialAlertDialogBuilder(
                            mContext
                        )
                            .setMessage(R.string.do_delete)
                            .setTitle(R.string.confirmation)
                            .setCancelable(true)
                            .setPositiveButton(R.string.button_yes) { _: DialogInterface?, _: Int ->
                                val hash1 = VkLinkParser.parseLocalServerURL(audio.url)
                                if (hash1.isNullOrEmpty()) {
                                    return@setPositiveButton
                                }
                                audioListDisposable =
                                    mAudioInteractor.delete_media(hash1).fromIOToMain().subscribe(
                                        { CreateCustomToast(mContext).showToast(R.string.success) }) { o ->
                                        Utils.showErrorInAdapter(
                                            mContext as Activity,
                                            o
                                        )
                                    }
                            }
                            .setNegativeButton(R.string.button_cancel, null)
                            .show()
                        else -> {}
                    }
                }
            })
    }

    private fun doPlay(position: Int, audio: Audio) {
        if (isNowPlayingOrPreparingOrPaused(audio)) {
            if (!Settings.get().other().isUse_stop_audio) {
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
            LayoutInflater.from(mContext).inflate(R.layout.item_audio_local_server, parent, false)
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
                        mContext.resources,
                        audioCoverSimple,
                        mContext.theme
                    ) ?: return
                )
                .transform(TransformCover())
                .tag(Constants.PICASSO_TAG)
                .into(holder.play_cover)
        } else {
            with().cancelRequest(holder.play_cover)
            holder.play_cover.setImageResource(audioCoverSimple)
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
        holder.play.setOnClickListener { v: View ->
            if (Settings.get().main().isRevert_play_audio) {
                doMenu(holder, position, v, audio)
            } else {
                doPlay(position, audio)
            }
        }
        holder.Track.setOnLongClickListener { v: View? ->
            if (!hasReadWriteStoragePermission(mContext)) {
                mClickListener?.onRequestWritePermissions()
                return@setOnLongClickListener false
            }
            audio.downloadIndicator = 1
            updateDownloadState(holder, audio)
            val ret =
                doDownloadAudio(
                    mContext, audio, Settings.get().accounts().current,
                    Force = false,
                    isLocal = true
                )
            when (ret) {
                0 -> CreateCustomToast(mContext).showToastBottom(R.string.saved_audio)
                1, 2 -> {
                    v?.let {
                        Utils.ThemedSnack(
                            it,
                            if (ret == 1) R.string.audio_force_download else R.string.audio_force_download_pc,
                            BaseTransientBottomBar.LENGTH_LONG
                        ).setAction(
                            R.string.button_yes
                        ) {
                            doDownloadAudio(
                                mContext,
                                audio,
                                Settings.get().accounts().current,
                                Force = true,
                                isLocal = true
                            )
                        }.show()
                    }
                }
                else -> {
                    audio.downloadIndicator = 0
                    updateDownloadState(holder, audio)
                    CreateCustomToast(mContext).showToastBottom(R.string.error_audio)
                }
            }
            true
        }
        holder.Track.setOnClickListener { view: View ->
            holder.cancelSelectionAnimation()
            holder.startSomeAnimation()
            if (Settings.get().main().isRevert_play_audio) {
                doPlay(position, audio)
            } else {
                doMenu(holder, position, view, audio)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mPlayerDisposable = observeServiceBinding()
            .toMainThread()
            .subscribe { onServiceBindEvent(it) }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mPlayerDisposable.dispose()
        audioListDisposable.dispose()
    }

    private fun onServiceBindEvent(@PlayerStatus status: Int) {
        when (status) {
            PlayerStatus.UPDATE_TRACK_INFO, PlayerStatus.SERVICE_KILLED, PlayerStatus.UPDATE_PLAY_PAUSE -> {
                updateAudio(currAudio)
                currAudio = currentAudio
                updateAudio(currAudio)
            }
            PlayerStatus.REPEATMODE_CHANGED, PlayerStatus.SHUFFLEMODE_CHANGED, PlayerStatus.UPDATE_PLAY_LIST -> {}
        }
    }

    private fun updateAudio(audio: Audio?) {
        val pos = data.indexOf(audio)
        if (pos != -1) {
            notifyItemChanged(pos)
        }
    }

    fun setClickListener(clickListener: ClickListener?) {
        mClickListener = clickListener
    }

    interface ClickListener {
        fun onClick(position: Int, audio: Audio)
        fun onUrlPhotoOpen(url: String, prefix: String, photo_prefix: String)
        fun onRequestWritePermissions()
    }

    inner class AudioHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artist: TextView = itemView.findViewById(R.id.dialog_title)
        val title: TextView = itemView.findViewById(R.id.dialog_message)
        val play: View = itemView.findViewById(R.id.item_audio_play)
        val play_cover: ImageView = itemView.findViewById(R.id.item_audio_play_cover)
        val Track: View = itemView.findViewById(R.id.track_option)
        val saved: ImageView = itemView.findViewById(R.id.saved)
        val selectionView: MaterialCardView = itemView.findViewById(R.id.item_audio_selection)
        val animationAdapter: Animator.AnimatorListener
        val visual: RLottieImageView = itemView.findViewById(R.id.item_audio_visual)
        val time: TextView = itemView.findViewById(R.id.item_audio_time)
        var animator: ObjectAnimator? = null
        fun startSelectionAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorPrimary(mContext))
            selectionView.alpha = 0.5f
            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f)
            animator?.duration = 1500
            animator?.addListener(animationAdapter)
            animator?.start()
        }

        fun startSomeAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorSecondary(mContext))
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

        init {
            animationAdapter = object : WeakViewAnimatorAdapter<View>(selectionView) {
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
        }
    }

    init {
        currAudio = currentAudio
        mAudioInteractor = InteractorFactory.createLocalServerInteractor()
    }
}