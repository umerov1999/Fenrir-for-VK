package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiAudioMessage : VKApiAttachment {
    @SerializedName("id")
    var id = 0

    @SerializedName("owner_id")
    var owner_id = 0

    @SerializedName("duration")
    var duration = 0

    @SerializedName("waveform")
    var waveform: ByteArray? = null

    @SerializedName("link_ogg")
    var linkOgg: String? = null

    @SerializedName("link_mp3")
    var linkMp3: String? = null

    @SerializedName("access_key")
    var access_key: String? = null

    @SerializedName("was_listened")
    var was_listened = false

    @SerializedName("transcript")
    var transcript: String? = null
    override fun getType(): String {
        return VKApiAttachment.TYPE_AUDIO_MESSAGE
    }
}