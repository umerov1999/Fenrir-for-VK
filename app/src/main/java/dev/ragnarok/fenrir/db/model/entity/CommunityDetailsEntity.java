package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

import java.util.List;

@Keep
public class CommunityDetailsEntity {

    private int allWallCount;

    private int ownerWallCount;

    private int postponedWallCount;

    private int suggestedWallCount;

    private boolean canMessage;

    private boolean isFavorite;

    private boolean isSubscribed;

    private int topicsCount;

    private int docsCount;

    private int photosCount;

    private int audiosCount;

    private int videosCount;

    private int articlesCount;

    private int productsCount;

    private int chatsCount;

    private String status;

    private AudioEntity statusAudio;

    private Cover cover;

    private String description;

    public Cover getCover() {
        return cover;
    }

    public CommunityDetailsEntity setCover(Cover cover) {
        this.cover = cover;
        return this;
    }

    public int getChatsCount() {
        return chatsCount;
    }

    public CommunityDetailsEntity setChatsCount(int chatsCount) {
        this.chatsCount = chatsCount;
        return this;
    }

    public int getAllWallCount() {
        return allWallCount;
    }

    public CommunityDetailsEntity setAllWallCount(int allWallCount) {
        this.allWallCount = allWallCount;
        return this;
    }

    public int getOwnerWallCount() {
        return ownerWallCount;
    }

    public CommunityDetailsEntity setOwnerWallCount(int ownerWallCount) {
        this.ownerWallCount = ownerWallCount;
        return this;
    }

    public int getPostponedWallCount() {
        return postponedWallCount;
    }

    public CommunityDetailsEntity setPostponedWallCount(int postponedWallCount) {
        this.postponedWallCount = postponedWallCount;
        return this;
    }

    public int getSuggestedWallCount() {
        return suggestedWallCount;
    }

    public CommunityDetailsEntity setSuggestedWallCount(int suggestedWallCount) {
        this.suggestedWallCount = suggestedWallCount;
        return this;
    }

    public boolean isCanMessage() {
        return canMessage;
    }

    public CommunityDetailsEntity setCanMessage(boolean canMessage) {
        this.canMessage = canMessage;
        return this;
    }

    public boolean isSetFavorite() {
        return isFavorite;
    }

    public CommunityDetailsEntity setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        return this;
    }

    public boolean isSetSubscribed() {
        return isSubscribed;
    }

    public CommunityDetailsEntity setSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
        return this;
    }

    public AudioEntity getStatusAudio() {
        return statusAudio;
    }

    public CommunityDetailsEntity setStatusAudio(AudioEntity statusAudio) {
        this.statusAudio = statusAudio;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public CommunityDetailsEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CommunityDetailsEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getTopicsCount() {
        return topicsCount;
    }

    public CommunityDetailsEntity setTopicsCount(int topicsCount) {
        this.topicsCount = topicsCount;
        return this;
    }

    public int getDocsCount() {
        return docsCount;
    }

    public CommunityDetailsEntity setDocsCount(int docsCount) {
        this.docsCount = docsCount;
        return this;
    }

    public int getArticlesCount() {
        return articlesCount;
    }

    public CommunityDetailsEntity setArticlesCount(int articlesCount) {
        this.articlesCount = articlesCount;
        return this;
    }

    public int getProductsCount() {
        return productsCount;
    }

    public CommunityDetailsEntity setProductsCount(int productsCount) {
        this.productsCount = productsCount;
        return this;
    }

    public int getPhotosCount() {
        return photosCount;
    }

    public CommunityDetailsEntity setPhotosCount(int photosCount) {
        this.photosCount = photosCount;
        return this;
    }

    public int getAudiosCount() {
        return audiosCount;
    }

    public CommunityDetailsEntity setAudiosCount(int audiosCount) {
        this.audiosCount = audiosCount;
        return this;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public CommunityDetailsEntity setVideosCount(int videosCount) {
        this.videosCount = videosCount;
        return this;
    }

    @Keep
    public static final class Cover {

        private boolean enabled;

        private List<CoverImage> images;

        public List<CoverImage> getImages() {
            return images;
        }

        public Cover setImages(List<CoverImage> images) {
            this.images = images;
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Cover setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
    }

    @Keep
    public static final class CoverImage {

        private String url;

        private int height;

        private int width;

        public CoverImage set(String url, int height, int width) {
            this.url = url;
            this.height = height;
            this.width = width;
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
