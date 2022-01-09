package dev.ragnarok.fenrir.picasso.transforms;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso3.RequestHandler;
import com.squareup.picasso3.Transformation;

public class PolyTransformation implements Transformation {

    private static final String TAG = PolyTransformation.class.getSimpleName();

    @NonNull
    @Override
    public String key() {
        return TAG + "()";
    }

    @Nullable
    @Override
    public Bitmap localTransform(@Nullable Bitmap source) {
        if (source == null) {
            return null;
        }
        return ImageHelper.getEllipseBitmap(source, 0.1f);
    }

    @NonNull
    @Override
    public RequestHandler.Result.Bitmap transform(@NonNull RequestHandler.Result.Bitmap source) {
        return new RequestHandler.Result.Bitmap(ImageHelper.getEllipseBitmap(source.getBitmap(), 0.1f), source.loadedFrom, source.exifRotation);
    }
}
