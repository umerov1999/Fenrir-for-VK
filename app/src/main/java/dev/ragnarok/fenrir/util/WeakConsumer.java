package dev.ragnarok.fenrir.util;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.functions.Consumer;

public class WeakConsumer<T> implements Consumer<T> {

    private final WeakReference<Consumer<T>> ref;

    public WeakConsumer(Consumer<T> orig) {
        ref = new WeakReference<>(orig);
    }

    @Override
    public void accept(T t) throws Throwable {
        Consumer<T> orig = ref.get();
        if (orig != null) {
            orig.accept(t);
        }
    }
}
