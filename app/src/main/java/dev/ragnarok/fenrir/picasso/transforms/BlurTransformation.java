package dev.ragnarok.fenrir.picasso.transforms;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso3.RequestHandler;
import com.squareup.picasso3.Transformation;

public class BlurTransformation implements Transformation {

    private static final String TAG = BlurTransformation.class.getSimpleName();

    private final float mRadius;

    private final Context mContext;

    public BlurTransformation(@FloatRange(from = 0.0f, to = 25.0f) float radius, Context mContext) {
        mRadius = radius;
        this.mContext = mContext;
    }

    public static Bitmap blurRenderScript(Context context, Bitmap inputBitmap, @FloatRange(from = 0.0f, to = 25.0f) float radius) {
        if (radius <= 0 || inputBitmap == null) {
            return inputBitmap;
        }
        Bitmap outputBitmap = inputBitmap.copy(inputBitmap.getConfig(), true);
        RenderScript renderScript = RenderScript.create(context);
        Allocation blurInput = Allocation.createFromBitmap(renderScript, inputBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, outputBitmap);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);
        blurOutput.copyTo(outputBitmap);
        renderScript.destroy();

        return outputBitmap;
    }

    @NonNull
    @Override
    public String key() {
        return TAG + "(radius=" + mRadius + ")";
    }

    @NonNull
    @Override
    public RequestHandler.Result.Bitmap transform(@NonNull RequestHandler.Result.Bitmap src) {
        Bitmap source = src.getBitmap();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && source.getConfig() == Bitmap.Config.HARDWARE) {
            Bitmap tmpSource = source.copy(Bitmap.Config.ARGB_8888, true);
            source.recycle();
            source = tmpSource;
        }

        Bitmap bitmap = blurRenderScript(mContext, source, mRadius);
        if (source != bitmap) {
            source.recycle();
        }

        return new RequestHandler.Result.Bitmap(bitmap, src.loadedFrom, src.exifRotation);
    }

    @Nullable
    @Override
    public Bitmap localTransform(@Nullable Bitmap source) {
        if (source == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && source.getConfig() == Bitmap.Config.HARDWARE) {
            Bitmap tmpSource = source.copy(Bitmap.Config.ARGB_8888, true);
            source.recycle();
            source = tmpSource;
        }

        Bitmap bitmap = blurRenderScript(mContext, source, mRadius);
        if (source != bitmap) {
            source.recycle();
        }

        return bitmap;
    }
}
