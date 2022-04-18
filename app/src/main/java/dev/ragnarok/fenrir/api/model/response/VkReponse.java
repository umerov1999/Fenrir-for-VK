package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.Error;


public class VkReponse {

    @Nullable
    @SerializedName("error")
    public Error error;

    @Nullable
    @SerializedName("execute_errors")
    public List<Error> executeErrors;
}
