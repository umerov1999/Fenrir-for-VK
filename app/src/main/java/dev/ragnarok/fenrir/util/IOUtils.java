package dev.ragnarok.fenrir.util;

import android.database.Cursor;
import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    public static void recycleBitmapQuietly(Bitmap bitmap) {
        if (bitmap != null) bitmap.recycle();
    }

    public static void closeStreamQuietly(InputStream streamToClose) {
        if (streamToClose == null) return;

        try {
            streamToClose.close();
        } catch (IOException ignored) {
        }
    }

    public static void closeStreamQuietly(OutputStream streamToClose) {
        if (streamToClose == null) return;

        try {
            streamToClose.close();
        } catch (IOException ignored) {
        }
    }

    public static void closeCursorQuietly(Cursor cursor) {
        if (cursor == null) return;

        try {
            cursor.close();
        } catch (Exception ignored) {
        }
    }
}
