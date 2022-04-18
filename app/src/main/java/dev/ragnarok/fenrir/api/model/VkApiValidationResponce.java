package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class VKApiValidationResponce {

    @SerializedName("sid")
    public String sid;

    @SerializedName("delay")
    public int delay;

    @Nullable
    @SerializedName("validation_type")
    public String validation_type;

    @Nullable
    @SerializedName("validation_resend")
    public String validation_resend;
}
