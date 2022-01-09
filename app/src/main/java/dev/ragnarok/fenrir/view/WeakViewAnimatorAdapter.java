package dev.ragnarok.fenrir.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import java.lang.ref.WeakReference;

public abstract class WeakViewAnimatorAdapter<V extends View> extends AnimatorListenerAdapter {

    private final WeakReference<V> ref;

    public WeakViewAnimatorAdapter(V ref) {
        this.ref = new WeakReference<>(ref);
    }

    @Override
    public final void onAnimationEnd(Animator animation) {
        V view = ref.get();
        if (view != null) {
            onAnimationEnd(view);
        }
    }

    @Override
    public final void onAnimationStart(Animator animation) {
        V view = ref.get();
        if (view != null) {
            onAnimationStart(view);
        }
    }

    @Override
    public final void onAnimationCancel(Animator animation) {
        V view = ref.get();
        if (view != null) {
            onAnimationCancel(view);
        }
    }

    protected void onAnimationCancel(V view) {

    }

    public abstract void onAnimationEnd(V view);

    public void onAnimationStart(V view) {

    }
}