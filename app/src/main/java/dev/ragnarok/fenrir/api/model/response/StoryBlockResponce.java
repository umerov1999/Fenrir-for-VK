package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiStory;

public class StoryBlockResponce {
    @Nullable
    @SerializedName("stories")
    public List<VKApiStory> stories;

    @Nullable
    @SerializedName("grouped")
    public List<StoryBlockResponce> grouped;
}
