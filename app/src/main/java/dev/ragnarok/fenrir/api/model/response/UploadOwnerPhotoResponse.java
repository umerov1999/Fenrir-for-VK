package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

public class UploadOwnerPhotoResponse {

    // response={"photo_hash":"aeb0c37a143182779da895766ae91413",
    // "photo_src":"http:\/\/cs625317.vk.me\/v625317989\/42a6c\/Vz-XBvhN_8c.jpg",
    // "photo_src_big":"http:\/\/cs625317.vk.me\/v625317989\/42a6d\/WpiNL1m8VAk.jpg",
    // "photo_src_small":"http:\/\/cs625317.vk.me\/v625317989\/42a6b\/ILQx6m7KhCY.jpg",
    // "saved":1,
    // "post_id":2596}

    @SerializedName("photo_hash")
    public String photoHash;

    @SerializedName("photo_src")
    public String photoSrc;

    @SerializedName("photo_src_big")
    public String photoSrcBig;

    @SerializedName("photo_src_small")
    public String photoSrcSmall;

    @SerializedName("saved")
    public boolean saved;

    @SerializedName("post_id")
    public int postId;
}