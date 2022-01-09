package ealvatag.tag.images;

/**
 * Provides a class for all Image handling, this is required because the image classes
 * provided by standard java are different to those provided by Android
 */
public class ImageHandlingFactory {
    private static StandardImageHandler standardImageHandler;

    public static ImageHandler getInstance() {
        if (standardImageHandler == null) {
            standardImageHandler = StandardImageHandler.getInstanceOf();
        }
        return standardImageHandler;
    }
}
