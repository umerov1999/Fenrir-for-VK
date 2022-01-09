package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiStory;

public class StoryBlockResponce {

    @SerializedName("stories")
    public List<VKApiStory> stories;

    @SerializedName("grouped")
    public List<StoryBlockResponce> grouped;
}
