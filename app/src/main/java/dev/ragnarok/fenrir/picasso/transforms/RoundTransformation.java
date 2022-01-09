package dev.ragnarok.fenrir.picasso.transforms;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso3.RequestHandler;
import com.squareup.picasso3.Transformation;

public class RoundTransformation implements Transformation {

    private static final String TAG = RoundTransformation.class.getSimpleName();

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
        return ImageHelper.getRoundedBitmap(source);
    }

    @NonNull
    @Override
    public RequestHandler.Result.Bitmap transform(@NonNull RequestHandler.Result.Bitmap source) {
        return new RequestHandler.Result.Bitmap(ImageHelper.getRoundedBitmap(source.getBitmap()), source.loadedFrom, source.exifRotation);
    }
}
