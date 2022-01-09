package dev.ragnarok.fenrir.media.video;

import android.content.Context;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.InternalVideoSize;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.model.VideoSize;


public interface IVideoPlayer {
    void updateSource(Context context, String url, ProxyConfig config, @InternalVideoSize int size);

    void play();

    void pause();

    void release();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long position);

    boolean isPlaying();

    int getBufferPercentage();

    long getBufferPosition();

    void setSurfaceHolder(SurfaceHolder holder);

    boolean isPlaybackSpeed();

    void togglePlaybackSpeed();

    void addVideoSizeChangeListener(IVideoSizeChangeListener listener);

    void removeVideoSizeChangeListener(IVideoSizeChangeListener listener);

    interface IVideoSizeChangeListener {
        void onVideoSizeChanged(@NonNull IVideoPlayer player, VideoSize size);
    }

    interface IUpdatePlayListener {
        void onPlayChanged(boolean isPause);
    }
}