package ealvatag.tag.images;

import android.graphics.Bitmap;

/**
 * BufferedImage methods
 * <p>
 * Not compatible with Android, delete from your source tree.
 */
public class Images {
    public static Bitmap getImage(Artwork artwork) throws IllegalArgumentException {
        return artwork.getImage();
    }
}
