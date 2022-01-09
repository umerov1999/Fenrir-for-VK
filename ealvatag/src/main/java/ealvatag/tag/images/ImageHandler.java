package ealvatag.tag.images;

import android.graphics.Bitmap;

/**
 * Image Handler
 */
public interface ImageHandler {
    void reduceQuality(Artwork artwork, int maxSize);

    void makeSmaller(Artwork artwork, int size);

    byte[] writeImageAsPng(Bitmap bi);
}
