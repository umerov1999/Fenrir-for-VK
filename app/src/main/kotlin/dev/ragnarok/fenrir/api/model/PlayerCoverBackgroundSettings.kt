package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class PlayerCoverBackgroundSettings {
    @SerializedName("enabled_rotation")
    var enabled_rotation = false

    @SerializedName("invert_rotation")
    var invert_rotation = false

    @SerializedName("fade_saturation")
    var fade_saturation = false

    @SerializedName("rotation_speed")
    var rotation_speed = 0f

    @SerializedName("zoom")
    var zoom = 0f

    @SerializedName("blur")
    var blur = 0
    fun set_default(): PlayerCoverBackgroundSettings {
        enabled_rotation = true
        invert_rotation = false
        fade_saturation = true
        rotation_speed = 0.3f
        zoom = 1.2f
        blur = 16
        return this
    }
}