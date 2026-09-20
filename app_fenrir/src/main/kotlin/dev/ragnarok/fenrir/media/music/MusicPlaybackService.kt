package dev.ragnarok.fenrir.media.music

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.adapter.future
import androidx.core.content.edit
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import de.maxr1998.modernpreferences.PreferenceScreen
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.UserAgentTool
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.getParcelableArrayListExtraCompat
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.DownloadWorkUtils.GetLocalTrackLink
import dev.ragnarok.fenrir.util.DownloadWorkUtils.TrackIsDownloaded
import dev.ragnarok.fenrir.util.Logger
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.andThen
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.hiddenIO
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import kotlin.random.Random

@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {
    private val mBinder: IBinder = ServiceStub(this)

    private var onceCloseMiniPlayer = false
    private var mAnyActivityInForeground = false
    private lateinit var mediaSession: MediaSession
    private lateinit var musicPlayer: MusicPlayer
    private lateinit var customCommands: List<CommandButton>
    private var shutdownDelayedDisposable = CancelableJob()

    private val MONO_SCHEDULER =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        if (Constants.IS_DEBUG) Logger.d(TAG, "Service bound, intent = $intent")
        mAnyActivityInForeground = false
        return mBinder
    }

    internal fun trunkBitmap(bitmap: Bitmap, maxResolution: Int): Bitmap {
        if (maxResolution < 0 || bitmap.width <= 0 || bitmap.height <= 0 || bitmap.width <= maxResolution && bitmap.height <= maxResolution) {
            return bitmap
        }
        var mWidth = bitmap.width
        var mHeight = bitmap.height
        val mCo = mHeight.coerceAtMost(mWidth).toFloat() / mHeight.coerceAtLeast(mWidth)
        if (mWidth > mHeight) {
            mWidth = maxResolution
            mHeight = (maxResolution * mCo).toInt()
        } else {
            mHeight = maxResolution
            mWidth = (maxResolution * mCo).toInt()
        }
        if (mWidth <= 0 || mHeight <= 0) {
            return bitmap
        }
        val tmp = bitmap.scale(mWidth, mHeight)
        bitmap.recycle()
        return tmp
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (Constants.IS_DEBUG) Logger.d(TAG, "Service unbound")
        if (musicPlayer.isPlaying || mAnyActivityInForeground) {
            Logger.d(
                TAG,
                "onUnbind, mIsSupposedToBePlaying || mPausedByTransientLossOfFocus || isPreparing()"
            )
            return true
        }
        Logger.d(TAG, "onUnbind, stopSelf(mServiceStartId)")
        pauseAllPlayersAndStopSelf()
        return true
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
        mAnyActivityInForeground = false
    }

    @SuppressLint("WrongConstant")
    override fun onCreate() {
        if (Constants.IS_DEBUG) Logger.d(TAG, "Creating service")
        super.onCreate()

        musicPlayer = MusicPlayer(this)
        mediaSession =
            MediaSession.Builder(application, musicPlayer.exoplayer)
                .setId(resources.getString(R.string.app_name))
                .setSessionActivity(NotificationHelper.getAudioPlayerPendingIntent(this))
                .setCallback(object : MediaSession.Callback {
                    override fun onConnect(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo
                    ): MediaSession.ConnectionResult {
                        val sessionCommands =
                            MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                                .add(SessionCommand(CUSTOM_COMMAND_CLOSE, Bundle.EMPTY))
                                .add(SessionCommand(CUSTOM_COMMAND_DISABLE_REPEAT, Bundle.EMPTY))
                                .add(SessionCommand(CUSTOM_COMMAND_REPEAT_MODE_ONCE, Bundle.EMPTY))
                                .add(SessionCommand(CUSTOM_COMMAND_REPEAT_MODE_ALL, Bundle.EMPTY))
                                .build()
                        return MediaSession.ConnectionResult.accept(
                            sessionCommands,
                            MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                        )
                    }

                    override fun onCustomCommand(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo,
                        customCommand: SessionCommand,
                        args: Bundle
                    ): ListenableFuture<SessionResult> {
                        return when (customCommand.customAction) {
                            CUSTOM_COMMAND_CLOSE -> {
                                pauseAllPlayersAndStopSelf()
                                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                            }

                            CUSTOM_COMMAND_DISABLE_REPEAT -> {
                                repeatMode = Player.REPEAT_MODE_OFF
                                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                            }

                            CUSTOM_COMMAND_REPEAT_MODE_ONCE -> {
                                repeatMode = Player.REPEAT_MODE_ONE
                                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                            }

                            CUSTOM_COMMAND_REPEAT_MODE_ALL -> {
                                repeatMode = Player.REPEAT_MODE_ALL
                                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                            }

                            else -> super.onCustomCommand(session, controller, customCommand, args)
                        }
                    }
                })
                .setBitmapLoader(CacheBitmapLoader(object : BitmapLoader {
                    private val limit by lazy { MediaSession.getBitmapDimensionLimit(this@MusicPlaybackService) }
                    override fun supportsMimeType(p0: String): Boolean {
                        return true
                    }

                    override fun decodeBitmap(p0: ByteArray): ListenableFuture<Bitmap> {
                        return MONO_SCHEDULER.future {
                            try {
                                trunkBitmap(
                                    BitmapFactory.decodeByteArray(
                                        p0,
                                        0,
                                        p0.size,
                                        BitmapFactory.Options().apply {
                                            outConfig = Bitmap.Config.ARGB_8888
                                            inMutable = true
                                        }), limit
                                )
                            } catch (_: Exception) {
                                BitmapFactory.decodeResource(
                                    resources, R.drawable.generic_audio_nowplaying_service
                                )
                            }
                        }
                    }

                    override fun loadBitmap(p0: Uri): ListenableFuture<Bitmap> {
                        return MONO_SCHEDULER.future {
                            if (p0.toString() == "file://null") {
                                BitmapFactory.decodeResource(
                                    resources, R.drawable.generic_audio_nowplaying_service
                                )
                            } else {
                                try {
                                    trunkBitmap(
                                        PicassoInstance.with().load(p0).get()?.copy(
                                            Bitmap.Config.ARGB_8888,
                                            true
                                        ) ?: BitmapFactory.decodeResource(
                                            resources, R.drawable.generic_audio_nowplaying_service
                                        ), limit
                                    )
                                } catch (_: Exception) {
                                    BitmapFactory.decodeResource(
                                        resources, R.drawable.generic_audio_nowplaying_service
                                    )
                                }
                            }
                        }
                    }

                }))
                .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelName(R.string.audio_channel)
                .setChannelId(AppNotificationChannels.AUDIO_CHANNEL_ID)
                .setNotificationId(
                    NotificationHelper.FENRIR_MUSIC_SERVICE
                ).build().apply {
                    setSmallIcon(R.drawable.song)
                }
        )
        addSession(mediaSession)

        customCommands =
            listOf(
                CommandButton.Builder(CommandButton.ICON_UNDEFINED)
                    .setCustomIconResId(R.drawable.close)
                    .setDisplayName(getString(R.string.close))
                    .setSessionCommand(SessionCommand(CUSTOM_COMMAND_CLOSE, Bundle.EMPTY))
                    .build(),
                CommandButton.Builder(CommandButton.ICON_REPEAT_OFF)
                    .setDisplayName(getString(R.string.repeat_mode))
                    .setSessionCommand(SessionCommand(CUSTOM_COMMAND_REPEAT_MODE_ALL, Bundle.EMPTY))
                    .build(),
                CommandButton.Builder(CommandButton.ICON_REPEAT_ALL)
                    .setDisplayName(getString(R.string.repeat_mode))
                    .setSessionCommand(
                        SessionCommand(
                            CUSTOM_COMMAND_REPEAT_MODE_ONCE,
                            Bundle.EMPTY
                        )
                    )
                    .build(),
                CommandButton.Builder(CommandButton.ICON_REPEAT_ONE)
                    .setDisplayName(getString(R.string.repeat_mode))
                    .setSessionCommand(SessionCommand(CUSTOM_COMMAND_DISABLE_REPEAT, Bundle.EMPTY))
                    .build()
            )

        refreshMediaButtonCustomLayout()
        notifyChange(META_CHANGED)
        scheduleDelayedShutdown()
    }

    override fun onDestroy() {
        if (Constants.IS_DEBUG) Logger.d(TAG, "Destroying service")
        shutdownDelayedDisposable.cancel()
        super.onDestroy()
        mediaSession.release()
        musicPlayer.release()
    }

    internal fun scheduleDelayedShutdown() {
        val delay = Settings.get().main().musicLifecycle
        shutdownDelayedDisposable.cancel()
        if (Constants.IS_DEBUG) Log.v(TAG, "Scheduling shutdown in $delay ms")
        shutdownDelayedDisposable.set(
            delayTaskFlow(delay.toLong())
                .toMain { pauseAllPlayersAndStopSelf() })
    }

    internal fun notifyChange(what: String) {
        if (Constants.IS_DEBUG) Logger.d(TAG, "notifyChange: what = $what")
        MusicPlaybackController.publishFromServiceState(what)
    }

    internal fun getRepeatCommand() =
        when (musicPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> customCommands[1]
            Player.REPEAT_MODE_ALL -> customCommands[2]
            Player.REPEAT_MODE_ONE -> customCommands[3]
            else -> throw IllegalArgumentException()
        }

    internal fun refreshMediaButtonCustomLayout() {
        mediaSession.setCustomLayout(
            ImmutableList.of(
                customCommands[0],
                getRepeatCommand()
            )
        )
    }

    internal val audioSessionId: Int
        get() {
            synchronized(this) { return musicPlayer.lastAudioSessionId }
        }

    internal var shuffleMode: Boolean
        get() = musicPlayer.shuffleMode
        set(shuffleMode) {
            synchronized(this) {
                if (musicPlayer.shuffleMode == shuffleMode) {
                    return
                }
                musicPlayer.shuffleMode = shuffleMode
                notifyChange(SHUFFLE_MODE_CHANGED)
                refreshMediaButtonCustomLayout()
                PreferenceScreen.getPreferences(this).edit(true) {
                    putBoolean(
                        PREFS_SHUFFLE,
                        shuffleMode
                    )
                }
            }
        }

    internal var repeatMode: Int
        get() = musicPlayer.repeatMode
        set(repeatMode) {
            synchronized(this) {
                musicPlayer.repeatMode = repeatMode
                notifyChange(REPEAT_MODE_CHANGED)
                refreshMediaButtonCustomLayout()
                PreferenceScreen.getPreferences(this).edit(true) {
                    putInt(
                        PREFS_REPEAT,
                        repeatMode
                    )
                }
            }
        }

    /**
     * {@inheritDoc}
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ret = super.onStartCommand(intent, flags, startId)
        if (Constants.IS_DEBUG) Logger.d(TAG, "Got new intent $intent, startId = $startId")

        handleCommandIntent(intent)
        return ret
    }

    internal fun handleCommandIntent(intent: Intent?) {
        val action = intent?.action ?: return
        if (Constants.IS_DEBUG) Logger.d(
            TAG,
            "handleCommandIntent: action = $action"
        )
        if (ACTION_PLAYLIST == action) {
            val audios: ArrayList<Audio>? =
                intent.getParcelableArrayListExtraCompat(Extra.AUDIOS)
            val position = intent.getIntExtra(Extra.POSITION, 0)
            if (audios.nonNullNoEmpty()) {
                val repeatMode = PreferenceScreen.getPreferences(this)
                    .getInt(PREFS_REPEAT, Player.REPEAT_MODE_OFF)
                val shuffleMode = PreferenceScreen.getPreferences(this)
                    .getBoolean(PREFS_SHUFFLE, false)
                var changed = false
                if (musicPlayer.repeatMode != repeatMode) {
                    musicPlayer.repeatMode = repeatMode
                    notifyChange(REPEAT_MODE_CHANGED)
                    refreshMediaButtonCustomLayout()
                    changed = true
                }
                musicPlayer.setSources(audios)
                musicPlayer.playAt(position)
                if (musicPlayer.shuffleMode != shuffleMode) {
                    musicPlayer.shuffleMode = shuffleMode
                    notifyChange(SHUFFLE_MODE_CHANGED)
                    if (!changed) {
                        refreshMediaButtonCustomLayout()
                    }
                }
            }
        }
    }

    internal fun doNotDestroyWhenActivityRecreated() {
        synchronized(this) {
            mAnyActivityInForeground = true
        }
    }

    internal class MusicPlayer(service: MusicPlaybackService) {
        val mService: WeakReference<MusicPlaybackService> = WeakReference(service)
        val exoplayer: ExoPlayer = ExoPlayer.Builder(
            service, DefaultRenderersFactory(service)
                .setExtensionRendererMode(
                    when (Settings.get().main().fFmpegPlugin) {
                        0 -> EXTENSION_RENDERER_MODE_OFF
                        1 -> EXTENSION_RENDERER_MODE_ON
                        2 -> EXTENSION_RENDERER_MODE_PREFER
                        else -> EXTENSION_RENDERER_MODE_OFF
                    }
                )
        ).build()
        private val cancelableJob = CancelableJob()
        private val audioInteractor: IAudioInteractor = InteractorFactory.createAudioInteractor()
        var isPreparing = false
            private set
        var isPrepared = false
            private set
        var isPlaying = false
            private set
        var lastAudioSessionId = C.AUDIO_SESSION_ID_UNSET

        private var errorsCount = 0
        private var hasErrorPlayback = false
        val factory = Utils.getExoPlayerFactory(
            UserAgentTool.USER_AGENT_CURRENT_ACCOUNT,
            Includes.proxySettings.activeProxy
        )
        val factoryLocal =
            DefaultDataSource.Factory(service)

        internal fun makeMediaSource(audio: Audio): MediaSource {
            if (Settings.get().main().isForce_cache && TrackIsDownloaded(audio) == 1)
                audio.setUrl(GetLocalTrackLink(audio))
            var res: String? = audio.url
            if (res?.contains("audio_api_unavailable") == true) {
                res = null
            }
            val url = Utils.firstNonEmptyString(
                res,
                "file:///android_asset/audio_error.ogg"
            )
            val mediaItem =
                MediaItem.Builder().setUri(url).setMediaId("${audio.id}_${audio.ownerId}")
                    .setTag(audio)
                    .setMediaMetadata(
                        MediaMetadata.Builder().setTitle(audio.title).setArtist(audio.artist)
                            .setAlbumTitle(audio.album_title)
                            .setArtworkUri(
                                Utils.firstNonEmptyString(
                                    audio.thumb_image_big,
                                    audio.thumb_image_very_big,
                                    audio.thumb_image_little
                                )?.toUri() ?: "file://null".toUri()
                            )
                            .build()
                    )
                    .build()
            return if (url?.contains("index.m3u8") == true)
                HlsMediaSource.Factory(factory)
                    .createMediaSource(mediaItem)
            else if (url?.contains("file://") == true || url?.contains("content://") == true) {
                ProgressiveMediaSource.Factory(factoryLocal)
                    .createMediaSource(mediaItem)
            } else {
                ProgressiveMediaSource.Factory(
                    factory
                ).createMediaSource(mediaItem)
            }
        }

        fun setSources(audios: List<Audio>) {
            errorsCount = 0
            hasErrorPlayback = false
            isPreparing = true
            val sources = ArrayList<MediaSource>(audios.size)
            for (i in audios) {
                sources.add(makeMediaSource(i))
            }
            exoplayer.setMediaSources(sources)
            exoplayer.prepare()
            exoplayer.setAudioAttributes(
                AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA).build(), true
            )
        }

        fun insertSourceAfterCurrent(audio: Audio) {
            exoplayer.addMediaSource(exoplayer.currentMediaItemIndex + 1, makeMediaSource(audio))
        }

        fun play() {
            if (isPrepared) {
                exoplayer.play()
            }
        }

        fun prev() {
            exoplayer.seekToPrevious()
        }

        fun next() {
            exoplayer.seekToNext()
        }

        fun playAt(position: Int) {
            exoplayer.seekTo(position, 0)
            exoplayer.play()
        }

        fun stop() {
            isPreparing = false
            isPrepared = false
            isPlaying = false
            exoplayer.stop()
        }

        fun release() {
            stop()
            exoplayer.clearMediaItems()
            exoplayer.release()
            mService.get()?.broadcastAudioSessionClose()
            cancelableJob.cancel()
        }

        fun pause() {
            if (isPlaying) {
                exoplayer.pause()
            }
        }

        val duration: Long
            get() = if (exoplayer.duration == C.TIME_UNSET) -1 else exoplayer.duration

        val position: Long
            get() = if (exoplayer.currentPosition == C.TIME_UNSET) -1 else exoplayer.currentPosition

        fun seek(whereto: Long): Long {
            exoplayer.seekTo(whereto)
            return whereto
        }

        val bufferPercent: Int
            get() = exoplayer.bufferedPercentage

        val bufferPos: Long
            get() = exoplayer.bufferedPosition

        val itemIndex: Int
            get() = exoplayer.currentMediaItemIndex

        val queueIndex: Int
            get() {
                if (exoplayer.shuffleModeEnabled) {
                    val s = getShuffleOrderIndexes()
                    for ((i, element) in s.withIndex()) {
                        if (element == exoplayer.currentMediaItemIndex) {
                            return i
                        }
                    }
                    return 0
                }
                return exoplayer.currentMediaItemIndex
            }

        val currentAudio: Audio?
            get() = exoplayer.currentMediaItem?.localConfiguration?.tag as? Audio

        fun getShuffleOrderIndexes(): List<Int> {
            val result = mutableListOf<Int>()
            if (exoplayer.shuffleModeEnabled) {
                val shuffleOrder = exoplayer.shuffleOrder
                var index = shuffleOrder.firstIndex

                while (index != C.INDEX_UNSET) {
                    result.add(index)
                    index = shuffleOrder.getNextIndex(index)
                }
            } else {
                for (i in 0 until exoplayer.mediaItemCount) {
                    result.add(i)
                }
            }

            return result
        }

        val currentAudios: List<Audio>
            get() {
                val ret = ArrayList<Audio>(exoplayer.mediaItemCount)
                for (i in getShuffleOrderIndexes()) {
                    val audio = exoplayer.getMediaItemAt(i).localConfiguration?.tag as? Audio
                    if (audio != null) {
                        ret.add(audio)
                    }
                }
                return ret
            }

        val currentMediaItem: MediaItem?
            get() = exoplayer.currentMediaItem

        var shuffleMode: Boolean
            get() = exoplayer.shuffleModeEnabled
            set(shuffleMode) {
                if (exoplayer.shuffleModeEnabled == shuffleMode) {
                    return
                }
                exoplayer.shuffleModeEnabled = shuffleMode
            }

        var repeatMode: Int
            get() = exoplayer.repeatMode
            set(repeatMode) {
                exoplayer.repeatMode = repeatMode
            }

        init {
            exoplayer.setHandleAudioBecomingNoisy(true)
            exoplayer.setWakeMode(C.WAKE_MODE_NETWORK)

            exoplayer.repeatMode = Player.REPEAT_MODE_OFF
            exoplayer.shuffleOrder = MusicShuffleOrder(0)

            exoplayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(@Player.State state: Int) {
                    when (state) {
                        Player.STATE_READY -> if (isPreparing) {
                            isPreparing = false
                            isPrepared = true
                            mService.get()?.notifyChange(PREPARED)
                            exoplayer.play()
                        }

                        Player.STATE_ENDED -> {
                            pause()
                            seek(0)
                        }

                        else -> {
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying && hasErrorPlayback) {
                        hasErrorPlayback = false
                    }
                    if (!isPlaying) {
                        mService.get()?.scheduleDelayedShutdown()
                    } else {
                        mService.get()?.shutdownDelayedDisposable?.cancel()
                    }
                }

                override fun onPlayWhenReadyChanged(
                    playWhenReady: Boolean,
                    @Player.PlayWhenReadyChangeReason reason: Int
                ) {
                    if (isPlaying != playWhenReady) {
                        isPlaying = playWhenReady
                        mService.get()?.notifyChange(PLAY_STATE_CHANGED)
                    }
                }

                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    @MediaItemTransitionReason reason: Int
                ) {
                    super.onMediaItemTransition(mediaItem, reason)
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
                        || reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
                        || reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
                    ) {
                        mService.get()?.onceCloseMiniPlayer = false
                        mService.get()?.notifyChange(META_CHANGED)

                        val accountId = Settings.get().accounts().current
                        val audio = mediaItem?.localConfiguration?.tag as? Audio
                        if (audio != null && !Utils.isHiddenAccount(accountId) && !audio.isLocalServer && !audio.isLocal) {
                            var single = audioInteractor.trackEvents(
                                accountId,
                                audio
                            )
                            if (Settings.get()
                                    .main()
                                    .isAudioBroadcastActive
                            ) {
                                single = single.andThen(
                                    audioInteractor.sendBroadcast(
                                        accountId,
                                        audio.ownerId,
                                        audio.id,
                                        audio.accessKey,
                                        setOf(accountId)
                                    )
                                )
                            }
                            cancelableJob.set(
                                single.hiddenIO()
                            )
                        }
                    }
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    if (events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED) || events.contains(
                            Player.EVENT_REPEAT_MODE_CHANGED
                        )
                        && !events.contains(Player.EVENT_TIMELINE_CHANGED)
                    ) {
                        if (events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED)) {
                            if (player.shuffleModeEnabled) {
                                val s = getShuffleOrderIndexes()
                                for (i in s.indices) {
                                    if (s[i] == itemIndex) {
                                        if (i > 0) {
                                            exoplayer.shuffleOrder =
                                                exoplayer.shuffleOrder.cloneAndMove(i, i, 0)
                                        }
                                        break
                                    }
                                }
                            }
                        }
                        mService.get()?.refreshMediaButtonCustomLayout()
                    } else if (events.contains(Player.EVENT_TRACKS_CHANGED)
                        && !events.contains(Player.EVENT_TIMELINE_CHANGED)
                    ) {
                        mService.get()?.notifyChange(TRACK_CHANGED)
                    }
                }

                override fun onAudioSessionIdChanged(audioSessionId: Int) {
                    super.onAudioSessionIdChanged(audioSessionId)
                    lastAudioSessionId = audioSessionId
                    mService.get()?.broadcastAudioSession()
                }

                override fun onPlayerError(error: PlaybackException) {
                    mService.get()?.let {
                        errorsCount++
                        if (errorsCount > 4) {
                            errorsCount = 0
                            if (hasErrorPlayback) {
                                it.pauseAllPlayersAndStopSelf()
                            } else if (exoplayer.hasNextMediaItem()) {
                                hasErrorPlayback = true
                                val pos = exoplayer.currentMediaItemIndex
                                exoplayer.stop()
                                exoplayer.prepare()
                                exoplayer.seekTo(
                                    pos + 1,
                                    0
                                )
                                exoplayer.play()
                            }
                        } else {
                            exoplayer.stop()
                            exoplayer.prepare()
                            exoplayer.seekTo(
                                exoplayer.currentMediaItemIndex,
                                exoplayer.currentPosition
                            )
                            exoplayer.play()
                        }
                    }
                }
            })
        }
    }

    internal fun broadcastAudioSession() {
        if (musicPlayer.lastAudioSessionId != C.AUDIO_SESSION_ID_UNSET) {
            Log.i(TAG, "broadcast audio session open: $musicPlayer.lastAudioSessionId")
            sendBroadcast(Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicPlayer.lastAudioSessionId)
                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            })
        } else {
            Log.e(TAG, "session id is 0? why????? THIS MIGHT BREAK EQUALIZER")
        }
    }

    internal fun broadcastAudioSessionClose() {
        if (musicPlayer.lastAudioSessionId != C.AUDIO_SESSION_ID_UNSET) {
            Log.i(TAG, "broadcast audio session close: $musicPlayer.lastAudioSessionId")
            sendBroadcast(Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicPlayer.lastAudioSessionId)
            })
            musicPlayer.lastAudioSessionId = C.AUDIO_SESSION_ID_UNSET
        }
    }

    internal fun openFile(audio: Audio) {
        synchronized(this) {
            musicPlayer.setSources(listOf(audio))
            notifyChange(QUEUE_CHANGED)
            musicPlayer.play()
        }
    }

    internal fun open(list: List<Audio>, position: Int) {
        synchronized(this) {
            musicPlayer.setSources(list)
            notifyChange(QUEUE_CHANGED)
            musicPlayer.playAt(position)
        }
    }

    internal val bufferPercent: Int
        get() {
            synchronized(this) { return musicPlayer.bufferPercent.orZero() }
        }

    internal val bufferPos: Long
        get() {
            synchronized(this) { return musicPlayer.bufferPos.orZero() }
        }

    internal val path: String?
        get() {
            synchronized(this) { return musicPlayer.currentAudio?.url }
        }

    internal val albumName: String?
        get() {
            synchronized(this) { return musicPlayer.currentAudio?.album_title }
        }

    internal val albumCover: String?
        get() {
            synchronized(this) {
                return musicPlayer.currentAudio?.let {
                    Utils.firstNonEmptyString(
                        it.thumb_image_big,
                        it.thumb_image_very_big,
                        it.thumb_image_little
                    )
                }
            }
        }

    internal val trackName: String?
        get() {
            synchronized(this) { return musicPlayer.currentAudio?.title }
        }

    internal val artistName: String?
        get() {
            synchronized(this) { return musicPlayer.currentAudio?.artist }
        }

    internal val currentTrack: Audio?
        get() {
            synchronized(this) { return musicPlayer.currentAudio }
        }

    internal val currentTrackPos: Int
        get() {
            synchronized(this) {
                return musicPlayer.queueIndex
            }
        }

    internal fun seek(position: Long): Long {
        if (musicPlayer.isPrepared) {
            var positionTemp = position
            if (positionTemp < 0) {
                positionTemp = 0
            } else if (positionTemp > musicPlayer.duration) {
                positionTemp = musicPlayer.duration
            }
            val result = musicPlayer.seek(positionTemp)
            return result
        }
        return -1
    }

    internal fun position(): Long {
        return if (musicPlayer.isPrepared) musicPlayer.position else -1
    }

    internal fun duration(): Long {
        return if (musicPlayer.isPrepared) musicPlayer.duration else -1
    }

    internal val queue: List<Audio>
        get() = synchronized(this) { musicPlayer.currentAudios }

    internal fun canPlayAfterCurrent(): Boolean {
        synchronized(this) {
            return musicPlayer.itemIndex >= 0 && !musicPlayer.shuffleMode
        }
    }

    internal fun playAfterCurrent(audio: Audio) {
        synchronized(this) {
            val current = musicPlayer.itemIndex
            if (current <= 0) {
                return
            }
            musicPlayer.insertSourceAfterCurrent(audio)
            notifyChange(QUEUE_CHANGED)
        }
    }

    internal fun play() {
        synchronized(this) {
            if (musicPlayer.isPrepared) {
                musicPlayer.play()
            }
        }
    }

    internal fun pause() {
        if (Constants.IS_DEBUG) Logger.d(TAG, "Pausing playback")
        synchronized(this) {
            if (musicPlayer.isPlaying) {
                musicPlayer.pause()
            }
        }
    }

    internal fun prev() {
        if (Constants.IS_DEBUG) Logger.d(TAG, "Going to previous track")
        synchronized(this) {
            musicPlayer.prev()
        }
    }

    internal fun next() {
        if (Constants.IS_DEBUG) Logger.d(TAG, "Going to previous track")
        synchronized(this) {
            musicPlayer.next()
        }
    }

    internal fun skip(pos: Int, force: Boolean) {
        if (!force && pos == currentTrackPos)
            return
        var tmpPos = pos
        if (musicPlayer.shuffleMode) {
            val s = musicPlayer.getShuffleOrderIndexes()
            if (s.size <= tmpPos) {
                return
            }
            tmpPos = s[tmpPos]
        }
        if (Constants.IS_DEBUG) Logger.d(TAG, "Going to next track")
        synchronized(this) {
            musicPlayer.playAt(tmpPos)
        }
    }

    internal fun refresh() {
        notifyChange(REFRESH)
    }

    class MusicShuffleOrder private constructor(
        private val shuffled: IntArray,
        private val random: Random
    ) : ShuffleOrder {
        private val indexInShuffled: IntArray = IntArray(shuffled.size)

        constructor(length: Int) : this(length, Random(System.nanoTime()))
        constructor(length: Int, randomSeed: Long) : this(length, Random(randomSeed))
        constructor(shuffledIndices: IntArray, randomSeed: Long) : this(
            shuffledIndices.copyOf(
                shuffledIndices.size
            ), Random(randomSeed)
        )

        private constructor(length: Int, random: Random) : this(
            createShuffledList(length, random),
            random
        )

        init {
            for (i in shuffled.indices) {
                indexInShuffled[shuffled[i]] = i
            }
        }

        override fun getLength(): Int {
            return shuffled.size
        }

        override fun getNextIndex(index: Int): Int {
            var shuffledIndex = indexInShuffled[index]
            return if (++shuffledIndex < shuffled.size) shuffled[shuffledIndex] else C.INDEX_UNSET
        }

        override fun getPreviousIndex(index: Int): Int {
            var shuffledIndex = indexInShuffled[index]
            return if (--shuffledIndex >= 0) shuffled[shuffledIndex] else C.INDEX_UNSET
        }

        override fun getLastIndex(): Int {
            return if (shuffled.isNotEmpty()) shuffled[shuffled.size - 1] else C.INDEX_UNSET
        }

        override fun getFirstIndex(): Int {
            return if (shuffled.isNotEmpty()) shuffled[0] else C.INDEX_UNSET
        }

        override fun cloneAndInsert(insertionIndex: Int, insertionCount: Int): ShuffleOrder {
            val insertionPoints = IntArray(insertionCount)
            val insertionValues = IntArray(insertionCount)
            for (i in 0..<insertionCount) {
                insertionPoints[i] = random.nextInt(shuffled.size + 1)
                val swapIndex = random.nextInt(i + 1)
                insertionValues[i] = insertionValues[swapIndex]
                insertionValues[swapIndex] = i + insertionIndex
            }
            insertionPoints.sort()
            val newShuffled = IntArray(shuffled.size + insertionCount)
            var indexInOldShuffled = 0
            var indexInInsertionList = 0
            for (i in 0..<shuffled.size + insertionCount) {
                if (indexInInsertionList < insertionCount
                    && indexInOldShuffled == insertionPoints[indexInInsertionList]
                ) {
                    newShuffled[i] = insertionValues[indexInInsertionList++]
                } else {
                    newShuffled[i] = shuffled[indexInOldShuffled++]
                    if (newShuffled[i] >= insertionIndex) {
                        newShuffled[i] += insertionCount
                    }
                }
            }
            return MusicShuffleOrder(newShuffled, Random(random.nextLong()))
        }

        override fun cloneAndRemove(indexFrom: Int, indexToExclusive: Int): ShuffleOrder {
            val numberOfElementsToRemove = indexToExclusive - indexFrom
            val newShuffled = IntArray(shuffled.size - numberOfElementsToRemove)
            var foundElementsCount = 0
            for (i in shuffled.indices) {
                if (shuffled[i] in indexFrom..<indexToExclusive) {
                    foundElementsCount++
                } else {
                    newShuffled[i - foundElementsCount] =
                        if (shuffled[i] >= indexFrom) shuffled[i] - numberOfElementsToRemove else shuffled[i]
                }
            }
            return MusicShuffleOrder(newShuffled, Random(random.nextLong()))
        }

        override fun cloneAndMove(
            indexFrom: Int,
            indexToExclusive: Int,
            newIndexFrom: Int
        ): ShuffleOrder {
            val newShuffled = IntArray(shuffled.size)
            for (i in shuffled.indices) {
                newShuffled[i] = shuffled[i]
            }
            val tmp = newShuffled[indexFrom]
            newShuffled[indexFrom] = newShuffled[newIndexFrom]
            newShuffled[newIndexFrom] = tmp
            return MusicShuffleOrder(newShuffled, random)
        }

        override fun cloneAndClear(): ShuffleOrder {
            return MusicShuffleOrder( /* length= */0, Random(random.nextLong()))
        }

        companion object {
            internal fun createShuffledList(length: Int, random: Random): IntArray {
                val shuffled = IntArray(length)
                for (i in 0..<length) {
                    val swapIndex = random.nextInt(i + 1)
                    shuffled[i] = shuffled[swapIndex]
                    shuffled[swapIndex] = i
                }
                return shuffled
            }
        }
    }

    private class ServiceStub(service: MusicPlaybackService) : IAudioPlayerService.Stub() {
        private val mService: WeakReference<MusicPlaybackService> = WeakReference(service)
        override fun openFile(audio: Audio) {
            mService.get()?.openFile(audio)
        }

        override fun open(list: List<Audio>, position: Int) {
            mService.get()?.open(list, position)
        }

        override fun stop() {
            mService.get()?.pause()
            mService.get()?.pauseAllPlayersAndStopSelf()
        }

        override fun pause() {
            mService.get()?.pause()
        }

        override fun play() {
            mService.get()?.play()
        }

        override fun prev() {
            mService.get()?.prev()
        }

        override fun next() {
            mService.get()?.next()
        }

        override fun setShuffleMode(shuffleMode: Int) {
            mService.get()?.shuffleMode = shuffleMode != 0
        }

        override fun setRepeatMode(repeatMode: Int) {
            mService.get()?.repeatMode = repeatMode
        }

        override fun closeMiniPlayer() {
            mService.get()?.onceCloseMiniPlayer = true
        }

        override fun refresh() {
            mService.get()?.refresh()
        }

        override fun isPlaying(): Boolean {
            return mService.get()?.musicPlayer?.isPlaying == true
        }

        override fun isPreparing(): Boolean {
            return mService.get()?.musicPlayer?.isPreparing == true
        }

        override fun isInitialized(): Boolean {
            return mService.get()?.musicPlayer?.isPrepared == true
        }

        override fun canPlayAfterCurrent(): Boolean {
            return mService.get()?.canPlayAfterCurrent() == true
        }

        override fun playAfterCurrent(audio: Audio) {
            mService.get()?.playAfterCurrent(audio)
        }

        override fun getQueue(): List<Audio>? {
            return mService.get()?.queue
        }

        override fun duration(): Long {
            return mService.get()?.duration() ?: -1
        }

        override fun position(): Long {
            return mService.get()?.position() ?: -1
        }

        override fun getMiniplayerVisibility(): Boolean {
            return mService.get()?.onceCloseMiniPlayer != true && mService.get()?.currentTrack != null
        }

        override fun seek(position: Long): Long {
            return mService.get()?.seek(position) ?: -1
        }

        override fun skip(position: Int) {
            mService.get()?.skip(position, false)
        }

        override fun getCurrentAudio(): Audio? {
            return mService.get()?.currentTrack
        }

        override fun getCurrentAudioPos(): Int {
            return mService.get()?.currentTrackPos ?: -1
        }

        override fun getArtistName(): String? {
            return mService.get()?.artistName
        }

        override fun getTrackName(): String? {
            return mService.get()?.trackName
        }

        override fun getAlbumName(): String? {
            return mService.get()?.albumName
        }

        override fun getAlbumCover(): String? {
            return mService.get()?.albumCover
        }

        override fun getPath(): String? {
            return mService.get()?.path
        }

        override fun getShuffleMode(): Int {
            return if (mService.get()?.shuffleMode == true) SHUFFLE else SHUFFLE_NONE
        }

        override fun getRepeatMode(): Int {
            return mService.get()?.repeatMode ?: Player.REPEAT_MODE_OFF
        }

        override fun getAudioSessionId(): Int {
            return mService.get()?.audioSessionId ?: -1
        }

        override fun getBufferPercent(): Int {
            return mService.get()?.bufferPercent.orZero()
        }

        override fun getBufferPosition(): Long {
            return mService.get()?.bufferPos.orZero()
        }

        override fun doNotDestroyWhenActivityRecreated() {
            mService.get()?.doNotDestroyWhenActivityRecreated()
        }
    }

    companion object {
        private const val TAG = "MusicPlaybackService"

        const val PLAY_STATE_CHANGED = "dev.ragnarok.fenrir.media.music.play_state_changed"
        const val META_CHANGED = "dev.ragnarok.fenrir.media.music.meta_changed"
        const val TRACK_CHANGED = "dev.ragnarok.fenrir.media.music.track_changed"
        const val PREPARED = "dev.ragnarok.fenrir.media.music.prepared"
        const val SHUFFLE_NONE = 0
        const val SHUFFLE = 1
        const val REPEAT_MODE_CHANGED = "dev.ragnarok.fenrir.media.music.repeat_mode_changed"
        const val SHUFFLE_MODE_CHANGED = "dev.ragnarok.fenrir.media.music.shuffle_mode_changed"
        const val QUEUE_CHANGED = "dev.ragnarok.fenrir.media.music.queue_changed"
        const val REFRESH = "dev.ragnarok.fenrir.media.music.refresh"

        const val CUSTOM_COMMAND_CLOSE = "dev.ragnarok.fenrir.media.music.custom_command_close"
        const val CUSTOM_COMMAND_DISABLE_REPEAT =
            "dev.ragnarok.fenrir.media.music.custom_command_disable_repeat"
        const val CUSTOM_COMMAND_REPEAT_MODE_ONCE =
            "dev.ragnarok.fenrir.media.music.custom_command_repeat_mode_once"
        const val CUSTOM_COMMAND_REPEAT_MODE_ALL =
            "dev.ragnarok.fenrir.media.music.custom_command_repeat_mode_all"

        const val PREFS_REPEAT =
            "music_playback_repeat"
        const val PREFS_SHUFFLE =
            "music_playback_shuffle"

        const val ACTION_PLAYLIST = "start_playlist"
        const val MAX_QUEUE_SIZE = 200

        fun startForPlayList(
            context: Context,
            audios: ArrayList<Audio>,
            position: Int
        ) {
            if (audios.isEmpty()) {
                return
            }
            Logger.d(TAG, "startForPlayList, count: " + audios.size + ", position: " + position)
            val target: ArrayList<Audio>
            var targetPosition: Int
            if (audios.size <= MAX_QUEUE_SIZE) {
                target = audios
                targetPosition = position
            } else {
                target = ArrayList(MAX_QUEUE_SIZE)
                val half = MAX_QUEUE_SIZE / 2
                var startAt = position - half
                if (startAt < 0) {
                    startAt = 0
                }
                targetPosition = position - startAt
                var i = startAt
                while (target.size < MAX_QUEUE_SIZE) {
                    if (i > audios.size - 1) {
                        break
                    }
                    target.add(audios[i])
                    i++
                }
                if (target.size < MAX_QUEUE_SIZE) {
                    var it = startAt - 1
                    while (target.size < MAX_QUEUE_SIZE) {
                        target.add(0, audios[it])
                        targetPosition++
                        it--
                    }
                }
            }
            val intent = Intent(context, MusicPlaybackService::class.java)
            intent.action = ACTION_PLAYLIST
            intent.putParcelableArrayListExtra(Extra.AUDIOS, target)
            intent.putExtra(Extra.POSITION, targetPosition)
            context.startService(intent)
        }
    }
}