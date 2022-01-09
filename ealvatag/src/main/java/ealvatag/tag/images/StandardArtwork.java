package ealvatag.tag.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;

public class StandardArtwork extends AbstractArtwork {

    StandardArtwork() {
    }

    static StandardArtwork createArtworkFromFile(File file) throws IOException {
        StandardArtwork artwork = new StandardArtwork();
        artwork.setFromFile(file);
        return artwork;
    }

    static StandardArtwork createLinkedArtworkFromURL(String url) {
        StandardArtwork artwork = new StandardArtwork();
        artwork.setImageUrl(url);
        return artwork;
    }

    public boolean setImageFromData() {
        try {
            Bitmap image = getImage();
            setWidth(image.getWidth());
            setHeight(image.getHeight());
        } catch (Exception ioe) {
            return false;
        }
        return true;
    }

    public Bitmap getImage() throws IllegalArgumentException {
        byte[] data = getBinaryData();
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

}
