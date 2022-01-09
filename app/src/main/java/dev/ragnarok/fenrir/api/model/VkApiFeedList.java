package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VkApiFeedList {

    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title;

    @SerializedName("no_reposts")
    public boolean no_reposts;

    @SerializedName("source_ids")
    public int[] source_ids;

    public VkApiFeedList() {

    }
}