package dev.ragnarok.fenrir.model;

import java.util.List;

import dev.ragnarok.fenrir.util.Utils;

public class StickerSet {

    private final String title;
    private final List<Image> icon;
    private final List<Sticker> stickers;

    public StickerSet(List<Image> icon, List<Sticker> stickers, String title) {
        this.icon = icon;
        this.stickers = stickers;
        this.title = title;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public String getTitle() {
        return title;
    }

    public List<Image> getIcon() {
        return icon;
    }

    public String getImageUrl(int prefSize) {
        if (Utils.isEmpty(icon)) {
            return null;
        }
        Image result = null;

        for (Image image : icon) {
            if (result == null) {
                result = image;
                continue;
            }

            if (Math.abs(image.calcAverageSize() - prefSize) < Math.abs(result.calcAverageSize() - prefSize)) {
                result = image;
            }
        }

        if (result == null) {
            // default
            return icon.get(0).url;
        }

        return result.url;
    }

    public static final class Image {
        private final String url;
        private final int width;
        private final int height;

        public Image(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        private int calcAverageSize() {
            return (width + height) / 2;
        }
    }
}