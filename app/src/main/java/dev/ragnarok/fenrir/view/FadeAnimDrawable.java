package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.model.PlayerCoverBackgroundSettings;
import dev.ragnarok.fenrir.settings.Settings;

public final class FadeAnimDrawable extends BitmapDrawable implements Animatable {
    // Only accessed from main thread.
    private static final float FADE_DURATION = 400f; //ms
    private final long startTimeMillis;
    private final PlayerCoverBackgroundSettings settings;
    private boolean animatingFade;
    private boolean animating;
    private int alpha = 0xFF;
    private View currentParentView;
    private float targetRotation;

    public FadeAnimDrawable(Context context, @NonNull Bitmap bitmap) {
        super(context.getResources(), bitmap);
        animatingFade = true;
        animating = false;
        startTimeMillis = SystemClock.uptimeMillis();
        settings = Settings.get().other().getPlayerCoverBackgroundSettings();
        targetRotation = 0;
    }

    public static void setBitmap(@NonNull ImageView target, Context context, @NonNull Bitmap bitmap, boolean start) {
        PlayerCoverBackgroundSettings settings = Settings.get().other().getPlayerCoverBackgroundSettings();
        if (!settings.enabled_rotation || settings.rotation_speed <= 0) {
            FadeDrawable.setBitmap(target, context, bitmap);
            return;
        }
        Drawable placeholder = target.getDrawable();
        if (placeholder instanceof Animatable) {
            ((Animatable) placeholder).stop();
        }
        FadeAnimDrawable drawable = new FadeAnimDrawable(context, bitmap);
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
        canvas.scale(settings.zoom, settings.zoom, (float) getBitmap().getWidth() / 2, (float) getBitmap().getHeight() / 2);
        if (!animatingFade) {
            if (animating) {
                if (!settings.invert_rotation) {
                    targetRotation += settings.rotation_speed;
                    if (targetRotation > 360) {
                        targetRotation = 0;
                    }
                } else {
                    targetRotation -= settings.rotation_speed;
                    if (targetRotation < -360) {
                        targetRotation = 0;
                    }
                }

                canvas.rotate((float) Math.toDegrees(targetRotation), (float) getBitmap().getWidth() / 2, (float) getBitmap().getHeight() / 2);
                try {
                    super.draw(canvas);
                    invalidateInternal();
                } catch (Exception e) {
                    if (Constants.IS_DEBUG) {
                        e.printStackTrace();
                    }
                }
            } else {
                canvas.rotate((float) Math.toDegrees(targetRotation), (float) getBitmap().getWidth() / 2, (float) getBitmap().getHeight() / 2);
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
