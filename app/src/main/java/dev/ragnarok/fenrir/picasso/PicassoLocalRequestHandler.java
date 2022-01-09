package dev.ragnarok.fenrir.picasso;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;

import com.squareup.picasso3.Picasso;
import com.squareup.picasso3.Request;
import com.squareup.picasso3.RequestHandler;

import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.util.Objects;

public class PicassoLocalRequestHandler extends RequestHandler {

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && data.uri.getPath() != null && data.uri.getLastPathSegment() != null && data.uri.getScheme() != null && data.uri.getScheme().equals("content");
    }

    @Override
    public void load(@NonNull Picasso picasso, @NonNull Request request, @NonNull Callback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Bitmap target = Stores.getInstance().localMedia().getThumbnail(request.uri, 256, 256);
            if (Objects.isNull(target)) {
                callback.onError(new Throwable("Picasso Thumb Not Support"));
            } else {
                callback.onSuccess(new Result.Bitmap(target, Picasso.LoadedFrom.DISK));
            }
        } else {
            long contentId = Long.parseLong(request.uri.getLastPathSegment());
            @Content_Local int ret;
            if (request.uri.getPath().contains("videos")) {
                ret = Content_Local.VIDEO;
            } else if (request.uri.getPath().contains("images")) {
                ret = Content_Local.PHOTO;
            } else if (request.uri.getPath().contains("audios")) {
                ret = Content_Local.AUDIO;
            } else {
                callback.onError(new Throwable("Picasso Thumb Not Support"));
                return;
            }
            Bitmap target = Stores.getInstance().localMedia().getOldThumbnail(ret, contentId);
            if (Objects.isNull(target)) {
                callback.onError(new Throwable("Picasso Thumb Not Support"));
                return;
            }
            callback.onSuccess(new Result.Bitmap(target, Picasso.LoadedFrom.DISK));
        }
    }
}
