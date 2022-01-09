package dev.ragnarok.fenrir.media.voice;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.util.Optional;

public interface IVoicePlayer {

    int STATUS_NO_PLAYBACK = 0;
    int STATUS_PREPARING = 1;
    int STATUS_PREPARED = 2;

    boolean toggle(int id, VoiceMessage audio) throws PrepareException;

    float getProgress();

    void setCallback(@Nullable IPlayerStatusListener listener);

    void setErrorListener(@Nullable IErrorListener errorListener);

    Optional<Integer> getPlayingVoiceId();

    boolean isSupposedToPlay();

    boolean isPlaybackSpeed();

    void togglePlaybackSpeed();

    void release();

    interface IPlayerStatusListener {
        void onPlayerStatusChange(int status);
    }

    interface IErrorListener {
        void onPlayError(Throwable t);
    }
}