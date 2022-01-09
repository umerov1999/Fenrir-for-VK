package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VkApiAudioMessage implements VKApiAttachment {
    @SerializedName("id")
    public int id;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("duration")
    public int duration;

    @SerializedName("waveform")
    public byte[] waveform;

    @SerializedName("link_ogg")
    public String linkOgg;

    @SerializedName("link_mp3")
    public String linkMp3;

    @SerializedName("access_key")
    public String access_key;

    @SerializedName("transcript")
    public String transcript;

    @Override
    public String getType() {
        return TYPE_AUDIO_MESSAGE;
    }
}