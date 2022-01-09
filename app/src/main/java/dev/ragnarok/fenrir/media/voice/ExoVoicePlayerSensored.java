package dev.ragnarok.fenrir.media.voice;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.media.exo.ExoUtil;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Utils;

public class ExoVoicePlayerSensored implements IVoicePlayer, SensorEventListener {

    private final Context app;
    private final ProxyConfig proxyConfig;
    private final SensorManager sensorManager;
    private final Sensor proxym;
    private final PowerManager.WakeLock proximityWakelock;
    private final MusicIntentReceiver headset;
    private ExoPlayer exoPlayer;
    private int status;
    private AudioEntry playingEntry;
    private boolean supposedToBePlaying;
    private IPlayerStatusListener statusListener;
    private IErrorListener errorListener;
    private boolean isProximityNear;
    private boolean isPlaying;
    private boolean HasPlaying;
    private boolean playbackSpeed;
    private boolean Registered;
    private boolean ProximitRegistered;
    private boolean isHeadset;

    public ExoVoicePlayerSensored(Context context, ProxyConfig config) {
        app = context.getApplicationContext();
        proxyConfig = config;
        status = STATUS_NO_PLAYBACK;
        headset = new MusicIntentReceiver();

        sensorManager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        proxym = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proximityWakelock = ((PowerManager) app.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "fenrir:voip=proxim");
        Registered = false;
        ProximitRegistered = false;
        HasPlaying = false;
    }

    private static boolean isOpusSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || Settings.get().other().isEnable_native() || Settings.get().other().getFFmpegPlugin() != 0;
    }

    @Override
    public boolean toggle(int id, VoiceMessage audio) {
        if (nonNull(playingEntry) && playingEntry.getId() == id) {
            setSupposedToBePlaying(!isSupposedToPlay());
            return false;
        }

        release();

        playingEntry = new AudioEntry(id, audio);
        supposedToBePlaying = true;

        preparePlayer();
        return true;
    }

    private void RegisterCallBack() {
        if (Registered)
            return;
        try {
            Registered = true;
            if (MusicPlaybackController.isPlaying() || MusicPlaybackController.isPreparing()) {
                MusicPlaybackController.notifyForegroundStateChanged(app, true);
                MusicPlaybackController.playOrPause();
                HasPlaying = true;
            }
            isProximityNear = false;
            isHeadset = false;
            exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true);
            sensorManager.registerListener(this, proxym, SensorManager.SENSOR_DELAY_NORMAL);
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            app.registerReceiver(headset, filter);
        } catch (Exception ignored) {
        }
    }

    private void UnRegisterCallBack() {
        if (!Registered)
            return;
        try {
            Registered = false;
            sensorManager.unregisterListener(this);
            app.unregisterReceiver(headset);
            if (HasPlaying) {
                MusicPlaybackController.playOrPause();
                MusicPlaybackController.notifyForegroundStateChanged(app, false);
            }
            HasPlaying = false;
            if (ProximitRegistered) {
                ProximitRegistered = false;
                proximityWakelock.release();
            }
            isProximityNear = false;
            isHeadset = false;
            isPlaying = false;
        } catch (Exception ignored) {
        }
    }

    private void setStatus(int status) {
        if (this.status != status) {
            this.status = status;

            if (nonNull(statusListener)) {
                statusListener.onPlayerStatusChange(status);
            }
        }
    }

    private void preparePlayer() {
        isProximityNear = false;
        isHeadset = false;
        isPlaying = false;
        setStatus(STATUS_PREPARING);

        int extensionRenderer = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        switch (Settings.get().other().getFFmpegPlugin()) {
            case 0:
                extensionRenderer = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
                break;
            case 1:
                extensionRenderer = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON;
                break;
            case 2:
                extensionRenderer = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;
                break;
        }
        exoPlayer = new ExoPlayer.Builder(app, new DefaultRenderersFactory(app).setExtensionRendererMode(extensionRenderer)).build();
        exoPlayer.setWakeMode(C.WAKE_MODE_NETWORK);

        // DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        // DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
        // DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(App.getInstance(), Util.getUserAgent(App.getInstance(), "exoplayer2example"), bandwidthMeterA);

        String userAgent = Constants.USER_AGENT(AccountType.BY_TYPE);

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:

        String url = isOpusSupported() ? Utils.firstNonEmptyString(playingEntry.getAudio().getLinkOgg(), playingEntry.getAudio().getLinkMp3()) : playingEntry.getAudio().getLinkMp3();

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(Utils.getExoPlayerFactory(userAgent, proxyConfig)).createMediaSource(Utils.makeMediaItem(url));
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int state) {
                onInternalPlayerStateChanged(state);
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, @Player.PlayWhenReadyChangeReason int reason) {
                if (isPlaying != playWhenReady) {
                    isPlaying = playWhenReady;
                    if (isPlaying) {
                        RegisterCallBack();
                    } else {
                        UnRegisterCallBack();
                    }
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                onExoPlayerException(error);
                UnRegisterCallBack();
            }
        });

        exoPlayer.setPlayWhenReady(supposedToBePlaying);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.setPlaybackSpeed(playbackSpeed ? 2f : 1f);
        exoPlayer.prepare();
    }

    private void onExoPlayerException(@NonNull PlaybackException e) {
        if (nonNull(errorListener)) {
            errorListener.onPlayError(new PrepareException(e));
        }
    }

    private void onInternalPlayerStateChanged(@Player.State int state) {
        Logger.d("ExoVoicePlayer", "onInternalPlayerStateChanged, state: " + state);

        switch (state) {
            case Player.STATE_READY:
                setStatus(STATUS_PREPARED);
                break;
            case Player.STATE_ENDED:
                setSupposedToBePlaying(false);
                exoPlayer.seekTo(0);
                UnRegisterCallBack();
                break;
            case Player.STATE_BUFFERING:
            case Player.STATE_IDLE:
                break;
        }
    }

    private void setSupposedToBePlaying(boolean supposedToBePlaying) {
        this.supposedToBePlaying = supposedToBePlaying;

        if (supposedToBePlaying) {
            ExoUtil.startPlayer(exoPlayer);
        } else {
            ExoUtil.pausePlayer(exoPlayer);
        }
    }

    private long getDuration() {
        if (isNull(playingEntry) || isNull(playingEntry.getAudio()) || playingEntry.getAudio().getDuration() == 0) {
            return Math.max(exoPlayer.getDuration(), 1L);
        }
        return playingEntry.getAudio().getDuration() * 1000L;
    }

    @Override
    public float getProgress() {
        if (isNull(exoPlayer)) {
            return 0f;
        }

        if (status != STATUS_PREPARED) {
            return 0f;
        }

        long duration = getDuration();
        long position = exoPlayer.getCurrentPosition();
        return (float) position / (float) duration;
    }

    @Override
    public void setCallback(@Nullable IPlayerStatusListener listener) {
        statusListener = listener;
    }

    @Override
    public void setErrorListener(@Nullable IErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public Optional<Integer> getPlayingVoiceId() {
        return isNull(playingEntry) ? Optional.empty() : Optional.wrap(playingEntry.getId());
    }

    @Override
    public boolean isSupposedToPlay() {
        return supposedToBePlaying;
    }

    @Override
    public boolean isPlaybackSpeed() {
        return playbackSpeed;
    }

    @Override
    public void togglePlaybackSpeed() {
        if (nonNull(exoPlayer)) {
            playbackSpeed = !playbackSpeed;
            exoPlayer.setPlaybackSpeed(playbackSpeed ? 2f : 1f);
        }
    }

    @Override
    public void release() {
        try {
            if (nonNull(exoPlayer)) {
                exoPlayer.stop();
                exoPlayer.release();
                UnRegisterCallBack();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isHeadset)
            return;
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            boolean newIsNear = event.values[0] < Math.min(event.sensor.getMaximumRange(), 3);
            if (newIsNear != isProximityNear) {
                isProximityNear = newIsNear;
                try {
                    if (isProximityNear) {
                        exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_SPEECH).setUsage(C.USAGE_VOICE_COMMUNICATION).build(), false);
                        if (!ProximitRegistered) {
                            ProximitRegistered = true;
                            proximityWakelock.acquire(10 * 60 * 1000L /*10 minutes*/);
                        }
                    } else {
                        if (ProximitRegistered) {
                            ProximitRegistered = false;
                            proximityWakelock.release(1); // this is non-public API before L
                        }
                        exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (java.util.Objects.equals(intent.getAction(), Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (isHeadset) {
                            isHeadset = false;
                        }
                        break;
                    case 1:
                        if (!isHeadset) {
                            isHeadset = true;
                            isProximityNear = false;
                            try {
                                if (ProximitRegistered) {
                                    ProximitRegistered = false;
                                    proximityWakelock.release(1); // this is non-public API before L
                                }
                                exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true);
                            } catch (Exception ignored) {
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}