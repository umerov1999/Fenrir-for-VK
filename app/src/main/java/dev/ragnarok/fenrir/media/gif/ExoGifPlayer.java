package dev.ragnarok.fenrir.media.gif;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.App;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.model.VideoSize;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Utils;

public class ExoGifPlayer implements IGifPlayer {

    private final String url;
    private final ProxyConfig proxyConfig;
    private final List<IVideoSizeChangeListener> videoSizeChangeListeners = new ArrayList<>(1);
    private final List<IStatusChangeListener> statusChangeListeners = new ArrayList<>(1);
    private final boolean isRepeat;
    private int status;
    private VideoSize size;
    private final Player.Listener videoListener = new Player.Listener() {
        @Override
        public void onVideoSizeChanged(@NonNull com.google.android.exoplayer2.video.VideoSize videoSize) {
            size = new VideoSize(videoSize.width, videoSize.height);
            ExoGifPlayer.this.onVideoSizeChanged();
        }

        @Override
        public void onRenderedFirstFrame() {

        }

        @Override
        public void onPlaybackStateChanged(@Player.State int state) {
            Logger.d("FenrirExo", "onPlaybackStateChanged, state: " + state);
            onInternalPlayerStateChanged(state);
        }
    };
    private ExoPlayer internalPlayer;
    private boolean supposedToBePlaying;

    public ExoGifPlayer(String url, ProxyConfig proxyConfig, boolean isRepeat) {
        this.isRepeat = isRepeat;
        this.url = url;
        this.proxyConfig = proxyConfig;
        status = IStatus.INIT;
    }

    private static void pausePlayer(ExoPlayer internalPlayer) {
        internalPlayer.setPlayWhenReady(false);
        internalPlayer.getPlaybackState();
    }

    private static void startPlayer(ExoPlayer internalPlayer) {
        internalPlayer.setPlayWhenReady(true);
        internalPlayer.getPlaybackState();
    }

    @Override
    public VideoSize getVideoSize() {
        return size;
    }

    @Override
    public void play() {
        if (supposedToBePlaying) return;

        supposedToBePlaying = true;

        switch (status) {
            case IStatus.PREPARED:
                AssertUtils.requireNonNull(internalPlayer);
                startPlayer(internalPlayer);
                break;
            case IStatus.INIT:
                preparePlayer();
                break;
            case IStatus.PREPARING:
                //do nothing
                break;
        }
    }

    private void preparePlayer() {
        setStatus(IStatus.PREPARING);
        internalPlayer = new ExoPlayer.Builder(App.getInstance()).build();


        String userAgent = Constants.USER_AGENT(AccountType.BY_TYPE);

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(Utils.getExoPlayerFactory(userAgent, proxyConfig)).createMediaSource(Utils.makeMediaItem((url)));
        internalPlayer.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        internalPlayer.addListener(videoListener);
        internalPlayer.setPlayWhenReady(true);
        internalPlayer.setMediaSource(mediaSource);
        internalPlayer.prepare();
    }

    private void onInternalPlayerStateChanged(@Player.State int state) {
        if (state == Player.STATE_READY) {
            setStatus(IStatus.PREPARED);
        } else if (state == Player.STATE_ENDED && !isRepeat) {
            setStatus(IStatus.ENDED);
        }
    }

    private void onVideoSizeChanged() {
        for (IVideoSizeChangeListener listener : videoSizeChangeListeners) {
            listener.onVideoSizeChanged(this, size);
        }
    }

    @Override
    public void pause() {
        if (!supposedToBePlaying) return;

        supposedToBePlaying = false;

        if (nonNull(internalPlayer)) {
            try {
                pausePlayer(internalPlayer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if (nonNull(internalPlayer)) {
            internalPlayer.setVideoSurfaceHolder(holder);
        }
    }

    @Override
    public void release() {
        if (nonNull(internalPlayer)) {
            try {
                internalPlayer.removeListener(videoListener);
                internalPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setStatus(int newStatus) {
        int oldStatus = status;

        if (status == newStatus) {
            return;
        }

        status = newStatus;
        for (IStatusChangeListener listener : statusChangeListeners) {
            listener.onPlayerStatusChange(this, oldStatus, newStatus);
        }
    }

    @Override
    public void addVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        videoSizeChangeListeners.add(listener);
    }

    @Override
    public void addStatusChangeListener(IStatusChangeListener listener) {
        statusChangeListeners.add(listener);
    }

    @Override
    public void removeVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        videoSizeChangeListeners.remove(listener);
    }

    @Override
    public void removeStatusChangeListener(IStatusChangeListener listener) {
        statusChangeListeners.remove(listener);
    }

    @Override
    public int getPlayerStatus() {
        return status;
    }
}