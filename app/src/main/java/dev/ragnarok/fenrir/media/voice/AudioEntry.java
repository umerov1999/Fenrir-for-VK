package dev.ragnarok.fenrir.media.voice;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.VoiceMessage;

public class AudioEntry {

    private final int id;
    private final VoiceMessage audio;

    public AudioEntry(int id, @NonNull VoiceMessage audio) {
        this.id = id;
        this.audio = audio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioEntry entry = (AudioEntry) o;
        return id == entry.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int getId() {
        return id;
    }

    public VoiceMessage getAudio() {
        return audio;
    }
}