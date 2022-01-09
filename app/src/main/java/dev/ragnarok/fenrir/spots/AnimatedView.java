package dev.ragnarok.fenrir.spots;

import android.content.Context;
import android.view.View;

import androidx.annotation.Keep;

class AnimatedView extends View {

    private int target;

    public AnimatedView(Context context) {
        super(context);
    }

    public float getXFactor() {
        return getX() / target;
    }

    @Keep
    public void setXFactor(float xFactor) {
        setX(target * xFactor);
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }
}
