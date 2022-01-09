package dev.ragnarok.fenrir.media.voice;

import androidx.annotation.NonNull;

public interface IVoicePlayerFactory {
    @NonNull
    IVoicePlayer createPlayer();
}