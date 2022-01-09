package dev.ragnarok.fenrir.api.model.feedback;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Copies {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<IdPair> pairs;

    public static class IdPair {

        @SerializedName("id")
        public int id;

        @SerializedName("from_id")
        public int owner_id;
    }
}
