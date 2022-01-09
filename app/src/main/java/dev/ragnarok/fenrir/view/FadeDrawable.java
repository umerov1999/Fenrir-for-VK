package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.Constants;

public final class FadeDrawable extends BitmapDrawable {
    // Only accessed from main thread.
    private static final float FADE_DURATION = 200f; //ms
    private final long startTimeMillis;
    private boolean animating;
    private int alpha = 0xFF;

    public FadeDrawable(Context context, @NonNull Bitmap bitmap) {
        super(context.getResources(), bitmap);
        animating = true;
        startTimeMillis = SystemClock.uptimeMillis();
    }

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied bitmap
     * image.
     */
    public static void setBitmap(@NonNull ImageView target, Context context, @NonNull Bitmap bitmap) {
        Drawable placeholder = target.getDrawable();
        if (placeholder instanceof Animatable) {
            ((Animatable) placeholder).stop();
        }
        FadeDrawable drawable =
                new FadeDrawable(context, bitmap);
        target.setImageDrawable(drawable);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!animating) {
            try {
                super.draw(canvas);
            } catch (Exception e) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                animating = false;
                try {
                    super.draw(canvas);
                } catch (Exception e) {
                    if (Constants.IS_DEBUG) {
                        e.printStackTrace();
                    }
                }
            } else {
                // setAlpha will call invalidateSelf and drive the animation.
                int partialAlpha = (int) (alpha * normalized);
                super.setAlpha(partialAlpha);
                try {
                    super.draw(canvas);
                } catch (Exception e) {
                    if (Constants.IS_DEBUG) {
                        e.printStackTrace();
                    }
                }
                super.setAlpha(alpha);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        super.setAlpha(alpha);
    }
}
