package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class SlidrSettings {
    @SerializedName("vertical_sensitive")
    public float vertical_sensitive;

    @SerializedName("horizontal_sensitive")
    public float horizontal_sensitive;

    @SerializedName("vertical_velocity_threshold")
    public float vertical_velocity_threshold;

    @SerializedName("horizontal_velocity_threshold")
    public float horizontal_velocity_threshold;

    @SerializedName("vertical_distance_threshold")
    public float vertical_distance_threshold;

    @SerializedName("horizontal_distance_threshold")
    public float horizontal_distance_threshold;

    public SlidrSettings set_default() {
        horizontal_sensitive = 0.5f;
        vertical_sensitive = 0.7f;
        vertical_velocity_threshold = 5f;
        horizontal_velocity_threshold = 5f;
        vertical_distance_threshold = 0.11f;
        horizontal_distance_threshold = 0.25f;
        return this;
    }
}
