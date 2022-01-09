/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso3;

import static android.graphics.Color.WHITE;
import static com.squareup.picasso3.Picasso.LoadedFrom.MEMORY;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.squareup.picasso3.RequestHandler.Result;

final class PicassoDrawable extends BitmapDrawable {
    // Only accessed from main thread.
    private static final Paint DEBUG_PAINT = new Paint();
    private static final float FADE_DURATION = 200f; //ms
    private final boolean debugging;
    private final float density;
    private final Picasso.LoadedFrom loadedFrom;
    @Nullable
    Drawable placeholder;
    long startTimeMillis;
    boolean animating;
    int alpha = 0xFF;

    PicassoDrawable(Context context, Bitmap bitmap, @Nullable Drawable placeholder,
                    Picasso.LoadedFrom loadedFrom, boolean noFade, boolean debugging) {
        super(context.getResources(), bitmap);

        this.debugging = debugging;
        density = context.getResources().getDisplayMetrics().density;

        this.loadedFrom = loadedFrom;

        boolean fade = loadedFrom != MEMORY && !noFade;
        if (fade) {
            this.placeholder = placeholder;
            animating = true;
            startTimeMillis = SystemClock.uptimeMillis();
        }
    }

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied bitmap
     * image.
     */
    static void setResult(ImageView target, Context context, Result result, boolean noFade,
                          boolean debugging) {
        Drawable placeholder = target.getDrawable();
        if (placeholder instanceof Animatable) {
            ((Animatable) placeholder).stop();
        }

        if (result instanceof Result.Bitmap) {
            Bitmap bitmap = ((Result.Bitmap) result).getBitmap();
            Picasso.LoadedFrom loadedFrom = result.loadedFrom;
            PicassoDrawable drawable =
                    new PicassoDrawable(context, bitmap, placeholder, loadedFrom, noFade, debugging);
            target.setImageDrawable(drawable);
        } else {
            Drawable drawable = ((Result.Drawable) result).getDrawable();
            target.setImageDrawable(drawable);
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        }
    }

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied
     * placeholder image.
     */
    static void setPlaceholder(ImageView target, @Nullable Drawable placeholderDrawable) {
        target.setImageDrawable(placeholderDrawable);
        if (target.getDrawable() instanceof Animatable) {
            ((Animatable) target.getDrawable()).start();
        }
    }

    private static Path getTrianglePath(int x1, int y1, int width) {
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x1 + width, y1);
        path.lineTo(x1, y1 + width);

        return path;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!animating) {
            try {
                super.draw(canvas);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                animating = false;
                placeholder = null;
                try {
                    super.draw(canvas);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (placeholder != null) {
                    try {
                        placeholder.draw(canvas);
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }

                // setAlpha will call invalidateSelf and drive the animation.
                int partialAlpha = (int) (alpha * normalized);
                super.setAlpha(partialAlpha);
                try {
                    super.draw(canvas);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                super.setAlpha(alpha);
            }
        }

        if (debugging) {
            drawDebugIndicator(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        if (placeholder != null) {
            placeholder.setAlpha(alpha);
        }
        super.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (placeholder != null) {
            placeholder.setColorFilter(cf);
        }
        super.setColorFilter(cf);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (placeholder != null) {
            placeholder.setBounds(bounds);
        }
        super.onBoundsChange(bounds);
    }

    private void drawDebugIndicator(Canvas canvas) {
        DEBUG_PAINT.setColor(WHITE);
        Path path = getTrianglePath(0, 0, (int) (16 * density));
        canvas.drawPath(path, DEBUG_PAINT);

        DEBUG_PAINT.setColor(loadedFrom.debugColor);
        path = getTrianglePath(0, 0, (int) (15 * density));
        canvas.drawPath(path, DEBUG_PAINT);
    }
}
