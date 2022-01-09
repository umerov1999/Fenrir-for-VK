package dev.ragnarok.fenrir.media.gif;

import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.VideoSize;

public interface IGifPlayer {

    VideoSize getVideoSize();

    void play() throws PlayerPrepareException;

    void pause();

    void setDisplay(SurfaceHolder holder);

    void release();

    void addVideoSizeChangeListener(IVideoSizeChangeListener listener);

    void addStatusChangeListener(IStatusChangeListener listener);

    void removeVideoSizeChangeListener(IVideoSizeChangeListener listener);

    void removeStatusChangeListener(IStatusChangeListener listener);

    int getPlayerStatus();

    interface IStatus {
        int INIT = 1;
        int PREPARING = 2;
        int PREPARED = 3;
        int ENDED = 4;
    }

    interface IVideoSizeChangeListener {
        void onVideoSizeChanged(@NonNull IGifPlayer player, VideoSize size);
    }

    interface IStatusChangeListener {
        void onPlayerStatusChange(@NonNull IGifPlayer player, int previousStatus, int currentStatus);
    }
}