package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VkApiValidationResponce {

    @SerializedName("sid")
    public String sid;

    @SerializedName("delay")
    public int delay;

    @SerializedName("validation_type")
    public String validation_type;

    @SerializedName("validation_resend")
    public String validation_resend;
}
