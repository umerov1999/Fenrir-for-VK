package dev.ragnarok.fenrir.api;

import java.lang.ref.WeakReference;


public class WeakPercentagePublisher implements PercentagePublisher {

    private final WeakReference<PercentagePublisher> ref;

    public WeakPercentagePublisher(PercentagePublisher listener) {
        ref = new WeakReference<>(listener);
    }

    @Override
    public void onProgressChanged(int percentage) {
        PercentagePublisher orig = ref.get();
        if (orig != null) {
            orig.onProgressChanged(percentage);
        }
    }
}