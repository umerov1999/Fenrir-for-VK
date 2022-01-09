package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class AudioMessageEntity extends Entity {

    private int id;

    private int ownerId;

    private int duration;

    private byte[] waveform;

    private String linkOgg;

    private String linkMp3;

    private String accessKey;

    private String transcript;

    public AudioMessageEntity set(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getDuration() {
        return duration;
    }

    public AudioMessageEntity setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public byte[] getWaveform() {
        return waveform;
    }

    public AudioMessageEntity setWaveform(byte[] waveform) {
        this.waveform = waveform;
        return this;
    }

    public String getLinkOgg() {
        return linkOgg;
    }

    public AudioMessageEntity setLinkOgg(String linkOgg) {
        this.linkOgg = linkOgg;
        return this;
    }

    public String getLinkMp3() {
        return linkMp3;
    }

    public AudioMessageEntity setLinkMp3(String linkMp3) {
        this.linkMp3 = linkMp3;
        return this;
    }

    public String getTranscript() {
        return transcript;
    }

    public AudioMessageEntity setTranscript(String transcript) {
        this.transcript = transcript;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public AudioMessageEntity setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }
}