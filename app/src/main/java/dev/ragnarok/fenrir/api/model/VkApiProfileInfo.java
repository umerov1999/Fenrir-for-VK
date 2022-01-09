package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;


public class VkApiProfileInfo {

    @SerializedName("first_name")
    public String first_name;

    @SerializedName("last_name")
    public String last_name;

    @SerializedName("maiden_name")
    public String maiden_name;

    @SerializedName("screen_name")
    public String screen_name;

    @SerializedName("home_town")
    public String home_town;

    @SerializedName("bdate")
    public String bdate;

    @SerializedName("sex")
    public int sex;
}
