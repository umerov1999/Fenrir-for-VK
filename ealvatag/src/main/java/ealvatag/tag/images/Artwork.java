package ealvatag.tag.images;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;

/**
 * Represents artwork in a format independent  way
 */
public interface Artwork {
    byte[] getBinaryData();

    Artwork setBinaryData(byte[] binaryData);

    String getMimeType();

    Artwork setMimeType(String mimeType);

    String getDescription();

    Artwork setDescription(String description);

    int getHeight();

    Artwork setHeight(int height);

    int getWidth();

    Artwork setWidth(int width);

    /**
     * Should be called when you wish to prime the artwork for saving.
     *
     * @return true if successful or if AndroidArtwork (always returns true)
     */
    boolean setImageFromData();

    /**
     * @return a BufferedImage if not on the Android platform. Null on Android
     * @throws IllegalArgumentException if error reading the image data
     */
    Bitmap getImage() throws IllegalArgumentException;

    boolean isLinked();

    Artwork setLinked(boolean linked);

    String getImageUrl();

    Artwork setImageUrl(String imageUrl);

    int getPictureType();

    Artwork setPictureType(int pictureType);

    Artwork setFromFile(File file) throws IOException;
}
