package dev.ragnarok.fenrir.db.model.entity;


import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class DocumentEntity extends Entity {

    private int id;

    private int ownerId;

    private String title;

    private long size;

    private String ext;

    private String url;

    private long date;

    private int type;

    private String accessKey;

    private PhotoSizeEntity photo;

    private GraffitiDbo graffiti;

    private VideoPreviewDbo video;

    public DocumentEntity set(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public DocumentEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public long getSize() {
        return size;
    }

    public DocumentEntity setSize(long size) {
        this.size = size;
        return this;
    }

    public String getExt() {
        return ext;
    }

    public DocumentEntity setExt(String ext) {
        this.ext = ext;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DocumentEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    public long getDate() {
        return date;
    }

    public DocumentEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public int getType() {
        return type;
    }

    public DocumentEntity setType(int type) {
        this.type = type;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public DocumentEntity setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public PhotoSizeEntity getPhoto() {
        return photo;
    }

    public DocumentEntity setPhoto(PhotoSizeEntity photo) {
        this.photo = photo;
        return this;
    }

    public GraffitiDbo getGraffiti() {
        return graffiti;
    }

    public DocumentEntity setGraffiti(GraffitiDbo graffiti) {
        this.graffiti = graffiti;
        return this;
    }

    public VideoPreviewDbo getVideo() {
        return video;
    }

    public DocumentEntity setVideo(VideoPreviewDbo video) {
        this.video = video;
        return this;
    }

    public static final class VideoPreviewDbo {

        @SerializedName("src")
        private String src;
        @SerializedName("width")
        private int width;
        @SerializedName("height")
        private int height;
        @SerializedName("fileSize")
        private long fileSize;

        public VideoPreviewDbo set(String src, int width, int height, long fileSize) {
            this.src = src;
            this.width = width;
            this.height = height;
            this.fileSize = fileSize;
            return this;
        }

        public String getSrc() {
            return src;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public long getFileSize() {
            return fileSize;
        }
    }

    public static final class GraffitiDbo {

        @SerializedName("src")
        private String src;
        @SerializedName("width")
        private int width;
        @SerializedName("height")
        private int height;

        public GraffitiDbo set(String src, int width, int height) {
            this.src = src;
            this.width = width;
            this.height = height;
            return this;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getSrc() {
            return src;
        }
    }
}