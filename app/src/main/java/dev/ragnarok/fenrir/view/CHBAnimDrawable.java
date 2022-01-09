package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.Constants;

public final class CHBAnimDrawable extends BitmapDrawable implements Animatable {
    // Only accessed from main thread.
    private static final float FADE_DURATION = 400f; //ms
    private final long startTimeMillis;
    private boolean animatingFade;
    private boolean animating;
    private int alpha = 0xFF;
    private View currentParentView;
    private float targetSaturation = 1f;

    public CHBAnimDrawable(Context context, @NonNull Bitmap bitmap, boolean animatingFade) {
        super(context.getResources(), bitmap);
        this.animatingFade = animatingFade;
        animating = false;
        startTimeMillis = SystemClock.uptimeMillis();
    }

    public static void setBitmap(@NonNull ImageView target, Context context, @NonNull Bitmap bitmap, boolean start, boolean fadeIn) {
        Drawable placeholder = target.getDrawable();
        if (placeholder instanceof Animatable) {
            ((Animatable) placeholder).stop();
        }
        CHBAnimDrawable drawable = new CHBAnimDrawable(context, bitmap, fadeIn);
        drawable.setCurrentParentView(target);
        if (start) {
            drawable.start();
        }
        target.setImageDrawable(drawable);
    }

    public void setCurrentParentView(View view) {
        currentParentView = view;
    }

    protected void invalidateInternal() {
        if (currentParentView != null) {
            currentParentView.invalidate();
        }
        if (getCallback() != null) {
            invalidateSelf();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!animatingFade) {
            if (animating) {
                targetSaturation -= 0.009f;
                if (targetSaturation <= 0) {
                    animating = false;
                }
                try {
                    ColorMatrix cm = new ColorMatrix();
                    cm.setSaturation(targetSaturation);
                    getPaint().setColorFilter(new ColorMatrixColorFilter(cm));
                    super.draw(canvas);
                    invalidateInternal();
                } catch (Exception e) {
                    if (Constants.IS_DEBUG) {
                        e.printStackTrace();
                    }
                }
            } else {
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(targetSaturation);
                getPaint().setColorFilter(new ColorMatrixColorFilter(cm));
                super.draw(canvas);
            }
        } else {
            float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                animatingFade = false;
                try {
                    super.draw(canvas);
                    if (animating) {
                        invalidateInternal();
                    }
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

    @Override
    public void start() {
        if (!animating) {
            animating = true;
            invalidateInternal();
        }
    }

    @Override
    public void stop() {
        if (animating) {
            animating = false;
            invalidateInternal();
        }
    }

    @Override
    public boolean isRunning() {
        return animating;
    }
}
