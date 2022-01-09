package dev.ragnarok.fenrir.model;

import java.util.List;


public class CommunityDetails {

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

    private Audio statusAudio;

    private Cover cover;

    private String description;

    public Cover getCover() {
        return cover;
    }

    public CommunityDetails setCover(Cover cover) {
        this.cover = cover;
        return this;
    }

    public int getChatsCount() {
        return chatsCount;
    }

    public CommunityDetails setChatsCount(int chatsCount) {
        this.chatsCount = chatsCount;
        return this;
    }

    public boolean isSetFavorite() {
        return isFavorite;
    }

    public CommunityDetails setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        return this;
    }

    public boolean isSetSubscribed() {
        return isSubscribed;
    }

    public CommunityDetails setSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
        return this;
    }

    public int getAllWallCount() {
        return allWallCount;
    }

    public CommunityDetails setAllWallCount(int allWallCount) {
        this.allWallCount = allWallCount;
        return this;
    }

    public int getOwnerWallCount() {
        return ownerWallCount;
    }

    public CommunityDetails setOwnerWallCount(int ownerWallCount) {
        this.ownerWallCount = ownerWallCount;
        return this;
    }

    public int getPostponedWallCount() {
        return postponedWallCount;
    }

    public CommunityDetails setPostponedWallCount(int postponedWallCount) {
        this.postponedWallCount = postponedWallCount;
        return this;
    }

    public int getSuggestedWallCount() {
        return suggestedWallCount;
    }

    public CommunityDetails setSuggestedWallCount(int suggestedWallCount) {
        this.suggestedWallCount = suggestedWallCount;
        return this;
    }

    public boolean isCanMessage() {
        return canMessage;
    }

    public CommunityDetails setCanMessage(boolean canMessage) {
        this.canMessage = canMessage;
        return this;
    }

    public Audio getStatusAudio() {
        return statusAudio;
    }

    public CommunityDetails setStatusAudio(Audio statusAudio) {
        this.statusAudio = statusAudio;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public CommunityDetails setStatus(String status) {
        this.status = status;
        return this;
    }

    public int getTopicsCount() {
        return topicsCount;
    }

    public CommunityDetails setTopicsCount(int topicsCount) {
        this.topicsCount = topicsCount;
        return this;
    }

    public int getDocsCount() {
        return docsCount;
    }

    public CommunityDetails setDocsCount(int docsCount) {
        this.docsCount = docsCount;
        return this;
    }

    public int getPhotosCount() {
        return photosCount;
    }

    public CommunityDetails setPhotosCount(int photosCount) {
        this.photosCount = photosCount;
        return this;
    }

    public int getArticlesCount() {
        return articlesCount;
    }

    public CommunityDetails setArticlesCount(int articlesCount) {
        this.articlesCount = articlesCount;
        return this;
    }

    public int getProductsCount() {
        return productsCount;
    }

    public CommunityDetails setProductsCount(int productsCount) {
        this.productsCount = productsCount;
        return this;
    }

    public int getAudiosCount() {
        return audiosCount;
    }

    public CommunityDetails setAudiosCount(int audiosCount) {
        this.audiosCount = audiosCount;
        return this;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public CommunityDetails setVideosCount(int videosCount) {
        this.videosCount = videosCount;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CommunityDetails setDescription(String description) {
        this.description = description;
        return this;
    }

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

    public static final class CoverImage {

        private final String url;

        private final int height;

        private final int width;

        public CoverImage(String url, int height, int width) {
            this.url = url;
            this.height = height;
            this.width = width;
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