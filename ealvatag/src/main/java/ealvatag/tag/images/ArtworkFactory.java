package ealvatag.tag.images;

import java.io.File;
import java.io.IOException;

/**
 * Get appropriate Artwork class
 */
public class ArtworkFactory {

    public static Artwork getNew() {
        return new StandardArtwork();
    }

    public static Artwork createArtworkFromFile(File file) throws IOException {
        return StandardArtwork.createArtworkFromFile(file);
    }

    public static Artwork createLinkedArtworkFromURL(String link) throws IOException {
        return StandardArtwork.createLinkedArtworkFromURL(link);
    }
}
