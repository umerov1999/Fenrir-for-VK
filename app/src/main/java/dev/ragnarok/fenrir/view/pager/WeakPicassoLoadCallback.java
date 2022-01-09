package dev.ragnarok.fenrir.view.pager;

import androidx.annotation.NonNull;

import com.squareup.picasso3.Callback;

import java.lang.ref.WeakReference;

public class WeakPicassoLoadCallback implements Callback {

    private final WeakReference<Callback> mReference;

    public WeakPicassoLoadCallback(Callback baseCallback) {
        mReference = new WeakReference<>(baseCallback);
    }

    @Override
    public void onSuccess() {
        Callback callback = mReference.get();
        if (callback != null) {
            callback.onSuccess();
        }
    }

    @Override
    public void onError(@NonNull Throwable t) {
        Callback callback = mReference.get();
        if (callback != null) {
            callback.onError(t);
        }
    }

}
