package dev.ragnarok.fenrir.picasso.transforms;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.os.Build;

public class ImageHelper {

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
            Bitmap tmpBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            bitmap.recycle();
            bitmap = tmpBitmap;
            if (bitmap == null) {
                return null;
            }
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawOval(0f, 0f, bitmap.getWidth(), bitmap.getHeight(), paint);

        if (bitmap != output) {
            bitmap.recycle();
        }
        return output;
    }

    public static Bitmap getEllipseBitmap(Bitmap bitmap, float angle) {
        if (bitmap == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
            Bitmap tmpBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            bitmap.recycle();
            bitmap = tmpBitmap;
            if (bitmap == null) {
                return null;
            }
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        float pth = ((float) (bitmap.getWidth() + bitmap.getHeight())) / 2;
        canvas.drawRoundRect(0f, 0f, bitmap.getWidth(), bitmap.getHeight(), pth * angle, pth * angle, paint);

        if (bitmap != output) {
            bitmap.recycle();
        }
        return output;
    }
}
