package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class BaseResponse<T> extends VkReponse {
    @Nullable
    @SerializedName("response")
    public T response;
}