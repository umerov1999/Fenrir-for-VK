package dev.ragnarok.fenrir.view.natives.animation;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.animation.AnimatedFileDrawable;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnimatedShapeableImageView extends ShapeableImageView {

    private static final ThreadLocal<byte[]> bufferLocal = new ThreadLocal<>();
    private final AnimationNetworkCache cache;
    private final int defaultWidth;
    private final int defaultHeight;
    private AnimatedFileDrawable drawable;
    private boolean attachedToWindow;
    private boolean playing;
    private onDecoderInit decoderCallback;
    private Disposable mDisposable = Disposable.disposed();

    public AnimatedShapeableImageView(Context context) {
        this(context, null);
    }

    public AnimatedShapeableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cache = new AnimationNetworkCache(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedShapeableImageView);
        defaultWidth = (int) a.getDimension(R.styleable.AnimatedShapeableImageView_default_width, 100);
        defaultHeight = (int) a.getDimension(R.styleable.AnimatedShapeableImageView_default_height, 100);
        a.recycle();
    }

    public void setDecoderCallback(onDecoderInit decoderCallback) {
        this.decoderCallback = decoderCallback;
    }

    private void setAnimationByUrlCache(String url, boolean fade) {
        if (!FenrirNative.isNativeLoaded()) {
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
            return;
        }
        File ch = cache.fetch(url);
        if (ch == null) {
            setImageDrawable(null);
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
            return;
        }
        setAnimation(new AnimatedFileDrawable(ch, 0, defaultWidth, defaultHeight, fade, () -> {
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
        }));
        playAnimation();
    }

    private void setAnimationByResCache(@RawRes int res, boolean fade) {
        if (!FenrirNative.isNativeLoaded()) {
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
            return;
        }
        File ch = cache.fetch(res);
        if (ch == null) {
            setImageDrawable(null);
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
            return;
        }
        setAnimation(new AnimatedFileDrawable(ch, 0, defaultWidth, defaultHeight, fade, () -> {
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
        }));
        playAnimation();
    }

    public void fromNet(@NonNull String key, @Nullable String url, OkHttpClient.Builder client) {
        if (!FenrirNative.isNativeLoaded() || url == null || url.isEmpty()) {
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
            return;
        }
        clearAnimationDrawable();
        if (cache.isCachedFile(key)) {
            setAnimationByUrlCache(key, true);
            return;
        }
        mDisposable = Single.create((SingleOnSubscribe<Boolean>) u -> {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.build().newCall(request).execute();
                if (!response.isSuccessful()) {
                    u.onSuccess(false);
                    return;
                }
                InputStream bfr = Objects.requireNonNull(response.body()).byteStream();
                BufferedInputStream input = new BufferedInputStream(bfr);
                cache.writeTempCacheFile(key, input);
                input.close();
                cache.renameTempFile(key);
            } catch (Exception e) {
                u.onSuccess(false);
                return;
            }
            u.onSuccess(true);
        }).compose(RxUtils.applySingleComputationToMainSchedulers()).subscribe(u -> {
            if (u) {
                setAnimationByUrlCache(key, true);
            } else {
                if (nonNull(decoderCallback)) {
                    decoderCallback.onLoaded(false);
                }
            }
        }, RxUtils.ignore());
    }

    public void fromRes(@RawRes int res) {
        if (!FenrirNative.isNativeLoaded()) {
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
            return;
        }
        clearAnimationDrawable();
        if (cache.isCachedRes(res)) {
            setAnimationByResCache(res, true);
            return;
        }
        mDisposable = Single.create((SingleOnSubscribe<Boolean>) u -> {
            try {
                if (!copyRes(res)) {
                    u.onSuccess(false);
                    return;
                }
                cache.renameTempFile(res);
            } catch (Exception e) {
                u.onSuccess(false);
                return;
            }
            u.onSuccess(true);
        }).compose(RxUtils.applySingleComputationToMainSchedulers()).subscribe(u -> {
            if (u) {
                setAnimationByResCache(res, true);
            } else {
                if (nonNull(decoderCallback)) {
                    decoderCallback.onLoaded(false);
                }
            }
        }, RxUtils.ignore());
    }

    private void setAnimation(@NonNull AnimatedFileDrawable videoDrawable) {
        if (nonNull(decoderCallback)) {
            decoderCallback.onLoaded(videoDrawable.isDecoded());
        }
        if (!videoDrawable.isDecoded())
            return;
        drawable = videoDrawable;
        drawable.setAllowDecodeSingleFrame(true);
        setImageDrawable(drawable);
    }

    public void fromFile(@NonNull File file) {
        if (!FenrirNative.isNativeLoaded()) {
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
            return;
        }
        clearAnimationDrawable();
        setAnimation(new AnimatedFileDrawable(file, 0, defaultWidth, defaultHeight, false, () -> {
            if (nonNull(decoderCallback)) {
                decoderCallback.onLoaded(false);
            }
        }));
    }

    public void clearAnimationDrawable() {
        mDisposable.dispose();
        if (drawable != null) {
            drawable.stop();
            drawable.setCallback(null);
            drawable.recycle();
            drawable = null;
        }
        setImageDrawable(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
        if (drawable != null) {
            drawable.setCallback(this);
            if (playing) {
                drawable.start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDisposable.dispose();
        attachedToWindow = false;
        if (drawable != null) {
            drawable.stop();
            drawable.setCallback(null);
        }
    }

    public boolean isPlaying() {
        return drawable != null && drawable.isRunning();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable dr) {
        super.setImageDrawable(dr);
        if (!(dr instanceof AnimatedFileDrawable)) {
            mDisposable.dispose();
            if (drawable != null) {
                drawable.stop();
                drawable.setCallback(null);
                drawable.recycle();
                drawable = null;
            }
        }
    }

    @Override
    public void setImageBitmap(@Nullable Bitmap bm) {
        super.setImageBitmap(bm);
        mDisposable.dispose();
        if (drawable != null) {
            drawable.stop();
            drawable.setCallback(null);
            drawable.recycle();
            drawable = null;
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mDisposable.dispose();
        if (drawable != null) {
            drawable.stop();
            drawable.setCallback(null);
            drawable.recycle();
            drawable = null;
        }
    }

    public void playAnimation() {
        if (drawable == null) {
            return;
        }
        playing = true;
        if (attachedToWindow) {
            drawable.start();
        }
    }

    public void resetFrame() {
        if (drawable == null) {
            return;
        }
        playing = true;
        if (attachedToWindow) {
            drawable.seekTo(0, true);
        }
    }

    public void stopAnimation() {
        if (drawable == null) {
            return;
        }
        playing = false;
        if (attachedToWindow) {
            drawable.stop();
        }
    }

    public @Nullable
    AnimatedFileDrawable getAnimatedDrawable() {
        return drawable;
    }

    private boolean copyRes(@RawRes int rawRes) {
        try (InputStream inputStream = FenrirNative.getAppContext().getResources().openRawResource(rawRes)) {
            File out = new File(AnimationNetworkCache.Companion.parentResDir(getContext()), AnimationNetworkCache.Companion.filenameForRes(rawRes, true));
            FileOutputStream o = new FileOutputStream(out);
            byte[] buffer = bufferLocal.get();
            if (buffer == null) {
                buffer = new byte[4096];
                bufferLocal.set(buffer);
            }
            while (inputStream.read(buffer, 0, buffer.length) >= 0) {
                o.write(buffer);
            }
            o.flush();
            o.close();
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public interface onDecoderInit {
        void onLoaded(boolean success);
    }
}
