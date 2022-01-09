package dev.ragnarok.fenrir.colorpicker.builder;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;

public class PaintBuilder {
    public static PaintHolder newPaint() {
        return new PaintHolder();
    }

    public static Shader createAlphaPatternShader(int size) {
        size /= 2;
        size = Math.max(8, size * 2);
        return new BitmapShader(createAlphaBackgroundPattern(size), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    }

    private static Bitmap createAlphaBackgroundPattern(int size) {
        Paint alphaPatternPaint = newPaint().build();
        Bitmap bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        int s = Math.round(size / 2f);
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++) {
                if ((i + j) % 2 == 0) alphaPatternPaint.setColor(0xffffffff);
                else alphaPatternPaint.setColor(0xffd0d0d0);
                c.drawRect(i * s, j * s, (i + 1) * s, (j + 1) * s, alphaPatternPaint);
            }
        return bm;
    }

    public static class PaintHolder {
        private final Paint paint;

        private PaintHolder() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        public PaintHolder color(int color) {
            paint.setColor(color);
            return this;
        }

        public PaintHolder antiAlias(boolean flag) {
            paint.setAntiAlias(flag);
            return this;
        }

        public PaintHolder style(Paint.Style style) {
            paint.setStyle(style);
            return this;
        }

        public PaintHolder mode(PorterDuff.Mode mode) {
            paint.setXfermode(new PorterDuffXfermode(mode));
            return this;
        }

        public PaintHolder stroke(float width) {
            paint.setStrokeWidth(width);
            return this;
        }

        public PaintHolder xPerMode(PorterDuff.Mode mode) {
            paint.setXfermode(new PorterDuffXfermode(mode));
            return this;
        }

        public PaintHolder shader(Shader shader) {
            paint.setShader(shader);
            return this;
        }

        public Paint build() {
            return paint;
        }
    }
}
