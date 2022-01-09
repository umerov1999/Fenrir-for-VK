package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

import java.util.List;

@Keep
public class AudioArtistEntity extends Entity {
    private String id;
    private String name;
    private List<AudioArtistImageEntity> photo;

    public String getId() {
        return id;
    }

    public AudioArtistEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AudioArtistEntity setName(String name) {
        this.name = name;
        return this;
    }

    public List<AudioArtistImageEntity> getPhoto() {
        return photo;
    }

    public AudioArtistEntity setPhoto(List<AudioArtistImageEntity> photo) {
        this.photo = photo;
        return this;
    }

    @Keep
    public static final class AudioArtistImageEntity {

        private String url;
        private int width;
        private int height;

        public AudioArtistImageEntity set(String url, int width, int height) {
            this.url = url;
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

        public String getUrl() {
            return url;
        }
    }
}
