package dev.ragnarok.fenrir.view.media;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Utils;

public class MediaActionDrawable extends Drawable {

    public static final int ICON_PLAY = 0;
    public static final int ICON_PAUSE = 1;
    public static final int ICON_DOWNLOAD = 2;
    public static final int ICON_CANCEL = 3;
    public static final int ICON_NONE = 4;
    public static final int ICON_CHECK = 6;
    public static final int ICON_SECRETCHECK = 7;
    public static final int ICON_EMPTY = 8;
    public static final int ICON_CANCEL_NOPROFRESS = 9;
    public static final int ICON_CANCEL_PERCENT = 10;
    public static final int ICON_CANCEL_FILL = 11;
    private final static float DOWNLOAD_TO_CANCEL_STAGE1 = 0.5f;
    private final static float DOWNLOAD_TO_CANCEL_STAGE2 = 0.2f;
    private final static float DOWNLOAD_TO_CANCEL_STAGE3 = 0.3f;
    private final static float CANCEL_TO_CHECK_STAGE1 = 0.5f;
    private final static float CANCEL_TO_CHECK_STAGE2 = 0.5f;
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final DecelerateInterpolator interpolator = new DecelerateInterpolator();
    protected ColorFilter mColorFilter;
    protected ColorStateList mTintList;
    protected PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;
    protected PorterDuffColorFilter mTintFilter;
    private float scale = 1.0f;
    private boolean isMini;
    private float transitionAnimationTime = 400.0f;
    private int lastPercent = -1;
    private String percentString;
    private int percentStringWidth;
    private float overrideAlpha = 1.0f;
    private int currentIcon;
    private int nextIcon;
    private float transitionProgress = 1.0f;
    private float savedTransitionProgress;
    private long lastAnimationTime;
    private boolean animatingTransition;
    private float downloadRadOffset;
    private float downloadProgress;
    private float animatedDownloadProgress;
    private float downloadProgressAnimationStart;
    private float downloadProgressTime;
    private MediaActionDrawableDelegate delegate;

    public MediaActionDrawable() {
        paint.setColor(0xffffffff);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Utils.dp(3));
        paint.setStyle(Paint.Style.STROKE);

        paint3.setColor(0xffffffff);

        textPaint.setTextSize(Utils.dp(13));
        textPaint.setColor(0xffffffff);

        paint2.setColor(0xffffffff);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    public void setOverrideAlpha(float alpha) {
        overrideAlpha = alpha;
    }

    private boolean updateTintFilter() {

        if (mTintList == null || mTintMode == null) {
            boolean hadTintFilter = mTintFilter != null;
            mTintFilter = null;
            return hadTintFilter;
        }

        int tintColor = mTintList.getColorForState(getState(), Color.TRANSPARENT);
        mTintFilter = new PorterDuffColorFilter(tintColor, mTintMode);
        return true;
    }

    protected ColorFilter getColorFilterForDrawing() {
        return mColorFilter != null ? mColorFilter : mTintFilter;
    }

    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mColorFilter = colorFilter;
        invalidateSelf();
    }

    @Override
    public void setTint(@ColorInt int tintColor) {
        setTintList(ColorStateList.valueOf(tintColor));
    }

    @Override
    public void setTintList(@Nullable ColorStateList tint) {
        mTintList = tint;
        if (updateTintFilter()) {
            invalidateSelf();
        }
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        mTintMode = tintMode;
        if (updateTintFilter()) {
            invalidateSelf();
        }
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return updateTintFilter();
    }

    @Override
    public boolean isStateful() {
        return mTintList != null && mTintList.isStateful();
    }

    public void setBackColor(int value) {
        backPaint.setColor(value | 0xff000000);
    }

    public void setMini(boolean value) {
        isMini = value;
        paint.setStrokeWidth(Utils.dp(isMini ? 2 : 3));
    }

    @Deprecated
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    public void setDelegate(MediaActionDrawableDelegate mediaActionDrawableDelegate) {
        delegate = mediaActionDrawableDelegate;
    }

    public boolean setIcon(int icon, boolean animated) {
        if (currentIcon == icon && nextIcon != icon) {
            currentIcon = nextIcon;
            transitionProgress = 1.0f;
        }
        if (animated) {
            if (currentIcon == icon || nextIcon == icon) {
                return false;
            }
            if (currentIcon == ICON_PLAY && icon == ICON_PAUSE || currentIcon == ICON_PAUSE && icon == ICON_PLAY) {
                transitionAnimationTime = 300.0f;
            } else if (currentIcon == ICON_DOWNLOAD && (icon == ICON_CANCEL || icon == ICON_CANCEL_FILL)) {
                transitionAnimationTime = 400.0f;
            } else if (currentIcon != ICON_NONE && icon == ICON_CHECK) {
                transitionAnimationTime = 360.0f;
            } else if (currentIcon == ICON_NONE && icon == ICON_CANCEL_FILL || currentIcon == ICON_CANCEL_FILL && icon == ICON_NONE) {
                transitionAnimationTime = 160.0f;
            } else {
                transitionAnimationTime = 220.0f;
            }
            if (animatingTransition) {
                currentIcon = nextIcon;
            }
            animatingTransition = true;
            nextIcon = icon;
            savedTransitionProgress = transitionProgress;
            transitionProgress = 0.0f;
        } else {
            if (currentIcon == icon) {
                return false;
            }
            animatingTransition = false;
            currentIcon = nextIcon = icon;
            savedTransitionProgress = transitionProgress;
            transitionProgress = 1.0f;
        }
        if (icon == ICON_CANCEL || icon == ICON_CANCEL_FILL) {
            downloadRadOffset = 112;
            animatedDownloadProgress = 0.0f;
            downloadProgressAnimationStart = 0.0f;
            downloadProgressTime = 0.0f;
        }
        invalidateSelf();
        return true;
    }

    public int getCurrentIcon() {
        return nextIcon;
    }

    public int getPreviousIcon() {
        return currentIcon;
    }

    public void setProgress(float value, boolean animated) {
        if (!animated) {
            animatedDownloadProgress = value;
            downloadProgressAnimationStart = value;
        } else {
            if (animatedDownloadProgress > value) {
                animatedDownloadProgress = value;
            }
            downloadProgressAnimationStart = animatedDownloadProgress;
        }
        downloadProgress = value;
        downloadProgressTime = 0;
        invalidateSelf();
    }

    public float getProgress() {
        return downloadProgress;
    }

    private float getCircleValue(float value) {
        while (value > 360) {
            value -= 360;
        }
        return value;
    }

    public float getProgressAlpha() {
        return 1.0f - transitionProgress;
    }

    public float getTransitionProgress() {
        return animatingTransition ? transitionProgress : 1.0f;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        scale = (right - left) / (float) getIntrinsicWidth();
        if (scale < 0.7f) {
            paint.setStrokeWidth(Utils.dp(2));
        }
    }

    @Override
    public void invalidateSelf() {
        super.invalidateSelf();
        if (delegate != null) {
            delegate.invalidate();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        android.graphics.Rect bounds = getBounds();
        paint.setShader(null);
        paint2.setShader(null);
        paint3.setShader(null);

        ColorFilter rs = getColorFilterForDrawing();

        paint.setColorFilter(rs);
        paint2.setColorFilter(rs);
        paint3.setColorFilter(rs);
        textPaint.setColorFilter(rs);

        int cx = bounds.centerX();
        int cy = bounds.centerY();

        int saveCount = 0;

        if (nextIcon == ICON_NONE) {
            if (currentIcon != ICON_CANCEL && currentIcon != ICON_CANCEL_FILL) {
                saveCount = canvas.save();
                float progress = 1.0f - transitionProgress;
                canvas.scale(progress, progress, cx, cy);
            }
        } else if ((nextIcon == ICON_CHECK || nextIcon == ICON_EMPTY) && currentIcon == ICON_NONE) {
            saveCount = canvas.save();
            canvas.scale(transitionProgress, transitionProgress, cx, cy);
        }

        if (currentIcon == ICON_DOWNLOAD || nextIcon == ICON_DOWNLOAD) {
            float yStart = cy - Utils.dp(9) * scale;
            float yEnd = cy + Utils.dp(9) * scale;
            float yStart2;
            float yEnd2 = cy + Utils.dp(12) * scale;

            float transition;
            if ((currentIcon == ICON_CANCEL || currentIcon == ICON_CANCEL_FILL) && nextIcon == ICON_DOWNLOAD) {
                paint.setAlpha((int) (255 * Math.min(1.0f, transitionProgress / 0.5f)));
                transition = transitionProgress;
                yStart2 = cy + Utils.dp(12) * scale;
            } else {
                if (nextIcon != ICON_CANCEL && nextIcon != ICON_CANCEL_FILL && nextIcon != ICON_DOWNLOAD) {
                    paint.setAlpha((int) (255 * Math.min(1.0f, savedTransitionProgress / 0.5f) * (1.0f - transitionProgress)));
                    transition = savedTransitionProgress;
                } else {
                    paint.setAlpha(255);
                    transition = transitionProgress;
                }
                yStart2 = cy + Utils.dp(1) * scale;
            }

            float y1, y2, x1, x2, y3;
            if (animatingTransition) {
                float progress = transition;
                if (nextIcon == ICON_DOWNLOAD || progress <= DOWNLOAD_TO_CANCEL_STAGE1) {
                    float currentProgress;
                    float currentBackProgress;
                    if (nextIcon == ICON_DOWNLOAD) {
                        currentBackProgress = transition;
                        currentProgress = 1.0f - currentBackProgress;
                    } else {
                        currentProgress = transition / DOWNLOAD_TO_CANCEL_STAGE1;
                        currentBackProgress = 1.0f - currentProgress;
                    }
                    y1 = yStart + (yStart2 - yStart) * currentProgress;
                    y2 = yEnd + (yEnd2 - yEnd) * currentProgress;
                    x1 = cx - Utils.dp(8) * currentBackProgress * scale;
                    x2 = cx + Utils.dp(8) * currentBackProgress * scale;
                    y3 = y2 - Utils.dp(8) * currentBackProgress * scale;
                } else {
                    float currentProgress;
                    float currentProgress2;
                    float currentProgress3;
                    float d = Utils.dp(13) * scale * scale + (isMini ? Utils.dp(2) : 0);

                    progress -= DOWNLOAD_TO_CANCEL_STAGE1;
                    currentProgress3 = progress / (DOWNLOAD_TO_CANCEL_STAGE2 + DOWNLOAD_TO_CANCEL_STAGE3);
                    if (progress > DOWNLOAD_TO_CANCEL_STAGE2) {
                        progress -= DOWNLOAD_TO_CANCEL_STAGE2;
                        currentProgress = 1.0f;
                        currentProgress2 = progress / DOWNLOAD_TO_CANCEL_STAGE3;
                    } else {
                        currentProgress = progress / DOWNLOAD_TO_CANCEL_STAGE2;
                        currentProgress2 = 0.0f;
                    }
                    rect.set(cx - d, yEnd2 - d / 2, cx, yEnd2 + d / 2);
                    float start = 100 * currentProgress2;
                    canvas.drawArc(rect, start, 104 * currentProgress3 - start, false, paint);

                    y1 = yStart2 + (yEnd2 - yStart2) * currentProgress;
                    y2 = y3 = yEnd2;
                    x1 = x2 = cx;

                    if (currentProgress2 > 0) {
                        float rotation;
                        if (nextIcon == ICON_CANCEL_FILL) {
                            rotation = 0;
                        } else {
                            rotation = -45 * (1.0f - currentProgress2);
                        }
                        d = Utils.dp(7) * currentProgress2 * scale;
                        int alpha = (int) (255 * currentProgress2);
                        if (nextIcon != ICON_CANCEL && nextIcon != ICON_CANCEL_FILL && nextIcon != ICON_DOWNLOAD) {
                            float backProgress = (1.0f - Math.min(1.0f, transitionProgress / 0.5f));
                            //d *= backProgress;
                            alpha *= backProgress;
                        }

                        if (rotation != 0) {
                            canvas.save();
                            canvas.rotate(rotation, cx, cy);
                        }
                        if (alpha != 0) {
                            paint.setAlpha(alpha);
                            if (nextIcon == ICON_CANCEL_FILL) {
                                paint3.setAlpha(alpha);
                                rect.set(cx - Utils.dp(3.5f), cy - Utils.dp(3.5f), cx + Utils.dp(3.5f), cy + Utils.dp(3.5f));
                                canvas.drawRoundRect(rect, Utils.dp(2), Utils.dp(2), paint3);

                                paint.setAlpha((int) (alpha * 0.15f));
                                int diff = Utils.dp(isMini ? 2 : 4);
                                rect.set(bounds.left + diff, bounds.top + diff, bounds.right - diff, bounds.bottom - diff);
                                canvas.drawArc(rect, 0, 360, false, paint);
                                paint.setAlpha(alpha);
                            } else {
                                canvas.drawLine(cx - d, cy - d, cx + d, cy + d, paint);
                                canvas.drawLine(cx + d, cy - d, cx - d, cy + d, paint);
                            }
                        }
                        if (rotation != 0) {
                            canvas.restore();
                        }
                    }
                }
            } else {
                y1 = yStart;
                y2 = yEnd;
                x1 = cx - Utils.dp(8) * scale;
                x2 = cx + Utils.dp(8) * scale;
                y3 = y2 - Utils.dp(8) * scale;
            }
            if (y1 != y2) {
                canvas.drawLine(cx, y1, cx, y2, paint);
            }
            if (x1 != cx) {
                canvas.drawLine(x1, y3, cx, y2, paint);
                canvas.drawLine(x2, y3, cx, y2, paint);
            }
        }

        if (currentIcon == ICON_CANCEL || currentIcon == ICON_CANCEL_FILL || currentIcon == ICON_NONE && (nextIcon == ICON_CANCEL_FILL || nextIcon == ICON_CANCEL)) {
            float d;
            float rotation;
            float iconScale = 1.0f;
            float iconScaleX = 0;
            float iconScaleY = 0;
            int alpha;
            if (nextIcon == ICON_DOWNLOAD) {
                if (transitionProgress <= DOWNLOAD_TO_CANCEL_STAGE3 + DOWNLOAD_TO_CANCEL_STAGE2) {
                    float progress = transitionProgress / (DOWNLOAD_TO_CANCEL_STAGE3 + DOWNLOAD_TO_CANCEL_STAGE2);
                    float backProgress = 1.0f - progress;
                    d = Utils.dp(7) * backProgress * scale;
                    alpha = (int) (255 * backProgress);
                } else {
                    d = 0;
                    alpha = 0;
                }
                rotation = 0;
            } else if (nextIcon == ICON_PLAY || nextIcon == ICON_PAUSE || nextIcon == ICON_SECRETCHECK || nextIcon == ICON_CHECK) {
                float progress;
                float backProgress;
                if (nextIcon == ICON_CHECK) {
                    progress = Math.min(1.0f, transitionProgress / CANCEL_TO_CHECK_STAGE1);
                } else {
                    progress = transitionProgress;
                }
                backProgress = 1.0f - progress;
                rotation = 45 * progress;
                d = Utils.dp(7) * backProgress * scale;
                alpha = (int) (255 * Math.min(1.0f, backProgress * 2.0f));
            } else if (nextIcon == ICON_NONE) {
                float progress = transitionProgress;
                float backProgress = 1.0f - progress;
                d = Utils.dp(7) * scale;
                alpha = (int) (255 * backProgress);
                if (currentIcon == ICON_CANCEL_FILL) {
                    rotation = 0;
                    iconScale = backProgress;
                    iconScaleX = bounds.left;
                    iconScaleY = bounds.top;
                } else {
                    rotation = 45 * progress;
                    iconScale = 1.0f;
                    iconScaleX = bounds.centerX();
                    iconScaleY = bounds.centerY();
                }
            } else if (nextIcon == ICON_CANCEL_FILL || nextIcon == ICON_CANCEL) {
                float progress = transitionProgress;
                float backProgress = 1.0f - progress;
                if (currentIcon == ICON_NONE) {
                    rotation = 0;
                    iconScale = progress;
                } else {
                    rotation = 45 * backProgress;
                    iconScale = 1.0f;
                }
                d = Utils.dp(7) * scale;
                alpha = (int) (255 * progress);
                if (nextIcon == ICON_CANCEL_FILL) {
                    iconScaleX = bounds.left;
                    iconScaleY = bounds.top;
                } else {
                    iconScaleX = bounds.centerX();
                    iconScaleY = bounds.centerY();
                }
            } else {
                rotation = 0;
                d = Utils.dp(7) * scale;
                alpha = 255;
            }
            if (iconScale != 1.0f) {
                canvas.save();
                canvas.scale(iconScale, iconScale, iconScaleX, iconScaleY);
            }
            if (rotation != 0) {
                canvas.save();
                canvas.rotate(rotation, cx, cy);
            }
            if (alpha != 0) {
                paint.setAlpha((int) (alpha * overrideAlpha));
                if (currentIcon == ICON_CANCEL_FILL || nextIcon == ICON_CANCEL_FILL) {
                    paint3.setAlpha((int) (alpha * overrideAlpha));
                    rect.set(cx - Utils.dp(3.5f), cy - Utils.dp(3.5f), cx + Utils.dp(3.5f), cy + Utils.dp(3.5f));
                    canvas.drawRoundRect(rect, Utils.dp(2), Utils.dp(2), paint3);
                } else {
                    canvas.drawLine(cx - d, cy - d, cx + d, cy + d, paint);
                    canvas.drawLine(cx + d, cy - d, cx - d, cy + d, paint);
                }
            }
            if (rotation != 0) {
                canvas.restore();
            }
            if ((currentIcon == ICON_CANCEL || currentIcon == ICON_CANCEL_FILL || currentIcon == ICON_NONE && (nextIcon == ICON_CANCEL_FILL || nextIcon == ICON_CANCEL)) && alpha != 0) {
                float rad = Math.max(4, 360 * animatedDownloadProgress);
                int diff = Utils.dp(isMini ? 2 : 4);
                rect.set(bounds.left + diff, bounds.top + diff, bounds.right - diff, bounds.bottom - diff);

                if (currentIcon == ICON_CANCEL_FILL || currentIcon == ICON_NONE && (nextIcon == ICON_CANCEL_FILL || nextIcon == ICON_CANCEL)) {
                    paint.setAlpha((int) (alpha * 0.15f * overrideAlpha));
                    canvas.drawArc(rect, 0, 360, false, paint);
                    paint.setAlpha(alpha);
                }
                canvas.drawArc(rect, downloadRadOffset, rad, false, paint);
            }
            if (iconScale != 1.0f) {
                canvas.restore();
            }
        } else if (currentIcon == ICON_EMPTY || nextIcon == ICON_EMPTY || currentIcon == ICON_CANCEL_PERCENT) {
            int alpha;
            if (nextIcon == ICON_NONE || nextIcon == ICON_CHECK) {
                float progress = transitionProgress;
                float backProgress = 1.0f - progress;
                alpha = (int) (255 * backProgress);
            } else {
                alpha = 255;
            }

            if (alpha != 0) {
                paint.setAlpha((int) (alpha * overrideAlpha));
                float rad = Math.max(4, 360 * animatedDownloadProgress);
                int diff = Utils.dp(isMini ? 2 : 4);
                rect.set(bounds.left + diff, bounds.top + diff, bounds.right - diff, bounds.bottom - diff);
                canvas.drawArc(rect, downloadRadOffset, rad, false, paint);
            }
        }

        float drawableScale;
        if (currentIcon == nextIcon) {
            drawableScale = 1.0f;
        } else if (currentIcon == ICON_NONE) {
            drawableScale = transitionProgress;
        } else {
            drawableScale = Math.min(1.0f, transitionProgress / 0.5f);
        }


        if (currentIcon == ICON_SECRETCHECK || nextIcon == ICON_SECRETCHECK) {
            paint.setAlpha(currentIcon == nextIcon ? 255 : (int) (transitionProgress * 255));
            int y = cy + Utils.dp(7);
            int x = cx - Utils.dp(3);
            if (currentIcon != nextIcon) {
                canvas.save();
                canvas.scale(transitionProgress, transitionProgress, cx, cy);
            }
            canvas.drawLine(x - Utils.dp(6), y - Utils.dp(6), x, y, paint);
            canvas.drawLine(x, y, x + Utils.dp(12), y - Utils.dp(12), paint);
            if (currentIcon != nextIcon) {
                canvas.restore();
            }
        }
        if (currentIcon == ICON_CANCEL_NOPROFRESS || nextIcon == ICON_CANCEL_NOPROFRESS) {
            float transition;
            if (currentIcon == nextIcon) {
                transition = 1.0f;
            } else if (nextIcon == ICON_CANCEL_PERCENT) {
                transition = transitionProgress;
            } else {
                transition = 1.0f - transitionProgress;
            }

            paint.setAlpha(currentIcon == nextIcon ? 255 : (int) (transition * 255));
            if (currentIcon != nextIcon) {
                canvas.save();
                canvas.scale(transition, transition, cx, cy);
            }

            float d = Utils.dp(7) * scale;
            canvas.drawLine(cx - d, cy - d, cx + d, cy + d, paint);
            canvas.drawLine(cx + d, cy - d, cx - d, cy + d, paint);

            if (currentIcon != nextIcon) {
                canvas.restore();
            }
        }
        if (currentIcon == ICON_CANCEL_PERCENT || nextIcon == ICON_CANCEL_PERCENT) {
            float transition;
            if (currentIcon == nextIcon) {
                transition = 1.0f;
            } else if (nextIcon == ICON_CANCEL_PERCENT) {
                transition = transitionProgress;
            } else {
                transition = 1.0f - transitionProgress;
            }

            textPaint.setAlpha((int) (transition * 255));
            int y = cy + Utils.dp(5);
            int x = cx - percentStringWidth / 2;
            if (currentIcon != nextIcon) {
                canvas.save();
                canvas.scale(transition, transition, cx, cy);
            }
            int newPercent = (int) (animatedDownloadProgress * 100);
            if (percentString == null || newPercent != lastPercent) {
                lastPercent = newPercent;
                percentString = String.format(Locale.getDefault(), "%d%%", lastPercent);
                percentStringWidth = (int) Math.ceil(textPaint.measureText(percentString));
            }
            canvas.drawText(percentString, x, y, textPaint);
            if (currentIcon != nextIcon) {
                canvas.restore();
            }
        }
        if (currentIcon == ICON_PLAY || currentIcon == ICON_PAUSE || nextIcon == ICON_PLAY || nextIcon == ICON_PAUSE) {
            float p;
            if (currentIcon == ICON_PLAY && nextIcon == ICON_PAUSE || currentIcon == ICON_PAUSE && nextIcon == ICON_PLAY) {
                if (animatingTransition) {
                    if (nextIcon == ICON_PLAY) {
                        p = 1.0f - transitionProgress;
                    } else {
                        p = transitionProgress;
                    }
                } else {
                    p = nextIcon == ICON_PAUSE ? 1.0f : 0.0f;
                }
            } else {
                p = currentIcon == ICON_PAUSE ? 1.0f : 0.0f;
            }

            if (nextIcon != ICON_PLAY && nextIcon != ICON_PAUSE || currentIcon != ICON_PLAY && currentIcon != ICON_PAUSE) {
                if (nextIcon == ICON_NONE) {
                    paint2.setAlpha((int) (255 * (1.0f - transitionProgress)));
                } else {
                    paint2.setAlpha((int) (transitionProgress * 255));
                }
            } else {
                paint2.setAlpha(255);
            }

            canvas.save();
            canvas.translate(bounds.centerX() + Utils.dp(1) * (1.0f - p), bounds.centerY());
            float ms = 500.0f * p;
            float rotation = currentIcon == ICON_PAUSE ? 90 : 0;
            if (currentIcon == ICON_PLAY && nextIcon == ICON_PAUSE) {
                if (ms < 384) {
                    rotation = 95 * CubicBezierInterpolator.EASE_BOTH.getInterpolation(ms / 384);
                } else if (ms < 484) {
                    rotation = 95 - 5 * CubicBezierInterpolator.EASE_BOTH.getInterpolation((ms - 384) / 100.0f);
                } else {
                    rotation = 90;
                }
                ms += 100;
            } else if (currentIcon == ICON_PAUSE && nextIcon == ICON_PLAY) {
                if (ms < 100) {
                    rotation = -5 * CubicBezierInterpolator.EASE_BOTH.getInterpolation(ms / 100.0f);
                } else if (ms < 484) {
                    rotation = -5 + 95 * CubicBezierInterpolator.EASE_BOTH.getInterpolation((ms - 100) / 384);
                }
            }
            canvas.rotate(rotation);
            if (currentIcon != ICON_PLAY && currentIcon != ICON_PAUSE) {
                canvas.scale(drawableScale, drawableScale);
            }
            CurrentTheme.getPlayPauseAnimator().draw(canvas, paint2, ms);
            canvas.scale(1.0f, -1.0f);
            CurrentTheme.getPlayPauseAnimator().draw(canvas, paint2, ms);

            canvas.restore();
        }
        if (currentIcon == ICON_CHECK || nextIcon == ICON_CHECK) {
            float progress1;
            float progress2;
            if (currentIcon != ICON_CHECK) {
                if (transitionProgress > CANCEL_TO_CHECK_STAGE1) {
                    float progress = (transitionProgress - CANCEL_TO_CHECK_STAGE1) / CANCEL_TO_CHECK_STAGE2;
                    progress1 = 1.0f - Math.min(1.0f, progress / 0.5f);
                    progress2 = progress > 0.5f ? ((progress - 0.5f) / 0.5f) : 0.0f;
                } else {
                    progress1 = 1.0f;
                    progress2 = 0.0f;
                }
            } else {
                progress1 = 0.0f;
                progress2 = 1.0f;
            }
            int y = cy + Utils.dp(7);
            int x = cx - Utils.dp(3);
            paint.setAlpha(255);
            if (progress1 < 1) {
                canvas.drawLine(x - Utils.dp(6), y - Utils.dp(6), x - Utils.dp(6) * progress1, y - Utils.dp(6) * progress1, paint);
            }
            if (progress2 > 0) {
                canvas.drawLine(x, y, x + Utils.dp(12) * progress2, y - Utils.dp(12) * progress2, paint);
            }
        }

        long newTime = System.currentTimeMillis();
        long dt = newTime - lastAnimationTime;
        if (dt > 17) {
            dt = 17;
        }
        lastAnimationTime = newTime;

        if (currentIcon == ICON_CANCEL || currentIcon == ICON_CANCEL_FILL || currentIcon == ICON_NONE && nextIcon == ICON_CANCEL_FILL || currentIcon == ICON_EMPTY || currentIcon == ICON_CANCEL_PERCENT) {
            downloadRadOffset += 360 * dt / 2500.0f;
            downloadRadOffset = getCircleValue(downloadRadOffset);

            if (nextIcon != ICON_DOWNLOAD) {
                float progressDiff = downloadProgress - downloadProgressAnimationStart;
                if (progressDiff > 0) {
                    downloadProgressTime += dt;
                    if (downloadProgressTime >= 200.0f) {
                        animatedDownloadProgress = downloadProgress;
                        downloadProgressAnimationStart = downloadProgress;
                        downloadProgressTime = 0;
                    } else {
                        animatedDownloadProgress = downloadProgressAnimationStart + progressDiff * interpolator.getInterpolation(downloadProgressTime / 200.0f);
                    }
                }
            }
            invalidateSelf();
        }

        if (animatingTransition) {
            if (transitionProgress < 1.0f) {
                transitionProgress += dt / transitionAnimationTime;
                if (transitionProgress >= 1.0f) {
                    currentIcon = nextIcon;
                    transitionProgress = 1.0f;
                    animatingTransition = false;
                }
                invalidateSelf();
            }
        }
        if (saveCount >= 1) {
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return Utils.dp(48);
    }

    @Override
    public int getIntrinsicHeight() {
        return Utils.dp(48);
    }

    @Override
    public int getMinimumWidth() {
        return Utils.dp(48);
    }

    @Override
    public int getMinimumHeight() {
        return Utils.dp(48);
    }

    public interface MediaActionDrawableDelegate {
        void invalidate();
    }
}