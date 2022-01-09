package dev.ragnarok.fenrir.view.natives.rlottie;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.appcompat.widget.AppCompatImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.rlottie.RLottieDrawable;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RLottieImageView extends AppCompatImageView {

    private final NetworkCache cache;
    private HashMap<String, Integer> layerColors;
    private RLottieDrawable drawable;
    private boolean autoRepeat;
    private boolean attachedToWindow;
    private boolean playing;
    private Disposable mDisposable = Disposable.disposed();

    public RLottieImageView(Context context) {
        this(context, null);
    }

    public RLottieImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cache = new NetworkCache(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RLottieImageView);
        int animRes = a.getResourceId(R.styleable.RLottieImageView_fromRes, 0);
        autoRepeat = a.getBoolean(R.styleable.RLottieImageView_loopAnimation, false);
        int width = (int) a.getDimension(R.styleable.RLottieImageView_w, 28);
        int height = (int) a.getDimension(R.styleable.RLottieImageView_h, 28);
        a.recycle();

        if (FenrirNative.isNativeLoaded() && animRes != 0) {
            drawable = new RLottieDrawable(animRes, "" + animRes, width, height, false, null, false);
            setAnimation(drawable);
            playAnimation();
        }
    }

    public void clearLayerColors() {
        layerColors.clear();
    }

    public void setLayerColor(String layer, int color) {
        if (layerColors == null) {
            layerColors = new HashMap<>();
        }
        layerColors.put(layer, color);
        if (drawable != null) {
            drawable.setLayerColor(layer, color);
        }
    }

    public void replaceColors(int[] colors) {
        if (drawable != null) {
            drawable.replaceColors(colors);
        }
    }

    private void setAnimationByUrlCache(String url, int w, int h) {
        if (!FenrirNative.isNativeLoaded()) {
            return;
        }
        File ch = cache.fetch(url);
        if (ch == null) {
            setImageDrawable(null);
            return;
        }
        autoRepeat = false;
        setAnimation(new RLottieDrawable(ch, true, w, h, false, false, null, false));
        playAnimation();
    }

    public void fromNet(String url, OkHttpClient.Builder client, int w, int h) {
        if (!FenrirNative.isNativeLoaded() || url == null || url.isEmpty()) {
            return;
        }
        clearAnimationDrawable();
        if (cache.isCachedFile(url)) {
            setAnimationByUrlCache(url, w, h);
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
                cache.writeTempCacheFile(url, input);
                input.close();
                cache.renameTempFile(url);
            } catch (Exception e) {
                u.onSuccess(false);
                return;
            }
            u.onSuccess(true);
        }).compose(RxUtils.applySingleComputationToMainSchedulers()).subscribe(u -> {
            if (u) {
                setAnimationByUrlCache(url, w, h);
            }
        }, RxUtils.ignore());
    }

    private void setAnimation(@NonNull RLottieDrawable rLottieDrawable) {
        drawable = rLottieDrawable;
        drawable.setAutoRepeat(autoRepeat ? 1 : 0);
        if (layerColors != null) {
            drawable.beginApplyLayerColors();
            for (HashMap.Entry<String, Integer> entry : layerColors.entrySet()) {
                drawable.setLayerColor(entry.getKey(), entry.getValue());
            }
            drawable.commitApplyLayerColors();
        }
        drawable.setAllowDecodeSingleFrame(true);
        drawable.setCurrentParentView(this);
        setImageDrawable(drawable);
    }

    public void fromRes(@RawRes int resId, int w, int h) {
        fromRes(resId, w, h, null, false);
    }

    public void fromRes(@RawRes int resId, int w, int h, int[] colorReplacement) {
        fromRes(resId, w, h, colorReplacement, false);
    }

    public void fromRes(@RawRes int resId, int w, int h, int[] colorReplacement, boolean useMoveColor) {
        if (!FenrirNative.isNativeLoaded()) {
            return;
        }
        clearAnimationDrawable();
        setAnimation(new RLottieDrawable(resId, "res_" + resId, w, h, false, colorReplacement, useMoveColor));
    }

    public void fromFile(@NonNull File file, int w, int h) {
        if (!FenrirNative.isNativeLoaded()) {
            return;
        }
        clearAnimationDrawable();
        setAnimation(new RLottieDrawable(file, false, w, h, false, false, null, false));
    }

    public void clearAnimationDrawable() {
        mDisposable.dispose();
        if (drawable != null) {
            drawable.stop();
            drawable.setCurrentParentView(null);
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
            drawable.setCurrentParentView(this);
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
            drawable.setCurrentParentView(null);
        }
    }

    public boolean isPlaying() {
        return drawable != null && drawable.isRunning();
    }

    public void setAutoRepeat(boolean repeat) {
        autoRepeat = repeat;
    }

    public void setProgress(float progress) {
        if (drawable == null) {
            return;
        }
        drawable.setProgress(progress);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable dr) {
        super.setImageDrawable(dr);
        if (!(dr instanceof RLottieDrawable)) {
            mDisposable.dispose();
            if (drawable != null) {
                drawable.stop();
                drawable.setCurrentParentView(null);
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
            drawable.setCurrentParentView(null);
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
            drawable.setCurrentParentView(null);
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

    public void replayAnimation() {
        if (drawable == null) {
            return;
        }
        playing = true;
        if (attachedToWindow) {
            drawable.stop();
            drawable.setAutoRepeat(1);
            drawable.start();
        }
    }

    public void resetFrame() {
        if (drawable == null) {
            return;
        }
        playing = true;
        if (attachedToWindow) {
            drawable.setAutoRepeat(1);
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
    RLottieDrawable getAnimatedDrawable() {
        return drawable;
    }
}
