package dev.ragnarok.fenrir.upload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.util.IOUtils;

public final class UploadUtils {

    private UploadUtils() {

    }

    private static void copyExif(ExifInterface originalExif, int width, int height, File imageOutputPath) {
        String[] attributes = {
                ExifInterface.TAG_F_NUMBER,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_FLASH,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_GPS_ALTITUDE_REF,
                ExifInterface.TAG_GPS_DATESTAMP,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                ExifInterface.TAG_GPS_PROCESSING_METHOD,
                ExifInterface.TAG_GPS_TIMESTAMP,
                ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_SUBSEC_TIME,
                ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
                ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                ExifInterface.TAG_WHITE_BALANCE
        };

        try {
            ExifInterface newExif = new ExifInterface(imageOutputPath);
            String value;
            for (String attribute : attributes) {
                value = originalExif.getAttribute(attribute);
                if (!TextUtils.isEmpty(value)) {
                    newExif.setAttribute(attribute, value);
                }
            }
            newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(width));
            newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(height));
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, "0");

            newExif.saveAttributes();

        } catch (IOException e) {
            Log.d("Exif upload resize", e.getMessage());
        }
    }

    private static Bitmap transformBitmap(@NonNull Bitmap bitmap, @NonNull Matrix transformMatrix) {
        try {
            Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformMatrix, true);
            if (!bitmap.sameAs(converted)) {
                bitmap = converted;
            }
        } catch (OutOfMemoryError error) {
            Log.e("Exif upload resize", "transformBitmap: ", error);
        }
        return bitmap;
    }

    public static InputStream createStream(Context context, Uri uri) throws IOException {
        InputStream originalStream;

        File filef = new File(uri.getPath());
        if (filef.isFile()) {
            originalStream = new FileInputStream(filef);
        } else {
            originalStream = context.getContentResolver().openInputStream(uri);
        }
        return originalStream;
    }

    public static InputStream openStream(Context context, Uri uri, int size) throws IOException {
        InputStream originalStream;

        File filef = new File(uri.getPath());
        if (filef.isFile()) {
            originalStream = new FileInputStream(filef);
        } else {
            originalStream = context.getContentResolver().openInputStream(uri);
        }

        if (size == Upload.IMAGE_SIZE_FULL || size == Upload.IMAGE_SIZE_CROPPING) {
            return originalStream;
        }

        Bitmap bitmap = BitmapFactory.decodeStream(originalStream);

        ExifInterface originalExif = null;
        Matrix matrix = new Matrix();
        boolean bApply = false;
        try {
            originalExif = new ExifInterface(createStream(context, uri));
            if (originalExif.getRotationDegrees() != 0) {
                matrix.preRotate(originalExif.getRotationDegrees());
                bApply = true;
            }
        } catch (Exception ignored) {
        }
        if (bApply) {
            bitmap = transformBitmap(bitmap, matrix);
        }

        File tempFile = new File(context.getExternalCacheDir() + File.separator + "scale.jpg");
        Bitmap target = null;

        try {
            if (tempFile.exists()) {
                if (!tempFile.delete()) {
                    throw new IOException("Unable to delete old image file");
                }
            }

            if (!tempFile.createNewFile()) {
                throw new IOException("Unable to create new file");
            }
            FileOutputStream ostream = new FileOutputStream(tempFile);
            target = scaleDown(bitmap, size, true);
            target.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.flush();
            ostream.close();

            if (originalExif != null) {
                copyExif(originalExif, bitmap.getWidth(), bitmap.getHeight(), tempFile);
            }
            return new FileInputStream(tempFile);
        } finally {
            IOUtils.recycleBitmapQuietly(bitmap);
            IOUtils.recycleBitmapQuietly(target);
            IOUtils.closeStreamQuietly(originalStream);
        }
    }

    public static List<UploadIntent> createIntents(int accountId, UploadDestination destination, List<LocalPhoto> photos, int size,
                                                   boolean autoCommit) {
        List<UploadIntent> intents = new ArrayList<>(photos.size());
        for (LocalPhoto photo : photos) {
            intents.add(new UploadIntent(accountId, destination)
                    .setSize(size)
                    .setAutoCommit(autoCommit)
                    .setFileId(photo.getImageId())
                    .setFileUri(photo.getFullImageUri()));
        }
        return intents;
    }

    public static List<UploadIntent> createVideoIntents(int accountId, UploadDestination destination, String path,
                                                        boolean autoCommit) {
        UploadIntent intent = new UploadIntent(accountId, destination).setAutoCommit(autoCommit).setFileUri(Uri.parse(path));
        return Collections.singletonList(intent);
    }

    public static List<UploadIntent> createIntents(int accountId, UploadDestination destination, String file, int size,
                                                   boolean autoCommit) {
        List<UploadIntent> intents = new ArrayList<>();
        intents.add(new UploadIntent(accountId, destination)
                .setSize(size)
                .setAutoCommit(autoCommit)
                .setFileUri(Uri.parse(file)));
        return intents;
    }

    private static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        if (realImage.getHeight() < maxImageSize && realImage.getWidth() < maxImageSize) {
            return realImage;
        }

        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }
}