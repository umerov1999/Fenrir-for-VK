package ealvatag.tag.images;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

/**
 * Image Handling used when running on standard JVM
 */
public class StandardImageHandler implements ImageHandler {
    private static volatile StandardImageHandler instance;

    private StandardImageHandler() {

    }

    public static StandardImageHandler getInstanceOf() {
        if (instance == null) {
            synchronized (StandardImageHandler.class) {
                if (instance == null) {
                    instance = new StandardImageHandler();
                }
            }
        }
        return instance;
    }

    /**
     * Resize the image until the total size require to store the image is less than maxsize
     */
    public void reduceQuality(Artwork artwork, int maxSize) {
        while (artwork.getBinaryData().length > maxSize) {
            Bitmap srcImage = artwork.getImage();
            int w = srcImage.getWidth();
            int newSize = w / 2;
            makeSmaller(artwork, newSize);
        }
    }

    public void makeSmaller(Artwork artwork, int size) {
        Bitmap srcImage = artwork.getImage();

        int w = srcImage.getWidth();
        int h = srcImage.getHeight();

        // Determine the scaling required to get desired result.
        float scaleW = (float) size / (float) w;
        float scaleH = (float) size / (float) h;

        //Create an image buffer in which to paint on, create as an opaque Rgb type image, it doesnt matter what type
        //the original image is we want to convert to the best type for displaying on screen regardless

        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleW, scaleH);

        artwork.setBinaryData(writeImageAsPng(Bitmap.createBitmap(srcImage, 0, 0, w, h, matrix, false)));
    }

    public byte[] writeImageAsPng(Bitmap bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bi.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}
