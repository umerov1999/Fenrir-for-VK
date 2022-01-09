package dev.ragnarok.fenrir.view.media;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MaterialPlayPauseFab extends FloatingActionButton {

    private MediaActionDrawable mDrawable;

    public MaterialPlayPauseFab(Context context) {
        super(context);

        init();
    }

    public MaterialPlayPauseFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaterialPlayPauseFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDrawable = new MediaActionDrawable();
        setImageDrawable(mDrawable);
        setScaleType(ScaleType.FIT_XY);
    }

    public void setIcon(int icon, boolean anim) {
        mDrawable.setIcon(icon, anim);
    }

    public void setProgress(int percent, boolean anim) {
        mDrawable.setProgress(percent, anim);
    }
}
